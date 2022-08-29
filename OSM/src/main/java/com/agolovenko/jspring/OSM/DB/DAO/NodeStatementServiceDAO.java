package com.agolovenko.jspring.OSM.DB.DAO;

import com.agolovenko.jspring.OSM.DB.UserServiceException;
import com.agolovenko.jspring.OSM.TimeTrack.TimeTracker;
import com.agolovenko.jspring.OSM.Util.Reference;
import com.agolovenko.jspring.osmjaxbclasses.Node;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Repository("simpleUserService")
@Slf4j
@ConditionalOnProperty(
        value = "dao.class",
        havingValue = "simple"
)
public class NodeStatementServiceDAO extends AbstractNodeServiceDAO implements AutoCloseable {
    private final DataSource dataSource;
    private final Connection con;
    private final Statement st;
    private int count = 0;

    public NodeStatementServiceDAO(@Qualifier("userDataSource") DataSource dataSource) {
        this.dataSource = dataSource;
        System.out.println("simple user ctr");
        try {
            con = getConnection();
            con.setAutoCommit(false);
            st = con.createStatement();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @TimeTracker
    public void createNode(Node nodeInfo) throws UserServiceException {
        try {
            StringWriter sw = new StringWriter();
            new ObjectMapper().writer().writeValue(sw, nodeInfo.getTag());
            String tagString = sw.toString().replace("'", "");
            String sql = "insert into node ( node_id, lat, lon, tags) values ( " +
                    nodeInfo.getId().toString() + ", " +
                    nodeInfo.getLat().toString() + ", " +
                    nodeInfo.getLon().toString() + ", " +
                    "'" + tagString + "')";
            st.executeUpdate(sql);


            if (++count > Reference.TRANSACT_SIZE) {
                count = 0;
                con.commit();
            }

        } catch (SQLException | IOException sqlex) {
            throw new UserServiceException("Can't create node", sqlex);
        }
    }

    protected Connection getConnection() throws SQLException {
        return DataSourceUtils.getConnection(dataSource);
    }

    @Override
    public void close() throws Exception {
        st.close();
        con.commit();
        con.close();
    }
}
