// --
//  Koya is an alfresco module that provides a corporate orientated dataroom.
//
//  Copyright (C) Itl Developpement 2014
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Affero General Public License as
//  published by the Free Software Foundation, either version 3 of the
//  License, or (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Affero General Public License for more details.
//
//  You should have received a copy of the GNU Affero General Public License
//  along with this program.  If not, see `<http://www.gnu.org/licenses/>`.
// --

function main() {

    try {
        for each (field in formdata.fields)
        {
            if (field.name == "parentnoderef")
            {
                parentnoderef = field.value;
            }

            else if (field.name == "file" && field.isFile)
            {
                originalFilename = field.filename;
                filename = originalFilename.trim().replaceAll("\\\"+|\\\*+|\\\\+|>+|<+|\\\?+|\\\/+|:+|\\\|+", "").replaceAll("\\\.+$", "");
                rename = filename !== field.filename;
                title = field.filename;
                content = field.content;
            }
        }
    } catch (e) {
        model.error = "form field error : " + e.toString();
        return;
    }


    // ensure mandatory file attributes have been located
    if (filename == undefined || content == undefined)
    {

        model.error = "Uploaded file cannot be located in request ";
        return;
    }



    try {
        var upParent = search.findNode(parentnoderef);


        if (upParent == null) {
            model.error = "Invalid Parent Node";
            return;
        }


        // create document in company home for uploaded file
        upload = upParent.createFile(filename);

        upload.properties.content.write(content);
        upload.properties.content.setEncoding("UTF-8");
        upload.properties.content.guessMimetype(filename);

        upload.properties.title = title;
        upload.save();

        
        model.filename = upload.name;
        model.originalFilename = originalFilename;
        model.rename = rename;
        model.size = upload.properties.content.size;
        
    } catch (e) {
        model.error = "Uploaded error : " + e.toString();
        return;
    }



}


var filename = null;
var originalFilename=null;
var rename = null;
var content = null;
var title = "";
var parentnoderef = "";

//model

model.filename = "";
model.originalFilename="";
model.rename = false;
model.size = 0;
model.error = "";

main();

