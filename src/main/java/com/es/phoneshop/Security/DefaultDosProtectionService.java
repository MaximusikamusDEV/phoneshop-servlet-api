package com.es.phoneshop.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DefaultDosProtectionService implements DosProtectionService {
    private static final String MANY_REQ_MESSAGE = "Somebody made more than 20 requests";
    private static final int ONE_MINUTE_IN_SEC = 60;
    private static final Long THRESHOLD = 20L;
    private static final Logger logger = LoggerFactory.getLogger(DefaultDosProtectionService.class);
    private Map<String, Long> countMap = new ConcurrentHashMap<>();
    private Map<String, Instant> requestTimeMap = new ConcurrentHashMap<>();

    private static class SingletonHolder {
        private static final DefaultDosProtectionService INSTANCE = new DefaultDosProtectionService();
    }

    public static DefaultDosProtectionService getInstance() {
        return DefaultDosProtectionService.SingletonHolder.INSTANCE;
    }

    public DefaultDosProtectionService() {
        dataMapsCleanup();
    }

    @Override
    public boolean isAllowed(String ip) {
        Instant now = Instant.now();
        requestTimeMap.putIfAbsent(ip, now);

        if(requestTimeMap.get(ip).isBefore(now.minusSeconds(ONE_MINUTE_IN_SEC))){
            countMap.put(ip, 1L);
            requestTimeMap.put(ip, now);
        }

        Long count = countMap.get(ip);

        if (count == null) {
            count = 1L;
        } else {
            if(count >= THRESHOLD) {
                logger.warn(MANY_REQ_MESSAGE + "{}", ip);
                return false;
            }
            count++;
       }
        countMap.put(ip, count);
        return true;
    }

    private void dataMapsCleanup(){
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(()->{
            synchronized (this){
                countMap.clear();
                requestTimeMap.clear();
            }
        }, 0, 1, TimeUnit.HOURS);
    }
}

