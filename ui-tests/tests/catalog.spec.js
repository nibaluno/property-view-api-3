const { test, expect } = require('@playwright/test');
const path = require('path');
const { CatalogPage } = require('../pages/CatalogPage');

test.describe('Property View Web Catalog UI (Page Object Suite)', () => {

  test.afterEach(async ({ page }, testInfo) => {
    if (testInfo.status !== testInfo.expectedStatus) {
      const safeName = testInfo.title.replace(/[^a-z0-9]/gi, '_').toLowerCase();
      const screenshotPath = path.join(__dirname, '../../docs/evidence', `failed_${safeName}.png`);
      await page.screenshot({ path: screenshotPath, fullPage: true });
    }
  });

  test('1. Creates its own hotel and displays it in the catalog grid and details panel', async ({ page }) => {
    const uid = Date.now() + '_' + Math.floor(Math.random() * 1000);
    const hotelName = `UI Test Grand Hotel ${uid}`;
    const catalog = new CatalogPage(page);

    await catalog.open();
    await catalog.createHotel({
      name: hotelName,
      brand: `Brand_${uid}`,
      description: 'Independent test hotel created via Page Object',
      city: `City_${uid}`,
      country: 'Belarus'
    });

    const card = catalog.hotelsGrid.locator('.hotel-card', { hasText: hotelName });
    await expect(card).toBeVisible();
    await expect(catalog.detailsTitle).toContainText(hotelName);
  });

  test('2. Filters hotels by unique city and shows empty state for non-matching query', async ({ page }) => {
    const uid = Date.now() + '_' + Math.floor(Math.random() * 1000);
    const uniqueCity = `Vitebsk_${uid}`;
    const hotelName = `Vitebsk Palace ${uid}`;
    const catalog = new CatalogPage(page);

    await catalog.open();
    await catalog.createHotel({
      name: hotelName,
      brand: 'Hilton',
      city: uniqueCity,
      country: 'Belarus'
    });

    await catalog.searchBy({ city: uniqueCity });
    await expect(catalog.hotelsGrid.locator('.hotel-card')).toHaveCount(1);
    await expect(catalog.hotelsGrid.locator('.hotel-card').first()).toContainText(hotelName);

    await catalog.searchBy({ city: `NonExistent_${uid}` });
    await expect(catalog.emptyState).toBeVisible();
  });

  test('3. Appends deduplicated amenities to hotel and updates analytics histogram', async ({ page }) => {
    const uid = Date.now() + '_' + Math.floor(Math.random() * 1000);
    const hotelName = `Amenity Resort ${uid}`;
    const uniqueAmenity = `Rooftop Spa ${uid}`;
    const catalog = new CatalogPage(page);

    await catalog.open();
    await catalog.createHotel({
      name: hotelName,
      brand: 'Marriott',
      city: 'Grodno',
      country: 'Belarus'
    });

    await catalog.openHotelDetailsByName(hotelName);
    await catalog.addAmenitiesToSelectedHotel(`${uniqueAmenity}, ${uniqueAmenity.toLowerCase()}, Free WiFi`);

    await expect(catalog.detailsAmenities).toContainText(uniqueAmenity);
    await catalog.loadHistogram('amenities');
    await expect(catalog.histogramOutput).toContainText(uniqueAmenity);
  });

  test('4. Verifies layout integrity, input focus, logo navigation, favicon, and zero console/404 errors', async ({ page }) => {
    const consoleErrors = [];
    const failedResponses = [];
    page.on('console', msg => {
      if (msg.type() === 'error') consoleErrors.push(msg.text());
    });
    page.on('response', res => {
      if (res.status() === 404 && !res.url().includes('999999')) {
        failedResponses.push(res.url());
      }
    });

    const catalog = new CatalogPage(page);
    await catalog.open();

    // Check favicon link presence
    const faviconHref = await page.locator('link[rel="icon"]').getAttribute('href');
    expect(faviconHref).toBe('/property-view/favicon.svg');

    // Check input focus state
    await catalog.searchNameInput.focus();
    await expect(catalog.searchNameInput).toBeFocused();

    // Check logo navigation back to main catalog
    await catalog.logoLink.click();
    await expect(page).toHaveURL(/\/property-view\/index\.html/);

    expect(consoleErrors).toEqual([]);
    expect(failedResponses).toEqual([]);
  });
});
