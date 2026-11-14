import { Injectable, signal } from '@angular/core';

export interface CountryPreference {
  country: string;
  countryCode: string;
  locale: string;
  currency: string;
}

export const COUNTRY_OPTIONS: CountryPreference[] = [
  { country: 'India', countryCode: 'IN', locale: 'en-IN', currency: 'INR' },
  { country: 'United States', countryCode: 'US', locale: 'en-US', currency: 'USD' },
  { country: 'United Kingdom', countryCode: 'GB', locale: 'en-GB', currency: 'GBP' },
  { country: 'Germany', countryCode: 'DE', locale: 'de-DE', currency: 'EUR' },
  { country: 'France', countryCode: 'FR', locale: 'fr-FR', currency: 'EUR' },
  { country: 'Canada', countryCode: 'CA', locale: 'en-CA', currency: 'CAD' },
  { country: 'Australia', countryCode: 'AU', locale: 'en-AU', currency: 'AUD' },
  { country: 'Japan', countryCode: 'JP', locale: 'ja-JP', currency: 'JPY' },
  { country: 'United Arab Emirates', countryCode: 'AE', locale: 'en-AE', currency: 'AED' },
  { country: 'Singapore', countryCode: 'SG', locale: 'en-SG', currency: 'SGD' },
  { country: 'Switzerland', countryCode: 'CH', locale: 'de-CH', currency: 'CHF' },
];

@Injectable({
  providedIn: 'root',
})
export class PreferencesService {
  private readonly storageKey = 'orderflow-preferences';

  readonly selectedCountry = signal<CountryPreference>(this.getInitialPreference());

  setCountry(countryCode: string): void {
    const selected = COUNTRY_OPTIONS.find((option) => option.countryCode === countryCode);

    if (!selected) return;

    this.selectedCountry.set(selected);

    localStorage.setItem(this.storageKey, JSON.stringify(selected));
  }

  formatCurrency(amount: number): string {
    const preference = this.selectedCountry();

    return new Intl.NumberFormat(preference.locale, {
      style: 'currency',
      currency: preference.currency,
    }).format(amount);
  }

  private getInitialPreference(): CountryPreference {
    const saved = localStorage.getItem(this.storageKey);

    if (saved) {
      try {
        const parsed = JSON.parse(saved) as CountryPreference;

        const valid = COUNTRY_OPTIONS.find((option) => option.countryCode === parsed.countryCode);

        if (valid) return valid;
      } catch {
        // Fall back to India if saved preferences are invalid.
      }
    }

    return COUNTRY_OPTIONS[0];
  }
}
