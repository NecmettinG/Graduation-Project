"use client";

import { useState } from "react";
import Link from "next/link";
import { Input } from "@/components/Input";
import { Button } from "@/components/Button";
import { fetchCoreApi } from "@/lib/api";
import styles from "../login/page.module.css";

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [sent, setSent] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const result = await fetchCoreApi("/users/password-reset-request", {
        method: "POST",
        body: JSON.stringify({ email }),
      });

      if (result?.operationResult === "SUCCESS") {
        setSent(true);
      } else {
        throw new Error("Could not send reset email. Please check your email address.");
      }
    } catch (err: any) {
      setError(err.message || "Failed to send reset email. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  if (sent) {
    return (
      <div className={styles.container}>
        <div className={`glass-panel ${styles.formCard}`} style={{ textAlign: "center" }}>
          <h1 className={styles.title} style={{ color: "#10B981" }}>Email Sent!</h1>
          <p className={styles.subtitle}>
            If an account exists with <strong>{email}</strong>, a password reset link has been sent.
          </p>
          <div style={{ background: "#EFF6FF", border: "1px solid #BFDBFE", borderRadius: "8px", padding: "1rem", marginBottom: "1.5rem" }}>
            <p style={{ color: "#1E40AF", fontSize: "0.9rem", fontWeight: 500, margin: 0 }}>
              📧 Check your inbox and click the reset link to set a new password.
            </p>
          </div>
          <Link href="/login" className={styles.link}>← Back to Login</Link>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={`glass-panel ${styles.formCard}`}>
        <h1 className={styles.title}>Forgot Password</h1>
        <p className={styles.subtitle}>Enter your email address and we'll send you a password reset link.</p>

        {error && <div className={styles.error}>{error}</div>}

        <form onSubmit={handleSubmit} className={styles.form}>
          <Input
            label="Email Address"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            placeholder="you@example.com"
          />

          <div style={{ marginTop: "0.5rem" }}>
            <Button type="submit" fullWidth disabled={loading}>
              {loading ? "Sending..." : "Send Reset Link"}
            </Button>
          </div>
        </form>

        <p className={styles.registerPrompt}>
          Remember your password? <Link href="/login" className={styles.link}>Sign in</Link>
        </p>
      </div>
    </div>
  );
}
