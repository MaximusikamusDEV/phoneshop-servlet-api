package com.es.phoneshop.web;

import com.es.phoneshop.model.product.HashMapProductDao;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.model.product.ProductNotFoundException;
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

    private HashMapProductDao productDao;

    private ProductDetailsPageServlet servlet;

    @Before
    public void setup() throws ServletException {
        servlet = new ProductDetailsPageServlet();
        productDao = HashMapProductDao.getInstance();
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

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("exception"), any(ProductNotFoundException.class));
        verify(request).getRequestDispatcher("/WEB-INF/pages/errorProductNotFound.jsp");
        verify(requestDispatcher).forward(request, response);
        verify(request, never()).setAttribute(eq("product"), any(Product.class));
        verify(request, never()).getRequestDispatcher("/WEB-INF/pages/productDetails.jsp");
    }

}