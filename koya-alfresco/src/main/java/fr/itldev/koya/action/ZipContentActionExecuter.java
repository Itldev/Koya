package fr.itldev.koya.action;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Deflater;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.WebScriptException;

import fr.itldev.koya.alfservice.KoyaActivityPoster;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.model.KoyaModel;

public class ZipContentActionExecuter extends ActionExecuterAbstractBase {

	Logger logger = Logger.getLogger(ZipContentActionExecuter.class);

	public static final String NAME = "koyaZip";

	public static String PARAM_RESULT = "result";

	public static String PARAM_ZIPNAME = "zipName";
	public static String PARAM_COMPANYTMPZIPDIR = "companyTmpZipDir";
	public static String PARAM_NODEREFS = "nodeRefs";
	public static String PARAM_PDF = "pdf";
	public static String PARAM_ASYNC = "async";

	private NodeService nodeService;
	private KoyaNodeService koyaNodeService;
	private ContentService contentService;
	private ActionService actionService;
	private TransactionService transactionService;
	private KoyaActivityPoster activityPoster;
	private DictionaryService dictionaryService;
	private NamespaceService namespaceService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}

	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	public void setActivityPoster(KoyaActivityPoster activityPoster) {
		this.activityPoster = activityPoster;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {

	}

	@Override
	public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef) {

		String zipName = (String) ruleAction.getParameterValue(PARAM_ZIPNAME);
		NodeRef companyTmpZipDir = (NodeRef) ruleAction.getParameterValue(PARAM_COMPANYTMPZIPDIR);
		Boolean pdf = (Boolean) ruleAction.getParameterValue(PARAM_PDF);
		Boolean async = (Boolean) ruleAction.getParameterValue(PARAM_ASYNC);
		List<NodeRef> nodeRefs = (List<NodeRef>) ruleAction.getParameterValue(PARAM_NODEREFS);

		NodeRef zipNodeRef = null;
		try {
			final Map<QName, Serializable> properties = new HashMap<>();
			zipName = koyaNodeService.getUniqueValidFileNameFromTitle(zipName);
			properties.put(ContentModel.PROP_NAME, zipName);
			ChildAssociationRef car = nodeService.createNode(companyTmpZipDir,
					ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, zipName),
					ContentModel.TYPE_CONTENT);

			zipNodeRef = car.getChildRef();
			Map<QName, Serializable> indexProp = new HashMap<>();
			indexProp.put(ContentModel.PROP_IS_INDEXED, false);
			indexProp.put(ContentModel.PROP_IS_CONTENT_INDEXED, false);
			nodeService.addAspect(zipNodeRef, ContentModel.ASPECT_INDEX_CONTROL, indexProp);
			
			nodeService.addAspect(zipNodeRef, KoyaModel.ASPECT_TEMPFILE, null);
			
			ContentWriter contentWriter = contentService.getWriter(zipNodeRef,
					ContentModel.PROP_CONTENT, true);
			OutputStream os = contentWriter.getContentOutputStream();
			CheckedOutputStream checksum = new CheckedOutputStream(os, new Adler32());
			BufferedOutputStream buff = new BufferedOutputStream(checksum);
			ZipArchiveOutputStream zipStream = new ZipArchiveOutputStream(buff);
			// NOTE: This encoding allows us to workaround bug...
			// http://bugs.sun.com/bugdatabase/view_bug.do;:WuuT?bug_id=4820807
			zipStream.setEncoding("UTF-8");

			zipStream.setMethod(ZipArchiveOutputStream.DEFLATED);
			zipStream.setLevel(Deflater.BEST_COMPRESSION);

			zipStream.setCreateUnicodeExtraFields(
					ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS);
			zipStream.setUseLanguageEncodingFlag(true);
			zipStream.setFallbackToUTF8(true);

			try {
				addToZip(nodeRefs, pdf, zipStream, "");
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			} finally {
				zipStream.close();
				buff.close();
				checksum.close();
				os.close();

			}
		} catch (IOException | WebScriptException e) {
			logger.error(e.getMessage(), e);
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		}

		if (!async) {
			ruleAction.setParameterValue(PARAM_RESULT, zipNodeRef);
		} else {
			KoyaActivityPoster.KoyaActivityInfo info = activityPoster.getActivityInfo(zipNodeRef);
			activityPoster.postDlFileAvailable(info,zipName);
		}
	}

	private void addToZip(List<NodeRef> nodeRefs, Boolean pdf, ZipArchiveOutputStream out,
			String path) throws IOException {
		for (NodeRef nodeRef : nodeRefs) {
			NodeRef nodeToAdd = nodeRef;
			String nodeName = null;
			if (pdf) {
				Action pdfRenderAction = actionService.createAction(PdfRenderActionExecuter.NAME);
				UserTransaction trx = transactionService.getNonPropagatingUserTransaction(false);
				try {
					trx.begin();
					actionService.executeAction(pdfRenderAction, nodeRef);
					trx.commit();
				} catch (Throwable e) {
					try {
						trx.rollback();
					} catch (IllegalStateException | SecurityException | SystemException e1) {
						;
					}
				}

				@SuppressWarnings("unchecked")
				Map<String, Serializable> result = (Map<String, Serializable>) pdfRenderAction
						.getParameterValue(PdfRenderActionExecuter.PARAM_RESULT);
				nodeToAdd = new NodeRef(
						result.get(PdfRenderActionExecuter.RESULT_PARAM_NODEREF).toString());
				nodeName = (String) result.get(PdfRenderActionExecuter.RESULT_PARAM_TITLE);
			}
			addToZip(nodeToAdd, pdf, nodeName, out, path);
		}
	}

	private void addToZip(NodeRef node, Boolean pdf, String nodeName, ZipArchiveOutputStream out,
			String path) throws IOException {
		QName nodeQnameType = this.nodeService.getType(node);

		// Special case : links
		if (this.dictionaryService.isSubClass(nodeQnameType, ApplicationModel.TYPE_FILELINK)) {
			NodeRef linkDestinationNode = (NodeRef) nodeService.getProperty(node,
					ContentModel.PROP_LINK_DESTINATION);
			if (linkDestinationNode == null) {
				return;
			}

			// Duplicate entry: check if link is not in the same space of the
			// link destination
			if (nodeService.getPrimaryParent(node).getParentRef()
					.equals(nodeService.getPrimaryParent(linkDestinationNode).getParentRef())) {
				return;
			}

			nodeQnameType = this.nodeService.getType(linkDestinationNode);
			node = linkDestinationNode;
		}

		/**
		 * TODO test name/title export result.
		 */
		if (nodeName == null) {
			nodeName = (String) nodeService.getProperty(node, ContentModel.PROP_TITLE);
		}
		nodeName = nodeName.replaceAll("([\\\"\\\\*\\\\\\>\\<\\?\\/\\:\\|]+)", "_");
		// nodeName = noaccent ? unAccent(nodeName) : nodeName;

		if (this.dictionaryService.isSubClass(nodeQnameType, ContentModel.TYPE_CONTENT)) {
			ContentReader reader = contentService.getReader(node, ContentModel.PROP_CONTENT);
			if (reader != null) {
				InputStream is = null;
				
				try {
					is = reader.getContentInputStream();
				} catch (ContentIOException cioex) {
					logger.error("Failed to read node content while add to zip (ContentIOException) : Silently return " + node.toString());
					logger.debug(cioex.toString());
					return;
				}
					
				String filename = path.isEmpty() ? nodeName : path + '/' + nodeName;

				ZipArchiveEntry entry = new ZipArchiveEntry(filename);
				entry.setTime(((Date) nodeService.getProperty(node, ContentModel.PROP_MODIFIED))
						.getTime());

				entry.setSize(reader.getSize());
				out.putArchiveEntry(entry);
				try {
					byte buffer[] = new byte[8192];
					while (true) {
						int nRead = is.read(buffer, 0, buffer.length);
						if (nRead <= 0) {
							break;
						}

						out.write(buffer, 0, nRead);
					}

				} catch (Exception exception) {
					logger.error(exception.getMessage(), exception);
				} finally {
					is.close();
					out.closeArchiveEntry();
				}
			} else {
				logger.warn("Could not read : " + nodeName + "content");
			}
		} else if (this.dictionaryService.isSubClass(nodeQnameType, ContentModel.TYPE_FOLDER)
				&& !this.dictionaryService.isSubClass(nodeQnameType,
						ContentModel.TYPE_SYSTEM_FOLDER)) {
			List<ChildAssociationRef> children = nodeService.getChildAssocs(node);
			if (children.isEmpty()) {
				String folderPath = path.isEmpty() ? nodeName + '/' : path + '/' + nodeName + '/';
				out.putArchiveEntry(new ZipArchiveEntry(folderPath));
				out.closeArchiveEntry();
			} else {
				List<NodeRef> nodeRefs = new ArrayList<>();
				for (ChildAssociationRef childAssoc : children) {
					NodeRef childNodeRef = childAssoc.getChildRef();
					nodeRefs.add(childNodeRef);
				}

				addToZip(nodeRefs, pdf, out, path.isEmpty() ? nodeName : path + '/' + nodeName);
			}
		} else {
			logger.info("Unmanaged type: " + nodeQnameType.getPrefixedQName(this.namespaceService)
					+ ", filename: " + nodeName);
		}
	}

}
