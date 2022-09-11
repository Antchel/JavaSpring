package com.agolovenko.jspring.OSM.DB.DAO;

import com.agolovenko.jspring.OSM.DB.NodeService;

public class AbstractNodeServiceDAO implements NodeService {
    private static final String[] KEY_COLUMNS = {"id"};
    protected String[] getKeyColumns() { return KEY_COLUMNS;}
}