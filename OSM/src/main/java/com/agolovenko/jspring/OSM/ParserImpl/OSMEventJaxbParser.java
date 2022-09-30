package com.agolovenko.jspring.OSM.ParserImpl;

import com.agolovenko.jspring.OSM.DB.NodeService;
import com.agolovenko.jspring.OSM.DB.NodeServiceException;
import com.agolovenko.jspring.OSM.Parser.IStAXAPIParser;
import com.agolovenko.jspring.osmjaxbclasses.Node;
import com.agolovenko.jspring.osmjaxbclasses.ObjectFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
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
        havingValue = "jaxb",
        matchIfMissing = true
)
public class OSMEventJaxbParser implements IStAXAPIParser {
    private static final Map<String, Integer> elements = new TreeMap<>();
    private XMLEventReader xmlEventReader;

    private void initParser(InputStream XMLDataStream) throws XMLStreamException {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        xmlEventReader = xmlInputFactory.createXMLEventReader(XMLDataStream);
    }

    @Override
    public void parseXML(InputStream XMLDataStream) throws XMLStreamException {
        initParser(XMLDataStream);
        Unmarshaller um;
        try {
            JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class);
            um = jc.createUnmarshaller();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
        XMLFinish:
        while (xmlEventReader.peek() != null) {
            XMLEvent xmlEvent = xmlEventReader.peek();
            switch (xmlEvent.getEventType()) {
                case XMLStreamConstants.START_DOCUMENT -> {
                    log.info("XMLEventJAXB.START_DOCUMENT");
                    xmlEventReader.nextEvent();
                }
                case XMLStreamConstants.START_ELEMENT -> {
                    StartElement startElement = xmlEvent.asStartElement();
                    if ((startElement.getName().toString()).equals("way") || startElement.getName().toString().equals("relation")) {
                        log.info("Elements amount is " + elements.size());
                        break XMLFinish;
                    }
                    if (startElement.getName().toString().equals("node")) {
                        JAXBElement<Node> element;
                        try {
                            element = um.unmarshal(xmlEventReader, Node.class);
                        } catch (JAXBException e) {
                            throw new RuntimeException(e);
                        }
                        Node response = element.getValue();
                        for (int i = 0; i < response.getTag().size(); i++) {
                            String key = response.getTag().get(i).getK();
                            if (elements.containsKey(key))
                                elements.put(key, elements.get(key) + 1);
                            else elements.put(key, 1);
                        }
                    }
                    else xmlEventReader.nextEvent();
                }
                case XMLStreamConstants.END_ELEMENT -> {
                    EndElement endElement = xmlEventReader.nextEvent().asEndElement();
                    log.debug("XMLEvent1.END_ELEMENT:   </" + endElement.getName() + ">");
                }
                case XMLStreamConstants.END_DOCUMENT -> {
                    xmlEventReader.nextEvent();
                    log.info("XMLEvent.END_DOCUMENT " + elements.size() + " elements");
                }
                default -> {
                    xmlEventReader.nextEvent();
                    log.debug("case default: Event Type = " + xmlEvent.getEventType());
                }
            }
        }
    }

    @Override
    public void parseXMLAndWriteToDB(InputStream XMLDataStream,
                                     NodeService nodeService) throws XMLStreamException {
        initParser(XMLDataStream);
        Unmarshaller um;
        try {
            JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class);
            um = jc.createUnmarshaller();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
        XMLFinish:
        while (xmlEventReader.peek() != null) {
            XMLEvent xmlEvent = xmlEventReader.peek();
            switch (xmlEvent.getEventType()) {
                case XMLStreamConstants.START_DOCUMENT -> {
                    log.info("XMLEventJAXB.START_DOCUMENT");
                    xmlEventReader.nextEvent();
                }
                case XMLStreamConstants.START_ELEMENT -> {
                    StartElement startElement = xmlEvent.asStartElement();
                    if ((startElement.getName().toString()).equals("way") || startElement.getName().toString().equals("relation")) {
                        log.info("Elements amount is " + elements.size());

                        break XMLFinish;
                    }
                    if (startElement.getName().toString().equals("node")) {
                        JAXBElement<Node> element;
                        try {
                            element = um.unmarshal(xmlEventReader, Node.class);
                        } catch (JAXBException e) {
                            throw new RuntimeException(e);
                        }
                        Node response = element.getValue();
                        try {
                            nodeService.createNode(response);
                        } catch (NodeServiceException e) {
                            throw new RuntimeException(e);
                        }
                        for (int i = 0; i < response.getTag().size(); i++) {
                            String key = response.getTag().get(i).getK();
                            if (elements.containsKey(key))
                                elements.put(key, elements.get(key) + 1);
                            else elements.put(key, 1);
                        }
                    }
                    else xmlEventReader.nextEvent();
                }
                case XMLStreamConstants.END_ELEMENT -> {
                    EndElement endElement = xmlEventReader.nextEvent().asEndElement();
                    log.debug("XMLEvent1.END_ELEMENT:   </" + endElement.getName() + ">");
                }
                case XMLStreamConstants.END_DOCUMENT -> {
                    xmlEventReader.nextEvent();
                    log.info("XMLEvent.END_DOCUMENT " + elements.size() + " elements");
                }
                default -> {
                    xmlEventReader.nextEvent();
                    log.debug("case default: Event Type = " + xmlEvent.getEventType());
                }
            }
        }
    }

    @Override
    public Map<String, Integer> getElements() {
        return elements;
    }
}
