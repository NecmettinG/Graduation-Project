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
  const [categories, setCategories] = useState<any[]>([]);
  const [allCategories, setAllCategories] = useState<any[]>([]);
  const [fadeState, setFadeState] = useState<'in' | 'out'>('in');

  useEffect(() => {
    async function loadRecommendations() {
      try {
        if (user) {
          // Fetch personalized recommendations from Java proxy (which calls Python service)
          const products = await fetchCoreApi(`/users/${user.userId}/recommendations?limit=4`, { requireAuth: true });

          if (products && products.length > 0) {
            setRecommendations(products);
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
        const data = await fetchCoreApi("/products?page=1&limit=4");
        // Assuming backend returns an array or an object with content array
        setRecommendations(Array.isArray(data) ? data.slice(0, 4) : (data.content || []).slice(0, 4));
      } catch (e) {
        console.error("Fallback products failed", e);
      }
    }

    let categoryInterval: NodeJS.Timeout;

    async function loadCategories() {
      try {
        const data = await fetchCoreApi('/categories?limit=50');
        const fetchedCats = Array.isArray(data) ? data : (data.content || []);

        if (fetchedCats.length > 0) {
          // Filter out duplicate categories by categoryId (or categoryName if id is missing)
          const uniqueCatsMap = new Map();
          fetchedCats.forEach(c => {
            const key = c.categoryId || c.categoryName;
            if (key) uniqueCatsMap.set(key, c);
          });
          const uniqueCats = Array.from(uniqueCatsMap.values());

          setAllCategories(uniqueCats);
          // Initial slice
          setCategories([...uniqueCats].sort(() => 0.5 - Math.random()).slice(0, 4));

          // Set up 15-second interval
          categoryInterval = setInterval(() => {
            setFadeState('out'); // Trigger fade out

            setTimeout(() => {
              // Swap categories while invisible
              setCategories([...uniqueCats].sort(() => 0.5 - Math.random()).slice(0, 4));
              setFadeState('in'); // Trigger fade in
            }, 300); // Wait 300ms for fade out to complete
          }, 15000);
        }
      } catch (err) {
        console.error("Failed to load categories", err);
      }
    }

    loadRecommendations();
    loadCategories();

    return () => {
      if (categoryInterval) clearInterval(categoryInterval);
    };
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
          <div
            className={styles.categoryGrid}
            style={{
              opacity: fadeState === 'in' ? 1 : 0,
              transition: 'opacity 0.3s ease-in-out'
            }}
          >
            {categories.length > 0 ? categories.map((cat, idx) => (
              <Link href={`/products?category=${cat.categoryId}`} key={cat.categoryId || idx} style={{ textDecoration: 'none' }}>
                <div className={`glass-panel ${styles.categoryCard}`} style={{ cursor: "pointer", transition: "transform 0.2s" }}
                  onMouseOver={(e) => e.currentTarget.style.transform = "scale(1.05)"}
                  onMouseOut={(e) => e.currentTarget.style.transform = "scale(1)"}>
                  <h3>{cat.categoryName}</h3>
                </div>
              </Link>
            )) : (
              // Fallback skeleton or default
              ['Electronics', 'Fashion', 'Home Decor', 'Sports'].map((cat, idx) => (
                <Link href="/products" key={idx} style={{ textDecoration: 'none' }}>
                  <div className={`glass-panel ${styles.categoryCard}`} style={{ cursor: "pointer", opacity: 0.5 }}>
                    <h3>{cat}</h3>
                  </div>
                </Link>
              ))
            )}
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
