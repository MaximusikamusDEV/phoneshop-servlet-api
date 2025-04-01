<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<tags:master pageTitle="Product Not Found">
    <h1> Product with <c:out value="${exception.productId}" /> code not found.</h1>
</tags:master>