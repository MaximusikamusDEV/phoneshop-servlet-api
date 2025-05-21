package com.es.phoneshop.web;

import com.es.phoneshop.exceptions.ProductNotFoundException;
import com.es.phoneshop.exceptions.ProductOutOfStockException;
import com.es.phoneshop.model.cart.CartService;
import com.es.phoneshop.model.cart.DefaultCartService;
import com.es.phoneshop.model.dao.productdao.HashMapProductDao;
import com.es.phoneshop.model.product.RecentlyViewedService;
import com.es.phoneshop.sortenums.SortField;
import com.es.phoneshop.sortenums.SortOrder;
import com.es.phoneshop.util.PagePaths;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

public class AdvancedSearchServlet extends HttpServlet {

    private static final String ATTRIBUTE_PRODUCTS = "products";
    private static final String INIT_FAIL_MESSAGE = "ProductDao or RecentlyViewedService or cartService are null";
    private static final String PRICE_FORMAT_EXCEPTION = "Not a number";
    private static final String PRODUCTS_PATH = "/products";
    private static final String QUANTITY_LOWER_THAN_ZERO = "Quantity cannot be zero or lower";
    private static final Logger logger = LoggerFactory.getLogger(ProductListPageServlet.class);
    private HashMapProductDao productDao;


    @Override
    public void init() throws ServletException {
        super.init();
        productDao = HashMapProductDao.getInstance();

        if (productDao == null) {
            logger.error(INIT_FAIL_MESSAGE);
            throw new ServletException(INIT_FAIL_MESSAGE);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute(ATTRIBUTE_PRODUCTS, Collections.emptyList());
        request.getRequestDispatcher(PagePaths.advancedSearch()).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> errors = new HashMap<>();

        errors = validatePriceRange(req);
        req.setAttribute("errors", validatePriceRange(req));

        if (errors.isEmpty()) {
            setProductsByQuery(req);
        } else {
            req.setAttribute(ATTRIBUTE_PRODUCTS, Collections.emptyList());
        }

        req.getRequestDispatcher(PagePaths.advancedSearch()).forward(req, resp);
    }


    private void setProductsByQuery(HttpServletRequest request) {
        String description = request.getParameter("description");
        String minPrice = request.getParameter("minPrice");
        String maxPrice = request.getParameter("maxPrice");
        String descriptionOption = request.getParameter("descriptionOption");

        description = description != null ? description : "";

        request.setAttribute(ATTRIBUTE_PRODUCTS, productDao.advancedFindProducts(description,
                minPrice,
                maxPrice,
                descriptionOption));
    }

    public static Map<String, String> validatePriceRange(HttpServletRequest request) {
        String minPriceStr = request.getParameter("minPrice");
        String maxPriceStr = request.getParameter("maxPrice");

        Map<String, String> errors = new HashMap<>();
        Double minPriceDouble = null;
        Double maxPriceDouble = null;

        if (!StringUtils.isEmpty(minPriceStr)) {
            try {
                minPriceDouble = Double.parseDouble(minPriceStr);
                if (minPriceDouble < 0) {
                    errors.put("minPrice", QUANTITY_LOWER_THAN_ZERO);
                }
            } catch (NumberFormatException e) {
                errors.put("minPrice", PRICE_FORMAT_EXCEPTION);
            }
        }

        if (!StringUtils.isEmpty(maxPriceStr)) {
            try {
                maxPriceDouble = Double.parseDouble(maxPriceStr);
                if (maxPriceDouble < 0) {
                    errors.put("maxPrice", QUANTITY_LOWER_THAN_ZERO);
                }
            } catch (NumberFormatException e) {
                errors.put("maxPrice", PRICE_FORMAT_EXCEPTION);
            }
        }
        return errors;
    }


    private String buildRedirectURL(HttpServletRequest req, String message) {
        return req.getContextPath() + PRODUCTS_PATH + message;
    }
}
