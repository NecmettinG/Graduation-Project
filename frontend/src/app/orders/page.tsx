"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { useRouter } from "next/navigation";
import { fetchCoreApi } from "@/lib/api";

export default function OrdersPage() {
  const { user, loading: authLoading } = useAuth();
  const router = useRouter();
  const [orders, setOrders] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (authLoading) return;
    if (!user) {
      router.push("/login");
      return;
    }

    async function fetchOrders() {
      try {
        const data = await fetchCoreApi(`/users/${user?.userId}/orders`, { requireAuth: true });
        setOrders(Array.isArray(data) ? data : (data.content || []));
      } catch (err) {
        console.warn("Failed to fetch orders", err);
      } finally {
        setLoading(false);
      }
    }

    fetchOrders();
  }, [user, authLoading, router]);

  if (authLoading || loading) return <div className="container" style={{ padding: "4rem 0", textAlign: "center" }}>Loading orders...</div>;

  return (
    <div className="container" style={{ padding: "4rem 1.5rem", minHeight: "60vh" }}>
      <h1 style={{ fontSize: "2.5rem", color: "var(--accent-primary)", marginBottom: "2rem", fontFamily: "var(--font-outfit)" }}>
        Order History
      </h1>

      {orders.length === 0 ? (
        <div className="glass-panel" style={{ padding: "4rem 2rem", textAlign: "center" }}>
          <h2 style={{ marginBottom: "1rem" }}>No orders found</h2>
          <p style={{ color: "var(--text-secondary)" }}>When you place orders, they will appear here.</p>
        </div>
      ) : (
        <div style={{ display: "flex", flexDirection: "column", gap: "1.5rem" }}>
          {orders.map((order, idx) => (
            <div key={idx} className="glass-panel" style={{ padding: "1.5rem" }}>
              <div style={{ display: "flex", justifyContent: "space-between", borderBottom: "1px solid var(--border-color)", paddingBottom: "1rem", marginBottom: "1rem" }}>
                <strong>Order #{order.orderId || idx + 1000}</strong>
                <span style={{ color: "var(--text-secondary)" }}>{order.orderDate ? new Date(order.orderDate).toLocaleDateString() : "Recent"}</span>
              </div>
              <div>
                Total Amount: <strong style={{ color: "var(--accent-primary)" }}>${order.totalAmount?.toFixed(2) || "0.00"}</strong>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
