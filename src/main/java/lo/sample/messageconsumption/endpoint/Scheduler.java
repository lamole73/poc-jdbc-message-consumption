package lo.sample.messageconsumption.endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class Scheduler {

    private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);

    // Just uncomment whichever implementation you like.
//    @Autowired
//    lo.sample.messageconsumption.JDBCRepository2 repository;
    @Autowired
    lo.sample.messageconsumption.JDBCRepository3 repository;

    @Scheduled(fixedRate = 5000)
    public void execute1() {
        logger.info("[{}] [thread: {}] START at time: {}", "execute1", Thread.currentThread().getName(), LocalDateTime.now());
        repository.runIt("execute1");
        logger.info("[{}] [thread: {}] DONE  at time: {}", "execute1", Thread.currentThread().getName(), LocalDateTime.now());
    }

    @Scheduled(fixedRate = 5000)
    public void execute2() {
        logger.info("[{}] [thread: {}] START at time: {}", "execute2", Thread.currentThread().getName(), LocalDateTime.now());
        repository.runIt("execute2");
        logger.info("[{}] [thread: {}] DONE  at time: {}", "execute2", Thread.currentThread().getName(), LocalDateTime.now());
    }

    @Scheduled(fixedRate = 5000)
    public void execute3() {
        logger.info("[{}] [thread: {}] START at time: {}", "execute3", Thread.currentThread().getName(), LocalDateTime.now());
        repository.runIt("execute2");
        logger.info("[{}] [thread: {}] DONE  at time: {}", "execute3", Thread.currentThread().getName(), LocalDateTime.now());
    }
}
