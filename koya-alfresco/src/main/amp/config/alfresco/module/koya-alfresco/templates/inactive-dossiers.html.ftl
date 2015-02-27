<html>
    <head>
		<meta content="text/html; charset=utf-8" http-equiv="Content-Type">
    </head>
    <body bgcolor="#dddddd" style="font-family: Arial, sans-serif;font-size: 14px;">
        <table width="100%" cellpadding="0" cellspacing="0" border="0">
<#list inactiveDossiers as inactiveDossier>
			<tr>
				<td>
				    <a href="${inactiveDossier.url}">${inactiveDossier.nodeRef.properties.title}</a> est inactif depuis ${inactiveDossier.nodeRef.properties['koya:lastModificationDate']?datetime}
				</td>
			</tr>
</#list>    
        </table>
    </body>
</html>
