package com.es.phoneshop.model.dao.orderdao;

import com.es.phoneshop.exceptions.OrderNotFoundException;
import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.model.cart.CartItem;
import com.es.phoneshop.model.order.Order;
import com.es.phoneshop.model.product.Product;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.Assert.*;

public class HashMapOrderDaoTest {
    private HashMapOrderDao orderDao;
    private Order testOrder;
    private Cart cart;
    private Product testProduct;
    private Currency usd;

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        orderDao = HashMapOrderDao.getInstance();
        usd = Currency.getInstance("USD");

        testProduct = new Product(1L, "test-product", "Samsung Galaxy S", new BigDecimal(100), usd, 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S.jpg");
        cart = new Cart();
        cart.getCartItems().add(new CartItem(testProduct, 1));
        cart.setTotalPrice(new BigDecimal(100));

        testOrder = new Order();
        testOrder.setCartItems(cart.getCartItems());
        testOrder.setSubtotal(new BigDecimal(100));
        testOrder.setDeliveryCost(new BigDecimal(5));
        testOrder.setTotalPrice(new BigDecimal(105));
    }

    @After
    public void clear() throws NoSuchFieldException, IllegalAccessException {
        Field orderDaoDataMapField = HashMapOrderDao.class.getSuperclass().getDeclaredField("dataMap");
        orderDaoDataMapField.setAccessible(true);
        ((Map<String, Order>) orderDaoDataMapField.get(orderDao)).clear();

        Field orderDaoMaxIdField = HashMapOrderDao.class.getSuperclass().getDeclaredField("maxId");
        orderDaoMaxIdField.setAccessible(true);
        orderDaoMaxIdField.set(orderDao, new AtomicLong(0));
    }

    @Test
    public void testSaveNewOrder() {
        orderDao.saveWithSecureId(testOrder);

        assertNotNull(testOrder.getSecureId());
        assertTrue(testOrder.getId() >= 0);
    }

    @Test
    public void testGetOrderBySecureId() throws OrderNotFoundException {
        orderDao.saveWithSecureId(testOrder);

        Order retrievedOrder = orderDao.getOrderBySecureId(testOrder.getSecureId());

        assertNotNull(retrievedOrder);
        assertEquals(testOrder.getId(), retrievedOrder.getId());
        assertEquals(testOrder.getSubtotal(), retrievedOrder.getSubtotal());
    }

    @Test(expected = OrderNotFoundException.class)
    public void testGetOrderWithIncorrectId() throws OrderNotFoundException {
        orderDao.getOrder(999L);
    }

    @Test(expected = OrderNotFoundException.class)
    public void testGetOrderBySecureIdWithIncorrectId() throws OrderNotFoundException {
        orderDao.getOrderBySecureId(UUID.randomUUID().toString());
    }

    @Test
    public void testUpdateOrder() throws OrderNotFoundException {
        orderDao.saveWithSecureId(testOrder);
        String originalSecureId = testOrder.getSecureId();

        testOrder.setSubtotal(new BigDecimal(200));
        orderDao.saveWithSecureId(testOrder);

        Order updatedOrder = orderDao.getOrderBySecureId(originalSecureId);

        assertNotNull(updatedOrder);
        assertEquals(new BigDecimal(200), updatedOrder.getSubtotal());
        assertEquals(originalSecureId, updatedOrder.getSecureId());
    }
}