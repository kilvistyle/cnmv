<%@page pageEncoding="UTF-8" isELIgnored="false"%>
<%@ include file="/cn/common/common.jsp"%>
<html>
<head>
<%@ include file="/cn/common/header.jsp"%>
<title>Search Tester</title>
<link rel="stylesheet" type="text/css" href="${f:url('/css/cn.css')}" />
</head>
<body>
Search Tester
<hr />
<form method="post" action="index">
<input type="hidden" name="save" value="true"/>
<input type="submit" value="SaveSearchKey" />
</form>
<form method="post" action="index">
Search : <input type="text" ${f:text("query")} />
<input type="submit" value="Search" />
</form>
<table>
  <thead>
    <tr>
      <c:forEach var="p" items="${propertyList}" varStatus="ps" >
        <th>${f:h(p.name)}</th>
        <c:if test="${p.primaryKey}">
          <th>ID/NAME</th>
        </c:if>
      </c:forEach>
    </tr>
  </thead>
  <tbody>
    <c:forEach var="m" items="${modelList}" varStatus="ms" >
      <tr>
      <c:forEach var="p" items="${propertyList}" varStatus="ps" >
        <c:choose>
          <c:when test="${p.primaryKey}">
            <td>${xf:h(xf:vi(ms.index, p.name))}</td>
            <td>${f:h(keyProp.id)}/${f:h(keyProp.name)}</td>
          </c:when>
          <c:when test="${(p.array || p.collection) && (elmLimit < xf:sizei(ms.index, p.name))}">
            <td><div class="toomany">* too many elements *</div></td>
          </c:when>
          <c:otherwise>
            <td>${xf:h(xf:vi(ms.index, p.name))}</td>
          </c:otherwise>
        </c:choose>
      </c:forEach>
      </tr>
    </c:forEach>
  </tbody>
</table>

<%@ include file="/cn/common/footer.jsp"%>
</body>
</html>