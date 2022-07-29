package com.agolovenko.jspring.OSM.OSMStat;

import com.agolovenko.jspring.OSM.Parser.IStAXAPIParser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@AllArgsConstructor
public class OSMStatistics {
    private final IStAXAPIParser parser;

    public void printOrderedByKeyName() {
        parser.getElements().entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(System.out::println);
    }

    public void printOrderedByKeyRepetition() {
        parser.getElements().entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(System.out::println);
    }
}
