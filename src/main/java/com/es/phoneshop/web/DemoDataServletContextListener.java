package com.es.phoneshop.web;

import com.es.phoneshop.productdao.HashMapProductDao;
import com.es.phoneshop.model.product.PriceHistoryProduct;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.productdao.ProductDao;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Currency;
import java.util.concurrent.ThreadLocalRandom;
import java.util.List;
import java.util.Comparator;

public class DemoDataServletContextListener implements ServletContextListener {

    private static final String PRODUCT_ADDED_SUCCESS_MESSAGE = "Product has been inserted successfully {}";
    private static final String PRODUCT_ADDED_FAIL_MESSAGE = "Failed to set sample history to product {}";
    private static final String DEMO_DATA_INSERT_SUCCESS_MESSAGE = "Demo data has been inserted successfully";
    private static final String GET_SAMPLE_HISTORY_FAIL_MESSAGE = "Failed to get sample history";
    private static final Logger logger = LoggerFactory.getLogger(DemoDataServletContextListener.class);
    private final ProductDao productDao;
    private static final String INSERT_DEMO_DATA = "insertSampleData";
    private static final int MAX_AMOUNT_TEST_HISTORY_PRICES = 20;
    private static final String CURRENCY_CODE_USD = "USD";

    public DemoDataServletContextListener() {
        this.productDao = HashMapProductDao.getInstance();
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        boolean isDemoData = Boolean.parseBoolean(event.getServletContext().getInitParameter(INSERT_DEMO_DATA));

        if (isDemoData) {
            try {
                    getSampleProducts().forEach(product -> {

                        try {
                            setSampleHistory(product);
                            logger.debug(PRODUCT_ADDED_SUCCESS_MESSAGE, product.getDescription());
                        } catch (Exception e) {
                            logger.error(PRODUCT_ADDED_FAIL_MESSAGE, product.getId(), e);
                        }

                        productDao.save(product);
                });

                    logger.info(DEMO_DATA_INSERT_SUCCESS_MESSAGE);
            } catch (Exception e) {
                logger.error(GET_SAMPLE_HISTORY_FAIL_MESSAGE, e);
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContextListener.super.contextDestroyed(sce);
    }

    public static List<Product> getSampleProducts() throws Exception {
        Currency usd = Currency.getInstance(CURRENCY_CODE_USD);
        List<Product> result = new ArrayList<>();
        result.add(new Product("sgs", "Samsung Galaxy S", new BigDecimal(100), usd, 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S.jpg"));
        result.add(new Product("sgs2", "Samsung Galaxy S II", new BigDecimal(200), usd, 0, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S%20II.jpg"));
        result.add(new Product("sgs3", "Samsung Galaxy S III", new BigDecimal(300), usd, 5, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S%20III.jpg"));
        result.add(new Product("iphone", "Apple iPhone", new BigDecimal(200), usd, 10, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Apple/Apple%20iPhone.jpg"));
        result.add(new Product("iphone6", "Apple iPhone 6", new BigDecimal(1000), usd, 30, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Apple/Apple%20iPhone%206.jpg"));
        result.add(new Product("htces4g", "HTC EVO Shift 4G", new BigDecimal(320), usd, 3, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/HTC/HTC%20EVO%20Shift%204G.jpg"));
        result.add(new Product("sec901", "Sony Ericsson C901", new BigDecimal(420), usd, 30, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Sony/Sony%20Ericsson%20C901.jpg"));
        result.add(new Product("xperiaxz", "Sony Xperia XZ", new BigDecimal(120), usd, 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Sony/Sony%20Xperia%20XZ.jpg"));
        result.add(new Product("nokia3310", "Nokia 3310", new BigDecimal(70), usd, 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Nokia/Nokia%203310.jpg"));
        result.add(new Product("palmp", "Palm Pixi", new BigDecimal(170), usd, 30, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Palm/Palm%20Pixi.jpg"));
        result.add(new Product("simc56", "Siemens C56", new BigDecimal(70), usd, 20, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Siemens/Siemens%20C56.jpg"));
        result.add(new Product("simc61", "Siemens C61", new BigDecimal(80), usd, 30, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Siemens/Siemens%20C61.jpg"));
        result.add(new Product("simsxg75", "Siemens SXG75", new BigDecimal(150), usd, 40, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Siemens/Siemens%20SXG75.jpg"));

        return result;
    }

    public static void setSampleHistory(Product product) throws Exception {
        List<PriceHistoryProduct> newHistory = product.getPriceHistoryProductList();

        if(newHistory == null) {
            newHistory = new ArrayList<>();
        }

        BigDecimal minPrice = new BigDecimal(200);
        BigDecimal maxPrice = new BigDecimal(3000);
        BigDecimal range = maxPrice.subtract(minPrice);

        LocalDate dateFrom = LocalDate.of(2010, 1, 1);
        LocalDate dateTo = LocalDate.of(2025, 3, 16);
        long betweenDates = ChronoUnit.DAYS.between(dateFrom, dateTo);

        for (int i = 0; i < ThreadLocalRandom.current().nextInt(MAX_AMOUNT_TEST_HISTORY_PRICES); i++) {
            BigDecimal price = minPrice.add(range.multiply(BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble())));
            LocalDate localDate = dateFrom.plusDays((long) (betweenDates * ThreadLocalRandom.current().nextDouble()));
            newHistory.add(new PriceHistoryProduct(localDate, price));
        }

        newHistory.sort(Comparator.comparing(PriceHistoryProduct::getDate));

        product.setPriceHistoryProductList(newHistory);
    }
}
