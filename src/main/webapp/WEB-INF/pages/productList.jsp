<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="products" type="java.util.ArrayList" scope="request"/>
<tags:master pageTitle="Product List">
    <p>
        Welcome to Expert-Soft training!
    </p>

    <link rel="stylesheet" type="text/css" href="styles/priceHistory.css"/>

    <form>
        <label>
            Enter product name:
            <input name="findProductQuery" value="${param.findProductQuery}">
        </label>

        <button>Search</button>
    </form>

    <table>
        <thead>
        <tr>
            <td>Image</td>
            <td>
                Description
                <tags:sortLink sortField="description" sortOrder="asc"/>
                <tags:sortLink sortField="description" sortOrder="desc"/>
            </td>
            <td class="price">
                Price
                <tags:sortLink sortField="price" sortOrder="asc"/>
                <tags:sortLink sortField="price" sortOrder="desc"/>
            </td>
        </tr>
        </thead>
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
            </tr>
        </c:forEach>
    </table>

</tags:master>