package org.springframework.samples.petclinic.migration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class TaskRunner {

    private final int checkingPeriodMillis = 3600000;

    @PostConstruct
    public void init() {
        Thread migration = new Thread(new Forklift());
        migration.setPriority(Thread.MIN_PRIORITY);

        Thread checkFirst = new Thread(new ConsistencyChecker());
        checkFirst.setPriority(Thread.MIN_PRIORITY);

        migration.start();
        try {
            migration.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        checkFirst.start();
    }

    @Scheduled(fixedDelay  = checkingPeriodMillis,initialDelay = checkingPeriodMillis)
    public void scheduledChecker(){
        Thread checker = new Thread(new ConsistencyChecker());
        checker.setPriority(Thread.MIN_PRIORITY);
        checker.start();
    }
}
