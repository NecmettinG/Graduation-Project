"use client";

import styles from "./Footer.module.css";
import Link from "next/link";
import { usePathname } from "next/navigation";

export function Footer() {
  const pathname = usePathname();

  if (pathname.startsWith("/admin")) return null;

  return (
    <footer className={styles.footer}>
      <div className={`container ${styles.footerContainer}`}>
        <div className={styles.brand}>
          <h3>Smarty<span>Commerce</span></h3>
          <p>The premium e-commerce experience curated for you.</p>
        </div>
        
        <div className={styles.links}>
          <div className={styles.linkColumn}>
            <h4>Shop</h4>
            <Link href="/products">All Products</Link>
            <Link href="/wishlist">Wishlist</Link>
          </div>
          <div className={styles.linkColumn}>
            <h4>Account</h4>
            <Link href="/orders">My Orders</Link>
            <Link href="/profile">Profile</Link>
          </div>
        </div>
      </div>
      <div className={styles.bottomBar}>
        <div className="container">
          <p>&copy; {new Date().getFullYear()} Smarty Commerce. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
}
