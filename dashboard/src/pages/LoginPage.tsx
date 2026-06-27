import { FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [username, setUsername] = useState("admin");
  const [password, setPassword] = useState("admin123");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setIsLoading(true);
    setError("");
    try {
      await login(username, password);
      navigate("/dashboard", { replace: true });
    } catch {
      setError("Login gagal. Jalankan endpoint /seed-admin jika user belum tersedia.");
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <section className="login-page">
      <form onSubmit={handleSubmit} className="login-card">
        <h2>Login Dashboard</h2>
        <p>Default akun: admin / admin123</p>
        <label htmlFor="username">Username</label>
        <input
          id="username"
          value={username}
          onChange={(event) => setUsername(event.target.value)}
          required
        />

        <label htmlFor="password">Password</label>
        <input
          id="password"
          type="password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          required
        />

        {error && <p className="error-text">{error}</p>}

        <button type="submit" disabled={isLoading}>
          {isLoading ? "Memproses..." : "Login"}
        </button>
      </form>
    </section>
  );
}
