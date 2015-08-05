/**
 * Koya is an alfresco module that provides a corporate orientated dataroom.
 *
 * Copyright (C) Itl Developpement 2014
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see `<http://www.gnu.org/licenses/>`.
 */

package fr.itldev.koya.action;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.log4j.Logger;

import fr.itldev.koya.alfservice.CompanyService;
import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.alfservice.security.SpaceAclService;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;

/**
 * This action cleans removes all user's groups member ship if groups belong to
 * defined company
 * 
 * 
 */
public class CleanUserPermissionsActionExecuter extends
		ActionExecuterAbstractBase {

	private Logger logger = Logger.getLogger(this.getClass());

	public static final String NAME = "cleanPermissions";

	public static final String PARAM_USERNAME = "userName";

	private SpaceAclService spaceAclService;
	private NodeService nodeService;
	private CompanyService companyService;
	private UserService userService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setSpaceAclService(SpaceAclService spaceAclService) {
		this.spaceAclService = spaceAclService;
	}

	public void setCompanyService(CompanyService companyService) {
		this.companyService = companyService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@Override
	protected void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef) {

		if (!nodeService.getType(actionedUponNodeRef).equals(
				SiteModel.TYPE_SITE)) {
			// if node is not a site then abort action
			return;
		}
		String siteName = (String) nodeService.getProperty(actionedUponNodeRef,
				ContentModel.PROP_NAME);
		Company c = companyService.getCompany(siteName);

		User u = userService.getUser((String) ruleAction
				.getParameterValue(PARAM_USERNAME));

		// removes any koya autority membership for user on spaces groups he
		// belongs to
		List<Space> spaceToRemovePermissions = spaceAclService
				.getKoyaUserSpaces(u, c);
		for (Space s : spaceToRemovePermissions) {
			spaceAclService.removeAnyKoyaAuthority(s, u);
		}

	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {

		paramList.add(new ParameterDefinitionImpl(PARAM_USERNAME,
				DataTypeDefinition.NODE_REF, true,
				getParamDisplayLabel(PARAM_USERNAME)));
	}

}
