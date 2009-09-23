
<html>
<head>
  <title>PSICQUIC Web Services</title>

    <style type="text/css">
        body {
            font-family: Verdana, Arial, Helvetica, sans-serif;
        }

        th {
            text-align: left;
            border-bottom: black solid 1px;
        }

        table {
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
<body>

  <h1>PSICQUIC Web Services</h1>

  <table cellpadding="4" cellspacing="0">
      <tr>
          <th>Name</th>
          <th>Active</th>
          <th>Interactions</th>
          <th>Version</th>
          <th><nobr>SOAP URL</nobr></th>
          <th><nobr>REST Access</nobr></th>
          <th>Restricted</th>
          <th>Comments</th>
      </tr>
    <#list services as service>
    <tr class="${service.active?string("active", "inactive")}" style="vertical-align:top">
        <td><a href="${service.organizationUrl}" target="_blank">${service.name}</a></td>
        <td>${service.active?string("YES", "NO")}</td>
        <td>${service.count}</td>
        <td><nobr>${service.version!'-'}</nobr></td>
        <td><nobr>${service.soapUrl}</nobr></td>
        <td>
            <#if service.restUrl??>
                <a href="${service.restUrl}" target="_blank">Example</a>
                <#else>
                NO
            </#if>
        </td>
        <td>${service.restricted?string("YES", "NO")}</td>
        <td>${service.comments!''}</td>
  </#list>
  </table>

  <h2>Using the Registry</h2>

  Check the documentation <a href="http://code.google.com/p/psicquic/wiki/Registry">here</a>
  on how to use this registry.

</body>
</html>