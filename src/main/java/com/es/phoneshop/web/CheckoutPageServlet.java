package com.es.phoneshop.web;

import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.model.cart.CartService;
import com.es.phoneshop.model.cart.DefaultCartService;
import com.es.phoneshop.model.order.DefaultOrderService;
import com.es.phoneshop.model.order.Order;
import com.es.phoneshop.model.order.OrderService;
import com.es.phoneshop.sortenums.PaymentMethod;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CheckoutPageServlet extends HttpServlet {
    private static final String ATTRIBUTE_ORDER_ITEMS = "orderItems";
    private static final String ATTRIBUTE_ORDER = "order";
    private static final String ATTRIBUTE_ERRORS = "errors";
    private static final String ATTRIBUTE_ERRORS_SESSION = "errorsProductDetails";
    private static final String PARAM_FIRST_NAME = "firstName";
    private static final String PARAM_LAST_NAME = "lastName";
    private static final String PARAM_PHONE = "phone";
    private static final String PARAM_DELIVERY_DATE = "deliveryDate";
    private static final String PARAM_DELIVERY_ADDRESS = "deliveryAddress";
    private static final String PARAM_PAYMENT_METHOD = "paymentMethod";
    private static final String PARAM_PAYMENT_METHODS = "paymentMethods";
    private static final String ERRORS_MESSAGE = "?error=true";
    private static final String ERROR_MESSAGE = "Value is required";
    private static final String EMPTY_VALUE_ERROR_MESSAGE = "Value is empty %s";
    private static final String INIT_ERROR_MESSAGE = "CartService or OrderService is null";
    private static final String ORDER_ERROR_MESSAGE = "Error while getting order";
    private static final String CART_ERROR_MESSAGE = "Cart is null";
    private static final String DATE_ERROR_MESSAGE = "Date is incorrect. Correct format: %s";
    private static final String DATA_ERROR_MESSAGE = "Data is incorrect. Correct format: %s";
    private static final String PAYMENT_METHOD_ERROR_MESSAGE = "Your payment method is incorrect";
    private static final String PAYMENT_METHOD_EXCEPTION = "Incorrect payment method";
    private static final String DATE_PARSE_EXCEPTION = "Exception while parsing date";
    private static final String DATE_CORRECT_FORMAT = "Year-Month-Day. Can't be before today";
    private static final String PHONE_CORRECT_FORMAT = "+375 and 9 numbers after it";
    private static final String NAME_CORRECT_FORMAT = "Includes only letters. First is uppercase";
    private static final String DELIVERY_ADDRESS_CORRECT_FORMAT = " Includes only letters and numbers";
    private static final String OVERVIEW_PATH = "/order/overview/";
    private static final String CHECKOUT_PATH = "/checkout?";
    private static String queryString;
    private static final Logger logger = LoggerFactory.getLogger(CheckoutPageServlet.class);
    private CartService cartService;
    private OrderService orderService;
    private static final Map<String, String> CORRECT_FORMATS = new HashMap<>();

    static {
        CORRECT_FORMATS.put(PARAM_FIRST_NAME, NAME_CORRECT_FORMAT);
        CORRECT_FORMATS.put(PARAM_LAST_NAME, NAME_CORRECT_FORMAT);
        CORRECT_FORMATS.put(PARAM_PHONE, PHONE_CORRECT_FORMAT);
        CORRECT_FORMATS.put(PARAM_DELIVERY_DATE, DATE_CORRECT_FORMAT);
        CORRECT_FORMATS.put(PARAM_DELIVERY_ADDRESS, DELIVERY_ADDRESS_CORRECT_FORMAT);
    }

    @Override
    public void init() throws ServletException {
        super.init();
        cartService = DefaultCartService.getInstance();
        orderService = DefaultOrderService.getInstance();

        if (cartService == null | orderService == null) {
            logger.error(INIT_ERROR_MESSAGE);
            throw new ServletException(INIT_ERROR_MESSAGE);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Cart cart;

        saveQueryString(request);

        try {
            cart = cartService.getCart(request);
        } catch (NullPointerException e) {
            logger.error(CART_ERROR_MESSAGE, e);
            request.getRequestDispatcher(PagePaths.emptyCart()).forward(request, response);
            return;
        }

        Order order;

        try {
            order = orderService.getOrder(cart);
        } catch (Exception e) {
            logger.error(ORDER_ERROR_MESSAGE, e);
            request.getRequestDispatcher(PagePaths.emptyCart()).forward(request, response);
            return;
        }

        setErrorsToAttribute(request);
        setAllParamsFromSession(request);
        setOrderItems(request, order);
        setOrder(request, order);
        request.setAttribute(PARAM_PAYMENT_METHODS, orderService.getPaymentMethods());
        request.getRequestDispatcher(PagePaths.checkout()).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Cart cart;

        try {
            cart = cartService.getCart(req);
        } catch (NullPointerException e) {
            logger.error(CART_ERROR_MESSAGE, e);
            req.getRequestDispatcher(PagePaths.emptyCart()).forward(req, resp);
            return;
        }

        Order order;

        try {
            order = orderService.getOrder(cart);
        } catch (RuntimeException e) {
            logger.error(ORDER_ERROR_MESSAGE, e);
            req.getRequestDispatcher(PagePaths.error()).forward(req, resp);
            return;
        }

        Map<String, String> errors = new HashMap<>();

        getRequiredParameter(req, PARAM_FIRST_NAME, errors, order::setFirstName, FirstLastNameValidator);
        getRequiredParameter(req, PARAM_LAST_NAME, errors, order::setLastName, FirstLastNameValidator);
        getRequiredParameter(req, PARAM_PHONE, errors, order::setPhone, PhoneValidator);
        getRequiredParameter(req, PARAM_DELIVERY_ADDRESS, errors, order::setDeliveryAddress, DeliveryAddressValidator);
        getDateParameter(req, errors, order);
        getPaymentMethodParameter(req, errors, order);

        setErrorsToSession(req, errors);

        if (errors.isEmpty()) {
            orderService.placeOrder(order);
            cartService.clearCart(cart);
            resp.sendRedirect(buildRedirectURL(req, OVERVIEW_PATH, order.getSecureId()));
        } else {
            resp.sendRedirect(buildRedirectURL(req, CHECKOUT_PATH + queryString, ERRORS_MESSAGE));
        }
    }

    private void setErrorsToAttribute(HttpServletRequest req) {
        HttpSession session = req.getSession();
        Map<String, String> errors = (Map<String, String>) session.getAttribute(ATTRIBUTE_ERRORS_SESSION);

        if(errors != null && !errors.isEmpty()) {
            req.setAttribute(ATTRIBUTE_ERRORS, errors);
            session.removeAttribute(ATTRIBUTE_ERRORS_SESSION);
        }
    }

    private void setErrorsToSession(HttpServletRequest req, Map<String, String> errors) {
        HttpSession session = req.getSession();
        session.setAttribute(ATTRIBUTE_ERRORS_SESSION, errors);
    }

    private String buildRedirectURL(HttpServletRequest req, String destination, String message) {
        return req.getContextPath() + destination + message;
    }

    private void setOrderItems(HttpServletRequest request, Order order) {
        request.setAttribute(ATTRIBUTE_ORDER_ITEMS, order.getCartItems());
    }

    private void setOrder(HttpServletRequest request, Order order) {
        request.setAttribute(ATTRIBUTE_ORDER, order);
    }

    private void getDateParameter(HttpServletRequest req, Map<String, String> errors, Order order) {
        String value = req.getParameter(PARAM_DELIVERY_DATE);

        if (value == null || StringUtils.isEmpty(value)) {
            logger.error(String.format(EMPTY_VALUE_ERROR_MESSAGE, PARAM_DELIVERY_DATE), value);
            errors.put(PARAM_DELIVERY_DATE, ERROR_MESSAGE);
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate deliveryDate;

        try {
            deliveryDate = LocalDate.parse(value, formatter);
        } catch (DateTimeParseException e) {
            logger.error(DATE_PARSE_EXCEPTION, e);
            errors.put(PARAM_DELIVERY_DATE, String.format(DATE_ERROR_MESSAGE, CORRECT_FORMATS.get(PARAM_DELIVERY_DATE)));
            return;
        }

        if(!DeliveryDateValidator.test(deliveryDate)) {
            errors.put(PARAM_DELIVERY_DATE, String.format(DATE_ERROR_MESSAGE, CORRECT_FORMATS.get(PARAM_DELIVERY_DATE)));
            return;
        }

        order.setDeliveryDate(deliveryDate);
    }

    private void getPaymentMethodParameter(HttpServletRequest req, Map<String, String> errors, Order order) {
        String value = req.getParameter(PARAM_PAYMENT_METHOD);

        if (value == null || StringUtils.isEmpty(value)) {
            logger.error(String.format(EMPTY_VALUE_ERROR_MESSAGE, PARAM_PAYMENT_METHOD), value);
            errors.put(PARAM_PAYMENT_METHOD, ERROR_MESSAGE);
            return;
        }

        PaymentMethod paymentMethod;

        try {
            paymentMethod = PaymentMethod.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.error(PAYMENT_METHOD_EXCEPTION, e);
            errors.put(PARAM_PAYMENT_METHOD, PAYMENT_METHOD_ERROR_MESSAGE);
            return;
        }

        order.setPaymentMethod(paymentMethod);
    }

    private void getRequiredParameter(HttpServletRequest req, String param, Map<String, String> errors,
                                      Consumer<String> consumer, Predicate predicate) {
        String value = req.getParameter(param);
        setParamToSession(req, param, value);

        if (value == null || StringUtils.isEmpty(value)) {
            logger.error(String.format(EMPTY_VALUE_ERROR_MESSAGE, param), value);
            errors.put(param, ERROR_MESSAGE);
            return;
        }

        if (!predicate.test(value)) {
            errors.put(param, String.format(DATA_ERROR_MESSAGE, CORRECT_FORMATS.get(param)));
            return;
        }

        consumer.accept(value);
    }

    private void setParamToSession(HttpServletRequest req, String param, String value) {
        HttpSession session = req.getSession();
        session.setAttribute(param, value);
    }

    private void setParamToAttribute(HttpServletRequest req, String param) {
        HttpSession session = req.getSession();
        String value = (String) session.getAttribute(param);

        if(StringUtils.isNotEmpty(value)) {
            req.setAttribute(param, value);
            session.removeAttribute(param);
        }
    }

    private void setAllParamsFromSession(HttpServletRequest req) {
        setParamToAttribute(req, PARAM_FIRST_NAME);
        setParamToAttribute(req, PARAM_LAST_NAME);
        setParamToAttribute(req, PARAM_PHONE);
        setParamToAttribute(req, PARAM_DELIVERY_DATE);
        setParamToAttribute(req, PARAM_DELIVERY_ADDRESS);
        setParamToAttribute(req, PARAM_PAYMENT_METHOD);
    }

    private void saveQueryString(HttpServletRequest req) {
        if(StringUtils.isEmpty(queryString))
            queryString = req.getQueryString();
    }

    private static final Predicate<String> FirstLastNameValidator =
            name -> name.matches("^[A-Z]{1}[a-zA-z]+$");

    private static final Predicate<String> PhoneValidator =
            phone -> phone.matches("^\\+375[0-9]{9}$");

    private static final Predicate<String> DeliveryAddressValidator =
            name -> name.matches("^[a-zA-Z0-9\\s,.-/#]+$");

    private static final Predicate<LocalDate> DeliveryDateValidator = date -> {
        LocalDate nowDate = LocalDate.now();
        return nowDate.isBefore(date) || nowDate.equals(date);
    };
}
