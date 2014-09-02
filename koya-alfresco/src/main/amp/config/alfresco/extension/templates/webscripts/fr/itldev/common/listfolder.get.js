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