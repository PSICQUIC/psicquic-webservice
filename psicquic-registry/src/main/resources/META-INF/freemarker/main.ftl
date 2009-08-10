
<html>
<head>
  <title>PSICQUIC Web Services</title>
</head>
<body>

  <h1>PSICQUIC Web Services</h1>

  <table>
      <tr>
          <th>Name</th>
          <th>Active</th>
          <th># Interactions</th>
          <th>Version</th>
          <th>SOAP URL</th>
      </tr>
    <#list services as service>
    <tr>
        <td><a href="${service.organizationUrl}" target="_blank">${service.name}</a></td>
        <td>${service.active?string("YES", "NO")}</td>
        <td>${service.count}</td>
        <td>${service.version!'-'}</td>
        <td>${service.url}</td>
  </#list>
  </table>
</body>
</html>