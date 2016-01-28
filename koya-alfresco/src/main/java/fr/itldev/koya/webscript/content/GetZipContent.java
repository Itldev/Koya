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

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.io.IOUtils;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.itldev.koya.alfservice.KoyaContentService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.webscript.KoyaWebscript;

/**
 * Mostly comming from atolcd ZipContents
 * http://github.com/atolcd/alfresco-zip-and-download.git
 *
 * http://www.atolcd.com/
 */
public class GetZipContent extends AbstractWebScript {

	private static final String ARG_NODEIDS = "nodeIds";
	private static final String ARG_PDF = "pdf";

	private KoyaContentService koyaContentService;
	private KoyaNodeService koyaNodeService;

	public void setKoyaContentService(KoyaContentService koyaContentService) {
		this.koyaContentService = koyaContentService;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		Map<String, String> urlParamsMap = KoyaWebscript.getUrlParamsMap(req);
		Boolean pdf;
		try {
			pdf = Boolean.valueOf(urlParamsMap.get(ARG_PDF));
		} catch (Exception e) {
			pdf = Boolean.FALSE;
		}

		String nodeIds;
		try {
			nodeIds = urlParamsMap.get(ARG_NODEIDS);
		} catch (Exception e) {
			nodeIds = "";
		}

		ArrayList<NodeRef> nodeRefs = new ArrayList<>();

		for (String nodeId : nodeIds.split(",")) {
			try {
				nodeRefs.add(koyaNodeService.getNodeRef("workspace://SpacesStore/" + nodeId));
			} catch (Exception ae) {
				// ignore invalid nodeId
			}
		}

		try {
			res.setContentType(MIMETYPE_ZIP);
			res.setHeader("Content-Transfer-Encoding", "binary");
			res.addHeader("Content-Disposition", "attachment");

			res.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
			res.setHeader("Pragma", "public");
			res.setHeader("Expires", "0");

			File tmpZipFile = koyaContentService.zip(nodeRefs, pdf);

			OutputStream outputStream = res.getOutputStream();			
			if (nodeRefs.size() > 0) {
				InputStream in = new FileInputStream(tmpZipFile);
				try {
					byte[] buffer = new byte[8192];
					int len;

					while ((len = in.read(buffer)) > 0) {
						outputStream.write(buffer, 0, len);
					}
				} finally {
					IOUtils.closeQuietly(in);
				}
			}
		} catch (KoyaServiceException ex) {
			throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
		} catch (RuntimeException e) {
			/**
			 * TODO koya specific exception
			 */
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST,
					"Erreur lors de la génération de l'archive.", e);
		}

	}

}
