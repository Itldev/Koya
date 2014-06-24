package fr.itldev.koya.webscript.content;

import fr.itldev.koya.alfservice.KoyaContentService;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Mostly comming from atolcd ZipContents
 * http://github.com/atolcd/alfresco-zip-and-download.git
 *
 * http://www.atolcd.com/
 */
public class ZipContent extends KoyaWebscript {

    private static Log logger = LogFactory.getLog(ZipContent.class);
//    private static final int BUFFER_SIZE = 8192;
    private static final String ARG_NODEREFS = "nodeRefs";
//    private static final String ARG_ZIPNAME = "zipName";

    private KoyaContentService koyaContentService;

    public void setKoyaContentService(KoyaContentService koyaContentService) {
        this.koyaContentService = koyaContentService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, Object> jsonPostMap = getJsonMap(req);

        ArrayList<String> nodeRefs = new ArrayList<>();
        JSONArray jsonArray = (JSONArray) jsonPostMap.get(ARG_NODEREFS);
        if (jsonArray != null) {
            int len = jsonArray.size();
            for (int i = 0; i < len; i++) {
                nodeRefs.add(jsonArray.get(i).toString());
            }
        }

//        String zipname = (String) jsonPostMap.get(ARG_ZIPNAME);

        try {
            res.setContentType(MIMETYPE_ZIP);
            res.setHeader("Content-Transfer-Encoding", "binary");
            res.addHeader("Content-Disposition", "attachment");

            res.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
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
        } catch (RuntimeException e) {
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Erreur lors de la génération de l'archive.", e);
        }

    }


    @Override
    public ItlAlfrescoServiceWrapper koyaExecute(ItlAlfrescoServiceWrapper wrapper, Map<String, String> urlParams, Map<String, Object> jsonPostMap) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
