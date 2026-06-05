"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { Input } from "@/components/Input";
import { Button } from "@/components/Button";
import styles from "./page.module.css";
import { fetchCoreApi } from "@/lib/api";

export default function CheckoutPage() {
  const { user, loading: authLoading } = useAuth();
  const router = useRouter();

  const [address, setAddress] = useState({
    street: "",
    city: "",
    state: "",
    zipCode: "",
    country: ""
  });
  
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  if (authLoading) return <div className="container" style={{ padding: "4rem 0", textAlign: "center" }}>Loading...</div>;
  if (!user) {
    router.push("/login");
    return null;
  }

  const handleCheckout = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      // 1. We might need to add the address to the user first if it's new
      // Assuming endpoint: POST /users/{userId}/addresses
      
      // 2. Place order
      // Assuming endpoint: POST /users/{userId}/orders
      await fetchCoreApi(`/users/${user.userId}/orders`, {
        method: "POST",
        requireAuth: true,
        body: JSON.stringify({
          addressId: "new-or-existing-address-id" // Mocked, ideally comes from address response
        })
      });

      setSuccess(true);
      setTimeout(() => {
        router.push("/orders");
      }, 3000);
      
    } catch (err) {
      alert("Failed to place order. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  if (success) {
    return (
      <div className={styles.container}>
        <div className={`glass-panel ${styles.checkoutCard}`} style={{ textAlign: "center" }}>
          <h1 className={styles.title} style={{ color: "#10B981" }}>Order Placed!</h1>
          <p className={styles.subtitle}>Thank you for shopping with Smarty Commerce.</p>
          <p style={{ color: "var(--text-secondary)" }}>Redirecting to your orders...</p>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={`glass-panel ${styles.checkoutCard}`}>
        <h1 className={styles.title}>Secure Checkout</h1>
        <p className={styles.subtitle}>Please enter your shipping details</p>

        <form onSubmit={handleCheckout} className={styles.form}>
          <Input 
            label="Street Address" 
            value={address.street}
            onChange={e => setAddress({...address, street: e.target.value})}
            required
          />
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "1rem" }}>
            <Input 
              label="City" 
              value={address.city}
              onChange={e => setAddress({...address, city: e.target.value})}
              required
            />
            <Input 
              label="State / Province" 
              value={address.state}
              onChange={e => setAddress({...address, state: e.target.value})}
              required
            />
          </div>
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "1rem" }}>
            <Input 
              label="Zip Code" 
              value={address.zipCode}
              onChange={e => setAddress({...address, zipCode: e.target.value})}
              required
            />
            <Input 
              label="Country" 
              value={address.country}
              onChange={e => setAddress({...address, country: e.target.value})}
              required
            />
          </div>

          <div style={{ marginTop: "2rem" }}>
            <Button type="submit" fullWidth disabled={loading}>
              {loading ? "Processing..." : "Place Order"}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
