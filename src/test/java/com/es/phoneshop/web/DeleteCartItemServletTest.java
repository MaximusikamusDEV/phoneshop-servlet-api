package com.es.phoneshop.web;

import com.es.phoneshop.cart.Cart;
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
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DeleteCartItemServletTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Cart cart;
    @Mock
    private ServletConfig servletConfig;
    @Mock
    HttpSession session;

    private DeleteCartItemServlet servlet;

    @Before
    public void setup() throws ServletException {
        servlet = new DeleteCartItemServlet();
        when(request.getPathInfo()).thenReturn("/1");
        when(request.getContextPath()).thenReturn("/app");
        servlet.init(servletConfig);
    }

    @Test
    public void testDoPost() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/1");
        when(request.getContextPath()).thenReturn("/app");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(anyString())).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect("/app/cart?message=Cart item deleted successfully");
    }
}