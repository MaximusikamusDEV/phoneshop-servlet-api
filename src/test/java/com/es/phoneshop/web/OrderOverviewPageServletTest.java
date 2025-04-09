package com.es.phoneshop.web;

import com.es.phoneshop.exceptions.OrderNotFoundException;
import com.es.phoneshop.model.cart.CartItem;
import com.es.phoneshop.model.dao.orderdao.HashMapOrderDao;
import com.es.phoneshop.model.order.DefaultOrderService;
import com.es.phoneshop.model.order.Order;
import com.es.phoneshop.sortenums.PaymentMethod;
import com.es.phoneshop.util.PagePaths;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrderOverviewPageServletTest {
    private static final String ATTRIBUTE_ORDER_ITEMS = "orderItems";
    private static final String ATTRIBUTE_ORDER = "order";
    private static final String PARAM_PAYMENT_METHODS = "paymentMethods";
    private OrderOverviewPageServlet servlet;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private RequestDispatcher requestDispatcher;
    @Mock
    private ServletConfig servletConfig;
    @Mock
    private HashMapOrderDao orderDao;
    @Mock
    private Order order;
    @Mock
    private List<CartItem> cartItems;
    @Mock
    private List<PaymentMethod> paymentMethods;
    @Mock
    private DefaultOrderService orderService;

    @Before
    public void setup() throws ServletException, NoSuchFieldException, IllegalAccessException {
        servlet = new OrderOverviewPageServlet();
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);

        servlet.init(servletConfig);

        Field orderDaoField = OrderOverviewPageServlet.class.getDeclaredField("orderDao");
        orderDaoField.setAccessible(true);
        orderDaoField.set(servlet, orderDao);

        Field orderServiceField = OrderOverviewPageServlet.class.getDeclaredField("orderService");
        orderServiceField.setAccessible(true);
        orderServiceField.set(servlet, orderService);
    }

    @Test
    public void testDoGet() throws ServletException, IOException, OrderNotFoundException {
        String orderId = "/1";
        when(request.getPathInfo()).thenReturn(orderId);
        when(orderDao.getOrderBySecureId("1")).thenReturn(order);
        when(order.getCartItems()).thenReturn(cartItems);
        when(orderService.getPaymentMethods()).thenReturn(paymentMethods);

        servlet.doGet(request, response);

        verify(request).setAttribute(ATTRIBUTE_ORDER_ITEMS, cartItems);
        verify(request).setAttribute(ATTRIBUTE_ORDER, order);
        verify(request).setAttribute(PARAM_PAYMENT_METHODS, paymentMethods);
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    public void testDoGetParseError() throws ServletException, IOException, OrderNotFoundException {
        String orderId = "/1";
        when(request.getPathInfo()).thenReturn(orderId);
        when(orderDao.getOrderBySecureId("1")).thenThrow(new OrderNotFoundException("1"));

        servlet.doGet(request, response);

        verify(request).getRequestDispatcher(PagePaths.orderNotFound());
        verify(requestDispatcher).forward(request, response);
    }
}