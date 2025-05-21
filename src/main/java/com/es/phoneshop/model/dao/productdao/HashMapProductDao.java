package com.es.phoneshop.model.dao.productdao;

import com.es.phoneshop.model.dao.abstractdao.AbstractMapDao;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.exceptions.ProductNotFoundException;
import com.es.phoneshop.sortenums.SortField;
import com.es.phoneshop.sortenums.SortOrder;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

public class HashMapProductDao extends AbstractMapDao<Long, Product, ProductNotFoundException> implements ProductDao {
    private static final String PRODUCT_NOT_FOUND_DELETE_MESSAGE = "Product with id {} not found, While delete";
    private static final String PRODUCT_NOT_FOUND_GET_MESSAGE = "Product with id {} not found. While getProduct";
    private static final Logger logger = LoggerFactory.getLogger(HashMapProductDao.class);

    private HashMapProductDao() {
        super();
    }

    @Override
    protected ProductNotFoundException createException(String message, Long key) throws ProductNotFoundException {
        return new ProductNotFoundException(key);
    }

    private static class SingletonHolder {
        private static final HashMapProductDao INSTANCE = new HashMapProductDao();
    }

    public static HashMapProductDao getInstance(){
        return SingletonHolder.INSTANCE;
    }

    @Override
    public Product getProduct(Long id) throws ProductNotFoundException {
        return get(id, PRODUCT_NOT_FOUND_GET_MESSAGE);
    }

    @Override
    public List<Product> findProducts(String findProductQuery, SortField sortField, SortOrder sortOrder) {
        lock.readLock().lock();

        try {
            //First of all comparing by relevance
            Comparator<Product> comparator = Comparator.comparingDouble(product ->
                    (-1) * calculateRelevance(product, findProductQuery)
            );
            //In case we have sortOrder - change comparing
            if (sortOrder != null) {
                Comparator<Product> sortOrderComparator = Optional.ofNullable(sortField)
                        .filter(SortField.DESCRIPTION::equals)
                        .map(sortfield -> Comparator.comparing(Product::getDescription))
                        .orElse(Comparator.comparing(Product::getPrice));

                if(SortOrder.DESC.equals(sortOrder)) {
                    sortOrderComparator = sortOrderComparator.reversed();
                }
                comparator = sortOrderComparator;
            }

            List<Product> filteredProducts = dataMap.values().stream()
                    .filter(product -> productDescriptionContainsQuery(product, findProductQuery))
                    .filter(this::productHasNotNullPrice)
                    .filter(this::productIsInStock)
                    .sorted(comparator)
                    .toList();

            return filteredProducts;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Product> advancedFindProducts(String description, String descriptionOption, String minPrice, String maxPrice) {
        lock.readLock().lock();

        try {
            //First of all comparing by relevance
            Comparator<Product> comparator = Comparator.comparingDouble(product ->
                    (-1) * calculateRelevance(product, description)
            );

            List<Product> filteredProducts = dataMap.values().stream()
                    .filter(product -> productDescriptionContainsQuery(product, description))
                    .filter(this::productHasNotNullPrice)
                    .filter(this::productIsInStock)
                    .sorted(comparator)
                    .toList();

            return filteredProducts;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void save(Product product) {
        lock.writeLock().lock();

        try {
            if (product.getId() == null) {
                product.setId(maxId.getAndIncrement());
                dataMap.put(product.getId(), product);
            } else {
                dataMap.put(product.getId(), product);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void delete(Long id) throws ProductNotFoundException {
        lock.writeLock().lock();

        try {
            if (dataMap.remove(id) == null) {
                logger.error(PRODUCT_NOT_FOUND_DELETE_MESSAGE, id);
                throw new ProductNotFoundException(id);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private boolean productIsInStock(Product product) {
        return product.getStock() > 0;
    }

    private boolean productHasNotNullPrice(Product product) {
        return product.getPrice() != null;
    }

    private boolean productDescriptionContainsQuery(Product product, String findProductQuery) {
        if (StringUtils.isEmpty(findProductQuery))
            return true;

        String[] partsFindProductQuery = findProductQuery.toLowerCase().split(" ");
        String productDescription = product.getDescription().toLowerCase();

        return Stream.of(partsFindProductQuery)
                .anyMatch(productDescription::contains);
    }

    private double calculateRelevance(Product product, String findProductQuery) {
        if (product == null || StringUtils.isEmpty(findProductQuery))
            return 0.0;

        String productDescription = product.getDescription().toLowerCase();
        String[] partsProductDescription = productDescription.split(" ");
        String[] partsFindProductQuery = findProductQuery.toLowerCase().split(" ");

        if(partsProductDescription.length == 0 || partsFindProductQuery.length == 0)
            return 0.0;

        //Count same words in Query and Description
        long identicalWords = Stream.of(partsFindProductQuery)
                .filter(productDescription::contains)
                .count();

        // % of equal words in Description and Query
        double countDescriptionMatches = (double) identicalWords / partsProductDescription.length;
        double countQueryMatches = (double) identicalWords / partsFindProductQuery.length;

        return countDescriptionMatches * countQueryMatches * 100;
    }
}
