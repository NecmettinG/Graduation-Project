"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useAuth } from "@/context/AuthContext";
import { fetchCoreApi, fetchRecApi } from "@/lib/api";
import { ProductCard } from "@/components/ProductCard";
import { Button } from "@/components/Button";
import styles from "./page.module.css";

export default function Home() {
  const { user } = useAuth();
  const [recommendations, setRecommendations] = useState<any[]>([]);
  const [loadingRecs, setLoadingRecs] = useState(true);

  useEffect(() => {
    async function loadRecommendations() {
      try {
        if (user) {
          // Fetch personalized recommendations from Python service
          const recs = await fetchRecApi(`/recommendations/user/${user.userId}?top_n=4`);
          
          // The Python service returns a list of { productId, score }. 
          // We need to fetch the actual product details from the Java backend.
          if (recs && recs.length > 0) {
            const productPromises = recs.map((r: any) => 
              fetchCoreApi(`/products/${r.productId}`).catch(() => null)
            );
            const products = await Promise.all(productPromises);
            setRecommendations(products.filter(p => p !== null));
          } else {
            // Fallback if no personalized recs
            await fetchFallbackProducts();
          }
        } else {
          // If not logged in, just fetch some general products
          await fetchFallbackProducts();
        }
      } catch (err) {
        console.error("Failed to load recommendations", err);
        await fetchFallbackProducts();
      } finally {
        setLoadingRecs(false);
      }
    }

    async function fetchFallbackProducts() {
      try {
        // Just fetch the first page of products from Java backend as fallback
        const data = await fetchCoreApi("/products?page=0&limit=4");
        // Assuming backend returns an array or an object with content array
        setRecommendations(Array.isArray(data) ? data.slice(0, 4) : (data.content || []).slice(0, 4));
      } catch (e) {
        console.error("Fallback products failed", e);
      }
    }

    loadRecommendations();
  }, [user]);

  return (
    <div className={styles.container}>
      {/* Hero Section */}
      <section className={styles.hero}>
        <div className="container">
          <div className={styles.heroContent}>
            <h1 className="fade-in">Elevate Your Shopping Experience</h1>
            <p className="fade-in" style={{ animationDelay: "0.2s" }}>
              Discover a curated collection of premium products, tailored specifically to your taste using our advanced AI recommendation engine.
            </p>
            <div className={`${styles.heroButtons} fade-in`} style={{ animationDelay: "0.4s" }}>
              <Link href="/products" className="btn btn-secondary">Shop Now</Link>
              {!user && <Link href="/register" className="btn btn-outline" style={{ color: "white", borderColor: "white" }}>Create Account</Link>}
            </div>
          </div>
        </div>
      </section>

      {/* Featured Categories */}
      <section className={styles.categories}>
        <div className="container">
          <div className={styles.sectionHeader}>
            <h2>Shop by Category</h2>
            <Link href="/categories" className={styles.viewAll}>View All</Link>
          </div>
          <div className={styles.categoryGrid}>
            {['Electronics', 'Fashion', 'Home Decor', 'Sports'].map((cat, idx) => (
              <div key={idx} className={`glass-panel ${styles.categoryCard}`}>
                <h3>{cat}</h3>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Recommendations Section */}
      <section className={styles.recommendations}>
        <div className="container">
          <div className={styles.sectionHeader}>
            <h2>{user ? "Recommended For You" : "Trending Products"}</h2>
          </div>
          
          {loadingRecs ? (
            <div className={styles.loadingState}>Analyzing your preferences...</div>
          ) : recommendations.length > 0 ? (
            <div className={styles.productGrid}>
              {recommendations.map((product, idx) => (
                <ProductCard key={product.productId || idx} product={product} />
              ))}
            </div>
          ) : (
            <div className={styles.emptyState}>Check back later for personalized recommendations.</div>
          )}
        </div>
      </section>
    </div>
  );
}
