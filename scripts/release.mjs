#!/usr/bin/env node
import { createInterface } from "readline/promises";
import { readFileSync, writeFileSync } from "fs";
import { resolve, dirname } from "path";
import { fileURLToPath } from "url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const CHANGELOG = resolve(__dirname, "../CHANGELOG.md");
const PKG = resolve(__dirname, "../package.json");

// --- Parse current version from CHANGELOG.md ---
const content = readFileSync(CHANGELOG, "utf8");
const versionMatch = content.match(/^## \[(\d+)\.(\d+)\.(\d+)\]/m);

if (!versionMatch) {
  console.error("Could not find an existing version in CHANGELOG.md.");
  process.exit(1);
}

let [, major, minor, patch] = versionMatch.map(Number);
const currentVersion = `${major}.${minor}.${patch}`;

// --- Check there is something to release ---
const unreleasedHeader = "## [Unreleased]";
const unreleasedIndex = content.indexOf(unreleasedHeader);
const nextSectionIndex = content.indexOf(
  "\n## [",
  unreleasedIndex + unreleasedHeader.length,
);
const unreleasedBlock = content
  .slice(
    unreleasedIndex + unreleasedHeader.length,
    nextSectionIndex === -1 ? undefined : nextSectionIndex,
  )
  .trim();

if (!unreleasedBlock) {
  console.error(
    "Nothing in [Unreleased] to release. Add changelog entries first with: npm run changelog",
  );
  process.exit(1);
}

// --- Ask bump type ---
const rl = createInterface({ input: process.stdin, output: process.stdout });

console.log(`\n=== Create Release ===\n`);
console.log(`Current version: ${currentVersion}\n`);
console.log("Release type:");
console.log("   1. patch  – backwards-compatible bug fixes      (x.x.+1)");
console.log("   2. minor  – new backwards-compatible features   (x.+1.0)");
console.log("   3. major  – breaking changes                    (+1.0.0)");

const answer = await rl.question("\nEnter number [1-3]: ");
rl.close();

const choice = parseInt(answer.trim(), 10);
if (choice < 1 || choice > 3) {
  console.error("Invalid selection. Exiting.");
  process.exit(1);
}

if (choice === 1) patch++;
else if (choice === 2) {
  minor++;
  patch = 0;
} else if (choice === 3) {
  major++;
  minor = 0;
  patch = 0;
}

const newVersion = `${major}.${minor}.${patch}`;
const today = new Date().toISOString().slice(0, 10);

// --- Update CHANGELOG.md: move [Unreleased] content into new version ---
const updated = content.replace(
  unreleasedHeader,
  `${unreleasedHeader}\n\n## [${newVersion}] - ${today}`,
);

writeFileSync(CHANGELOG, updated, "utf8");

// --- Update package.json version ---
const pkg = JSON.parse(readFileSync(PKG, "utf8"));
pkg.version = newVersion;
writeFileSync(PKG, JSON.stringify(pkg, null, 2) + "\n", "utf8");

console.log(`\nReleased ${currentVersion} → ${newVersion}`);
console.log(`  Updated CHANGELOG.md and package.json\n`);
console.log(`Next steps:`);
console.log(`  git add CHANGELOG.md package.json`);
console.log(`  git commit -m "chore: release ${newVersion}"`);
console.log(`  git tag v${newVersion}\n`);
