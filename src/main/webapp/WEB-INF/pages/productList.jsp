<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:useBean id="products" type="java.util.List" scope="request"/>
<tags:master pageTitle="Product List">
    <p>
        Welcome to Expert-Soft training!
    </p>

    <link rel="stylesheet" type="text/css" href="styles/priceHistory.css"/>

    <c:if test="${not empty param.message && empty errors}">
        <div class="success">
                ${param.message}
        </div>
    </c:if>

    <c:if test="${not empty errors}">
        <div class="error">
            Error during adding to cart.
        </div>
    </c:if>

    <form>
        <label>
            Enter product name:
            <input name="findProductQuery" value="${param.findProductQuery}">
        </label>

        <button>Search</button>
    </form>

    <form method="POST" action="${pageContext.servletContext.contextPath}/products">
        <table>
            <thead>
            <tr>
                <td>Image</td>
                <td>
                    Description
                    <tags:sortLink sortField="description" sortOrder="asc"/>
                    <tags:sortLink sortField="description" sortOrder="desc"/>
                </td>

                <td class="quantity">
                    Quantity
                </td>

                <td class="price">
                    Price
                    <tags:sortLink sortField="price" sortOrder="asc"/>
                    <tags:sortLink sortField="price" sortOrder="desc"/>
                </td>

                <td></td>
            </tr>
            </thead>
            <c:forEach var="product" items="${products}" varStatus="status">
                <tr>
                    <td>
                        <img class="product-tile" src="${product.imageUrl}">
                    </td>
                    <td>
                        <a href="${pageContext.servletContext.contextPath}/products/${product.id}">
                                ${product.description}
                        </a>
                    </td>

                    <td class="quantity">
                        <fmt:formatNumber value="${not empty allQuantities[product.id] ? allQuantities[product.id] : 1}" var="defaultQuantity"/>


                        <c:set var="error" value="${errors[product.id]}"/>
                        <input class="quantity" name="quantity"
                               value="${not empty error && not empty inputQuantities ? inputQuantities[status.index] :
                            not empty allQuantities ? allQuantities[product.id] : defaultQuantity}">

                        <c:if test="${not empty error}">
                            <div class="error">
                                    ${error}
                            </div>
                        </c:if>

                        <input type="hidden" name="productId" value="${product.id}"/>


                    </td>

                    <td class="price">
                        <details>
                            <summary>
                                <fmt:formatNumber value="${product.price}" type="currency"
                                                  currencySymbol="${product.currency.symbol}"/>
                            </summary>
                            <div class="price-box">
                                <h1>Price history</h1>
                                <h2>${product.description}</h2>
                                <h3>Start date Price</h3>

                                <c:forEach var="historyItem" items="${product.priceHistoryProductList}">
                                    <p>
                                            ${historyItem.date} <fmt:formatNumber value="${historyItem.price}"
                                                                                  type="currency"
                                                                                  currencySymbol="${product.currency.symbol}"/>
                                    </p>
                                </c:forEach>
                            </div>
                        </details>
                    </td>

                    <td>
                        <button name="action" value="${product.id}">
                            Add to cart
                        </button>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </form>

    <tags:recentlyView recentlyViewed="${param.recentlyViewed}"/>

</tags:master>