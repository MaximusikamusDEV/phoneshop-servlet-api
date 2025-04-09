package com.es.phoneshop.web;

import com.es.phoneshop.model.cart.CartService;
import com.es.phoneshop.model.cart.DefaultCartService;
import com.es.phoneshop.util.PagePaths;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Currency;

public class MiniCartServlet extends HttpServlet {
    private static final String INIT_ERROR_MESSAGE = "CartService is null";
    private static final String ATTRIBUTE_CURRENCY_SYMBOL = "currencySymbol";
    private static final String ATTRIBUTE_CART = "cart";
    private static final String CURRENCY_CODE_USD = "USD";
    private static final Logger logger = LoggerFactory.getLogger(MiniCartServlet.class);
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
        request.setAttribute(ATTRIBUTE_CART, cartService.getCart(request));
        request.setAttribute(ATTRIBUTE_CURRENCY_SYMBOL, Currency.getInstance(CURRENCY_CODE_USD).getSymbol());
        request.getRequestDispatcher(PagePaths.miniCart()).include(request, response);
    }
}
