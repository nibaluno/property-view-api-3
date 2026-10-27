#!/usr/bin/env python3
"""
Executes the 3 Load Testing runs defined in docs/LOAD_TEST_PLAN.md against the live
Spring Boot application:
  1. Baseline Run (1 concurrent user)
  2. Target Load Test (50 concurrent users)
  3. Stress & Recovery Test (stepping 1 -> 10 -> 25 -> 50 -> 100 -> 150 -> 250 concurrent users,
     plus recovery at 20 users)
Generates:
  - docs/evidence/load-test-results.json
  - docs/evidence/load-test-response-time.png (Chart of Response Time p50/p95/p99 & RPS vs Users)
"""
import concurrent.futures
import datetime
import json
import math
import os
import random
import statistics
import struct
import subprocess
import time
import urllib.error
import urllib.request
import uuid
import zlib

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
EVIDENCE_DIR = os.path.join(ROOT, "docs", "evidence")
os.makedirs(EVIDENCE_DIR, exist_ok=True)
BASE_URL = "http://localhost:8092/property-view"


def http_call(method, path, payload=None):
    url = f"{BASE_URL}{path}"
    headers = {"Accept": "application/json"}
    data = None
    if payload is not None:
        data = json.dumps(payload).encode("utf-8")
        headers["Content-Type"] = "application/json"
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    t0 = time.perf_counter()
    try:
        with urllib.request.urlopen(req, timeout=4.0) as resp:
            resp.read()
            ms = (time.perf_counter() - t0) * 1000.0
            return resp.status, ms, False
    except urllib.error.HTTPError as e:
        e.read()
        ms = (time.perf_counter() - t0) * 1000.0
        is_err = e.code >= 500
        return e.code, ms, is_err
    except Exception:
        ms = (time.perf_counter() - t0) * 1000.0
        return 0, ms, True


def run_single_workload_action(seed_ids, stress_mode=False):
    r = random.random()
    if r < 0.30:
        city = random.choice(["Minsk", "Moscow", "Brest", "Grodno"])
        return http_call("GET", f"/search?city={city}")
    elif r < 0.55:
        return http_call("GET", "/hotels")
    elif r < 0.75:
        hid = random.choice(seed_ids)
        return http_call("GET", f"/hotels/{hid}")
    elif r < 0.90:
        param = random.choice(["city", "brand", "country", "amenities"])
        return http_call("GET", f"/histogram/{param}")
    elif r < 0.95:
        uid = uuid.uuid4().hex[:6]
        body = {
            "name": f"LoadHotel {uid}",
            "brand": random.choice(["Hilton", "Marriott", "Accor"]),
            "address": {"houseNumber": 10, "street": "Main", "city": random.choice(["Minsk", "Moscow"]), "country": "Belarus", "postCode": "220000"},
            "contacts": {"phone": "+375 17 000-00-00", "email": f"u{uid}@test.com"},
            "arrivalTime": {"checkIn": "14:00", "checkOut": "12:00"}
        }
        return http_call("POST", "/hotels", body)
    else:
        hid = random.choice(seed_ids)
        if stress_mode and random.random() < 0.35:
            # In stress mode, concurrent threads occasionally introduce a shared new amenity name,
            # exercising the DB unique index uq_amenity_normalized_name (DEF-01)
            shared_name = f"StressSharedAmenity_{int(time.time() * 2)}"
            return http_call("POST", f"/hotels/{hid}/amenities", [shared_name, "Free WiFi"])
        return http_call("POST", f"/hotels/{hid}/amenities", ["Free WiFi", "Pool", "Parking"])


def percentile(sorted_vals, pct):
    if not sorted_vals:
        return 0.0
    idx = min(len(sorted_vals) - 1, max(0, int(math.ceil((pct / 100.0) * len(sorted_vals))) - 1))
    return round(sorted_vals[idx], 2)


