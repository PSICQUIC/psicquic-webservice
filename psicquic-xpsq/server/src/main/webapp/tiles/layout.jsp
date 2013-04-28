<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="t" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%-- ===========================================================================
 ! $HeadURL:: https://imex.mbi.ucla.edu/svn/central/trunk/icentral/src/main/we#$
 ! $Id:: layout.jsp 394 2013-02-05 19:59:08Z zplat                             $
 ! Version: $Rev:: 394                                                         $
 !========================================================================= --%>
<html lang="en">
 <head>
 <title>
  <t:getAsString name="title"/>
 </title>
 <t:insertDefinition name="htmlhead"/>
 </head>
 <body class="yui-skin-sam">
 <iframe id="yui-history-iframe" src="img/xpsq_logo_small.png"></iframe>
 <input id="yui-history-field" type="hidden">

  <div id="page">
   <t:insertDefinition name="header"/>
   <table class='center' width="95%" cellspacing="0" cellpadding="0">
    <tr>
     <td class="content">
       <t:insertAttribute name="body" />
     </td>
    </tr>
   </table>
   <t:insertDefinition name="footer" />
  </page>
 <body>
</html>

