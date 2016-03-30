package fr.itldev.koya.webscript.dossier;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.dictionary.RepositoryLocation;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.itldev.koya.alfservice.CompanyPropertiesService;
import fr.itldev.koya.alfservice.CompanyService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.exceptions.KoyaErrorCodes;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.webscript.KoyaWebscript;

/**
 * This Service generates a PDF showing Dossier's repositories tree.
 * 
 * This document is placed on dossier root
 * 
 */
public class GenerateSummary extends AbstractWebScript implements InitializingBean {

	private final static String TPL_SUMMARY = "//app:company_home/app:dictionary/cm:koya/cm:templates/cm:dossier-summary.html.ftl";

	private KoyaNodeService koyaNodeService;
	private TemplateService templateService;
	private NamespaceService namespaceService;
	private FileFolderService fileFolderService;
	private SearchService searchService;
	private NodeService nodeService;
	protected ServiceRegistry serviceRegistry;
	private ContentService contentService;
	private VersionService versionService;
	private CompanyService companyService;
	private CompanyPropertiesService companyPropertiesService;
	private SysAdminParams sysAdminParams;

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	public void setTemplateService(TemplateService templateService) {
		this.templateService = templateService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setVersionService(VersionService versionService) {
		this.versionService = versionService;
	}

	public void setCompanyService(CompanyService companyService) {
		this.companyService = companyService;
	}

	public void setCompanyPropertiesService(CompanyPropertiesService companyPropertiesService) {
		this.companyPropertiesService = companyPropertiesService;
	}

	public void setSysAdminParams(SysAdminParams sysAdminParams) {
		this.sysAdminParams = sysAdminParams;
	}

	protected RepositoryLocation templateLocation;

	@Override
	public void afterPropertiesSet() throws Exception {
		templateLocation = new RepositoryLocation(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
				TPL_SUMMARY, "xpath");
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		Map<String, String> urlParams = KoyaWebscript.getUrlParamsMap(req);

		NodeRef htmlSummaryNodeRef;
		NodeRef pdfSummaryNodeRef;
		String response;

		try {

			/**
			 * =================================================
			 * 
			 * Find Dossier
			 */
			Dossier d = koyaNodeService.getKoyaNode(
					koyaNodeService.getNodeRef(
							"workspace://SpacesStore/" + (String) urlParams.get("nodeId")),
					Dossier.class);

			Company c = (Company) koyaNodeService.getFirstParentOfType(d.getNodeRef(),
					Company.class);
			/**
			 * =================================================
			 * 
			 * Get Localized Template
			 * 
			 * TODO try to read template from company settings if exists
			 * 
			 */
			// TODO get dynamicly found template
			NodeRef template = getStaticTemplate();

			if (template == null) {
				throw new KoyaServiceException(KoyaErrorCodes.SUMMARY_TEMPLATE_NOT_FOUND);
			}

			/**
			 * =================================================
			 * 
			 * Generate summary html from template
			 */
			String htmlText = null;
			Map<String, Serializable> paramsTemplate = new HashMap<>();
			paramsTemplate.put("dossier", new TemplateNode(d.getNodeRef(), serviceRegistry, null));

			// logo parameters
			paramsTemplate.put("dossier", new TemplateNode(d.getNodeRef(), serviceRegistry, null));

			final String logoUrl = sysAdminParams.getAlfrescoProtocol() + "://"
					+ sysAdminParams.getAlfrescoHost() + ":" + sysAdminParams.getAlfrescoPort()
					+ "/" + sysAdminParams.getAlfrescoContext() + "/s/fr/itldev/koya/company/logo/"
					+ c.getName();

			NodeRef logo = companyPropertiesService.getLogo(c);
			ContentReader contentReader = contentService.getReader(logo, ContentModel.PROP_CONTENT);

			// scale image
			final BufferedImage bimg = ImageIO.read(contentReader.getContentInputStream());
			final int heigth = 80;
			final int width = (heigth * bimg.getWidth()) / bimg.getHeight();

			HashMap<String, String> logoSettings = new HashMap<>();
			logoSettings.put("url", logoUrl);
			logoSettings.put("height", Integer.valueOf(heigth).toString());
			logoSettings.put("width", Integer.valueOf(width).toString());
			logoSettings.put("companyName", c.getName());
			paramsTemplate.put("logo", logoSettings);

			try {
				htmlText = templateService.processTemplate("freemarker", template.toString(),
						paramsTemplate);
			} catch (Exception templateEx) {
				throw new KoyaServiceException(KoyaErrorCodes.SUMMARY_TEMPLATE_PROCESS_ERROR);
			}

			NodeRef companyTmpSummaryDir = companyService.getTmpSummaryDir(c);
			String summaryFileName = getDossierSummaryFileName(d);
			/**
			 * =================================================
			 * 
			 * Write html file
			 */

			htmlSummaryNodeRef = nodeService.getChildByName(companyTmpSummaryDir,
					ContentModel.ASSOC_CONTAINS, summaryFileName + ".html");

			if (htmlSummaryNodeRef == null) {
				htmlSummaryNodeRef = fileFolderService.create(companyTmpSummaryDir,
						summaryFileName + ".html", ContentModel.TYPE_CONTENT).getNodeRef();
			}

			ContentWriter fileWriter = fileFolderService.getWriter(htmlSummaryNodeRef);
			fileWriter.setEncoding("UTF-8");
			fileWriter.putContent(htmlText);
			// creates new revision
			versionService.createVersion(htmlSummaryNodeRef, null);

			/**
			 * =================================================
			 * 
			 * Convert html to pdf
			 */

			pdfSummaryNodeRef = nodeService.getChildByName(companyTmpSummaryDir,
					ContentModel.ASSOC_CONTAINS, summaryFileName + ".pdf");

			if (pdfSummaryNodeRef == null) {
				pdfSummaryNodeRef = fileFolderService.create(companyTmpSummaryDir,
						summaryFileName + ".pdf", ContentModel.TYPE_CONTENT).getNodeRef();
			}

			ContentReader htmlSummaryReader = fileFolderService.getReader(htmlSummaryNodeRef);
			htmlSummaryReader.setMimetype("text/html");

			ContentWriter pdfSummaryWriter = fileFolderService.getWriter(pdfSummaryNodeRef);
			pdfSummaryWriter.setEncoding("UTF-8");
			pdfSummaryWriter.setMimetype("application/pdf");

			ContentTransformer transformer = contentService.getTransformer("text/html",
					"application/pdf");

			transformer.transform(htmlSummaryReader, pdfSummaryWriter);
			// creates new revision
			versionService.createVersion(pdfSummaryNodeRef, null);
			
			response = KoyaWebscript.getObjectAsJson(koyaNodeService.getKoyaNode(pdfSummaryNodeRef));

		} catch (KoyaServiceException ex) {
			throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
		}

		res.setContentEncoding("UTF-8");
		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write(response);
	}
	
	
	

	private String getDossierSummaryFileName(Dossier d) {
		return "dossier-summary-" + d.getNodeRef().getId();
	}

	private static final String SUMMARY_NODEREF = "workspace://SpacesStore/5e43d1bd-b2cd-4ffd-ae81-84f0aa9a2154";
	private NodeRef summarytemplate;

	private NodeRef getStaticTemplate() {
		/**
		 * Loads template with static reference as it's defined in koya
		 * bootstrap
		 * 
		 * Fix xpath search very long
		 * 
		 * TODO prefer dynamic loading with short template search time
		 */
		return new NodeRef(SUMMARY_NODEREF);
	}

	@SuppressWarnings("unused")
	private NodeRef getTemplate() {

		if (summarytemplate != null) {
			return summarytemplate;
		}

		List<NodeRef> nodeRefs = searchService.selectNodes(
				nodeService.getRootNode(templateLocation.getStoreRef()), templateLocation.getPath(),
				null, namespaceService, false);

		if (nodeRefs.isEmpty()) {
			throw new KoyaServiceException(KoyaErrorCodes.SUMMARY_TEMPLATE_NOT_FOUND);
		}
		summarytemplate = fileFolderService.getLocalizedSibling(nodeRefs.get(0));
		return summarytemplate;
	}

}
