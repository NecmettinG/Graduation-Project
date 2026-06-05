"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { fetchCoreApi } from "@/lib/api";
import { Button } from "@/components/Button";
import styles from "./page.module.css";

export default function CartPage() {
  const { user, loading: authLoading } = useAuth();
  const router = useRouter();
  
  const [cartItems, setCartItems] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (authLoading) return;
    
    if (!user) {
      router.push("/login");
      return;
    }

    async function fetchCart() {
      try {
        // Assume API endpoint is /users/{userId}/cart
        const data = await fetchCoreApi(`/users/${user?.userId}/cart`, { requireAuth: true });
        
        // Handle varying responses (array of items or object with items array)
        if (data && Array.isArray(data)) {
          setCartItems(data);
        } else if (data && data.cartItems) {
          setCartItems(data.cartItems);
        }
      } catch (err) {
        console.warn("Failed to fetch cart, using empty cart", err);
      } finally {
        setLoading(false);
      }
    }

    fetchCart();
  }, [user, authLoading, router]);

  const handleRemove = async (itemId: string) => {
    try {
      await fetchCoreApi(`/users/${user?.userId}/cart/items/${itemId}`, { 
        method: 'DELETE',
        requireAuth: true
      });
      setCartItems(prev => prev.filter(item => (item.cartItemId || item.id) !== itemId));
    } catch (e) {
      alert("Failed to remove item");
    }
  };

  const handleUpdateQuantity = async (itemId: string, currentQty: number, change: number, product: any) => {
    const newQty = currentQty + change;
    if (newQty < 1) return; // Can't have 0, use remove instead
    
    try {
      await fetchCoreApi(`/users/${user?.userId}/cart/items/${itemId}`, {
        method: "PUT",
        requireAuth: true,
        body: JSON.stringify({
          productId: product.productId,
          quantity: newQty
        })
      });
      setCartItems(prev => prev.map(item => (item.cartItemId || item.id) === itemId ? { ...item, quantity: newQty } : item));
    } catch (err) {
      alert("Failed to update quantity");
    }
  };

  const calculateTotal = () => {
    return cartItems.reduce((total, item) => {
      // Assuming item has product.price and quantity
      const price = item.product?.price || item.price || 0;
      const qty = item.quantity || 1;
      return total + (price * qty);
    }, 0);
  };

  if (authLoading || loading) return <div className="container" style={{ padding: "4rem 0", textAlign: "center" }}>Loading your cart...</div>;

  return (
    <div className={styles.container}>
      <div className="container">
        <h1 className={styles.title}>Shopping Cart</h1>

        {cartItems.length === 0 ? (
          <div className={`glass-panel ${styles.emptyState}`}>
            <h2>Your cart is empty</h2>
            <p>Looks like you haven't added any premium items to your cart yet.</p>
            <Link href="/products" className="btn btn-primary" style={{ marginTop: "1.5rem" }}>
              Continue Shopping
            </Link>
          </div>
        ) : (
          <div className={styles.cartLayout}>
            <div className={styles.itemList}>
              {cartItems.map((item, idx) => {
                const product = item.product || item;
                const price = product.price || 0;
                const qty = item.quantity || 1;
                return (
                  <div key={item.cartItemId || item.id || idx} className={`glass-panel ${styles.cartItem}`}>
                    <div className={styles.itemImagePlaceholder}>
                      {product.imageUrls && product.imageUrls.length > 0 ? <img src={product.imageUrls[0]} alt={product.productName} /> : "Item"}
                    </div>
                    <div className={styles.itemDetails}>
                      <h3>{product.productName}</h3>
                      <p className={styles.price}>${price.toFixed(2)}</p>
                      <div className={styles.qtyControls} style={{ display: "flex", alignItems: "center", gap: "0.5rem", marginTop: "0.5rem" }}>
                        <button 
                          onClick={() => handleUpdateQuantity(item.cartItemId || item.id, qty, -1, product)}
                          style={{ background: "var(--border-color)", border: "none", width: "24px", height: "24px", borderRadius: "4px", cursor: "pointer", fontWeight: "bold" }}
                        >-</button>
                        <span>{qty}</span>
                        <button 
                          onClick={() => handleUpdateQuantity(item.cartItemId || item.id, qty, 1, product)}
                          style={{ background: "var(--border-color)", border: "none", width: "24px", height: "24px", borderRadius: "4px", cursor: "pointer", fontWeight: "bold" }}
                        >+</button>
                      </div>
                    </div>
                    <button onClick={() => handleRemove(item.cartItemId || item.id)} className={styles.removeBtn}>
                      Remove
                    </button>
                  </div>
                );
              })}
            </div>

            <div className={`glass-panel ${styles.orderSummary}`}>
              <h2>Order Summary</h2>
              <div className={styles.summaryRow}>
                <span>Subtotal</span>
                <span>${calculateTotal().toFixed(2)}</span>
              </div>
              <div className={styles.summaryRow}>
                <span>Shipping</span>
                <span>Calculated at checkout</span>
              </div>
              <div className={styles.divider}></div>
              <div className={styles.summaryRow} style={{ fontWeight: 700, fontSize: "1.25rem", color: "var(--accent-primary)" }}>
                <span>Total</span>
                <span>${calculateTotal().toFixed(2)}</span>
              </div>
              
              <Link href="/checkout" className={`btn btn-primary ${styles.checkoutBtn}`}>
                Proceed to Checkout
              </Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
