package com.agolovenko.jspring.OSM.DB.DAO;

import com.agolovenko.jspring.OSM.TimeTrack.TimeTracker;
import com.agolovenko.jspring.osmjaxbclasses.Node;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import lombok.SneakyThrows;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;

@Repository("copyService")
@ConditionalOnProperty(
        value="dao.class",
        havingValue = "copy",
        matchIfMissing = true
)
public class NodeCopyServiceDAO extends AbstractNodeServiceDAO{

    private final DataSource dataSource;
    private final Connection con;
    private final CopyManager cpm;
    private final CSVWriter csvWriter;
    private final String TEMP_FILE = "c:\\Work\\table.csv";

    @SneakyThrows
    public NodeCopyServiceDAO(DataSource dataSource) throws IOException {
        this.dataSource = dataSource;
        con = getConnection();
        try {
            cpm = new CopyManager(con.unwrap(BaseConnection.class));
            FileWriter fileWriter = new FileWriter(TEMP_FILE);
            csvWriter = new CSVWriter(fileWriter);
        } catch (SQLException e) {
            con.rollback();
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    @Override
    @TimeTracker
    public void createNode(Node nodeInfo) {
        String jsonString;
        try {
            // To avoid encoding text used StringWriter + writeValue(sw, nodeInfo.getTag())
            StringWriter sw = new StringWriter();
            new ObjectMapper().writer().writeValue(sw, nodeInfo.getTag());
            jsonString = sw.toString();
        } catch (IOException e) {
            con.rollback();
            throw new RuntimeException(e);
        }

        csvWriter.writeNext(new String[]{nodeInfo.getId().toString(), nodeInfo.getLat().toString(), nodeInfo.getLon().toString(), jsonString});

    }

    protected Connection getConnection() throws SQLException {
        return DataSourceUtils.getConnection(dataSource);
    }

    @Override
    public void close() throws Exception {
        csvWriter.close();
        FileReader fileReader = new FileReader(TEMP_FILE);
        cpm.copyIn("COPY node (node_id, lat, lon, tags) FROM STDIN with csv", fileReader );
        fileReader.close();
        new File(TEMP_FILE).delete();
        con.close();
    }
}
