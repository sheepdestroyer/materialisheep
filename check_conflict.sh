#!/bin/bash
echo "=== GIT STATUS ===" > conflict_status.txt
git status >> conflict_status.txt 2>&1
echo "=== GITIGNORE ===" >> conflict_status.txt
cat .gitignore >> conflict_status.txt
