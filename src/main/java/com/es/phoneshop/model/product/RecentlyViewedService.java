package com.es.phoneshop.model.product;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.LinkedList;
import java.util.List;

public class RecentlyViewedService {
    private static final String RECENTLY_VIEWED_ATTRIBUTE = RecentlyViewedService.class.getName() + ".recentlyViewed";
    private static final int MAX_ITEMS_AMOUNT = 3;

    private RecentlyViewedService() {
    }

    private static class SingletonHolder {
        private static final RecentlyViewedService INSTANCE = new RecentlyViewedService();
    }

    public static RecentlyViewedService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void add(HttpServletRequest request, Product product) {
        HttpSession session = request.getSession();

        List<Product> recentlyViewed = getRecentlyViewed(session);

        recentlyViewed.remove(product);
        recentlyViewed.add(0, product);

        if (recentlyViewed.size() > MAX_ITEMS_AMOUNT) {
            recentlyViewed.remove(recentlyViewed.size() - 1);
        }

        session.setAttribute(RECENTLY_VIEWED_ATTRIBUTE, recentlyViewed);
    }

    public List<Product> getRecentlyViewed(HttpSession session) {
        List<Product> recentlyViewed = (List<Product>) session.getAttribute(RECENTLY_VIEWED_ATTRIBUTE);

        if (recentlyViewed == null) {
            recentlyViewed = new LinkedList<>();
            session.setAttribute(RECENTLY_VIEWED_ATTRIBUTE, recentlyViewed);
        }

        return recentlyViewed;
    }
}
