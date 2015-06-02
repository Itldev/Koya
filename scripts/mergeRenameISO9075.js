
for each(var child in space.children) {
    if (child.name.indexOf('_x') != -1) {
        mergeRename(child, space);

    }

}

function mergeRename(source, target) {
    var decodedName = search.ISO9075Decode(source.name);
    var decodedNode = target.childByNamePath(decodedName);
    var merging = true;
    if (decodedNode === null) {
        merging = false;
        if (String(target.nodeRef) !== String(source.parent.nodeRef)) {
            logger.log("moving " + source.name + " to " + target.name);
            source.move(target);
        }

        logger.log("decoding name " + source.name + " into " + decodedName);

        source.name = decodedName;
        source.save();



        decodedNode = source;
    }

    if (source.isContainer) {
        for each(var child in source.children) {
            if (child.name.indexOf('_x') != -1) {
                mergeRename(child, decodedNode);
            } else if (!child.parent.nodeRef.equals(decodedNode.nodeRef)) {
                var filename = child.name;
                var filenameBase = filename;
                var ext = '';
                if (filename.lastIndexOf(".") != -1) {
                    filenameBase = filename.substring(0, filename.lastIndexOf("."));
                    ext = filename.substring(filename.lastIndexOf("."));
                }
                var i = 1;
                if (decodedNode.childByNamePath(filename)) {

                    while (decodedNode.childByNamePath(filename) !== null) {
                        filename = filenameBase + "-" + i + ext;
                        i++;
                    }
                    logger.log("Duplicate : renaming " + child.name + " into " + filename);
                    child.name = filename;
                    child.save();
                }
                logger.log("moving " + child.name + " to " + decodedNode.name);
                child.move(decodedNode);
            }
        }
    }

    if (merging) {
        source.remove();
    }
}