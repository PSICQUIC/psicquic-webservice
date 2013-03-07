<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="t" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
   
<table width="100%" cellspacing="0" cellpadding="0" border="0">
 <tr>
  <td class="menulogo" rowspan="3" width="1%">
   <table width="100%" cellspacing="0" border="0">
    <tr>
     <td class="logo" valign="middle">
      <a href="."><img height="55" border="0" src="img/psicquic-logo-2-small.png" alt="MPSQ"/></a>
     </td>
    </tr>
   </table>
  </td>
<!-- top menu -->
  <td class="topmenu" align="left">
   <table class="menu1_background" width="100%" cellspacing="0" cellpadding="0">
    <tr>
     <s:if test='menuContext.jsonConfig.menu[0].menu.size > 0'>
      <td class="menu1first">&nbsp;</td>
       <s:iterator value="menuContext.jsonConfig.menu[0].menu" id="exp" status="mpos">
        <s:set name="itemon" value="#exp.roles.{? #this in #session['USER_ROLE'].keySet()}.size > 0"/>
        <s:if test="#exp.roles==null || #exp.roles.size==0 || #itemon ">
         <s:if test='#mpos.count==menuSel[0]'>
          <td class="menu1select" nowrap>
           <a <s:if test="disabled == 'false'">onMouseOver="this.className='mmMON'" onMouseOut="this.className='mmMOFF'"
              class="topmenulinkon" HREF="<s:property value='#exp.link' />"</s:if>
              <s:else>class="topmenulinkon disabled"</s:else>>
            <s:property value="label"/>
           </a>
          </td>
         </s:if>
         <s:else>
          <td class="menu1" nowrap>      
           <a <s:if test="disabled == 'false'">onMouseOver="this.className='MON'" onMouseOut="this.className='MOFF'"
              class="topmenulink" HREF="<s:property value='#exp.link' />"</s:if>
              <s:else>class="topmenulink disabled"</s:else>>
            <s:property value="#exp.label" />
           </a>
          </td>
         </s:else>  
        </s:if>
       </s:iterator>
      </s:if>
      <td width="100%" class="menu1last">&nbsp;</td>
<!-- login -->
      <td nowrap class="menu1blank">

         <table width="100%" cellpadding="0" cellspacing="0">
          <tr>
           <td align="right" nowrap>
             <a class="midmenulink" onMouseOver="this.className='mmMON'" onMouseOut="this.className='mmMOFF'" 
                 HREF="page?id=help">Help</a>
            | <a class="midmenulink" onMouseOver="this.className='mmMON'" onMouseOut="this.className='mmMOFF'" 
                 HREF="feedback">Contact Us</a>
           </td>
          </tr>
         </table>

<%--
       <table width="100%" cellpadding="0" cellspacing="0">
        <tr>
         <td align="right" class="acccell">
--%>
          <%-- <s:if test="userOn"> --%>
<%--
           <s:if test="#session['USER_ROLE'].administrator != null" >
              (<a href="edit?ret=menu&mst=<s:property value='mst'/>">MENU EDIT</a>) |
           </s:if>
           <s:if test="#session['USER_ID'] > 0">
             User:  |
              <a href='user?op=edit'>
                <s:property value="#session['LOGIN']" />
              </a>
               |
               <a href="user?op=logout" class="acmain">Log out</a>
           </s:if>
           <s:else>
              User: | <a onMouseOver="this.className='acMON'" onMouseOut="this.className='acMOFF'" 
                        href="user?op=regf" class="acMOFF">Sign up</a>
                   | <a onMouseOver="this.className='acMON';" onMouseOut="this.className='acMOFF';" 
                        href="user" class="acMOFF">Log in</a>
           </s:else>
--%>
          <%-- </s:if> --%>
<%--
         </td>
        </tr>
       </table>
--%>
<!-- login: ends -->      
      </td>
<%--      <td class="menu1blank">&nbsp;</td> --%>
     </tr>
    </table>
   </td>
  </tr>
