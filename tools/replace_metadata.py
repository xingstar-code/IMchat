#!/usr/bin/env python3
"""Preview or apply exact metadata replacements without touching license notices."""

from __future__ import annotations

import argparse
from pathlib import Path


SKIP_DIRS = {
    ".git",
    ".idea",
    ".venv",
    "venv",
    "node_modules",
    "target",
    "build",
    "dist",
    "__pycache__",
}
PROTECTED_FILES = {"LICENSE", "LICENSE.md", "LICENSE.txt", "COPYING", "NOTICE", "NOTICE.md"}


def parse_replacements(values: list[str]) -> list[tuple[str, str]]:
    replacements = []
    for value in values:
        if "=" not in value:
            raise ValueError(f"Replacement must use OLD=NEW syntax: {value!r}")
        old, new = value.split("=", 1)
        if not old:
            raise ValueError("OLD value must not be empty.")
        replacements.append((old, new))
    return replacements


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--replace", action="append", required=True, metavar="OLD=NEW")
    parser.add_argument("--apply", action="store_true", help="Write changes. Default is preview only.")
    parser.add_argument("root", nargs="?", default=".")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    replacements = parse_replacements(args.replace)
    changed: list[tuple[Path, int]] = []

    for path in root.rglob("*"):
        if not path.is_file() or path.name in PROTECTED_FILES:
            continue
        if any(part in SKIP_DIRS for part in path.parts) or path.stat().st_size > 2_000_000:
            continue
        try:
            original = path.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            continue
        updated = original
        replacements_in_file = 0
        for old, new in replacements:
            replacements_in_file += updated.count(old)
            updated = updated.replace(old, new)
        if updated != original:
            changed.append((path.relative_to(root), replacements_in_file))
            if args.apply:
                path.write_text(updated, encoding="utf-8", newline="")

    action = "Updated" if args.apply else "Would update"
    for path, count in changed:
        print(f"{action}: {path} ({count} replacement(s))")
    print(f"{action} {len(changed)} file(s).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
