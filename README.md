# LinuxCLI — Linux Command Line Simulator

## Overview
**LinuxCLI** is a Java-based simulation of a Linux command line interface demonstrating how real command-line interpreters parse user input, manipulate the file system, and handle output redirection.

The project is built around three main classes:
1. **Parser** – Parses user input and extracts commands with their arguments.  
2. **Terminal** – Implements all command functionalities and manages file operations.  
3. **Main** – The entry point of the program that initializes and runs the CLI session.

It replicates the behavior of core Linux shell commands 
| Command | Description |
|----------|-------------|
| `pwd` | Displays the current working directory. |
| `cd` | Changes the current directory. Supports `..`, relative, and absolute paths. |
| `ls` | Lists directory contents sorted alphabetically. |
| `mkdir` | Creates one or more directories (absolute or relative paths). |
| `rmdir` | Removes empty directories or all empty directories in the current folder using `rmdir *`. |
| `touch` | Creates a new file (absolute or relative path). |
| `rm` | Deletes a single file. |
| `cp` | Copies a file to another file. |
| `cp -r` | Recursively copies directories and their contents. |
| `cat` | Displays the contents of one file, or concatenates two files. |
| `wc` | Counts the number of lines, words, and characters in a file. |
| `echo` | Prints text or messages to the console (supports output redirection with `>` or `>>`). |
| `>` | Redirects output to a file (creates or overwrites). |
| `>>` | Appends output to a file if it exists. |
| `zip` | Compresses one or more files or directories into a ZIP archive. Supports `-r` for recursive compression. |
| `unzip` | Extracts ZIP archives into the current or specified directory using `-d`. |
| `exit` | Safely closes the CLI session. |
Error handling ensures invalid commands or parameters do not crash the program.

Together, they create a compact, fully functional shell-like environment that mimics the structure and flow of real Linux terminals.


## Project Structure
```
src/
│
├── Parser.java      # Parses and tokenizes user input
├── Terminal.java    # Implements all command logic
└── Main.java        # Runs the main CLI loop
```
## 3. Example Session
```bash
USER@CLI:~$ mkdir folder1
USER@CLI:~$ cd folder1
USER@CLI:~/folder1$ touch fileA.txt
USER@CLI:~/folder1$ echo Hello World > fileA.txt
USER@CLI:~/folder1$ echo This is a second line >> fileA.txt
USER@CLI:~/folder1$ cat fileA.txt
Hello World
This is a second line
USER@CLI:~/folder1$ wc fileA.txt
2 7 32 fileA.txt
USER@CLI:~/folder1$ cd ..
USER@CLI:~$ zip -r archive.zip folder1
USER@CLI:~$ unzip archive.zip -d extracted
USER@CLI:~$ exit
CLI session closed by user
```

## License
This project is licensed under the **MIT License**.



---
