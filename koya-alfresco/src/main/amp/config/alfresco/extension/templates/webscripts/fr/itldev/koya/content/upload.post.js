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
                filename = field.filename;
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
        model.size = upload.properties.content.size;

    } catch (e) {
        model.error = "Uploaded error : " + e.toString();
        return;
    }



}


var filename = null;
var content = null;
var title = "";
var parentnoderef = "";

//model

model.filename = "";
model.size = 0;
model.error = "";

main();

