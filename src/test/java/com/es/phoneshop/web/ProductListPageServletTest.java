package com.es.phoneshop.web;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductListPageServletTest {
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

    private ProductListPageServlet servlet;

    @Before
    public void setup() throws ServletException {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(anyString())).thenReturn(null);
        servlet = new ProductListPageServlet();
        servlet.init(servletConfig);
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
    }

    @Test
    public void testDoGetWithProducts() throws ServletException, IOException {
        servlet.doGet(request, response);

        verify(request).setAttribute(eq("products"), anyList());
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    public void testDoPost() throws ServletException, IOException {
        when(request.getParameter("action")).thenReturn("1");
        when(request.getParameterValues("productId")).thenReturn(new String[]{"1", "2"});
        when(request.getParameterValues("quantity")).thenReturn(new String[]{"2", "3"});
        when(request.getLocale()).thenReturn(Locale.ENGLISH);

        servlet.doPost(request, response);

        verify(response).sendRedirect(contains("/products?message=Product added to cart successfully"));
    }
}