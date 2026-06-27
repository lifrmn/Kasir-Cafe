import { useEffect, useState } from "react";
import api from "../lib/api";
import { AuthAuditLog } from "../types";

export default function AuthAuditPage() {
  const [logs, setLogs] = useState<AuthAuditLog[]>([]);
  const [event, setEvent] = useState("");
  const [username, setUsername] = useState("");
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(10);
  const [total, setTotal] = useState(0);
  const [error, setError] = useState("");

  useEffect(() => {
    async function load() {
      try {
        setError("");
        const response = await api.get<AuthAuditLog[]>("/admin/auth-audit-logs", {
          params: {
            event: event || undefined,
            username: username || undefined,
            date_from: dateFrom || undefined,
            date_to: dateTo || undefined,
            page,
            limit
          }
        });
        setLogs(response.data);
        setTotal(Number(response.headers["x-total-count"] ?? 0));
      } catch {
        setError("Gagal memuat auth audit logs");
      }
    }

    void load();
  }, [event, username, dateFrom, dateTo, page, limit]);

  const totalPages = Math.max(1, Math.ceil(total / limit));

  return (
    <section className="table-card">
      <div className="table-head">
        <h2>Auth Audit Logs</h2>
      </div>

      <div className="toolbar-grid">
        <select
          value={event}
          onChange={(e) => {
            setEvent(e.target.value);
            setPage(1);
          }}
        >
          <option value="">Semua Event</option>
          <option value="login">Login</option>
          <option value="refresh">Refresh</option>
          <option value="logout">Logout</option>
        </select>
        <input
          placeholder="Filter username"
          value={username}
          onChange={(e) => {
            setUsername(e.target.value);
            setPage(1);
          }}
        />
        <input
          type="datetime-local"
          value={dateFrom}
          onChange={(e) => {
            setDateFrom(e.target.value);
            setPage(1);
          }}
        />
        <input
          type="datetime-local"
          value={dateTo}
          onChange={(e) => {
            setDateTo(e.target.value);
            setPage(1);
          }}
        />
        <select
          value={limit}
          onChange={(e) => {
            setLimit(Number(e.target.value));
            setPage(1);
          }}
        >
          <option value={10}>10 / halaman</option>
          <option value={20}>20 / halaman</option>
          <option value={50}>50 / halaman</option>
        </select>
      </div>

      {error && <p className="error-text">{error}</p>}

      <table>
        <thead>
          <tr>
            <th>Waktu</th>
            <th>Event</th>
            <th>Username</th>
            <th>Role</th>
            <th>Status</th>
            <th>IP</th>
            <th>Detail</th>
          </tr>
        </thead>
        <tbody>
          {logs.map((log) => (
            <tr key={log.id}>
              <td>{new Date(log.created_at).toLocaleString("id-ID")}</td>
              <td>{log.event}</td>
              <td>{log.username || "-"}</td>
              <td>{log.role || "-"}</td>
              <td>{log.success ? "SUCCESS" : "FAILED"}</td>
              <td>{log.ip_address || "-"}</td>
              <td>{log.detail || "-"}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="pager-row">
        <button type="button" onClick={() => setPage((p) => Math.max(1, p - 1))}>Sebelumnya</button>
        <p>Halaman {page} dari {totalPages}</p>
        <button
          type="button"
          onClick={() => setPage((p) => p + 1)}
          disabled={page >= totalPages}
        >
          Berikutnya
        </button>
      </div>
    </section>
  );
}
