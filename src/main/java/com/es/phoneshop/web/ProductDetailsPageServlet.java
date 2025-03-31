package com.es.phoneshop.web;

import com.es.phoneshop.model.product.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ProductDetailsPageServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ProductDetailsPageServlet.class);
    private HashMapProductDao productDao;

    @Override
    public void init() throws ServletException {
        super.init();
        productDao = HashMapProductDao.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String productIdUri = request.getPathInfo();


        try {

            Long productId = Long.valueOf(productIdUri.substring(1));

            request.setAttribute("product", productDao.getProduct(productId));
            request.getRequestDispatcher("/WEB-INF/pages/productDetails.jsp").forward(request, response);
        }
        catch (NumberFormatException e) {
            logger.error("Invalid product ID: {}", productIdUri);
            request.getRequestDispatcher("/WEB-INF/pages/error.jsp").forward(request, response);
        }
        catch (ProductNotFoundException e) {
            logger.error("Product not found: {}", productIdUri);
            request.setAttribute("exception", e);
            request.getRequestDispatcher("/WEB-INF/pages/errorProductNotFound.jsp").forward(request, response);
        }

    }

}
