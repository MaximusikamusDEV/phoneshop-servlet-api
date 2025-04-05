package com.es.phoneshop.web;

import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.model.cart.CartItem;
import com.es.phoneshop.model.cart.CartService;
import com.es.phoneshop.model.order.Order;
import com.es.phoneshop.model.order.OrderService;
import com.es.phoneshop.sortenums.PaymentMethod;
import com.es.phoneshop.util.PagePaths;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CheckoutPageServletTest {
    private static final String ATTRIBUTE_ORDER_ITEMS = "orderItems";
    private static final String ATTRIBUTE_ORDER = "order";
    private static final String PARAM_PAYMENT_METHODS = "paymentMethods";
    private CheckoutPageServlet servlet;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private RequestDispatcher requestDispatcher;
    @Mock
    private ServletConfig servletConfig;
    @Mock
    private HttpSession session;
    @Mock
    private CartService cartService;
    @Mock
    private OrderService orderService;
    @Mock
    private Cart cart;
    @Mock
    private Order order;
    @Mock
    private List<CartItem> cartItems;
    @Mock
    private List<PaymentMethod> paymentMethods;

    @Before
    public void setup() throws ServletException, NoSuchFieldException, IllegalAccessException {
        servlet = new CheckoutPageServlet();
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
        when(request.getSession()).thenReturn(session);

        servlet.init(servletConfig);

        Field cartServiceField = CheckoutPageServlet.class.getDeclaredField("cartService");
        cartServiceField.setAccessible(true);
        cartServiceField.set(servlet, cartService);

        Field orderServiceField = CheckoutPageServlet.class.getDeclaredField("orderService");
        orderServiceField.setAccessible(true);
        orderServiceField.set(servlet, orderService);
    }

    @Test
    public void testDoGet() throws ServletException, IOException {
        when(cartService.getCart(request)).thenReturn(cart);
        when(orderService.getOrder(cart)).thenReturn(order);
        when(order.getCartItems()).thenReturn(cartItems);
        when(orderService.getPaymentMethods()).thenReturn(paymentMethods);

        servlet.doGet(request, response);

        verify(request).setAttribute(ATTRIBUTE_ORDER_ITEMS, cartItems);
        verify(request).setAttribute(ATTRIBUTE_ORDER, order);
        verify(request).setAttribute(PARAM_PAYMENT_METHODS, paymentMethods);
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    public void testDoPost() throws ServletException, IOException {
        when(cartService.getCart(request)).thenReturn(cart);
        when(orderService.getOrder(cart)).thenReturn(order);
        when(request.getParameter("firstName")).thenReturn("Ab");
        when(request.getParameter("lastName")).thenReturn("Ab");
        when(request.getParameter("phone")).thenReturn("+375336727408");
        when(request.getParameter("deliveryAddress")).thenReturn("Av");
        when(request.getParameter("deliveryDate")).thenReturn("2025-12-31");
        when(request.getParameter("paymentMethod")).thenReturn("CASH");
        when(order.getSecureId()).thenReturn("1");
        when(request.getContextPath()).thenReturn("/app");

        servlet.doPost(request, response);

        verify(orderService).placeOrder(order);
        verify(cartService).clearCart(cart);
        verify(response).sendRedirect("/app/order/overview/1");
    }

    @Test
    public void testDoGetCartNullPointerException() throws ServletException, IOException {
        when(cartService.getCart(request)).thenThrow(new NullPointerException("Cart is null"));

        servlet.doGet(request, response);

        verify(request).getRequestDispatcher(PagePaths.emptyCart());
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    public void testDoGetOrderException() throws ServletException, IOException {
        when(cartService.getCart(request)).thenReturn(cart);
        when(orderService.getOrder(cart)).thenThrow(new RuntimeException());

        servlet.doGet(request, response);

        verify(request).getRequestDispatcher(PagePaths.emptyCart());
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    public void testDoPostCartNullPointerException() throws ServletException, IOException {
        when(cartService.getCart(request)).thenThrow(new NullPointerException("Cart is null"));

        servlet.doPost(request, response);

        verify(request).getRequestDispatcher(PagePaths.emptyCart());
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    public void testDoPostOrderException() throws ServletException, IOException {
        when(cartService.getCart(request)).thenReturn(cart);
        when(orderService.getOrder(cart)).thenThrow(new RuntimeException());

        servlet.doPost(request, response);

        verify(request).getRequestDispatcher(PagePaths.error());
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    public void testDoPostErrorsNotEmpty() throws ServletException, IOException {
        when(cartService.getCart(request)).thenReturn(cart);
        when(orderService.getOrder(cart)).thenReturn(order);
        when(request.getParameter("firstName")).thenReturn("bb");
        when(request.getParameter("lastName")).thenReturn("bb");
        when(request.getParameter("phone")).thenReturn("7408");
        when(request.getParameter("deliveryAddress")).thenReturn("Av");
        when(request.getParameter("deliveryDate")).thenReturn("25-12-31");
        when(request.getParameter("paymentMethod")).thenReturn("CASH");
        when(request.getContextPath()).thenReturn("/app");

        servlet.doPost(request, response);

        verify(response).sendRedirect("/app/checkout?null?error=true");
    }

    @Test
    public void testDoPostPaymentMethod() throws ServletException, IOException {
        when(cartService.getCart(request)).thenReturn(cart);
        when(orderService.getOrder(cart)).thenReturn(order);
        when(request.getParameter("firstName")).thenReturn("bb");
        when(request.getParameter("lastName")).thenReturn("bb");
        when(request.getParameter("phone")).thenReturn("7408");
        when(request.getParameter("deliveryAddress")).thenReturn("Av");
        when(request.getParameter("deliveryDate")).thenReturn("25-12-31");
        when(request.getParameter("paymentMethod")).thenReturn("CASH234423");
        when(request.getContextPath()).thenReturn("/app");

        servlet.doPost(request, response);

        verify(response).sendRedirect("/app/checkout?null?error=true");
    }
}
