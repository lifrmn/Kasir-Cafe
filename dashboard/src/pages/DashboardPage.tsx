import {
  Area,
  AreaChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis
} from "recharts";

import { useEffect, useMemo, useState } from "react";
import api from "../lib/api";
import { Report, Transaction } from "../types";

const days = ["Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab"];

export default function DashboardPage() {
  const [report, setReport] = useState<Report | null>(null);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    async function load() {
      try {
        const [reportRes, txRes] = await Promise.all([
          api.get<Report>("/laporan"),
          api.get<Transaction[]>("/transaksi")
        ]);
        setReport(reportRes.data);
        setTransactions(txRes.data);
      } catch {
        setError("Gagal memuat data dashboard");
      }
    }

    void load();
  }, []);

  const chartData = useMemo(() => {
    const byDay = new Map<string, number>();
    for (const tx of transactions) {
      const date = new Date(tx.tanggal);
      const label = days[date.getDay()];
      byDay.set(label, (byDay.get(label) ?? 0) + tx.grand_total);
    }
    return days.map((day) => ({ day, sales: byDay.get(day) ?? 0 }));
  }, [transactions]);

  const daily = report?.harian;

  return (
    <section className="panel-grid">
      <article className="stat-card">
        <h2>Total Penjualan Hari Ini</h2>
        <p>Rp {(daily?.total_penjualan ?? 0).toLocaleString("id-ID")}</p>
      </article>
      <article className="stat-card">
        <h2>Total Transaksi</h2>
        <p>{(daily?.transaksi ?? 0).toLocaleString("id-ID")}</p>
      </article>
      <article className="stat-card">
        <h2>Produk Terjual</h2>
        <p>{transactions.length.toLocaleString("id-ID")}</p>
      </article>
      <article className="stat-card">
        <h2>Profit</h2>
        <p>Rp {(daily?.profit ?? 0).toLocaleString("id-ID")}</p>
      </article>

      {error && <p className="error-text">{error}</p>}

      <article className="chart-card">
        <h2>Grafik Penjualan Mingguan</h2>
        <div className="chart-wrap">
          <ResponsiveContainer width="100%" height={280}>
            <AreaChart data={chartData}>
              <defs>
                <linearGradient id="sales" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#e85d04" stopOpacity={0.7} />
                  <stop offset="95%" stopColor="#e85d04" stopOpacity={0.05} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="day" />
              <YAxis />
              <Tooltip />
              <Area type="monotone" dataKey="sales" stroke="#e85d04" fill="url(#sales)" />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </article>
    </section>
  );
}
