import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import {
  LucideAngularModule,
  LayoutDashboard,
  Package,
  Tags,
  ShoppingCart,
  ClipboardList,
  Settings,
} from 'lucide-angular';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, LucideAngularModule],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
})
export class SidebarComponent {
  readonly icons = {
    LayoutDashboard,
    Package,
    Tags,
    ShoppingCart,
    ClipboardList,
    Settings,
  };
}
