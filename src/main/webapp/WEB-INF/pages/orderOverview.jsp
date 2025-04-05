<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<tags:master pageTitle="Overview">
    <p>
        Cart: ${cart}
    </p>

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
                    <fmt:formatNumber value="${items.product.price}" type="currency"
                                      currencySymbol="${items.product.currency.symbol}"/>
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

    <table>

        <tags:overviewFormRow label="First name" name="firstName"/>
        <tags:overviewFormRow label="Last name" name="lastName"/>
        <tags:overviewFormRow label="Phone" name="phone"/>
        <tags:overviewFormRow label="Delivery date" name="deliveryDate"/>
        <tags:overviewFormRow label="Delivery address" name="deliveryAddress"/>
        <tags:overviewFormRow label="Payment method" name="paymentMethod"/>

    </table>


</tags:master>