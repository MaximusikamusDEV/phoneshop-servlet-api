package com.es.phoneshop.web;

import com.es.phoneshop.model.product.ArrayListProductDao;
import com.es.phoneshop.model.product.Product;

import com.es.phoneshop.model.product.ProductNotFoundException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;


public class ProductListPageServlet extends HttpServlet {

    private ArrayListProductDao productDao;

    @Override
    public void init() throws ServletException {
        super.init();
        productDao = new ArrayListProductDao();
        productDao.setSampleProducts();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            request.setAttribute("products", productDao.findProducts());
        } catch (ProductNotFoundException e) {
            request.setAttribute("products", new ArrayList<Product>());
        }

        request.getRequestDispatcher("/WEB-INF/pages/productList.jsp").forward(request, response);
    }

}
