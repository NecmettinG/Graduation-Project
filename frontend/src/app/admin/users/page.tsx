"use client";

import { useEffect, useState } from "react";
import { fetchCoreApi } from "@/lib/api";

export default function AdminUsers() {
  const [users, setUsers] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchUsers();
  }, []);

  async function fetchUsers() {
    setLoading(true);
    try {
      const data = await fetchCoreApi("/users?page=1&limit=50", { requireAuth: true });
      setUsers(Array.isArray(data) ? data : (data.content || []));
    } catch (err) {
      console.error("Failed to load users", err);
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete(id: string) {
    if (!confirm("Are you sure you want to delete this user?")) return;
    try {
      await fetchCoreApi(`/users/${id}`, { method: "DELETE", requireAuth: true });
      setUsers(prev => prev.filter(u => u.userId !== id));
    } catch (err) {
      alert("Failed to delete user");
    }
  }

  return (
    <div>
      <div style={{ marginBottom: "2rem" }}>
        <h1 style={{ fontSize: "2rem", color: "var(--accent-primary)", fontFamily: "var(--font-outfit)" }}>
          Manage Users
        </h1>
        <p style={{ color: "var(--text-secondary)" }}>View and manage registered customers.</p>
      </div>

      <div className="glass-panel" style={{ overflowX: "auto" }}>
        {loading ? (
          <div style={{ padding: "3rem", textAlign: "center", color: "var(--text-secondary)" }}>Loading users...</div>
        ) : (
          <table style={{ width: "100%", borderCollapse: "collapse", textAlign: "left" }}>
            <thead>
              <tr style={{ borderBottom: "1px solid var(--border-color)", backgroundColor: "var(--bg-base)" }}>
                <th style={{ padding: "1rem", color: "var(--text-secondary)", fontWeight: 600 }}>ID</th>
                <th style={{ padding: "1rem", color: "var(--text-secondary)", fontWeight: 600 }}>First Name</th>
                <th style={{ padding: "1rem", color: "var(--text-secondary)", fontWeight: 600 }}>Last Name</th>
                <th style={{ padding: "1rem", color: "var(--text-secondary)", fontWeight: 600 }}>Email</th>
                <th style={{ padding: "1rem", color: "var(--text-secondary)", fontWeight: 600 }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u, idx) => (
                <tr key={u.userId || idx} style={{ borderBottom: "1px solid var(--border-color)" }}>
                  <td style={{ padding: "1rem", color: "var(--text-muted)", fontSize: "0.875rem" }}>
                    {u.userId?.substring(0, 8)}...
                  </td>
                  <td style={{ padding: "1rem", fontWeight: 500, color: "var(--text-primary)" }}>{u.firstName}</td>
                  <td style={{ padding: "1rem", color: "var(--text-primary)" }}>{u.lastName}</td>
                  <td style={{ padding: "1rem", color: "var(--text-secondary)" }}>{u.email}</td>
                  <td style={{ padding: "1rem" }}>
                    <div style={{ display: "flex", gap: "0.5rem" }}>
                      <button onClick={() => handleDelete(u.userId)} style={{ color: "#EF4444", background: "none", border: "none", cursor: "pointer", fontWeight: 500 }}>Delete</button>
                    </div>
                  </td>
                </tr>
              ))}
              {users.length === 0 && (
                <tr>
                  <td colSpan={5} style={{ padding: "3rem", textAlign: "center", color: "var(--text-secondary)" }}>
                    No users found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
