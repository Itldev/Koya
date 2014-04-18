
// extract folder listing arguments from URI
//var verbose = (args.verbose == "true" ? true : false);
var folderpath = url.templateArgs.folderpath;

// search for folder within Alfresco content repository
//var folder = roothome.childByNamePath();
//var children = companyhome.childByNamePath("app:dictionary").getChildren();
var folder = search.xpathSearch(folderpath);
//var folder = roothome.getChildren()

//// validate that folder has been found
if (folder == undefined || !folder.length == 1) {
   status.code = 404;
   status.message = "Folder " + folderpath + " not found.";
   status.redirect = true;
}

model.children = folder[0].getChildren();