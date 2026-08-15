INSERT INTO categories (cat_name, description, created_at, updated_at)
SELECT 'Electronics', 'Phones, audio, and smart devices', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE cat_name = 'Electronics'
);

INSERT INTO categories (cat_name, description, created_at, updated_at)
SELECT 'Groceries', 'Daily household and kitchen essentials', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE cat_name = 'Groceries'
);

UPDATE categories
SET description = 'Phones, audio, and smart devices',
    updated_at = NOW()
WHERE cat_name = 'Electronics';

UPDATE categories
SET description = 'Daily household and kitchen essentials',
    updated_at = NOW()
WHERE cat_name = 'Groceries';

INSERT INTO users (name, email, password, created_at, updated_at)
SELECT 'Rahul Sharma', 'rahul@example.com', 'rahul123', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'rahul@example.com'
);

INSERT INTO users (name, email, password, created_at, updated_at)
SELECT 'Priya Verma', 'priya@example.com', 'priya123', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'priya@example.com'
);

UPDATE users
SET name = 'Rahul Sharma',
    password = 'rahul123',
    updated_at = NOW()
WHERE email = 'rahul@example.com';

UPDATE users
SET name = 'Priya Verma',
    password = 'priya123',
    updated_at = NOW()
WHERE email = 'priya@example.com';

INSERT INTO addresses (user_id, street, city, state, zip_code, country)
SELECT u.id, '221 MG Road', 'Bengaluru', 'Karnataka', '560001', 'India'
FROM users u
WHERE u.email = 'rahul@example.com'
  AND NOT EXISTS (
      SELECT 1
      FROM addresses a
      WHERE a.user_id = u.id
        AND a.street = '221 MG Road'
        AND a.zip_code = '560001'
  );

INSERT INTO addresses (user_id, street, city, state, zip_code, country)
SELECT u.id, '14 Park Street', 'Kolkata', 'West Bengal', '700016', 'India'
FROM users u
WHERE u.email = 'priya@example.com'
  AND NOT EXISTS (
      SELECT 1
      FROM addresses a
      WHERE a.user_id = u.id
        AND a.street = '14 Park Street'
        AND a.zip_code = '700016'
  );

UPDATE addresses a
JOIN users u ON u.id = a.user_id
SET a.street = '221 MG Road',
    a.city = 'Bengaluru',
    a.state = 'Karnataka',
    a.zip_code = '560001',
    a.country = 'India'
WHERE u.email = 'rahul@example.com'
  AND a.street = '221 MG Road';

UPDATE addresses a
JOIN users u ON u.id = a.user_id
SET a.street = '14 Park Street',
    a.city = 'Kolkata',
    a.state = 'West Bengal',
    a.zip_code = '700016',
    a.country = 'India'
WHERE u.email = 'priya@example.com'
  AND a.street = '14 Park Street';

INSERT INTO products (
    sku, name, description, price, stock_qty, low_stock_qty, category_id, active, created_at, updated_at
)
SELECT
    'SKU-MOB-001',
    'Samsung Galaxy M14',
    '5G smartphone with 6000mAh battery',
    12999.00,
    25,
    5,
    c.id,
    true,
    NOW(),
    NOW()
FROM categories c
WHERE c.cat_name = 'Electronics'
  AND NOT EXISTS (
      SELECT 1 FROM products p WHERE p.sku = 'SKU-MOB-001'
  );

INSERT INTO products (
    sku, name, description, price, stock_qty, low_stock_qty, category_id, active, created_at, updated_at
)
SELECT
    'SKU-AUD-001',
    'Boat Rockerz 450',
    'Wireless Bluetooth headphone',
    1599.00,
    40,
    10,
    c.id,
    true,
    NOW(),
    NOW()
FROM categories c
WHERE c.cat_name = 'Electronics'
  AND NOT EXISTS (
      SELECT 1 FROM products p WHERE p.sku = 'SKU-AUD-001'
  );

INSERT INTO products (
    sku, name, description, price, stock_qty, low_stock_qty, category_id, active, created_at, updated_at
)
SELECT
    'SKU-GRC-001',
    'Basmati Rice 5kg',
    'Premium long grain basmati rice',
    799.00,
    60,
    10,
    c.id,
    true,
    NOW(),
    NOW()
FROM categories c
WHERE c.cat_name = 'Groceries'
  AND NOT EXISTS (
      SELECT 1 FROM products p WHERE p.sku = 'SKU-GRC-001'
  );

UPDATE products p
JOIN categories c ON c.id = p.category_id
SET p.name = 'Samsung Galaxy M14',
    p.description = '5G smartphone with 6000mAh battery',
    p.price = 12999.00,
    p.stock_qty = 25,
    p.low_stock_qty = 5,
    p.category_id = c.id,
    p.active = true,
    p.updated_at = NOW()
WHERE p.sku = 'SKU-MOB-001'
  AND c.cat_name = 'Electronics';

UPDATE products p
JOIN categories c ON c.id = p.category_id
SET p.name = 'Boat Rockerz 450',
    p.description = 'Wireless Bluetooth headphone',
    p.price = 1599.00,
    p.stock_qty = 40,
    p.low_stock_qty = 10,
    p.category_id = c.id,
    p.active = true,
    p.updated_at = NOW()
