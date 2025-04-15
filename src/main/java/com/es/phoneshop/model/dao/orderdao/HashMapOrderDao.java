package com.es.phoneshop.model.dao.orderdao;

import com.es.phoneshop.exceptions.OrderNotFoundException;
import com.es.phoneshop.model.dao.abstractdao.AbstractMapDao;
import com.es.phoneshop.model.order.Order;
import java.util.UUID;

public class HashMapOrderDao extends AbstractMapDao<String, Order, OrderNotFoundException> implements OrderDao{
    private static final String ORDER_NOT_FOUND_GET_MESSAGE = "Order with id {} not found. While getOrder";

    private HashMapOrderDao() {
        super();
    }

    private static class SingletonHolder {
        private static final HashMapOrderDao INSTANCE = new HashMapOrderDao();
    }

    public static HashMapOrderDao getInstance(){
        return SingletonHolder.INSTANCE;
    }

    @Override
    public Order getOrder(Long id) throws OrderNotFoundException {
        return get(id.toString(), ORDER_NOT_FOUND_GET_MESSAGE);
    }

    @Override
    public Order getOrderBySecureId(String secureId) throws OrderNotFoundException {
        return get(secureId, ORDER_NOT_FOUND_GET_MESSAGE);
    }

    @Override
    public void saveWithSecureId(Order order) {
        lock.writeLock().lock();

        try {
            if (order.getSecureId() == null) {
                String secureId = UUID.randomUUID().toString();
                order.setSecureId(secureId);
                order.setId(maxId.getAndIncrement());
                dataMap.put(secureId, order);
            } else {
                dataMap.put(order.getSecureId(), order);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    protected OrderNotFoundException createException(String message, String key) throws OrderNotFoundException {
        return new OrderNotFoundException(key);
    }
}
