<#--

    Koya is an alfresco module that provides a corporate orientated dataroom.

    Copyright (C) Itl Developpement 2014

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see `<http://www.gnu.org/licenses/>`.

-->

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