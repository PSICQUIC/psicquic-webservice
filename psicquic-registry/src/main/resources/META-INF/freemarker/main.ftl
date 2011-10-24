
<html>
<head>
  <title>PSICQUIC Registry</title>

    <style type="text/css">
        body {
            font-family: Verdana, Arial, Helvetica, sans-serif;
            font-size: 9pt;
        }

        .registry th {
            text-align: left;
            border-bottom: black solid 1px;
            font-size: 9pt;
        }

        .registry td {
            font-size: 9pt;
            border-bottom: gray dashed 1px;
        }

        input.registry {
            font-size: 9pt;
        }

        table.registry {
            border-bottom: black solid 1px;
        }

        .active {
            background-color: #edf5ea;
        }
        .inactive {
            background-color: #f8cfcf;'
        }
    </style>
</head>
<body style="background-image: url(http://www.ebi.ac.uk:80/Tools/webservices/psicquic/view/images/top_background.jpg); background-repeat: repeat-x;">

<h2>PSICQUIC Registry</h2>

  <table cellpadding="4" cellspacing="0" class="registry">
      <tr>
          <th>Name</th>
          <th>Active</th>
          <th>Interactions</th>
          <th>Version</th>
          <th><nobr>SOAP URL</nobr></th>
          <th><nobr>REST URL</nobr></th>
          <th><nobr>REST Example</nobr></th>
          <th>Restricted</th>
          <th>Tags</th>
          <th>Comments</th>
      </tr>
    <#list registry.services as service>
    <tr class="${service.active?string("active", "inactive")}" style="vertical-align:top">
        <td><a href="${service.organizationUrl}" target="_blank">${service.name}</a></td>
        <td>${service.active?string("YES", "NO")}</td>
        <td style="text-align:right">${service.count}</td>
        <td><nobr>${service.version!'-'}</nobr></td>
        <td><input type="text" value="${service.soapUrl}" readonly="true"/></td>
        <td>
            <#if service.restExample??>
                <input type="text" value="${service.restUrl}" readonly="true"/>
                <#else>
                N/A
            </#if>
        </td>
        <td>
            <#if service.restExample??>
                <a href="${service.restExample}" target="_blank">Example</a>
                <#else>
                NO
            </#if>
        </td>
        <td>${service.restricted?string("YES", "NO")}</td>
        <td>

                <#list service.tags as tag>
                    <#if tag?starts_with('MI:')>
                        <NOBR> <a href="http://www.ebi.ac.uk/ontology-lookup/?termId=${tag}" target="_blank">${termName(tag)}</a> </NOBR>
                    <#else>
                        <NOBR> ${termName(tag)} </NOBR>
                    </#if>
                    <#if tag_has_next> <br/> </#if>
                </#list>&#160;

        </td>
        <td>${service.comments!''}&#160;</td>
  </#list>
  </table>

   <p>Total: <strong>${totalCount}</strong>&#160;Interactions from <strong>${serviceCount}</strong> PSICQUIC Services.</p>

  <h2>How to use the Registry</h2>

  <p>Check the documentation <a href="http://code.google.com/p/psicquic/wiki/Registry">here</a>
  on how to use this registry.</p>

  <p>Want to add your PSICQUIC service here? Check <a href="http://code.google.com/p/psicquic/wiki/HowToInstall">this</a>.</p>

</body>
</html>