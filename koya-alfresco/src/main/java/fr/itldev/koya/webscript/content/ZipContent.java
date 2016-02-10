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
package fr.itldev.koya.webscript.content;

import static org.alfresco.repo.content.MimetypeMap.MIMETYPE_ZIP;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.rest.framework.jacksonextensions.NodeRefSerializer;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.itldev.koya.alfservice.CompanyService;
import fr.itldev.koya.alfservice.KoyaContentService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.webscript.KoyaWebscript;

/**
 * Mostly comming from atolcd ZipContents
 * http://github.com/atolcd/alfresco-zip-and-download.git
 * 
 * http://www.atolcd.com/
 */
public class ZipContent extends AbstractWebScript {

	private static final String ARG_NODEREFS = "nodeRefs";
	private static final String ARG_PDF = "pdf";
	private static final String ARG_ZIPNAME = "zipname";

	private KoyaContentService koyaContentService;
	private KoyaNodeService koyaNodeService;
	private CompanyService companyService;

	public void setKoyaContentService(KoyaContentService koyaContentService) {
		this.koyaContentService = koyaContentService;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	public void setCompanyService(CompanyService companyService) {
		this.companyService = companyService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {

		NodeRef zipNodeRef;
		String response;

		try {
			Map<String, Object> jsonPostMap = KoyaWebscript.getJsonMap(req);

			ArrayList<NodeRef> nodeRefs = new ArrayList<>();
			JSONArray jsonArray = (JSONArray) jsonPostMap.get(ARG_NODEREFS);
			Boolean pdf = (Boolean) jsonPostMap.get(ARG_PDF);
			String zipname = (String) jsonPostMap.get(ARG_ZIPNAME);

			if (pdf == null) {
				pdf = Boolean.FALSE;
			}

			int len = jsonArray.size();
			for (int i = 0; i < len; i++) {
				NodeRef n = koyaNodeService.getNodeRef(jsonArray.get(i)
						.toString());
				nodeRefs.add(n);
			}

			Company c = (Company) koyaNodeService.getFirstParentOfType(
					nodeRefs.get(0), Company.class);

			NodeRef companyTmpZipDir = companyService.getTmpZipDir(c);

			zipNodeRef = koyaContentService.zip(nodeRefs, zipname, pdf, companyTmpZipDir);

			response = KoyaWebscript.getObjectAsJson(koyaNodeService
					.getKoyaNode(zipNodeRef));

		} catch (KoyaServiceException ex) {
			throw new WebScriptException("KoyaError : "
					+ ex.getErrorCode().toString());
		} catch (RuntimeException e) {
			/**
			 * TODO koya specific exception
			 */
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST,
					"Erreur lors de la génération de l'archive.", e);
		}
		res.setContentEncoding("UTF-8");
		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write(response);
	}
}
