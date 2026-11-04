import { Component, inject } from '@angular/core';
import {
  LucideAngularModule,
  TrendingUp,
  TrendingDown,
  ShoppingBag,
  IndianRupee,
  Package,
  Users,
  ArrowUpRight,
  ArrowDownRight,
  Clock,
  CheckCircle,
  Truck,
  AlertTriangle,
} from 'lucide-angular';
import { PreferencesService } from '../../core/services/preferences.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [LucideAngularModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class DashboardComponent {
  readonly preferences = inject(PreferencesService);

  readonly icons = {
    TrendingUp,
    TrendingDown,
    ShoppingBag,
    IndianRupee,
    Package,
    Users,
    ArrowUpRight,
    ArrowDownRight,
    Clock,
    CheckCircle,
    Truck,
    AlertTriangle,
  };

  readonly stats = [
    {
      label: 'Total Revenue',
      value: 128450,
      change: '+12.8%',
      positive: true,
      icon: 'revenue',
      detail: 'vs. last month',
    },
    {
      label: 'Total Orders',
      value: 1248,
      change: '+8.2%',
      positive: true,
      icon: 'orders',
      detail: 'vs. last month',
    },
    {
      label: 'Total Products',
      value: 356,
      change: '+4.5%',
      positive: true,
      icon: 'products',
      detail: 'vs. last month',
    },
    {
      label: 'Total Customers',
      value: 892,
      change: '-2.4%',
      positive: false,
      icon: 'customers',
      detail: 'vs. last month',
    },
  ];

  readonly recentOrders = [
    {
      id: '#ORD-1024',
      customer: 'Olivia Rhye',
      email: 'olivia@example.com',
      date: '04 Nov 2026',
      amount: 2450,
      status: 'Delivered',
    },
    {
      id: '#ORD-1023',
      customer: 'Phoenix Baker',
      email: 'phoenix@example.com',
      date: '04 Nov 2026',
      amount: 1890,
      status: 'Processing',
    },
    {
      id: '#ORD-1022',
      customer: 'Lana Steiner',
      email: 'lana@example.com',
      date: '03 Nov 2026',
      amount: 5200,
      status: 'Shipped',
    },
    {
      id: '#ORD-1021',
      customer: 'Demi Wilkinson',
      email: 'demi@example.com',
      date: '03 Nov 2026',
      amount: 1250,
      status: 'Pending',
    },
    {
      id: '#ORD-1020',
      customer: 'Candice Wu',
      email: 'candice@example.com',
      date: '02 Nov 2026',
      amount: 3400,
      status: 'Delivered',
    },
  ];

  readonly topProducts = [
    { name: 'Wireless Headphones', category: 'Electronics', sold: 245, revenue: 73500, stock: 12 },
    { name: 'Smart Watch Pro', category: 'Electronics', sold: 189, revenue: 94500, stock: 8 },
    { name: 'Classic Backpack', category: 'Accessories', sold: 156, revenue: 46800, stock: 32 },
    { name: 'Running Shoes', category: 'Footwear', sold: 124, revenue: 62000, stock: 5 },
  ];

  formatCurrency(amount: number): string {
    return this.preferences.formatCurrency(amount);
  }
}
