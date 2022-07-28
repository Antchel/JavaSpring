package com.agolovenko.jspring.OSM.ParserImpl;

import ch.qos.logback.classic.Level;
import com.agolovenko.jspring.OSM.Parser.IStAXAPIParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

@Component
@Primary
public class OSMDataCursorParser implements IStAXAPIParser {

    private final Logger logger = LoggerFactory.getLogger(OSMDataCursorParser.class);
    private final ch.qos.logback.classic.Logger logger2 =
            (ch.qos.logback.classic.Logger) logger;

    private static final Map<String, Integer> elements = new TreeMap<>();

    private XMLStreamReader xmlStreamReader;

    private void initParser(InputStream XMLDataStream) throws XMLStreamException {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        xmlStreamReader = xmlInputFactory.createXMLStreamReader(XMLDataStream);
    }

    @Override
    public void parseXML(InputStream XMLDataStream) throws XMLStreamException {
        initParser(XMLDataStream);
        logger2.setLevel(Level.INFO);

        XMLFINISH:
        while (true) {
            try {
                if (!xmlStreamReader.hasNext()) break;

                xmlStreamReader.next();
                switch (xmlStreamReader.getEventType()) {
                    case XMLStreamConstants.START_DOCUMENT -> logger.info("XMLStream.START_DOCUMENT");
                    case XMLStreamConstants.START_ELEMENT -> {
                        logger.debug("XMLStream.START_ELEMENT <" + xmlStreamReader.getLocalName() + ">");
                        if ((xmlStreamReader.getLocalName()).equals("way") || xmlStreamReader.getLocalName().equals("relation")) {
                            logger.info("XMLStream FINISH found " + elements.size() + " unique elements!");
                            break XMLFINISH;
                        }
                        if ((xmlStreamReader.getLocalName()).equals("node")) {
                            while (xmlStreamReader.hasNext()) {
                                xmlStreamReader.next();
                                if (xmlStreamReader.getEventType() == XMLStreamConstants.START_ELEMENT && xmlStreamReader.getLocalName().equals("tag")) {
                                    String nodeKey = xmlStreamReader.getAttributeValue(0);
                                    if (elements.containsKey(nodeKey))
                                        elements.put(nodeKey, elements.get(nodeKey) + 1);
                                    else
                                        elements.put(nodeKey, 1);
                                }
                                if (xmlStreamReader.getEventType() == XMLStreamConstants.END_ELEMENT && xmlStreamReader.getLocalName().equals("node")) {
                                    logger.debug("XMLStream.END_ELEMENT <" + xmlStreamReader.getLocalName() + "\\>");
                                    break;
                                }
                            }
                        }
                    }
                    case XMLStreamConstants.END_ELEMENT ->
                            logger.info("XMLStream.END_ELEMENT <" + xmlStreamReader.getLocalName() + "\\>");
                    case XMLStreamConstants.END_DOCUMENT -> logger.info("XMLStream.END_DOCUMENT");
                }
            } catch (XMLStreamException e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Override
    public Map<String, Integer> getElements() {
        return elements;
    }
}
