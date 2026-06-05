"use client";

import { useEffect, useState } from "react";
import { fetchCoreApi } from "@/lib/api";

export default function AdminOrders() {
  const [orders, setOrders] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchOrders();
  }, []);

  async function fetchOrders() {
    setLoading(true);
    try {
      const data = await fetchCoreApi("/orders?page=1&limit=50", { requireAuth: true });
      setOrders(Array.isArray(data) ? data : (data.content || []));
    } catch (err) {
      console.error("Failed to load orders", err);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <div style={{ marginBottom: "2rem" }}>
        <h1 style={{ fontSize: "2rem", color: "var(--accent-primary)", fontFamily: "var(--font-outfit)" }}>
          Manage Orders
        </h1>
        <p style={{ color: "var(--text-secondary)" }}>View all system orders across all users.</p>
      </div>

      <div className="glass-panel" style={{ overflowX: "auto" }}>
        {loading ? (
          <div style={{ padding: "3rem", textAlign: "center", color: "var(--text-secondary)" }}>Loading orders...</div>
        ) : (
          <table style={{ width: "100%", borderCollapse: "collapse", textAlign: "left" }}>
            <thead>
              <tr style={{ borderBottom: "1px solid var(--border-color)", backgroundColor: "var(--bg-base)" }}>
                <th style={{ padding: "1rem", color: "var(--text-secondary)", fontWeight: 600 }}>Order ID</th>
                <th style={{ padding: "1rem", color: "var(--text-secondary)", fontWeight: 600 }}>Date</th>
                <th style={{ padding: "1rem", color: "var(--text-secondary)", fontWeight: 600 }}>Customer (User ID)</th>
                <th style={{ padding: "1rem", color: "var(--text-secondary)", fontWeight: 600 }}>Status</th>
                <th style={{ padding: "1rem", color: "var(--text-secondary)", fontWeight: 600 }}>Total</th>
                <th style={{ padding: "1rem", color: "var(--text-secondary)", fontWeight: 600 }}>Payment</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((o, idx) => (
                <tr key={o.orderId || idx} style={{ borderBottom: "1px solid var(--border-color)" }}>
                  <td style={{ padding: "1rem", color: "var(--text-primary)", fontWeight: 500 }}>
                    {o.orderId?.substring(0, 8)}...
                  </td>
                  <td style={{ padding: "1rem", color: "var(--text-secondary)" }}>
                    {o.orderDate ? new Date(o.orderDate).toLocaleDateString() : "N/A"}
                  </td>
                  <td style={{ padding: "1rem", color: "var(--text-muted)", fontSize: "0.875rem" }}>
                    {o.user?.userId?.substring(0,8)}...
                  </td>
                  <td style={{ padding: "1rem" }}>
                    <span style={{ 
                      padding: "0.25rem 0.5rem", 
                      borderRadius: "4px", 
                      fontSize: "0.75rem", 
                      fontWeight: 600,
                      backgroundColor: o.orderStatus === "DELIVERED" ? "#ECFDF5" : o.orderStatus === "CANCELLED" ? "#FEF2F2" : "#EFF6FF",
                      color: o.orderStatus === "DELIVERED" ? "#065F46" : o.orderStatus === "CANCELLED" ? "#991B1B" : "#1E40AF"
                    }}>
                      {o.orderStatus}
                    </span>
                  </td>
                  <td style={{ padding: "1rem", fontWeight: 600, color: "var(--text-primary)" }}>${o.totalAmount?.toFixed(2)}</td>
                  <td style={{ padding: "1rem", color: "var(--text-secondary)", fontSize: "0.875rem" }}>
                    {o.paymentMethod} <br/>
                    ({o.paymentStatus})
                  </td>
                </tr>
              ))}
              {orders.length === 0 && (
                <tr>
                  <td colSpan={6} style={{ padding: "3rem", textAlign: "center", color: "var(--text-secondary)" }}>
                    No orders found.
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
