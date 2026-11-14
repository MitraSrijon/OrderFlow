import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { OrderService } from '../../core/services/order.service';
import { Product } from '../products/product.model';

import { OrderRequest, OrderResponse, OrderStatus } from './order.model';

interface OrderItemForm {
  productId: number | null;
  quantity: number;
}

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './orders.html',
  styleUrl: './orders.scss',
})
export class Orders implements OnInit {
  private orderService = inject(OrderService);

  orders: OrderResponse[] = [];
  products: Product[] = [];

  selectedOrder: OrderResponse | null = null;

  loading = false;
  productsLoading = false;
  submitting = false;

  errorMessage = '';
  successMessage = '';

  showCreateForm = false;

  customerName = '';
  customerEmail = '';

  orderItems: OrderItemForm[] = [{ productId: null, quantity: 1 }];

  statusFilter = 'ALL';
  emailFilter = '';

  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;

  ngOnInit(): void {
    this.loadOrders();
    this.loadProducts();
  }

  loadOrders(): void {
    this.loading = true;
    this.errorMessage = '';
    this.selectedOrder = null;

    let request;

    if (this.emailFilter.trim()) {
      request = this.orderService.getByCustomerEmail(
        this.emailFilter.trim(),
        this.currentPage,
        this.pageSize,
      );
    } else if (this.statusFilter !== 'ALL') {
      request = this.orderService.getByStatus(
        this.statusFilter as OrderStatus,
        this.currentPage,
        this.pageSize,
      );
    } else {
      request = this.orderService.getAll(this.currentPage, this.pageSize);
    }

    request.subscribe({
      next: (data) => {
        this.orders = data.content;
        this.totalPages = data.totalPages;
        this.totalElements = data.totalElements;
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = this.getErrorMessage(error, 'Unable to load orders.');
        this.loading = false;
      },
    });
  }

  loadProducts(): void {
    this.productsLoading = true;

    this.orderService.getProducts().subscribe({
      next: (data) => {
        this.products = data.content;
        this.productsLoading = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load products for order creation.';
        this.productsLoading = false;
      },
    });
  }

  onFilterChange(): void {
    this.emailFilter = '';
    this.currentPage = 0;
    this.loadOrders();
  }

  searchByEmail(): void {
    this.statusFilter = 'ALL';
    this.currentPage = 0;
    this.loadOrders();
  }

  clearFilters(): void {
    this.statusFilter = 'ALL';
    this.emailFilter = '';
    this.currentPage = 0;
    this.loadOrders();
  }

  changePage(page: number): void {
    if (page < 0 || page >= this.totalPages) {
      return;
    }

    this.currentPage = page;
    this.loadOrders();
  }

  openCreateForm(): void {
    this.showCreateForm = true;
    this.selectedOrder = null;
    this.clearMessages();
  }

  closeCreateForm(): void {
    this.showCreateForm = false;
    this.resetOrderForm();
  }

  addItem(): void {
    this.orderItems.push({
      productId: null,
      quantity: 1,
    });
  }

  removeItem(index: number): void {
    if (this.orderItems.length > 1) {
      this.orderItems.splice(index, 1);
    }
  }

  getProduct(productId: number | null): Product | undefined {
    if (productId === null) {
      return undefined;
    }

    return this.products.find((product) => product.id === productId);
  }

  getItemSubtotal(item: OrderItemForm): number {
    const product = this.getProduct(item.productId);

    if (!product || item.quantity < 1) {
      return 0;
    }

    return product.price * item.quantity;
  }

  getEstimatedTotal(): number {
    return this.orderItems.reduce((total, item) => total + this.getItemSubtotal(item), 0);
  }

  isItemInvalid(item: OrderItemForm): boolean {
    if (!item.productId || !Number.isInteger(item.quantity) || item.quantity < 1) {
      return true;
    }

    const product = this.getProduct(item.productId);

    return !product || item.quantity > product.availableStock;
  }

  createOrder(): void {
    this.clearMessages();

    if (!this.customerName.trim() || !this.customerEmail.trim()) {
      this.errorMessage = 'Customer name and email are required.';
      return;
    }

    if (this.orderItems.some((item) => this.isItemInvalid(item))) {
      this.errorMessage =
        'Select a valid product and quantity. Quantity must not exceed available stock.';
      return;
    }

    const request: OrderRequest = {
      customerName: this.customerName.trim(),
      customerEmail: this.customerEmail.trim(),
      items: this.orderItems.map((item) => ({
        productId: item.productId!,
        quantity: item.quantity,
      })),
    };

    this.submitting = true;

    this.orderService.create(request).subscribe({
      next: (createdOrder) => {
        this.submitting = false;
        this.showCreateForm = false;
        this.resetOrderForm();

        this.successMessage = `Order #${createdOrder.id} created successfully.`;
        this.currentPage = 0;

        this.loadOrders();
        this.loadProducts();
      },
      error: (error) => {
        this.submitting = false;
        this.errorMessage = this.getErrorMessage(error, 'Unable to create order.');
      },
    });
  }

  viewOrder(order: OrderResponse): void {
    this.clearMessages();
    this.showCreateForm = false;

    this.orderService.getById(order.id).subscribe({
      next: (data) => {
        this.selectedOrder = data;
      },
      error: (error) => {
        this.errorMessage = this.getErrorMessage(error, 'Unable to load order details.');
      },
    });
  }

  closeDetails(): void {
    this.selectedOrder = null;
  }

  updateStatus(order: OrderResponse, status: OrderStatus): void {
    this.clearMessages();

    this.orderService.updateStatus(order.id, status).subscribe({
      next: (updatedOrder) => {
        this.successMessage = `Order #${updatedOrder.id} status updated to ${updatedOrder.orderStatus}.`;

        if (this.selectedOrder?.id === updatedOrder.id) {
          this.selectedOrder = updatedOrder;
        }

        this.loadOrders();
        this.loadProducts();
      },
      error: (error) => {
        this.errorMessage = this.getErrorMessage(error, 'Unable to update order status.');
      },
    });
  }

  canConfirm(order: OrderResponse): boolean {
    return order.orderStatus === 'PENDING';
  }

  canCancel(order: OrderResponse): boolean {
    return order.orderStatus === 'PENDING' || order.orderStatus === 'CONFIRMED';
  }

  resetOrderForm(): void {
    this.customerName = '';
    this.customerEmail = '';
    this.orderItems = [{ productId: null, quantity: 1 }];
  }

  clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  private getErrorMessage(error: any, fallback: string): string {
    return error?.error?.detail || error?.error?.message || error?.error?.error || fallback;
  }
}
