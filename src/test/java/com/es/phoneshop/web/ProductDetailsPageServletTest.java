package com.es.phoneshop.web;

import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.model.cart.DefaultCartService;
import com.es.phoneshop.model.dao.productdao.HashMapProductDao;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.exceptions.ProductNotFoundException;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ProductDetailsPageServletTest {
    private static final String ATTRIBUTE_ERRORS_SESSION = "errorsProductDetails";
    private static final String QUANTITY_NOT_NUMBER_MESSAGE = "Quantity is not a number ";
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
    @Mock
    private DefaultCartService cartService;
    @Mock
    private Cart cart;

    private ProductDetailsPageServlet servlet;

    @Before
    public void setup() throws ServletException, NoSuchFieldException, IllegalAccessException {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(anyString())).thenReturn(null);

        servlet = new ProductDetailsPageServlet();
        productDao = HashMapProductDao.getInstance();
        productDao.save(new Product(1L, "test-product", "Samsung Galaxy S", new BigDecimal(100), Currency.getInstance("USD"), 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S.jpg"));
        servlet.init(servletConfig);

        Field cartServiceField = ProductDetailsPageServlet.class.getDeclaredField("cartService");
        cartServiceField.setAccessible(true);
        cartServiceField.set(servlet, cartService);

        when(request.getRequestDispatcher("/WEB-INF/pages/productDetails.jsp")).thenReturn(requestDispatcher);
        when(request.getRequestDispatcher("/WEB-INF/pages/errorProductNotFound.jsp")).thenReturn(requestDispatcher);
        when(cartService.getCart(any(HttpServletRequest.class))).thenReturn(cart);
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
    public void testDoGetNumberFormatException() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/abc");
        when(request.getRequestDispatcher(PagePaths.error())).thenReturn(requestDispatcher);

        servlet.doGet(request, response);

        verify(request).getRequestDispatcher(PagePaths.error());
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    public void testDoPostNumberFormatException() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/abc");
        when(request.getRequestDispatcher(PagePaths.error())).thenReturn(requestDispatcher);

        servlet.doPost(request, response);

        verify(request).getRequestDispatcher(PagePaths.error());
        verify(requestDispatcher).forward(request, response);
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
    public void testDoPostSuccess() throws Exception {
        when(request.getPathInfo()).thenReturn("/1");
        when(request.getParameter("quantity")).thenReturn("2");
        when(request.getLocale()).thenReturn(Locale.US);

        servlet.doPost(request, response);

        verify(cartService).add(eq(cart), eq(1L), eq(2));
        verify(response).sendRedirect(contains("/products/1?message=Product Added To Cart"));
        verify(session, never()).setAttribute(eq(ATTRIBUTE_ERRORS_SESSION), anyString());
    }

    @Test
    public void testDoPostParseException() throws ServletException, IOException {
        when(request.getLocale()).thenReturn(Locale.US);
        when(request.getParameter("quantity")).thenReturn("abc");
        when(request.getPathInfo()).thenReturn("/1");

        servlet.doPost(request, response);

        verify(session).setAttribute(ATTRIBUTE_ERRORS_SESSION, QUANTITY_NOT_NUMBER_MESSAGE);
        verify(response).sendRedirect(anyString());
    }
}