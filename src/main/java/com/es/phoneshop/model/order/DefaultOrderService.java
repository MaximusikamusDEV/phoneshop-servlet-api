package com.es.phoneshop.model.order;

import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.model.cart.CartItem;
import com.es.phoneshop.model.dao.orderdao.HashMapOrderDao;
import com.es.phoneshop.model.dao.orderdao.OrderDao;
import com.es.phoneshop.sortenums.PaymentMethod;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class DefaultOrderService implements OrderService {
    private final OrderDao orderDao = HashMapOrderDao.getInstance();
    private final static BigDecimal HARDCODED_DELIVERY_COST = new BigDecimal(5);

    private static class SingletonHolder {
        private static final DefaultOrderService INSTANCE = new DefaultOrderService();
    }

    public static DefaultOrderService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public Order getOrder(Cart cart) {
        Order order = new Order();
        order.setCartItems(cart.getCartItems().stream().map(
                cartItem ->{
                    try {
                        return (CartItem) cartItem.clone();
                    } catch (CloneNotSupportedException e) {
                        throw new RuntimeException(e);
                    }
                }
        ).toList());
        order.setSubtotal(cart.getTotalPrice());
        order.setDeliveryCost(calculateDeliveryCost());
        order.setTotalPrice(order.getSubtotal().add(order.getDeliveryCost()));

        return order;
    }

    @Override
    public List<PaymentMethod> getPaymentMethods() {
        return Arrays.asList(PaymentMethod.values());
    }

    private BigDecimal calculateDeliveryCost() {
        return HARDCODED_DELIVERY_COST;
    }

    @Override
    public void placeOrder(Order order) {
        orderDao.saveWithSecureId(order);
    }
}
