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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.lucene.LuceneUtils;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.Pair;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.log4j.Logger;

import fr.itldev.koya.action.UpdateLastModificationDateActionExecuter;
import fr.itldev.koya.alfservice.security.SpaceAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.exceptions.KoyaErrorCodes;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Document;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;

/**
 * Dossiers Handling Service
 * 
 * 
 */
public class DossierService {

	private final Logger logger = Logger.getLogger(this.getClass());

	private NodeService nodeService;
	private KoyaNodeService koyaNodeService;
	protected SearchService searchService;
	private NamespacePrefixResolver prefixResolver;
	private TransactionService transactionService;
	private SpaceAclService spaceAclService;
	private KoyaContentService koyaContentService;
	private UserService userService;
	private AuthenticationService authenticationService;
	private OwnableService ownableService;
	private KoyaActivityPoster koyaActivityPoster;
	private ActionService actionService;

	// <editor-fold defaultstate="collapsed" desc="getters/setters">
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public void setPrefixResolver(NamespacePrefixResolver prefixResolver) {
		this.prefixResolver = prefixResolver;
	}

	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	public void setSpaceAclService(SpaceAclService spaceAclService) {
		this.spaceAclService = spaceAclService;
	}


	public void setKoyaContentService(KoyaContentService koyaContentService) {
		this.koyaContentService = koyaContentService;
	}


	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setAuthenticationService(
			AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}
	
	
	public void setOwnableService(OwnableService ownableService) {
		this.ownableService = ownableService;
	}

	public void setKoyaActivityPoster(KoyaActivityPoster koyaActivityPoster) {
		this.koyaActivityPoster = koyaActivityPoster;
	}

