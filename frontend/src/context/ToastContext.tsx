"use client";

import React, { createContext, useContext, useState, useCallback, useRef } from "react";

type ToastType = "success" | "error" | "info" | "warning";

interface Toast {
  id: number;
  message: string;
  type: ToastType;
}

interface ToastContextType {
  toast: (message: string, type?: ToastType) => void;
}

const ToastContext = createContext<ToastContextType | undefined>(undefined);

let toastIdCounter = 0;

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);
  const timersRef = useRef<Map<number, ReturnType<typeof setTimeout>>>(new Map());

  const removeToast = useCallback((id: number) => {
    setToasts(prev => prev.filter(t => t.id !== id));
    const timer = timersRef.current.get(id);
    if (timer) {
      clearTimeout(timer);
      timersRef.current.delete(id);
    }
  }, []);

  const toast = useCallback((message: string, type: ToastType = "info") => {
    const id = ++toastIdCounter;
    setToasts(prev => [...prev, { id, message, type }]);
    const timer = setTimeout(() => removeToast(id), 4000);
    timersRef.current.set(id, timer);
  }, [removeToast]);

  return (
    <ToastContext.Provider value={{ toast }}>
      {children}

      {/* Toast Container */}
      <div style={{
        position: "fixed",
        top: "1.5rem",
        right: "1.5rem",
        zIndex: 9999,
        display: "flex",
        flexDirection: "column",
        gap: "0.5rem",
        pointerEvents: "none",
        maxWidth: "420px",
        width: "100%",
      }}>
        {toasts.map(t => (
          <div
            key={t.id}
            style={{
              pointerEvents: "auto",
              display: "flex",
              alignItems: "center",
              gap: "0.75rem",
              padding: "0.85rem 1.25rem",
              borderRadius: "10px",
              boxShadow: "0 8px 24px rgba(0,0,0,0.15), 0 2px 6px rgba(0,0,0,0.08)",
              animation: "toastSlideIn 0.3s ease-out",
              cursor: "pointer",
              ...getToastStyle(t.type),
            }}
            onClick={() => removeToast(t.id)}
          >
            <span style={{ fontSize: "1.15rem", flexShrink: 0 }}>{getToastIcon(t.type)}</span>
            <span style={{ fontSize: "0.9rem", fontWeight: 500, lineHeight: 1.4, flex: 1 }}>{t.message}</span>
            <button
              onClick={(e) => { e.stopPropagation(); removeToast(t.id); }}
              style={{
                background: "none",
                border: "none",
                color: "inherit",
                opacity: 0.6,
                cursor: "pointer",
                fontSize: "1rem",
                padding: "0 0 0 0.5rem",
                flexShrink: 0,
              }}
            >✕</button>
          </div>
        ))}
      </div>

      <style>{`
        @keyframes toastSlideIn {
          from { opacity: 0; transform: translateX(60px); }
          to { opacity: 1; transform: translateX(0); }
        }
      `}</style>
    </ToastContext.Provider>
  );
}

function getToastIcon(type: ToastType): string {
  switch (type) {
    case "success": return "✓";
    case "error":   return "✕";
    case "warning": return "⚠";
    case "info":    return "ℹ";
  }
}

function getToastStyle(type: ToastType): React.CSSProperties {
  switch (type) {
    case "success":
      return { background: "#065F46", color: "#ECFDF5", borderLeft: "4px solid #34D399" };
    case "error":
      return { background: "#7F1D1D", color: "#FEF2F2", borderLeft: "4px solid #F87171" };
    case "warning":
      return { background: "#78350F", color: "#FFFBEB", borderLeft: "4px solid #FBBF24" };
    case "info":
      return { background: "#1E3A5F", color: "#EFF6FF", borderLeft: "4px solid #60A5FA" };
  }
}

export function useToast() {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error("useToast must be used within a ToastProvider");
  }
  return context;
}
