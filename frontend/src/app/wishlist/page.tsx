"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { useRouter } from "next/navigation";
import { fetchCoreApi } from "@/lib/api";
import { ProductCard } from "@/components/ProductCard";

export default function WishlistPage() {
  const { user, loading: authLoading } = useAuth();
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

  if (authLoading || loading) return <div className="container" style={{ padding: "4rem 0", textAlign: "center" }}>Loading wishlist...</div>;

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
          {wishlistItems.map((item, idx) => (
            <ProductCard key={item.id || idx} product={item.product || item} />
          ))}
        </div>
      )}
    </div>
  );
}
