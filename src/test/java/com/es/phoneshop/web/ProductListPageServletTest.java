package com.es.phoneshop.web;

import com.es.phoneshop.model.product.ArrayListProductDao;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.model.product.ProductNotFoundException;
import jakarta.servlet.ServletConfig;
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
import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
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
    private ArrayListProductDao productDao;

    private ProductListPageServlet servlet;

    @Before
    public void setup() throws ServletException {
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
    public void testDoGetWithoutProducts() throws ServletException, IOException, NoSuchFieldException, IllegalAccessException, ProductNotFoundException {
        Field productDaoInServlet = ProductListPageServlet.class.getDeclaredField("productDao");
        productDaoInServlet.setAccessible(true);
        productDaoInServlet.set(servlet, productDao);

        when(productDao.findProducts()).thenThrow(new ProductNotFoundException());
        servlet.doGet(request, response);
        verify(request).setAttribute(eq("products"), eq(new ArrayList<Product>()));
    }
}