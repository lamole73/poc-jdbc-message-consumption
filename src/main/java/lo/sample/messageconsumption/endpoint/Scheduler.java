package lo.sample.messageconsumption.endpoint;

import lo.sample.messageconsumption.JDBCRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class Scheduler {

    private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);

    @Autowired
    JDBCRepository repository;

    @Scheduled(fixedRate = 5000)
    public void execute1() {
        logger.info("[thread: {}] execute1 START at time: {}", Thread.currentThread().getName(), LocalDateTime.now());
        repository.runIt();
        logger.info("[thread: {}] execute1 DONE  at time: {}", Thread.currentThread().getName(), LocalDateTime.now());
    }

    @Scheduled(fixedRate = 5000)
    public void execute2() {
        logger.info("[thread: {}] execute2 START at time: {}", Thread.currentThread().getName(), LocalDateTime.now());
        repository.runIt();
        logger.info("[thread: {}] execute2 DONE  at time: {}", Thread.currentThread().getName(), LocalDateTime.now());
    }
}
