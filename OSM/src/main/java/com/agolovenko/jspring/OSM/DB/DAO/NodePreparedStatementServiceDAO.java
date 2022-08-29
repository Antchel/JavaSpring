package com.agolovenko.jspring.OSM.DB.DAO;

import com.agolovenko.jspring.OSM.DB.UserServiceException;
import com.agolovenko.jspring.OSM.TimeTrack.TimeTracker;
import com.agolovenko.jspring.OSM.Util.Reference;
import com.agolovenko.jspring.osmjaxbclasses.Node;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Repository("preparedUserService")
@Slf4j
@ConditionalOnProperty(
        value = "dao.class",
        havingValue = "prepared"
)
public class NodePreparedStatementServiceDAO extends AbstractNodeServiceDAO {
    private final DataSource dataSource;
    private final Connection con;
    PreparedStatement st;
    private int counter = 0;

    public NodePreparedStatementServiceDAO(@Qualifier("userDataSource") DataSource dataSource) {
        this.dataSource = dataSource;
        try {
            con = getConnection();
            con.setAutoCommit(false);
            st = con.prepareStatement("insert into node (node_id, lat, lon, tags) " +
                    "values (?,?,?,?::json)", getKeyColumns());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @TimeTracker
    public void createNode(Node nodeInfo) throws UserServiceException {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            st.setBigDecimal(1, new BigDecimal(nodeInfo.getId()));
            st.setBigDecimal(2, nodeInfo.getLat());
            st.setBigDecimal(3, nodeInfo.getLon());
            st.setString(4, objectMapper.writeValueAsString(nodeInfo.getTag()));
            st.executeUpdate();
            if (++counter > Reference.TRANSACT_SIZE) {
                counter = 0;
                CommitToDB();
            }
        } catch (SQLException sqlex) {
            throw new UserServiceException("Can't create node", sqlex);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            con.commit();
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void CommitToDB() {
        try {
            con.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    protected Connection getConnection() throws SQLException {
        return DataSourceUtils.getConnection(dataSource);
    }
}
