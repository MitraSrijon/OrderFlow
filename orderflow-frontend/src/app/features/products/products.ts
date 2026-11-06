import { Component, OnInit, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';

import { ProductService } from '../../core/services/product.service';
import { CategoryService } from '../../core/services/category.service';
import { PreferencesService } from '../../core/services/preferences.service';

import { Product, ProductRequest, ProductPage } from './product.model';

import { Category } from '../categories/category.model';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [DatePipe, FormsModule],
  templateUrl: './products.html',
  styleUrl: './products.scss',
})
export class Products implements OnInit {
  private readonly productService = inject(ProductService);
  private readonly categoryService = inject(CategoryService);
  private readonly preferencesService = inject(PreferencesService);

  products: Product[] = [];
  categories: Category[] = [];

  // Search and filters
  searchTerm = '';
  selectedCategoryId = 0;

  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;

  // Sorting
  sortBy = 'id';
  sortDirection: 'asc' | 'desc' = 'asc';

  // Loading states
  loading = false;
  saving = false;
  loadingCategories = false;
  updatingStock = false;

  // Delete
  deletingProductId: number | null = null;

  // Stock editing
  editingStockProductId: number | null = null;
  stockQuantity = 0;

  // Messages
  errorMessage = '';
  categoryError = '';
  successMessage = '';

  // Product form
  showAddForm = false;
  editingProductId: number | null = null;

  newProduct: ProductRequest = this.createEmptyProduct();

  ngOnInit(): void {
    this.loadProducts();
    this.loadCategories();
  }

  createEmptyProduct(): ProductRequest {
    return {
      categoryId: 0,
      name: '',
      description: '',
      price: 0,
      availableStock: 0,
    };
  }

  // Currency preference
  get currencyCode(): string {
    return this.preferencesService.selectedCountry().currency;
  }

  formatPrice(price: number): string {
    return this.preferencesService.formatCurrency(price);
  }

  // Product loading
  loadProducts(): void {
    this.loading = true;
    this.errorMessage = '';

    const search = this.searchTerm.trim();

    let request;

    if (this.selectedCategoryId > 0) {
      request = this.productService.getByCategory(
        this.selectedCategoryId,
        this.currentPage,
        this.pageSize,
        this.sortBy,
        this.sortDirection,
      );
    } else if (search) {
      request = this.productService.search(
        search,
        this.currentPage,
        this.pageSize,
        this.sortBy,
        this.sortDirection,
      );
    } else {
      request = this.productService.getAll(
        this.currentPage,
        this.pageSize,
        this.sortBy,
        this.sortDirection,
      );
    }

    request.subscribe({
      next: (data: ProductPage) => {
        this.products = data.content;
        this.totalElements = data.totalElements;
        this.totalPages = data.totalPages;
        this.currentPage = data.number;
        this.loading = false;
      },
      error: (error) => {
        console.error('Failed to load products:', error);

        this.errorMessage =
          error.error?.message ||
          error.error?.detail ||
          'Unable to load products. Please try again.';

        this.loading = false;
      },
    });
  }

  // Categories
  loadCategories(): void {
    this.loadingCategories = true;
    this.categoryError = '';

    this.categoryService.getAll().subscribe({
      next: (data) => {
        this.categories = data.content;
        this.loadingCategories = false;
      },
      error: (error) => {
        console.error('Failed to load categories:', error);

        this.categoryError = 'Unable to load categories.';
        this.loadingCategories = false;
      },
    });
  }

  // Search
  onSearch(): void {
    this.currentPage = 0;
    this.loadProducts();
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.currentPage = 0;
    this.loadProducts();
  }

  // Category filter
  onCategoryFilterChange(): void {
    this.currentPage = 0;
    this.loadProducts();
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedCategoryId = 0;
    this.currentPage = 0;
    this.loadProducts();
  }

  // Sorting
  onSortChange(): void {
    this.currentPage = 0;
    this.loadProducts();
  }

  toggleSortDirection(): void {
    this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';

    this.currentPage = 0;
    this.loadProducts();
  }

