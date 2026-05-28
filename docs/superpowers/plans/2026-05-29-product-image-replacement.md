# Product Image Replacement Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the current 12 seed product placeholder images with local generated product images that match each product name and description.

**Architecture:** Keep `data.sql` as the source of demo catalog truth. Store generated static assets under Vite public assets and reference them with stable `/images/products/*.webp` URLs from both product rows and existing order item snapshot rows.

**Tech Stack:** Spring Boot seed SQL, Vue 3/Vite public assets, built-in image generation, Python Pillow for WebP conversion/validation.

---

## File Structure

- Modify: `mall-backend/src/main/resources/sql/data.sql`
  - Replace 12 `product.image_url` values.
  - Replace 10 existing `order_item.product_image` snapshot values.
- Create: `mall-frontend/public/images/products/*.webp`
  - One WebP image per current product ID 1-12.
- Create: `docs/product-images/12-product-images.md`
  - Record product-to-image mapping and the generation prompt summary for each image.
- Read: `docs/superpowers/specs/2026-05-29-product-image-replacement-design.md`
  - Confirm implementation stays within approved scope.

## Image Prompt Template

Use this template once per product with the product name and description filled in:

```text
Use case: product-mockup
Asset type: ecommerce product catalog image for a Vue mall product card and detail page
Primary request: Create a clean realistic product photo for "<商品名>" based on this Chinese product description: "<商品描述>"
Scene/backdrop: bright clean light-gray or white studio background, subtle surface, minimal props only if they clarify the product use
Subject: the product is the clear centered subject, fully visible, realistic proportions, no cropped edges
Style/medium: polished ecommerce product photography, natural materials and textures, consistent clean catalog style
Composition/framing: square composition, product centered, generous padding, suitable for thumbnail and detail page
Lighting/mood: soft commercial lighting, bright but not overexposed, soft natural shadow under the product
Constraints: no text, no watermark, no logo, no real brand marks, no people, no hands, no packaging with readable text
Avoid: distorted product shape, duplicate products unless the item is naturally a set, busy background, dark background
```

## Product Mapping

| ID | 商品名 | 描述 | Final path |
|---:|---|---|---|
| 1 | 蓝牙降噪耳机 | 适合通勤和自习室使用的轻量降噪耳机。 | `mall-frontend/public/images/products/001-bluetooth-noise-cancelling-earbuds.webp` |
| 2 | 便携机械键盘 | 紧凑配列，适合宿舍和实验室桌面。 | `mall-frontend/public/images/products/002-portable-mechanical-keyboard.webp` |
| 3 | 智能手环 | 记录运动、睡眠和课程日程提醒。 | `mall-frontend/public/images/products/003-smart-fitness-band.webp` |
| 4 | 护眼台灯 | 三档亮度，适合夜间阅读和编程。 | `mall-frontend/public/images/products/004-eye-care-desk-lamp.webp` |
| 5 | 保温杯 | 316 不锈钢内胆，适合课堂和图书馆。 | `mall-frontend/public/images/products/005-insulated-travel-tumbler.webp` |
| 6 | 桌面收纳盒 | 分类整理文具、数据线和小物件。 | `mall-frontend/public/images/products/006-desktop-organizer-box.webp` |
| 7 | 数据库课程笔记本 | 方格内页，适合记录 E-R 图和 SQL。 | `mall-frontend/public/images/products/007-database-course-notebook.webp` |
| 8 | U 盘 64GB | 课程资料、报告和演示文件备份。 | `mall-frontend/public/images/products/008-64gb-usb-flash-drive.webp` |
| 9 | 考试资料文件夹 | 透明分页，整理试卷和课程讲义。 | `mall-frontend/public/images/products/009-exam-document-folder.webp` |
| 10 | 挂耳咖啡 | 深夜写报告时的提神补给。 | `mall-frontend/public/images/products/010-drip-bag-coffee.webp` |
| 11 | 每日坚果 | 低库存商品，用于后台库存预警展示。 | `mall-frontend/public/images/products/011-daily-mixed-nuts.webp` |
| 12 | 低糖饼干 | 课间补充能量的小包装饼干。 | `mall-frontend/public/images/products/012-low-sugar-biscuits.webp` |

