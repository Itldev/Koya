package fr.itldev.koya.webscript.share;

import fr.itldev.koya.alfservice.KoyaShareService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.io.IOException;
import java.util.Map;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * List users shares in a company.
 *
 *
 */
public class ListUserShares extends AbstractWebScript {

    private KoyaShareService koyaShareService;

    public void setKoyaShareService(KoyaShareService koyaShareService) {
        this.koyaShareService = koyaShareService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, String> urlParams = KoyaWebscript.getUrlParamsMap(req);
        String userName = (String) urlParams.get(KoyaWebscript.WSCONST_USERNAME);
        String companyName = (String) urlParams.get(KoyaWebscript.WSCONST_COMPANYNAME);
        String response;

        try {
            response = KoyaWebscript.getObjectAsJson(koyaShareService.listItemsShared(userName, companyName));
        } catch (KoyaServiceException ex) {
            throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
        }
        res.setContentType("application/json");
        res.getWriter().write(response);
    }
}
