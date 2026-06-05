"use client";

import { useEffect, useState } from "react";
import { useRouter, usePathname } from "next/navigation";
import Link from "next/link";
import { useAuth } from "@/context/AuthContext";
import { fetchCoreApi } from "@/lib/api";
import styles from "./layout.module.css";

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const { user, loading: authLoading, logout } = useAuth();
  const router = useRouter();
  const pathname = usePathname();
  const [isAdmin, setIsAdmin] = useState(false);
  const [verifying, setVerifying] = useState(true);

  useEffect(() => {
    if (authLoading) return;
    
    if (!user) {
      router.push("/login");
      return;
    }

    async function verifyAdmin() {
      try {
        // Optimistic verification: try to hit an admin-only endpoint
        // Using GET /users with limit 1 just to check access
        await fetchCoreApi("/users?page=1&limit=1", { requireAuth: true });
        setIsAdmin(true);
      } catch (err: any) {
        console.error("Admin verification failed", err);
        // If 403 Forbidden or other error, redirect to home
        router.push("/");
      } finally {
        setVerifying(false);
      }
    }

    verifyAdmin();
  }, [user, authLoading, router]);

  if (authLoading || verifying) {
    return <div className={styles.loadingContainer}>Verifying admin access...</div>;
  }

  if (!isAdmin) return null;

  return (
    <div className={styles.adminContainer}>
      {/* Admin Sidebar */}
      <aside className={styles.sidebar}>
        <div className={styles.sidebarHeader}>
          <h2>Smarty <span>Admin</span></h2>
        </div>
        <nav className={styles.sidebarNav}>
          <Link href="/admin" className={pathname === "/admin" ? styles.active : ""}>
            Dashboard
          </Link>
          <Link href="/admin/products" className={pathname.startsWith("/admin/products") ? styles.active : ""}>
            Products
          </Link>
          <Link href="/admin/users" className={pathname.startsWith("/admin/users") ? styles.active : ""}>
            Users
          </Link>
          <Link href="/admin/orders" className={pathname.startsWith("/admin/orders") ? styles.active : ""}>
            Orders
          </Link>
        </nav>
        <div className={styles.sidebarFooter}>
          <button onClick={() => { logout(); router.push("/login"); }} className={styles.logoutBtn}>
            Logout
          </button>
        </div>
      </aside>

      {/* Main Admin Content */}
      <main className={styles.mainContent}>
        {children}
      </main>
    </div>
  );
}