## Task 1: Prepare Asset Directory And Manifest

**Files:**
- Create: `mall-frontend/public/images/products/`
- Create: `docs/product-images/12-product-images.md`

- [ ] **Step 1: Create directories**

Run:

```bash
mkdir -p mall-frontend/public/images/products docs/product-images
```

Expected: both directories exist.

- [ ] **Step 2: Create image manifest**

Create `docs/product-images/12-product-images.md` with a table containing all 12 product IDs, names, descriptions, final `/images/products/*.webp` URL paths, and prompt summaries.

- [ ] **Step 3: Check git status**

Run:

```bash
git status --short
```

Expected: new docs and directories are visible; no unrelated tracked files are modified.

## Task 2: Generate And Save 12 Product Images

**Files:**
- Create: `mall-frontend/public/images/products/*.webp`

- [ ] **Step 1: Generate one image per product**

Use the built-in image generation tool once for each product in the Product Mapping table. Each call uses the Image Prompt Template with that product's name and description.

- [ ] **Step 2: Copy generated source files into a temporary review directory**

Save the selected generated outputs under:

```text
.superpowers/product-image-sources/
```

Use filenames with the matching product ID and slug, preserving the generated source extension.

- [ ] **Step 3: Convert sources to WebP**

Run a Python Pillow conversion that writes each final image to the mapped `mall-frontend/public/images/products/*.webp` path. Use RGB output, quality 88, and keep square dimensions.

- [ ] **Step 4: Validate generated images**

Run:

```bash
python3 - <<'PY'
from pathlib import Path
from PIL import Image

paths = sorted(Path('mall-frontend/public/images/products').glob('*.webp'))
assert len(paths) == 12, f'expected 12 images, got {len(paths)}'
for path in paths:
    with Image.open(path) as image:
        assert image.format == 'WEBP', f'{path} is not WebP'
        width, height = image.size
        assert width == height, f'{path} is not square: {image.size}'
        assert width >= 512, f'{path} is too small: {image.size}'
print('image validation passed')
PY
```

Expected: `image validation passed`.

## Task 3: Replace SQL Image References

**Files:**
- Modify: `mall-backend/src/main/resources/sql/data.sql`

- [ ] **Step 1: Replace product image URLs**

In the `INSERT INTO product` rows, replace each `https://picsum.photos/...` URL with its corresponding `/images/products/*.webp` URL from the Product Mapping table.

- [ ] **Step 2: Replace order item snapshot URLs**

In the `INSERT INTO order_item` rows, replace each snapshot image URL with the matching product image URL for its `product_id`.

- [ ] **Step 3: Verify no placeholder URLs remain**

Run:

```bash
rg -n "picsum\\.photos" mall-backend/src/main/resources/sql/data.sql
```

Expected: no output and exit code 1.

## Task 4: Validate References And Frontend Build

**Files:**
- Read: `mall-backend/src/main/resources/sql/data.sql`
- Read: `mall-frontend/public/images/products/*.webp`

- [ ] **Step 1: Verify SQL references point to existing images**

Run:

```bash
python3 - <<'PY'
import re
from pathlib import Path

sql = Path('mall-backend/src/main/resources/sql/data.sql').read_text(encoding='utf-8')
urls = re.findall(r"'/images/products/([^']+\\.webp)'", sql)
assert len(urls) == 22, f'expected 22 image references, got {len(urls)}'
missing = [url for url in urls if not Path('mall-frontend/public/images/products', url).exists()]
assert not missing, f'missing image files: {missing}'
assert 'picsum.photos' not in sql
print('sql image references passed')
PY
```

Expected: `sql image references passed`.

- [ ] **Step 2: Run frontend build**

Run:

```bash
cd mall-frontend
npm run build
```

Expected: Vite build exits with code 0.

- [ ] **Step 3: Review final git diff**

Run:

```bash
git diff --stat
git status --short
```

Expected: changes are limited to `data.sql`, generated WebP files, and the two documentation files. Do not commit unless the user explicitly asks for a commit.
