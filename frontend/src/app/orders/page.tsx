"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { useRouter } from "next/navigation";
import { fetchCoreApi } from "@/lib/api";
import { OrderSkeleton } from "@/components/Skeleton";

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
        const ordersList = Array.isArray(data) ? data : (data.content || []);
        // Sort orders by date descending (newest first)
        ordersList.sort((a: any, b: any) => {
          if (!a.orderDate) return 1;
          if (!b.orderDate) return -1;
          return new Date(b.orderDate).getTime() - new Date(a.orderDate).getTime();
        });
        setOrders(ordersList);
      } catch (err) {
        console.warn("Failed to fetch orders", err);
      } finally {
        setLoading(false);
      }
    }

    fetchOrders();
  }, [user, authLoading, router]);

  if (authLoading || loading) return (
    <div className="container" style={{ padding: "4rem 0", display: "flex", flexDirection: "column", gap: "1rem" }}>
      <OrderSkeleton />
      <OrderSkeleton />
      <OrderSkeleton />
    </div>
  );

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
                <div>
                  <strong>Order #{order.orderId || idx + 1000}</strong>
                  <div style={{ fontSize: "0.875rem", color: "var(--text-secondary)", marginTop: "0.25rem" }}>
                    Status: <span style={{ color: "var(--accent-primary)", fontWeight: 500 }}>{order.orderStatus || "PROCESSING"}</span>
                  </div>
                </div>
                <div style={{ textAlign: "right" }}>
                  <span style={{ color: "var(--text-secondary)" }}>{order.orderDate ? new Date(order.orderDate).toLocaleDateString() : "Recent"}</span>
                  <div style={{ fontSize: "0.875rem", color: "var(--text-secondary)", marginTop: "0.25rem" }}>
                    Payment: <span style={{ fontWeight: 500 }}>{order.paymentStatus || "PAID"}</span>
                  </div>
                </div>
              </div>
              
              <div style={{ marginBottom: "1.5rem" }}>
                {order.orderItems && order.orderItems.length > 0 ? (
                  <ul style={{ listStyleType: "none", padding: 0, margin: 0 }}>
                    {order.orderItems.map((item: any, i: number) => {
                      const product = item.product || {};
                      return (
                        <li key={i} style={{ display: "flex", justifyContent: "space-between", padding: "0.5rem 0", fontSize: "0.95rem" }}>
                          <span>{item.quantity}x {product.productName || "Product"}</span>
                          <span>₺{(product.price * item.quantity).toFixed(2)}</span>
                        </li>
                      );
                    })}
                  </ul>
                ) : (
                  <div style={{ color: "var(--text-muted)", fontSize: "0.9rem" }}>No item details available.</div>
                )}
              </div>

              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", borderTop: "1px dashed var(--border-color)", paddingTop: "1rem" }}>
                <span style={{ color: "var(--text-secondary)" }}>Shipping to: {order.shippingAddress || "Default Address"}</span>
                <div>
                  Total Amount: <strong style={{ color: "var(--accent-primary)", fontSize: "1.25rem", marginLeft: "0.5rem" }}>₺{order.totalAmount?.toFixed(2) || "0.00"}</strong>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
