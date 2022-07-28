package com.agolovenko.jspring.OSM.OSMStat;

import com.agolovenko.jspring.OSM.Parser.IStAXAPIParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OSMStatistics {

    private final IStAXAPIParser parser;

    @Autowired
    OSMStatistics(IStAXAPIParser parser) {
        this.parser = parser;
    }

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
