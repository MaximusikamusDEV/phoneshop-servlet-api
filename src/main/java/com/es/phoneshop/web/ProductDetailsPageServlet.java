package com.es.phoneshop.web;

import com.es.phoneshop.cart.Cart;
import com.es.phoneshop.cart.CartService;
import com.es.phoneshop.cart.DefaultCartService;
import com.es.phoneshop.cart.RecentlyViewedService;
import com.es.phoneshop.exceptions.ProductNotFoundException;
import com.es.phoneshop.exceptions.ProductOutOfStockException;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.productdao.HashMapProductDao;
import com.es.phoneshop.util.PagePaths;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.codehaus.plexus.util.StringUtils;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;

public class ProductDetailsPageServlet extends HttpServlet {
    private static final String INIT_ERROR_MESSAGE = "ProductDao or CartService or RecentlyViewedService is null";
    private static final String INVALID_PRODUCT_ID = "Invalid product ID";
    private static final String PRODUCT_NOT_FOUND = "Product not found: ";
    private static final String EMPTY_QUANTITY_MESSAGE = "Quantity is missing or empty";
    private static final String ATTRIBUTE_ERROR = "error";
    private static final String ATTRIBUTE_EXCEPTION = "exception";
    private static final String ATTRIBUTE_QUANTITY = "quantity";
    private static final String ATTRIBUTE_CART = "cart";
    private static final String ATTRIBUTE_PRODUCT = "product";
    private static final String ATTRIBUTE_RECENTLY_VIEWED = "recentlyViewed";
    private static final String ADDED_TO_CART_MESSAGE = "ProductAddedToCart";
    private static final String PRODUCT_OUT_OF_STOCK = "Product is out of stock.";
    private static final String PRODUCT_OUT_OF_STOCK_AVAILABLE = "Product is out of stock. Available %s";
    private static final String REDIRECT_MESSAGE = "?message=";
    private static final String PARAMETER_QUANTITY = "quantity";
    private static final String QUANTITY_NOT_NUMBER_MESSAGE = "Quantity is not a number ";
    private static final String PRODUCTS_PATH = "/products/";
    private static final Logger logger = LoggerFactory.getLogger(ProductDetailsPageServlet.class);
    private HashMapProductDao productDao;
    private CartService cartService;
    private RecentlyViewedService recentlyViewedService;

    @Override
    public void init() throws ServletException {
        super.init();
        productDao = HashMapProductDao.getInstance();
        cartService = DefaultCartService.getInstance();
        recentlyViewedService = RecentlyViewedService.getInstance();

        if (productDao == null || cartService == null || recentlyViewedService == null) {
            logger.error(INIT_ERROR_MESSAGE);
            throw new ServletException(INIT_ERROR_MESSAGE);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long productId = getProductIdFromUri(request);
        Product product;

        try {
            product = setProduct(request, productId);
            setCart(request);
            setRecentlyViewed(request, product);
            request.getRequestDispatcher(PagePaths.productDetails()).forward(request, response);
        } catch (NumberFormatException e) {
            handleException(request, response, INVALID_PRODUCT_ID + getProductIdFromUri(request),
                    INVALID_PRODUCT_ID, PagePaths.error());
        } catch (ProductNotFoundException e) {
            handleException(request, response, PRODUCT_NOT_FOUND + productId,
                    PRODUCT_NOT_FOUND + productId, PagePaths.productNotFound());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int quantity;
        Long productId = getProductIdFromUri(req);

        try {
            quantity = getQuantity(req);
        } catch (ParseException e) {
            handleError(req, resp, QUANTITY_NOT_NUMBER_MESSAGE + req.getParameter(PARAMETER_QUANTITY),
                    QUANTITY_NOT_NUMBER_MESSAGE);
            return;
        } catch (NullPointerException e){
            handleError(req, resp, EMPTY_QUANTITY_MESSAGE + e, EMPTY_QUANTITY_MESSAGE);
            return;
        }

        try {
            addToCartService(req, productId, quantity);
        } catch (ProductOutOfStockException e) {
            handleError(req, resp, PRODUCT_OUT_OF_STOCK,
                    String.format(PRODUCT_OUT_OF_STOCK_AVAILABLE, e.getAvailableStock()));
            return;
        }

        resp.sendRedirect(buildRedirectURL(req, productId, ADDED_TO_CART_MESSAGE));
    }

    private String buildRedirectURL(HttpServletRequest req, long productId, String message) {
        return req.getContextPath() + PRODUCTS_PATH + productId + REDIRECT_MESSAGE + message;
    }

    private void setRecentlyViewed(HttpServletRequest request, Product product) {
        recentlyViewedService.add(request, product);
        request.setAttribute(ATTRIBUTE_RECENTLY_VIEWED, recentlyViewedService.getRecentlyViewed(request.getSession()));
    }

    private void setCart(HttpServletRequest request) {
        request.setAttribute(ATTRIBUTE_CART, cartService.getCart(request));
    }

    private Product setProduct(HttpServletRequest request, Long productId) throws ProductNotFoundException {
        Product product = productDao.getProduct(productId);
        request.setAttribute(ATTRIBUTE_PRODUCT, product);
        return product;
    }

    private void addToCartService(HttpServletRequest req, Long productId, int quantity) throws ProductOutOfStockException {
        Cart cart = cartService.getCart(req);
        cartService.add(cart, productId, quantity);
    }

    private int getQuantity(HttpServletRequest req) throws ParseException {
        NumberFormat format = NumberFormat.getInstance(req.getLocale());
        String quantity = req.getParameter(ATTRIBUTE_QUANTITY);

        if(quantity == null || StringUtils.isEmpty(quantity)) {
            throw new NullPointerException();
        }

        return format.parse(quantity).intValue();
    }

    private Long getProductIdFromUri(HttpServletRequest request) {
        return Long.valueOf(request.getPathInfo().substring(1));
    }

    private void handleException(HttpServletRequest req, HttpServletResponse resp, String logMessage,
                                 String exceptionMessage, String forwardPage) throws ServletException, IOException {
        logger.error(logMessage);
        req.setAttribute(ATTRIBUTE_EXCEPTION, exceptionMessage);
        req.getRequestDispatcher(forwardPage).forward(req, resp);
    }

    private void handleError(HttpServletRequest req, HttpServletResponse resp, String logMessage,
                             String errorMessage) throws ServletException, IOException {
        logger.error(logMessage);
        req.setAttribute(ATTRIBUTE_ERROR, errorMessage);
        doGet(req, resp);
    }
}
