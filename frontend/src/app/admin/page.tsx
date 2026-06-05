"use client";

import { useAuth } from "@/context/AuthContext";

export default function AdminDashboard() {
  const { user } = useAuth();

  return (
    <div>
      <h1 style={{ fontSize: "2rem", color: "var(--accent-primary)", marginBottom: "1rem", fontFamily: "var(--font-outfit)" }}>
        Dashboard
      </h1>
      <p style={{ color: "var(--text-secondary)", marginBottom: "2rem" }}>
        Welcome back, {user?.firstName} {user?.lastName}. Here's an overview of your store.
      </p>

      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(240px, 1fr))", gap: "1.5rem" }}>
        <div className="glass-panel" style={{ padding: "1.5rem", borderLeft: "4px solid var(--accent-primary)" }}>
          <h3 style={{ color: "var(--text-secondary)", fontSize: "0.875rem", marginBottom: "0.5rem" }}>Total Sales</h3>
          <p style={{ fontSize: "2rem", fontWeight: "bold", color: "var(--text-primary)" }}>$12,450</p>
        </div>
        
        <div className="glass-panel" style={{ padding: "1.5rem", borderLeft: "4px solid #10B981" }}>
          <h3 style={{ color: "var(--text-secondary)", fontSize: "0.875rem", marginBottom: "0.5rem" }}>Active Users</h3>
          <p style={{ fontSize: "2rem", fontWeight: "bold", color: "var(--text-primary)" }}>1,248</p>
        </div>
        
        <div className="glass-panel" style={{ padding: "1.5rem", borderLeft: "4px solid #F59E0B" }}>
          <h3 style={{ color: "var(--text-secondary)", fontSize: "0.875rem", marginBottom: "0.5rem" }}>Pending Orders</h3>
          <p style={{ fontSize: "2rem", fontWeight: "bold", color: "var(--text-primary)" }}>42</p>
        </div>
      </div>
    </div>
  );
}
