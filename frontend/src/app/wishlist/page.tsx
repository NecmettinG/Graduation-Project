"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { useToast } from "@/context/ToastContext";
import { useRouter } from "next/navigation";
import { fetchCoreApi } from "@/lib/api";
import { ProductCard } from "@/components/ProductCard";
import { ProductGridSkeleton } from "@/components/Skeleton";

export default function WishlistPage() {
  const { user, loading: authLoading } = useAuth();
  const { toast } = useToast();
  const router = useRouter();
  const [wishlistItems, setWishlistItems] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (authLoading) return;
    if (!user) {
      router.push("/login");
      return;
    }

    async function fetchWishlist() {
      try {
        const data = await fetchCoreApi(`/users/${user?.userId}/wishlist`, { requireAuth: true });
        setWishlistItems(Array.isArray(data) ? data : (data.content || []));
      } catch (err) {
        console.warn("Failed to fetch wishlist", err);
      } finally {
        setLoading(false);
      }
    }

    fetchWishlist();
  }, [user, authLoading, router]);

  const handleRemove = async (productId: string) => {
    try {
      await fetchCoreApi(`/users/${user?.userId}/wishlist/${productId}`, {
        method: "DELETE",
        requireAuth: true
      });
      setWishlistItems(prev => prev.filter(item => {
        const id = item.product?.productId || item.productId;
        return id !== productId;
      }));
    } catch (err) {
      toast("Failed to remove from wishlist.", "error");
    }
  };

  if (authLoading || loading) return (
    <div className="container" style={{ padding: "4rem 1.5rem" }}>
      <ProductGridSkeleton count={4} />
    </div>
  );

  return (
    <div className="container" style={{ padding: "4rem 1.5rem", minHeight: "60vh" }}>
      <h1 style={{ fontSize: "2.5rem", color: "var(--accent-primary)", marginBottom: "2rem", fontFamily: "var(--font-outfit)" }}>
        My Wishlist
      </h1>

      {wishlistItems.length === 0 ? (
        <div className="glass-panel" style={{ padding: "4rem 2rem", textAlign: "center" }}>
          <h2 style={{ marginBottom: "1rem" }}>Your wishlist is empty</h2>
          <p style={{ color: "var(--text-secondary)" }}>Save items you like here to purchase them later.</p>
        </div>
      ) : (
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(280px, 1fr))", gap: "2rem" }}>
          {wishlistItems.map((item, idx) => {
            const product = item.product || item;
            return (
              <div key={item.id || idx} style={{ position: "relative" }}>
                <ProductCard product={product} />
                <button 
                  onClick={() => handleRemove(product.productId)}
                  style={{
                    position: "absolute",
                    top: "10px",
                    right: "10px",
                    background: "rgba(239, 68, 68, 0.9)",
                    color: "white",
                    border: "none",
                    borderRadius: "50%",
                    width: "32px",
                    height: "32px",
                    cursor: "pointer",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    fontWeight: "bold",
                    zIndex: 10,
                    boxShadow: "0 2px 4px rgba(0,0,0,0.2)"
                  }}
                  title="Remove from Wishlist"
                >
                  ✕
                </button>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
