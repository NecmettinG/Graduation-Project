"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { useCart } from "@/context/CartContext";
import { useToast } from "@/context/ToastContext";
import { Input } from "@/components/Input";
import { Button } from "@/components/Button";
import styles from "./page.module.css";
import { fetchCoreApi } from "@/lib/api";

export default function CheckoutPage() {
  const { user, loading: authLoading } = useAuth();
  const { refreshCart } = useCart();
  const { toast } = useToast();
  const router = useRouter();

  const [addresses, setAddresses] = useState<any[]>([]);
  const [selectedAddressId, setSelectedAddressId] = useState<string>("new");

  const [address, setAddress] = useState({
    street: "",
    city: "",
    state: "",
    zipCode: "",
    country: "",
    type: "shipping"
  });
  
  const [paymentMethod, setPaymentMethod] = useState("CREDIT_CARD");
  
  const [payment, setPayment] = useState({
    cardNumber: "",
    expiryDate: "",
    cvv: ""
  });
  
  const handleCardNumberChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value.replace(/\D/g, "");
    const formatted = value.match(/.{1,4}/g)?.join(" ") || value;
    setPayment({ ...payment, cardNumber: formatted.slice(0, 19) });
  };

  const handleExpiryChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    let value = e.target.value.replace(/\D/g, "");
    if (value.length >= 2) {
      value = value.slice(0, 2) + "/" + value.slice(2, 4);
    }
    setPayment({ ...payment, expiryDate: value.slice(0, 5) });
  };
  
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    if (user) {
      fetchCoreApi(`/users/${user.userId}/addresses`, { requireAuth: true })
        .then(data => {
          if (Array.isArray(data) && data.length > 0) {
            setAddresses(data);
            setSelectedAddressId(data[0].addressId);
          }
        })
        .catch(err => console.warn("Could not fetch addresses", err));
    }
  }, [user]);

  if (authLoading) return <div className="container" style={{ padding: "4rem 0", textAlign: "center" }}>Loading...</div>;
  if (!user) {
    router.push("/login");
    return null;
  }

  const handleCheckout = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      let finalAddressId = selectedAddressId;

      // 1. Create address if new
      if (selectedAddressId === "new") {
        const newAddress = await fetchCoreApi(`/users/${user.userId}/addresses`, {
          method: "POST",
          requireAuth: true,
          body: JSON.stringify({
            city: address.city,
            country: address.country,
            streetName: address.street,
            postalCode: address.zipCode,
            type: "shipping"
          })
        });
        finalAddressId = newAddress.addressId;
      }
      
      // 2. Place order
      const requestBody: any = {
        addressId: finalAddressId,
        paymentMethod: paymentMethod
      };
      
      if (paymentMethod === "CREDIT_CARD") {
        requestBody.paymentToken = "fake-tok-" + payment.cardNumber.replace(/\D/g, "").slice(-4);
      }

      await fetchCoreApi(`/users/${user.userId}/orders`, {
        method: "POST",
        requireAuth: true,
        body: JSON.stringify(requestBody)
      });

      // 3. Clear cart
      await fetchCoreApi(`/users/${user.userId}/cart`, {
        method: "DELETE",
        requireAuth: true
      });

      setSuccess(true);
      refreshCart();
      setTimeout(() => {
        router.push("/orders");
      }, 3000);
      
    } catch (err) {
      console.error(err);
      toast("Failed to place order. Please try again.", "error");
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
        <p className={styles.subtitle}>Please provide your shipping details</p>

        <form onSubmit={handleCheckout} className={styles.form}>
          {addresses.length > 0 && (
            <div style={{ marginBottom: "1.5rem" }}>
              <label style={{ display: "block", marginBottom: "0.5rem", color: "var(--text-secondary)", fontSize: "0.875rem" }}>
                Select Delivery Address
              </label>
              <select 
                value={selectedAddressId}
                onChange={(e) => setSelectedAddressId(e.target.value)}
                style={{ width: "100%", padding: "0.75rem", borderRadius: "8px", border: "1px solid var(--border-color)", background: "rgba(255,255,255,0.05)", color: "var(--text-primary)" }}
              >
                {addresses.map(addr => (
                  <option key={addr.addressId} value={addr.addressId} style={{ color: "#000" }}>
                    {addr.streetName}, {addr.city}, {addr.country}
                  </option>
                ))}
                <option value="new" style={{ color: "#000" }}>+ Add New Address</option>
              </select>
            </div>
          )}

          {selectedAddressId === "new" && (
            <>
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
                  label="Zip Code" 
                  value={address.zipCode}
                  onChange={e => setAddress({...address, zipCode: e.target.value})}
                  required
                />
              </div>
              <Input 
                label="Country" 
                value={address.country}
                onChange={e => setAddress({...address, country: e.target.value})}
                required
              />
            </>
          )}

          <div style={{ marginTop: "2rem" }}>
            <h3 style={{ marginBottom: "1rem", color: "var(--text-primary)", fontSize: "1.125rem", fontFamily: "var(--font-inter)" }}>Payment Information</h3>
            
            <div style={{ marginBottom: "1.5rem" }}>
              <label style={{ display: "block", marginBottom: "0.5rem", color: "var(--text-secondary)", fontSize: "0.875rem" }}>
                Payment Method
              </label>
              <select 
                value={paymentMethod}
                onChange={(e) => setPaymentMethod(e.target.value)}
                style={{ width: "100%", padding: "0.75rem", borderRadius: "8px", border: "1px solid var(--border-color)", background: "rgba(255,255,255,0.05)", color: "var(--text-primary)" }}
              >
                <option value="CREDIT_CARD" style={{ color: "#000" }}>Credit Card</option>
                <option value="UPON_DELIVERY" style={{ color: "#000" }}>Pay Upon Delivery</option>
                <option value="PAYPAL" style={{ color: "#000" }}>PayPal</option>
              </select>
            </div>

            {paymentMethod === "CREDIT_CARD" && (
              <div style={{ background: "rgba(255, 255, 255, 0.02)", padding: "1.5rem", borderRadius: "8px", border: "1px solid var(--border-color)", boxSizing: "border-box" }}>
                <Input 
                  label="Card Number" 
                  placeholder="0000 0000 0000 0000"
                  value={payment.cardNumber}
                  onChange={handleCardNumberChange}
                  required
                  style={{ boxSizing: "border-box", width: "100%" }}
                />
                <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "1rem", marginTop: "1rem", boxSizing: "border-box" }}>
                  <div style={{ minWidth: 0 }}>
                    <Input 
                      label="Expiry Date" 
                      placeholder="MM/YY"
                      value={payment.expiryDate}
                      onChange={handleExpiryChange}
                      required
                      style={{ boxSizing: "border-box", width: "100%" }}
                    />
                  </div>
                  <div style={{ minWidth: 0 }}>
                    <Input 
                      label="CVV" 
                      type="password"
                      placeholder="123"
                      value={payment.cvv}
                      onChange={e => setPayment({...payment, cvv: e.target.value.replace(/\D/g, "").slice(0, 4)})}
                      required
                      style={{ boxSizing: "border-box", width: "100%" }}
                    />
                  </div>
                </div>
              </div>
            )}
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
