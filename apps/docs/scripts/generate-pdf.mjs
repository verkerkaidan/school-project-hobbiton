import { chromium } from "playwright";
import { PDFDocument } from "pdf-lib";
import { createServer } from "http";
import { readFileSync, writeFileSync, readdirSync, statSync } from "fs";
import { join, extname, posix } from "path";
import { fileURLToPath } from "url";
import handler from "serve-handler";

const __dirname = fileURLToPath(new URL(".", import.meta.url));
const ROOT = join(__dirname, "..");
const BUILD = join(ROOT, "build");
const STATIC = join(ROOT, "static");
const DOCS = join(ROOT, "docs");
const PORT = 3099;
const BASE = `http://localhost:${PORT}`;

const HIDE_UI_CSS = `
  nav.navbar, footer.footer, .theme-doc-sidebar-container,
  .pagination-nav, .theme-doc-breadcrumbs, .theme-doc-toc-desktop,
  .theme-doc-version-badge { display: none !important; }
  article { max-width: 100% !important; padding: 0 !important; }
  .docMainContainer { max-width: 100% !important; }
  main { padding: 0 !important; }
  [data-theme="dark"] {
    --ifm-background-color: #fff;
    --ifm-font-color-base: #1c1e21;
    --ifm-heading-color: #1c1e21;
  }
  a { color: inherit !important; text-decoration: none !important; pointer-events: none !important; }
`;

function collectAutoDir(dirName) {
  const dir = join(DOCS, dirName);
  const pages = [];
  function walk(d) {
    for (const entry of readdirSync(d)) {
      const full = join(d, entry);
      if (statSync(full).isDirectory()) {
        walk(full);
      } else if (extname(entry) === ".md" || extname(entry) === ".mdx") {
        const slug = full
          .slice(DOCS.length + 1)
          .replace(/\\/g, "/")
          .replace(/\.(md|mdx)$/, "");
        pages.push(slug);
      }
    }
  }
  walk(dir);
  pages.sort((a, b) => {
    const aIsIndex = a.endsWith("/index") || a === "index";
    const bIsIndex = b.endsWith("/index") || b === "index";
    if (aIsIndex && !bIsIndex) return -1;
    if (!aIsIndex && bIsIndex) return 1;
    return a.localeCompare(b);
  });
  return pages;
}

function collectSlugs() {
  const dirs = readdirSync(DOCS)
    .filter((entry) => statSync(join(DOCS, entry)).isDirectory())
    .sort();

  const slugs = [""];
  for (const dir of dirs) {
    slugs.push(...collectAutoDir(dir));
  }
  return slugs;
}

async function main() {
  const slugs = collectSlugs();
  console.log(`Found ${slugs.length} doc pages to render`);

  const server = createServer((req, res) => handler(req, res, { public: BUILD }));
  await new Promise((resolve) => server.listen(PORT, resolve));
  console.log(`Static server running on ${BASE}`);

  const browser = await chromium.launch();
  const page = await browser.newPage();
  const pdfBuffers = [];

  for (const slug of slugs) {
    const url = `${BASE}/docs/${slug}`;
    console.log(`  Rendering: ${slug}`);
    try {
      await page.goto(url, { waitUntil: "networkidle", timeout: 30000 });
      await page.addStyleTag({ content: HIDE_UI_CSS });
      await page.waitForTimeout(1000);

      const pdf = await page.pdf({
        format: "A4",
        margin: { top: "60px", bottom: "60px", left: "40px", right: "40px" },
        printBackground: true,
        displayHeaderFooter: true,
        headerTemplate: "<span></span>",
        footerTemplate: `<div style="font-size:9px;width:100%;text-align:center;color:#666;">
          <span class="pageNumber"></span> / <span class="totalPages"></span>
        </div>`,
      });
      pdfBuffers.push(pdf);
    } catch (err) {
      console.warn(`  Warning: failed to render ${slug}: ${err.message}`);
    }
  }

  await browser.close();
  server.close();

  console.log("Merging PDFs...");
  const merged = await PDFDocument.create();
  for (const buf of pdfBuffers) {
    const doc = await PDFDocument.load(buf);
    const pages = await merged.copyPages(doc, doc.getPageIndices());
    for (const p of pages) merged.addPage(p);
  }

  const pdfBytes = await merged.save();
  const buildOutput = join(BUILD, "hobbithon-docs.pdf");
  const staticOutput = join(STATIC, "hobbithon-docs.pdf");
  writeFileSync(buildOutput, pdfBytes);
  writeFileSync(staticOutput, pdfBytes);
  console.log(`PDF saved to ${buildOutput} and ${staticOutput} (${merged.getPageCount()} pages)`);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
