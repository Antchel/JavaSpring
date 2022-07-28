package com.agolovenko.jspring.OSM.ParserImpl;

import ch.qos.logback.classic.Level;
import com.agolovenko.jspring.OSM.Parser.IStAXAPIParser;
import com.agolovenko.jspring.OSM.ParserImpl.OSMDataCursorParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

@Component
public class OSMDataIteratorParser implements IStAXAPIParser {

    private final Logger logger = LoggerFactory.getLogger(OSMDataCursorParser.class);
    final ch.qos.logback.classic.Logger logger2 =
            (ch.qos.logback.classic.Logger) logger;

    private static final Map<String, Integer> elements = new TreeMap<>();
    private XMLEventReader xmlEventReader;

    private void initParser(InputStream XMLDataStream) throws XMLStreamException {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        xmlEventReader = xmlInputFactory.createXMLEventReader(XMLDataStream);
    }

    @Override
    public void parseXML(InputStream XMLDataStream) throws XMLStreamException {
        initParser(XMLDataStream);
        logger2.setLevel(Level.INFO);
        XMLFinish:
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent;
            try {
                xmlEvent = xmlEventReader.nextEvent();
            } catch (XMLStreamException e) {
                throw new RuntimeException(e);
            }
            switch (xmlEvent.getEventType()) {
                case XMLStreamConstants.START_DOCUMENT -> logger.info("XMLEvent.START_DOCUMENT");
                case XMLStreamConstants.START_ELEMENT -> {
                    StartElement startElement = xmlEvent.asStartElement();
                    if ((startElement.getName().toString()).equals("way") || startElement.getName().toString().equals("relation")) {
                        logger.info("Elements camount is " + elements.size());
                        break XMLFinish;
                    }
                    if (startElement.getName().toString().equals("node")) {
                        while (xmlEventReader.hasNext()) {
                            try {
                                xmlEvent = xmlEventReader.nextEvent();
                                if (xmlEvent.isStartElement()) startElement = xmlEvent.asStartElement();
                            } catch (XMLStreamException e) {
                                throw new RuntimeException(e);
                            }
                            if (xmlEvent.getEventType() == XMLStreamConstants.START_ELEMENT && startElement.getName().toString().equals("tag")) {
                                String attrName = startElement.getAttributeByName(new QName("k")).toString();
                                if (elements.containsKey(attrName))
                                    elements.put(attrName, elements.get(attrName) + 1);
                                else elements.put(attrName, 1);
                            }
                            if (xmlEvent.getEventType() == XMLStreamConstants.END_ELEMENT && startElement.getName().toString().equals("node")) {
                                logger.debug("XMLStream.END_ELEMENT <node\\>");
                                break;
                            }
                        }
                    }
                }
                case XMLStreamConstants.END_ELEMENT -> {
                    EndElement endElement = xmlEvent.asEndElement();
                    logger.debug("XMLEvent1.END_ELEMENT:   </" + endElement.getName() + ">");
                }
                case XMLStreamConstants.END_DOCUMENT ->
                        logger.info("XMLEvent.END_DOCUMENT " + elements.size() + " elements");
                default -> logger.debug("case default: Event Type = " + xmlEvent.getEventType());
            }
        }
    }
    @Override
    public Map<String, Integer> getElements() {
        return elements;
    }
}
