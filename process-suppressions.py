#!/usr/bin/env python3
"""
Script to help identify method-level @SuppressWarnings annotations.
This will search for patterns where @SuppressWarnings is on a method signature.
"""

import os
import re
from pathlib import Path

def find_method_level_suppressions(file_path, line_number):
    """Find if the @SuppressWarnings at the given line is at method level."""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
        
        # Check if line_number is valid
        if line_number > len(lines) or line_number < 1:
            return None
        
        # Line numbers are 1-indexed, but list is 0-indexed
        idx = line_number - 1
        
        # Look at the line and see if it has @SuppressWarnings
        if '@SuppressWarnings' not in lines[idx]:
            # Maybe the line number points to the code, look backwards
            for i in range(max(0, idx - 10), idx + 1):
                if '@SuppressWarnings' in lines[i]:
                    idx = i
                    break
            else:
                return None
        
        # Now check if the next few lines contain a method signature
        # Method signatures typically have: visibility modifier, return type, method name, parameters
        for i in range(idx + 1, min(len(lines), idx + 5)):
            line = lines[i].strip()
            # Skip blank lines and other annotations
            if not line or line.startswith('@') or line.startswith('//') or line.startswith('/*'):
                continue
            # Check if it looks like a method signature (has parentheses and not just a variable)
            if '(' in line and not line.endswith(';'):
                return {
                    'file': file_path,
                    'line': line_number,
                    'suppress_line': lines[idx].rstrip(),
                    'method_line': lines[i].rstrip(),
                    'context': ''.join(lines[max(0, idx-2):min(len(lines), idx+10)])
                }
        
        # If no method found, might be statement-level (already correct)
        return None
        
    except Exception as e:
        print(f"Error processing {file_path}:{line_number}: {e}")
        return None

def main():
    # Read the suppress-warnings.md file
    suppress_md_path = Path('suppress-warnings.md')
    
    if not suppress_md_path.exists():
        print("suppress-warnings.md not found!")
        return
    
    with open(suppress_md_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Pattern to match file paths and line numbers
    # Format: - [ ] path/to/file.java:123
    pattern = r'- \[ \] (.+\.java):(\d+)'
    
    matches = re.findall(pattern, content)
    
    print(f"Found {len(matches)} potential suppressions to review\n")
    
    method_level_count = 0
    statement_level_count = 0
    
    for file_path, line_num in matches:
        full_path = Path(file_path)
        line_number = int(line_num)
        
        if not full_path.exists():
            print(f"⚠️  File not found: {file_path}")
            continue
        
        result = find_method_level_suppressions(str(full_path), line_number)
        
        if result:
            method_level_count += 1
            print(f"📝 METHOD-LEVEL: {file_path}:{line_num}")
            print(f"   {result['suppress_line']}")
            print(f"   {result['method_line']}")
            print()
        else:
            statement_level_count += 1
    
    print(f"\n{'='*60}")
    print(f"Summary:")
    print(f"  Method-level (need to fix): {method_level_count}")
    print(f"  Statement-level (already OK): {statement_level_count}")
    print(f"  Total: {len(matches)}")

if __name__ == '__main__':
    main()
