#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "usage: $0 /path/to/groundwork-binary" >&2
  exit 1
fi

BINARY="$1"
if [[ ! -x "$BINARY" ]]; then
  echo "binary is not executable: $BINARY" >&2
  exit 1
fi

ROOT_DIR="$(mktemp -d "${TMPDIR:-/tmp}/groundwork-smoke.XXXXXX")"
trap 'rm -rf "$ROOT_DIR"' EXIT

export HOME="$ROOT_DIR/home"
export JAVA_TOOL_OPTIONS="-Duser.home=$ROOT_DIR/home"
export GROUNDWORK_HOME="$ROOT_DIR/home/.groundwork"

mkdir -p "$ROOT_DIR/workspace/core-agent/src"
mkdir -p "$ROOT_DIR/workspace/core-agent/.git"
mkdir -p "$ROOT_DIR/workspace/core-agent/node_modules/pkg"
mkdir -p "$ROOT_DIR/workspace/mobile-app/app"
mkdir -p "$ROOT_DIR/workspace/mobile-app/.git"
mkdir -p "$ROOT_DIR/workspace/mobile-app/build/output"
mkdir -p "$ROOT_DIR/workspace/mobile-app/ios"
mkdir -p "$ROOT_DIR/workspace/mobile-app/android"
mkdir -p "$ROOT_DIR/blueprints"
mkdir -p "$ROOT_DIR/output"

printf 'Core agent logic\n' > "$ROOT_DIR/workspace/core-agent/src/agent.txt"
printf 'dependency noise\n' > "$ROOT_DIR/workspace/core-agent/node_modules/pkg/tmp.txt"
printf '[core]\nrepositoryformatversion = 0\n' > "$ROOT_DIR/workspace/core-agent/.git/config"
printf 'Mobile UI\n' > "$ROOT_DIR/workspace/mobile-app/app/screen.tsx"
printf 'build noise\n' > "$ROOT_DIR/workspace/mobile-app/build/output/app.bin"
printf 'iOS source placeholder\n' > "$ROOT_DIR/workspace/mobile-app/ios/AppDelegate.swift"
printf 'Android source placeholder\n' > "$ROOT_DIR/workspace/mobile-app/android/MainActivity.kt"
printf '[core]\nrepositoryformatversion = 0\n' > "$ROOT_DIR/workspace/mobile-app/.git/config"
printf '# PlantSnap Blueprint\n\nSmoke test blueprint.\n' > "$ROOT_DIR/blueprints/plantsnap.md"

"$BINARY" init >/dev/null
"$BINARY" template create plantsnap \
  --description "Plant app archetype" \
  --repo "$ROOT_DIR/workspace/core-agent::Core reasoning patterns" \
  --repo "$ROOT_DIR/workspace/mobile-app::Mobile UI patterns" \
  --claude-template python-fastapi \
  --gitignore-profile general \
  --blueprint "$ROOT_DIR/blueprints/plantsnap.md" \
  --dir backend/ \
  --dir mobile/ \
  --force >/dev/null

"$BINARY" list | grep -q "plantsnap"
"$BINARY" validate plantsnap | grep -q "Template is valid: plantsnap"
"$BINARY" new PlantSnap \
  --template plantsnap \
  --output-dir "$ROOT_DIR/output" \
  --description "AI plant species identifier" >/dev/null

test -f "$ROOT_DIR/output/PlantSnap/CLAUDE.md"
test -f "$ROOT_DIR/output/PlantSnap/README.md"
test -f "$ROOT_DIR/output/PlantSnap/.gitignore"
test -f "$ROOT_DIR/output/PlantSnap/docs/plantsnap.md"
test -f "$ROOT_DIR/output/PlantSnap/_reference/core-agent/src/agent.txt"
test -f "$ROOT_DIR/output/PlantSnap/_reference/mobile-app/app/screen.tsx"
test -f "$ROOT_DIR/output/PlantSnap/_reference/mobile-app/ios/AppDelegate.swift"
test -f "$ROOT_DIR/output/PlantSnap/_reference/mobile-app/android/MainActivity.kt"
test ! -e "$ROOT_DIR/output/PlantSnap/_reference/core-agent/.git"
test ! -e "$ROOT_DIR/output/PlantSnap/_reference/core-agent/node_modules"
test ! -e "$ROOT_DIR/output/PlantSnap/_reference/mobile-app/build"

"$BINARY" clean "$ROOT_DIR/output/PlantSnap" >/dev/null
test ! -e "$ROOT_DIR/output/PlantSnap/_reference"

"$BINARY" template discover discovered-stack \
  --from "$ROOT_DIR/workspace" \
  --description "Discovered workspace template" \
  --dir backend/ \
  --dir mobile/ >/dev/null

"$BINARY" validate discovered-stack | grep -q "Template is valid: discovered-stack"
"$BINARY" new PlantSnapDiscovered \
  --template discovered-stack \
  --output-dir "$ROOT_DIR/output" \
  --description "Discovered smoke project" >/dev/null

test -f "$ROOT_DIR/output/PlantSnapDiscovered/CLAUDE.md"
test -f "$ROOT_DIR/output/PlantSnapDiscovered/README.md"
test -f "$ROOT_DIR/output/PlantSnapDiscovered/_reference/core-agent/src/agent.txt"
test -f "$ROOT_DIR/output/PlantSnapDiscovered/_reference/mobile-app/app/screen.tsx"

echo "Smoke test passed with binary: $BINARY"
