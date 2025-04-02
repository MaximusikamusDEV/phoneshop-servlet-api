<%@ tag trimDirectiveWhitespaces="true" %>
<%@ attribute name="sortField" required="true" %>
<%@ attribute name="sortOrder" required="true" %>
<link rel="stylesheet" type="text/css" href="styles/arrows.css"/>

<a href="?findProductQuery=${param.findProductQuery}&sortField=${sortField}&sortOrder=${sortOrder}">
        <span class="${sortField eq param.sortField and sortOrder eq param.sortOrder ? 'sort-arrow-active' : 'sort-arrow-inactive'}">
            ${sortOrder eq 'asc' ? "&#8595;" : "&#8593;"}
        </span>
</a>
