#!/usr/bin/env python3
"""Extract the final assistant message from review CLI JSONL output."""

from __future__ import annotations

import argparse
import json
from pathlib import Path


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", required=True)
    parser.add_argument("--output", required=True)
    return parser.parse_args()


def extract_final_message(path: Path) -> str:
    if not path.exists():
        raise ValueError(f"Review output file is missing: {path}")

    final_message: str | None = None

    for line_number, raw_line in enumerate(path.read_text(encoding="utf-8").splitlines(), start=1):
        line = raw_line.strip()
        if not line:
            continue
        try:
            event = json.loads(line)
        except json.JSONDecodeError as exc:
            raise ValueError(f"Invalid JSONL from review output on line {line_number}: {exc}") from exc

        if event.get("type") != "assistant.message":
            continue

        data = event.get("data")
        if not isinstance(data, dict):
            continue

        content = data.get("content")
        if not isinstance(content, str):
            continue

        final_message = content

    if final_message is None:
        raise ValueError(
            "Review output did not contain an assistant.message event. "
            "The agent may not have produced a final response."
        )

    if not final_message.strip():
        raise ValueError("Final assistant message was empty")

    return final_message.strip()


def main() -> None:
    args = parse_args()
    report = extract_final_message(Path(args.input))
    Path(args.output).write_text(report + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()