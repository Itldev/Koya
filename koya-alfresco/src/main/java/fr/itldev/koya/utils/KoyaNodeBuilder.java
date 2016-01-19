package fr.itldev.koya.utils;

import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.QName;

import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.ModelService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.exceptions.KoyaErrorCodes;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Directory;
import fr.itldev.koya.model.impl.Document;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;

/**
 *
 *
 */
public class KoyaNodeBuilder {

	private final NodeService nodeService;
	private final KoyaNodeService koyaNodeService;
	private final WorkflowService workflowService;
	private final ModelService modelService;
	
	public KoyaNodeBuilder(NodeService nodeService,
			KoyaNodeService koyaNodeService,WorkflowService workflowService, ModelService modelService) {
		this.nodeService = nodeService;
		this.koyaNodeService = koyaNodeService;
		this.workflowService = workflowService;
		this.modelService = modelService;
	}

	public KoyaNode build(NodeRef nodeRef) throws KoyaServiceException {

		QName type = nodeService.getType(nodeRef);
		KoyaNode si;
		if (type.equals(KoyaModel.TYPE_COMPANY)) {
			si = companyBuilder(nodeRef);
		} else if (type.equals(KoyaModel.TYPE_SPACE)) {
			si = nodeSpaceBuilder(nodeRef);
		} else if (type.equals(KoyaModel.TYPE_DOSSIER)) {
			si = nodeDossierBuilder(nodeRef);
		} else if (type.equals(ContentModel.TYPE_FOLDER)
				&& (koyaNodeService
						.getFirstParentOfType(nodeRef, Dossier.class) != null)) {
			si = nodeDirBuilder(nodeRef);
		} else if ((type.equals(ContentModel.TYPE_CONTENT) || type
				.equals(ContentModel.TYPE_THUMBNAIL))) {
			si = nodeDocumentBuilder(nodeRef);
		} else {
			throw new KoyaServiceException(
					KoyaErrorCodes.INVALID_KOYANODE_NODEREF);
		}
		return si;
	}

	private Company companyBuilder(NodeRef n) throws KoyaServiceException {
		Company c = Company.newInstance();
		c.setName((String) nodeService.getProperty(n, ContentModel.PROP_NAME));
		c.setTitle((String) nodeService.getProperty(n, ContentModel.PROP_TITLE));
		c.setFtpUsername(modelService.getCompanyImporterUsername(c.getName()));
		c.setNodeRef(n);
		return c;
	}

	private Space nodeSpaceBuilder(final NodeRef spaceNodeRef)
			throws KoyaServiceException {
		Space s = Space.newInstance();
		s.setNodeRef(spaceNodeRef);
		s.setName((String) nodeService.getProperty(spaceNodeRef,
				ContentModel.PROP_NAME));
		s.setTitle((String) nodeService.getProperty(spaceNodeRef,
				ContentModel.PROP_TITLE));
		return s;
	}
	
	private Dossier nodeDossierBuilder(final NodeRef dossierNodeRef) {
		Dossier d = Dossier.newInstance();
		d.setNodeRef(dossierNodeRef);
		d.setName((String) nodeService.getProperty(dossierNodeRef,
				ContentModel.PROP_NAME));
		if (nodeService.getProperty(dossierNodeRef,
				KoyaModel.PROP_LASTMODIFICATIONDATE) != null) {
			d.setLastModifiedDate((Date) nodeService.getProperty(
					dossierNodeRef, KoyaModel.PROP_LASTMODIFICATIONDATE));
		} else {
			d.setLastModifiedDate((Date) nodeService.getProperty(
					dossierNodeRef, ContentModel.PROP_MODIFIED));
		}

		if (nodeService.getProperty(dossierNodeRef, KoyaModel.PROP_ACTIVITIIDS) != null) {

			
			@SuppressWarnings("unchecked")
			List<String> ids = (List<String>) nodeService.getProperty(
					dossierNodeRef, KoyaModel.PROP_ACTIVITIIDS);
			for (String activitiId : ids) {
				
				String status=null;
				try{
					WorkflowInstance wf = workflowService.getWorkflowById(activitiId);
					status = (String) nodeService.getProperty(wf.getWorkflowPackage(), KoyaModel.PROP_BPMCURRENTSTATUS);
					if(status == null ){
						status = KoyaModel.BpmStatusValues.UNKNOWN;
					}
				}catch(Exception e){
					status = KoyaModel.BpmStatusValues.UNKNOWN;
				}
				d.getWorkflows().put(activitiId,status);
			}

		}

		d.setTitle((String) nodeService.getProperty(dossierNodeRef,
				ContentModel.PROP_TITLE));
				return d;
	}

	private Directory nodeDirBuilder(NodeRef dirNodeRef) {
		Directory dir = Directory.newInstance();
		dir.setNodeRef(dirNodeRef);
		dir.setName((String) nodeService.getProperty(dirNodeRef,
				ContentModel.PROP_NAME));
		dir.setTitle((String) nodeService.getProperty(dirNodeRef,
				ContentModel.PROP_TITLE));
		return dir;
	}

	private Document nodeDocumentBuilder(final NodeRef docNodeRef) {
		Document doc = Document.newInsance();
		doc.setNodeRef(docNodeRef);
		doc.setName((String) nodeService.getProperty(docNodeRef,
				ContentModel.PROP_NAME));
		doc.setTitle((String) nodeService.getProperty(docNodeRef,
				ContentModel.PROP_TITLE));
		doc.setByteSize(AuthenticationUtil
				.runAsSystem(new AuthenticationUtil.RunAsWork<Long>() {
					@Override
					public Long doWork() throws Exception {
						return koyaNodeService.getByteSize(docNodeRef);
					}
				}));

		ContentData contentData = (ContentData) nodeService.getProperty(
				docNodeRef, ContentModel.PROP_CONTENT);
		doc.setMimeType(contentData.getMimetype());
		return doc;
	}

}
