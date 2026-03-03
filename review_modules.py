#!/usr/bin/env python3
"""
Iterates through instrumentation modules and runs copilot code-review-and-fix
on each one. If fixes are applied (new commit created), cherry-picks to a clean
branch, pushes it, and switches back.

Usage:
    python review_modules.py [--continue-from MODULE_SHORT_NAME]
"""

import argparse
import os
import re
import shutil
import subprocess
import sys
import time
from datetime import datetime, timezone
from urllib.parse import quote


def _github_owner_repo(remote: str) -> tuple[str, str]:
    """Extract (owner, repo) from a GitHub remote URL."""
    r = subprocess.run(
        ["git", "remote", "get-url", remote],
        capture_output=True, text=True, check=True,
    )
    url = r.stdout.strip().removesuffix(".git")
    # handles https://github.com/owner/repo and git@github.com:owner/repo
    parts = url.replace(":", "/").split("/")
    return parts[-2], parts[-1]

# The branch where this script and the agent definition live.
WORK_BRANCH = "code-review-agent-plus-module-by-module-review-script"

# Models to run, in order.
MODELS = ["gpt-5.3-codex", "claude-sonnet-4.6"]

# Output markdown log file (in logs/ which is gitignored).
LOG_FILE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "logs", "review_log.md")


def load_modules() -> list[str]:
    """Parse settings.gradle.kts for instrumentation leaf modules.

    Extracts include(":instrumentation:...") entries and converts colon-separated
    Gradle paths to slash-separated directory paths.
    E.g. ':instrumentation:activej-http-6.0:javaagent' -> 'instrumentation/activej-http-6.0/javaagent'
    """
    settings_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "settings.gradle.kts")
    pattern = re.compile(r'include\(":instrumentation:([^"]+)"\)')
    modules = []
    with open(settings_path, encoding="utf-8") as f:
        for line in f:
            m = pattern.search(line)
            if m:
                modules.append("instrumentation/" + m.group(1).replace(":", "/"))
    modules.sort()
    return modules


def short_name(module_dir: str) -> str:
    """Second-to-last and last path components joined with '-', used for branch naming.

    E.g. 'instrumentation/activej-http-6.0/javaagent' -> 'activej-http-6.0-javaagent'
    """
    parts = module_dir.rstrip("/").split("/")
    return f"{parts[-2]}-{parts[-1]}"


def shell_join(cmd: list[str]) -> str:
    """Join command list into a shell-like display string, quoting args with spaces."""
    parts = []
    for arg in cmd:
        if " " in arg:
            parts.append(f'"{arg}"')
        else:
            parts.append(arg)
    return " ".join(parts)


def run(cmd: list[str], **kwargs) -> subprocess.CompletedProcess:
    """Run a command, printing it first."""
    print(f"  $ {shell_join(cmd)}", flush=True)
    return subprocess.run(cmd, **kwargs)


def git(*args: str, check: bool = True, capture: bool = False) -> subprocess.CompletedProcess:
    cmd = ["git"] + list(args)
    return run(
        cmd,
        check=check,
        capture_output=capture,
        text=True if capture else None,
    )


def current_commit_sha() -> str:
    r = git("rev-parse", "HEAD", capture=True)
    return r.stdout.strip()


def ensure_on_work_branch():
    r = git("branch", "--show-current", capture=True)
    branch = r.stdout.strip()
    if branch != WORK_BRANCH:
        print(f"Switching to {WORK_BRANCH} from {branch}")
        git("checkout", WORK_BRANCH)


def append_log(text: str):
    with open(LOG_FILE, "a", encoding="utf-8") as f:
        f.write(text)


