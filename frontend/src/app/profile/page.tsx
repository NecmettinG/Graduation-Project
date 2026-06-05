"use client";

import { useAuth } from "@/context/AuthContext";
import { useRouter } from "next/navigation";
import { Input } from "@/components/Input";
import { Button } from "@/components/Button";

export default function ProfilePage() {
  const { user, loading } = useAuth();
  const router = useRouter();

  if (loading) return <div className="container" style={{ padding: "4rem 0", textAlign: "center" }}>Loading...</div>;
  if (!user) {
    router.push("/login");
    return null;
  }

  return (
    <div className="container" style={{ padding: "4rem 1.5rem" }}>
      <h1 style={{ fontSize: "2.5rem", color: "var(--accent-primary)", marginBottom: "2rem", fontFamily: "var(--font-outfit)" }}>
        My Profile
      </h1>
      
      <div className="glass-panel" style={{ padding: "2rem", maxWidth: "600px" }}>
        <h2 style={{ marginBottom: "1.5rem", fontFamily: "var(--font-inter)" }}>Personal Information</h2>
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "1rem" }}>
          <Input label="First Name" value={user.firstName} readOnly />
          <Input label="Last Name" value={user.lastName} readOnly />
        </div>
        <Input label="Email Address" value={user.email} readOnly />
        
        <Button style={{ marginTop: "1rem" }}>Edit Profile</Button>
      </div>
    </div>
  );
}
