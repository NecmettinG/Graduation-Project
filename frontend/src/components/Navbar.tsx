"use client";

import Link from "next/link";
import { useAuth } from "@/context/AuthContext";
import styles from "./Navbar.module.css";

export function Navbar() {
  const { user, logout, loading } = useAuth();

  return (
    <header className={styles.header}>
      <div className={`container ${styles.navContainer}`}>
        <Link href="/" className={styles.logo}>
          Smarty<span>Commerce</span>
        </Link>

        <nav className={styles.navLinks}>
          <Link href="/products" className={styles.navLink}>Products</Link>
          <Link href="/cart" className={styles.navLink}>Cart</Link>
          
          {!loading && (
            user ? (
              <div className={styles.userMenu}>
                <span className={styles.greeting}>Hi, {user.firstName}</span>
                <Link href="/profile" className={styles.navLink}>Profile</Link>
                <button onClick={logout} className="btn btn-outline" style={{ padding: '0.4rem 1rem', fontSize: '0.875rem' }}>Logout</button>
              </div>
            ) : (
              <div className={styles.authLinks}>
                <Link href="/login" className="btn btn-outline" style={{ padding: '0.4rem 1rem', fontSize: '0.875rem' }}>Login</Link>
                <Link href="/register" className="btn btn-primary" style={{ padding: '0.4rem 1rem', fontSize: '0.875rem' }}>Sign Up</Link>
              </div>
            )
          )}
        </nav>
      </div>
    </header>
  );
}
