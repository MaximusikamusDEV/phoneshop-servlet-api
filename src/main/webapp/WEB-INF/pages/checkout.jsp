<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<tags:master pageTitle="Checkout">
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
            Error during placing order.
        </div>
    </c:if>

    <link rel="stylesheet" type="text/css" href="styles/priceHistory.css"/>


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

        </tr>
        </thead>
        <c:forEach var="items" items="${orderItems}" varStatus="status">
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
                        ${quantity}
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
            </tr>


        </c:forEach>

        <tags:cost name="Subtotal: " price="${order.subtotal}"
                   currSymbol="${order.cartItems[0].product.currency.symbol}"/>

        <tags:cost name="Delivery Cost: " price="${order.deliveryCost}"
                   currSymbol="${order.cartItems[0].product.currency.symbol}"/>

        <tags:cost name="Total price: " price="${order.totalPrice}"
                   currSymbol="${order.cartItems[0].product.currency.symbol}"/>
    </table>

    <p></p>

    <form method="POST">
        <table>

            <tags:orderFormRow label="First name" errors="${errors}" name="firstName" type="text"
                               placeholder="e.g. Maxim, Viktoriya" paramName="${firstName}"/>
            <tags:orderFormRow label="Last name" errors="${errors}" name="lastName" type="text"
                               placeholder="e.g. Motevich, McGregor" paramName="${lastName}"/>
            <tags:orderFormRow label="Phone" errors="${errors}" name="phone" type="tel"
                               placeholder="e.g. +375336727408" paramName="${phone}"/>
            <tags:orderFormRow label="Delivery date" errors="${errors}" name="deliveryDate"
                               type="date" placeholder="YEAR-MONTH-DAY" paramName="${deliveryDate}"/>
            <tags:orderFormRow label="Delivery address" errors="${errors}" name="deliveryAddress" type="text"
                               placeholder="e.g. 192 Gorkogo street." paramName="${deliveryAddress}"/>

            <tr>
                <td>Payment method<span style="color: red">*</span></td>
                <td>
                    <c:set var="error" value="${errors['paymentMethod']}"/>
                    <c:set var="selectedMethod" value="${not empty errors ? param['paymentMethod'] : order['paymentMethod']}"/>

                    <select name="paymentMethod">
                        <option value="" disabled>Select payment method</option>

                        <c:forEach var="method" items="${paymentMethods}">
                            <option value="${method}" ${selectedMethod == method ? 'selected' : ''}>${method}</option>
                        </c:forEach>
                    </select>

                    <c:if test="${not empty error}">
                        <div class="error">
                                ${error}
                        </div>
                    </c:if>
                </td>
            </tr>
        </table>

        <p>
            <button type="submit" name="submit">Submit order</button>
        </p>
    </form>

    <form id="deleteCartItem" method="POST">
    </form>

</tags:master>