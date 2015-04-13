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

import fr.itldev.koya.alfservice.KoyaContentService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import static org.alfresco.repo.content.MimetypeMap.MIMETYPE_ZIP;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Mostly comming from atolcd ZipContents
 * http://github.com/atolcd/alfresco-zip-and-download.git
 *
 * http://www.atolcd.com/
 */
public class ZipContent extends AbstractWebScript {

    private static final String ARG_NODEREFS = "nodeRefs";

    private KoyaContentService koyaContentService;

    public void setKoyaContentService(KoyaContentService koyaContentService) {
        this.koyaContentService = koyaContentService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res)
            throws IOException {
        Map<String, Object> jsonPostMap = KoyaWebscript.getJsonMap(req);

        ArrayList<String> nodeRefs = new ArrayList<>();
        JSONArray jsonArray = (JSONArray) jsonPostMap.get(ARG_NODEREFS);
        if (jsonArray != null) {
            int len = jsonArray.size();
            for (int i = 0; i < len; i++) {
                nodeRefs.add(jsonArray.get(i).toString());
            }
        }

        try {
            res.setContentType(MIMETYPE_ZIP);
            res.setHeader("Content-Transfer-Encoding", "binary");
            res.addHeader("Content-Disposition", "attachment");

            res.setHeader("Cache-Control",
                    "must-revalidate, post-check=0, pre-check=0");
            res.setHeader("Pragma", "public");
            res.setHeader("Expires", "0");

            File tmpZipFile = koyaContentService.zip(nodeRefs);

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
            throw new WebScriptException("KoyaError : "
                    + ex.getErrorCode().toString());
        } catch (RuntimeException e) {
            /**
             * TODO koya specific exception
             */
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST,
                    "Erreur lors de la génération de l'archive.", e);
        }

    }

}
