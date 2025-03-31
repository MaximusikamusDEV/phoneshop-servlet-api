package com.es.phoneshop.model.product;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;


public class HashMapProductDaoTest
{
    private Product testProduct;
    private Currency usd;
    private ProductDao productDao;

    @Before
    public void setup() {
        productDao = HashMapProductDao.getInstance();

        usd = Currency.getInstance("USD");
        testProduct = new Product("test-product", "Samsung Galaxy S", new BigDecimal(100), usd, 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S.jpg");
    }



    @After
    public void clear() throws NoSuchFieldException, IllegalAccessException {
        Field productField = HashMapProductDao.class.getDeclaredField("products");
        productField.setAccessible(true);
        ((Map<Long, Product>) productField.get(productDao)).clear();

        Field maxIdField = HashMapProductDao.class.getDeclaredField("maxId");
        maxIdField.setAccessible(true);
        maxIdField.set(productDao, 0);
    }

    @Test
    public void testSaveNewProduct(){
        productDao.save(testProduct);
        assertTrue(testProduct.getId() >= 0);
    }

    @Test
    public void testGetProduct() throws ProductNotFoundException {
        productDao.save(testProduct);

        Product testGetProduct = productDao.getProduct(0L);
        assertNotNull(testGetProduct);
        assertEquals(testProduct.getCode(), productDao.getProduct(0L).getCode());

        Product newTestProduct = new Product(20L, "newCodeAndNumb", "Samsung Galaxy S", new BigDecimal(100), usd, 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S.jpg");
        productDao.save(newTestProduct);
        testGetProduct = productDao.getProduct( 20L);
        assertEquals(20L, (long) testGetProduct.getId());
        assertEquals("newCodeAndNumb", testGetProduct.getCode());
    }

    @Test
    public void testUpdateProduct() throws ProductNotFoundException {
        productDao.save(testProduct);
        Product testGetProduct;
        testProduct.setCode("newCode");
        productDao.save(testProduct);
        assertTrue(testProduct.getId() >= 0);
        testGetProduct = productDao.getProduct(0L);
        assertEquals("newCode", testGetProduct.getCode());
    }

    @Test(expected = ProductNotFoundException.class)
    public void testWithIncorrectIdDeleteProduct() throws ProductNotFoundException {
        productDao.delete(10L);
    }

    @Test(expected = ProductNotFoundException.class)
    public void testDeleteProduct() throws ProductNotFoundException {
        Product newTestProduct = new Product(20L, "newCodeAndNumb", "Samsung Galaxy S", new BigDecimal(100), usd, 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S.jpg");
        productDao.save(newTestProduct);

        productDao.delete(0L);
        productDao.getProduct(0L);
    }
    @Test
    public void testEmptyFilterProducts() throws ProductNotFoundException {
        Product testProductWithoutPrice = new Product("test-product1", "Samsung Galaxy S",  100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S.jpg");
        Product testProductWithoutStock = new Product("test-product2", "Samsung Galaxy S", new BigDecimal(100), usd, 0, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S.jpg");

        productDao.save(testProductWithoutPrice);
        productDao.save(testProductWithoutStock);

        List<Product> testGetAfterFilterProduct = productDao.findProducts(null, null, null);

        assertEquals(0, testGetAfterFilterProduct.size());

        productDao.save(testProduct);
        testGetAfterFilterProduct = productDao.findProducts("Samsung Galaxy S", null, null);
        assertNotNull(testGetAfterFilterProduct);
        assertEquals(testProduct.getCode(), testGetAfterFilterProduct.get(0).getCode());

        productDao.delete(0L);
        productDao.delete(1L);
    }

    @Test
    public void testFilterProducts() {
        productDao.save(testProduct);
        List<Product> testGetAfterFilterProduct = productDao.findProducts("Samsung Galaxy S", null, null);
        assertNotNull(testGetAfterFilterProduct);

        Product product = testGetAfterFilterProduct.stream().findFirst().get();

        assertEquals(testProduct.getCode(), product.getCode());
    }

    @Test
    public void testSortProducts() {
        productDao.save(new Product("sgs", "Samsung Galaxy S", new BigDecimal(100), usd, 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S.jpg"));
        productDao.save(new Product("sgs2", "Samsung Galaxy S II", new BigDecimal(200), usd, 0, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S%20II.jpg"));
        productDao.save(new Product("sgs3", "Samsung Galaxy S III", new BigDecimal(300), usd, 5, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S%20III.jpg"));
        productDao.save(new Product("iphone", "Apple iPhone", new BigDecimal(200), usd, 10, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Apple/Apple%20iPhone.jpg"));

        List<Product> result = productDao.findProducts("Samsung Galaxy S", null, null);

        assertNotNull(result);
        assertEquals("Samsung Galaxy S", result.get(0).getDescription());

        List<Product> result2 = productDao.findProducts("Samsung Galaxy S", SortField.PRICE, SortOrder.DESC);


        assertNotNull(result);
        assertEquals("Samsung Galaxy S III", result2.get(0).getDescription());



    }

}
