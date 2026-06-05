"use client";

import { useEffect, useState } from "react";
import { fetchCoreApi } from "@/lib/api";
import { Button } from "@/components/Button";

export default function AdminProducts() {
  const [products, setProducts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchProducts();
  }, []);

  async function fetchProducts() {
    setLoading(true);
    try {
      const data = await fetchCoreApi("/products?page=1&limit=50");
      setProducts(Array.isArray(data) ? data : (data.content || []));
    } catch (err) {
      console.error("Failed to load products", err);
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete(id: string) {
    if (!confirm("Are you sure you want to delete this product?")) return;
    try {
      await fetchCoreApi(`/products/${id}`, { method: "DELETE", requireAuth: true });
      setProducts(prev => prev.filter(p => p.productId !== id));
    } catch (err) {
      alert("Failed to delete product");
    }
  }

  return (
    <div>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "2rem" }}>
        <h1 style={{ fontSize: "2rem", color: "var(--accent-primary)", fontFamily: "var(--font-outfit)" }}>
          Manage Products
        </h1>
        <Button>Add New Product</Button>
      </div>

      <div className="glass-panel" style={{ overflowX: "auto" }}>
        {loading ? (
          <div style={{ padding: "3rem", textAlign: "center", color: "var(--text-secondary)" }}>Loading products...</div>
        ) : (
          <table style={{ width: "100%", borderCollapse: "collapse", textAlign: "left" }}>
            <thead>
              <tr style={{ borderBottom: "1px solid var(--border-color)", backgroundColor: "var(--bg-base)" }}>
                <th style={{ padding: "1rem", color: "var(--text-secondary)", fontWeight: 600 }}>ID</th>
                <th style={{ padding: "1rem", color: "var(--text-secondary)", fontWeight: 600 }}>Image</th>
                <th style={{ padding: "1rem", color: "var(--text-secondary)", fontWeight: 600 }}>Name</th>
                <th style={{ padding: "1rem", color: "var(--text-secondary)", fontWeight: 600 }}>Price</th>
                <th style={{ padding: "1rem", color: "var(--text-secondary)", fontWeight: 600 }}>Stock</th>
                <th style={{ padding: "1rem", color: "var(--text-secondary)", fontWeight: 600 }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {products.map((p, idx) => (
                <tr key={p.productId || idx} style={{ borderBottom: "1px solid var(--border-color)" }}>
                  <td style={{ padding: "1rem", color: "var(--text-muted)", fontSize: "0.875rem" }}>
                    {p.productId?.substring(0, 8)}...
                  </td>
                  <td style={{ padding: "1rem" }}>
                    {p.imageUrls && p.imageUrls.length > 0 ? (
                      <img src={p.imageUrls[0]} alt={p.productName} style={{ width: "40px", height: "40px", objectFit: "cover", borderRadius: "4px" }} />
                    ) : (
                      <div style={{ width: "40px", height: "40px", backgroundColor: "#E2E8F0", borderRadius: "4px" }}></div>
                    )}
                  </td>
                  <td style={{ padding: "1rem", fontWeight: 500, color: "var(--text-primary)" }}>{p.productName}</td>
                  <td style={{ padding: "1rem", color: "var(--text-secondary)" }}>${p.price?.toFixed(2)}</td>
                  <td style={{ padding: "1rem", color: "var(--text-secondary)" }}>{p.stock}</td>
                  <td style={{ padding: "1rem" }}>
                    <div style={{ display: "flex", gap: "0.5rem" }}>
                      <button style={{ color: "var(--accent-primary)", background: "none", border: "none", cursor: "pointer", fontWeight: 500 }}>Edit</button>
                      <button onClick={() => handleDelete(p.productId)} style={{ color: "#EF4444", background: "none", border: "none", cursor: "pointer", fontWeight: 500 }}>Delete</button>
                    </div>
                  </td>
                </tr>
              ))}
              {products.length === 0 && (
                <tr>
                  <td colSpan={6} style={{ padding: "3rem", textAlign: "center", color: "var(--text-secondary)" }}>
                    No products found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
