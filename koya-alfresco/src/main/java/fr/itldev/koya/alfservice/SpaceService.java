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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;

/**
 * Spaces Handling service
 */
public class SpaceService {

	private final Logger logger = Logger.getLogger(this.getClass());

	private NodeService nodeService;
	private KoyaNodeService koyaNodeService;
	private CompanyService companyService;
	private FileFolderService fileFolderService;

	// <editor-fold defaultstate="collapsed" desc="getters/setters">
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	public void setCompanyService(CompanyService companyService) {
		this.companyService = companyService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	// </editor-fold>
	/**
	 * Space creation in a valid Container : Space or Company
	 * 
	 * @param title
	 * @param target
	 * @param prop
	 * @return
	 * @throws KoyaServiceException
	 */
	public Space create(String title, NodeRef target, Map<String, String> prop)
			throws KoyaServiceException {
		// Space must have a name
		if (title == null || title.isEmpty()) {
			throw new KoyaServiceException(KoyaErrorCodes.SPACE_EMPTY_NAME);
		}

		NodeRef nrParent = null;

		if (nodeService.getType(target).equals(KoyaModel.TYPE_SPACE)) {
			// if parent is a space, select his node
			nrParent = target;
		} else if (nodeService.getType(target).equals(KoyaModel.TYPE_COMPANY)) {
			// if it's a company, select documentLibrary's node
			nrParent = getDocLibNodeRef(target);
		} else {
			throw new KoyaServiceException(KoyaErrorCodes.SPACE_INVALID_PARENT);
		}

		String name = koyaNodeService.getUniqueValidFileNameFromTitle(title);

		// build node properties
		final Map<QName, Serializable> properties = new HashMap<>();
		properties.put(ContentModel.PROP_NAME, name);
		properties.put(ContentModel.PROP_TITLE, title);

		ChildAssociationRef car = nodeService
				.createNode(nrParent, ContentModel.ASSOC_CONTAINS, QName
						.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
								name), KoyaModel.TYPE_SPACE, properties);

		return koyaNodeService.getKoyaNode(car.getChildRef(), Space.class);
	}

	/**
	 * Returns Company Spaces recursive list.
	 * 
	 * @param companyShortName
	 * @param depth
	 * @return
	 * @throws fr.itldev.koya.exception.KoyaServiceException
	 */
	public List<Space> list(String companyShortName, Integer depth)
			throws KoyaServiceException {
		NodeRef nodeDocLib = getDocLibNodeRef(companyService.getSiteInfo(
				companyShortName).getNodeRef());
		return listRecursive(nodeDocLib, depth);
	}
	
	
	public List<Space> list(Company c)
			throws KoyaServiceException {
		NodeRef nodeDocLib = getDocLibNodeRef(c.getNodeRef());
		return listRecursive(nodeDocLib, Integer.MAX_VALUE);
	}

	/**
	 * private recursive spaces list builder method.
	 * 
	 * @param rootNodeRef
	 * @param depth
	 * @return
	 * @throws KoyaServiceException
	 */
	private List<Space> listRecursive(NodeRef rootNodeRef, Integer depth)
			throws KoyaServiceException {
		List<Space> spaces = new ArrayList<>();
		if (depth <= 0) {
			return spaces;// return empty list if max depth < = 0 : ie max depth
							// reached
		}

		for (final FileInfo fi : fileFolderService.listFolders(rootNodeRef)) {

			if (fi.getType().equals(KoyaModel.TYPE_SPACE)) {
				Space space = koyaNodeService.getKoyaNode(fi.getNodeRef(),
						Space.class);
				space.setChildSpaces(listRecursive(fi.getNodeRef(), depth - 1));
				spaces.add(space);
			}
		}
		return spaces;
	}

	/**
	 * 
	 * =============== private Methods =================
	 * 
	 */
	/**
	 * 
	 * Returns Company "documentLibrary" NodeRef (root spaces parent).
	 * 
	 * @param s
	 * @return
	 * @throws KoyaServiceException
	 */
	private NodeRef getDocLibNodeRef(NodeRef companyNodeRef)
			throws KoyaServiceException {
		// TODO cache noderef / companies
		// TODO use
		// siteService.getContainer(siteService.getSite(companyNodeRef).getShortName(),
		// SiteService.DOCUMENT_LIBRARY);
		for (ChildAssociationRef car : nodeService
				.getChildAssocs(companyNodeRef)) {
			if (nodeService.getProperty(car.getChildRef(),
					ContentModel.PROP_NAME).equals(KoyaNodeService.DOCLIB_NAME)) {
				return car.getChildRef();
			}
		}

		throw new KoyaServiceException(
				KoyaErrorCodes.SPACE_DOCLIB_NODE_NOT_FOUND);
	}
}
