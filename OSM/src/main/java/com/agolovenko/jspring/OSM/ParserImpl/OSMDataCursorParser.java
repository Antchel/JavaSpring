package com.agolovenko.jspring.OSM.ParserImpl;

import com.agolovenko.jspring.OSM.DB.NodeService;
import com.agolovenko.jspring.OSM.Parser.IStAXAPIParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

@Component
@ConditionalOnProperty(
        value="parser.type.selected",
        havingValue = "cursor"
)

@Slf4j
public class OSMDataCursorParser implements IStAXAPIParser {

    private static final Map<String, Integer> elements = new TreeMap<>();

    private XMLStreamReader xmlStreamReader;

    private void initParser(InputStream XMLDataStream) throws XMLStreamException {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        xmlStreamReader = xmlInputFactory.createXMLStreamReader(XMLDataStream);
    }

    @Override
    public void parseXML(InputStream XMLDataStream) throws XMLStreamException {
        initParser(XMLDataStream);

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
                                    log.debug("XMLStream.END_ELEMENT <" + xmlStreamReader.getLocalName() + "\\>");
                                    break;
                                }
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
    public void parseXMLAndWriteToDB(InputStream XMLDataStream, NodeService nodeService) {

    }

    @Override
    public Map<String, Integer> getElements() {
        return elements;
    }
}