WHERE p.sku = 'SKU-AUD-001'
  AND c.cat_name = 'Electronics';

UPDATE products p
JOIN categories c ON c.id = p.category_id
SET p.name = 'Basmati Rice 5kg',
    p.description = 'Premium long grain basmati rice',
    p.price = 799.00,
    p.stock_qty = 60,
    p.low_stock_qty = 10,
    p.category_id = c.id,
    p.active = true,
    p.updated_at = NOW()
WHERE p.sku = 'SKU-GRC-001'
  AND c.cat_name = 'Groceries';

INSERT INTO carts (user_id, total_amount, created_at, updated_at)
SELECT u.id, 14598.00, NOW(), NOW()
FROM users u
WHERE u.email = 'rahul@example.com'
  AND NOT EXISTS (
      SELECT 1 FROM carts c WHERE c.user_id = u.id
  );

INSERT INTO carts (user_id, total_amount, created_at, updated_at)
SELECT u.id, 2398.00, NOW(), NOW()
FROM users u
WHERE u.email = 'priya@example.com'
  AND NOT EXISTS (
      SELECT 1 FROM carts c WHERE c.user_id = u.id
  );

UPDATE carts c
JOIN users u ON u.id = c.user_id
SET c.total_amount = 14598.00,
    c.updated_at = NOW()
WHERE u.email = 'rahul@example.com';

UPDATE carts c
JOIN users u ON u.id = c.user_id
SET c.total_amount = 2398.00,
    c.updated_at = NOW()
WHERE u.email = 'priya@example.com';

INSERT INTO cart_items (cart_id, product_id, quantity, sub_total, created_at, updated_at)
SELECT c.id, p.id, 1, 12999.00, NOW(), NOW()
FROM carts c
JOIN users u ON u.id = c.user_id
JOIN products p ON p.sku = 'SKU-MOB-001'
WHERE u.email = 'rahul@example.com'
  AND NOT EXISTS (
      SELECT 1 FROM cart_items ci WHERE ci.cart_id = c.id AND ci.product_id = p.id
  );

INSERT INTO cart_items (cart_id, product_id, quantity, sub_total, created_at, updated_at)
SELECT c.id, p.id, 1, 1599.00, NOW(), NOW()
FROM carts c
JOIN users u ON u.id = c.user_id
JOIN products p ON p.sku = 'SKU-AUD-001'
WHERE u.email = 'rahul@example.com'
  AND NOT EXISTS (
      SELECT 1 FROM cart_items ci WHERE ci.cart_id = c.id AND ci.product_id = p.id
  );

INSERT INTO cart_items (cart_id, product_id, quantity, sub_total, created_at, updated_at)
SELECT c.id, p.id, 1, 1599.00, NOW(), NOW()
FROM carts c
JOIN users u ON u.id = c.user_id
JOIN products p ON p.sku = 'SKU-AUD-001'
WHERE u.email = 'priya@example.com'
  AND NOT EXISTS (
      SELECT 1 FROM cart_items ci WHERE ci.cart_id = c.id AND ci.product_id = p.id
  );

INSERT INTO cart_items (cart_id, product_id, quantity, sub_total, created_at, updated_at)
SELECT c.id, p.id, 1, 799.00, NOW(), NOW()
FROM carts c
JOIN users u ON u.id = c.user_id
JOIN products p ON p.sku = 'SKU-GRC-001'
WHERE u.email = 'priya@example.com'
  AND NOT EXISTS (
      SELECT 1 FROM cart_items ci WHERE ci.cart_id = c.id AND ci.product_id = p.id
  );

UPDATE cart_items ci
JOIN carts c ON c.id = ci.cart_id
JOIN users u ON u.id = c.user_id
JOIN products p ON p.id = ci.product_id
SET ci.quantity = 1,
    ci.sub_total = 12999.00,
    ci.updated_at = NOW()
WHERE u.email = 'rahul@example.com'
  AND p.sku = 'SKU-MOB-001';

UPDATE cart_items ci
JOIN carts c ON c.id = ci.cart_id
JOIN users u ON u.id = c.user_id
JOIN products p ON p.id = ci.product_id
SET ci.quantity = 1,
    ci.sub_total = 1599.00,
    ci.updated_at = NOW()
WHERE u.email = 'rahul@example.com'
  AND p.sku = 'SKU-AUD-001';

UPDATE cart_items ci
JOIN carts c ON c.id = ci.cart_id
JOIN users u ON u.id = c.user_id
JOIN products p ON p.id = ci.product_id
SET ci.quantity = 1,
    ci.sub_total = 1599.00,
    ci.updated_at = NOW()
WHERE u.email = 'priya@example.com'
  AND p.sku = 'SKU-AUD-001';

UPDATE cart_items ci
JOIN carts c ON c.id = ci.cart_id
JOIN users u ON u.id = c.user_id
JOIN products p ON p.id = ci.product_id
SET ci.quantity = 1,
    ci.sub_total = 799.00,
    ci.updated_at = NOW()
WHERE u.email = 'priya@example.com'
  AND p.sku = 'SKU-GRC-001';
