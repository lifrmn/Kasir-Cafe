import { Link, useLocation } from "react-router-dom";
import { PropsWithChildren } from "react";
import { useAuth } from "../context/AuthContext";

export default function Layout({ children }: PropsWithChildren) {
  const location = useLocation();
  const { auth, logout } = useAuth();

  const links = [
    { to: "/dashboard", label: "Dashboard" },
    { to: "/produk", label: "Produk" },
    { to: "/kasir", label: "Kasir" },
    ...(auth?.role === "admin" ? [{ to: "/audit", label: "Audit Auth" }] : [])
  ];

  return (
    <div className="app-shell">
      <header className="app-header">
        <div>
          <p className="eyebrow">Kasir Cafe</p>
          <h1>POS Admin Dashboard</h1>
          <p className="role-pill">Role: {auth?.role ?? "-"}</p>
        </div>
        <div className="header-actions">
          <nav className="app-nav">
            {links.map((link) => (
              <Link
                key={link.to}
                to={link.to}
                className={location.pathname === link.to ? "active" : ""}
              >
                {link.label}
              </Link>
            ))}
          </nav>
          <button type="button" onClick={() => void logout()}>Logout</button>
        </div>
      </header>
      <main className="app-main">{children}</main>
    </div>
  );
}