<!-- top menu: END -->
<!-- mid menu -->
  <tr>
   <td class="midmenu">
    <table class="menu2_background" width="100%" cellspacing="0" cellpadding="0">
     <s:if test='menuContext.jsonConfig.menu[0].menu[menuSel[0]-1].menu.size > 0'>
       <tr>
        <td class="menu2first">&nbsp;</td>
         <s:iterator value="menuContext.jsonConfig.menu[0].menu[menuSel[0]-1].menu" id="exp" status="mpos" >
          <s:set name="itemon" value="#exp.roles.{? #this in #session['USER_ROLE'].keySet()}.size > 0"/>
          <s:if test="#exp.roles==null || #exp.roles.size==0 || #itemon ">
           <s:if test='#mpos.count == menuSel[1]'>
            <td class="menu2select" nowrap>
             <a <s:if test="disabled == 'false'">onMouseOver="this.className='bmMON'" onMouseOut="this.className='bmMOFF'"
                      class="midmenulinkon" HREF="<s:property value='link' />"</s:if>
                <s:else>class="midmenulinkon disabled"</s:else>>
              <s:property value='label' />          
             </a>
            </td>
           </s:if>
           <s:else> 
            <td class="menu2" nowrap>
             <a <s:if test="disabled == 'false'">onMouseOver="this.className='bmMON'" onMouseOut="this.className='bmMOFF'"
                 class="midmenulinkon" HREF="<s:property value='link' />"</s:if>
                <s:else>class="midmenulinkon disabled"</s:else>>
              <s:property value='label' />
             </a>
            </td>
           </s:else>
          </s:if>
         </s:iterator>
        </s:if> 
        <td width="100%" class="menu2last">&nbsp;</td>

        <td class="menu2blank">
<%--
         <table width="100%" cellpadding="0" cellspacing="0">
          <tr>
           <td align="right" nowrap>
             <a class="midmenulink" onMouseOver="this.className='mmMON'" onMouseOut="this.className='mmMOFF'" 
                 HREF="page?id=help">Help</a>
            | <a class="midmenulink" onMouseOver="this.className='mmMON'" onMouseOut="this.className='mmMOFF'" 
                 HREF="feedback">Contact Us</a>
           </td>
          </tr>
         </table>
--%>
        </td>
        <td class="menu2blank">&nbsp; </td>
       </tr>
      </table>

     </td>

    </tr>

<!-- mid menu: END -->   

<!-- bottom menu -->   
    <tr>
     <td class="btmmenu" colspan="2">
      <table width="100%" cellspacing="0" cellpadding="0">
       <tr>
        <td width="100%">
         <table class="menu3_background" width="100%" cellspacing="0" cellpadding="0">
          <tr>
           <s:if test='menuContext.jsonConfig.menu[0].menu[menuSel[0]-1].menu[menuSel[1]-1].menu.size > 0'>
            <td class="menu3first">&nbsp;</td>
            <s:iterator value="menuContext.jsonConfig.menu[0].menu[menuSel[0]-1].menu[menuSel[1]-1].menu" id="exp" status="mpos" >
             <s:set name="itemon" value="#exp.roles.{? #this in #session['USER_ROLE'].keySet()}.size > 0"/>
             <s:if test="#exp.roles==null || #exp.roles.size==0 || #itemon ">
              <s:if test='#mpos.count == menuSel[2]'>
               <td class="menu3select" nowrap>
                <a <s:if test="disabled == 'false'">onMouseOver="this.className='bmMON'" onMouseOut="this.className='bmMOFF'"
                    class="btmmenulink" HREF="<s:property value='link' />" </s:if>
                   <s:else>class="btmmenulink disabled"</s:else>>
                 <s:property value='label' />
                </a>
               </td>
              </s:if>
              <s:else>
               <td class="menu3" nowrap>
                <a <s:if test="disabled == 'false'">onMouseOver="this.className='bmMON'" onMouseOut="this.className='bmMOFF'"
                    class="btmmenulink" HREF="<s:property value='link' />" </s:if>
                   <s:else>class="btmmenulink disabled"</s:else>>
                 <s:property value='label' />
                </a>
               </td>
              </s:else>
             </s:if>
            </s:iterator>
            <td width="100%" class="menu3last">&nbsp;</td>
           </s:if>
           <s:else>
            <td width="100%" class="menu3null">&nbsp;</td>
           </s:else>
          </tr>
         </table>
        </td>
        <td class="btmsearch">
         <table cellspacing="0" cellpadding="0">
          <tr>
           <td align="left" class="btmsearchtab" nowrap>
<%--
            <s:if test="searchOn"><s:form action="pubsrc" theme="simple">
             PMID:<s:textfield theme="simple" name="pub.pmid" size="16" size="16" value="" maxlength="128"/>
             <s:submit type="input" tabindex="3" name="op.esrc" value="SEARCH" theme="simple"/>
            </s:form>
         
           </s:if>
--%>  
           </td>
           <td class="menu3blank">&nbsp;
           </td>
          </tr>
         </table>
        </td>
       </tr>
<!-- bottom menu: END -->
     </table>
     </td>
    </tr>
   </table>
   <table cellpadding="0" cellspacing="0" width="100%">
   <tbody>
    <tr>
     <td>&nbsp;</td>
    </tr>
   </tbody>
   </table>


