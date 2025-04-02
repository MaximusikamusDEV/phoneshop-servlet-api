package com.es.phoneshop.cart;

import com.es.phoneshop.exceptions.ProductNotFoundException;
import com.es.phoneshop.exceptions.ProductOutOfStockException;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.productdao.HashMapProductDao;
import com.es.phoneshop.productdao.ProductDao;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.math.BigDecimal;
import java.util.Currency;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@RunWith(JUnit4.class)
public class DefaultCartServiceTest
{
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpSession session;

    private ProductDao productDao;
    private Product testProduct1;
    private Product testProduct2;
    private Product testProduct3;
    Cart cart;
    DefaultCartService defaultCartService;

    @Before
    public void setup() {
        Currency usd = Currency.getInstance("USD");
        MockitoAnnotations.initMocks(this);
        defaultCartService = DefaultCartService.getInstance();
        productDao = HashMapProductDao.getInstance();
        testProduct1 = new Product(1L, "test-product1", "Samsung Galaxy S", new BigDecimal(100), usd, 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S.jpg");
        testProduct2 = new Product(2L, "test-product2", "Samsung Galaxy S", new BigDecimal(100), usd, 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S.jpg");
        testProduct3 = new Product(3L, "test-product3", "Samsung Galaxy S", new BigDecimal(100), usd, 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S.jpg");
    }

    @Test
    public void testGetCart() {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(anyString())).thenReturn(null);
        Cart cart1 = defaultCartService.getCart(request);

        assertEquals( 0,cart1.getCartItems().size());

        cart = new Cart();
        cart.getCartItems().add(0, new CartItem(testProduct1, 1));
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(anyString())).thenReturn(cart);

        Cart cart2 = defaultCartService.getCart(request);

        assertEquals(1, cart2.getCartItems().size());
    }

    @Test
    public void testAddCartItem() throws ProductOutOfStockException {
        cart = new Cart();
        productDao.save(testProduct1);
        productDao.save(testProduct2);
        productDao.save(testProduct3);
        defaultCartService.add(cart, 1L, 88);

        assertEquals(1, cart.getCartItems().size());
        assertEquals(88, cart.getCartItems().get(0).getQuantity());

        defaultCartService.add(cart, 1L, 2);

        assertEquals(1, cart.getCartItems().size());
        assertEquals(90, cart.getCartItems().get(0).getQuantity());
    }

    @Test
    public void testUpdateCartItem() throws ProductOutOfStockException, ProductNotFoundException {
        cart = new Cart();
        productDao.save(testProduct1);
        productDao.save(testProduct2);
        productDao.save(testProduct3);
        defaultCartService.add(cart, 1L, 88);

        assertEquals(1, cart.getCartItems().size());
        assertEquals(88, cart.getCartItems().get(0).getQuantity());

        defaultCartService.update(cart, 1L, 2);

        assertEquals(1, cart.getCartItems().size());
        assertEquals(2, cart.getCartItems().get(0).getQuantity());
    }

    @Test
    public void testDeleteCartItem() throws ProductOutOfStockException {
        cart = new Cart();

        productDao.save(testProduct1);
        productDao.save(testProduct2);
        productDao.save(testProduct3);

        defaultCartService.delete(cart, 1L);
        assertEquals(0, cart.getCartItems().size());
    }

    @Test(expected = ProductOutOfStockException.class)
    public void testAddCartOutOfStock() throws ProductOutOfStockException {
        cart = new Cart();
        defaultCartService.add(cart, 1L, 1000);
    }

    @Test
    public void testAddCartProductNotFound() throws ProductOutOfStockException {
        cart = new Cart();
        defaultCartService.add(cart, 18L, 88);
        assertEquals(0, cart.getCartItems().size());
    }
}
