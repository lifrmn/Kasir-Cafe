import { createContext, PropsWithChildren, useContext, useMemo, useState } from "react";
import api from "../lib/api";

type AuthState = {
  token: string;
  role: string;
};

type AuthContextValue = {
  auth: AuthState | null;
  isAuthenticated: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: PropsWithChildren) {
  const [auth, setAuth] = useState<AuthState | null>(() => {
    const token = localStorage.getItem("pos_token");
    const role = localStorage.getItem("pos_role");
    if (!token || !role) return null;
    return { token, role };
  });

  const value = useMemo<AuthContextValue>(
    () => ({
      auth,
      isAuthenticated: Boolean(auth?.token),
      login: async (username: string, password: string) => {
        const response = await api.post<{ token: string; role: string }>("/login", {
          username,
          password
        });

        const nextAuth = {
          token: response.data.token,
          role: response.data.role
        };

        localStorage.setItem("pos_token", nextAuth.token);
        localStorage.setItem("pos_role", nextAuth.role);
        setAuth(nextAuth);
      },
      logout: async () => {
        try {
          await api.post("/logout");
        } catch {
          // Keep client logout resilient even if backend token already invalid.
        }
        localStorage.removeItem("pos_token");
        localStorage.removeItem("pos_role");
        setAuth(null);
      }
    }),
    [auth]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