def execute_stage(stage_name, users, total_requests, seed_ids, think_time_sec=0.02, stress_mode=False):
    latencies = []
    errors = 0

    def worker(n_iters):
        local_lat = []
        local_err = 0
        for _ in range(n_iters):
            _, ms, is_err = run_single_workload_action(seed_ids, stress_mode=stress_mode)
            local_lat.append(ms)
            if is_err:
                local_err += 1
            if think_time_sec > 0:
                time.sleep(think_time_sec)
        return local_lat, local_err

    iters_per_user = max(1, total_requests // users)
    actual_total = iters_per_user * users
    t_start = time.perf_counter()
    with concurrent.futures.ThreadPoolExecutor(max_workers=users) as executor:
        futures = [executor.submit(worker, iters_per_user) for _ in range(users)]
        for fut in concurrent.futures.as_completed(futures):
            l_lat, l_err = fut.result()
            latencies.extend(l_lat)
            errors += l_err
    duration = max(0.001, time.perf_counter() - t_start)
    latencies.sort()
    rps = round(actual_total / duration, 2)
    err_rate = round((errors / actual_total) * 100.0, 2)
    return {
        "stage": stage_name,
        "users": users,
        "totalRequests": actual_total,
        "durationSec": round(duration, 2),
        "rps": rps,
        "avgMs": round(statistics.mean(latencies), 2),
        "p50Ms": percentile(latencies, 50),
        "p95Ms": percentile(latencies, 95),
        "p99Ms": percentile(latencies, 99),
        "maxMs": round(latencies[-1], 2),
        "errorsCount": errors,
        "errorRatePercent": err_rate
    }


def draw_response_time_chart_png(filepath, stages):
    """Renders a clean visual chart of Response Time (p50, p95, p99) & SLA threshold vs Virtual Users."""
    width, height = 960, 540
    canvas = [[(255, 255, 255) for _ in range(width)] for _ in range(height)]

    def set_px(x, y, color):
        if 0 <= x < width and 0 <= y < height:
            canvas[y][x] = color

    def fill_rect(x0, y0, x1, y1, color):
        for yy in range(max(0, y0), min(height, y1 + 1)):
            for xx in range(max(0, x0), min(width, x1 + 1)):
                canvas[yy][xx] = color

    def draw_line(x0, y0, x1, y1, color, thickness=2):
        steps = max(abs(x1 - x0), abs(y1 - y0), 1)
        for i in range(steps + 1):
            x = int(round(x0 + (x1 - x0) * (i / steps)))
            y = int(round(y0 + (y1 - y0) * (i / steps)))
            for dy in range(-thickness, thickness + 1):
                for dx in range(-thickness, thickness + 1):
                    set_px(x + dx, y + dy, color)

    # Background & header bar
    fill_rect(0, 0, width - 1, height - 1, (248, 250, 252))
    fill_rect(0, 0, width - 1, 48, (30, 58, 138))
    fill_rect(60, 70, width - 40, height - 60, (255, 255, 255))

    plot_x0, plot_y0, plot_x1, plot_y1 = 80, 90, width - 60, height - 80
    plot_w = plot_x1 - plot_x0
    plot_h = plot_y1 - plot_y0

    # Horizontal grid lines
    for i in range(6):
        gy = plot_y1 - int((i / 5.0) * plot_h)
        draw_line(plot_x0, gy, plot_x1, gy, (226, 232, 240), thickness=1)

    # Axes
    draw_line(plot_x0, plot_y0, plot_x0, plot_y1, (71, 85, 105), thickness=2)
    draw_line(plot_x0, plot_y1, plot_x1, plot_y1, (71, 85, 105), thickness=2)

    max_y = max(max(s["p99Ms"] for s in stages) * 1.15, 140.0)

    # SLA 100ms horizontal dashed red line
    sla_y = plot_y1 - int((100.0 / max_y) * plot_h)
    if plot_y0 <= sla_y <= plot_y1:
        for xx in range(plot_x0, plot_x1, 12):
            draw_line(xx, sla_y, min(xx + 6, plot_x1), sla_y, (220, 38, 38), thickness=1)

    n = len(stages)
    pts_avg, pts_p50, pts_p95, pts_p99 = [], [], [], []
    for idx, st in enumerate(stages):
        px = plot_x0 + int((idx / max(1, n - 1)) * plot_w)
        pts_avg.append((px, plot_y1 - int((min(st["avgMs"], max_y) / max_y) * plot_h)))
        pts_p50.append((px, plot_y1 - int((min(st["p50Ms"], max_y) / max_y) * plot_h)))
        pts_p95.append((px, plot_y1 - int((min(st["p95Ms"], max_y) / max_y) * plot_h)))
        pts_p99.append((px, plot_y1 - int((min(st["p99Ms"], max_y) / max_y) * plot_h)))

    for series, color in [
        (pts_p50, (21, 128, 61)),   # Green: p50
        (pts_avg, (37, 99, 235)),   # Blue: avg
        (pts_p95, (234, 88, 12)),   # Orange: p95
        (pts_p99, (185, 28, 28)),   # Red: p99
    ]:
        for i in range(len(series) - 1):
            draw_line(series[i][0], series[i][1], series[i + 1][0], series[i + 1][1], color, thickness=2)
        for (px, py) in series:
            fill_rect(px - 4, py - 4, px + 4, py + 4, color)

    raw_rows = bytearray()
    for y in range(height):
        raw_rows.append(0)
        for x in range(width):
            raw_rows.extend(canvas[y][x])

    def chunk(tag, data):
        return struct.pack("!I", len(data)) + tag + data + struct.pack("!I", zlib.crc32(tag + data) & 0xFFFFFFFF)

    ihdr = struct.pack("!IIBBBBB", width, height, 8, 2, 0, 0, 0)
    idat = zlib.compress(bytes(raw_rows), 6)
    with open(filepath, "wb") as f:
        f.write(b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr) + chunk(b"IDAT", idat) + chunk(b"IEND", b""))


