<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="product" type="com.es.phoneshop.model.product.Product" scope="request"/>
<tags:master pageTitle="Product Details">

    <p>
        Cart: ${cart}
    </p>

    <c:if test="${not empty param.message && empty error}">
        <div class="success">
            ${param.message}
        </div>
    </c:if>

    <c:if test="${not empty error}">
        <div class="error">
                Error during adding to cart.
        </div>
    </c:if>

    <p>
            ${product.description}
    </p>

    <form method="post">
        <table>
            <tr>
                <td>Image</td>
                <td>
                    <img class="img" src="${product.imageUrl}"/>
                </td>
            </tr>

            <tr>

                <td>Price</td>
                <td class="price">
                        ${product.price}
                </td>
            </tr>

            <tr>
                <td>Code</td>
                <td class="code">
                        ${product.code}
                </td>
            </tr>

            <tr>
                <td>Stock</td>
                <td class="stock">
                        ${product.stock}
                </td>
            </tr>

            <tr>
                <td>Quantity</td>
                <td class="quantity">
                    <input class="quantity" name="quantity" value="${not empty inputQuantity ? inputQuantity : 1}"/>

                    <c:if test="${not empty error}">
                        <div class="error">${error}</div>
                    </c:if>
                </td>



            </tr>

        </table>

        <button>Add to Cart</button>

    </form>

    <tags:recentlyView recentlyViewed="${param.recentlyViewed}"/>

</tags:master>