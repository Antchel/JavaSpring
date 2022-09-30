package com.agolovenko.jspring.OSM.REST.Controller;

import com.agolovenko.jspring.OSM.REST.Service.NodeService;
import com.agolovenko.jspring.OSM.REST.persist.Node;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/nodes")
public class NodeController {
    NodeService nodeService;

    @Autowired
    public NodeController(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @GetMapping()
    ResponseEntity<?> get(@RequestParam(name = "lat") Float lat,
                          @RequestParam(name = "lon") Float lon,
                          @RequestParam(name = "radix") Integer radix) {
        List<Node> nodes = nodeService.getNodesIntoArea(lat, lon, radix);
        if (!nodes.isEmpty())
            return new ResponseEntity<>(nodes, HttpStatus.OK);
        else
            return new ResponseEntity<>("Nodes near point {" + lat + "," + lon + "} not found.", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/")
    ResponseEntity<?> get(@RequestParam(name = "lat") Float lat,
                          @RequestParam(name = "lon") Float lon,
                          @RequestParam(name = "amenity") String amenity,
                          @RequestParam(name = "distance") Integer distance) {
        List<Node> nodes = nodeService.getNodesByAmenity(lat, lon, amenity, distance);
        if (!nodes.isEmpty())
            return new ResponseEntity<>(nodes, HttpStatus.OK);
        else
            return new ResponseEntity<>("Nodes near point {" + lat + "," + lon + "} with " + amenity + "not found.", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{id}")
    ResponseEntity<?> get(@PathVariable(value = "id") Long id) {
        log.info("/nodes get with id == {} ", id);
        Node node = nodeService.getNode(id);
        if (node == null) {
            return new ResponseEntity<>("Node with id: " + id + " not found.", HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(node, HttpStatus.OK);
        }
    }

    @PostMapping()
    public ResponseEntity<?> create(@RequestBody Node node) {
        nodeService.create(node);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<?> update(@PathVariable(name = "id") Long id, @RequestBody Node node) {
        final boolean updated = nodeService.update(node, id);

        return updated
                ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> delete(@PathVariable(name = "id") Long id) {
        nodeService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);

    }
}
