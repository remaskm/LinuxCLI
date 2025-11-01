package com.remas.cli;
import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * Terminal class represents CLI that mimics Linux shell commands.
 * Each command is implemented as a separate method that  manages current directory, interpreting commands, and executes file operations
 */
public class Terminal {
    Parser parser;
    String currentDirectory; //tracks the user's current working directory

    /**
     * Initializes the terminal by setting the starting directory
     * to the current working directory of the program.
     */
    public Terminal() {
        // using user.dir ensures the CLI starts wherever the program is run
        currentDirectory = System.getProperty("user.dir");
    }

    /**
     * @return The absolute path of the current directory.
     */
    public String pwd() {
        return currentDirectory;
    }

    /**
     * Changes the current working directory supporting `cd` `cd ..` `cd <path>`
     * @param args The path arguments provided by the user.
     */
    public void cd(String[] args) {
        //case 'cd': no argument → goes to the user's home directory
        if (args.length == 0) {
            currentDirectory = System.getProperty("user.home");
            return;
        }
        String path = args[0];

        //case 'cd ..' → moves one level up to the parent directory
        if (path.equals("..")) {
            File currentDir = new File(pwd());
            String parentPath = currentDir.getParent();

            //parent path null if already at root
            if (parentPath != null) {
                currentDirectory = parentPath;
            } else {
                System.out.println("Error: Already at root directory");
            }
            return;
        }

        //case 'cd <path>' → changes to a specific directory (absolute or relative)
        File newDir;
        if (new File(path).isAbsolute()) {
            //no need to attach current directory
            newDir = new File(path);
        } else {
            //build a full path from the current directory
            newDir = new File(pwd() + File.separator + path);
        }

        //change directory if it actually exists and is valid
        if (newDir.exists() && newDir.isDirectory()) {
            try {
                //resolves "..", ".", and symbolic links safely
                currentDirectory = newDir.getCanonicalPath();
            } catch (IOException e) {
                System.out.println("Error: Cannot change to directory");
            }
        } else {
            System.out.println("Error: Directory does not exist");
        }
    }

    /**
     * Lists all files and subdirectories alphabetically in the current directory.
     */
    public void ls() {
        //use the current working directory as the target
        File dir = new File(pwd());
        String[] filesList = dir.list();

        //list() returns null if directory couldn’t be read (permissions or corruption)
        if (filesList == null) {
            System.out.println("Error: Cannot read directory");
            return;
        }

        //sort for consistent and predictable output order
        Arrays.sort(filesList);

        //print each file and subdirectory
        for (int i = 0; i < filesList.length; i++) {
            System.out.println(filesList[i]);
        }
    }

    /**
     * Creates a new directory or multiple directories at once.
     * @param args One or more directory names.
     */
    public void mkdir(String[] args) {
        //user must provide at least one directory name to create
        if (args.length < 1) {
            System.out.println("Error: mkdir requires at least one directory name");
            return;
        }

        for (int i = 0; i < args.length; i++) {
            String dirName = args[i];
            File newDir;

            //absolute paths are used as-is
            if (new File(dirName).isAbsolute()) {
                newDir = new File(dirName);
            } else {
                //relative ones are appended to current dir
                newDir = new File(pwd() + File.separator + dirName);
            }

            // mkdirs() creates intermediate directories too (e.g., "folder/subfolder")
            boolean created = newDir.mkdirs();
            if (!created) {
                //directory already exists or access denied
                System.out.println("Error: Failed to create directory " + dirName);
            }
        }
    }

    /**
     * Removes an empty directory and supports `*` to remove all empty directories in the current directory.
     * @param args The directory name or `*` wildcard.
     */
    public void rmdir(String[] args) {
        //enforcing one argument to avoid accidental mass deletion
        if (args.length != 1) {
            System.out.println("Error: rmdir requires exactly one argument");
            return;
        }

        String dirName = args[0];

        //'*' is helpful cleanup option
        if (dirName.equals("*")) {
            File currentDir = new File(pwd());
            File[] allFiles = currentDir.listFiles();

            //null if directory is unreadable
            if (allFiles == null) {
                System.out.println("Error: Cannot read current directory");
                return;
            }
            for (int i = 0; i < allFiles.length; i++) {
                //only target directories
                if (allFiles[i].isDirectory()) {
                    //remove directory if empty
                    String[] contents = allFiles[i].list();
                    if (contents != null && contents.length == 0) {
                        allFiles[i].delete();
                    }
                }
            }
            return;
        }

        //file object points to the directory we want to remove
        File dirToRemove;

        if (new File(dirName).isAbsolute()) {
            //use absolute path directly
            dirToRemove = new File(dirName);
        } else {
            //build path relative to current directory
            dirToRemove = new File(pwd() + File.separator + dirName);
        }

        //avoids trying to delete nonexistent paths
        if (!dirToRemove.exists()) {
            System.out.println("Error: Directory does not exist");
            return;
        }

        //prevents accidental deletion of files instead of folder
        if (!dirToRemove.isDirectory()) {
            System.out.println("Error: Not a directory");
            return;
        }

        //delete if empty
        String[] contents = dirToRemove.list();
        if (contents != null && contents.length > 0) {
            System.out.println("Error: Directory is not empty");
            return;
        }

        boolean deleted = dirToRemove.delete();
        if (!deleted) {
            System.out.println("Error: Failed to remove directory");
        }
    }


