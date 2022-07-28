package com.agolovenko.jspring.OSM.CLI;

import org.apache.commons.cli.*;

public class CommandLineOptionsHandler {
    private static final Options options = new Options();
    private CommandLine cmd = null;

    static {
        options.addRequiredOption("f", "failPath", true, "Path to OSM file");

    }

    public CommandLineOptionsHandler(String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
    }

    public <T> T getOptionByName(String optionName, T storage) throws ParseException {
        if (cmd.hasOption(optionName)) {
            if (storage instanceof Number)
                storage = (T) cmd.getParsedOptionValue(optionName);
            else if (storage instanceof String)
                storage = (T) cmd.getOptionValue(optionName);
            else
                throw new RuntimeException("Wrong storage variable type");
        }
        return storage;
    }


}
