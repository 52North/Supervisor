<?xml version="1.0" encoding="utf-8"?>
<%@page import="org.n52.owsSupervisor.ICheckResult"%>
<%@page import="java.util.Collection"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<jsp:useBean id="supervisor"
	class="org.n52.owsSupervisor.ui.SupervisorBean" scope="application" />

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>OWS Supervisor</title>

<meta http-equiv="content-type" content="text/html;charset=utf-8" />
<meta http-equiv="Refresh"
	content="<%=supervisor.getPageRefreshIntervalSecs()%>" />

<link href="styles.css" rel="stylesheet" type="text/css" />
<link href="<%=request.getContextPath()%>/favicon.ico"
	rel="shortcut icon" type="image/x-icon" />

</head>

<body>

<div id="content">

<div id="header">
<div id="headline"><a href="<%=request.getContextPath()%>"
	title="Home"> <span class="title">OWS Supervisor</span><br />
<span class="infotext">OGC Web Service Supervisor Version <%=supervisor.getVersion()%></span></a></div>
<div id="logos"><a href="http://52north.org"
	title="52° North Open Source Initiative"> <img
	src="<%=request.getContextPath()%>/images/logo.gif" height="62"
	alt="52N logo" /></a></div>

</div>



<p class="infotext">The latest che ck results (maximum of <%=supervisor.getMaximumNumberOfResults()%>,
automatic refresh every <%=supervisor.getPageRefreshIntervalSecs()%>
seconds):</p>

<ul>
	<%
		Collection<ICheckResult> results = supervisor.getCheckResults();
		for (ICheckResult current : results) {
			String style = "";
			switch (current.getType()) {
			case POSITIVE:
				style = "checkPositive";
				break;
			case NEGATIVE:
				style = "checkNegative";
				break;
			case NEUTRAL:
				style = "checkNeutral";
				break;
			}
	%>
	<li><span class="checkTime"><%=current.getTimeOfCheck()%></span>:
	<span class="checkService"><%=current.getCheckIdentifier()%></span> - <span
		class="<%=style%>"><%=current.getResult()%></span></li>
	<%
		}
	%>
</ul>

<p class="infotext">Developer contact: Daniel N&uuml;st,
d.nuest@52north.org | More information: <a href="https://wiki.52north.org/bin/view/Sensornet/OwsSupervisor" title="OwsSupervisor @ 52North Wiki">52North Wiki: OwsSupervisor</a>.</p>

</div>

<div class="center"><a
	href="http://validator.w3.org/check?uri=referer"> <img
	src="<%=request.getContextPath()%>/images/valid-xhtml11.png"
	alt="Valid XHTML 1.1" /> </a> <a
	href="http://jigsaw.w3.org/css-validator/check/referer"> <img
	src="<%=request.getContextPath()%>/images/vcss.gif" alt="CSS is valid!" />
</a></div>

</body>
</html>