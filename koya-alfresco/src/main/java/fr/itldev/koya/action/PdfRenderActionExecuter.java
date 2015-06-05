/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.itldev.koya.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.rendition.executer.AbstractRenderingEngine;
import org.alfresco.repo.rendition.executer.ReformatRenderingEngine;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 *
 * @author nico
 */
public class PdfRenderActionExecuter extends ActionExecuterAbstractBase {

    public static final String NAME = "pdfRender";

    private RenditionService renditionService;
    private FileFolderService fileFolderService;
    private NodeService nodeService;

    public static final String RESULT_PARAM_NODEREF = "nodeRef";
    public static final String RESULT_PARAM_SUCCESS = "sucess";
    public static final String RESULT_PARAM_NAME = "name";
    public static final String RESULT_PARAM_TITLE = "title";
    public static QName QNAME_RENDTION = QName.createQName(
            NamespaceService.CONTENT_MODEL_1_0_URI, "pdfrendition");

    private RenditionDefinition renditionDef;

    public RenditionService getRenditionService() {
        return renditionService;
    }

    public void setRenditionService(RenditionService renditionService) {
        this.renditionService = renditionService;
    }

    public FileFolderService getFileFolderService() {
        return fileFolderService;
    }

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
        Map<String, Serializable> result = new HashMap<>();

        FileInfo fi = fileFolderService.getFileInfo(actionedUponNodeRef);

        result.put(RESULT_PARAM_NODEREF, fi.getNodeRef().toString());
        result.put(RESULT_PARAM_NAME, fi.getName());
        result.put(RESULT_PARAM_TITLE, nodeService.getProperty(actionedUponNodeRef,
                ContentModel.PROP_TITLE));
        result.put(RESULT_PARAM_SUCCESS, Boolean.FALSE);

        if (!fi.isFolder()) {
            // Check for an already existing PDF rendition
            ChildAssociationRef car = renditionService.getRenditionByName(
                    actionedUponNodeRef, renditionDef.getRenditionName());
            NodeRef pdfRendition = null;
            if (car != null) {
                pdfRendition = car.getChildRef();

                // Check if rendition is up to date
                Date docModified = (Date) nodeService.getProperty(
                        actionedUponNodeRef, ContentModel.PROP_MODIFIED);
                Date rentionCreated = (Date) nodeService.getProperty(
                        pdfRendition, ContentModel.PROP_CREATED);

                if (docModified.after(rentionCreated)) {
                    car = null;
                }
            }

            if (car == null) {
                car = renditionService.render(actionedUponNodeRef,
                        renditionDef);

                pdfRendition = car.getChildRef();

                // Add the aspect to hide the rendition from searches
                nodeService.setType(pdfRendition, ContentModel.TYPE_THUMBNAIL);
                nodeService.addAspect(pdfRendition,
                        RenditionModel.ASPECT_HIDDEN_RENDITION, null);
            }

            String name = (String) nodeService.getProperty(actionedUponNodeRef,
                    ContentModel.PROP_NAME);
            name = name.replaceFirst("(. *)\\.[^.]+$", "$1.pdf");
            String title = (String) nodeService.getProperty(actionedUponNodeRef,
                    ContentModel.PROP_TITLE);
            title = title.replaceFirst("(. *)\\.[^.]+$", "$1.pdf");

            result.put(RESULT_PARAM_NODEREF, pdfRendition.toString());
            result.put(RESULT_PARAM_NAME, name);
            result.put(RESULT_PARAM_TITLE, title);
            result.put(RESULT_PARAM_SUCCESS, Boolean.TRUE);

        }

        action.setParameterValue(PARAM_RESULT, (Serializable) result);
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
    }

    public void init() {
        // Names must be provided for the rendition definition and the rendering
        // engine to use.
        String renderingEngineName = ReformatRenderingEngine.NAME;

        // Create the Rendition Definition object.
        renditionDef = renditionService
                .createRenditionDefinition(QNAME_RENDTION, renderingEngineName);

        // Set parameters on the rendition definition.
        renditionDef.setParameterValue(AbstractRenderingEngine.PARAM_MIME_TYPE,
                MimetypeMap.MIMETYPE_PDF);

    }

}
