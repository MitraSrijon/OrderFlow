import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Product } from '../../features/products/product.model';
import {
  OrderRequest,
  OrderResponse,
  OrderStatus,
  OrderPage,
} from '../../features/orders/order.model';

interface ProductPage {
  content: Product[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class OrderService {
  private http = inject(HttpClient);

  private readonly apiUrl = 'http://localhost:8080/api/orders';
  private readonly productApiUrl = 'http://localhost:8080/api/products';

  getAll(page = 0, size = 10): Observable<OrderPage> {
    const params = new HttpParams().set('page', page).set('size', size);

    return this.http.get<OrderPage>(this.apiUrl, { params });
  }

  getById(id: number): Observable<OrderResponse> {
    return this.http.get<OrderResponse>(`${this.apiUrl}/${id}`);
  }

  create(request: OrderRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(this.apiUrl, request);
  }

  updateStatus(id: number, status: OrderStatus): Observable<OrderResponse> {
    return this.http.patch<OrderResponse>(`${this.apiUrl}/${id}/status`, { status });
  }

  getByStatus(status: OrderStatus, page = 0, size = 10): Observable<OrderPage> {
    const params = new HttpParams().set('page', page).set('size', size);

    return this.http.get<OrderPage>(`${this.apiUrl}/status/${status}`, { params });
  }

  getByCustomerEmail(email: string, page = 0, size = 10): Observable<OrderPage> {
    const params = new HttpParams().set('email', email).set('page', page).set('size', size);

    return this.http.get<OrderPage>(`${this.apiUrl}/customer`, { params });
  }

  getProducts(): Observable<ProductPage> {
    const params = new HttpParams().set('page', 0).set('size', 100);

    return this.http.get<ProductPage>(this.productApiUrl, { params });
  }
}
