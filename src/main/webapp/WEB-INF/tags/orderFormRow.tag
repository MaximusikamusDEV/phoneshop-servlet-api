<%@ tag trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ attribute name="label" required="true" %>
<%@ attribute name="errors" required="true" type="java.util.Map" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="type" required="true" %>
<%@ attribute name="placeholder" required="true" %>
<%@ attribute name="paramName" required="true" %>

<tr>
    <td>${label}<span style="color: red">*</span></td>
    <td>
        <c:set var="error" value="${errors[name]}"/>
        <input class="input" placeholder="${placeholder}" type="${type}" name="${name}"
               value="${not empty errors ? paramName : order[name]}"/>
        <c:if test="${not empty error}">
            <div class="error">
                    ${error}
            </div>
        </c:if>
    </td>
</tr>

