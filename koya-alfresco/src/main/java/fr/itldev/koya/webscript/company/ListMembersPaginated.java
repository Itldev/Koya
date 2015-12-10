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
package fr.itldev.koya.webscript.company;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.itldev.koya.alfservice.security.CompanyAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.webscript.KoyaWebscript;

/**
 * List Company members.
 */
public class ListMembersPaginated extends AbstractWebScript {

	private CompanyAclService companyAclService;

	// private KoyaNodeService koyaNodeService;

	public void setCompanyAclService(CompanyAclService companyAclService) {
		this.companyAclService = companyAclService;
	}

	// public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
	// this.koyaNodeService = koyaNodeService;
	// }

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {

		Map<String, String> urlParamsMap = KoyaWebscript.getUrlParamsMap(req);
		String companyName = urlParamsMap
				.get(KoyaWebscript.WSCONST_COMPANYNAME);
		String roleFilter = urlParamsMap.get(KoyaWebscript.WSCONST_ROLEFILTER);

		String sortField = urlParamsMap.get("sortField");
		sortField = (!sortField.isEmpty()) ? sortField.replaceFirst("-", ":")
				: null;
		String strAscending = urlParamsMap.get("ascending");
		Boolean ascending = (strAscending != null) ? Boolean
				.valueOf(strAscending) : true;
		String typeFilter = urlParamsMap.get("typeFilter");

		List<String> roles = new ArrayList<>();
		try {
			for (String role : roleFilter.split(",")) {
				roles.add(role);
			}
		} catch (Exception ex) {
		}

		Boolean withAdmins = ((String) urlParamsMap.get("withAdmins"))
				.equals("true");

		Integer maxItems = null;
		Integer skipCount = null;

		maxItems = Integer.valueOf(urlParamsMap.get("maxItems"));
		skipCount = Integer.valueOf(urlParamsMap.get("skipCount"));

		String response;
		try {

			response = KoyaWebscript.getObjectAsJson(companyAclService
					.listMembersPaginated(companyName, roles, skipCount,
							maxItems, withAdmins, sortField, ascending));
		} catch (KoyaServiceException ex) {
			throw new WebScriptException("KoyaError : "
					+ ex.getErrorCode().toString());
		}
		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write(response);
	}
}
