INSERT INTO users (username, password, role)
VALUES ('kasir', '$2a$10$uS9SvQW27yN2f9xYQm2k6uS9xQfK4gQ6h38IriGv.v9G2jUo3V8sW', 'kasir')
ON CONFLICT (username) DO NOTHING;

INSERT INTO products (nama, barcode, kategori, harga_beli, harga_jual, stok, foto)
VALUES
  ('Espresso', '111111', 'Minuman', 12000, 18000, 30, ''),
  ('Cappuccino', '222222', 'Minuman', 18000, 26000, 20, ''),
  ('Croissant', '333333', 'Bakery', 12000, 22000, 15, '')
ON CONFLICT DO NOTHING;
