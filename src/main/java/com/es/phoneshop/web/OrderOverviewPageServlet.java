package com.es.phoneshop.web;

import com.es.phoneshop.exceptions.OrderNotFoundException;
import com.es.phoneshop.model.order.DefaultOrderService;
import com.es.phoneshop.model.order.Order;
import com.es.phoneshop.model.order.OrderService;
import com.es.phoneshop.model.dao.orderdao.HashMapOrderDao;
import com.es.phoneshop.model.dao.orderdao.OrderDao;
import com.es.phoneshop.util.PagePaths;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class OrderOverviewPageServlet extends HttpServlet {
    private static final String ATTRIBUTE_ORDER_ITEMS = "orderItems";
    private static final String ATTRIBUTE_ORDER = "order";
    private static final String PARAM_PAYMENT_METHODS = "paymentMethods";
    private static final String PARSE_ERROR_MESSAGE = "OrderId is empty, null or has incorrect format";
    private static final String INIT_ERROR_MESSAGE = "CartService or OrderService or OrderDao is null";
    private static final String ORDER_ERROR_MESSAGE = "Error while getting order";
    private static final Logger logger = LoggerFactory.getLogger(OrderOverviewPageServlet.class);
    private OrderService orderService;
    private OrderDao orderDao;

    @Override
    public void init() throws ServletException {
        super.init();
        orderService = DefaultOrderService.getInstance();
        orderDao = HashMapOrderDao.getInstance();

        if (orderService == null | orderDao == null) {
            logger.error(INIT_ERROR_MESSAGE);
            throw new ServletException(INIT_ERROR_MESSAGE);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String secureOrderId;

        try {
            secureOrderId = getOrderId(request);
        }catch (NullPointerException e){
            logger.error(PARSE_ERROR_MESSAGE, e);
            request.getRequestDispatcher(PagePaths.error()).forward(request, response);
            return;
        }

        Order order;

        try {
            order = orderDao.getOrderBySecureId(secureOrderId);
        } catch (RuntimeException | OrderNotFoundException e) {
            logger.error(ORDER_ERROR_MESSAGE, e);
            request.getRequestDispatcher(PagePaths.orderNotFound()).forward(request, response);
            return;
        }

        setOrderItems(request, order);
        setOrder(request, order);
        request.setAttribute(PARAM_PAYMENT_METHODS, orderService.getPaymentMethods());
        request.getRequestDispatcher(PagePaths.orderOverview()).forward(request, response);
    }

    private void setOrderItems(HttpServletRequest request, Order order) {
        request.setAttribute(ATTRIBUTE_ORDER_ITEMS, order.getCartItems());
    }

    private void setOrder(HttpServletRequest request, Order order) {
        request.setAttribute(ATTRIBUTE_ORDER, order);
    }

    private String getOrderId(HttpServletRequest req) {
        String orderId = req.getPathInfo();

        if(orderId == null || StringUtils.isEmpty(orderId)) {
            throw new NullPointerException();
        }

        return orderId.substring(1);
    }
}
