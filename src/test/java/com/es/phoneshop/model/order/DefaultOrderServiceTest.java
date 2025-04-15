package com.es.phoneshop.model.order;

import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.model.cart.CartItem;
import com.es.phoneshop.model.dao.orderdao.HashMapOrderDao;
import com.es.phoneshop.model.dao.orderdao.OrderDao;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.sortenums.PaymentMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class DefaultOrderServiceTest {
    private DefaultOrderService orderService;
    private OrderDao orderDao;
    private Cart cart;
    private Product testProduct1;
    private Product testProduct2;

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        orderService = DefaultOrderService.getInstance();
        orderDao = HashMapOrderDao.getInstance();

        cart = new Cart();
        cart.getCartItems().add(new CartItem(testProduct1, 2));
        cart.getCartItems().add(new CartItem(testProduct2, 1));
        cart.setTotalPrice(new BigDecimal(400));
    }

    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        Field orderDaoDataMapField = HashMapOrderDao.class.getSuperclass().getDeclaredField("dataMap");
        orderDaoDataMapField.setAccessible(true);
        ((Map<String, Order>) orderDaoDataMapField.get(orderDao)).clear();

        Field orderDaoMaxIdField = HashMapOrderDao.class.getSuperclass().getDeclaredField("maxId");
        orderDaoMaxIdField.setAccessible(true);
        orderDaoMaxIdField.set(orderDao, new AtomicLong(0));
    }

    @Test
    public void testGetOrder() {
        Order order = orderService.getOrder(cart);

        assertNotNull(order);
        assertEquals(2, order.getCartItems().size());
        assertEquals(new BigDecimal(400), order.getSubtotal());
        assertEquals(new BigDecimal(5), order.getDeliveryCost());
        assertEquals(new BigDecimal(405), order.getTotalPrice());

        assertNotSame(cart.getCartItems().get(0), order.getCartItems().get(0));
        assertEquals(cart.getCartItems().get(0).getProduct(), order.getCartItems().get(0).getProduct());
        assertEquals(cart.getCartItems().get(0).getQuantity(), order.getCartItems().get(0).getQuantity());
    }

    @Test
    public void testGetPaymentMethods() {
        List<PaymentMethod> paymentMethods = orderService.getPaymentMethods();

        assertNotNull(paymentMethods);
        assertEquals(PaymentMethod.values().length, paymentMethods.size());
        assertTrue(paymentMethods.containsAll(Arrays.asList(PaymentMethod.values())));
    }

    @Test
    public void testPlaceOrder() throws Exception {
        Order order = orderService.getOrder(cart);
        order.setFirstName("Av");
        order.setLastName("Ab");
        order.setPhone("1234567890");
        order.setDeliveryAddress("Vv");
        order.setDeliveryDate(java.time.LocalDate.now().plusDays(1));
        order.setPaymentMethod(PaymentMethod.CASH);

        orderService.placeOrder(order);

        Order savedOrder = orderDao.getOrderBySecureId(order.getSecureId());
        assertNotNull(savedOrder);
        assertEquals(order.getSubtotal(), savedOrder.getSubtotal());
        assertEquals(order.getDeliveryCost(), savedOrder.getDeliveryCost());
        assertEquals(order.getTotalPrice(), savedOrder.getTotalPrice());
        assertEquals("Av", savedOrder.getFirstName());
    }
}