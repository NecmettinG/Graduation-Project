import styles from "./Skeleton.module.css";

interface SkeletonProps {
  width?: string;
  height?: string;
  borderRadius?: string;
  className?: string;
}

/** A single pulsing skeleton rectangle. */
export function Skeleton({ width = "100%", height = "1rem", borderRadius = "8px", className = "" }: SkeletonProps) {
  return <div className={`${styles.skeleton} ${className}`} style={{ width, height, borderRadius }} />;
}

/** Skeleton for a ProductCard. */
export function ProductCardSkeleton() {
  return (
    <div className={`glass-panel ${styles.cardSkeleton}`}>
      <Skeleton height="200px" borderRadius="12px 12px 0 0" />
      <div className={styles.cardBody}>
        <Skeleton width="75%" height="1rem" />
        <Skeleton width="40%" height="1.25rem" />
        <Skeleton height="2.5rem" borderRadius="12px" />
      </div>
    </div>
  );
}

/** Grid of ProductCard skeletons. */
export function ProductGridSkeleton({ count = 8 }: { count?: number }) {
  return (
    <div className={styles.grid}>
      {Array.from({ length: count }).map((_, i) => (
        <ProductCardSkeleton key={i} />
      ))}
    </div>
  );
}

/** Skeleton for product detail page. */
export function ProductDetailSkeleton() {
  return (
    <div className={styles.detailLayout}>
      <div className={styles.detailLeft}>
        <Skeleton height="420px" borderRadius="12px" />
        <div className={styles.detailThumbs}>
          {Array.from({ length: 3 }).map((_, i) => (
            <Skeleton key={i} width="72px" height="72px" borderRadius="8px" />
          ))}
        </div>
      </div>
      <div className={styles.detailRight}>
        <Skeleton width="60%" height="2.5rem" />
        <Skeleton width="90px" height="1.5rem" borderRadius="20px" />
        <Skeleton width="35%" height="2rem" />
        <Skeleton height="1rem" />
        <Skeleton height="1rem" />
        <Skeleton width="80%" height="1rem" />
        <div style={{ marginTop: "1rem" }}>
          <Skeleton height="1rem" borderRadius="0" />
          <Skeleton height="1rem" borderRadius="0" />
          <Skeleton height="1rem" borderRadius="0" />
        </div>
        <div className={styles.detailActions}>
          <Skeleton height="3rem" borderRadius="12px" />
          <Skeleton width="120px" height="3rem" borderRadius="12px" />
        </div>
      </div>
    </div>
  );
}

/** Skeleton for an order card. */
export function OrderSkeleton() {
  return (
    <div className={`glass-panel ${styles.orderSkeleton}`}>
      <div className={styles.orderHeader}>
        <Skeleton width="200px" height="1rem" />
        <Skeleton width="100px" height="1.5rem" borderRadius="6px" />
      </div>
      <Skeleton height="1rem" />
      <Skeleton width="60%" height="1rem" />
      <div className={styles.orderFooter}>
        <Skeleton width="40%" height="1rem" />
        <Skeleton width="100px" height="1.5rem" />
      </div>
    </div>
  );
}

/** Skeleton for cart items. */
export function CartSkeleton() {
  return (
    <div className={styles.cartLayout}>
      <div className={styles.cartItems}>
        {Array.from({ length: 3 }).map((_, i) => (
          <div key={i} className={`glass-panel ${styles.cartItem}`}>
            <Skeleton width="100px" height="100px" borderRadius="8px" />
            <div className={styles.cartItemDetails}>
              <Skeleton width="70%" height="1.2rem" />
              <Skeleton width="30%" height="1rem" />
              <Skeleton width="120px" height="2rem" borderRadius="8px" />
            </div>
          </div>
        ))}
      </div>
      <div className={`glass-panel ${styles.cartSummary}`}>
        <Skeleton width="50%" height="1.5rem" />
        <Skeleton height="1rem" />
        <Skeleton height="1rem" />
        <Skeleton height="2px" />
        <Skeleton height="1.5rem" />
        <Skeleton height="3rem" borderRadius="12px" />
      </div>
    </div>
  );
}