def extract_summary(full_output: str) -> str:
    """Extract the Fix Review Summary section and usage stats from copilot output."""
    lines = full_output.splitlines(keepends=True)
    summary_start = None
    usage_start = None

    for idx, line in enumerate(lines):
        if summary_start is None and (
            line.startswith("## Fix Review Summary")
            or "No fix-review issues found" in line
        ):
            summary_start = idx
        if line.startswith("Total usage est:"):
            usage_start = idx

    parts = []
    if summary_start is not None:
        end = usage_start if usage_start is not None else len(lines)
        parts.append("".join(lines[summary_start:end]).rstrip())
    if usage_start is not None:
        parts.append("".join(lines[usage_start:]).rstrip())

    return "\n\n".join(parts) if parts else "(no summary found)"


def init_log():
    os.makedirs(os.path.dirname(LOG_FILE), exist_ok=True)
    if not os.path.exists(LOG_FILE):
        append_log(f"# Module-by-Module Code Review Log\n\n")
        append_log(f"Started: {datetime.now(timezone.utc).isoformat()}\n\n")


def run_copilot_review(module_dir: str, model: str, index: int, total: int) -> tuple[str | None, str]:
    """Run copilot review for a module with a given model.

    Returns (commit_sha_or_None, summary_text).
    The caller must be on WORK_BRANCH. If fixes are applied, a new commit is
    created on WORK_BRANCH; the returned SHA is that commit.
    """
    sha_before = current_commit_sha()

    copilot_exe = shutil.which("copilot")
    if copilot_exe is None:
        print("FATAL: 'copilot' CLI not found on PATH.")
        sys.exit(1)
    copilot_cmd = [
        copilot_exe,
        "--allow-all-tools",
        "--allow-all-urls",
        "--agent", "code-review-and-fix",
        "--model", model,
        "--prompt", f"review all files under {module_dir}",
    ]
    proc = subprocess.Popen(
        copilot_cmd,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        bufsize=1,
        encoding="utf-8",
        errors="replace",
    )
    print(f"  $ {shell_join(copilot_cmd)}", flush=True)
    output_lines = []
    for line in proc.stdout:
        sys.stdout.write(line)
        sys.stdout.flush()
        output_lines.append(line)
    proc.wait()
    full_output = "".join(output_lines)

    if proc.returncode != 0:
        print(f"  WARNING: copilot ({model}) exited with code {proc.returncode}")

    sha_after = current_commit_sha()
    has_fixes = sha_after != sha_before
    summary = extract_summary(full_output)

    # Log results for this model run
    append_log(f"### {model}\n\n")
    append_log(f"- **Fixes applied**: {'Yes' if has_fixes else 'No'}\n")
    append_log(f"\n{summary}\n\n")

    if has_fixes:
        return sha_after, summary
    return None, summary


