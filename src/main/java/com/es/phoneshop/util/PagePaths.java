package com.es.phoneshop.util;

import com.es.phoneshop.exceptions.PropertiesNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PagePaths {
    private static final String PROPERTIES_FILE = "pagePaths.properties";
    private static final String PROPERTIES_EXCEPTION_MESSAGE = "pagePaths";
    private static final Properties properties = new Properties();

    static {
        try (InputStream in = PagePaths.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if(in == null){
                throw new PropertiesNotFoundException(PROPERTIES_EXCEPTION_MESSAGE);
            }

            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getPagePath(String pageName) {
        String pagePath = properties.getProperty(pageName);

        if(pagePath == null){
            throw new PropertiesNotFoundException(PROPERTIES_EXCEPTION_MESSAGE);
        }

        return pagePath;
    }

    public static String error() {
        return getPagePath("pagePaths.error");
    }

    public static String productNotFound() {
        return getPagePath("pagePaths.error.productNotFound");
    }

    public static String productDetails() {
        return getPagePath("pagePaths.productDetails");
    }

    public static String productList() {
        return getPagePath("pagePaths.productList");
    }

    public static String cart() {
        return getPagePath("pagePaths.cart");
    }

    public static String miniCart() {
        return getPagePath("pagePaths.cart.miniCart");
    }
}
