<%--

    ﻿Copyright (C) 2013
    by 52 North Initiative for Geospatial Open Source Software GmbH

    Contact: Andreas Wytzisk
    52 North Initiative for Geospatial Open Source Software GmbH
    Martin-Luther-King-Weg 24
    48155 Muenster, Germany
    info@52north.org

    This program is free software; you can redistribute and/or modify it under
    the terms of the GNU General Public License version 2 as published by the
    Free Software Foundation.

    This program is distributed WITHOUT ANY WARRANTY; even without the implied
    WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
    General Public License for more details.

    You should have received a copy of the GNU General Public License along with
    this program (see gnu-gpl v2.txt). If not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
    visit the Free Software Foundation web page, http://www.fsf.org.

--%>
<?xml version="1.0" encoding="utf-8"?>
<%@page import="java.util.Collections"%>
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



<p class="infotext">The latest check results (maximum of <%=supervisor.getMaximumNumberOfResults()%>,
automatic refresh every <%=supervisor.getPageRefreshIntervalSecs()%>
seconds):</p>

<ul>
	<%
		Collection<ICheckResult> results = supervisor.getCheckResultsReversed();
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

<p class="infotext">Admin contact: <%=supervisor.getAdminEmail()%> | More information: <a href="https://wiki.52north.org/bin/view/Sensornet/OwsSupervisor" title="OwsSupervisor @ 52&deg;North Wiki">52&deg;North Wiki: OwsSupervisor</a>.</p>

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