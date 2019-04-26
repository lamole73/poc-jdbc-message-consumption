package lo.sample.messageconsumption;

import lo.sample.messageconsumption.endpoint.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JDBCRepository3 {

    private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);

    @Value("${sample.jdbc.delay}")
    private Long sampleJdbcDelay;

    @Autowired
    private DataSource dataSource;

    public void runIt(final String runningAppInstance) {
        String sqlQuery = "SELECT MSG_ID, MSG_STATUS, MSG_CONTENT, PROC_CONTENT, PROC_APP, PROC_TIME, VENDOR_ID " +
                "FROM MSG_DATA WHERE MSG_STATUS = 1 FOR UPDATE SKIP LOCKED";

        Integer msgId = null;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);) {

            logger.info("[{}] [thread: {}] Started msgId not known yet", runningAppInstance, Thread.currentThread().getName());

            // Set MaxRows to 1 for the PreparedStatement
            statement.setMaxRows(1);

            List<Integer> msgIdsProcessed = new ArrayList<>();
            int rowsProcessedCount = 0;
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                rowsProcessedCount++;
                msgId = resultSet.getInt("MSG_ID");
                msgIdsProcessed.add(msgId);
                logger.info("[{}] [thread: {}] Processing msgId={}", runningAppInstance, Thread.currentThread().getName(), msgId);
                resultSet.updateInt("MSG_STATUS", 2);
                resultSet.updateInt("VENDOR_ID", resultSet.getInt("VENDOR_ID") + 1);
                resultSet.updateString("PROC_CONTENT", Thread.currentThread().getName());
                resultSet.updateString("PROC_APP", runningAppInstance + " / thread=" + Thread.currentThread().getName());
                resultSet.updateTimestamp("PROC_TIME", Timestamp.from(Instant.now()));

                // TODO Just wait for testing
                if (null != sampleJdbcDelay) {
                    try {
                        Thread.sleep(sampleJdbcDelay);
                    } catch (InterruptedException e) {
                        // OK
                    }
                }

                resultSet.updateRow();  //flushing all the updates to the table and release the row lock
            }

            // If not processed at least 1 row then just log
            if (rowsProcessedCount == 0) {
                logger.info("[{}] [thread: {}] No row found", runningAppInstance, Thread.currentThread().getName());
            } else {
                logger.info("[{}] [thread: {}] processed {} records with ids={}", runningAppInstance, Thread.currentThread().getName(), rowsProcessedCount, msgIdsProcessed);
            }


        } catch (SQLException e) {
            logger.info("[{}] [thread: {}] Failed with error: {}", runningAppInstance, Thread.currentThread().getName(), e.getMessage());
            throw new RuntimeException(e);
        }

        logger.info("[{}] [thread: {}] Ended msgId={}", runningAppInstance, Thread.currentThread().getName(), msgId);

    }
}