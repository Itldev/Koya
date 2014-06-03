package fr.itldev.koya.webscript.share;

import fr.itldev.koya.alfservice.KoyaShareService;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.util.Map;

/**
 * List users shares in a company.
 *
 *
 */
public class ListUserShares extends KoyaWebscript {

    private KoyaShareService koyaShareService;

    public void setKoyaShareService(KoyaShareService koyaShareService) {
        this.koyaShareService = koyaShareService;
    }

    @Override
    public ItlAlfrescoServiceWrapper koyaExecute(ItlAlfrescoServiceWrapper wrapper, Map<String, String> urlParams, Map<String, Object> jsonPostMap) throws Exception {

        String userName = (String) urlParams.get(KoyaWebscript.WSCONST_USERNAME);
        String companyName = (String) urlParams.get(KoyaWebscript.WSCONST_COMPANYNAME);
        wrapper.addItems(koyaShareService.listItemsShared(userName, companyName));
        return wrapper;
    }
}
