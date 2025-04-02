package com.es.phoneshop.web;

import com.es.phoneshop.productdao.HashMapProductDao;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.exceptions.ProductNotFoundException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ProductDetailsPageServletTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private RequestDispatcher requestDispatcher;
    @Mock
    private ServletConfig servletConfig;
    @Mock
    private Product product;
    @Mock
    private HttpSession session;
    @Mock
    private HashMapProductDao productDao;

    private ProductDetailsPageServlet servlet;

    @Before
    public void setup() throws ServletException {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(anyString())).thenReturn(null);

        servlet = new ProductDetailsPageServlet();
        productDao = HashMapProductDao.getInstance();
        productDao.save(new Product(1L, "test-product", "Samsung Galaxy S", new BigDecimal(100), Currency.getInstance("USD"), 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S.jpg"));
        servlet.init(servletConfig);
        when(request.getRequestDispatcher("/WEB-INF/pages/productDetails.jsp")).thenReturn(requestDispatcher);
        when(request.getRequestDispatcher("/WEB-INF/pages/errorProductNotFound.jsp")).thenReturn(requestDispatcher);
    }

    @Test
    public void testDoGetWithProducts() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/0");
        productDao.save(product);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("product"), any(Product.class));
        verify(request).getRequestDispatcher("/WEB-INF/pages/productDetails.jsp");
        verify(requestDispatcher).forward(request, response);
        verify(request, never()).setAttribute(eq("exception"), any(ProductNotFoundException.class));
        verify(request, never()).getRequestDispatcher("/WEB-INF/pages/errorProductNotFound.jsp");
    }

    @Test
    public void testDoGetWithoutProducts() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/10000");

        ArgumentCaptor<ProductNotFoundException> exceptionCaptor =
                ArgumentCaptor.forClass(ProductNotFoundException.class);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("exception"), exceptionCaptor.capture());
        verify(request).getRequestDispatcher("/WEB-INF/pages/errorProductNotFound.jsp");
        verify(requestDispatcher).forward(request, response);
        verify(request, never()).setAttribute(eq("product"), any(Product.class));
        verify(request, never()).getRequestDispatcher("/WEB-INF/pages/productDetails.jsp");
    }

    @Test
    public void testDoPostParseException() throws ServletException, IOException {
        when(request.getLocale()).thenReturn(Locale.US);
        when(request.getParameter("quantity")).thenReturn("abc");
        when(request.getPathInfo()).thenReturn("/1");

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("error"), eq("Quantity is not a number "));
        verify(request).getRequestDispatcher(anyString());
        verify(requestDispatcher).forward(request, response);
        verify(response, never()).sendRedirect(anyString());
    }
}