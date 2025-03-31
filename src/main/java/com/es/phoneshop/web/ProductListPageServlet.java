package com.es.phoneshop.web;

import com.es.phoneshop.model.product.HashMapProductDao;
import com.es.phoneshop.model.product.SortField;
import com.es.phoneshop.model.product.SortOrder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

public class ProductListPageServlet extends HttpServlet {

    private HashMapProductDao productDao;

    @Override
    public void init() throws ServletException {
        super.init();
        productDao = HashMapProductDao.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String findProductQuery = request.getParameter("findProductQuery");
        String sortField = request.getParameter("sortField");
        String sortOrder = request.getParameter("sortOrder");

        request.setAttribute("products", productDao.findProducts(findProductQuery,
                Optional.ofNullable(sortField)
                        .map(
                                sortfield -> SortField.valueOf(sortfield.toUpperCase())
                        )
                        .orElse(null),
                Optional.ofNullable(sortOrder)
                        .map(
                                sortorder -> SortOrder.valueOf(sortorder.toUpperCase())
                        )
                        .orElse(null)));


        request.getRequestDispatcher("/WEB-INF/pages/productList.jsp").forward(request, response);
    }

}
