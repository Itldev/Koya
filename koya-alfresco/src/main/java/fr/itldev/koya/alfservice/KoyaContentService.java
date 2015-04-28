/**
 * Koya is an alfresco module that provides a corporate orientated dataroom.
 *
 * Copyright (C) Itl Developpement 2014
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see `<http://www.gnu.org/licenses/>`.
 */
package fr.itldev.koya.alfservice;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Deflater;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.ActivityPoster;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.WebScriptException;

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.Directory;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;

/**
 * Koya Specific documents and directories Service.
 */
public class KoyaContentService {

	private final Logger logger = Logger.getLogger(this.getClass());

	private NodeService nodeService;
	private KoyaNodeService koyaNodeService;

	protected DictionaryService dictionaryService;
	protected ContentService contentService;
	protected NamespaceService namespaceService;
	protected FileFolderService fileFolderService;
	protected ActionService actionService;
	private ActivityPoster activityPoster;

	// <editor-fold defaultstate="collapsed" desc="getters/setters">
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}

	public void setActivityPoster(ActivityPoster activityPoster) {
		this.activityPoster = activityPoster;
	}

	// </editor-fold>
	public Directory createDir(String title, NodeRef parent)
			throws KoyaServiceException {

		if (!(nodeService.getType(parent).equals(KoyaModel.TYPE_DOSSIER) || nodeService
				.getType(parent).equals(ContentModel.TYPE_FOLDER))) {
			throw new KoyaServiceException(
					KoyaErrorCodes.DIR_CREATION_INVALID_PARENT_TYPE);
		}

		String name = koyaNodeService.getUniqueValidFileNameFromTitle(title);

		// build node properties
		final Map<QName, Serializable> properties = new HashMap<>();
		properties.put(ContentModel.PROP_NAME, name);
		properties.put(ContentModel.PROP_TITLE, title);

		try {
			ChildAssociationRef car = nodeService.createNode(parent,
					ContentModel.ASSOC_CONTAINS, QName.createQName(
							NamespaceService.CONTENT_MODEL_1_0_URI, name),
					ContentModel.TYPE_FOLDER, properties);

			NodeRef dirNodeRef = car.getChildRef();

			activityPoster.postFileFolderAdded(dirNodeRef);

			return koyaNodeService.getKoyaNode(dirNodeRef, Directory.class);
		} catch (DuplicateChildNodeNameException dcne) {
			throw new KoyaServiceException(
					KoyaErrorCodes.DIR_CREATION_NAME_EXISTS);
		}

	}

	/**
	 * Checks for the presence of, and creates as necessary, the folder
	 * structure in the provided path.
	 * <p>
	 * An empty path list is not allowed as it would be impossible to
	 * necessarily return file info for the parent node - it might not be a
	 * folder node.
	 * 
	 * @param parentNodeRef
	 *            the node under which the path will be created
	 * @param pathElements
	 *            the folder name path to create - may not be empty
	 * 
	 * @return Returns the info of the last folder in the path.
	 */
	public Directory makeFolders(NodeRef parentNodeRef,
			List<String> pathElements) throws KoyaServiceException {
		if (!(nodeService.getType(parentNodeRef).equals(KoyaModel.TYPE_DOSSIER) || nodeService
				.getType(parentNodeRef).equals(ContentModel.TYPE_FOLDER))) {
			throw new KoyaServiceException(
					KoyaErrorCodes.DIR_CREATION_INVALID_PARENT_TYPE);
		}

		if (pathElements != null && pathElements.size() == 0) {
			throw new IllegalArgumentException("Path element list is empty");
		}
		NodeRef currentParentRef = parentNodeRef;
		// just loop and create if necessary
		for (final String pathElement : pathElements) {
			// ignoring empty path part
			if (pathElement != null && !pathElement.isEmpty()) {
				// does it exist?
				// Navigation should not check permissions
				NodeRef nodeRef = AuthenticationUtil
						.runAsSystem(new SearchAsSystem(
								fileFolderService,
								currentParentRef,
								koyaNodeService
										.getUniqueValidFileNameFromTitle(pathElement)));

				if (nodeRef == null) {
					try {
						// not present - make it
						// If this uses the public service it will check create
						// permissions
						Directory directory = createDir(pathElement,
								currentParentRef);
						currentParentRef = directory.getNodeRef();
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
					} finally {

					}
				} else {
					// it exists
					currentParentRef = nodeRef;
				}
			}
		}

		return koyaNodeService.getKoyaNode(currentParentRef, Directory.class);
	}

	private static class SearchAsSystem implements
			AuthenticationUtil.RunAsWork<NodeRef> {

		FileFolderService service;
		NodeRef node;
		String name;

		SearchAsSystem(FileFolderService service, NodeRef node, String name) {
			this.service = service;
			this.node = node;
			this.name = name;
		}

		public NodeRef doWork() throws Exception {
			return service.searchSimple(node, name);
		}
	}

	public Map<String, String> createContentNode(NodeRef parent,
			String fileName,
			org.springframework.extensions.surf.util.Content content)
			throws KoyaServiceException {

		return createContentNode(parent, fileName, null, content.getMimetype(),
				content.getEncoding(), content.getInputStream());
	}

	public Map<String, String> createContentNode(NodeRef parent,
			String fileName, InputStream contentInputStream)
			throws KoyaServiceException {
		return createContentNode(parent, fileName, null, contentInputStream);
	}

	public Map<String, String> createContentNode(NodeRef parent,
			String fileName, String name, InputStream contentInputStream)
			throws KoyaServiceException {
		return createContentNode(parent, fileName, name, null, null,
				contentInputStream);
	}

	public Map<String, String> createContentNode(NodeRef parent,
			String fileName, String name, String mimetype, String encoding,
			InputStream contentInputStream) throws KoyaServiceException {
		Boolean rename = false;
		if (name == null) {
			name = koyaNodeService.getUniqueValidFileNameFromTitle(fileName);

			rename = !fileName.equals(name);
		}

		/**
		 * CREATE NODE
		 */
		NodeRef createdNode;
		try {
			final Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, name);
			properties.put(ContentModel.PROP_TITLE, fileName);
			ChildAssociationRef car = nodeService.createNode(parent,
					ContentModel.ASSOC_CONTAINS, QName.createQName(
							NamespaceService.CONTENT_MODEL_1_0_URI, name),
					ContentModel.TYPE_CONTENT, properties);

			createdNode = car.getChildRef();

			activityPoster.postFileFolderAdded(createdNode);
		} catch (DuplicateChildNodeNameException ex) {
			throw new KoyaServiceException(
					KoyaErrorCodes.FILE_UPLOAD_NAME_EXISTS, fileName);
		} catch (IllegalArgumentException ex) {
			logger.error(fileName);
			throw ex;
		}

		/**
		 * ADD CONTENT TO CREATED NODE
		 * 
		 */
		ContentWriter writer = this.contentService.getWriter(createdNode,
				ContentModel.PROP_CONTENT, true);
		if (mimetype != null) {
			writer.setMimetype(mimetype);
		} else {
			writer.guessMimetype(fileName);
		}
		if (encoding != null) {
			writer.setEncoding(encoding);
		}
		writer.guessEncoding();
		writer.putContent(contentInputStream);

		Map<String, String> retMap = new HashMap<>();

		retMap.put("filename", name);
		retMap.put("originalFilename", fileName);
		retMap.put("rename", rename.toString());
		retMap.put("size", Long.toString(writer.getSize()));

		return retMap;
	}

	/**
	 * 
	 * TODO execute in an action.
	 * 
	 */
	/**
	 * 
	 * @param nodeRefs
	 * @return
	 * @throws KoyaServiceException
	 */
	public File zip(List<String> nodeRefs) throws KoyaServiceException {
		File tmpZipFile = null;
		try {
			tmpZipFile = TempFileProvider.createTempFile("tmpDL", ".zip");
			FileOutputStream fos = new FileOutputStream(tmpZipFile);
			CheckedOutputStream checksum = new CheckedOutputStream(fos,
					new Adler32());
			BufferedOutputStream buff = new BufferedOutputStream(checksum);
			ZipArchiveOutputStream zipStream = new ZipArchiveOutputStream(buff);
			// NOTE: This encoding allows us to workaround bug...
			// http://bugs.sun.com/bugdatabase/view_bug.do;:WuuT?bug_id=4820807
			zipStream.setEncoding("UTF-8");

			zipStream.setMethod(ZipArchiveOutputStream.DEFLATED);
			zipStream.setLevel(Deflater.BEST_COMPRESSION);

			zipStream
					.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS);
			zipStream.setUseLanguageEncodingFlag(true);
			zipStream.setFallbackToUTF8(true);

			try {
				for (String nodeRef : nodeRefs) {
					addToZip(koyaNodeService.getNodeRef(nodeRef), zipStream, "");
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				throw new WebScriptException(
						HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			} finally {
				zipStream.close();
				buff.close();
				checksum.close();
				fos.close();

			}
		} catch (IOException | WebScriptException e) {
			logger.error(e.getMessage(), e);
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST,
					e.getMessage());
		}

		return tmpZipFile;
	}

	public static final List<String> ZIP_MIMETYPES = Collections
			.unmodifiableList(new ArrayList() {
				{
					add(MimetypeMap.MIMETYPE_ZIP);
					add("application/x-zip-compressed");
					add("application/x-zip");
				}
			});

	private void addToZip(NodeRef node, ZipArchiveOutputStream out, String path)
			throws IOException {
		QName nodeQnameType = this.nodeService.getType(node);

		// Special case : links
		if (this.dictionaryService.isSubClass(nodeQnameType,
				ApplicationModel.TYPE_FILELINK)) {
			NodeRef linkDestinationNode = (NodeRef) nodeService.getProperty(
					node, ContentModel.PROP_LINK_DESTINATION);
			if (linkDestinationNode == null) {
				return;
			}

			// Duplicate entry: check if link is not in the same space of the
			// link destination
			if (nodeService
					.getPrimaryParent(node)
					.getParentRef()
					.equals(nodeService.getPrimaryParent(linkDestinationNode)
							.getParentRef())) {
				return;
			}

			nodeQnameType = this.nodeService.getType(linkDestinationNode);
			node = linkDestinationNode;
		}

		/**
		 * TODO test name/title export result.
		 */
		String nodeName = (String) nodeService.getProperty(node,
				ContentModel.PROP_TITLE);
		nodeName = nodeName.replaceAll("([\\\"\\\\*\\\\\\>\\<\\?\\/\\:\\|]+)",
				"_");
		// nodeName = noaccent ? unAccent(nodeName) : nodeName;

		if (this.dictionaryService.isSubClass(nodeQnameType,
				ContentModel.TYPE_CONTENT)) {
			ContentReader reader = contentService.getReader(node,
					ContentModel.PROP_CONTENT);
			if (reader != null) {
				InputStream is = reader.getContentInputStream();

				String filename = path.isEmpty() ? nodeName : path + '/'
						+ nodeName;

				ZipArchiveEntry entry = new ZipArchiveEntry(filename);
				entry.setTime(((Date) nodeService.getProperty(node,
						ContentModel.PROP_MODIFIED)).getTime());

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
		} else if (this.dictionaryService.isSubClass(nodeQnameType,
				ContentModel.TYPE_FOLDER)
				&& !this.dictionaryService.isSubClass(nodeQnameType,
						ContentModel.TYPE_SYSTEM_FOLDER)) {
			List<ChildAssociationRef> children = nodeService
					.getChildAssocs(node);
			if (children.isEmpty()) {
				String folderPath = path.isEmpty() ? nodeName + '/' : path
						+ '/' + nodeName + '/';
				out.putArchiveEntry(new ZipArchiveEntry(folderPath));
				out.closeArchiveEntry();
			} else {
				for (ChildAssociationRef childAssoc : children) {
					NodeRef childNodeRef = childAssoc.getChildRef();

					addToZip(childNodeRef, out, path.isEmpty() ? nodeName
							: path + '/' + nodeName);
				}
			}
		} else {
			logger.info("Unmanaged type: "
					+ nodeQnameType.getPrefixedQName(this.namespaceService)
					+ ", filename: " + nodeName);
		}
	}

}
