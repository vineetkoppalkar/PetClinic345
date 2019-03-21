package org.springframework.samples.petclinic.migration;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ForkliftRunner {

    @PostConstruct
    public void init() {
        Thread migration = new Thread(new Forklift());
        migration.setPriority(Thread.MIN_PRIORITY);
        migration.start();
    }
}
