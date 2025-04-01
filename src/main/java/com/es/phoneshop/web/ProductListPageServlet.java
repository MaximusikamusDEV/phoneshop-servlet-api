package com.es.phoneshop.web;

import com.es.phoneshop.cart.RecentlyViewedService;
import com.es.phoneshop.productdao.HashMapProductDao;
import com.es.phoneshop.sortenums.SortField;
import com.es.phoneshop.sortenums.SortOrder;
import com.es.phoneshop.util.PagePaths;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Optional;

public class ProductListPageServlet extends HttpServlet {
    private static final String INIT_FAIL_MESSAGE = "ProductDao or RecentlyViewedService are null";
    private static final String ATTRIBUTE_RECENTLY_VIEWED = "recentlyViewed";
    private static final String ATTRIBUTE_PRODUCTS = "products";
    private static final String PARAMETER_SORT_FIELD = "sortField";
    private static final String PARAMETER_SORT_ORDER = "sortOrder";
    private static final String PARAMETER_FIND_PRODUCT_QUERY = "findProductQuery";
    private static final Logger logger = LoggerFactory.getLogger(ProductListPageServlet.class);
    private HashMapProductDao productDao;
    private RecentlyViewedService recentlyViewedService;

    @Override
    public void init() throws ServletException {
        super.init();
        productDao = HashMapProductDao.getInstance();
        recentlyViewedService = RecentlyViewedService.getInstance();

        if(productDao == null || recentlyViewedService == null) {
            logger.error(INIT_FAIL_MESSAGE);
            throw new ServletException(INIT_FAIL_MESSAGE);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setProductsWithAttributes(request);
        setRecentlyViewed(request);
        request.getRequestDispatcher(PagePaths.productList()).forward(request, response);
    }

    private void setRecentlyViewed(HttpServletRequest request) {
        request.setAttribute(ATTRIBUTE_RECENTLY_VIEWED, recentlyViewedService.getRecentlyViewed(request.getSession()));
    }

    private void setProductsWithAttributes(HttpServletRequest request) {
        String findProductQuery = request.getParameter(PARAMETER_FIND_PRODUCT_QUERY) != null ? request.getParameter(PARAMETER_FIND_PRODUCT_QUERY): "";
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
    }
}
