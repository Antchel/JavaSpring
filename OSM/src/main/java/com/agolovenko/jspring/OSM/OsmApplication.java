package com.agolovenko.jspring.OSM;

import ch.qos.logback.classic.Level;
import com.agolovenko.jspring.OSM.CLI.CommandLineOptionsHandler;
import com.agolovenko.jspring.OSM.DB.NodeService;
import com.agolovenko.jspring.OSM.OSMStat.OSMStatistics;
import com.agolovenko.jspring.OSM.Parser.IStAXAPIParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.ParseException;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
@Slf4j
public class OsmApplication {
    @Bean(name="userDataSource") //we can skip name specification here actually. Name is based on method name
    @Primary
    @ConfigurationProperties(prefix = "userds")
    DataSource userDataSource() {
        log.debug("allocating userDataSource");
        DataSourceBuilder<?> b = DataSourceBuilder.create();
        DataSource ds = b.build();
        log.debug("userDataSource created");
        return ds;
    }
    private final int BUFF_SIZE = 32786;

    @Bean
    CommandLineRunner processXML(IStAXAPIParser osmDataParser, OSMStatistics statistic,
                                 @Qualifier("copyService")
                                 NodeService userService) {
        return args -> {
            CommandLineOptionsHandler cmdHandler = new CommandLineOptionsHandler(args);
            ((ch.qos.logback.classic.Logger) log).setLevel(Level.ALL);
            String filePath = "C:\\Users\\agolovenko\\Downloads\\ural-fed-district-latest.osm.bz2";
            try (InputStream in = Files.newInputStream(Paths.get(cmdHandler.getOptionByName("f", filePath)));
                 BufferedInputStream bin = new BufferedInputStream(in, BUFF_SIZE);
                 BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(bin)) {

                osmDataParser.parseXMLAndWriteToDB(bzIn, userService);
                statistic.printOrderedByKeyRepetition();

            } catch (ParseException | IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(OsmApplication.class, args);
    }

}
