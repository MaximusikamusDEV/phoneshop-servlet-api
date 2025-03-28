package com.es.phoneshop.web;

import com.es.phoneshop.cart.DefaultCartService;
import com.es.phoneshop.exceptions.ProductNotFoundException;
import com.es.phoneshop.exceptions.ProductOutOfStockException;
import com.es.phoneshop.model.product.RecentlyViewedService;
import com.es.phoneshop.productdao.HashMapProductDao;
import com.es.phoneshop.sortenums.SortField;
import com.es.phoneshop.sortenums.SortOrder;
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
import java.util.Optional;

public class ProductListPageServlet extends HttpServlet {
    private static final String INIT_FAIL_MESSAGE = "ProductDao or RecentlyViewedService are null";
    private static final String ATTRIBUTE_RECENTLY_VIEWED = "recentlyViewed";
    private static final String ATTRIBUTE_PRODUCTS = "products";
    private static final String PARAMETER_SORT_FIELD = "sortField";
    private static final String PARAMETER_SORT_ORDER = "sortOrder";
    private static final String PARAMETER_FIND_PRODUCT_QUERY = "findProductQuery";
    private static final String SUCCESS_MESSAGE = "?message=Product added to cart successfully";
    private static final String PRODUCTS_PATH = "/products";
    private static final String PARAM_VALUES_PRODUCT_ID = "productId";
    private static final String PARAM_VALUES_QUANTITY = "quantity";
    private static final String ATTRIBUTE_ERRORS = "errors";
    private static final String PRODUCT_OUT_OF_STOCK_AVAILABLE = "Product is out of stock. Available %s";
    private static final String QUANTITY_NOT_NUMBER_MESSAGE = "Quantity is not a number ";
    private static final String QUANTITY_LOWER_THAN_ZERO = "Quantity cannot be zero or lower";
    private static final String ATTRIBUTE_ALL_QUANTITIES = "allQuantities";
    private static final String PRODUCT_NUMBER_FORMAT_EXCEPTION = "Product number format exception";
    private static final String PARAMETER_ACTION = "action";
    private static final Logger logger = LoggerFactory.getLogger(ProductListPageServlet.class);
    private HashMapProductDao productDao;
    private RecentlyViewedService recentlyViewedService;
    private DefaultCartService cartService;

    @Override
    public void init() throws ServletException {
        super.init();
        productDao = HashMapProductDao.getInstance();
        recentlyViewedService = RecentlyViewedService.getInstance();
        cartService = DefaultCartService.getInstance();

        if (productDao == null || recentlyViewedService == null || cartService == null) {
            logger.error(INIT_FAIL_MESSAGE);
            throw new ServletException(INIT_FAIL_MESSAGE);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setProductsWithAttributes(request);
        setRecentlyViewed(request);
        getAllQuantities(request);
        request.getRequestDispatcher(PagePaths.productList()).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<Long, Integer> allQuantities = new HashMap<>();
        Map<Long, String> errors = new HashMap<>();

        int actionId = Integer.parseInt(req.getParameter(PARAMETER_ACTION));
        String[] productIds = req.getParameterValues(PARAM_VALUES_PRODUCT_ID);
        String[] quantities = req.getParameterValues(PARAM_VALUES_QUANTITY);

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

            int quantity;

            try {
                quantity = getQuantity(quantities[i], req);
                allQuantities.put(productId, quantity);

                if (productId == actionId) {
                    cartService.add(cartService.getCart(req), productId, quantity);
                }

            } catch (ParseException | ProductOutOfStockException | NullPointerException e) {
                handleErrorForJsp(errors, productId, e);
            }
        }

        req.getSession().setAttribute(ATTRIBUTE_ALL_QUANTITIES, allQuantities);

        if (errors.isEmpty()) {
            resp.sendRedirect(buildRedirectURL(req));
        } else {
            req.setAttribute(ATTRIBUTE_ERRORS, errors);
            doGet(req, resp);
        }
    }

    private void getAllQuantities(HttpServletRequest req) {
        Map<Long, Integer> allQuantities = (Map<Long, Integer>) req.getSession().getAttribute(ATTRIBUTE_ALL_QUANTITIES);

        if (allQuantities != null) {
            req.setAttribute(ATTRIBUTE_ALL_QUANTITIES, allQuantities);
            req.getSession().removeAttribute(ATTRIBUTE_ALL_QUANTITIES);
        }
    }

    private void setRecentlyViewed(HttpServletRequest request) {
        request.setAttribute(ATTRIBUTE_RECENTLY_VIEWED, recentlyViewedService.getRecentlyViewed(request.getSession()));
    }

    private void setProductsWithAttributes(HttpServletRequest request) {
        String parameterQuery = request.getParameter(PARAMETER_FIND_PRODUCT_QUERY);
        String findProductQuery = parameterQuery != null ? parameterQuery : "";
        String sortField = request.getParameter(PARAMETER_SORT_FIELD);
        String sortOrder = request.getParameter(PARAMETER_SORT_ORDER);

        request.setAttribute(ATTRIBUTE_PRODUCTS, productDao.findProducts(findProductQuery,
                Optional.ofNullable(sortField)
                        .map(
                                sortfield -> SortField.valueOf(sortfield.toUpperCase())
                        )
                        .orElse(null),
                Optional.ofNullable(sortOrder)
                        .map(
                                sortorder -> SortOrder.valueOf(sortorder.toUpperCase())
                        )
                        .orElse(null)));

        request.setAttribute("recentlyViewed", recentlyViewedService.getRecentlyViewed(request.getSession()));
    }

    private int getQuantity(String quantity, HttpServletRequest req) throws ParseException {
        NumberFormat format = NumberFormat.getInstance(req.getLocale());

        if (quantity == null || StringUtils.isEmpty(quantity)) {
            throw new NullPointerException();
        }

        return format.parse(quantity).intValue();
    }

    private String buildRedirectURL(HttpServletRequest req) {
        return req.getContextPath() + PRODUCTS_PATH + SUCCESS_MESSAGE;
    }

    private void handleErrorForJsp(Map<Long, String> errors, Long productId, Exception e) {
        if (e instanceof ParseException || e instanceof NullPointerException) {
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