    /**
     * Creates a new file if it doesn’t already exist.
     * @param args A single filename.
     */
    public void touch(String[] args) {
        //operates on a single file
        if (args.length != 1) {
            System.out.println("Error: touch requires exactly one filename");
            return;
        }

        String fileName = args[0];
        File newFile;


        if (new File(fileName).isAbsolute()) {
            newFile = new File(fileName);
        } else {
            newFile = new File(pwd() + File.separator + fileName);
        }

        try {
            // createNewFile() only creates file if it doesn’t already exist
            if (newFile.createNewFile()) {
                //file was created successfully
            } else {
                //file already exists
            }
        } catch (IOException e) {
            System.out.println("Error: Cannot create file");
        }
    }

    /**
     * Copies a file from one location -source- to another -destination, overwritten if exists-.
     * @param args [sourceFile, destinationFile]
     */
    public void cp(String[] args) {
        //verify that the user provided both a source and a destination path
        if (args.length != 2) {
            System.out.println("Error: cp requires source and destination files");
            return;
        }

        //Store the two paths from arguments
        String sourceFileName = args[0];
        String destFileName = args[1];


        File sourceFile; //create file object for the source
        if (new File(sourceFileName).isAbsolute()) {
            // use absolute path directly
            sourceFile = new File(sourceFileName);
        } else {
            //build relative path based on the current working directory
            sourceFile = new File(pwd() + File.separator + sourceFileName);
        }

        //same logic as source file but for the destination file
        File destFile;
        if (new File(destFileName).isAbsolute()) {
            destFile = new File(destFileName);
        } else {
            destFile = new File(pwd() + File.separator + destFileName);
        }

        //ensure that the source file exists and isn't a directory
        if (!sourceFile.exists() || sourceFile.isDirectory()) {
            System.out.println("Error: Source file does not exist or is a directory");
            return;
        }

        //prepare variables for file I/O streams
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(sourceFile); //open input stream → read bytes from the source file
            outputStream = new FileOutputStream(destFile); //open output stream → write/overwrite bytes to the destination file
            byte[] buffer = new byte[512]; //buffer holds chunks of data as they're transferred
            int bytesRead;

            //read from source and write to destination until EOF is reached
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead); //chunk by chunk is read then written
            }
        } catch (IOException e) {
            System.out.println("Error: Cannot copy file");
        } finally {
            try {
                //close streams → free resources
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                System.out.println("Error closing files");
            }
        }
    }

    /**
     * Copies an entire directory and its contents recursively to a new location.
     * @param args [sourceDirectory, destinationDirectory]
     */
    public void cp_r(String[] args) {
        if (args.length != 2) {
            System.out.println("Error: cp -r requires source and destination directories");
            return;
        }

        String sourceDirName = args[0];
        String destDirName = args[1];

        //resolve path
        File sourceDir;
        if (new File(sourceDirName).isAbsolute()) {
            sourceDir = new File(sourceDirName);
        } else {
            sourceDir = new File(pwd() + File.separator + sourceDirName);
        }

        //resolve path
        File destDir;
        if (new File(destDirName).isAbsolute()) {
            destDir = new File(destDirName);
        } else {
            destDir = new File(pwd() + File.separator + destDirName);
        }

        // check if the source directory exists and is a directory
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            System.out.println("Error: Source directory does not exist");
            return;
        }

        // If destination directory doesn’t exist, create it
        if (!destDir.exists()) {
            destDir.mkdirs();  //use mkdirs() to create missing parent directories
        }

        //helper method to copy all files and subfolders
        copyDirectoryRecursive(sourceDir, destDir);
    }

    /**
     * Recursively copies all files/subdirectories from source directory to destination directory.
     * @param source      The folder being copied.
     * @param destination The target folder where contents will be placed.
     */
    private void copyDirectoryRecursive(File source, File destination) {
        // Create a new subdirectory inside the destination that has the same name as the source folder..
        File newDestination = new File(destination, source.getName());
        newDestination.mkdirs();


        File[] files = source.listFiles(); //get all files and folders inside the source directory.
        if (files == null) return; //return if directory is unreadable or empty.

        //loop through each file or subfolder inside the source directory.
        for (int i = 0; i < files.length; i++) {
            File currentFile = files[i];

            if (currentFile.isDirectory()) {
                //If the current item is a directory → (recursion) to copy that subfolder and its contents.
                copyDirectoryRecursive(currentFile, newDestination);
            } else {

                //if a regular file → create a destination file with the same name inside the new destination folder.
                File destFile = new File(newDestination, currentFile.getName());

                //prepare file streams for reading and writing
                FileInputStream inputStream = null;
                FileOutputStream outputStream = null;

                try {
                    inputStream = new FileInputStream(currentFile);
                    outputStream = new FileOutputStream(destFile);
                    byte[] buffer = new byte[512];
                    int length; //stores how many bytes were read

                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                } catch (IOException e) {
                    System.out.println("Error copying file: " + currentFile.getName());
                } finally {
                    try {
                        if (inputStream != null) inputStream.close();
                        if (outputStream != null) outputStream.close();
                    } catch (IOException e) {
                        //ignore closing error.
                    }
                }
            }
        }
    }

    /**
     * Deletes a file permanently.
     * @param args A single filename.
     */
    public void rm(String[] args) {
        //ensures user doesn’t accidentally delete everything
        if (args.length != 1) {
            System.out.println("Error: rm requires exactly one filename");
            return;
        }

        String fileName = args[0];
        File fileToDelete;

        //resolve path
        if (new File(fileName).isAbsolute()) {
            fileToDelete = new File(fileName);
        } else {
            fileToDelete = new File(pwd() + File.separator + fileName);
        }

        //Confirm that the file exists
        if (!fileToDelete.exists()) {
            System.out.println("Error: File does not exist");
            return;
        }

        //prevent directory deletion using the wrong command → should use rmdir
        if (fileToDelete.isDirectory()) {
            System.out.println("Error: Cannot remove directory with rm, use rmdir");
            return;
        }

        boolean deleted = fileToDelete.delete(); //delete the file from the filesystem.

        // If false → the operation failed
        if (!deleted) {
            System.out.println("Error: Failed to delete file");
        }
    }

    /**
     * Displays the contents of one or more files (sequentially).
     * @param args One or two filenames.
     */
    public void cat(String[] args) {
        //validate argument count
        if (args.length == 0 || args.length > 2) {
            System.out.println("Error: cat requires one or two filenames");
            return;
        }

        //loop through all provided file names 1/2
        for (int i = 0; i < args.length; i++) {
            String fileName = args[i];
            File file;

            //resolve path
            if (new File(fileName).isAbsolute()) {
                file = new File(fileName);
            } else {
                file = new File(pwd() + File.separator + fileName);
            }

            // Check if the file exists or isnt a directory.
            if (!file.exists() || file.isDirectory()) {
                System.out.println("Error: File " + fileName + " does not exist");
                continue; //skip the problem file and move to the next if exists
            }

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                String line; //read one line at a time

                //keep reading until end of file EOF
                while ((line = reader.readLine()) != null) {
                    System.out.println(line); //print each line as-is
                }
            } catch (IOException e) {
                System.out.println("Error: Cannot read file " + fileName);
            } finally {
                try {
                    //close the reader to free resources
                    if (reader != null) reader.close();
                } catch (IOException e) {
                    //ignore closing error.
                }
            }
        }
    }

    /**
     * Counts the number of lines, words, and characters in a file in the following format: lines words characters filename
     * @param args A single filename.
     */
    public void wc(String[] args) {
        // wc always expects one input file
        if (args.length != 1) {
            System.out.println("Error: wc requires exactly one filename");
            return;
        }

        String fileName = args[0];
        File file;

        //resolve path
        if (new File(fileName).isAbsolute()) {
            file = new File(fileName);
        } else {
            file = new File(pwd() + File.separator + fileName);
        }

        //check that the file exists and is not a directory.
        if (!file.exists() || file.isDirectory()) {
            System.out.println("Error: File does not exist");
            return;
        }

        //initialize counters for lines, words, and characters.
        int lineCount = 0;
        int wordCount = 0;
        int charCount = 0;

        BufferedReader reader = null;
        try {
            //Open the file for reading line by line.
            reader = new BufferedReader(new FileReader(file));
            String line;

            //process each line of the file until EOF
            while ((line = reader.readLine()) != null) {
                lineCount++;
                charCount += line.length(); //count characters in this line.
                String trimmedLine = line.trim(); //trim spaces from the start and end to avoid counting extra words.

                //split line by one or more spaces to count words when not empty
                if (trimmedLine.length() > 0) {
                    String[] words = trimmedLine.split("\\s+");
                    wordCount += words.length; //count words in this line
                }
            }

            System.out.println(lineCount + " " + wordCount + " " + charCount + " " + file.getName());

        } catch (IOException e) {
            System.out.println("Error: Cannot read file");
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                // Ignore closing error.
            }
        }
    }

    /**
     * Compresses files or directories into a ZIP archive and supports`-r` zipping for directories
     * @param args the name of the zip fil and the at least one file/directory
     */

    public void zip(String[] args) {
        //validate that the user provided an archive name and at least one file.
        if (args.length < 2) {
            System.out.println("Error: zip requires archive name and at least one file");
            return;
        }


        boolean recursive = false; //indicates whether to zip subdirectories.
        int startIndex = 0; //determines where file names start in args[].

        // if "zip -r", enable recursive mode for directories
        if (args[0].equals("-r")) {
            recursive = true;
            startIndex = 1;
        }

        //ensure there’s at least one file/directory to add to the archive.
        if (args.length - startIndex < 2) {
            System.out.println("Error: zip requires archive name and at least one file/directory");
            return;
        }

        String zipFileName = args[startIndex]; //extract the name for the zip archive


        if (!zipFileName.endsWith(".zip")) {
            zipFileName = zipFileName + ".zip"; //automatically append ".zip" if user didn’t include it.
        }

        //resolve absolute or relative path
        File zipFile;
        if (new File(zipFileName).isAbsolute()) {
            zipFile = new File(zipFileName);
        } else {
            zipFile = new File(pwd() + File.separator + zipFileName);
        }

        ZipOutputStream zipOut = null;
        try {
            //output stream that write compressed data to the file.
            FileOutputStream fos = new FileOutputStream(zipFile);
            zipOut = new ZipOutputStream(fos);

            //iterate over all files/directories that should be added to the archive.
            for (int i = startIndex + 1; i < args.length; i++) {
                String itemName = args[i];
                File itemFile;

                // Resolve the path for each item
                if (new File(itemName).isAbsolute()) {
                    itemFile = new File(itemName);
                } else {
                    itemFile = new File(pwd() + File.separator + itemName);
                }

                // Skip missing items but warn the user.
                if (!itemFile.exists()) {
                    System.out.println("Warning: " + itemName + " does not exist, skipping");
                    continue;
                }

                if (itemFile.isDirectory() && recursive) {
                    //item is a directory and recursive mode is enabled → copy its contents recursively.
                    zipDirectoryRecursive(itemFile, itemFile.getName(), zipOut);
                } else if (itemFile.isFile()) {
                    //item is a regular file → zip it directly.
                    zipSingleFile(itemFile, itemFile.getName(), zipOut);
                }
            }

        } catch (IOException e) {
            System.out.println("Error: Cannot create zip file");
        } finally {
            try {
                if (zipOut != null) zipOut.close();
            } catch (IOException e) {
                //ignore closing error.
            }
        }
    }

    /**
     * Helper method to add a single file into a ZIP archive.
     * @param file The file to compress.
     * @param fileName The name of the entry inside the zip.
     * @param zipOut The output ZIP stream.
     * @throws IOException If reading or writing fails.
     */
    private void zipSingleFile(File file, String fileName, ZipOutputStream zipOut) throws IOException {
        FileInputStream fis = new FileInputStream(file); // Open input stream read bytes from the file.
        ZipEntry zipEntry = new ZipEntry(fileName);  //create ZIP entry with the file’s name representing it inside the archive.
        zipOut.putNextEntry(zipEntry); //writes a new entry to the zip.

        //read file data into a buffer and write it to the ZIP output stream.
        byte[] buffer = new byte[500];
        int length;
        while ((length = fis.read(buffer)) >= 0) {
            zipOut.write(buffer, 0, length);
        }

        //close the file and the current entry once done.
        fis.close();
        zipOut.closeEntry();
    }

    /**
     * Helper method to compress an entire directory recursively.
     * @param folder The source directory to compress.
     * @param parentFolder The base path name for entries.
     * @param zipOut The output ZIP stream.
     * @throws IOException If reading or writing fails.
     */
    private void zipDirectoryRecursive(File folder, String parentFolder, ZipOutputStream zipOut) throws IOException {
        File[] files = folder.listFiles();
        if (files == null) return; //empty/unreadable folder

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            //recursive call to ensure nested folders are included too
            if (file.isDirectory()) {
                zipDirectoryRecursive(file, parentFolder + "/" + file.getName(), zipOut);
            } else {
                //add normal file entry inside correct subpath
                zipSingleFile(file, parentFolder + "/" + file.getName(), zipOut);
            }
        }
    }


    /**
     * Extracts the contents of a ZIP archive and supports `-d` to specify a destination directory.
     * @param args The zip file name and optional -d flag + destination.
     */
    public void unzip(String[] args) {
        //must at least include zip file name
        if (args.length < 1) {
            System.out.println("Error: unzip requires zip file name");
            return;
        }

        String zipFileName = args[0];
        File zipFile;

        //resolve path
        if (new File(zipFileName).isAbsolute()) {
            zipFile = new File(zipFileName);
        } else {
            zipFile = new File(pwd() + File.separator + zipFileName);
        }

        // Check if ZIP file exists
        if (!zipFile.exists()) {
            System.out.println("Error: Zip file does not exist");
            return;
        }

        String extractPath = pwd(); //default extraction path is the current working directory.
        //if using the zip -d option followed by a destination, extract there instead.
        if (args.length >= 3 && args[1].equals("-d")) {
            String destPath = args[2];
            File extractDir;

            //resolve path for destination folder
            if (new File(destPath).isAbsolute()) {
                extractDir = new File(destPath);
            } else {
                extractDir = new File(pwd() + File.separator + destPath);
            }

            //ensure the destination directory exists and created by mkdir if missing.
            extractPath = extractDir.getAbsolutePath();
            if (!extractDir.exists()) {
                extractDir.mkdirs();
            }
        }


        ZipInputStream zipIn = null;
        try {
            //open the zip file for reading.
            FileInputStream fis = new FileInputStream(zipFile);
            zipIn = new ZipInputStream(fis);
            ZipEntry entry;

            //loop through each file/directory inside the ZIP.
            while ((entry = zipIn.getNextEntry()) != null) {
                String filePath = extractPath + File.separator + entry.getName();

                if (entry.isDirectory()) {
                    //entry is a directory → create the folder structure.
                    File dir = new File(filePath);
                    dir.mkdirs();
                } else {
                    //it’s a file → extract it to the destination directory.
                    File extractFile = new File(filePath);

                    File parent = extractFile.getParentFile();
                    if (parent != null) {
                        parent.mkdirs(); //create parent directories if they don’t already exist
                    }

                    //write the file contents from the zip stream to disk
                    FileOutputStream fos = new FileOutputStream(extractFile);
                    byte[] buffer = new byte[500];
                    int length;
                    while ((length = zipIn.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                    fos.close();
                }
                zipIn.closeEntry();  //close current zip entry and move to the next.
            }

        } catch (IOException e) {
            System.out.println("Error: Cannot extract zip file");
        } finally {
            try {
                if (zipIn != null) zipIn.close();
            } catch (IOException e) {
                //ignore error
            }
        }
    }

    /**
     * Acts as the main controller that routes user input to the correct function.
     */
    public void chooseCommandAction() {
        String command = parser.getCommandName(); //parsed command name
        String[] arguments = parser.getArgs(); //arguments from the parser

        //match the parsed command to its corresponding function.
        if (command.equals("pwd")) {
            System.out.println(pwd());
        } else if (command.equals("cd")) {
            cd(arguments);
        } else if (command.equals("ls")) {
            ls();
        } else if (command.equals("mkdir")) {
            mkdir(arguments);
        } else if (command.equals("rmdir")) {
            rmdir(arguments);
        } else if (command.equals("touch")) {
            touch(arguments);
        } else if (command.equals("cp")) {
            //check if cp includes "-r"
            if (arguments.length > 0 && arguments[0].equals("-r")) {
                //remove -r from arguments before passing to cp_r().
                String[] newArgs = new String[arguments.length - 1];
                for (int i = 1; i < arguments.length; i++) {
                    newArgs[i - 1] = arguments[i];
                }
                cp_r(newArgs); //copy directories recursively.
            } else {
                cp(arguments); //perform normal file copy.
            }
        } else if (command.equals("rm")) {
            rm(arguments);
        } else if (command.equals("cat")) {
            cat(arguments);
        } else if (command.equals("wc")) {
            wc(arguments);
        } else if (command.equals("zip")) {
            zip(arguments);
        } else if (command.equals("unzip")) {
            unzip(arguments);
        } else if (command.equals("exit")) {
            System.out.println("Exiting CLI...");
            System.exit(0); //exit the CLI session.
        } else {
            System.out.println("Command not found: " + command); //unrecognized commands
        }
    }
}

