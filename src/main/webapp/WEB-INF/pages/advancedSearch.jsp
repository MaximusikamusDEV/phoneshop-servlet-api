<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<link rel="stylesheet" type="text/css" href="styles/table.css"/>

<tags:master pageTitle="Advanced Search">
    <h1>
        Advanced Search
    </h1>

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

</tags:master>