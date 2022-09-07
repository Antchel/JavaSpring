package com.agolovenko.jspring.OSM.REST.Service.Impl;

import com.agolovenko.jspring.OSM.REST.Service.NodeService;
import com.agolovenko.jspring.OSM.REST.persist.Node;
import com.agolovenko.jspring.OSM.REST.persist.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;


@Service
public class NodeServiceImpl implements NodeService {

    private final NodeRepository nodeRepository;

    @Autowired
    public NodeServiceImpl(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    @Override
    @Transactional
    public List<Node> getAllUsers() {
        ArrayList<Node> nodes = new ArrayList<>();
        nodeRepository.findAll().forEach(nodes::add);
        return nodes;
    }

    @Override
    @Transactional
    public Node getNode(Long id) {
        return nodeRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void create(Node node) {
        nodeRepository.save(node);
    }

    @Override
    @Transactional
    public boolean update(Node node, Long id) {
        nodeRepository.deleteById(id);
        try {
            nodeRepository.save(node);
        } catch (IllegalArgumentException ex) {
            return false;
        }
        return true;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        nodeRepository.deleteById(id);
    }

    public List<Node> getNodesIntoArea(Float lat, Float lon, Integer distance) {
        return nodeRepository.getNodesByDistance(lat, lon, distance);
    }

    public List<Node> getNodesByAmenity(Float lat, Float lon, String amenity, Integer distance) {
        return nodeRepository.getNodesByAmenity(lat, lon, amenity, distance);
    }
}
