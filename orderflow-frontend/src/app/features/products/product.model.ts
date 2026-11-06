export interface Product {
  id: number;
  name: string;
  description: string | null;
  price: number;
  availableStock: number;
  categoryId: number;
  categoryName: string;
  created: string;
  updated: string;
}

export interface ProductRequest {
  categoryId: number;
  name: string;
  description: string;
  price: number;
  availableStock: number;
}

export interface StockUpdateRequest {
  quantity: number;
}

export interface ProductPage {
  content: Product[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}
