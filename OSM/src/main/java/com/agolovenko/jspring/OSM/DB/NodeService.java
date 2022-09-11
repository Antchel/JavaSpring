package com.agolovenko.jspring.OSM.DB;

import com.agolovenko.jspring.osmjaxbclasses.Node;

public interface NodeService extends AutoCloseable {
    default NodeInfo getNode(Long id) throws UserServiceException {
        throw new RuntimeException("Operation not implemented");
    }

    default NodeInfo findByNodeId(String userName) throws UserServiceException {
        throw new RuntimeException("Operation not implemented");
    }

    default void createNode(Node userInfo) throws UserServiceException {
        throw new RuntimeException("Operation not implemented");
    }

    default void updateNode(NodeInfo userInfo) throws UserServiceException {
        throw new RuntimeException("Operation not implemented");
    }

    default void deleteNode(NodeInfo userInfo) throws UserServiceException {
        throw new RuntimeException("Operation not implemented");
    }

    @Override
    default void close() throws Exception {
        throw new RuntimeException("Operation not implemented");
    }
}