def main():
    jar_path = os.path.join(ROOT, "target", "property-view-api-0.0.1-SNAPSHOT.jar")
    java_bin = "/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin/java"
    if not os.path.exists(java_bin):
        java_bin = "java"

    proc = subprocess.Popen(
        [java_bin, "-jar", jar_path, "--spring.datasource.hikari.maximum-pool-size=10"],
        cwd=ROOT,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
    )

    try:
        for _ in range(60):
            st, _, _ = http_call("GET", "/hotels")
            if st == 200:
                break
            time.sleep(0.5)
        else:
            raise RuntimeError("Server did not start on port 8092")

        # Seed 20 initial hotels with amenities
        seed_ids = []
        for i in range(20):
            body = {
                "name": f"Seed Hotel {i+1}",
                "brand": "Hilton" if i % 2 == 0 else "Marriott",
                "address": {"houseNumber": i + 1, "street": "Avenue", "city": "Minsk" if i % 2 == 0 else "Moscow", "country": "Belarus", "postCode": "220004"},
                "contacts": {"phone": "+375 17 309-80-00", "email": f"seed{i}@hilton.com"},
                "arrivalTime": {"checkIn": "14:00", "checkOut": "12:00"}
            }
            st, _, _ = http_call("POST", "/hotels", body)
            if st == 201:
                seed_ids.append(i + 1)
                http_call("POST", f"/hotels/{i+1}/amenities", ["Free WiFi", "Free parking", "Concierge"])

        # Warmup
        execute_stage("Warmup", users=5, total_requests=50, seed_ids=seed_ids, think_time_sec=0.0)

        # 1. Baseline Run (1 user)
        baseline = execute_stage("1. Baseline (1 user)", users=1, total_requests=150, seed_ids=seed_ids, think_time_sec=0.005)

        # 2. Target Load Run (50 users)
        target_load = execute_stage("2. Target Load (50 users)", users=50, total_requests=600, seed_ids=seed_ids, think_time_sec=0.02)

        # 3. Stress & Recovery Ramp (10 -> 25 -> 50 -> 100 -> 150 -> 250 -> Recovery 20)
        stress_stages = [
            baseline,
            execute_stage("Stress Step 10u", users=10, total_requests=200, seed_ids=seed_ids, think_time_sec=0.01),
            execute_stage("Stress Step 25u", users=25, total_requests=300, seed_ids=seed_ids, think_time_sec=0.005),
            target_load,
            execute_stage("Stress Step 100u", users=100, total_requests=600, seed_ids=seed_ids, think_time_sec=0.0, stress_mode=True),
            execute_stage("Stress Step 150u", users=150, total_requests=750, seed_ids=seed_ids, think_time_sec=0.0, stress_mode=True),
            execute_stage("Stress Peak 250u", users=250, total_requests=1000, seed_ids=seed_ids, think_time_sec=0.0, stress_mode=True),
        ]
        recovery = execute_stage("4. Recovery (20 users after stress)", users=20, total_requests=300, seed_ids=seed_ids, think_time_sec=0.01)

        report_data = {
            "executedAt": datetime.datetime.now().isoformat(),
            "baselineRun": baseline,
            "targetLoadRun": target_load,
            "stressStages": stress_stages,
            "recoveryRun": recovery
        }
        results_json_path = os.path.join(EVIDENCE_DIR, "load-test-results.json")
        with open(results_json_path, "w", encoding="utf-8") as f:
            json.dump(report_data, f, indent=2, ensure_ascii=False)

        chart_path = os.path.join(EVIDENCE_DIR, "load-test-response-time.png")
        draw_response_time_chart_png(chart_path, stress_stages)

        print(json.dumps(report_data, indent=2))
    finally:
        proc.terminate()
        try:
            proc.wait(timeout=5)
        except Exception:
            proc.kill()


if __name__ == "__main__":
    main()
