import styles from "./Footer.module.css";
import Link from "next/link";

export function Footer() {
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
            <Link href="/categories">Categories</Link>
          </div>
          <div className={styles.linkColumn}>
            <h4>Support</h4>
            <Link href="/faq">FAQ</Link>
            <Link href="/contact">Contact Us</Link>
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
