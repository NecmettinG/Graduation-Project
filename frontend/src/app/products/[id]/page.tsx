"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { fetchCoreApi, fetchRecApi } from "@/lib/api";
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
          const recs = await fetchRecApi(`/recommendations/${id}?top_n=4`);
          if (recs && recs.length > 0) {
            const productPromises = recs.map((r: any) => 
              fetchCoreApi(`/products/${r.productId}`).catch(() => null)
            );
            const resolvedRecs = await Promise.all(productPromises);
            setSimilarProducts(resolvedRecs.filter(p => p !== null));
          }
        } catch (recErr) {
          console.warn("Could not fetch similar items", recErr);
        }
      } catch (err) {
        console.error("Failed to load product", err);
      } finally {
        setLoading(false);
      }
    }
    
    loadData();
  }, [id]);

  const handleAddToCart = async () => {
    setAddingToCart(true);
    try {
      // Typically we would post to /cart here and include JWT token
      // Mocking realistic interaction delay
      await new Promise(res => setTimeout(res, 500));
      alert("Added to cart successfully!");
    } catch (err) {
      alert("Failed to add to cart");
    } finally {
      setAddingToCart(false);
    }
  };

  if (loading) return <div className="container" style={{ padding: "4rem 0", textAlign: "center" }}>Loading...</div>;
  if (!product) return <div className="container" style={{ padding: "4rem 0", textAlign: "center" }}>Product not found</div>;

  return (
    <div className={styles.container}>
      <div className={`container ${styles.productLayout}`}>
        <div className={styles.imageGallery}>
          {product.imageUrl ? (
            <img src={product.imageUrl} alt={product.name} className={styles.mainImage} />
          ) : (
            <div className={styles.placeholderImage}>No Image Available</div>
          )}
        </div>
        
        <div className={styles.productInfo}>
          <h1 className={styles.title}>{product.name}</h1>
          <div className={styles.price}>${product.price?.toFixed(2)}</div>
          
          <div className={styles.description}>
            {product.description || "No description provided for this premium item. Crafted with excellence."}
          </div>
          
          <div className={styles.actions}>
            <Button onClick={handleAddToCart} disabled={addingToCart} className={styles.addBtn}>
              {addingToCart ? "Adding..." : "Add to Cart"}
            </Button>
            <Button variant="outline" className={styles.wishlistBtn}>
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
