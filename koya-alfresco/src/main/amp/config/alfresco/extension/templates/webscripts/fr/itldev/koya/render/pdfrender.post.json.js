//var fileNode, result, pdfFile;
var nodeRef = json.get("nodeRef");

fileNode = search.findNode(nodeRef);

pdfRender = actions.create("pdfRender");
pdfRender.execute(fileNode);

model.result = pdfRender.parameters.result;