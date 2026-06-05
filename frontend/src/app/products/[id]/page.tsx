"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { fetchCoreApi, fetchRecApi } from "@/lib/api";
import { useAuth } from "@/context/AuthContext";
import { Button } from "@/components/Button";
import { ProductCard } from "@/components/ProductCard";
import styles from "./page.module.css";

export default function ProductDetailPage() {
  const params = useParams();
  const id = params.id as string;

  const [product, setProduct] = useState<any>(null);
  const [similarProducts, setSimilarProducts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [addingToCart, setAddingToCart] = useState(false);

  useEffect(() => {
    if (!id) return;

    async function loadData() {
      try {
        // Fetch core product details
        const prodData = await fetchCoreApi(`/products/${id}`);
        setProduct(prodData);

        // Fetch Similar Items from recommendation service
        try {
          const products = await fetchCoreApi(`/products/${id}/recommendations?limit=4`);
          if (products && products.length > 0) {
            setSimilarProducts(products);
          }
        } catch (err) {
          console.warn("Could not fetch similar items", err);
        }
      } catch (err) {
        console.error("Failed to load product", err);
      } finally {
        setLoading(false);
      }
    }
    
    loadData();
  }, [id]);

  const { user } = useAuth();
  const router = useRouter();

  const handleAddToCart = async () => {
    if (!user) {
      alert("Please login to add items to your cart.");
      router.push("/login");
      return;
    }

    setAddingToCart(true);
    try {
      await fetchCoreApi(`/users/${user.userId}/cart/items`, {
        method: "POST",
        requireAuth: true,
        body: JSON.stringify({
          productId: id,
          quantity: 1
        })
      });
      alert(`${product.productName} added to cart!`);
    } catch (err) {
      console.error(err);
      alert("Failed to add to cart");
    } finally {
      setAddingToCart(false);
    }
  };

  const handleWishlist = async () => {
    if (!user) {
      alert("Please login to add items to your wishlist.");
      router.push("/login");
      return;
    }

    try {
      await fetchCoreApi(`/users/${user.userId}/wishlist/${id}`, {
        method: "POST",
        requireAuth: true
      });
      alert(`${product.productName} added to wishlist!`);
    } catch (err) {
      console.error(err);
      alert("Failed to add to wishlist");
    }
  };

  if (loading) return <div className="container" style={{ padding: "4rem 0", textAlign: "center" }}>Loading...</div>;
  if (!product) return <div className="container" style={{ padding: "4rem 0", textAlign: "center" }}>Product not found</div>;

  return (
    <div className={styles.container}>
      <div className={`container ${styles.productLayout}`}>
        <div className={styles.imageGallery}>
          {product.imageUrls && product.imageUrls.length > 0 ? (
            <img src={product.imageUrls[0]} alt={product.productName} className={styles.mainImage} />
          ) : (
            <div className={styles.placeholderImage}>No Image Available</div>
          )}
        </div>
        
        <div className={styles.productInfo}>
          <h1 className={styles.title}>{product.productName}</h1>
          <div className={styles.price}>${product.price?.toFixed(2)}</div>
          
          <div className={styles.description}>
            {product.description || "No description provided for this premium item. Crafted with excellence."}
          </div>
          
          <div className={styles.actions}>
            <Button onClick={handleAddToCart} disabled={addingToCart} className={styles.addBtn}>
              {addingToCart ? "Adding..." : "Add to Cart"}
            </Button>
            <Button variant="outline" className={styles.wishlistBtn} onClick={handleWishlist}>
              ♡ Wishlist
            </Button>
          </div>
        </div>
      </div>

      {similarProducts.length > 0 && (
        <section className={styles.similarSection}>
          <div className="container">
            <h2 className={styles.sectionTitle}>Similar Products</h2>
            <div className={styles.grid}>
              {similarProducts.map((p, idx) => (
                <ProductCard key={p.productId || idx} product={p} />
              ))}
            </div>
          </div>
        </section>
      )}
    </div>
  );
}
