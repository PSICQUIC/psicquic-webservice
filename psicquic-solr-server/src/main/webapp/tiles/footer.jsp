<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="t" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<div id="footer">
 <table width="100%" cellpadding="0" cellspacing="0">
  <tr>
   <td>
    <table width="100%" class="footer" cellpadding="0" cellspacing="0">
     <s:if test="#session['USER_ROLE'].administrator != null" >
      <t:insertAttribute name="edit" ignore="true"/>
     </s:if>
     <tr>
      <td class="copyright2" nowrap>
       Copyright 2013 IMEx Consortium
      </td>
      <td width="5%" class="copyright3" align="center" nowrap>
       <A HREF="http://www.imexconsortium.org">Imex Consortium</A>.
      </td>
     </tr>
    </table>
   </td>
  </tr>
  <tr>
   <td align="center" width="100%">
    <table width="98%" cellspacing="0">
     <tr>
      <td align="left" nowrap>
       <font size="-5">Ver: ${mpsq.version} (${mpsq.bld})</font>
      </td>
      <td align="right" nowrap>
       <font size="-5">
         <i>Code by:<A HREF="mailto:lukasz@mbi.ucla.edu">LS</A></i>
       </font>
      </td>
     </tr>
    </table>
   </td>
  </tr>
 </table>
</div>
