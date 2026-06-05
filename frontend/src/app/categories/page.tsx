"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { fetchCoreApi } from "@/lib/api";

export default function CategoriesPage() {
  const [categories, setCategories] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadCategories() {
      try {
        const data = await fetchCoreApi(`/categories?limit=100`);
        setCategories(Array.isArray(data) ? data : (data.content || []));
      } catch (err) {
        console.error("Failed to load categories", err);
      } finally {
        setLoading(false);
      }
    }
    loadCategories();
  }, []);

  if (loading) {
    return <div className="container" style={{ padding: "4rem 0", textAlign: "center" }}>Loading categories...</div>;
  }

  return (
    <div className="container" style={{ padding: "4rem 1.5rem", minHeight: "60vh" }}>
      <div style={{ marginBottom: "3rem", textAlign: "center" }}>
        <h1 style={{ fontSize: "2.5rem", color: "var(--accent-primary)", marginBottom: "1rem", fontFamily: "var(--font-outfit)" }}>
          All Categories
        </h1>
        <p style={{ color: "var(--text-secondary)", maxWidth: "600px", margin: "0 auto" }}>
          Browse our entire selection of product categories to find exactly what you're looking for.
        </p>
      </div>

      <div style={{ 
        display: "grid", 
        gridTemplateColumns: "repeat(auto-fill, minmax(250px, 1fr))", 
        gap: "1.5rem" 
      }}>
        {categories.map((cat, idx) => (
          <Link href={`/products?category=${cat.categoryId}`} key={cat.categoryId || idx} style={{ textDecoration: 'none' }}>
            <div className="glass-panel" style={{ 
              padding: "2rem", 
              textAlign: "center",
              cursor: "pointer", 
              transition: "all 0.2s ease-in-out",
              height: "100%",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              flexDirection: "column"
            }} 
            onMouseOver={(e) => {
              e.currentTarget.style.transform = "translateY(-5px)";
              e.currentTarget.style.boxShadow = "0 10px 20px rgba(0,0,0,0.2)";
            }}
            onMouseOut={(e) => {
              e.currentTarget.style.transform = "translateY(0)";
              e.currentTarget.style.boxShadow = "none";
            }}>
              <h3 style={{ margin: 0, fontSize: "1.25rem", color: "var(--text-primary)" }}>{cat.categoryName}</h3>
            </div>
          </Link>
        ))}
      </div>

      {categories.length === 0 && (
        <div style={{ textAlign: "center", padding: "4rem", color: "var(--text-secondary)" }}>
          No categories found.
        </div>
      )}
    </div>
  );
}
