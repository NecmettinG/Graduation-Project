"use client";

import { useState, useEffect } from "react";
import { useAuth } from "@/context/AuthContext";
import { useToast } from "@/context/ToastContext";
import { useRouter } from "next/navigation";
import { Input } from "@/components/Input";
import { Button } from "@/components/Button";
import { Skeleton } from "@/components/Skeleton";
import { fetchCoreApi } from "@/lib/api";

export default function ProfilePage() {
  const { user, loading } = useAuth();
  const { toast } = useToast();
  const router = useRouter();

  const [isEditing, setIsEditing] = useState(false);
  const [profileData, setProfileData] = useState({ firstName: "", lastName: "" });
  const [isSavingProfile, setIsSavingProfile] = useState(false);

  const [passwordData, setPasswordData] = useState({ password: "", confirmPassword: "" });
  const [isSavingPassword, setIsSavingPassword] = useState(false);

  const [addresses, setAddresses] = useState<any[]>([]);
  const [isAddressesLoading, setIsAddressesLoading] = useState(false);
  const [isAddingAddress, setIsAddingAddress] = useState(false);
  const [newAddress, setNewAddress] = useState({
    streetName: "",
    city: "",
    postalCode: "",
    country: "",
    type: "Shipping"
  });

  useEffect(() => {
    if (user) {
      setProfileData({ firstName: user.firstName, lastName: user.lastName });
      loadAddresses(user.userId);
    }
  }, [user]);

  const loadAddresses = async (userId: string) => {
    setIsAddressesLoading(true);
    try {
      const data = await fetchCoreApi(`/users/${userId}/addresses`, { requireAuth: true });
      if (Array.isArray(data)) {
        setAddresses(data);
      }
    } catch (err) {
      console.error("Failed to load addresses", err);
    } finally {
      setIsAddressesLoading(false);
    }
  };

  useEffect(() => {
    if (!loading && !user) {
      router.push("/login");
    }
  }, [user, loading, router]);

  if (loading || !user) {
    return (
      <div className="container" style={{ padding: "4rem 1.5rem", maxWidth: "720px", margin: "0 auto" }}>
        <Skeleton width="40%" height="2rem" />
        <div style={{ marginTop: "1.5rem", display: "flex", flexDirection: "column", gap: "1rem" }}>
          <Skeleton height="3rem" />
          <Skeleton height="3rem" />
          <Skeleton width="60%" height="3rem" />
        </div>
        <div style={{ marginTop: "2.5rem" }}>
          <Skeleton width="35%" height="1.5rem" />
          <div style={{ marginTop: "1rem", display: "flex", flexDirection: "column", gap: "0.75rem" }}>
            <Skeleton height="4rem" />
            <Skeleton height="4rem" />
          </div>
        </div>
      </div>
    );
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
      toast("Profile updated successfully!", "success");
      // Note: In a real app, we'd also update the AuthContext state here
    } catch (err) {
      toast("Failed to update profile.", "error");
    } finally {
      setIsSavingProfile(false);
    }
  };

  const handlePasswordSave = async () => {
    if (!passwordData.password) return;
    if (passwordData.password !== passwordData.confirmPassword) {
      toast("Passwords do not match!", "warning");
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
      toast("Password updated successfully!", "success");
    } catch (err) {
      toast("Failed to update password.", "error");
    } finally {
      setIsSavingPassword(false);
    }
  };

  const handleAddAddress = async () => {
    if (!newAddress.streetName || !newAddress.city || !newAddress.country) {
      toast("Please fill in all required fields (Street, City, Country).", "warning");
      return;
    }
    
    try {
      await fetchCoreApi(`/users/${user?.userId}/addresses`, {
        method: "POST",
        requireAuth: true,
        body: JSON.stringify(newAddress)
      });
      setIsAddingAddress(false);
      setNewAddress({ streetName: "", city: "", postalCode: "", country: "", type: "Shipping" });
      if (user) loadAddresses(user.userId);
      toast("Address added successfully!", "success");
    } catch (err) {
      toast("Failed to add address.", "error");
    }
  };

  const handleDeleteAddress = async (addressId: string) => {
    if (!confirm("Are you sure you want to delete this address?")) return;
    try {
      await fetchCoreApi(`/users/${user?.userId}/addresses/${addressId}`, {
        method: "DELETE",
        requireAuth: true
      });
      if (user) loadAddresses(user.userId);
    } catch (err) {
      toast("Failed to delete address.", "error");
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

        {/* My Addresses Section */}
        <div className="glass-panel" style={{ padding: "2rem", maxWidth: "600px" }}>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1.5rem" }}>
            <h2 style={{ fontFamily: "var(--font-inter)", margin: 0 }}>My Addresses</h2>
            {!isAddingAddress ? (
              <button onClick={() => setIsAddingAddress(true)} style={{ background: "transparent", border: "none", color: "var(--accent-primary)", cursor: "pointer", fontWeight: 600 }}>
                + Add New
              </button>
            ) : (
              <button onClick={() => setIsAddingAddress(false)} style={{ background: "transparent", border: "none", color: "var(--text-secondary)", cursor: "pointer", fontWeight: 600 }}>
                Cancel
              </button>
            )}
          </div>

          {isAddressesLoading ? (
            <p style={{ color: "var(--text-secondary)" }}>Loading addresses...</p>
          ) : addresses.length === 0 ? (
            <p style={{ color: "var(--text-secondary)" }}>You have no saved addresses.</p>
          ) : (
            <div style={{ display: "flex", flexDirection: "column", gap: "1rem", marginBottom: isAddingAddress ? "2rem" : "0" }}>
              {addresses.map(addr => (
                <div key={addr.addressId} style={{ padding: "1rem", border: "1px solid var(--border-color)", borderRadius: "8px", position: "relative" }}>
                  <div style={{ position: "absolute", top: "1rem", right: "1rem" }}>
                    <button 
                      onClick={() => handleDeleteAddress(addr.addressId)} 
                      style={{ background: "transparent", border: "none", color: "#EF4444", cursor: "pointer", fontSize: "0.875rem" }}
                    >
                      Delete
                    </button>
                  </div>
                  <div style={{ display: "inline-block", padding: "0.2rem 0.5rem", background: "var(--accent-primary)", color: "#fff", borderRadius: "4px", fontSize: "0.75rem", marginBottom: "0.5rem" }}>
                    {addr.type || "Address"}
                  </div>
                  <p style={{ margin: "0 0 0.25rem 0", fontWeight: 500 }}>{addr.streetName}</p>
                  <p style={{ margin: "0", color: "var(--text-secondary)", fontSize: "0.9rem" }}>{addr.city}, {addr.postalCode}</p>
                  <p style={{ margin: "0", color: "var(--text-secondary)", fontSize: "0.9rem" }}>{addr.country}</p>
                </div>
              ))}
            </div>
          )}

          {isAddingAddress && (
            <div style={{ marginTop: "1.5rem", borderTop: addresses.length > 0 ? "1px solid var(--border-color)" : "none", paddingTop: addresses.length > 0 ? "1.5rem" : "0" }}>
              <h3 style={{ fontSize: "1.1rem", marginBottom: "1rem" }}>Add a New Address</h3>
              <Input 
                label="Street Name & Number" 
                type="text" 
                value={newAddress.streetName}
                onChange={(e) => setNewAddress({ ...newAddress, streetName: e.target.value })}
              />
              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "1rem" }}>
                <Input 
                  label="City" 
                  type="text" 
                  value={newAddress.city}
                  onChange={(e) => setNewAddress({ ...newAddress, city: e.target.value })}
                />
                <Input 
                  label="Postal Code" 
                  type="text" 
                  value={newAddress.postalCode}
                  onChange={(e) => setNewAddress({ ...newAddress, postalCode: e.target.value })}
                />
              </div>
              <div style={{ display: "flex", gap: "1rem", marginBottom: "1rem" }}>
                <Input 
                  label="Country" 
                  type="text" 
                  value={newAddress.country}
                  onChange={(e) => setNewAddress({ ...newAddress, country: e.target.value })}
                  style={{ flex: 2 }}
                />
                <div style={{ flex: 1, display: "flex", flexDirection: "column", gap: "0.5rem" }}>
                  <label style={{ fontSize: "0.875rem", fontWeight: 500 }}>Type</label>
                  <select 
                    value={newAddress.type} 
                    onChange={(e) => setNewAddress({ ...newAddress, type: e.target.value })}
                    style={{ padding: "0.75rem", borderRadius: "0.5rem", border: "1px solid var(--border-color)", background: "var(--bg-primary)", color: "var(--text-primary)" }}
                  >
                    <option value="Shipping" style={{ color: "#000" }}>Shipping</option>
                    <option value="Billing" style={{ color: "#000" }}>Billing</option>
                  </select>
                </div>
              </div>
              <Button onClick={handleAddAddress}>Save Address</Button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
