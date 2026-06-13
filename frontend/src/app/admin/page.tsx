"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useAuth } from "@/context/AuthContext";
import { fetchCoreApi, fetchRecApi } from "@/lib/api";
import { Button } from "@/components/Button";
import styles from "./dashboard.module.css";

interface DashboardStats {
  totalUsers: number;
  totalProducts: number;
  totalOrders: number;
  totalRevenue: number;
  recentOrders: any[];
  ordersByStatus: Record<string, number>;
}

export default function AdminDashboard() {
  const { user } = useAuth();
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [engineOnline, setEngineOnline] = useState(false);
  const [loading, setLoading] = useState(true);
  const [rebuilding, setRebuilding] = useState(false);
  const [rebuildMsg, setRebuildMsg] = useState("");

  useEffect(() => {
    loadDashboard();
  }, []);

  async function loadDashboard() {
    setLoading(true);
    try {
      const [usersData, productsData, ordersData] = await Promise.all([
        fetchCoreApi("/users?page=1&limit=1000", { requireAuth: true }).catch(() => []),
        fetchCoreApi("/products?page=1&limit=1000").catch(() => []),
        fetchCoreApi("/orders?page=1&limit=1000", { requireAuth: true }).catch(() => []),
      ]);

      const users = Array.isArray(usersData) ? usersData : (usersData?.content || []);
      const products = Array.isArray(productsData) ? productsData : (productsData?.content || []);
      const orders = Array.isArray(ordersData) ? ordersData : (ordersData?.content || []);

      const totalRevenue = orders.reduce((sum: number, o: any) => sum + (o.totalAmount || 0), 0);

      const ordersByStatus: Record<string, number> = {};
      orders.forEach((o: any) => {
        const s = o.orderStatus || "UNKNOWN";
        ordersByStatus[s] = (ordersByStatus[s] || 0) + 1;
      });

      const sortedOrders = [...orders].sort((a: any, b: any) => {
        if (!a.orderDate) return 1;
        if (!b.orderDate) return -1;
        return new Date(b.orderDate).getTime() - new Date(a.orderDate).getTime();
      });

      setStats({
        totalUsers: users.length,
        totalProducts: products.length,
        totalOrders: orders.length,
        totalRevenue,
        recentOrders: sortedOrders.slice(0, 5),
        ordersByStatus,
      });

      // Check recommendation engine health
      try {
        const health = await fetchRecApi("/health");
        setEngineOnline(!!health?.engineReady);
      } catch {
        setEngineOnline(false);
      }
    } catch (err) {
      console.error("Failed to load dashboard", err);
    } finally {
      setLoading(false);
    }
  }

  async function handleRebuild() {
    setRebuilding(true);
    setRebuildMsg("");
    try {
      const res = await fetchRecApi("/admin/rebuild", { method: "POST", requireAuth: true });
      setRebuildMsg(`✅ Rebuilt — ${res.productCount} products, ${res.userCount} users`);
      setEngineOnline(true);
    } catch (err: any) {
      setRebuildMsg(`❌ Failed: ${err.message}`);
    } finally {
      setRebuilding(false);
    }
  }

  function fmt(amount: number) {
    return "₺" + amount.toLocaleString("tr-TR", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  function statusColor(s: string) {
    switch (s) {
      case "DELIVERED": return { bg: "#ECFDF5", fg: "#065F46" };
      case "CANCELLED": return { bg: "#FEF2F2", fg: "#991B1B" };
      case "SHIPPED": return { bg: "#FFF7ED", fg: "#9A3412" };
      case "PROCESSING": return { bg: "#EFF6FF", fg: "#1E40AF" };
      default: return { bg: "#F1F5F9", fg: "#475569" };
    }
  }

  if (loading) {
    return (
      <div className={styles.loadingBox}>
        <div className={styles.spinner}></div>
        <p>Loading dashboard...</p>
      </div>
    );
  }

  return (
    <div className={styles.dashboard}>
      {/* Header */}
      <div className={styles.header}>
        <div>
          <h1>Dashboard</h1>
          <p>Welcome back, {user?.firstName}. Here's your store overview.</p>
        </div>
        <Button variant="outline" onClick={loadDashboard}>↻ Refresh</Button>
      </div>

      {/* KPI Cards */}
      <div className={styles.kpiGrid}>
        <div className={`glass-panel ${styles.kpi}`}>
          <div className={styles.kpiIcon} style={{ background: "linear-gradient(135deg, #305CDE, #6366F1)" }}>₺</div>
          <div className={styles.kpiBody}>
            <span className={styles.kpiLabel}>Total Revenue</span>
            <span className={styles.kpiValue}>{fmt(stats?.totalRevenue || 0)}</span>
          </div>
        </div>
        <div className={`glass-panel ${styles.kpi}`}>
          <div className={styles.kpiIcon} style={{ background: "linear-gradient(135deg, #10B981, #34D399)" }}>🛒</div>
          <div className={styles.kpiBody}>
            <span className={styles.kpiLabel}>Total Orders</span>
            <span className={styles.kpiValue}>{stats?.totalOrders || 0}</span>
          </div>
        </div>
        <div className={`glass-panel ${styles.kpi}`}>
          <div className={styles.kpiIcon} style={{ background: "linear-gradient(135deg, #F59E0B, #FBBF24)" }}>👤</div>
          <div className={styles.kpiBody}>
            <span className={styles.kpiLabel}>Registered Users</span>
            <span className={styles.kpiValue}>{stats?.totalUsers || 0}</span>
          </div>
        </div>
        <div className={`glass-panel ${styles.kpi}`}>
          <div className={styles.kpiIcon} style={{ background: "linear-gradient(135deg, #8B5CF6, #A78BFA)" }}>📦</div>
          <div className={styles.kpiBody}>
            <span className={styles.kpiLabel}>Products in Catalog</span>
            <span className={styles.kpiValue}>{stats?.totalProducts || 0}</span>
          </div>
        </div>
      </div>

      {/* Middle Row */}
      <div className={styles.midRow}>
        {/* Order Status Breakdown */}
        <div className={`glass-panel ${styles.card}`}>
          <h2 className={styles.cardTitle}>Order Status Breakdown</h2>
          {stats?.ordersByStatus && Object.keys(stats.ordersByStatus).length > 0 ? (
            <div className={styles.statusList}>
              {Object.entries(stats.ordersByStatus)
                .sort(([, a], [, b]) => b - a)
                .map(([status, count]) => {
                  const c = statusColor(status);
                  const pct = stats.totalOrders > 0 ? Math.round((count / stats.totalOrders) * 100) : 0;
                  return (
                    <div key={status} className={styles.statusRow}>
                      <div className={styles.statusInfo}>
                        <span className={styles.badge} style={{ backgroundColor: c.bg, color: c.fg }}>{status}</span>
                        <span className={styles.statusCount}>{count}</span>
                      </div>
                      <div className={styles.barTrack}>
                        <div className={styles.barFill} style={{ width: `${pct}%`, backgroundColor: c.fg }}></div>
                      </div>
                      <span className={styles.pct}>{pct}%</span>
                    </div>
                  );
                })}
            </div>
          ) : (
            <p className={styles.empty}>No orders yet.</p>
          )}
        </div>

        {/* AI Engine */}
        <div className={`glass-panel ${styles.card}`}>
          <h2 className={styles.cardTitle}>
            AI Recommendation Engine
            <span className={styles.badge} style={{
              backgroundColor: engineOnline ? "#ECFDF5" : "#FEF2F2",
              color: engineOnline ? "#065F46" : "#991B1B",
              marginLeft: "0.75rem"
            }}>
              {engineOnline ? "● Online" : "● Offline"}
            </span>
          </h2>
          <div className={styles.engineTable}>
            <div className={styles.eRow}><span>Algorithm</span><span>Hybrid CF + CBF Cosine</span></div>
            <div className={styles.eRow}><span>Blending</span><span>70% CF / 30% CBF</span></div>
            <div className={styles.eRow}><span>Auto-Rebuild</span><span>Every 30 minutes</span></div>
            <div className={styles.eRow}><span>Cold-Start</span><span>Popularity Fallback</span></div>
          </div>
          <Button onClick={handleRebuild} disabled={rebuilding} variant="secondary" fullWidth>
            {rebuilding ? "Rebuilding..." : "⟳ Rebuild Similarity Matrix"}
          </Button>
          {rebuildMsg && <p className={styles.rebuildMsg}>{rebuildMsg}</p>}
        </div>
      </div>

      {/* Recent Orders */}
      <div className={`glass-panel ${styles.card}`}>
        <div className={styles.cardHeader}>
          <h2 className={styles.cardTitle}>Recent Orders</h2>
          <Link href="/admin/orders" className={styles.viewAll}>View All →</Link>
        </div>
        {stats?.recentOrders && stats.recentOrders.length > 0 ? (
          <div className={styles.tableWrap}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Order ID</th>
                  <th>Date</th>
                  <th>Items</th>
                  <th>Status</th>
                  <th>Payment</th>
                  <th style={{ textAlign: "right" }}>Total</th>
                </tr>
              </thead>
              <tbody>
                {stats.recentOrders.map((o, i) => {
                  const c = statusColor(o.orderStatus);
                  return (
                    <tr key={o.orderId || i}>
                      <td className={styles.mono}>{o.orderId?.substring(0, 10)}...</td>
                      <td className={styles.muted}>
                        {o.orderDate ? new Date(o.orderDate).toLocaleDateString("en-GB", {
                          day: "2-digit", month: "short", year: "numeric"
                        }) : "—"}
                      </td>
                      <td className={styles.muted}>{o.orderItems?.length || 0} item{(o.orderItems?.length || 0) !== 1 ? "s" : ""}</td>
                      <td><span className={styles.badge} style={{ backgroundColor: c.bg, color: c.fg }}>{o.orderStatus}</span></td>
                      <td className={styles.muted}>{o.paymentMethod?.replace(/_/g, " ")}</td>
                      <td style={{ textAlign: "right", fontWeight: 600 }}>{fmt(o.totalAmount || 0)}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        ) : (
          <p className={styles.empty}>No orders yet.</p>
        )}
      </div>

      {/* Quick Links */}
      <div className={styles.linksGrid}>
        <Link href="/admin/products" className={`glass-panel ${styles.qlink}`}>
          <span className={styles.qlinkIcon}>📦</span>
          <span className={styles.qlinkTitle}>Manage Products</span>
          <span className={styles.qlinkSub}>{stats?.totalProducts || 0} items</span>
        </Link>
        <Link href="/admin/users" className={`glass-panel ${styles.qlink}`}>
          <span className={styles.qlinkIcon}>👥</span>
          <span className={styles.qlinkTitle}>Manage Users</span>
          <span className={styles.qlinkSub}>{stats?.totalUsers || 0} accounts</span>
        </Link>
        <Link href="/admin/orders" className={`glass-panel ${styles.qlink}`}>
          <span className={styles.qlinkIcon}>🛒</span>
          <span className={styles.qlinkTitle}>Manage Orders</span>
          <span className={styles.qlinkSub}>{stats?.totalOrders || 0} orders</span>
        </Link>
        <Link href="/" className={`glass-panel ${styles.qlink}`}>
          <span className={styles.qlinkIcon}>🏪</span>
          <span className={styles.qlinkTitle}>View Store</span>
          <span className={styles.qlinkSub}>Customer view</span>
        </Link>
      </div>
    </div>
  );
}
