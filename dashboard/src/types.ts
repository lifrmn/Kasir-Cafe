export type Product = {
  id: number;
  nama: string;
  barcode: string;
  kategori: string;
  harga_beli: number;
  harga_jual: number;
  stok: number;
  foto?: string;
};

export type Transaction = {
  id: number;
  tanggal: string;
  total: number;
  diskon: number;
  pajak: number;
  grand_total: number;
  metode_pembayaran: string;
  kasir: string;
};

export type ReportPeriod = {
  total_penjualan: number;
  transaksi: number;
  profit: number;
};

export type Report = {
  harian: ReportPeriod;
  mingguan: ReportPeriod;
  bulanan: ReportPeriod;
  tahunan: ReportPeriod;
};

export type AuthAuditLog = {
  id: number;
  event: "login" | "refresh" | "logout";
  username: string;
  role: string;
  success: boolean;
  ip_address: string;
  user_agent: string;
  detail: string;
  created_at: string;
};

export type AuthAuditDailyFailedLogin = {
  day: string;
  total: number;
};

export type AuthAuditTopIP = {
  ip_address: string;
  total: number;
};

export type AuthAuditSummary = {
  total_events: number;
  total_login: number;
  total_failed_login: number;
  total_logout: number;
  total_refresh: number;
  failed_login_per_day: AuthAuditDailyFailedLogin[];
  top_ip_addresses: AuthAuditTopIP[];
};
