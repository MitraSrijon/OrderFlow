import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import {
  LucideAngularModule,
  TrendingUp,
  ShoppingBag,
  IndianRupee,
  Package,
  Users,
  Clock,
  AlertTriangle,
  RefreshCw,
} from 'lucide-angular';

import { PreferencesService } from '../../core/services/preferences.service';
import { OrderService } from '../../core/services/order.service';
import { Product } from '../products/product.model';
import { OrderResponse } from '../orders/order.model';

interface ProductPage {
  content: Product[];
  totalElements: number;
  totalPages: number;
  number: number;
  last: boolean;
}

interface DashboardStat {
  label: string;
  value: number;
  icon: string;
  detail: string;
}

interface TopProduct {
  id: number;
  name: string;
  category: string;
  sold: number;
  revenue: number;
  stock: number;
}

interface StockAlert {
  id: number;
  name: string;
  stock: number;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, LucideAngularModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class DashboardComponent implements OnInit {
  readonly preferences = inject(PreferencesService);

  private readonly http = inject(HttpClient);
  private readonly orderService = inject(OrderService);

  private readonly productsApiUrl = 'http://localhost:8080/api/products';

  private readonly LOW_STOCK_THRESHOLD = 10;
  private readonly PAGE_SIZE = 100;

  readonly icons = {
    TrendingUp,
    ShoppingBag,
    IndianRupee,
    Package,
    Users,
    Clock,
    AlertTriangle,
    RefreshCw,
  };

  loading = true;
  errorMessage = '';

  stats: DashboardStat[] = [];

  recentOrders: OrderResponse[] = [];
  topProducts: TopProduct[] = [];
  stockAlerts: StockAlert[] = [];

  totalOrders = 0;
  totalProducts = 0;
  totalCustomers = 0;
  totalRevenue = 0;

  monthlyRevenue: { month: string; revenue: number }[] = [];

  chartLinePath = '';
  chartAreaPath = '';
  chartYLabels: number[] = [];

  private readonly monthNames = [
    'Jan',
    'Feb',
    'Mar',
    'Apr',
    'May',
    'Jun',
    'Jul',
    'Aug',
    'Sep',
    'Oct',
    'Nov',
    'Dec',
  ];

  ngOnInit(): void {
    this.loadDashboard();
  }

  async loadDashboard(): Promise<void> {
    this.loading = true;
    this.errorMessage = '';

    try {
      const [orders, products] = await Promise.all([
        this.fetchAllOrders(),
        this.fetchAllProducts(),
      ]);

      this.totalOrders = orders.length;
      this.totalProducts = products.length;

      this.totalCustomers = new Set(
        orders.map((order) => order.customerEmail?.toLowerCase().trim()).filter((email) => !!email),
      ).size;

      const activeOrders = orders.filter((order) => order.orderStatus !== 'CANCELLED');

      this.totalRevenue = activeOrders.reduce(
        (total, order) => total + Number(order.totalAmount || 0),
        0,
      );

      this.recentOrders = [...orders]
        .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
        .slice(0, 5);

      this.buildStats();
      this.buildStockAlerts(products);
      this.buildTopProducts(activeOrders, products);
      this.buildRevenueChart(activeOrders);
    } catch (error) {
      console.error('Dashboard loading failed:', error);
      this.errorMessage = 'Unable to load dashboard data. Please check your backend connection.';
    } finally {
      this.loading = false;
    }
  }

  private async fetchAllOrders(): Promise<OrderResponse[]> {
    const orders: OrderResponse[] = [];
    let pageNumber = 0;

    while (true) {
      const response = await firstValueFrom(this.orderService.getAll(pageNumber, this.PAGE_SIZE));

      orders.push(...response.content);

      if (response.last || response.content.length === 0) {
        break;
      }

      pageNumber++;
    }

    return orders;
  }

  private async fetchAllProducts(): Promise<Product[]> {
    const products: Product[] = [];
    let pageNumber = 0;

    while (true) {
      const response = await firstValueFrom(
        this.http.get<ProductPage>(
          `${this.productsApiUrl}?page=${pageNumber}&size=${this.PAGE_SIZE}`,
        ),
      );

      products.push(...response.content);

      if (response.last || response.content.length === 0) {
        break;
      }

      pageNumber++;
    }

    return products;
  }

  private buildStats(): void {
    this.stats = [
      {
        label: 'Total Revenue',
        value: this.totalRevenue,
        icon: 'revenue',
        detail: 'All non-cancelled orders',
      },
      {
        label: 'Total Orders',
        value: this.totalOrders,
        icon: 'orders',
        detail: 'All recorded orders',
      },
      {
        label: 'Total Products',
        value: this.totalProducts,
        icon: 'products',
        detail: 'Current product catalogue',
      },
      {
        label: 'Total Customers',
        value: this.totalCustomers,
        icon: 'customers',
        detail: 'Unique customer emails',
      },
    ];
  }

  private buildStockAlerts(products: Product[]): void {
    this.stockAlerts = products
      .filter((product) => product.availableStock <= this.LOW_STOCK_THRESHOLD)
      .sort((a, b) => a.availableStock - b.availableStock)
      .slice(0, 5)
      .map((product) => ({
        id: product.id,
        name: product.name,
        stock: product.availableStock,
      }));
  }

  private buildTopProducts(orders: OrderResponse[], products: Product[]): void {
    const sales = new Map<
      number,
      {
        name: string;
        sold: number;
        revenue: number;
      }
    >();

    const productMap = new Map(products.map((product) => [product.id, product]));

    for (const order of orders) {
      for (const item of order.items || []) {
        const existing = sales.get(item.productId) || {
          name: item.productName,
          sold: 0,
          revenue: 0,
        };

        existing.sold += item.quantity;
        existing.revenue += Number(item.subtotal);

        sales.set(item.productId, existing);
      }
    }

    this.topProducts = Array.from(sales.entries())
      .map(([id, sale]) => {
        const product = productMap.get(id);

        return {
          id,
          name: sale.name,
          category: product?.categoryName || 'Uncategorized',
          sold: sale.sold,
          revenue: sale.revenue,
          stock: product?.availableStock ?? 0,
        };
      })
      .sort((a, b) => b.sold - a.sold)
      .slice(0, 5);
  }

  private buildRevenueChart(orders: OrderResponse[]): void {
    const currentYear = new Date().getFullYear();
    const monthlyValues = new Array(12).fill(0);

    for (const order of orders) {
      const orderDate = new Date(order.createdAt);

      if (orderDate.getFullYear() !== currentYear) {
        continue;
      }

      const month = orderDate.getMonth();

      monthlyValues[month] += Number(order.totalAmount || 0);
    }

    this.monthlyRevenue = monthlyValues.map((revenue, index) => ({
      month: this.monthNames[index],
      revenue,
    }));

    const maximum = Math.max(...monthlyValues);
    const chartMaximum = Math.max(Math.ceil(maximum / 50000) * 50000, 50000);

    this.chartYLabels = [3, 2, 1, 0].map((value) => (chartMaximum * value) / 3);

    const points = monthlyValues.map((value, index) => {
      const x = (index / 11) * 700;
      const y = 205 - (value / chartMaximum) * 180;

      return `${x},${y}`;
    });

    this.chartLinePath = points
      .map((point, index) => `${index === 0 ? 'M' : 'L'}${point}`)
      .join(' ');

    this.chartAreaPath = `${this.chartLinePath} L700 210 L0 210 Z`;
  }

  formatCurrency(amount: number): string {
    return this.preferences.formatCurrency(amount);
  }

  formatCompactCurrency(amount: number): string {
    if (amount >= 100000) {
      return `₹${(amount / 100000).toFixed(1)}L`;
    }

    if (amount >= 1000) {
      return `₹${(amount / 1000).toFixed(0)}K`;
    }

    return `₹${amount.toFixed(0)}`;
  }

  formatOrderId(id: number): string {
    return `#${id}`;
  }

  get currentYear(): number {
    return new Date().getFullYear();
  }
}
