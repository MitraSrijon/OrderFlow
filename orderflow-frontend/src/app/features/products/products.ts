import { Component, OnInit, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ProductService } from '../../core/services/product.service';
import { Product } from './product.model';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './products.html',
  styleUrl: './products.scss',
})
export class Products implements OnInit {
  private readonly productService = inject(ProductService);

  products: Product[] = [];
  searchTerm = '';
  loading = false;
  errorMessage = '';

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading = true;
    this.errorMessage = '';

    this.productService.getAll().subscribe({
      next: (data) => {
        console.log('API Response:', data);
        console.log('Products received:', data.content);

        this.products = data.content;

        console.log('Products assigned:', this.products);

        this.loading = false;
      },
      error: (error) => {
        console.error('Failed to load products:', error);
        this.errorMessage = 'Unable to load products. Please try again.';
        this.loading = false;
      },
    });
  }

  onSearch(event: Event): void {
    this.searchTerm = (event.target as HTMLInputElement).value;
  }

  get filteredProducts(): Product[] {
    const term = this.searchTerm.trim().toLowerCase();

    if (!term) {
      return this.products;
    }

    return this.products.filter((product) => product.name.toLowerCase().includes(term));
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
    }).format(price);
  }

  getStockStatus(stock: number): string {
    if (stock === 0) return 'Out of stock';
    if (stock <= 5) return 'Low stock';
    return 'In stock';
  }
}
