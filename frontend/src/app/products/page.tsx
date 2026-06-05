"use client";

import { useEffect, useState } from "react";
import { fetchCoreApi } from "@/lib/api";
import { ProductCard } from "@/components/ProductCard";
import styles from "./page.module.css";

export default function ProductsPage() {
  const [products, setProducts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);

  useEffect(() => {
    async function loadProducts() {
      setLoading(true);
      try {
        const data = await fetchCoreApi(`/products?page=${page}&limit=12`);
        setProducts(Array.isArray(data) ? data : (data.content || []));
      } catch (err) {
        console.error("Failed to load products", err);
      } finally {
        setLoading(false);
      }
    }
    loadProducts();
  }, [page]);

  return (
    <div className={styles.container}>
      <div className={`container ${styles.header}`}>
        <h1>All Products</h1>
        <p>Explore our premium collection of curated items.</p>
      </div>

      <div className="container">
        {loading ? (
          <div className={styles.loading}>Loading products...</div>
        ) : (
          <>
            <div className={styles.grid}>
              {products.map((p, idx) => (
                <ProductCard key={p.productId || idx} product={p} />
              ))}
            </div>
            
            <div className={styles.pagination}>
              <button 
                className="btn btn-outline" 
                disabled={page === 0} 
                onClick={() => setPage(p => p - 1)}
              >
                Previous
              </button>
              <span className={styles.pageIndicator}>Page {page + 1}</span>
              <button 
                className="btn btn-outline" 
                onClick={() => setPage(p => p + 1)}
                disabled={products.length < 12}
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
