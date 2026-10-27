"""
Locust load testing script for Property View API (Assignment 5).
Implements the exact workload profile defined in docs/LOAD_TEST_PLAN.md:
- 30% GET /property-view/search
- 25% GET /property-view/hotels
- 20% GET /property-view/hotels/{id}
- 15% GET /property-view/histogram/{param}
-  5% POST /property-view/hotels
-  5% POST /property-view/hotels/{id}/amenities

Run with Locust CLI:
  locust -f load-tests/locustfile.py --host=http://localhost:8092 --headless -u 50 -r 10 -t 30s
"""
import random
import uuid
from locust import HttpUser, task, between


class PropertyViewUser(HttpUser):
    wait_time = between(0.1, 0.5)
    known_hotel_ids = [1, 2]

    def on_start(self):
        # Ensure at least one baseline hotel exists
        payload = {
            "name": "DoubleTree by Hilton Minsk",
            "description": "Baseline load test hotel",
            "brand": "Hilton",
            "address": {
                "houseNumber": 9,
                "street": "Pobediteley Avenue",
                "city": "Minsk",
                "country": "Belarus",
                "postCode": "220004"
            },
            "contacts": {
                "phone": "+375 17 309-80-00",
                "email": "loadtest@hilton.com"
            },
            "arrivalTime": {"checkIn": "14:00", "checkOut": "12:00"}
        }
        with self.client.post("/property-view/hotels", json=payload, catch_response=True, name="POST /hotels (seed)") as resp:
            if resp.status_code == 201:
                hid = resp.json().get("id")
                if hid and hid not in self.known_hotel_ids:
                    self.known_hotel_ids.append(hid)

    @task(30)
    def search_hotels(self):
        city = random.choice(["Minsk", "Moscow", "Grodno", "Brest"])
        self.client.get(f"/property-view/search?city={city}", name="GET /property-view/search")

    @task(25)
    def list_hotels(self):
        self.client.get("/property-view/hotels", name="GET /property-view/hotels")

    @task(20)
    def get_hotel_details(self):
        hid = random.choice(self.known_hotel_ids)
        self.client.get(f"/property-view/hotels/{hid}", name="GET /property-view/hotels/{id}")

    @task(15)
    def get_histogram(self):
        param = random.choice(["city", "brand", "country", "amenities"])
        self.client.get(f"/property-view/histogram/{param}", name="GET /property-view/histogram/{param}")

    @task(5)
    def create_hotel(self):
        suffix = uuid.uuid4().hex[:8]
        payload = {
            "name": f"LoadTest Hotel {suffix}",
            "brand": random.choice(["Hilton", "Marriott", "Accor", "Radisson"]),
            "address": {
                "houseNumber": random.randint(1, 200),
                "street": "Load Test Street",
                "city": random.choice(["Minsk", "Moscow", "Brest", "Vitebsk"]),
                "country": "Belarus",
                "postCode": "220000"
            },
            "contacts": {
                "phone": "+375 17 111-22-33",
                "email": f"hotel.{suffix}@example.com"
            },
            "arrivalTime": {"checkIn": "14:00", "checkOut": "12:00"}
        }
        with self.client.post("/property-view/hotels", json=payload, name="POST /property-view/hotels") as resp:
            if resp.status_code == 201:
                hid = resp.json().get("id")
                if hid and len(self.known_hotel_ids) < 50:
                    self.known_hotel_ids.append(hid)

    @task(5)
    def add_amenities(self):
        hid = random.choice(self.known_hotel_ids)
        amenities = random.sample(["Free WiFi", "Free parking", "Pool", "Fitness center", "Concierge", "Sauna"], 2)
        self.client.post(f"/property-view/hotels/{hid}/amenities", json=amenities, name="POST /property-view/hotels/{id}/amenities")
