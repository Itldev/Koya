<html>
 <head> 
   <title>Upload Web Script Koya</title> 
   <link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">
 </head>
 <body>   
   <table>
     <form action="${url.service}" method="post" enctype="multipart/form-data" accept-charset="utf-8">
       <tr><td>File:</td><td><input type="file" name="file"></td></tr>
       <tr><td>Parent NodeRef:</td><td><input name="parentnoderef"></td></tr>       
       <tr><td></td></tr>
       <tr><td><input type="submit" name="submit" value="Upload"></td></tr>
     </form>
   </table>
 </body>
</html>