package com.es.phoneshop.cart;

import com.es.phoneshop.exceptions.ProductNotFoundException;
import com.es.phoneshop.exceptions.ProductOutOfStockException;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.productdao.HashMapProductDao;
import com.es.phoneshop.productdao.ProductDao;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DefaultCartService implements CartService {
    private static final String PRODUCT_NOT_FOUND_MESSAGE = "Product with id {} not found";
    private static final String PRODUCT_OUT_OF_STOCK_MESSAGE = "Product with id {} is out of stock";
    private static final String CART_SESSION_ATTRIBUTE = DefaultCartService.class.getName() + ".cart";
    private static final Logger logger = LoggerFactory.getLogger(DefaultCartService.class);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ProductDao productDao;

    private DefaultCartService() {
        productDao = HashMapProductDao.getInstance();
    }

    private static class SingletonHolder {
        private static final DefaultCartService INSTANCE = new DefaultCartService();
    }

    public static DefaultCartService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public Cart getCart(HttpServletRequest request) {
        lock.writeLock().lock();
        try {
            Cart cart = (Cart) request.getSession().getAttribute(CART_SESSION_ATTRIBUTE);

            if (cart == null) {
                cart = new Cart();
                request.getSession().setAttribute(CART_SESSION_ATTRIBUTE, cart);
            }

            return cart;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void add(Cart cart, Long productId, int quantity) throws ProductOutOfStockException {

        lock.writeLock().lock();

        try {
            try {
                Product product = productDao.getProduct(productId);

                Optional<CartItem> cartItem = cart.getCartItems().stream()
                        .filter(cartitem -> cartitem.getProduct().getId().equals(productId))
                        .findFirst();

                if (cartItem.isPresent()) {
                    updateCartItemQuantity(cartItem.get(), product, quantity);
                } else {
                    CartItem newCartItem = new CartItem(product, 0);
                    updateCartItemQuantity(newCartItem, product, quantity);

                    cart.getCartItems().add(newCartItem);
                }

            } catch (ProductNotFoundException e) {   //Not found in productDAO
                logger.error(PRODUCT_NOT_FOUND_MESSAGE, productId);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void updateCartItemQuantity(CartItem cartItem, Product product, int quantity) throws ProductOutOfStockException {
        int totalQuantity = cartItem.getQuantity() + quantity;

        if (totalQuantity > product.getStock()) {
            logger.error(PRODUCT_OUT_OF_STOCK_MESSAGE, product.getId());
            throw new ProductOutOfStockException(product, product.getStock(), quantity);
        }

        cartItem.setQuantity(totalQuantity);
    }
}