def main():
    parser = argparse.ArgumentParser(description="Run copilot code review on each instrumentation module.")
    parser.add_argument(
        "--continue-from",
        metavar="SHORT_NAME",
        help="Short name of the module to continue from (skip all modules before it).",
    )
    args = parser.parse_args()

    modules = load_modules()
    print(f"Loaded {len(modules)} modules from settings.gradle.kts")

    # Sanity check: verify all short names are unique
    all_short_names = [short_name(m) for m in modules]
    seen = {}
    for m, s in zip(modules, all_short_names):
        if s in seen:
            print(f"FATAL: Duplicate short name '{s}': {seen[s]} and {m}")
            sys.exit(1)
        seen[s] = m

    # Validate --continue-from
    start_index = 0
    if args.continue_from:
        if args.continue_from not in all_short_names:
            print(f"Error: '{args.continue_from}' is not a valid module short name.")
            print(f"Valid short names: {', '.join(all_short_names[:10])}...")
            sys.exit(1)
        start_index = all_short_names.index(args.continue_from)
        print(f"Continuing from module '{args.continue_from}' (index {start_index})")

    os.chdir(os.path.dirname(os.path.abspath(__file__)))

    ensure_on_work_branch()
    init_log()

    total = len(modules)
    for i, module_dir in enumerate(modules[start_index:], start=start_index):
        sname = short_name(module_dir)
        branch_name = f"code-review-{sname}"
        print(f"\n{'='*72}")
        print(f"[{i+1}/{total}] Reviewing: {module_dir}  (short: {sname})")
        print(f"{'='*72}\n")

        ensure_on_work_branch()
        base_sha = current_commit_sha()

        # Log module header
        append_log(f"---\n\n## {module_dir}\n\n")
        append_log(f"- **Index**: {i+1}/{total}\n")
        append_log(f"- **Time**: {datetime.now(timezone.utc).isoformat()}\n")

        # Run each model in sequence, stacking on top of each other
        any_fixes = False
        commit_bodies = []
        for model in MODELS:
            print(f"\n  --- Running {model} ---\n")
            sha, summary = run_copilot_review(module_dir, model, i, total)
            if sha:
                any_fixes = True
                # Capture the commit body (bullet points) from this model's commit
                body = git("log", "-1", "--format=%b", sha, capture=True).stdout.strip()
                if body:
                    commit_bodies.append(body)

        if not any_fixes:
            print("  No fixes applied by any model.")
            continue

        # Squash all commits since base into one, preserving bullet points
        git("reset", "--soft", base_sha)
        subject = f"Review fixes for {sname.replace('-', ' ', 1)}"
        body = "\n".join(commit_bodies)
        commit_args = ["-m", subject]
        if body:
            commit_args += ["-m", body]
        git("commit", *commit_args, check=False)
        squashed_sha = current_commit_sha()

        print(f"\n  Fixes squashed into {squashed_sha[:8]}. "
              f"Cherry-picking to {branch_name}...")

        # Stash any modified/untracked files before switching branches
        stash_before = git("stash", "list", capture=True).stdout
        git("stash", "push", "--include-untracked", "-m", f"review-stash-{sname}")
        stash_after = git("stash", "list", capture=True).stdout
        did_stash = stash_before != stash_after

        # Create a clean branch from main and cherry-pick the squashed commit
        git("checkout", "main")
        git("branch", "-D", branch_name, check=False)
        git("checkout", "-b", branch_name)

        cp = git("cherry-pick", squashed_sha, check=False, capture=True)
        if cp.returncode != 0:
            print(f"  WARNING: Cherry-pick failed!"
                  f"\n{cp.stdout}\n{cp.stderr}")
            append_log(f"- **Cherry-pick**: FAILED\n")
            git("cherry-pick", "--abort", check=False)
            git("checkout", WORK_BRANCH)
            if did_stash:
                git("stash", "pop")
            git("branch", "-D", branch_name, check=False)
            continue

        # Push the branch
        push_ok = False
        for attempt in range(3):
            push_result = git("push", "--force", "origin", branch_name,
                              check=False, capture=True)
            if push_result.returncode == 0:
                push_ok = True
                break
            print(f"  Push attempt {attempt + 1}/3 failed: "
                  f"{push_result.stderr.strip()}")
            if attempt < 2:
                time.sleep(5)
        if push_ok:
            print(f"  Pushed {branch_name} to origin.")
            origin_owner, _ = _github_owner_repo("origin")
            upstream_owner, upstream_repo = _github_owner_repo("upstream")
            pr_url = (f"https://github.com/{upstream_owner}/{upstream_repo}"
                      f"/compare/main...{origin_owner}:{branch_name}?expand=1")
            append_log(f"- **Push**: OK\n")
            append_log(f"- **Create PR**: [{pr_url}]({pr_url})\n")
        else:
            print(f"  WARNING: Push failed after 3 attempts! "
                  f"Output:\n{push_result.stdout}\n{push_result.stderr}")
            append_log(f"- **Push**: FAILED - {push_result.stderr.strip()}\n")

        # Switch back to work branch and restore stashed files
        git("checkout", WORK_BRANCH)
        if did_stash:
            git("stash", "pop")

    print(f"\n{'='*72}")
    print(f"All done! Review log written to {LOG_FILE}")
    print(f"{'='*72}")


if __name__ == "__main__":
    main()
