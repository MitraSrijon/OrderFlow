import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CategoryPage } from '../../features/categories/category.model';

@Injectable({
  providedIn: 'root',
})
export class CategoryService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/categories';

  getAll(): Observable<CategoryPage> {
    return this.http.get<CategoryPage>(`${this.apiUrl}?page=0&size=100&sortBy=name&direction=asc`);
  }
}
