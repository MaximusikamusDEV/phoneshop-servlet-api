package com.es.phoneshop.model.dao.abstractdao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AbstractMapDao<K, V, E extends Exception> {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractMapDao.class);
    protected final AtomicLong maxId;
    protected final Map<K, V> dataMap;
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    protected AbstractMapDao() {
        this.dataMap = new HashMap<>();
        maxId = new AtomicLong(0);
    }

    protected V get(K key, String notFoundMessage) throws E {
        lock.readLock().lock();
        try {
            V value = dataMap.get(key);
            if (value == null) {
                logger.error(notFoundMessage, key);
                throw createException(notFoundMessage, key);
            }
            return value;
        } finally {
            lock.readLock().unlock();
        }
    }

    protected abstract E createException(String message, K key) throws E;
}
