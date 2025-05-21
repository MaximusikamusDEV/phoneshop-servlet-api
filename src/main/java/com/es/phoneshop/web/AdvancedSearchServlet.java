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

    private static final String ATTRIBUTE_RECENTLY_VIEWED = "recentlyViewed";
    private static final String ATTRIBUTE_PRODUCTS = "products";
    private static final String ATTRIBUTE_ALL_QUANTITIES_SESSION = "allQuantitiesProductList";
    private static final String ATTRIBUTE_ERRORS = "errors";
    private static final String ATTRIBUTE_ERRORS_SESSION = "errorsProductList";
    private static final String ATTRIBUTE_QUANTITIES_SESSION = "quantitiesProductList";
    private static final String PARAMETER_SORT_FIELD = "sortField";
    private static final String PARAMETER_SORT_ORDER = "sortOrder";
    private static final String PARAMETER_FIND_PRODUCT_QUERY = "findProductQuery";
    private static final String PARAM_VALUES_PRODUCT_ID = "productId";
    private static final String PARAM_VALUES_QUANTITY = "quantity";
    private static final String PARAM_VALUES_INPUT_QUANTITIES = "inputQuantities";
    private static final String PARAM_VALUES_ALL_QUANTITIES = "allQuantities";
    private static final String PARAMETER_ACTION = "action";
    private static final String SUCCESS_MESSAGE = "?message=Product added to cart successfully";
    private static final String ERRORS_MESSAGE = "?error=true";
    private static final String QUANTITY_NOT_NUMBER_MESSAGE = "Quantity is not a number ";
    private static final String INIT_FAIL_MESSAGE = "ProductDao or RecentlyViewedService or cartService are null";
    private static final String PRODUCT_OUT_OF_STOCK_AVAILABLE = "Product is out of stock. Available %s";
    private static final String PRODUCT_OUT_OF_STOCK = "Product is out of stock.";
    private static final String PRODUCT_NUMBER_FORMAT_EXCEPTION = "Product number format exception";
    private static final String PRODUCTS_PATH = "/products";
    private static final String QUANTITY_LOWER_THAN_ZERO = "Quantity cannot be zero or lower";
    private static final Logger logger = LoggerFactory.getLogger(ProductListPageServlet.class);
    private HashMapProductDao productDao;
    private RecentlyViewedService recentlyViewedService;
    private DefaultCartService cartService;

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
        setProductsByQuery(req);

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


    private String buildRedirectURL(HttpServletRequest req, String message) {
        return req.getContextPath() + PRODUCTS_PATH + message;
    }
}
