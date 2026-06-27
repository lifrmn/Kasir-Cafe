import { Navigate, Route, Routes } from "react-router-dom";
import Layout from "./components/Layout";
import DashboardPage from "./pages/DashboardPage";
import ProductsPage from "./pages/ProductsPage";
import CashierPage from "./pages/CashierPage";
import LoginPage from "./pages/LoginPage";
import { useAuth } from "./context/AuthContext";
import AuthAuditPage from "./pages/AuthAuditPage";

function ProtectedLayout() {
  const { isAuthenticated, auth } = useAuth();
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/produk" element={<ProductsPage />} />
        <Route path="/kasir" element={<CashierPage />} />
        {auth?.role === "admin" && <Route path="/audit" element={<AuthAuditPage />} />}
      </Routes>
    </Layout>
  );
}

export default function App() {
  const { isAuthenticated } = useAuth();

  return (
    <Routes>
      <Route
        path="/login"
        element={isAuthenticated ? <Navigate to="/dashboard" replace /> : <LoginPage />}
      />
      <Route path="/*" element={<ProtectedLayout />} />
    </Routes>
  );
}
