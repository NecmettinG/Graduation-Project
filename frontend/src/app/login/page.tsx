"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Input } from "@/components/Input";
import { Button } from "@/components/Button";
import { useAuth } from "@/context/AuthContext";
import { fetchCoreApi } from "@/lib/api";
import styles from "./page.module.css";

export default function LoginPage() {
  const router = useRouter();
  const { login } = useAuth();
  
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      // The backend Spring Security JWT filter normally returns the token in the header 
      // but let's assume it returns { userId, token } or we can fetch it.
      // Based on standard Spring JWT, it might be in headers: "Authorization"
      
      const response = await fetch("http://localhost:8080/smarty-commerce/users/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });

      if (!response.ok) {
        throw new Error("Invalid credentials");
      }

      // Spring Security typically sends back the token in the Authorization header
      // and maybe userId in another header like "userId". 
      // Let's extract them.
      const token = response.headers.get("Authorization") || response.headers.get("token");
      const userId = response.headers.get("userId");

      if (token && userId) {
        // Strip "Bearer " if it's there
        const cleanToken = token.replace("Bearer ", "");
        await login(cleanToken, userId);
        router.push("/");
      } else {
        // Fallback if the body has it
        const data = await response.json();
        if (data.token && data.userId) {
          await login(data.token, data.userId);
          router.push("/");
        } else {
          throw new Error("Invalid response from server (missing token/userId)");
        }
      }
    } catch (err: any) {
      setError(err.message || "Failed to login. Please check your credentials.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <div className={`glass-panel ${styles.formCard}`}>
        <h1 className={styles.title}>Welcome Back</h1>
        <p className={styles.subtitle}>Sign in to your account to continue</p>

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
          <Input 
            label="Password" 
            type="password" 
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required 
            placeholder="••••••••"
          />
          
          <div className={styles.forgotPassword}>
            <Link href="/forgot-password" className={styles.link}>Forgot your password?</Link>
          </div>

          <Button type="submit" fullWidth disabled={loading}>
            {loading ? "Signing in..." : "Sign In"}
          </Button>
        </form>

        <p className={styles.registerPrompt}>
          Don't have an account? <Link href="/register" className={styles.link}>Sign up</Link>
        </p>
      </div>
    </div>
  );
}
