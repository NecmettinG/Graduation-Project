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

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    
    if (password !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    setLoading(true);

    try {
      const payload = {
        firstName,
        lastName,
        email,
        password,
        roles: ["ROLE_USER"],
        addresses: [] // Empty addresses for initial registration
      };

      const response = await fetch("http://localhost:8080/smarty-commerce/users", {
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
      }, 3000);
      
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
          <h1 className={styles.title} style={{ color: "#10B981" }}>Success!</h1>
          <p className={styles.subtitle}>Your account has been created successfully.</p>
          <p style={{ color: "var(--text-secondary)" }}>Redirecting to login...</p>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={`glass-panel ${styles.formCard}`}>
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
            style={{ marginBottom: "1.5rem" }}
          />

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
