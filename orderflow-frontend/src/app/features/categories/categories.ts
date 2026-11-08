import { CommonModule, DatePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { CategoryService } from '../../core/services/category.service';
import { Category, CategoryRequest } from './category.model';

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe],
  templateUrl: './categories.html',
  styleUrl: './categories.scss',
})
export class Categories {
  private readonly categoryService = inject(CategoryService);

  categories: Category[] = [];

  isLoading = false;
  isSaving = false;
  isEditing = false;

  editingId: number | null = null;
  deletingId: number | null = null;

  showForm = false;

  searchTerm = '';

  page = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;

  sortBy = 'id';
  direction: 'asc' | 'desc' = 'asc';

  successMessage = '';
  errorMessage = '';

  form = {
    name: '',
    description: '',
  };

  ngOnInit(): void {
    this.loadCategories();
  }

  get filteredCategories(): Category[] {
    const search = this.searchTerm.trim().toLowerCase();

    if (!search) {
      return this.categories;
    }

    return this.categories.filter(
      (category) =>
        category.name.toLowerCase().includes(search) ||
        (category.description ?? '').toLowerCase().includes(search),
    );
  }

  loadCategories(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.categoryService.getAll(this.page, this.pageSize, this.sortBy, this.direction).subscribe({
      next: (response) => {
        this.categories = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.page = response.number;
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = this.getErrorMessage(
          error,
          'Unable to load categories. Please try again.',
        );
      },
    });
  }

  openCreateForm(): void {
    this.resetForm();
    this.showForm = true;
    this.clearMessages();
  }

  openEditForm(category: Category): void {
    this.isEditing = true;
    this.editingId = category.id;
    this.showForm = true;

    this.form = {
      name: category.name,
      description: category.description ?? '',
    };

    this.clearMessages();
  }

  closeForm(): void {
    this.showForm = false;
    this.resetForm();
  }

  saveCategory(): void {
    const name = this.form.name.trim();

    if (!name) {
      this.errorMessage = 'Category name is required.';
      return;
    }

    const request: CategoryRequest = {
      name,
      description: this.form.description.trim() || null,
    };

    this.isSaving = true;
    this.clearMessages();

    const operation =
      this.isEditing && this.editingId !== null
        ? this.categoryService.update(this.editingId, request)
        : this.categoryService.create(request);

    operation.subscribe({
      next: () => {
        this.isSaving = false;
        this.showForm = false;

        this.successMessage = this.isEditing
          ? 'Category updated successfully.'
          : 'Category created successfully.';

        this.resetForm();
        this.loadCategories();
      },
      error: (error) => {
        this.isSaving = false;
        this.errorMessage = this.getErrorMessage(
          error,
          'Unable to save category. Please try again.',
        );
      },
    });
  }

  deleteCategory(category: Category): void {
    const confirmed = confirm(`Are you sure you want to delete "${category.name}"?`);

    if (!confirmed) {
      return;
    }

    this.deletingId = category.id;
    this.clearMessages();

    this.categoryService.delete(category.id).subscribe({
      next: () => {
        this.deletingId = null;

        this.successMessage = 'Category deleted successfully.';

        if (this.categories.length === 1 && this.page > 0) {
          this.page--;
        }

        this.loadCategories();
      },
      error: (error: HttpErrorResponse) => {
        this.deletingId = null;

        if (error.status === 409) {
          this.errorMessage =
            'This category cannot be deleted because one or more products are associated with it. Reassign or remove those products first.';
          return;
        }

        this.errorMessage = this.getErrorMessage(
          error,
          'Unable to delete category. Please try again.',
        );
      },
    });
  }

  changeSortField(value: string): void {
    this.sortBy = value;
    this.page = 0;
    this.loadCategories();
  }

  toggleSortDirection(): void {
    this.direction = this.direction === 'asc' ? 'desc' : 'asc';
    this.page = 0;
    this.loadCategories();
  }

  changePageSize(value: string): void {
    this.pageSize = Number(value);
    this.page = 0;
    this.loadCategories();
  }

  previousPage(): void {
    if (this.page > 0) {
      this.page--;
      this.loadCategories();
    }
  }

  nextPage(): void {
    if (this.page + 1 < this.totalPages) {
      this.page++;
      this.loadCategories();
    }
  }

  clearSearch(): void {
    this.searchTerm = '';
  }

  clearMessages(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }

  private resetForm(): void {
    this.form = {
      name: '',
      description: '',
    };

    this.isEditing = false;
    this.editingId = null;
  }

  private getErrorMessage(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse) {
      const message = error.error?.message ?? error.error?.detail ?? error.error?.error;

      if (typeof message === 'string' && message.trim()) {
        return message;
      }

      if (error.status === 0) {
        return 'Unable to connect to the server. Check that the backend is running.';
      }
    }

    return fallback;
  }
}
