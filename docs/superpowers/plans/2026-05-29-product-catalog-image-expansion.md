# Product Catalog Image Expansion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Expand the mall seed catalog to 150 daily-life products across 10 categories, with generated local product images and 30-product review checkpoints.

**Architecture:** The first implementation surface is seed data and static frontend assets. `data.sql` remains the source of demo catalog truth, while product images live under Vite public assets and are referenced through stable `/images/products/*.webp` URLs.

**Tech Stack:** Spring Boot 3 seed SQL, Vue 3/Vite public assets, WebP images, Git batch commits.

---

## File Structure

- Modify `mall-backend/src/main/resources/sql/data.sql`: replace category seed rows, expand product seed rows, and update order item snapshot image URLs.
- Create `mall-frontend/public/images/products/`: final generated WebP product images.
- Create `docs/product-catalog/`: batch review manifests that list product IDs, names, categories, descriptions, and image paths.
- Modify `mall-backend/api-test.http` only if demo product IDs or product names become misleading.
- Modify `README.md` only if the visible product count or category descriptions become stale.

## Task 1: Batch 1 Catalog Data

**Files:**
- Modify: `mall-backend/src/main/resources/sql/data.sql`
- Create: `docs/product-catalog/batch-01-products.md`

- [ ] **Step 1: Define the 10 categories and first 30 products**

Use 10 categories in `data.sql`:

```sql
INSERT INTO category (id, name, sort_order, status) VALUES
(1, '数码家电', 1, 1),
(2, '家居日用', 2, 1),
(3, '食品饮料', 3, 1),
(4, '个护清洁', 4, 1),
(5, '美妆护肤', 5, 1),
(6, '服饰鞋包', 6, 1),
(7, '母婴宠物', 7, 1),
(8, '运动户外', 8, 1),
(9, '图书文具', 9, 1),
(10, '厨房餐厨', 10, 1);
```

Create 30 product rows for categories 1 and 2. Each product must have a local image URL in this format:

```text
/images/products/001-wireless-noise-cancelling-earbuds.webp
```

- [ ] **Step 2: Preserve demo references**

Keep cart and order references valid by ensuring products with IDs `1`, `2`, `4`, `5`, `7`, `9`, `10`, and `11` still exist. Update `order_item.product_name`, `product_price`, and `product_image` when the referenced product meaning changes.

- [ ] **Step 3: Write the batch review manifest**

Create `docs/product-catalog/batch-01-products.md` with a table:

```markdown
| ID | 品类 | 商品名 | 图片路径 | 描述摘要 |
|---:|---|---|---|---|
| 1 | 数码家电 | 无线降噪蓝牙耳机 | /images/products/001-wireless-noise-cancelling-earbuds.webp | 主动降噪、通勤和办公使用 |
```

## Task 2: Batch 1 Images

**Files:**
- Create: `mall-frontend/public/images/products/*.webp`
- Modify: `docs/product-catalog/batch-01-products.md`

- [ ] **Step 1: Generate one image per product**

For each of the 30 batch products, generate a square product image using this prompt structure:

```text
Use case: product-mockup
Asset type: ecommerce product catalog image
Primary request: Create a realistic product image for "<商品名>" based on this Chinese product description: "<商品描述>"
Scene/backdrop: subtle daily-life setting that matches the product category, clean and uncluttered
Subject: the product is the clear centered subject, fully visible, realistic proportions
Style/medium: polished ecommerce product photography, natural materials and textures
Composition/framing: square composition, product centered, generous padding, suitable for thumbnail and detail page
Lighting/mood: soft commercial lighting, bright but not overexposed
Constraints: no text, no watermark, no logo, no real brand marks, no people, no hands, no packaging with readable text
Avoid: distorted product shape, duplicate products unless the item is naturally a set, busy background
```

- [ ] **Step 2: Save final images**

Save accepted images as WebP files under:

```text
mall-frontend/public/images/products/
```

Use filenames that match `data.sql`.

## Task 3: Batch 1 Validation

**Files:**
- Read: `mall-backend/src/main/resources/sql/data.sql`
- Read: `mall-frontend/public/images/products/`

- [ ] **Step 1: Run seed data checks**

Run a local script or shell checks to verify:

```text
category row count >= 10
processed product row count = 30
all processed product image_url values start with /images/products/
all processed image files exist
no processed product or order snapshot image uses picsum.photos
```

- [ ] **Step 2: Run frontend build**

Run:

```bash
cd mall-frontend
npm run build
```

Expected: Vite build exits with code 0.

- [ ] **Step 3: Commit batch 1 and stop**

Commit only batch 1 data, images, and review manifest:

```bash
git add mall-backend/src/main/resources/sql/data.sql mall-frontend/public/images/products docs/product-catalog/batch-01-products.md
git commit -m "feat: expand catalog batch 1 products"
```

After this commit, update the browser review page and stop for user review.

## Tasks 4-7: Remaining Batches

Repeat the same pattern for each 30-product batch:

- Batch 2: 食品饮料、个护清洁.
- Batch 3: 美妆护肤、服饰鞋包.
- Batch 4: 母婴宠物、运动户外.
- Batch 5: 图书文具、厨房餐厨.

Each batch must create 30 images, update `data.sql`, write `docs/product-catalog/batch-0N-products.md`, run validation, commit once, update the preview page, and stop for review.

## Final Validation

After batch 5:

- Verify exactly 150 products in `data.sql`.
- Verify exactly 10 categories in `data.sql`.
- Verify each category has 10 to 20 products.
- Verify every `product.image_url` uses `/images/products/*.webp`.
- Verify every referenced product image file exists.
- Verify `picsum.photos` no longer appears in product or order item image URLs.
- Run `npm run build` from `mall-frontend`.
