package com.agolovenko.jspring.OSM.DB;

import lombok.Data;

import java.io.Serializable;

@Data
public class NodeInfo implements Serializable {
    private Long  id;
    private Long  nodeId;
    private Float lat;
    private Float lon;
    private String tags;

    public NodeInfo(long id, long node_id, float lat, float lon, String tags) {

    }
}
