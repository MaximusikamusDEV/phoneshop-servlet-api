package com.es.phoneshop.cart;

import com.es.phoneshop.exceptions.ProductNotFoundException;
import com.es.phoneshop.exceptions.ProductOutOfStockException;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.productdao.HashMapProductDao;
import com.es.phoneshop.productdao.ProductDao;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DefaultCartService implements CartService {
    private static final String PRODUCT_NOT_FOUND_MESSAGE = "Product with id {} not found";
    private static final boolean ENABLE_UPDATE = true;
    private static final boolean DISABLE_UPDATE = false;
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
                Optional<CartItem> cartItem = findCartItem(productId, cart);

                if (cartItem.isPresent()) {
                    addCartItemQuantity(cartItem.get(), product, quantity, DISABLE_UPDATE);
                } else {
                    if (product.getStock() < quantity)
                        throw new ProductOutOfStockException(product, product.getStock(), quantity);

                    CartItem newCartItem = new CartItem(product);
                    cart.getCartItems().add(newCartItem);
                    addCartItemQuantity(newCartItem, product, quantity, DISABLE_UPDATE);
                }
                recalculateCartPriceAndQuantity(cart);
            } catch (ProductNotFoundException e) {
                logger.error(PRODUCT_NOT_FOUND_MESSAGE + "{}", e, productId);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void update(Cart cart, Long productId, int quantity) throws ProductOutOfStockException, ProductNotFoundException {
        lock.writeLock().lock();

        try {
            Product product = productDao.getProduct(productId);
            Optional<CartItem> cartItem = findCartItem(productId, cart);

            if (!cartItem.isPresent()) {
                throw new ProductNotFoundException(productId);
            }

            addCartItemQuantity(cartItem.get(), product, quantity, ENABLE_UPDATE);
            recalculateCartPriceAndQuantity(cart);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void delete(Cart cart, Long productId) {
        cart.getCartItems().removeIf(cartItem ->
                productId.equals(cartItem.getProduct().getId()));

        recalculateCartPriceAndQuantity(cart);
    }

    private void recalculateCartPriceAndQuantity(Cart cart) {
        recalculateCartTotalPrice(cart);
        recalculateCartTotalQuantity(cart);
    }

    private void recalculateCartTotalQuantity(Cart cart) {
        cart.setTotalQuantity(
                cart.getCartItems().stream().map(CartItem::getQuantity).mapToInt(Integer::intValue).sum()
        );
    }

    private void recalculateCartTotalPrice(Cart cart) {
        cart.setTotalPrice(
                cart.getCartItems().stream()
                        .map(cartItem -> cartItem.getProduct().getPrice()
                                .multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );
    }

    private Optional<CartItem> findCartItem(Long productId, Cart cart) throws ProductNotFoundException {
        return cart.getCartItems().stream()
                .filter(cartItem -> cartItem.getProduct().getId().equals(productId))
                .findFirst();
    }

    private void addCartItemQuantity(CartItem cartItem, Product product, int quantity, boolean isUpdate) throws ProductOutOfStockException {
        if (quantity <= 0) {
            throw new ProductOutOfStockException(product, product.getStock(), quantity);
        }

        int totalQuantity = quantity;

        if(!isUpdate) {
           totalQuantity = cartItem.getQuantity() + quantity;
        }

        if (totalQuantity > product.getStock()) {
            logger.error(PRODUCT_OUT_OF_STOCK_MESSAGE, product.getId());
            throw new ProductOutOfStockException(product, product.getStock(), quantity);
        }

        cartItem.setQuantity(totalQuantity);
    }
}
