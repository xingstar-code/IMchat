#!/usr/bin/env python3
"""Scan a repository for secrets and publication metadata before committing."""

from __future__ import annotations

import argparse
import re
import sys
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
    "tests",
    "__pycache__",
}
SKIP_FILES = {"LICENSE", "LICENSE.md", "LICENSE.txt", "COPYING", "NOTICE", "NOTICE.md"}
TEXT_SUFFIXES = {
    "",
    ".java",
    ".kt",
    ".xml",
    ".yml",
    ".yaml",
    ".properties",
    ".md",
    ".txt",
    ".json",
    ".toml",
    ".py",
    ".js",
    ".ts",
    ".vue",
    ".sh",
    ".ps1",
    ".cmd",
    ".bat",
}

RULES = {
    "Alibaba Cloud AccessKey ID": re.compile(r"\bLTAI[A-Za-z0-9]{12,}\b"),
    "email": re.compile(r"(?i)\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}\b"),
    "repository URL": re.compile(r"(?i)https?://(?:www\.)?(?:github|gitee)\.com/\S+"),
    "author tag": re.compile(r"(?i)^\s*(?:\*|//|#)\s*@author\b"),
    "private key": re.compile(r"-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----"),
    "likely credential": re.compile(
        r"(?i)^\s*[\w.-]*(?:password|passwd|secret|api[_-]?key|access[_-]?key|token)[\w.-]*"
        r"\s*[:=]\s*[\"']?(?!\$\{|<|change-me|your-|example|test|dummy|fake|$)"
        r"([A-Z0-9/+_!@#$%^&*.-]{8,})[\"']?\s*(?:#.*)?$"
    ),
}


def iter_text_files(root: Path):
    for path in root.rglob("*"):
        if not path.is_file():
            continue
        if path.name == "repository_audit.py":
            continue
        if any(part in SKIP_DIRS for part in path.parts):
            continue
        if path.name in SKIP_FILES or path.suffix.lower() not in TEXT_SUFFIXES:
            continue
        if path.stat().st_size > 2_000_000:
            continue
        yield path


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("root", nargs="?", default=".")
    parser.add_argument("--deny", action="append", default=[], help="Additional literal name or domain to reject.")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    findings: list[str] = []

    for path in iter_text_files(root):
        try:
            text = path.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            continue
        for line_number, line in enumerate(text.splitlines(), start=1):
            for label, pattern in RULES.items():
                if label == "likely credential" and path.suffix.lower() not in {
                    "",
                    ".env",
                    ".json",
                    ".properties",
                    ".toml",
                    ".xml",
                    ".yaml",
                    ".yml",
                }:
                    continue
                match = pattern.search(line)
                if match and not (
                    label == "email"
                    and match.group(0).lower().endswith(("@example.com", "@example.org", "@example.invalid"))
                ):
                    rel = path.relative_to(root)
                    findings.append(f"{rel}:{line_number}: {label}: {line.strip()[:180]}")
            for denied in args.deny:
                if denied and denied.lower() in line.lower():
                    rel = path.relative_to(root)
                    findings.append(f"{rel}:{line_number}: denied literal {denied!r}: {line.strip()[:180]}")

    if findings:
        print("\n".join(findings))
        print(f"\nFound {len(findings)} item(s). Review them before publishing.")
        return 1
    print("Repository audit passed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
