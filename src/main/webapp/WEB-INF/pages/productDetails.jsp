<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="product" type="com.es.phoneshop.model.product.Product" scope="request"/>

<tags:master pageTitle="Product Details">
    <p>
            ${product.description}
    </p>

    <table>
        <tr>
            <td>Image</td>
            <td>
                <img class="product-tile" src="${product.imageUrl}" style="max-width: 100%; max-height: 100%;"/>
            </td>
        </tr>

        <tr>
            <td>Price</td>
            <td>
                    ${product.price}
            </td>
        </tr>

        <tr>
            <td>Code</td>
            <td>
                    ${product.code}
            </td>
        </tr>

        <tr>
            <td>Stock</td>
            <td>
                    ${product.stock}
            </td>
        </tr>
    </table>

</tags:master>