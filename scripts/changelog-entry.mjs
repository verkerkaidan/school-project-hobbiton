#!/usr/bin/env node
import { createInterface } from "readline/promises";
import { readFileSync, writeFileSync } from "fs";
import { resolve, dirname } from "path";
import { fileURLToPath } from "url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const CHANGELOG = resolve(__dirname, "../CHANGELOG.md");
const PKG = resolve(__dirname, "../package.json");
const PKG_LOCK = resolve(__dirname, "../package-lock.json");

// Conventional commit type → Keep a Changelog section
const TYPES = [
  { label: "feat", description: "new feature", section: "Added" },
  { label: "fix", description: "bug fix", section: "Fixed" },
  { label: "hotfix", description: "critical production fix", section: "Fixed" },
  {
    label: "refactor",
    description: "code change, no new feat",
    section: "Changed",
  },
  {
    label: "remove",
    description: "removal of functionality",
    section: "Removed",
  },
  { label: "security", description: "security patch", section: "Security" },
  { label: "docs", description: "documentation update", section: "Changed" },
  { label: "chore", description: "maintenance / tooling", section: "Changed" },
];

const rl = createInterface({ input: process.stdin, output: process.stdout });

// ── Step 1: changelog entry ──────────────────────────────────────────────────
console.log("\n=== Changelog Entry ===\n");

// Pick change type
console.log("Change type:");
TYPES.forEach((t, i) =>
  console.log(
    `  ${String(i + 1).padStart(2)}. ${t.label.padEnd(10)} – ${t.description}`,
  ),
);
const typeAnswer = await rl.question(`\nEnter number [1-${TYPES.length}]: `);
const typeIndex = parseInt(typeAnswer.trim(), 10) - 1;

if (typeIndex < 0 || typeIndex >= TYPES.length) {
  console.error("Invalid selection. Exiting.");
  rl.close();
  process.exit(1);
}

const chosen = TYPES[typeIndex];

// Get description
const description = await rl.question(`\nDescribe the ${chosen.label}: `);

if (!description.trim()) {
  console.error("Description cannot be empty. Exiting.");
  rl.close();
  process.exit(1);
}

const changeType = chosen.section;

// Insert into CHANGELOG.md
const content = readFileSync(CHANGELOG, "utf8").replace(/\r\n/g, "\n");
const unreleasedHeader = "## [Unreleased]";
const sectionHeader = `### ${changeType}`;
const entry = `- **${chosen.label}**: ${description.trim()}`;

if (!content.includes(unreleasedHeader)) {
  console.error(`Could not find "${unreleasedHeader}" in CHANGELOG.md.`);
  rl.close();
  process.exit(1);
}

let updated;

if (content.includes(`${unreleasedHeader}\n\n${sectionHeader}`)) {
  // Section already exists directly under [Unreleased] — append to it
  updated = content.replace(
    `${unreleasedHeader}\n\n${sectionHeader}\n`,
    `${unreleasedHeader}\n\n${sectionHeader}\n${entry}\n`,
  );
} else if (content.includes(`${unreleasedHeader}\n\n### `)) {
  // Other sections exist — insert new section before the first one
  updated = content.replace(
    `${unreleasedHeader}\n\n### `,
    `${unreleasedHeader}\n\n${sectionHeader}\n${entry}\n\n### `,
  );
} else {
  // No sections yet under [Unreleased]
  updated = content.replace(
    `${unreleasedHeader}\n`,
    `${unreleasedHeader}\n\n${sectionHeader}\n${entry}\n`,
  );
}

writeFileSync(CHANGELOG, updated, "utf8");
console.log(
  `\nAdded to CHANGELOG.md under [Unreleased] > ${changeType} (${chosen.label}):`,
);
console.log(`  ${entry}\n`);

// ── Step 2: release (optional) ───────────────────────────────────────────────
const doRelease = await rl.question("Create a release now? [y/N]: ");

if (doRelease.trim().toLowerCase() !== "y") {
  rl.close();
  console.log("Done. Run `npm run changelog` again when ready to release.\n");
  process.exit(0);
}

const versionMatch = readFileSync(CHANGELOG, "utf8")
  .replace(/\r\n/g, "\n")
  .match(/^## \[(\d+)\.(\d+)\.(\d+)\]/m);
if (!versionMatch) {
  console.error("Could not find an existing version in CHANGELOG.md.");
  rl.close();
  process.exit(1);
}

let [, major, minor, patch] = versionMatch.map(Number);
const currentVersion = `${major}.${minor}.${patch}`;

console.log(`\nCurrent version: ${currentVersion}\n`);
console.log("Release type:");
console.log("   1. patch  – backwards-compatible bug fixes      (x.x.+1)");
console.log("   2. minor  – new backwards-compatible features   (x.+1.0)");
console.log("   3. major  – breaking changes                    (+1.0.0)");

const releaseAnswer = await rl.question("\nEnter number [1-3]: ");
rl.close();

const choice = parseInt(releaseAnswer.trim(), 10);
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

const afterEntry = readFileSync(CHANGELOG, "utf8");
const released = afterEntry.replace(
  unreleasedHeader,
  `${unreleasedHeader}\n\n## [${newVersion}] - ${today}`,
);
writeFileSync(CHANGELOG, released, "utf8");

const pkg = JSON.parse(readFileSync(PKG, "utf8"));
pkg.version = newVersion;
writeFileSync(PKG, JSON.stringify(pkg, null, 2) + "\n", "utf8");

const lock = JSON.parse(readFileSync(PKG_LOCK, "utf8"));
lock.version = newVersion;
if (lock.packages?.[""]) lock.packages[""].version = newVersion;
writeFileSync(PKG_LOCK, JSON.stringify(lock, null, 2) + "\n", "utf8");

console.log(`\nReleased ${currentVersion} → ${newVersion}`);
console.log("Updated CHANGELOG.md and package.json\n");
console.log("Next steps:");
console.log(`  git add CHANGELOG.md package.json package-lock.json`);
console.log(`  git commit -m "chore: release ${newVersion}"`);
console.log(`  git tag v${newVersion}\n`);
