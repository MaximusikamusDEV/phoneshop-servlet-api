package com.es.phoneshop.web;

import com.es.phoneshop.cart.Cart;
import com.es.phoneshop.cart.CartService;
import com.es.phoneshop.cart.DefaultCartService;
import com.es.phoneshop.util.PagePaths;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class DeleteCartItemServlet extends HttpServlet {
    private static final String INIT_ERROR_MESSAGE = "ProductDao or CartService or RecentlyViewedService is null";
    private static final String PARSE_ERROR_MESSAGE = "ProductId is empty, null or has incorrect format";
    private static final String SUCCESS_MESSAGE = "?message=Cart item deleted successfully";
    private static final String CART_PATH = "/cart";
    private static final Logger logger = LoggerFactory.getLogger(DeleteCartItemServlet.class);
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long productId;

        try {
            productId = getProductId(req);
        }catch (NullPointerException | NumberFormatException e){
            logger.error(PARSE_ERROR_MESSAGE, e);
            req.getRequestDispatcher(PagePaths.error()).forward(req, resp);
            return;
        }

        Cart cart = cartService.getCart(req);
        cartService.delete(cart, productId);

        resp.sendRedirect(buildRedirectURL(req));
    }

    private String buildRedirectURL(HttpServletRequest req) {
        return req.getContextPath() + CART_PATH + SUCCESS_MESSAGE;
    }

    private Long getProductId(HttpServletRequest req) {
        String productId = req.getPathInfo();

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
}
