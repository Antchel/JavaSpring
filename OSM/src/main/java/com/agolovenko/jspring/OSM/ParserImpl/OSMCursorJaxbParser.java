package com.agolovenko.jspring.OSM.ParserImpl;

import com.agolovenko.jspring.OSM.DB.NodeService;
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
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

@Component
@Slf4j
@ConditionalOnProperty(
        value = "parser.type.selected",
        havingValue = "jaxb_cursor",
        matchIfMissing = true
)
public class OSMCursorJaxbParser implements IStAXAPIParser {
    private static final Map<String, Integer> elements = new TreeMap<>();
    private XMLStreamReader xmlStreamReader;

    private void initParser(InputStream XMLDataStream) throws XMLStreamException {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        xmlStreamReader = xmlInputFactory.createXMLStreamReader(XMLDataStream);
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

        XMLFINISH:
        while (true) {
            try {
                if (!xmlStreamReader.hasNext()) break;

                xmlStreamReader.next();
                switch (xmlStreamReader.getEventType()) {
                    case XMLStreamConstants.START_DOCUMENT -> log.info("XMLStream.START_DOCUMENT");
                    case XMLStreamConstants.START_ELEMENT -> {
                        log.debug("XMLStream.START_ELEMENT <" + xmlStreamReader.getLocalName() + ">");
                        if ((xmlStreamReader.getLocalName()).equals("way") || xmlStreamReader.getLocalName().equals("relation")) {
                            log.info("XMLStream FINISH found " + elements.size() + " unique elements!");
                            break XMLFINISH;
                        }
                        if ((xmlStreamReader.getLocalName()).equals("node")) {
                            JAXBElement<Node> element;
                            try {
                                element = um.unmarshal(xmlStreamReader, Node.class);
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
                    }
                    case XMLStreamConstants.END_ELEMENT ->
                            log.info("XMLStream.END_ELEMENT <" + xmlStreamReader.getLocalName() + "\\>");
                    case XMLStreamConstants.END_DOCUMENT -> log.info("XMLStream.END_DOCUMENT");
                }
            } catch (XMLStreamException e) {
                throw new RuntimeException(e);
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
        boolean running = true;
        XMLFINISH:
        while (running) {
            try {
                if (!xmlStreamReader.hasNext()) break;

                switch (xmlStreamReader.next()) {
                    case XMLStreamConstants.START_DOCUMENT -> log.info("XMLStream.START_DOCUMENT");
                    case XMLStreamConstants.START_ELEMENT -> {
                        log.debug("XMLStream.START_ELEMENT <" + xmlStreamReader.getLocalName() + ">");
                        if ((xmlStreamReader.getLocalName()).equals("way") || xmlStreamReader.getLocalName().equals("relation")) {
                            log.info("XMLStream FINISH found " + elements.size() + " unique elements!");
//                            nodeService.close();
                            break XMLFINISH;
                        }
                        if ((xmlStreamReader.getLocalName()).equals("node")) {
                            JAXBElement<Node> element;
                            try {
                                element = um.unmarshal(xmlStreamReader, Node.class);
                            } catch (JAXBException e) {
                                throw new RuntimeException(e);
                            }
                            Node response = element.getValue();
                            nodeService.createNode(response);

//                            for (int i = 0; i < response.getTag().size(); i++) {
//                                String key = response.getTag().get(i).getK();
//                                if (elements.containsKey(key))
//                                    elements.put(key, elements.get(key) + 1);
//                                else elements.put(key, 1);
//                            }
                        }
                    }
                    case XMLStreamConstants.END_ELEMENT ->
                            log.info("XMLStream.END_ELEMENT <" + xmlStreamReader.getLocalName() + "\\>");
                    case XMLStreamConstants.END_DOCUMENT -> {
                        log.info("XMLStream.END_DOCUMENT");
                        running = false;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Map<String, Integer> getElements() {
        return elements;
    }
}
