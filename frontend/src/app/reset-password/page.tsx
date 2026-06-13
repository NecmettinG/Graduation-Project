"use client";

import { useState, Suspense } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import Link from "next/link";
import { Input } from "@/components/Input";
import { Button } from "@/components/Button";
import { fetchCoreApi } from "@/lib/api";
import styles from "../login/page.module.css";

function ResetPasswordForm() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const token = searchParams.get("token") || "";

  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);

  if (!token) {
    return (
      <div className={styles.container}>
        <div className={`glass-panel ${styles.formCard}`} style={{ textAlign: "center" }}>
          <h1 className={styles.title} style={{ color: "#EF4444" }}>Invalid Link</h1>
          <p className={styles.subtitle}>This password reset link is invalid or has expired.</p>
          <Link href="/forgot-password" className={styles.link}>Request a new reset link</Link>
        </div>
      </div>
    );
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");

    if (password !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    if (password.length < 8) {
      setError("Password must be at least 8 characters.");
      return;
    }

    setLoading(true);

    try {
      const result = await fetchCoreApi("/users/password-reset", {
        method: "POST",
        body: JSON.stringify({ token, password }),
      });

      if (result?.operationResult === "SUCCESS") {
        setSuccess(true);
        setTimeout(() => {
          router.push("/login");
        }, 3000);
      } else {
        throw new Error("Password reset failed. The link may have expired.");
      }
    } catch (err: any) {
      setError(err.message || "Failed to reset password. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  if (success) {
    return (
      <div className={styles.container}>
        <div className={`glass-panel ${styles.formCard}`} style={{ textAlign: "center" }}>
          <h1 className={styles.title} style={{ color: "#10B981" }}>Password Reset!</h1>
          <p className={styles.subtitle}>Your password has been updated successfully.</p>
          <p style={{ color: "var(--text-secondary)", fontSize: "0.875rem" }}>Redirecting to login...</p>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={`glass-panel ${styles.formCard}`}>
        <h1 className={styles.title}>Set New Password</h1>
        <p className={styles.subtitle}>Enter your new password below.</p>

        {error && <div className={styles.error}>{error}</div>}

        <form onSubmit={handleSubmit} className={styles.form}>
          <Input
            label="New Password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            placeholder="••••••••"
          />
          <Input
            label="Confirm New Password"
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required
            placeholder="••••••••"
          />

          <div style={{ marginTop: "0.5rem" }}>
            <Button type="submit" fullWidth disabled={loading}>
              {loading ? "Resetting..." : "Reset Password"}
            </Button>
          </div>
        </form>

        <p className={styles.registerPrompt}>
          <Link href="/login" className={styles.link}>← Back to Login</Link>
        </p>
      </div>
    </div>
  );
}

export default function ResetPasswordPage() {
  return (
    <Suspense fallback={
      <div className={styles.container}>
        <div className={`glass-panel ${styles.formCard}`} style={{ textAlign: "center" }}>
          <p style={{ color: "var(--text-secondary)" }}>Loading...</p>
        </div>
      </div>
    }>
      <ResetPasswordForm />
    </Suspense>
  );
}
