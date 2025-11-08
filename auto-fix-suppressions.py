#!/usr/bin/env python3
"""
Script to automatically move method-level @SuppressWarnings to statement level.
This is a complex refactoring that requires careful analysis of each case.
"""

import os
import re
from pathlib import Path
from typing import List, Optional, Tuple

def read_file_lines(file_path: str) -> List[str]:
    """Read file and return lines."""
    with open(file_path, 'r', encoding='utf-8') as f:
        return f.readlines()

def write_file_lines(file_path: str, lines: List[str]):
    """Write lines to file."""
    with open(file_path, 'w', encoding='utf-8', newline='\n') as f:
        f.writelines(lines)

def find_suppress_warnings_line(lines: List[str], target_line: int) -> Optional[int]:
    """Find the line with @SuppressWarnings around the target line."""
    # Start from target line and look backwards
    for i in range(max(0, target_line - 10), min(len(lines), target_line + 2)):
        if '@SuppressWarnings' in lines[i]:
            return i
    return None

def get_suppression_value(line: str) -> str:
    """Extract the suppression value from @SuppressWarnings annotation."""
    match = re.search(r'@SuppressWarnings\s*\(\s*(.+?)\s*\)', line)
    if match:
        return match.group(1)
    return '""'

def is_method_signature(lines: List[str], start_idx: int) -> Tuple[bool, Optional[int]]:
    """
    Check if lines starting from start_idx represent a method signature.
    Returns (is_method, method_start_line_idx).
    """
    # Look for method signature pattern within next 5 lines
    for i in range(start_idx, min(len(lines), start_idx + 5)):
        line = lines[i].strip()
        # Skip blank lines, comments, and annotations
        if not line or line.startswith('@') or line.startswith('//') or line.startswith('/*'):
            continue
        # Method signature has '(' and typically ends with ')' or '{'
        if '(' in line and not line.endswith(';'):
            return True, i
    return False, None

def has_type_cast(line: str) -> bool:
    """Check if a line contains a type cast."""
    # Simple heuristic: look for (Type) pattern
    return bool(re.search(r'\([A-Z][A-Za-z0-9_<>?,\s\[\]]*\)\s*[a-zA-Z_]', line))

def process_file(file_path: str, line_number: int) -> bool:
    """
    Process a single file to move @SuppressWarnings from method to statement level.
    Returns True if file was modified, False otherwise.
    """
    lines = read_file_lines(file_path)
    
    # Find the @SuppressWarnings line
    suppress_idx = find_suppress_warnings_line(lines, line_number - 1)
    if suppress_idx is None:
        print(f"  ⚠️  Could not find @SuppressWarnings near line {line_number}")
        return False
    
    # Check if it's on a method
    is_method, method_idx = is_method_signature(lines, suppress_idx + 1)
    if not is_method:
        print(f"  ℹ️  Not a method-level suppression (already statement-level)")
        return False
    
    # Get the suppression value
    suppress_value = get_suppression_value(lines[suppress_idx])
    
    print(f"  📝 Moving {suppress_value} from method to statement level")
    print(f"     Method: {lines[method_idx].strip()[:60]}...")
    
    # Note: Actual transformation is complex and case-specific
    # This would require AST parsing or manual intervention for each case
    print(f"  ⚠️  Skipping auto-fix (too complex) - manual fix required")
    return False

def main():
    # Read the suppress-warnings.md file
    suppress_md_path = Path('suppress-warnings.md')
    
    if not suppress_md_path.exists():
        print("suppress-warnings.md not found!")
        return
    
    with open(suppress_md_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Pattern to match file paths and line numbers
    pattern = r'- \[ \] (.+\.java):(\d+)'
    matches = re.findall(pattern, content)
    
    print(f"Found {len(matches)} potential suppressions\n")
    
    modified_count = 0
    for file_path, line_num in matches:
        full_path = Path(file_path)
        if not full_path.exists():
            continue
        
        print(f"\nProcessing: {file_path}:{line_num}")
        if process_file(str(full_path), int(line_num)):
            modified_count += 1
    
    print(f"\n{'='*60}")
    print(f"Modified {modified_count} files")

if __name__ == '__main__':
    main()
