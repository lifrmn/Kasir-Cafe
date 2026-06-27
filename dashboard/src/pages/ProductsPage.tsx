import { FormEvent, useEffect, useState } from "react";
import api from "../lib/api";
import { Product } from "../types";

const emptyForm: Product = {
  id: 0,
  nama: "",
  barcode: "",
  kategori: "",
  harga_beli: 0,
  harga_jual: 0,
  stok: 0,
  foto: ""
};

export default function ProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [form, setForm] = useState<Product>(emptyForm);
  const [error, setError] = useState("");

  async function loadProducts() {
    try {
      const response = await api.get<Product[]>("/produk");
      setProducts(response.data);
    } catch {
      setError("Gagal memuat produk");
    }
  }

  useEffect(() => {
    void loadProducts();
  }, []);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError("");
    try {
      if (form.id === 0) {
        await api.post("/produk", form);
      } else {
        await api.put(`/produk/${form.id}`, form);
      }
      setForm(emptyForm);
      await loadProducts();
    } catch {
      setError("Gagal menyimpan produk");
    }
  }

  async function handleDelete(id: number) {
    try {
      await api.delete(`/produk/${id}`);
      await loadProducts();
    } catch {
      setError("Gagal menghapus produk");
    }
  }

  return (
    <section className="table-card">
      <div className="table-head">
        <h2>Produk</h2>
        <button type="button" onClick={() => setForm(emptyForm)}>Form Baru</button>
      </div>

      <form className="form-grid" onSubmit={handleSubmit}>
        <input
          placeholder="Nama"
          value={form.nama}
          onChange={(event) => setForm((prev) => ({ ...prev, nama: event.target.value }))}
          required
        />
        <input
          placeholder="Barcode"
          value={form.barcode}
          onChange={(event) => setForm((prev) => ({ ...prev, barcode: event.target.value }))}
          required
        />
        <input
          placeholder="Kategori"
          value={form.kategori}
          onChange={(event) => setForm((prev) => ({ ...prev, kategori: event.target.value }))}
          required
        />
        <input
          placeholder="Harga Beli"
          type="number"
          value={form.harga_beli}
          onChange={(event) => setForm((prev) => ({ ...prev, harga_beli: Number(event.target.value) }))}
          required
        />
        <input
          placeholder="Harga Jual"
          type="number"
          value={form.harga_jual}
          onChange={(event) => setForm((prev) => ({ ...prev, harga_jual: Number(event.target.value) }))}
          required
        />
        <input
          placeholder="Stok"
          type="number"
          value={form.stok}
          onChange={(event) => setForm((prev) => ({ ...prev, stok: Number(event.target.value) }))}
          required
        />
        <button type="submit">{form.id === 0 ? "Tambah Produk" : "Update Produk"}</button>
      </form>

      {error && <p className="error-text">{error}</p>}

      <table>
        <thead>
          <tr>
            <th>Nama</th>
            <th>Kategori</th>
            <th>Stok</th>
            <th>Harga Jual</th>
            <th>Aksi</th>
          </tr>
        </thead>
        <tbody>
          {products.map((product) => (
            <tr key={product.id}>
              <td>{product.nama}</td>
              <td>{product.kategori}</td>
              <td>{product.stok}</td>
              <td>Rp {product.harga_jual.toLocaleString("id-ID")}</td>
              <td>
                <div className="quick-actions">
                  <button type="button" onClick={() => setForm(product)}>Edit</button>
                  <button type="button" onClick={() => void handleDelete(product.id)}>Hapus</button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