	public void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}
	// </editor-fold>
	/**
	 * 
	 * @param title
	 * @param parent
	 * @param prop
	 * @return
	 * @throws KoyaServiceException
	 */
	public Dossier create(String title, NodeRef parent, Map<QName, String> prop)
			throws KoyaServiceException {

		// Dossier must have a title
		if (title == null || title.isEmpty()) {
			throw new KoyaServiceException(KoyaErrorCodes.DOSSIER_EMPTY_NAME);
		}

		// parent must be a Space
		if (!nodeService.getType(parent).equals(KoyaModel.TYPE_SPACE)) {
			throw new KoyaServiceException(KoyaErrorCodes.DOSSIER_NOT_IN_SPACE);
		}

		String name = koyaNodeService.getUniqueValidFileNameFromTitle(title);

		if (nodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS,
				name) != null) {
			throw new KoyaServiceException(KoyaErrorCodes.DOSSIER_NAME_EXISTS);
		}

		// build node properties
		final Map<QName, Serializable> properties = new HashMap<>();
		properties.put(ContentModel.PROP_NAME, name);
		properties.put(ContentModel.PROP_TITLE, title);
		if (prop != null) {
			properties.putAll(prop);
		}

		ChildAssociationRef car = nodeService
				.createNode(parent, ContentModel.ASSOC_CONTAINS, QName
						.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
								name), KoyaModel.TYPE_DOSSIER, properties);

		Dossier created = koyaNodeService.getKoyaNode(car.getChildRef(),
				Dossier.class);
		
		// Add lastModified Aspect
			Map<QName, Serializable> props = new HashMap<>();
			props.put(KoyaModel.PROP_LASTMODIFICATIONDATE,
				new Date());
			nodeService.addAspect(created.getNodeRef(),
					KoyaModel.ASPECT_LASTMODIFIED, props);
		
		logger.info("[Koya] Dossier creation : " + created.getTitle()
				+ " created (" + created.getNodeRef() + ")");
		return created;
	}

	/**
	 * 
	 * @param parent
	 * @return
	 * @throws KoyaServiceException
	 */
	public List<Dossier> list(NodeRef parent) throws KoyaServiceException {

		if (!nodeService.getType(parent).equals(KoyaModel.TYPE_SPACE)) {
			throw new KoyaServiceException(
					KoyaErrorCodes.DOSSIER_INVALID_PARENT_NODE);
		}

		List nodes = nodeService.getChildAssocs(parent, new HashSet<QName>() {
			{
				add(KoyaModel.TYPE_DOSSIER);
			}
		});

		/**
		 * transform List<ChildAssociationRef> to List<Dossier>
		 */
		CollectionUtils.transform(nodes, new Transformer() {
			@Override
			public Object transform(Object input) {
				try {
					return koyaNodeService.getKoyaNode(
							((ChildAssociationRef) input).getChildRef(),
							Dossier.class);
				} catch (KoyaServiceException ex) {
					return null;
				}
			}
		});

		return nodes;
	}

	/**
	 * Get Dossier by reference
	 * 
	 * @param company
	 * @param reference
	 * @return
	 * @throws fr.itldev.koya.exception.KoyaServiceException
	 */
	public Dossier getDossier(final Company company, final String reference) {
		String luceneRequest = "TYPE:\"koya:dossier\" AND @koya\\:reference:\""
				+ reference + "\" AND PATH:\" /app:company_home/st:sites/cm:"
				+ company.getName() + "/cm:documentLibrary/*/*\"";

		ResultSet rs = null;
		try {
			rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
					SearchService.LANGUAGE_LUCENE, luceneRequest);

			if (rs.length() == 1) {
				return koyaNodeService.getKoyaNode(rs.iterator().next()
						.getNodeRef(), Dossier.class);
			}

			if (rs.length() == 0) {
				return null;
			}

			if (rs.length() > 1) {
				logger.error(rs.length() + " dossiers match reference "
						+ reference);
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
		return null;
	}

	public List<Dossier> getInactiveDossier(final Space space,
			final Date inactiveFrom, final boolean notNotifiedOnly)
			throws KoyaServiceException {
		String luceneRequest = "+PATH:\""
				+ nodeService.getPath(space.getNodeRef()).toPrefixString(
						prefixResolver)
				+ "/*\" +TYPE:\"koya:dossier\" +@koya\\:lastModificationDate:[MIN TO \""
				+ LuceneUtils.getLuceneDateString(inactiveFrom) + "\"]";
		if (notNotifiedOnly) {
			luceneRequest += " +@koya\\:notified:false";
		}
		ResultSet rs = null;
		try {
			rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
					SearchService.LANGUAGE_LUCENE, luceneRequest);

			List<Dossier> inactiveDossier = new ArrayList<>(rs.length());
			for (NodeRef n : rs.getNodeRefs()) {
				inactiveDossier.add(koyaNodeService.getKoyaNode(n,
						Dossier.class));
			}

			return inactiveDossier;
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
	}

	public void updateLastModificationDate(final Dossier d) {
		if (d == null) {
			return;
		}
		Action updateLastModificationDateAction = actionService.createAction(UpdateLastModificationDateActionExecuter.NAME);
		actionService.executeAction(updateLastModificationDateAction, d.getNodeRef(), false, true);
	}

	/*
	 * ========== Site Consumer Upload in specific upload directory ==========
	 */

	public Map<String, String> uploadSiteConsumerDocument(Dossier dossier,
			String fileName,
			org.springframework.extensions.surf.util.Content content,
			String clientMessage) throws KoyaServiceException {

		NodeRef upDir = koyaNodeService.getPublicUploadFolder(dossier);
		if (upDir == null) {
			upDir = createPublicUploadFolder(dossier);
		}
		final NodeRef finalUpDir = upDir;
		

		String realFileName = fileName;
		boolean exists = true;
		int uniqueFileCounter = 0;
		while (exists) {
			// run as System to check existance of any file in dir (even other
			// users files)
			
			final String finalFileName = realFileName;
			exists = AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Boolean>() {
				@Override
				public Boolean doWork() throws Exception {
					return nodeService.getChildByName(finalUpDir, ContentModel.ASSOC_CONTAINS,
							finalFileName) != null;
				}
			});
			
			if (exists) {
				// build new filename
				int dot = fileName.lastIndexOf('.');
				realFileName = fileName.substring(0, dot) + "-" + ++uniqueFileCounter
						+ fileName.substring(dot);
			}
		}
		
		
		Pair<NodeRef,Map<String, String>> uploadResult = koyaContentService.createContentNode(
				upDir, realFileName, null, content.getMimetype(),
				content.getEncoding(), content.getInputStream(), false);
		
		//===== permissions and ownable
		ownableService.setOwner(uploadResult.getFirst(), authenticationService.getCurrentUserName());
		//no inherence
		spaceAclService.initConsumerUploadedDocument(dossier, uploadResult.getFirst());
		
		Document d = koyaNodeService.getKoyaNode(
				new NodeRef(uploadResult.getSecond().get("nodeRef")), Document.class);
		User uploader = userService.getUserByUsername(authenticationService
				.getCurrentUserName());
		koyaActivityPoster.postConsumerUpload(d,dossier, uploader);
		return uploadResult.getSecond();
	}

	public List<Document> listSiteConsumerDocuments(final Dossier dossier) {
		NodeRef upDir = koyaNodeService.getPublicUploadFolder(dossier);
		if (upDir != null) {
			List<Document> docs = new ArrayList<>();

			for (KoyaNode k : koyaNodeService.listChildrenPaginated(upDir, 0,
					Integer.MAX_VALUE, false, null, null, null).getFirst()) {
				if (Document.class.isAssignableFrom(k.getClass())) {
					docs.add((Document) k);
				}
			}
			return docs;
		} else {
			return new ArrayList<>();
		}
	}

	private NodeRef createPublicUploadFolder(final Dossier dossier) {
		// create folder if not exists
		NodeRef upDir = AuthenticationUtil
				.runAsSystem(new AuthenticationUtil.RunAsWork<NodeRef>() {
					@Override
					public NodeRef doWork() throws Exception {
						
						Company c = koyaNodeService.getFirstParentOfType(
								dossier.getNodeRef(), Company.class);
						// create public client upload dir if not exists
						NodeRef companyClientUpDir = nodeService
								.getChildByName(c.getNodeRef(),
										ContentModel.ASSOC_CONTAINS,
										KoyaNodeService.SITECONSUMER_UPLOADDIR_NAME);

						if (companyClientUpDir == null) {
							final Map<QName, Serializable> properties = new HashMap<>();
							properties.put(ContentModel.PROP_NAME,
									KoyaNodeService.SITECONSUMER_UPLOADDIR_NAME);
							properties.put(ContentModel.PROP_TITLE,
									KoyaNodeService.SITECONSUMER_UPLOADDIR_NAME);

							ChildAssociationRef car = nodeService.createNode(
									c.getNodeRef(),
									ContentModel.ASSOC_CONTAINS,
									QName.createQName(
											NamespaceService.CONTENT_MODEL_1_0_URI,
											KoyaNodeService.SITECONSUMER_UPLOADDIR_NAME),
									ContentModel.TYPE_FOLDER, properties);		
							companyClientUpDir = car.getChildRef();
						}

						// create dossier public upload client dir
						final Map<QName, Serializable> properties = new HashMap<>();
						properties.put(ContentModel.PROP_NAME,
								koyaNodeService.publicUploadFolderName(dossier));
						properties.put(ContentModel.PROP_TITLE,
								dossier.getName());
						properties.put(KoyaModel.PROP_DOSSIERREF,
								dossier.getNodeRef());

						ChildAssociationRef car = nodeService.createNode(
								companyClientUpDir,
								ContentModel.ASSOC_CONTAINS, QName.createQName(
										NamespaceService.CONTENT_MODEL_1_0_URI,
										koyaNodeService.publicUploadFolderName(dossier)),
								KoyaModel.TYPE_DOSSIERCLASSIFYFOLDER, properties);

						spaceAclService.initSingleDossierSiteConsumerUploadDirAcl(
								dossier, car.getChildRef());
						return car.getChildRef();
					}
				});
		return upDir;
	}	
}
