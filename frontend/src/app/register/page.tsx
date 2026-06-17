"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Input } from "@/components/Input";
import { Button } from "@/components/Button";
import styles from "../login/page.module.css";

export default function RegisterPage() {
  const router = useRouter();
  
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  // Initial Address fields
  const [streetName, setStreetName] = useState("");
  const [city, setCity] = useState("");
  const [country, setCountry] = useState("");
  const [postalCode, setPostalCode] = useState("");
  const [addressType, setAddressType] = useState("Shipping");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    
    if (password !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    setLoading(true);

    try {
      const payload: any = {
        firstName,
        lastName,
        email,
        password,
        roles: ["ROLE_USER"],
        addresses: []
      };

      // Add address if at least street, city and country are provided
      if (streetName && city && country) {
        payload.addresses.push({
          streetName,
          city,
          country,
          postalCode,
          type: addressType
        });
      }

      const response = await fetch("/api/core/users", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        let errorMsg = "Registration failed";
        try {
          const errData = await response.json();
          errorMsg = errData.message || errorMsg;
        } catch (e) {}
        throw new Error(errorMsg);
      }

      setSuccess(true);
      setTimeout(() => {
        router.push("/login");
      }, 5000);
      
    } catch (err: any) {
      setError(err.message || "Failed to create account. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  if (success) {
    return (
      <div className={styles.container}>
        <div className={`glass-panel ${styles.formCard}`} style={{ textAlign: "center" }}>
          <h1 className={styles.title} style={{ color: "#10B981" }}>Account Created!</h1>
          <p className={styles.subtitle}>A verification email has been sent to your inbox.</p>
          <div style={{ background: "#EFF6FF", border: "1px solid #BFDBFE", borderRadius: "8px", padding: "1rem", marginBottom: "1.5rem" }}>
            <p style={{ color: "#1E40AF", fontSize: "0.9rem", fontWeight: 500, margin: 0 }}>
              📧 Please check your email and click the verification link to activate your account.
            </p>
          </div>
          <p style={{ color: "var(--text-secondary)", fontSize: "0.875rem" }}>Redirecting to login in 5 seconds...</p>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={`glass-panel ${styles.formCard}`} style={{ maxWidth: "600px", width: "100%" }}>
        <h1 className={styles.title}>Create Account</h1>
        <p className={styles.subtitle}>Join Smarty Commerce today</p>

        {error && <div className={styles.error}>{error}</div>}

        <form onSubmit={handleSubmit} className={styles.form}>
          <div style={{ display: "flex", gap: "1rem" }}>
            <Input 
              label="First Name" 
              type="text" 
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
              required 
              style={{ flex: 1 }}
            />
            <Input 
              label="Last Name" 
              type="text" 
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              required 
              style={{ flex: 1 }}
            />
          </div>
          
          <Input 
            label="Email Address" 
            type="email" 
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required 
          />
          <Input 
            label="Password" 
            type="password" 
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required 
          />
          <Input 
            label="Confirm Password" 
            type="password" 
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required 
            style={{ marginBottom: "2rem" }}
          />

          <hr style={{ border: "0", borderTop: "1px solid var(--border-color)", marginBottom: "2rem" }} />
          <h2 style={{ fontSize: "1.2rem", marginBottom: "1rem", fontFamily: "var(--font-inter)" }}>Initial Address (Optional)</h2>

          <Input 
            label="Street Name & Number" 
            type="text" 
            value={streetName}
            onChange={(e) => setStreetName(e.target.value)}
          />
          
          <div style={{ display: "flex", gap: "1rem" }}>
            <Input 
              label="City" 
              type="text" 
              value={city}
              onChange={(e) => setCity(e.target.value)}
              style={{ flex: 1 }}
            />
            <Input 
              label="Postal Code" 
              type="text" 
              value={postalCode}
              onChange={(e) => setPostalCode(e.target.value)}
              style={{ flex: 1 }}
            />
          </div>

          <div style={{ display: "flex", gap: "1rem", marginBottom: "1.5rem" }}>
            <Input 
              label="Country" 
              type="text" 
              value={country}
              onChange={(e) => setCountry(e.target.value)}
              style={{ flex: 2 }}
            />
            <div style={{ flex: 1, display: "flex", flexDirection: "column", gap: "0.5rem" }}>
              <label style={{ fontSize: "0.875rem", fontWeight: 500 }}>Type</label>
              <select 
                value={addressType} 
                onChange={(e) => setAddressType(e.target.value)}
                style={{ padding: "0.75rem", borderRadius: "0.5rem", border: "1px solid var(--border-color)", background: "var(--bg-primary)", color: "var(--text-primary)" }}
              >
                <option value="Shipping" style={{ color: "#000" }}>Shipping</option>
                <option value="Billing" style={{ color: "#000" }}>Billing</option>
              </select>
            </div>
          </div>

          <Button type="submit" fullWidth disabled={loading}>
            {loading ? "Creating account..." : "Sign Up"}
          </Button>
        </form>

        <p className={styles.registerPrompt}>
          Already have an account? <Link href="/login" className={styles.link}>Sign in</Link>
        </p>
      </div>
    </div>
  );
}
