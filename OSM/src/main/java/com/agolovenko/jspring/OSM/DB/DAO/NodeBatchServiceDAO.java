package com.agolovenko.jspring.OSM.DB.DAO;

import com.agolovenko.jspring.OSM.DB.UserServiceException;
import com.agolovenko.jspring.OSM.TimeTrack.TimeTracker;
import com.agolovenko.jspring.OSM.Util.Reference;
import com.agolovenko.jspring.osmjaxbclasses.Node;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Repository("batchUserService")
@ConditionalOnProperty(
        value="dao.class",
        havingValue = "batch"
)
public class NodeBatchServiceDAO extends AbstractNodeServiceDAO implements AutoCloseable{
    private final DataSource dataSource;
    private final Connection con;
    PreparedStatement st;
    private int count = 0;

    public NodeBatchServiceDAO(@Qualifier("userDataSource") DataSource dataSource) {
        this.dataSource = dataSource;
        try {
            con = getConnection();
            con.setAutoCommit(false);
            st = con.prepareStatement("insert into node (node_id, lat, lon, tags) " +
                    "values (?,?,?,to_json(?::json))");
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

            if (count < Reference.TRANSACT_SIZE) {
                st.addBatch();
                count++;
            } else {
                count = 0;
                st.executeBatch();
                con.commit();
            }


        } catch (SQLException | JsonProcessingException sqlex) {
            try {
                if (con != null)
                    con.rollback();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            throw new UserServiceException("Can't create node", sqlex);
        }
    }

    protected Connection getConnection() throws SQLException {
        return DataSourceUtils.getConnection(dataSource);
    }

    @Override
    public void close() throws Exception {
        st.executeBatch();
        st.close();
        con.commit();
        con.close();
    }
}
