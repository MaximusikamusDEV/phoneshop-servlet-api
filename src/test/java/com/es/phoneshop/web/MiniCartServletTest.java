package com.es.phoneshop.web;

import com.es.phoneshop.model.cart.Cart;
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
import java.util.Currency;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class MiniCartServletTest {
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
    private MiniCartServlet servlet;

    @Before
    public void setup() throws ServletException {
        servlet = new MiniCartServlet();
        servlet.init(servletConfig);
        when(request.getSession()).thenReturn(session);

        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
    }

    /*@Test
    public void testInit() throws ServletException {
        when(DefaultCartService.getInstance()).thenReturn(null);

        servlet.init();
    }*/

    @Test
    public void testDoGet() throws ServletException, IOException {
        servlet.doGet(request, response);

        verify(request).setAttribute(eq("cart"), any(Cart.class));
        verify(request).setAttribute(eq("currencySymbol"),
                eq(Currency.getInstance("USD").getSymbol()));
        verify(requestDispatcher).include(request, response);
    }
}