package com.es.phoneshop.web;

import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.model.cart.CartService;
import com.es.phoneshop.model.cart.DefaultCartService;
import com.es.phoneshop.model.product.RecentlyViewedService;
import com.es.phoneshop.exceptions.ProductNotFoundException;
import com.es.phoneshop.exceptions.ProductOutOfStockException;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.model.dao.productdao.HashMapProductDao;
import com.es.phoneshop.util.PagePaths;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.codehaus.plexus.util.StringUtils;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;

public class ProductDetailsPageServlet extends HttpServlet {
    private static final String ATTRIBUTE_ERROR = "error";
    private static final String ATTRIBUTE_EXCEPTION = "exception";
    private static final String ATTRIBUTE_QUANTITY = "quantity";
    private static final String ATTRIBUTE_CART = "cart";
    private static final String ATTRIBUTE_PRODUCT = "product";
    private static final String ATTRIBUTE_RECENTLY_VIEWED = "recentlyViewed";
    private static final String ATTRIBUTE_ERRORS_SESSION = "errorsProductDetails";
    private static final String ATTRIBUTE_QUANTITY_SESSION = "quantityProductDetails";
    private static final String PARAM_VALUES_INPUT_QUANTITY = "inputQuantity";
    private static final String PARAMETER_QUANTITY = "quantity";
    private static final String ERRORS_MESSAGE = "?error=true";
    private static final String INIT_ERROR_MESSAGE = "ProductDao or CartService or RecentlyViewedService is null";
    private static final String EMPTY_QUANTITY_MESSAGE = "Quantity is missing or empty";
    private static final String ADDED_TO_CART_MESSAGE = "?message=Product Added To Cart";
    private static final String QUANTITY_NOT_NUMBER_MESSAGE = "Quantity is not a number ";
    private static final String INVALID_PRODUCT_ID = "Invalid product ID ";
    private static final String PRODUCT_NOT_FOUND = "Product not found: ";
    private static final String PRODUCT_OUT_OF_STOCK = "Product is out of stock.";
    private static final String PRODUCT_OUT_OF_STOCK_AVAILABLE = "Product is out of stock. Available %s";
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
        setErrorToAttribute(request);
        setQuantityToAttribute(request);

        Long productId;

        try {
            productId = getProductIdFromUri(request);
        }catch (NumberFormatException | NullPointerException e) {
            handleException(request, response, INVALID_PRODUCT_ID,
                    INVALID_PRODUCT_ID, PagePaths.error());
            return;
        }

        Product product;

        try {
            product = setProduct(request, productId);
            setCart(request);
            setRecentlyViewed(request, product);
            request.getRequestDispatcher(PagePaths.productDetails()).forward(request, response);
        } catch (NumberFormatException e) {
            handleException(request, response, INVALID_PRODUCT_ID + e,
                    INVALID_PRODUCT_ID, PagePaths.error());
        } catch (ProductNotFoundException e) {
            handleException(request, response, PRODUCT_NOT_FOUND + productId + e,
                    PRODUCT_NOT_FOUND + productId, PagePaths.productNotFound());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int quantity;
        Long productId;

        try {
            productId = getProductIdFromUri(req);
        }catch (NumberFormatException | NullPointerException e) {
            handleException(req, resp, INVALID_PRODUCT_ID + e,
                    INVALID_PRODUCT_ID, PagePaths.error());
            return;
        }

        try {
            quantity = getQuantity(req);
        } catch (ParseException e) {
            handleError(req, resp, QUANTITY_NOT_NUMBER_MESSAGE + req.getParameter(PARAMETER_QUANTITY) + e,
                    QUANTITY_NOT_NUMBER_MESSAGE, productId);
            return;
        } catch (NullPointerException e){
            handleError(req, resp, EMPTY_QUANTITY_MESSAGE + e, EMPTY_QUANTITY_MESSAGE, productId);
            return;
        }

        try {
            addToCartService(req, productId, quantity);
        } catch (ProductOutOfStockException e) {
            handleError(req, resp, PRODUCT_OUT_OF_STOCK + e,
                    String.format(PRODUCT_OUT_OF_STOCK_AVAILABLE, e.getAvailableStock()), productId);
            return;
        }

        resp.sendRedirect(buildRedirectURL(req, productId, ADDED_TO_CART_MESSAGE));
    }

    private void setErrorToAttribute(HttpServletRequest req) {
        HttpSession session = req.getSession();
        String error = (String) session.getAttribute(ATTRIBUTE_ERRORS_SESSION);

        if(StringUtils.isNotEmpty(error)) {
            req.setAttribute(ATTRIBUTE_ERROR, error);
            session.removeAttribute(ATTRIBUTE_ERRORS_SESSION);
        }
    }

    private void setErrorToSession(HttpServletRequest req, String error) {
        HttpSession session = req.getSession();
        session.setAttribute(ATTRIBUTE_ERRORS_SESSION, error);
    }

    private void setQuantityToAttribute(HttpServletRequest req) {
        HttpSession session = req.getSession();
        String quantity = (String) session.getAttribute(ATTRIBUTE_QUANTITY_SESSION);

        if(StringUtils.isNotEmpty(quantity)) {
            req.setAttribute(PARAM_VALUES_INPUT_QUANTITY, quantity);
            req.getSession().removeAttribute(ATTRIBUTE_QUANTITY_SESSION);
        }
    }

    private void setQuantityToSession(HttpServletRequest req, String inputQuantity) {
        HttpSession session = req.getSession();
        session.setAttribute(ATTRIBUTE_QUANTITY_SESSION, inputQuantity);
    }

    private String buildRedirectURL(HttpServletRequest req, long productId, String message) {
        return req.getContextPath() + PRODUCTS_PATH + productId + message;
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
        setQuantityToSession(req, quantity);

        if(quantity == null || StringUtils.isEmpty(quantity)) {
            throw new NullPointerException();
        }

        return format.parse(quantity).intValue();
    }

    private Long getProductIdFromUri(HttpServletRequest request) {
        String productId = request.getPathInfo();

        if(productId == null || StringUtils.isEmpty(productId)) {
            throw new NullPointerException();
        }

        productId = productId.substring(1);

        try {
            return Long.valueOf(productId);
        } catch (NumberFormatException e) {
            throw new NumberFormatException();
        }
    }

    private void handleException(HttpServletRequest req, HttpServletResponse resp, String logMessage,
                                 Object exceptionMessage, String forwardPage) throws ServletException, IOException {
        logger.error(logMessage);
        req.setAttribute(ATTRIBUTE_EXCEPTION, exceptionMessage);
        req.getRequestDispatcher(forwardPage).forward(req, resp);
    }

    private void handleError(HttpServletRequest req, HttpServletResponse resp, String logMessage,
                             String errorMessage, long productId) throws IOException {
        logger.error(logMessage);
        setErrorToSession(req, errorMessage);
        resp.sendRedirect(buildRedirectURL(req, productId, ERRORS_MESSAGE));
    }
}
