<%@ tag trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="price" required="true" %>
<%@ attribute name="currSymbol" required="true" %>

<tr class="price">
    <td></td>
    <td></td>
    <td>${name}</td>
    <td><fmt:formatNumber value="${price}"
                          type="currency"
                          currencySymbol="${currSymbol}"/></td>
</tr>

