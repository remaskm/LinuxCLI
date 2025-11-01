package com.remas.cli;
import java.io.*;
import java.util.*;


/**
 * Main class runs the main CLI loop that reads user commands, parses them, and executes the corresponding actions.
 * Supports output redirection using '>' and '>>'
 */
public class Main {
    public static void main(String[] args) {
        //initialize CLI environment and its command parser.
        Terminal ourCLI = new Terminal();
        ourCLI.parser = new Parser();
        Scanner scanner = new Scanner(System.in);
        String userInput;

        while (true) { //keep the CLI running until user exits manually
            System.out.print(System.getProperty("user.name") + "@CLI:" + ourCLI.currentDirectory + "$ ");
            userInput = scanner.nextLine().trim(); //read the user's command input and trim extra spaces.

            //exit when user explicitly types "exit".
            if (userInput.equalsIgnoreCase("exit")) {
                System.out.println("CLI session closed by user (" + System.getProperty("user.name") + ")");
                break;
            }

            //handle output redirection with ">>" append
            if (userInput.contains(">>")) {
                String[] parts = userInput.split(">>", 2);
                String cmd = parts[0].trim(); //extract command before >>
                String outFile = parts[1].trim(); //extract output file name

                //parse and validate the command
                if (ourCLI.parser.parse(cmd)) {
                    //temporarily redirect system output to capture command output.
                    PrintStream oldOut = System.out;
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    PrintStream captureOut = new PrintStream(outputStream);
                    System.setOut(captureOut);
                    ourCLI.chooseCommandAction(); //execute the command but capture its output.
                    System.setOut(oldOut); //restore original output stream to the console.
                    String output = outputStream.toString();

                    //append the captured output to the target file.
                    try {
                        File outFileObj = new File(ourCLI.currentDirectory, outFile); // relative to current directory
                        FileWriter writer = new FileWriter(outFileObj, true); // true = append mode
                        writer.write(output);
                        writer.close();
                    } catch (IOException e) {
                        System.out.println("Error writing to file");
                    }
                }
            }
            //handle output redirection with ">" overwrite
            else if (userInput.contains(">")) {
                String[] parts = userInput.split(">", 2);
                String cmd = parts[0].trim();
                String outFile = parts[1].trim();

                if (ourCLI.parser.parse(cmd)) {
                    //redirect output temporarily.
                    PrintStream oldOut = System.out;
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    PrintStream captureOut = new PrintStream(outputStream);
                    System.setOut(captureOut);
                    ourCLI.chooseCommandAction(); //run command and capture output.
                    System.setOut(oldOut);
                    String output = outputStream.toString(); //restore normal console output.

                    //overwrite the output file with the new content.
                    try {
                        File outFileObj = new File(ourCLI.currentDirectory, outFile); //relative to current directory
                        FileWriter writer = new FileWriter(outFileObj, false); // false = overwrite mode
                        writer.write(output);
                        writer.close();
                    } catch (IOException e) {
                        System.out.println("Error writing to file");
                    }
                }
                //normal command execution → no redirection
            } else {
                if (ourCLI.parser.parse(userInput)) {
                    ourCLI.chooseCommandAction(); //run the recognized command
                } else {
                    System.out.println("Error: Command not recognized.");  //invalid / unsupported command
                }
            }
        }
        scanner.close();
    }
}
