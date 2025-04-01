package com.es.phoneshop.cart;

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

import java.util.List;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.LinkedList;


import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.when;

import static org.mockito.ArgumentMatchers.anyString;


@RunWith(JUnit4.class)
public class RecentlyViewedServiceTest {


    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpSession session;
    private Product testProduct1;
    private Product testProduct2;
    private Product testProduct3;
    private Product testProduct4;


    RecentlyViewedService recentlyViewedService;

    @Before
    public void setup()  {
        MockitoAnnotations.initMocks(this);
        Currency usd = Currency.getInstance("USD");
        recentlyViewedService = RecentlyViewedService.getInstance();
        ProductDao productDao = HashMapProductDao.getInstance();
        testProduct1 = new Product(1L, "test-product1", "Samsung Galaxy S", new BigDecimal(100), usd, 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S.jpg");
        testProduct2 = new Product(1L, "test-product2", "Samsung Galaxy S", new BigDecimal(100), usd, 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S.jpg");
        testProduct3 = new Product(2L, "test-product3", "Samsung Galaxy S", new BigDecimal(100), usd, 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S.jpg");
        testProduct4 = new Product(3L, "test-product3", "Samsung Galaxy S", new BigDecimal(100), usd, 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S.jpg");
        productDao.save(testProduct1);
        productDao.save(testProduct2);
        productDao.save(testProduct3);
    }

    @Test
    public void getRecentlyViewedTest(){
        List<Product> recentlyViewed = new LinkedList<>();
        recentlyViewed.add(testProduct1);
        recentlyViewed.add(testProduct2);
        recentlyViewed.add(testProduct3);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(anyString())).thenReturn(null);

        List<Product> testListRecView = RecentlyViewedService.getInstance().getRecentlyViewed(session);

        assertEquals(0, testListRecView.size());

        when(session.getAttribute(anyString())).thenReturn(recentlyViewed);
        testListRecView = RecentlyViewedService.getInstance().getRecentlyViewed(session);
        assertEquals(3, testListRecView.size());

    }

    @Test
    public void addTest(){
        List<Product> recentlyViewed = new LinkedList<>();
        recentlyViewed.add(testProduct1);
        recentlyViewed.add(testProduct2);
        recentlyViewed.add(testProduct3);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(anyString())).thenReturn(recentlyViewed);

        RecentlyViewedService.getInstance().add(request, testProduct3);
        List<Product> testListRecView = RecentlyViewedService.getInstance().getRecentlyViewed(session);

        assertEquals(3, testListRecView.size());
        assertEquals(testProduct3, testListRecView.get(0));

        RecentlyViewedService.getInstance().add(request, testProduct4);
        testListRecView = RecentlyViewedService.getInstance().getRecentlyViewed(session);

        assertEquals(3, testListRecView.size());
        assertEquals(testProduct4, testListRecView.get(0));
        assertEquals(testProduct3, testListRecView.get(1));

    }


}