  // Pagination
  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages || this.loading) {
      return;
    }

    this.currentPage = page;
    this.loadProducts();
  }

  previousPage(): void {
    this.goToPage(this.currentPage - 1);
  }

  nextPage(): void {
    this.goToPage(this.currentPage + 1);
  }

  onPageSizeChange(): void {
    this.currentPage = 0;
    this.loadProducts();
  }

  get startItem(): number {
    return this.totalElements === 0 ? 0 : this.currentPage * this.pageSize + 1;
  }

  get endItem(): number {
    return Math.min((this.currentPage + 1) * this.pageSize, this.totalElements);
  }

  get pageNumbers(): number[] {
    const pages: number[] = [];

    const start = Math.max(0, this.currentPage - 2);
    const end = Math.min(this.totalPages - 1, this.currentPage + 2);

    for (let i = start; i <= end; i++) {
      pages.push(i);
    }

    return pages;
  }

  // Add and edit
  toggleAddForm(): void {
    if (this.showAddForm) {
      this.cancelForm();
      return;
    }

    this.editingProductId = null;
    this.newProduct = this.createEmptyProduct();

    this.errorMessage = '';
    this.successMessage = '';
    this.showAddForm = true;
  }

  editProduct(product: Product): void {
    this.editingProductId = product.id;

    this.newProduct = {
      categoryId: product.categoryId,
      name: product.name,
      description: product.description ?? '',
      price: product.price,
      availableStock: product.availableStock,
    };

    this.errorMessage = '';
    this.successMessage = '';
    this.showAddForm = true;

    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    });
  }

  cancelForm(): void {
    this.showAddForm = false;
    this.editingProductId = null;
    this.newProduct = this.createEmptyProduct();
    this.errorMessage = '';
  }

  saveProduct(form: NgForm): void {
    if (form.invalid || this.newProduct.categoryId <= 0) {
      form.control.markAllAsTouched();
      return;
    }

    if (!this.newProduct.name.trim()) {
      return;
    }

    const product: ProductRequest = {
      ...this.newProduct,
      name: this.newProduct.name.trim(),
      description: this.newProduct.description.trim(),
    };

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    const isEditing = this.editingProductId !== null;

    const request = isEditing
      ? this.productService.update(this.editingProductId!, product)
      : this.productService.create(product);

    request.subscribe({
      next: () => {
        this.saving = false;

        this.successMessage = isEditing
          ? 'Product updated successfully!'
          : 'Product created successfully!';

        this.cancelForm();
        this.currentPage = 0;
        this.loadProducts();
      },
      error: (error) => {
        console.error('Failed to save product:', error);

        this.saving = false;

        this.errorMessage =
          error.error?.message ||
          error.error?.detail ||
          'Unable to save product. Please try again.';
      },
    });
  }

  // Delete
  deleteProduct(product: Product): void {
    const confirmed = window.confirm(`Are you sure you want to delete "${product.name}"?`);

    if (!confirmed) {
      return;
    }

    this.deletingProductId = product.id;
    this.errorMessage = '';
    this.successMessage = '';

    this.productService.delete(product.id).subscribe({
      next: () => {
        this.deletingProductId = null;

        this.successMessage = `"${product.name}" deleted successfully!`;

        if (this.editingProductId === product.id) {
          this.cancelForm();
        }

        // Move back if the current page becomes empty.
        if (this.products.length === 1 && this.currentPage > 0) {
          this.currentPage--;
        }

        this.loadProducts();
      },
      error: (error) => {
        console.error('Failed to delete product:', error);

        this.deletingProductId = null;

        this.errorMessage =
          error.error?.message ||
          error.error?.detail ||
          (error.status === 409
            ? 'This product cannot be deleted because it is currently referenced by another record.'
            : 'Unable to delete product. Please try again.');
      },
    });
  }

  // Stock management
  startStockEdit(product: Product): void {
    this.editingStockProductId = product.id;
    this.stockQuantity = product.availableStock;
    this.errorMessage = '';
    this.successMessage = '';
  }

  cancelStockEdit(): void {
    this.editingStockProductId = null;
    this.stockQuantity = 0;
  }

  saveStock(product: Product): void {
    if (!Number.isInteger(this.stockQuantity) || this.stockQuantity < 0) {
      this.errorMessage = 'Stock must be a non-negative whole number.';
      return;
    }

    this.updatingStock = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.productService.updateStock(product.id, { quantity: this.stockQuantity }).subscribe({
      next: () => {
        this.updatingStock = false;
        this.editingStockProductId = null;

        this.successMessage = `Stock updated for "${product.name}".`;

        this.loadProducts();
      },
      error: (error) => {
        console.error('Failed to update stock:', error);

        this.updatingStock = false;

        this.errorMessage =
          error.error?.message ||
          error.error?.detail ||
          'Unable to update stock. Please try again.';
      },
    });
  }

  // Stock status
  getStockStatus(stock: number): string {
    if (stock === 0) return 'Out of stock';
    if (stock <= 5) return 'Low stock';
    return 'In stock';
  }
}
