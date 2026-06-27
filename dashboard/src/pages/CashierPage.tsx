import { useEffect, useMemo, useState } from "react";
import api from "../lib/api";
import { Product, Transaction } from "../types";

type CartItem = {
  product: Product;
  qty: number;
};

export default function CashierPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [cart, setCart] = useState<CartItem[]>([]);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [metodePembayaran, setMetodePembayaran] = useState("Tunai");
  const [diskon, setDiskon] = useState(0);
  const [pajak, setPajak] = useState(0);
  const [error, setError] = useState("");

  async function loadData() {
    try {
      const [productsRes, txRes] = await Promise.all([
        api.get<Product[]>("/produk"),
        api.get<Transaction[]>("/transaksi")
      ]);
      setProducts(productsRes.data);
      setTransactions(txRes.data);
    } catch {
      setError("Gagal memuat data kasir");
    }
  }

  useEffect(() => {
    void loadData();
  }, []);

  const total = useMemo(
    () => cart.reduce((sum, item) => sum + item.product.harga_jual * item.qty, 0),
    [cart]
  );
  const grandTotal = total - diskon + pajak;

  function addToCart(product: Product) {
    setCart((prev) => {
      const existing = prev.find((item) => item.product.id === product.id);
      if (!existing) return [...prev, { product, qty: 1 }];
      return prev.map((item) =>
        item.product.id === product.id ? { ...item, qty: item.qty + 1 } : item
      );
    });
  }

  async function checkout() {
    if (!cart.length) {
      setError("Keranjang masih kosong");
      return;
    }

    try {
      await api.post("/transaksi", {
        total,
        diskon,
        pajak,
        grand_total: grandTotal,
        metode_pembayaran: metodePembayaran,
        kasir: "kasir-web",
        detail: cart.map((item) => ({
          produk_id: item.product.id,
          jumlah: item.qty,
          harga: item.product.harga_jual,
          subtotal: item.product.harga_jual * item.qty
        }))
      });
      setCart([]);
      setError("");
      await loadData();
    } catch {
      setError("Checkout gagal");
    }
  }

  return (
    <section className="cashier-card">
      <h2>Kasir</h2>
      <p>Kelola keranjang, diskon, pajak, dan pembayaran transaksi.</p>

      <div className="quick-actions">
        <label>
          Metode
          <select value={metodePembayaran} onChange={(e) => setMetodePembayaran(e.target.value)}>
            <option value="Tunai">Tunai</option>
            <option value="QRIS">QRIS</option>
            <option value="Transfer">Transfer</option>
            <option value="Kartu">Kartu</option>
          </select>
        </label>
        <label>
          Diskon
          <input type="number" value={diskon} onChange={(e) => setDiskon(Number(e.target.value))} />
        </label>
        <label>
          Pajak
          <input type="number" value={pajak} onChange={(e) => setPajak(Number(e.target.value))} />
        </label>
      </div>

      <p>Total: Rp {total.toLocaleString("id-ID")}</p>
      <p>Grand Total: Rp {grandTotal.toLocaleString("id-ID")}</p>
      {error && <p className="error-text">{error}</p>}

      <div className="quick-actions">
        <button type="button" onClick={() => void checkout()}>Checkout</button>
      </div>

      <h3>Daftar Produk</h3>
      <div className="quick-actions">
        {products.map((product) => (
          <button key={product.id} type="button" onClick={() => addToCart(product)}>
            {product.nama} - Rp {product.harga_jual.toLocaleString("id-ID")}
          </button>
        ))}
      </div>

      <h3>Keranjang</h3>
      <ul>
        {cart.map((item) => (
          <li key={item.product.id}>
            {item.product.nama} x {item.qty} = Rp {(item.product.harga_jual * item.qty).toLocaleString("id-ID")}
          </li>
        ))}
      </ul>

      <h3>Riwayat Transaksi</h3>
      <ul>
        {transactions.map((tx) => (
          <li key={tx.id}>
            #{tx.id} - {tx.metode_pembayaran} - Rp {tx.grand_total.toLocaleString("id-ID")}
          </li>
        ))}
      </ul>
    </section>
  );
}
