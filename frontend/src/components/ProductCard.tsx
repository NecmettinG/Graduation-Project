import Link from "next/link";
import { useRouter } from "next/navigation";
import { Button } from "./Button";
import { useAuth } from "@/context/AuthContext";
import { fetchCoreApi } from "@/lib/api";
import styles from "./ProductCard.module.css";

interface Product {
  productId: string;
  productName: string;
  price: number;
  description?: string;
  imageUrls?: string[];
}

interface ProductCardProps {
  product: Product;
}

export function ProductCard({ product }: ProductCardProps) {
  const { user } = useAuth();
  const router = useRouter();

  const handleAddToCart = async (e: React.MouseEvent) => {
    e.preventDefault(); // Prevent link navigation if button clicked
    if (!user) {
      alert("Please login to add items to your cart.");
      router.push("/login");
      return;
    }

    try {
      await fetchCoreApi(`/users/${user.userId}/cart/items`, {
        method: "POST",
        requireAuth: true,
        body: JSON.stringify({
          productId: product.productId,
          quantity: 1
        })
      });
      alert(`${product.productName} added to cart!`);
    } catch (err) {
      console.error(err);
      alert("Failed to add to cart.");
    }
  };

  return (
    <div className={`glass-panel ${styles.card}`}>
      <Link href={`/products/${product.productId}`} style={{ textDecoration: 'none', color: 'inherit' }}>
        <div className={styles.imageContainer}>
          {product.imageUrls && product.imageUrls.length > 0 ? (
            <img src={product.imageUrls[0]} alt={product.productName} className={styles.image} />
          ) : (
            <div className={styles.placeholderImage}>No Image</div>
          )}
        </div>
        <div className={styles.details}>
          <h3 className={styles.title}>{product.productName}</h3>
          <p className={styles.price}>${product.price.toFixed(2)}</p>
        </div>
      </Link>
      <div className={styles.actions}>
        <Button variant="secondary" fullWidth onClick={handleAddToCart}>Add to Cart</Button>
      </div>
    </div>
  );
}
