"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { fetchCoreApi, fetchRecApi } from "@/lib/api";
import { useAuth } from "@/context/AuthContext";
import { useCart } from "@/context/CartContext";
import { useToast } from "@/context/ToastContext";
import { Button } from "@/components/Button";
import { ProductCard } from "@/components/ProductCard";
import { ProductDetailSkeleton } from "@/components/Skeleton";
import styles from "./page.module.css";

export default function ProductDetailPage() {
  const params = useParams();
  const id = params.id as string;

  const [product, setProduct] = useState<any>(null);
  const [similarProducts, setSimilarProducts] = useState<any[]>([]);
  const [comments, setComments] = useState<any[]>([]);
  const [newComment, setNewComment] = useState("");
  const [isSubmittingComment, setIsSubmittingComment] = useState(false);
  const [loading, setLoading] = useState(true);
  const [addingToCart, setAddingToCart] = useState(false);
  const [selectedImage, setSelectedImage] = useState(0);

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
        // Fetch Comments
        try {
          const fetchedComments = await fetchCoreApi(`/products/${id}/comments`);
          if (Array.isArray(fetchedComments)) {
            setComments(fetchedComments);
          }
        } catch (err) {
          console.warn("Could not fetch comments", err);
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
  const { refreshCart } = useCart();
  const router = useRouter();
  const { toast } = useToast();

  const handleAddToCart = async () => {
    if (!user) {
      toast("Please login to add items to your cart.", "info");
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
      toast(`${product.productName} added to cart!`, "success");
      refreshCart();
    } catch (err) {
      console.error(err);
      toast("Failed to add to cart.", "error");
    } finally {
      setAddingToCart(false);
    }
  };

  const handleWishlist = async () => {
    if (!user) {
      toast("Please login to add items to your wishlist.", "info");
      router.push("/login");
      return;
    }

    try {
      await fetchCoreApi(`/users/${user.userId}/wishlist/${id}`, {
        method: "POST",
        requireAuth: true
      });
      toast(`${product.productName} added to wishlist!`, "success");
    } catch (err) {
      console.error(err);
      toast("Failed to add to wishlist.", "error");
    }
  };

  const handlePostComment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) {
      toast("Please login to post a comment.", "info");
      router.push("/login");
      return;
    }
    if (!newComment.trim()) return;

    setIsSubmittingComment(true);
    try {
      await fetchCoreApi(`/products/${id}/comments/users/${user.userId}`, {
        method: "POST",
        requireAuth: true,
        body: JSON.stringify({ content: newComment })
      });
      setNewComment("");
      
      // Reload comments
      const fetchedComments = await fetchCoreApi(`/products/${id}/comments`);
      if (Array.isArray(fetchedComments)) {
        setComments(fetchedComments);
      }
      toast("Comment posted successfully!", "success");
    } catch (err: any) {
      console.error(err);
      toast(err.message || "Failed to post comment.", "error");
    } finally {
      setIsSubmittingComment(false);
    }
  };

  if (loading) return <ProductDetailSkeleton />;
  if (!product) return <div className="container" style={{ padding: "4rem 0", textAlign: "center" }}>Product not found</div>;

  return (
    <div className={styles.container}>
      <div className={`container ${styles.productLayout}`}>
        <div className={styles.imageGallery}>
          {product.imageUrls && product.imageUrls.length > 0 ? (
            <>
              <img
                src={product.imageUrls[selectedImage] || product.imageUrls[0]}
                alt={product.productName}
                className={styles.mainImage}
              />
              {product.imageUrls.length > 1 && (
                <div className={styles.thumbnailStrip}>
                  {product.imageUrls.map((url: string, idx: number) => (
                    <button
                      key={idx}
                      className={`${styles.thumbnail} ${idx === selectedImage ? styles.thumbnailActive : ""}`}
                      onClick={() => setSelectedImage(idx)}
                    >
                      <img src={url} alt={`${product.productName} ${idx + 1}`} />
                    </button>
                  ))}
                </div>
              )}
            </>
          ) : (
            <div className={styles.placeholderImage}>No Image Available</div>
          )}
        </div>
        
        <div className={styles.productInfo}>
          <h1 className={styles.title}>{product.productName}</h1>
          {product.category && (
            <span className={styles.categoryBadge}>{product.category.categoryName}</span>
          )}
          <div className={styles.price}>₺{product.price?.toFixed(2)}</div>
          
          <div className={styles.description}>
            {product.description || "No description provided for this premium item. Crafted with excellence."}
          </div>

          {/* Product Attributes */}
          {product.attributes && Object.keys(product.attributes).length > 0 && (
            <div className={styles.attributes}>
              <h3 className={styles.attributesTitle}>Product Details</h3>
              <div className={styles.attributeGrid}>
                {Object.entries(product.attributes).map(([key, value]) => (
                  <div key={key} className={styles.attributeRow}>
                    <span className={styles.attributeKey}>{key}</span>
                    <span className={styles.attributeValue}>{String(value)}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
          
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

      <section className="container" style={{ marginTop: "4rem", marginBottom: "4rem" }}>
        <h2 className={styles.sectionTitle}>Customer Reviews</h2>
        
        <div style={{ background: "var(--bg-secondary)", borderRadius: "12px", padding: "2rem", border: "1px solid var(--border-color)", marginBottom: "2rem" }}>
          {user ? (
            <form onSubmit={handlePostComment} style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
              <textarea 
                placeholder="Share your thoughts about this product..."
                value={newComment}
                onChange={(e) => setNewComment(e.target.value)}
                style={{ width: "100%", padding: "1rem", borderRadius: "8px", border: "1px solid var(--border-color)", background: "var(--bg-primary)", color: "var(--text-primary)", minHeight: "100px", fontFamily: "inherit", resize: "vertical" }}
                required
              />
              <div style={{ alignSelf: "flex-end" }}>
                <Button type="submit" disabled={isSubmittingComment || !newComment.trim()}>
                  {isSubmittingComment ? "Posting..." : "Post Comment"}
                </Button>
              </div>
            </form>
          ) : (
            <div style={{ textAlign: "center", padding: "1rem" }}>
              <p style={{ color: "var(--text-secondary)", marginBottom: "1rem" }}>Please login to leave a review.</p>
              <Button variant="outline" onClick={() => router.push("/login")}>Login to Comment</Button>
            </div>
          )}
        </div>

        <div style={{ display: "flex", flexDirection: "column", gap: "1.5rem" }}>
          {comments.length > 0 ? comments.map(comment => (
            <div key={comment.commentId} style={{ padding: "1.5rem", borderBottom: "1px solid var(--border-color)" }}>
              <div style={{ display: "flex", alignItems: "center", gap: "1rem", marginBottom: "0.5rem" }}>
                <div style={{ width: "40px", height: "40px", borderRadius: "50%", background: "var(--accent-primary)", display: "flex", alignItems: "center", justifyContent: "center", color: "#fff", fontWeight: "bold" }}>
                  {comment.user?.firstName?.charAt(0) || "U"}
                </div>
                <div>
                  <h4 style={{ margin: 0, fontSize: "1rem" }}>{comment.user?.firstName} {comment.user?.lastName}</h4>
                  <span style={{ fontSize: "0.8rem", color: "var(--text-secondary)" }}>
                    {comment.createdAt ? new Date(comment.createdAt).toLocaleDateString() : "Just now"}
                  </span>
                </div>
              </div>
              <p style={{ margin: "1rem 0 0 0", lineHeight: 1.6, color: "var(--text-primary)" }}>{comment.content}</p>
            </div>
          )) : (
            <div style={{ textAlign: "center", padding: "2rem", color: "var(--text-secondary)" }}>
              No comments yet. Be the first to review this product!
            </div>
          )}
        </div>
      </section>

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
