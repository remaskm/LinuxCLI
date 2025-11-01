package com.remas.cli;

/**
 * parser class is responsible for interpreting user input
 * entered into the terminal so terminal class decide which
 * command to execute and what parameters to pass along.
 */
public class Parser {
    String commandName;
    String[] args;

    /** user's input string is split into the command name and its arguments.
     * @param input user full command line.
     * @return true if parsed, false if empty/invalid.
     */
    public boolean parse(String input){
        //doesnt go ahead with the splitting process if input is empty
        if (input == null || input.trim().length() == 0) {
            return false;
        }

        //split depending on space occurrence
        String[] words = input.trim().split("\\s+");

        //no actual text after splitting
        if (words.length == 0) {
            return false;
        }

        commandName = words[0]; //first token is the main command

        //remaining tokens are treated as command arguments
        args = new String[words.length - 1];
        for (int i = 1; i < words.length; i++) {
            args[i - 1] = words[i];  //copy each argument in order
        }
        return true;
    }

    /**
     * @return The parsed command name e.g. "ls"
     */
    public String getCommandName(){
        return commandName;
    }

    /**
     * @return The list of arguments passed along with the command
     */
    public String[] getArgs(){
        return args;
    }

}
