"use client";

import { useEffect, useState, Suspense } from "react";
import { useSearchParams } from "next/navigation";
import Link from "next/link";
import { fetchCoreApi } from "@/lib/api";
import styles from "./page.module.css";

function VerifyEmailContent() {
  const searchParams = useSearchParams();
  const token = searchParams.get("token");
  const [status, setStatus] = useState<"loading" | "success" | "error">("loading");

  useEffect(() => {
    if (!token) {
      setStatus("error");
      return;
    }

    async function verify() {
      try {
        const res = await fetchCoreApi(`/users/email-verification?token=${token}`);
        if (res?.operationResult === "SUCCESS") {
          setStatus("success");
        } else {
          setStatus("error");
        }
      } catch {
        setStatus("error");
      }
    }

    verify();
  }, [token]);

  return (
    <div className={styles.container}>
      <div className={`glass-panel ${styles.card}`}>
        {status === "loading" && (
          <>
            <div className={styles.spinner} />
            <h1 className={styles.title}>Verifying your email...</h1>
            <p className={styles.subtitle}>Please wait while we confirm your email address.</p>
          </>
        )}

        {status === "success" && (
          <>
            <div className={styles.iconSuccess}>✓</div>
            <h1 className={styles.title}>Email Verified!</h1>
            <p className={styles.subtitle}>
              Your email has been successfully verified. You can now log in to your account.
            </p>
            <Link href="/login" className="btn btn-primary" style={{ marginTop: "1.5rem" }}>
              Go to Login
            </Link>
          </>
        )}

        {status === "error" && (
          <>
            <div className={styles.iconError}>✕</div>
            <h1 className={styles.title}>Verification Failed</h1>
            <p className={styles.subtitle}>
              The verification link is invalid or has expired. Please try registering again.
            </p>
            <Link href="/register" className="btn btn-primary" style={{ marginTop: "1.5rem" }}>
              Register Again
            </Link>
          </>
        )}
      </div>
    </div>
  );
}

export default function VerifyEmailPage() {
  return (
    <Suspense fallback={
      <div className={styles.container}>
        <div className={`glass-panel ${styles.card}`}>
          <div className={styles.spinner} />
          <h1 className={styles.title}>Verifying your email...</h1>
        </div>
      </div>
    }>
      <VerifyEmailContent />
    </Suspense>
  );
}
