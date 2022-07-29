package com.agolovenko.jspring.OSM;

import com.agolovenko.jspring.OSM.CLI.CommandLineOptionsHandler;
import com.agolovenko.jspring.OSM.OSMStat.OSMStatistics;
import com.agolovenko.jspring.OSM.Parser.IStAXAPIParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
public class OsmApplication {
    private final int BUFF_SIZE = 32786;

    @Bean
    CommandLineRunner processXML(IStAXAPIParser osmDataParser, OSMStatistics statistic) {
        return args -> {
            CommandLineOptionsHandler cmdHandler = new CommandLineOptionsHandler(args);
            String filePath = "";
            try (InputStream in = Files.newInputStream(Paths.get(cmdHandler.getOptionByName("f", filePath)));
                 BufferedInputStream bin = new BufferedInputStream(in, BUFF_SIZE);
                 BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(bin)) {

                osmDataParser.parseXML(bzIn);
                statistic.printOrderedByKeyRepetition();

            } catch (ParseException | IOException | XMLStreamException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(OsmApplication.class, args);
    }

}
