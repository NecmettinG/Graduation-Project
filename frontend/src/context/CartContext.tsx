"use client";

import React, { createContext, useContext, useState, useCallback, useEffect } from "react";
import { useAuth } from "./AuthContext";
import { fetchCoreApi } from "@/lib/api";

interface CartContextType {
  cartCount: number;
  refreshCart: () => void;
}

const CartContext = createContext<CartContextType>({ cartCount: 0, refreshCart: () => {} });

export function CartProvider({ children }: { children: React.ReactNode }) {
  const { user, loading: authLoading } = useAuth();
  const [cartCount, setCartCount] = useState(0);

  const refreshCart = useCallback(async () => {
    if (!user) {
      setCartCount(0);
      return;
    }
    try {
      const cart = await fetchCoreApi(`/users/${user.userId}/cart`, { requireAuth: true });
      const items = cart?.cartItems || cart?.items || [];
      const total = items.reduce((sum: number, item: any) => sum + (item.quantity || 1), 0);
      setCartCount(total);
    } catch {
      setCartCount(0);
    }
  }, [user]);

  useEffect(() => {
    if (!authLoading) {
      refreshCart();
    }
  }, [user, authLoading, refreshCart]);

  return (
    <CartContext.Provider value={{ cartCount, refreshCart }}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  return useContext(CartContext);
}
