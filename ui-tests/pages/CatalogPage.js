const { expect } = require('@playwright/test');

/**
 * Page Object representing the Property View Catalog & Analytics page (/property-view/index.html).
 * Encapsulates locators and user actions with explicit Playwright waits (zero fixed sleep pauses).
 */
class CatalogPage {
  /**
   * @param {import('@playwright/test').Page} page
   */
  constructor(page) {
    this.page = page;
    this.logoLink = page.locator('#logo-link');
    this.searchNameInput = page.locator('#search-name');
    this.searchBrandInput = page.locator('#search-brand');
    this.searchCityInput = page.locator('#search-city');
    this.searchCountryInput = page.locator('#search-country');
    this.searchAmenitiesInput = page.locator('#search-amenities');
    this.searchSubmitBtn = page.locator('#search-submit-btn');
    this.searchResetBtn = page.locator('#search-reset-btn');

    this.hotelNameInput = page.locator('#hotel-name');
    this.hotelBrandInput = page.locator('#hotel-brand');
    this.hotelDescriptionInput = page.locator('#hotel-description');
    this.hotelHouseInput = page.locator('#hotel-house');
    this.hotelStreetInput = page.locator('#hotel-street');
    this.hotelCityInput = page.locator('#hotel-city');
    this.hotelCountryInput = page.locator('#hotel-country');
    this.hotelPostcodeInput = page.locator('#hotel-postcode');
    this.hotelPhoneInput = page.locator('#hotel-phone');
    this.hotelEmailInput = page.locator('#hotel-email');
    this.hotelCheckinInput = page.locator('#hotel-checkin');
    this.createHotelBtn = page.locator('#create-hotel-btn');

    this.statusBanner = page.locator('#status-banner');
    this.hotelsGrid = page.locator('#hotels-grid');
    this.emptyState = page.locator('#empty-state');
    this.detailsPanel = page.locator('#hotel-details-panel');
    this.detailsTitle = page.locator('#details-title');
    this.detailsAmenities = page.locator('#details-amenities');
    this.amenityInput = page.locator('#amenity-input');
    this.addAmenityBtn = page.locator('#add-amenity-btn');
    this.histogramOutput = page.locator('#histogram-output');
  }

  async open() {
    await this.page.goto('/property-view/index.html');
    await expect(this.logoLink).toBeVisible();
  }

  async createHotel({ name, brand, description, houseNumber = '9', street = 'Pobediteley Ave', city = 'Minsk', country = 'Belarus', postCode = '220004', phone = '+375 17 309-80-00', email = 'info@hilton.com', checkIn = '14:00' }) {
    await this.hotelNameInput.fill(name);
    await this.hotelBrandInput.fill(brand);
    await this.hotelDescriptionInput.fill(description || '');
    await this.hotelHouseInput.fill(String(houseNumber));
    await this.hotelStreetInput.fill(street);
    await this.hotelCityInput.fill(city);
    await this.hotelCountryInput.fill(country);
    await this.hotelPostcodeInput.fill(postCode);
    await this.hotelPhoneInput.fill(phone);
    await this.hotelEmailInput.fill(email);
    await this.hotelCheckinInput.fill(checkIn);

    const responsePromise = this.page.waitForResponse(
      res => res.url().includes('/property-view/hotels') && res.request().method() === 'POST'
    );
    await this.createHotelBtn.click();
    const response = await responsePromise;
    expect(response.status()).toBe(201);
    await expect(this.statusBanner).toContainText(`Created hotel "${name}"`);
  }

  async searchBy({ name, brand, city, country, amenities }) {
    await this.searchNameInput.fill(name || '');
    await this.searchBrandInput.fill(brand || '');
    await this.searchCityInput.fill(city || '');
    await this.searchCountryInput.fill(country || '');
    await this.searchAmenitiesInput.fill(amenities || '');

    const searchPromise = this.page.waitForResponse(
      res => res.url().includes('/property-view/search') && res.status() === 200
    );
    await this.searchSubmitBtn.click();
    await searchPromise;
  }

  async openHotelDetailsByName(hotelName) {
    const card = this.hotelsGrid.locator('.hotel-card', { hasText: hotelName });
    await expect(card).toBeVisible();
    await card.locator('.view-details-btn').click();
    await expect(this.detailsPanel).toBeVisible();
    await expect(this.detailsTitle).toContainText(hotelName);
  }

  async addAmenitiesToSelectedHotel(amenitiesCsv) {
    await this.amenityInput.fill(amenitiesCsv);
    const amenityPromise = this.page.waitForResponse(
      res => res.url().includes('/amenities') && res.request().method() === 'POST'
    );
    await this.addAmenityBtn.click();
    await amenityPromise;
    await expect(this.statusBanner).toContainText('Amenities updated');
  }

  async loadHistogram(param) {
    const btn = this.page.locator(`.hist-btn[data-param="${param}"]`);
    const histPromise = this.page.waitForResponse(
      res => res.url().includes(`/property-view/histogram/${param}`) && res.status() === 200
    );
    await btn.click();
    await histPromise;
  }
}

module.exports = { CatalogPage };
