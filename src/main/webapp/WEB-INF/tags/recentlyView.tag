<%@ tag trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ attribute name="recentlyViewed" required="true" type="java.util.List" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<link rel="stylesheet" type="text/css" href="${pageContext.servletContext.contextPath}/styles/recentlyViewed.css"/>

<h2>Recently viewed</h2>

<div class="recently-viewed-container">
    <c:forEach var="product" items="${recentlyViewed}">
        <span class="recently-viewed-box">

        <a href="${pageContext.servletContext.contextPath}/products/${product.id}">
            <img class="product-tile" src="${product.imageUrl}"/>
            <p>${product.description}</p>
        </a>

        <p><fmt:formatNumber value="${product.price}" type="currency"
                             currencySymbol="${product.currency.symbol}"/> </p>
        </span>

    </c:forEach>
</div>