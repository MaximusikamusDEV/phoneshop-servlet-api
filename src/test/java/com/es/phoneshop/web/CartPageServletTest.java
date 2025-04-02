package com.es.phoneshop.web;

import com.es.phoneshop.cart.Cart;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CartPageServletTest {
    private static final String ATTRIBUTE_CART_ITEMS = "cartItems";
    private static final String ATTRIBUTE_CART = "cart";
    private static final String PARAM_VALUES_PRODUCT_ID = "productId";
    private static final String PARAM_VALUES_QUANTITY = "quantity";
    private static final String ATTRIBUTE_ERRORS = "errors";
    private CartPageServlet servlet;

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

    @Before
    public void setup() throws ServletException {
        servlet = new CartPageServlet();
        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
        when(request.getLocale()).thenReturn(Locale.US);

        servlet.init(servletConfig);
    }

    @Test
    public void testDoGet() throws ServletException, IOException {
        servlet.doGet(request, response);

        verify(request).setAttribute(eq(ATTRIBUTE_CART_ITEMS), any(List.class));
        verify(request).setAttribute(eq(ATTRIBUTE_CART), any(Cart.class));
        verify(request).getRequestDispatcher(eq("/WEB-INF/pages/cart.jsp"));
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    public void testDoPostFail() throws ServletException, IOException{
        String[] productIds = {"1", "2"};
        String[] quantities = {"5", "3"};

        when(request.getParameterValues(PARAM_VALUES_PRODUCT_ID)).thenReturn(productIds);
        when(request.getParameterValues(PARAM_VALUES_QUANTITY)).thenReturn(quantities);
        when(request.getLocale()).thenReturn(Locale.US);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq(ATTRIBUTE_ERRORS), any(Map.class));
        verify(requestDispatcher).forward(request, response);
    }
}