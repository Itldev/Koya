var filename = null;
var content = null;
var title = "";
var parentnoderef = "";

// locate file attributes
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
        ;
        content = field.content;
    }
}

// ensure mandatory file attributes have been located
if (filename == undefined || content == undefined)
{
    status.code = 400;
    status.message = "Uploaded file cannot be located in request";
    status.redirect = true;
}
else
{
    try {
        var upParent = search.findNode(parentnoderef);

        // create document in company home for uploaded file
        upload = upParent.createFile(filename);

        upload.properties.content.write(content);
        upload.properties.content.setEncoding("UTF-8");
        upload.properties.content.guessMimetype(filename);

        upload.properties.title = title;
        upload.save();

        // setup model for response template
        model.upload = upload;

    } catch (e) {

        status.code = 400;
        status.message = "Uploaded error : " + e.toString();
        status.redirect = true;

    }


}