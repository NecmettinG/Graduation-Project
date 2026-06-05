"use client";

import { useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import { fetchCoreApi } from "@/lib/api";
import { ProductCard } from "@/components/ProductCard";
import styles from "./page.module.css";

export default function ProductsPage() {
  const [products, setProducts] = useState<any[]>([]);
  const [categories, setCategories] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [searchTerm, setSearchTerm] = useState("");
  const [debouncedSearch, setDebouncedSearch] = useState("");
  const [selectedCategory, setSelectedCategory] = useState<string>("");
  const [totalPages, setTotalPages] = useState(1);
  const searchParams = useSearchParams();

  // Fetch categories on mount
  useEffect(() => {
    async function loadCategories() {
      try {
        const data = await fetchCoreApi(`/categories?limit=50`);
        setCategories(Array.isArray(data) ? data : (data.content || []));
        
        // Set initial category from URL if present
        const catParam = searchParams.get('category');
        if (catParam) {
          setSelectedCategory(catParam);
        }
      } catch (err) {
        console.error("Failed to load categories", err);
      }
    }
    loadCategories();
  }, [searchParams]);

  // Debounce search term
  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedSearch(searchTerm);
      setPage(1); // Reset to page 1 on new search
    }, 500);
    return () => clearTimeout(handler);
  }, [searchTerm]);

  // Reset page when category changes
  useEffect(() => {
    setPage(1);
  }, [selectedCategory]);

  useEffect(() => {
    async function loadProducts() {
      setLoading(true);
      try {
        let endpoint = `/products?page=${page}&limit=12`;
        if (debouncedSearch) endpoint += `&search=${encodeURIComponent(debouncedSearch)}`;
        if (selectedCategory) endpoint += `&categoryId=${encodeURIComponent(selectedCategory)}`;
        
        const data = await fetchCoreApi(endpoint);
        setProducts(Array.isArray(data) ? data : (data.content || []));
        
        if (data && typeof data.totalPages === 'number') {
          setTotalPages(data.totalPages);
        } else {
          // Fallback if totalPages is not returned
          const itemsCount = Array.isArray(data) ? data.length : (data.content?.length || 0);
          setTotalPages(itemsCount === 12 ? page + 1 : page);
        }
      } catch (err) {
        console.error("Failed to load products", err);
      } finally {
        setLoading(false);
      }
    }
    loadProducts();
  }, [page, debouncedSearch, selectedCategory]);

  const renderPageNumbers = () => {
    let pages = [];
    const maxPagesToShow = 5;
    let startPage = Math.max(1, page - 2);
    let endPage = Math.min(totalPages, startPage + maxPagesToShow - 1);

    if (endPage - startPage + 1 < maxPagesToShow) {
      startPage = Math.max(1, endPage - maxPagesToShow + 1);
    }

    for (let i = startPage; i <= endPage; i++) {
      pages.push(
        <button
          key={i}
          onClick={() => setPage(i)}
          style={{ 
            padding: "0.5rem 1rem", 
            minWidth: "40px", 
            margin: "0 0.25rem", 
            border: page === i ? "none" : "1px solid var(--border-color)", 
            background: page === i ? "var(--accent-primary)" : "transparent", 
            color: page === i ? "#fff" : "var(--text-primary)", 
            borderRadius: "4px", 
            cursor: "pointer",
            transition: "all 0.2s"
          }}
        >
          {i}
        </button>
      );
    }
    return pages;
  };

  return (
    <div className={styles.container}>
      <div className={`container ${styles.header}`}>
        <h1>All Products</h1>
        <p>Explore our premium collection of curated items.</p>
      </div>

      <div className="container">
        <div className={styles.searchBarContainer} style={{ display: "flex", gap: "1rem" }}>
          <input 
            type="text" 
            placeholder="Search for products..." 
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className={styles.searchInput}
            style={{ flex: 1 }}
          />
          <select 
            className={styles.searchInput}
            style={{ flex: "0 0 250px", cursor: "pointer" }}
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(e.target.value)}
          >
            <option value="" style={{ color: "#000" }}>All Categories</option>
            {categories.map((cat, idx) => (
              <option key={cat.categoryId || idx} value={cat.categoryId} style={{ color: "#000" }}>
                {cat.categoryName}
              </option>
            ))}
          </select>
        </div>

        {loading ? (
          <div className={styles.loading}>Loading products...</div>
        ) : (
          <>
            <div className={styles.grid}>
              {products.map((p, idx) => (
                <ProductCard key={p.productId || idx} product={p} />
              ))}
              {products.length === 0 && (
                <div style={{ gridColumn: "1 / -1", textAlign: "center", padding: "4rem", color: "var(--text-secondary)" }}>
                  No products found matching your filters.
                </div>
              )}
            </div>
            
            <div className={styles.pagination} style={{ display: "flex", justifyContent: "center", alignItems: "center", marginTop: "3rem" }}>
              <button 
                className="btn btn-outline" 
                disabled={page === 1} 
                onClick={() => setPage(p => p - 1)}
                style={{ marginRight: "0.5rem" }}
              >
                Previous
              </button>
              
              <div style={{ display: "flex" }}>
                {renderPageNumbers()}
              </div>
              
              <button 
                className="btn btn-outline" 
                onClick={() => setPage(p => p + 1)}
                disabled={page >= totalPages}
                style={{ marginLeft: "0.5rem" }}
              >
                Next
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
