package com.es.phoneshop.model.product;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;


public class ArrayListProductDaoTest
{
    private Product testProduct;
    private Currency usd;
    private ProductDao productDao;

    @Before
    public void setup() {
        productDao = new ArrayListProductDao();
        usd = Currency.getInstance("USD");
        testProduct = new Product("test-product", "Samsung Galaxy S", new BigDecimal(100), usd, 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S.jpg");
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
        assertEquals(testProduct.getCode(), testGetProduct.getCode());

        Product newTestProduct = new Product(20L, "newCodeAndNumb", "Samsung Galaxy S", new BigDecimal(100), usd, 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S.jpg");
        productDao.save(newTestProduct);
        testGetProduct = productDao.getProduct( 1L);
        assertEquals(1L, (long) testGetProduct.getId());
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

    @Test
    public void testDeleteProduct() throws ProductNotFoundException {
        Product newTestProduct = new Product(20L, "newCodeAndNumb", "Samsung Galaxy S", new BigDecimal(100), usd, 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S.jpg");
        productDao.save(newTestProduct);

        productDao.delete(0L);
        assertEquals(0, productDao.findProducts().size());
    }

    @Test
    public void testEmptyFilterProducts() throws ProductNotFoundException {
        Product testProductWithoutPrice = new Product("test-product1", "Samsung Galaxy S",  100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S.jpg");
        Product testProductWithoutStock = new Product("test-product2", "Samsung Galaxy S", new BigDecimal(100), usd, 0, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S.jpg");

        productDao.save(testProductWithoutPrice);
        productDao.save(testProductWithoutStock);

        List<Product> testGetAfterFilterProduct = productDao.findProducts();

        assertEquals(0, testGetAfterFilterProduct.size());

        productDao.save(testProduct);
        testGetAfterFilterProduct = productDao.findProducts();
        assertNotNull(testGetAfterFilterProduct);
        assertEquals(testProduct.getCode(), testGetAfterFilterProduct.get(0).getCode());
    }

    @Test
    public void testFilterProducts() throws ProductNotFoundException {
        productDao.save(testProduct);
        List<Product> testGetAfterFilterProduct = productDao.findProducts();
        assertNotNull(testGetAfterFilterProduct);

        Product product = testGetAfterFilterProduct.stream().findFirst().get();

        assertEquals(testProduct.getCode(), product.getCode());
    }

     @Test
    public void testSetSampleProducts() throws ProductNotFoundException {
         ArrayListProductDao productDao1 = new ArrayListProductDao();
         productDao1.setSampleProducts();

         List<Product> testProductList = productDao1.findProducts();

         assertNotNull(testProductList);

         assertEquals(testProductList.size(), 12);
     }
}
