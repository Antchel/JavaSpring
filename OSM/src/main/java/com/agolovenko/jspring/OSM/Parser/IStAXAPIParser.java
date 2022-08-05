package com.agolovenko.jspring.OSM.Parser;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.Map;

public interface IStAXAPIParser {
    void parseXML(InputStream XMLDataStream) throws XMLStreamException;

    Map<String, Integer> getElements();
}
