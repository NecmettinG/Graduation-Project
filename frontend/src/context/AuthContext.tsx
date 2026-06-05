"use client";

import React, { createContext, useContext, useState, useEffect } from "react";
import { fetchCoreApi, setAuthToken, removeAuthToken, getAuthToken } from "@/lib/api";

interface User {
  userId: string;
  firstName: string;
  lastName: string;
  email: string;
}

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (token: string, userId: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if token exists on mount
    const initAuth = async () => {
      const token = getAuthToken();
      const storedUserId = localStorage.getItem("userId");
      
      if (token && storedUserId) {
        try {
          // Verify by fetching user details
          const userData = await fetchCoreApi(`/users/${storedUserId}`, { requireAuth: true });
          setUser(userData);
        } catch (error) {
          console.error("Token invalid or expired", error);
          removeAuthToken();
          localStorage.removeItem("userId");
        }
      }
      setLoading(false);
    };

    initAuth();
  }, []);

  const login = async (token: string, userId: string) => {
    setAuthToken(token);
    localStorage.setItem("userId", userId);
    
    // Fetch user details immediately after setting token
    const userData = await fetchCoreApi(`/users/${userId}`, { requireAuth: true });
    setUser(userData);
  };

  const logout = () => {
    removeAuthToken();
    localStorage.removeItem("userId");
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
