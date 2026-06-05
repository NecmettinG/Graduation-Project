import Link from "next/link";
import { Button } from "./Button";
import styles from "./ProductCard.module.css";

interface Product {
  productId: string;
  name: string;
  price: number;
  description?: string;
  imageUrl?: string;
}

interface ProductCardProps {
  product: Product;
}

export function ProductCard({ product }: ProductCardProps) {
  return (
    <div className={`glass-panel ${styles.card}`}>
      <Link href={`/products/${product.productId}`}>
        <div className={styles.imageContainer}>
          {product.imageUrl ? (
            <img src={product.imageUrl} alt={product.name} className={styles.image} />
          ) : (
            <div className={styles.placeholderImage}>No Image</div>
          )}
        </div>
        <div className={styles.details}>
          <h3 className={styles.title}>{product.name}</h3>
          <p className={styles.price}>${product.price.toFixed(2)}</p>
        </div>
      </Link>
      <div className={styles.actions}>
        <Button variant="secondary" fullWidth>Add to Cart</Button>
      </div>
    </div>
  );
}
