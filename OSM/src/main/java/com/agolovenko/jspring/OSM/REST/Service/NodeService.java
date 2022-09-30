package com.agolovenko.jspring.OSM.REST.Service;

import com.agolovenko.jspring.OSM.REST.persist.Node;

import java.util.List;

public interface NodeService {
    List<Node> getAllNodes();

    Node getNode(Long id);

    void create(Node node);

    boolean update(Node node, Long id);

    void delete(Long id);

    List<Node> getNodesByAmenity(Float lat, Float lon, String amenity, Integer distance);

    List<Node> getNodesIntoArea(Float lat, Float lon, Integer distance);
}
