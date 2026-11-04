import { Component, inject } from '@angular/core';
import { LucideAngularModule, Menu, Sun, Moon, Globe } from 'lucide-angular';
import { ThemeService } from '../../core/services/theme.service';
import { PreferencesService, COUNTRY_OPTIONS } from '../../core/services/preferences.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [LucideAngularModule],
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class HeaderComponent {
  readonly themeService = inject(ThemeService);
  readonly preferencesService = inject(PreferencesService);

  readonly countries = COUNTRY_OPTIONS;

  readonly icons = { Menu, Sun, Moon, Globe };

  onCountryChange(event: Event): void {
    const countryCode = (event.target as HTMLSelectElement).value;
    this.preferencesService.setCountry(countryCode);
  }
}
