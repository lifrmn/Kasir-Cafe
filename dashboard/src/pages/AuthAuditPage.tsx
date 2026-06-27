import { useEffect, useState } from "react";
import api from "../lib/api";
import { AuthAuditLog, AuthAuditSummary } from "../types";

export default function AuthAuditPage() {
  const [logs, setLogs] = useState<AuthAuditLog[]>([]);
  const [summary, setSummary] = useState<AuthAuditSummary | null>(null);
  const [event, setEvent] = useState("");
  const [username, setUsername] = useState("");
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(10);
  const [total, setTotal] = useState(0);
  const [error, setError] = useState("");
  const [exporting, setExporting] = useState(false);

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

  useEffect(() => {
    async function loadSummary() {
      try {
        const response = await api.get<AuthAuditSummary>("/admin/auth-audit-summary", {
          params: { days: 30 }
        });
        setSummary(response.data);
      } catch {
        setSummary(null);
      }
    }

    void loadSummary();
  }, []);

  async function handleExport() {
    try {
      setExporting(true);
      setError("");
      const response = await api.get("/admin/auth-audit-logs/export", {
        params: {
          event: event || undefined,
          username: username || undefined,
          date_from: dateFrom || undefined,
          date_to: dateTo || undefined
        },
        responseType: "blob"
      });

      const url = window.URL.createObjectURL(new Blob([response.data as BlobPart]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", `auth-audit-logs-${new Date().toISOString().slice(0, 10)}.csv`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch {
      setError("Gagal mengekspor CSV");
    } finally {
      setExporting(false);
    }
  }

  const totalPages = Math.max(1, Math.ceil(total / limit));
  const maxFailedPerDay = Math.max(1, ...(summary?.failed_login_per_day.map((d) => d.total) ?? [1]));

  return (
    <section className="table-card">
      <div className="table-head">
        <h2>Auth Audit Logs</h2>
        <button type="button" onClick={() => void handleExport()} disabled={exporting}>
          {exporting ? "Mengekspor..." : "Export CSV"}
        </button>
      </div>

      {summary && (
        <div className="summary-grid">
          <div className="summary-card">
            <span className="summary-label">Total Event (30h)</span>
            <strong className="summary-value">{summary.total_events}</strong>
          </div>
          <div className="summary-card">
            <span className="summary-label">Login</span>
            <strong className="summary-value">{summary.total_login}</strong>
          </div>
          <div className="summary-card summary-card--danger">
            <span className="summary-label">Failed Login</span>
            <strong className="summary-value">{summary.total_failed_login}</strong>
          </div>
          <div className="summary-card">
            <span className="summary-label">Logout</span>
            <strong className="summary-value">{summary.total_logout}</strong>
          </div>
          <div className="summary-card">
            <span className="summary-label">Refresh</span>
            <strong className="summary-value">{summary.total_refresh}</strong>
          </div>
        </div>
      )}

      {summary && (
        <div className="observability-grid">
          <div className="observability-panel">
            <h3>Failed Login per Hari</h3>
            {summary.failed_login_per_day.length === 0 ? (
              <p className="muted-text">Tidak ada failed login.</p>
            ) : (
              <ul className="bar-list">
                {summary.failed_login_per_day.map((item) => (
                  <li key={item.day}>
                    <span className="bar-label">
                      {new Date(item.day).toLocaleDateString("id-ID")}
                    </span>
                    <span className="bar-track">
                      <span
                        className="bar-fill"
                        style={{ width: `${(item.total / maxFailedPerDay) * 100}%` }}
                      />
                    </span>
                    <span className="bar-value">{item.total}</span>
                  </li>
                ))}
              </ul>
            )}
          </div>
          <div className="observability-panel">
            <h3>Top IP Address</h3>
            {summary.top_ip_addresses.length === 0 ? (
              <p className="muted-text">Belum ada data IP.</p>
            ) : (
              <ul className="rank-list">
                {summary.top_ip_addresses.map((item) => (
                  <li key={item.ip_address}>
                    <span>{item.ip_address}</span>
                    <strong>{item.total}</strong>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      )}

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
