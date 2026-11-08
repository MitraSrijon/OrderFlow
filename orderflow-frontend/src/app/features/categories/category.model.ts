export interface Category {
  id: number;
  name: string;
  description: string | null;
  created: string;
  updated: string;
}

export interface CategoryRequest {
  name: string;
  description: string | null;
}

export interface CategoryPage {
  content: Category[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}
