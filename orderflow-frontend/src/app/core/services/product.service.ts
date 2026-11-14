import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  Product,
  ProductRequest,
  ProductPage,
  StockUpdateRequest,
} from '../../features/products/product.model';

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  private readonly http = inject(HttpClient);

  private readonly apiUrl = 'http://localhost:8080/api/products';

  getAll(page = 0, size = 10, sortBy = 'id', direction = 'asc'): Observable<ProductPage> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('direction', direction);

    return this.http.get<ProductPage>(this.apiUrl, { params });
  }

  getById(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.apiUrl}/${id}`);
  }

  create(product: ProductRequest): Observable<Product> {
    return this.http.post<Product>(this.apiUrl, product);
  }

  update(id: number, product: ProductRequest): Observable<Product> {
    return this.http.put<Product>(`${this.apiUrl}/${id}`, product);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  search(
    name: string,
    page = 0,
    size = 10,
    sortBy = 'id',
    direction = 'asc',
  ): Observable<ProductPage> {
    const params = new HttpParams()
      .set('name', name)
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('direction', direction);

    return this.http.get<ProductPage>(`${this.apiUrl}/search`, { params });
  }

  getByCategory(
    categoryId: number,
    page = 0,
    size = 10,
    sortBy = 'id',
    direction = 'asc',
  ): Observable<ProductPage> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('direction', direction);

    return this.http.get<ProductPage>(`${this.apiUrl}/category/${categoryId}`, { params });
  }

  updateStock(id: number, request: StockUpdateRequest): Observable<Product> {
    return this.http.patch<Product>(`${this.apiUrl}/${id}/stock`, request);
  }
}
