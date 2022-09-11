package com.agolovenko.jspring.OSM.ParserImpl;

import com.agolovenko.jspring.OSM.DB.NodeService;
import com.agolovenko.jspring.OSM.Parser.IStAXAPIParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@Slf4j
@ConditionalOnProperty(
        value = "parser.type.selected",
        havingValue = "event"
)

public class OSMDataIteratorParser implements IStAXAPIParser {

    private static final Map<String, Integer> elements = new TreeMap<>();
    private XMLEventReader xmlEventReader;

    private void initParser(InputStream XMLDataStream) throws XMLStreamException {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        xmlEventReader = xmlInputFactory.createXMLEventReader(XMLDataStream);
    }

    @Override
    public void parseXML(InputStream XMLDataStream) throws XMLStreamException {
        initParser(XMLDataStream);
        XMLFinish:
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent;
            try {
                xmlEvent = xmlEventReader.nextEvent();
            } catch (XMLStreamException e) {
                throw new RuntimeException(e);
            }
            switch (xmlEvent.getEventType()) {
                case XMLStreamConstants.START_DOCUMENT -> log.info("XMLEvent.START_DOCUMENT");
                case XMLStreamConstants.START_ELEMENT -> {
                    StartElement startElement = xmlEvent.asStartElement();
                    if ((startElement.getName().toString()).equals("way") || startElement.getName().toString().equals("relation")) {
                        log.info("Elements amount is " + elements.size());

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
                                log.debug("XMLStream.END_ELEMENT <node\\>");
                                break;
                            }
                        }
                    }
                }
                case XMLStreamConstants.END_ELEMENT -> {
                    EndElement endElement = xmlEvent.asEndElement();
                    log.debug("XMLEvent1.END_ELEMENT:   </" + endElement.getName() + ">");
                }
                case XMLStreamConstants.END_DOCUMENT ->
                        log.info("XMLEvent.END_DOCUMENT " + elements.size() + " elements");
                default -> log.debug("case default: Event Type = " + xmlEvent.getEventType());
            }
        }
    }

    @Override
    public void parseXMLAndWriteToDB(InputStream XMLDataStream, NodeService nodeService) {

    }

    @Override
    public Map<String, Integer> getElements() {
        return elements;
    }
}
