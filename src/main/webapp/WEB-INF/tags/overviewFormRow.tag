<%@ tag trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ attribute name="label" required="true" %>
<%@ attribute name="name" required="true" %>

<tr>
    <td>${label}</td>
    <td>
        ${order[name]}
    </td>
</tr>

