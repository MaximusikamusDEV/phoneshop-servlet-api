<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<link rel="stylesheet" type="text/css" href="styles/table.css"/>
<jsp:useBean id="products" type="java.util.List" scope="request"/>

<tags:master pageTitle="Advanced Search">
    <h1>
        Advanced Search
    </h1>


    <form method="POST" action="${pageContext.servletContext.contextPath}/advancedSearch">
    <table class="table">

        <tr>
            <td>Description</td>

            <td>
                <input name="description">
            </td>

            <td>
                <select name="descriptionOption">
                    <option value="allWords">all words</option>
                    <option value="anyWord">any word</option>
                </select>
            </td>
        </tr>

        <tr>
            <td>Min price</td>

            <td>
                <input name="minPrice">
            </td>
        </tr>

        <tr>
            <td>Max price</td>

            <td>
                <input name="maxPrice">
            </td>
        </tr>

    </table>

    <br>

    <button>Search</button>
    </form>


    <table>
        <thead>
        <tr>
            <td>Image</td>
            <td>Description</td>
            <td>Price</td>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="product" items="${products}">
            <tr>
                <td>
                    <img class="product-tile" src="${product.imageUrl}">
                </td>
                <td>
                    <a href="${pageContext.servletContext.contextPath}/products/${product.id}">
                            ${product.description}
                    </a>
                </td>
                <td>
                    <fmt:formatNumber value="${product.price}" type="currency"
                                      currencySymbol="${product.currency.symbol}"/>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

</tags:master>