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

import java.sql.*;
import java.time.Instant;
import java.util.List;

@Repository
public class JDBCRepository1 {

    private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);

    @Value("${sample.jdbc.delay}")
    private Long sampleJdbcDelay;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void runIt(final String runningAppInstance) {
        List<Long> msgIds;
        try {
            msgIds = jdbcTemplate.queryForList("SELECT MSG_ID FROM MSG_DATA WHERE MSG_STATUS = 1", Long.class);

        } catch (Exception e) {
            // handle exception
            return;
        }

        if (msgIds.isEmpty()) {
            // Create required log if required
            return;
        }

        for (final Long msgId : msgIds) {
            try {
                // row lock
                PreparedStatementCreatorFactory pscf = new PreparedStatementCreatorFactory(
                        "SELECT MSG_ID , MSG_STATUS, MSG_CONTENT, PROC_CONTENT, PROC_TIME, VENDOR_ID " +
                                "FROM MSG_DATA WHERE MSG_ID = ? AND MSG_STATUS = 1 FOR UPDATE SKIP LOCKED",
                        new int[]{Types.INTEGER});

                pscf.setUpdatableResults(true);
                pscf.setResultSetType(ResultSet.TYPE_FORWARD_ONLY);

                RowCallbackHandler rch = new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet resultSet) throws SQLException {
                        try {
                            logger.info("[{}] [thread: {}] Processing msgId={}", runningAppInstance, Thread.currentThread().getName(), msgId);
                            resultSet.updateInt("MSG_STATUS", 2);
                            resultSet.updateInt("VENDOR_ID", resultSet.getInt("VENDOR_ID") + 1);
                            resultSet.updateString("PROC_CONTENT", Thread.currentThread().getName());
                            resultSet.updateString("PROC_APP", runningAppInstance + " / thread=" + Thread.currentThread().getName());
                            resultSet.updateTimestamp("PROC_TIME", Timestamp.from(Instant.now()));

                            // TODO Just wait for testing
                            if (null != sampleJdbcDelay) {
                                Thread.sleep(sampleJdbcDelay);
                            }

                            resultSet.updateInt("MSG_ID", resultSet.getInt("MSG_ID"));  // Mock update to make sure resultSet is always an updated one
                            resultSet.updateRow();  //flushing all the updates to the table and release the row lock
                            logger.info("[{}] [thread: {}] Processed msgId={}", runningAppInstance, Thread.currentThread().getName(), msgId);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                };

                jdbcTemplate.setMaxRows(1);
                logger.info("[{}] [thread: {}] Started msgId={}", runningAppInstance, Thread.currentThread().getName(), msgId);
                jdbcTemplate.query(pscf.newPreparedStatementCreator(new Object[]{msgId}), rch);
                // If processed 1 row break
                logger.info("[{}] [thread: {}] Ended msgId={}", runningAppInstance, Thread.currentThread().getName(), msgId);

                break;

            } catch (Exception e) {
                // Handle the exception
                logger.info("[{}] [thread: {}] Failed on msgId={} with error: {}", runningAppInstance, Thread.currentThread().getName(), msgId, e.getMessage());
//                e.printStackTrace();
                return;
            }
        }
    }
}
