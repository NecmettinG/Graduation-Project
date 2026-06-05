"use client";

import { useState, useEffect } from "react";
import { useAuth } from "@/context/AuthContext";
import { useRouter } from "next/navigation";
import { Input } from "@/components/Input";
import { Button } from "@/components/Button";
import { fetchCoreApi } from "@/lib/api";

export default function ProfilePage() {
  const { user, loading } = useAuth();
  const router = useRouter();

  const [isEditing, setIsEditing] = useState(false);
  const [profileData, setProfileData] = useState({ firstName: "", lastName: "" });
  const [isSavingProfile, setIsSavingProfile] = useState(false);

  const [passwordData, setPasswordData] = useState({ password: "", confirmPassword: "" });
  const [isSavingPassword, setIsSavingPassword] = useState(false);

  useEffect(() => {
    if (user) {
      setProfileData({ firstName: user.firstName, lastName: user.lastName });
    }
  }, [user]);

  useEffect(() => {
    if (!loading && !user) {
      router.push("/login");
    }
  }, [user, loading, router]);

  if (loading || !user) {
    return <div className="container" style={{ padding: "4rem 0", textAlign: "center" }}>Loading...</div>;
  }

  const handleProfileSave = async () => {
    setIsSavingProfile(true);
    try {
      await fetchCoreApi(`/users/${user.userId}`, {
        method: "PUT",
        requireAuth: true,
        body: JSON.stringify({
          firstName: profileData.firstName,
          lastName: profileData.lastName,
          // Sending empty password usually ignores it in backend unless validated,
          // but we can just send what's required or not update password.
        })
      });
      setIsEditing(false);
      alert("Profile updated successfully!");
      // Note: In a real app, we'd also update the AuthContext state here
    } catch (err) {
      alert("Failed to update profile");
    } finally {
      setIsSavingProfile(false);
    }
  };

  const handlePasswordSave = async () => {
    if (!passwordData.password) return;
    if (passwordData.password !== passwordData.confirmPassword) {
      alert("Passwords do not match!");
      return;
    }
    
    setIsSavingPassword(true);
    try {
      // By sending password in the same PUT endpoint, it updates the user's password.
      await fetchCoreApi(`/users/${user.userId}`, {
        method: "PUT",
        requireAuth: true,
        body: JSON.stringify({
          firstName: user.firstName,
          lastName: user.lastName,
          password: passwordData.password
        })
      });
      setPasswordData({ password: "", confirmPassword: "" });
      alert("Password updated successfully!");
    } catch (err) {
      alert("Failed to update password");
    } finally {
      setIsSavingPassword(false);
    }
  };

  return (
    <div className="container" style={{ padding: "4rem 1.5rem" }}>
      <h1 style={{ fontSize: "2.5rem", color: "var(--accent-primary)", marginBottom: "2rem", fontFamily: "var(--font-outfit)" }}>
        My Profile
      </h1>
      
      <div style={{ display: "flex", flexDirection: "column", gap: "2rem" }}>
        {/* Personal Info Section */}
        <div className="glass-panel" style={{ padding: "2rem", maxWidth: "600px" }}>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1.5rem" }}>
            <h2 style={{ fontFamily: "var(--font-inter)", margin: 0 }}>Personal Information</h2>
            {!isEditing ? (
              <button onClick={() => setIsEditing(true)} style={{ background: "transparent", border: "none", color: "var(--accent-primary)", cursor: "pointer", fontWeight: 600 }}>
                Edit
              </button>
            ) : (
              <button onClick={() => setIsEditing(false)} style={{ background: "transparent", border: "none", color: "var(--text-secondary)", cursor: "pointer", fontWeight: 600 }}>
                Cancel
              </button>
            )}
          </div>

          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "1rem" }}>
            <Input 
              label="First Name" 
              value={isEditing ? profileData.firstName : user.firstName} 
              readOnly={!isEditing} 
              onChange={e => setProfileData({ ...profileData, firstName: e.target.value })}
            />
            <Input 
              label="Last Name" 
              value={isEditing ? profileData.lastName : user.lastName} 
              readOnly={!isEditing} 
              onChange={e => setProfileData({ ...profileData, lastName: e.target.value })}
            />
          </div>
          <Input label="Email Address" value={user.email} readOnly />
          
          {isEditing && (
            <Button style={{ marginTop: "1rem" }} onClick={handleProfileSave} disabled={isSavingProfile}>
              {isSavingProfile ? "Saving..." : "Save Profile"}
            </Button>
          )}
        </div>

        {/* Change Password Section */}
        <div className="glass-panel" style={{ padding: "2rem", maxWidth: "600px" }}>
          <h2 style={{ marginBottom: "1.5rem", fontFamily: "var(--font-inter)" }}>Security</h2>
          <p style={{ color: "var(--text-secondary)", marginBottom: "1rem", fontSize: "0.9rem" }}>Update your password here.</p>
          
          <div style={{ display: "grid", gridTemplateColumns: "1fr", gap: "1rem" }}>
            <Input 
              label="New Password" 
              type="password" 
              value={passwordData.password} 
              onChange={e => setPasswordData({ ...passwordData, password: e.target.value })}
              placeholder="Enter new password"
            />
            <Input 
              label="Confirm New Password" 
              type="password" 
              value={passwordData.confirmPassword} 
              onChange={e => setPasswordData({ ...passwordData, confirmPassword: e.target.value })}
              placeholder="Verify your new password"
            />
          </div>
          
          <Button 
            style={{ marginTop: "1rem" }} 
            onClick={handlePasswordSave} 
            disabled={isSavingPassword || !passwordData.password || !passwordData.confirmPassword}
          >
            {isSavingPassword ? "Updating..." : "Update Password"}
          </Button>
        </div>
      </div>
    </div>
  );
}
