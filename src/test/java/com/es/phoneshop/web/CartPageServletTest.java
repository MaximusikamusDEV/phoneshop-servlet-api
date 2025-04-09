package com.es.phoneshop.web;

import com.es.phoneshop.exceptions.ProductNotFoundException;
import com.es.phoneshop.exceptions.ProductOutOfStockException;
import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.model.cart.CartItem;
import com.es.phoneshop.model.cart.DefaultCartService;
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
import java.util.HashMap;
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
    private static final String ATTRIBUTE_ERRORS_SESSION = "errorsCart";
    private static final String ATTRIBUTE_QUANTITIES_SESSION = "quantitiesCart";
    private static final String PARAM_VALUES_INPUT_QUANTITIES = "inputQuantities";
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
    @Mock
    private DefaultCartService cartService;
    @Mock
    private Cart cart;
    @Mock
    private List<CartItem> cartItems;

    @Before
    public void setup() throws ServletException, NoSuchFieldException, IllegalAccessException {
        servlet = new CartPageServlet();
        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
        when(request.getLocale()).thenReturn(Locale.US);

        servlet.init(servletConfig);

        Field cartServiceField = CartPageServlet.class.getDeclaredField("cartService");
        cartServiceField.setAccessible(true);
        cartServiceField.set(servlet, cartService);

        when(cartService.getCart(request)).thenReturn(cart);
        when(cart.getCartItems()).thenReturn(cartItems);
    }

    @Test
    public void testDoGet() throws ServletException, IOException {
        Map<Long, String> errors = new HashMap<>();
        errors.put(2L, "Quantity is not a number");
        String[] inputQuantities = new String[]{"1", "abc", "3"};
        when(session.getAttribute(ATTRIBUTE_ERRORS_SESSION)).thenReturn(errors);
        when(session.getAttribute(ATTRIBUTE_QUANTITIES_SESSION)).thenReturn(inputQuantities);

        servlet.doGet(request, response);

        verify(request).setAttribute(ATTRIBUTE_CART_ITEMS, cartItems);
        verify(request).setAttribute(ATTRIBUTE_CART, cart);
        verify(request).setAttribute(ATTRIBUTE_ERRORS, errors);
        verify(request).setAttribute(PARAM_VALUES_INPUT_QUANTITIES, inputQuantities);
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    public void testDoPostFail() throws ServletException, IOException, ProductOutOfStockException, ProductNotFoundException {
        String[] productIds = {"1", "2", "3"};
        String[] quantities = {"5", "abc", "3"};
        when(request.getParameterValues(PARAM_VALUES_PRODUCT_ID)).thenReturn(productIds);
        when(request.getParameterValues(PARAM_VALUES_QUANTITY)).thenReturn(quantities);
        when(request.getContextPath()).thenReturn("/phoneshop");

        servlet.doPost(request, response);

        Map<Long, String> errors = new HashMap<>();
        errors.put(2L, "Quantity is not a number ");

        verify(session).setAttribute(ATTRIBUTE_ERRORS_SESSION, errors);
        verify(session).setAttribute(ATTRIBUTE_QUANTITIES_SESSION, quantities);
        verify(response).sendRedirect("/phoneshop/cart?error=true");
        verify(cartService).update(cart, 1L, 5);
        verify(cartService).update(cart, 3L, 3);
    }

    @Test
    public void testDoPostSuccess() throws ServletException, IOException, ProductOutOfStockException, ProductNotFoundException {
        String[] productIds = {"1", "2", "3"};
        String[] quantities = {"5", "2", "3"};
        when(request.getParameterValues(PARAM_VALUES_PRODUCT_ID)).thenReturn(productIds);
        when(request.getParameterValues(PARAM_VALUES_QUANTITY)).thenReturn(quantities);
        when(request.getContextPath()).thenReturn("/phoneshop");

        servlet.doPost(request, response);

        verify(session).setAttribute(ATTRIBUTE_QUANTITIES_SESSION, quantities);
        verify(cartService).update(cart, 1L, 5);
        verify(cartService).update(cart, 2L, 2);
        verify(cartService).update(cart, 3L, 3);
        verify(response).sendRedirect("/phoneshop/cart?message=Updated successfully");
    }
}