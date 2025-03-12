package com.es.phoneshop.model.product;

import org.codehaus.plexus.util.StringUtils;

import java.util.Optional;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class HashMapProductDao implements ProductDao {

    private long maxId;
    private final Map<Long, Product> products;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private HashMapProductDao() {
        this.products = new HashMap<>();
        maxId = 0L;
    }

    private static class SingletonHolder {
        private static final HashMapProductDao INSTANCE = new HashMapProductDao();
    }

    public static HashMapProductDao getInstance(){
        return SingletonHolder.INSTANCE;
    }

    @Override
    public Product getProduct(Long id) throws ProductNotFoundException {
        lock.readLock().lock();

        try {
            Product product = products.get(id);

            if (product == null) {
                throw new ProductNotFoundException(id);
            }

            return product;
        } finally {
            lock.readLock().unlock();
        }
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

            List<Product> filteredProducts = products.values().stream()
                    .filter(product -> productDescriptionContainsQuery(product, findProductQuery))
                    .filter(this::productHasNotNullPrice)
                    .filter(this::productIsInStock)
                    .sorted(
                            comparator
                    )
                    .collect(Collectors.toList());


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
                product.setId(maxId++);
                products.put(product.getId(), product);
            } else {
                products.put(product.getId(), product);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void delete(Long id) throws ProductNotFoundException {
        lock.writeLock().lock();

        try {
            if (products.remove(id) == null) {
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

        return Stream.of(partsFindProductQuery)
                .anyMatch(product.getDescription().toLowerCase()::contains);
    }

    private double calculateRelevance(Product product, String findProductQuery) {
        if (product == null || StringUtils.isEmpty(findProductQuery))
            return 0.0;

        String productDescription = product.getDescription().toLowerCase();
        String[] partsProductDescription = productDescription.split(" ");
        String[] partsFindProductQuery = findProductQuery.toLowerCase().split(" ");

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
