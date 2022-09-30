package com.agolovenko.jspring.OSM.DB.DAO;

import com.agolovenko.jspring.OSM.DB.NodeServiceException;
import com.agolovenko.jspring.OSM.TimeTrack.TimeTracker;
import com.agolovenko.jspring.OSM.Util.Reference;
import com.agolovenko.jspring.osmjaxbclasses.Node;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
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

    @SneakyThrows
    public NodeBatchServiceDAO(@Qualifier("userDataSource") DataSource dataSource) {
        this.dataSource = dataSource;
        con = getConnection();
        try {
            con.setAutoCommit(false);
            st = con.prepareStatement("insert into node (node_id, lat, lon, tags) " +
                    "values (?,?,?,to_json(?::json))");
        } catch (SQLException e) {
            con.rollback();
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    @Override
    @TimeTracker
    public void createNode(Node nodeInfo) throws NodeServiceException {
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
                con.rollback();
                throw new RuntimeException(e);
            }
            throw new NodeServiceException("Can't create node", sqlex);
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
