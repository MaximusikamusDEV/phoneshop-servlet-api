<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<tags:master pageTitle="Cart">
    <p>
        Cart: ${cart}
    </p>

    <c:if test="${not empty param.message && empty errors}">
        <div class="success">
                ${param.message}
        </div>
    </c:if>

    <c:if test="${not empty errors}">
        <div class="error">
            Error during updating cart.
        </div>
    </c:if>

    <link rel="stylesheet" type="text/css" href="styles/priceHistory.css"/>

    <form method="POST" action="${pageContext.servletContext.contextPath}/cart">
        <table>
            <thead>
            <tr>
                <td>Image</td>
                <td>
                    Description
                </td>
                <td class="quantity">
                    Quantity
                </td>

                <td class="price">
                    Price
                </td>

                <td></td>
            </tr>
            </thead>
            <c:forEach var="items" items="${cartItems}" varStatus="status">
                <tr>
                    <td>
                        <img class="product-tile" src="${items.product.imageUrl}">
                    </td>
                    <td>
                        <a href="${pageContext.servletContext.contextPath}/products/${items.product.id}">
                                ${items.product.description}
                        </a>
                    </td>

                    <td class="quantity">
                        <fmt:formatNumber value="${items.quantity}" var="quantity"/>

                        <c:set var="error" value="${errors[items.product.id]}"/>

                        <input class="quantity" name="quantity"
                               value="${not empty error && not empty inputQuantities ? inputQuantities[status.index] : quantity}">

                        <c:if test="${not empty error}">
                            <div class="error">
                                    ${error}
                            </div>
                        </c:if>

                        <input type="hidden" name="productId" value="${items.product.id}"/>
                    </td>


                    <td class="price">
                        <details>
                            <summary>
                                <fmt:formatNumber value="${items.product.price}" type="currency"
                                                  currencySymbol="${items.product.currency.symbol}"/>
                            </summary>
                            <div class="price-box">
                                <h1>Price history</h1>
                                <h2>${items.product.description}</h2>
                                <h3>Start date Price</h3>

                                <c:forEach var="historyItem" items="${items.product.priceHistoryProductList}">
                                    <p>
                                            ${historyItem.date} <fmt:formatNumber value="${historyItem.price}"
                                                                                  type="currency"
                                                                                  currencySymbol="${items.product.currency.symbol}"/>
                                    </p>
                                </c:forEach>
                            </div>
                        </details>
                    </td>

                    <td>
                        <button form="deleteCartItem"
                                formaction="${pageContext.servletContext.contextPath}/cart/deleteCartItem/${items.product.id}">
                            Delete
                        </button>
                    </td>

                </tr>


            </c:forEach>

            <tr>
                <td></td>
                <td></td>
                <td class="quantity">Total quantity: ${cart.totalQuantity}</td>
                <td class="price">Total price: <fmt:formatNumber value="${cart.totalPrice}"
                                                   type="currency"
                                                   currencySymbol="${items[0].product.currency.symbol}"/></td>
            </tr>
        </table>
        <p>
            <button>Update</button>
            <button formaction="${pageContext.servletContext.contextPath}/checkout" formmethod="get">Checkout</button>
        </p>
    </form>

    <form id="deleteCartItem" method="POST">
    </form>

</tags:master>