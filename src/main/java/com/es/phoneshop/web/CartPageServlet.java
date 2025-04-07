package com.es.phoneshop.web;

import com.es.phoneshop.cart.CartService;
import com.es.phoneshop.cart.DefaultCartService;
import com.es.phoneshop.exceptions.ProductNotFoundException;
import com.es.phoneshop.exceptions.ProductOutOfStockException;
import com.es.phoneshop.util.PagePaths;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class CartPageServlet extends HttpServlet {
    private static final String INIT_ERROR_MESSAGE = "ProductDao or CartService or RecentlyViewedService is null";
    private static final String ATTRIBUTE_CART_ITEMS = "cartItems";
    private static final String PRODUCT_NOT_FOUND_MESSAGE = "Product with id {} not found";
    private static final String PRODUCT_NUMBER_FORMAT_EXCEPTION = "Product number format exception";
    private static final String ATTRIBUTE_CART = "cart";
    private static final String PARAM_VALUES_PRODUCT_ID = "productId";
    private static final String PARAM_VALUES_QUANTITY = "quantity";
    private static final String PRODUCT_OUT_OF_STOCK_AVAILABLE = "Product is out of stock. Available %s";
    private static final String QUANTITY_NOT_NUMBER_MESSAGE = "Quantity is not a number ";
    private static final String QUANTITY_LOWER_THAN_ZERO = "Quantity cannot be zero or lower";
    private static final String ATTRIBUTE_ERRORS = "errors";
    private static final String SUCCESS_MESSAGE = "?message=Updated successfully";
    private static final String CART_PATH = "/cart";
    private static final Logger logger = LoggerFactory.getLogger(CartPageServlet.class);
    private CartService cartService;

    @Override
    public void init() throws ServletException {
        super.init();
        cartService = DefaultCartService.getInstance();

        if (cartService == null) {
            logger.error(INIT_ERROR_MESSAGE);
            throw new ServletException(INIT_ERROR_MESSAGE);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCartItems(request);
        setCart(request);
        request.getRequestDispatcher(PagePaths.cart()).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] productIds = req.getParameterValues(PARAM_VALUES_PRODUCT_ID);
        String[] quantities = req.getParameterValues(PARAM_VALUES_QUANTITY);
        Map<Long, String> errors = new HashMap<>();

        if (productIds == null || productIds.length == 0 || quantities == null || quantities.length == 0) {
            doGet(req, resp);
            return;
        }

        for (int i = 0; i < productIds.length; i++) {
            Long productId;

            try {
                productId = Long.valueOf(productIds[i]);
            } catch (NumberFormatException e) {
                logger.error(PRODUCT_NUMBER_FORMAT_EXCEPTION, e);
                doGet(req, resp);
                return;
            }

            try {
                int quantity = getQuantity(quantities[i], req);
                cartService.update(cartService.getCart(req), productId, quantity);
            } catch (ParseException | ProductOutOfStockException | ProductNotFoundException | NullPointerException e) {
                handleErrorForJsp(errors, productId, e);
            }
        }

        if (errors.isEmpty()) {
            resp.sendRedirect(buildRedirectURL(req));
        } else {
            req.setAttribute(ATTRIBUTE_ERRORS, errors);
            doGet(req, resp);
        }
    }

    private String buildRedirectURL(HttpServletRequest req) {
        return req.getContextPath() + CART_PATH + SUCCESS_MESSAGE;
    }

    private void setCartItems(HttpServletRequest request) {
        request.setAttribute(ATTRIBUTE_CART_ITEMS, cartService.getCart(request).getCartItems());
    }

    private void setCart(HttpServletRequest request) {
        request.setAttribute(ATTRIBUTE_CART, cartService.getCart(request));
    }

    private int getQuantity(String quantityString, HttpServletRequest req) throws ParseException {
        NumberFormat format = NumberFormat.getInstance(req.getLocale());

        if (quantityString == null || StringUtils.isEmpty(quantityString)) {
            throw new NullPointerException();
        }

        return format.parse(quantityString).intValue();
    }

    private void handleErrorForJsp(Map<Long, String> errors, Long productId, Exception e) {
        if (e instanceof ProductNotFoundException) {
            logger.error(PRODUCT_NOT_FOUND_MESSAGE, productId, e);
            errors.put(productId, PRODUCT_NOT_FOUND_MESSAGE);
        } else if (e instanceof ParseException || e instanceof NullPointerException) {
            logger.error(QUANTITY_NOT_NUMBER_MESSAGE, e);
            errors.put(productId, QUANTITY_NOT_NUMBER_MESSAGE);
        } else {
            if (((ProductOutOfStockException) e).getRequiredStock() <= 0) {
                logger.error(QUANTITY_LOWER_THAN_ZERO, e);
                errors.put(productId, QUANTITY_LOWER_THAN_ZERO);
            } else {
                logger.error(e.getMessage());
                errors.put(productId,
                        String.format(PRODUCT_OUT_OF_STOCK_AVAILABLE, ((ProductOutOfStockException) e).getAvailableStock()));
            }
        }
    }
}
