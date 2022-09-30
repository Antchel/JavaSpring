package com.agolovenko.jspring.OSM.Parser;

import com.agolovenko.jspring.OSM.DB.NodeService;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.Map;

public interface IStAXAPIParser {
    void parseXML(InputStream XMLDataStream) throws XMLStreamException;
    void parseXMLAndWriteToDB(InputStream XMLDataStream, NodeService nodeService) throws XMLStreamException;

    Map<String, Integer> getElements();
}
