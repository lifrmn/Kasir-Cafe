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
