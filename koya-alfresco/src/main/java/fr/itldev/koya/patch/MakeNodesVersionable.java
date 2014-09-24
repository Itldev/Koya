package fr.itldev.koya.patch;

import fr.itldev.koya.alfservice.KoyaNodeService;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;

/**
 *
 * Make all xPathNodes Versionnable
 *
 */
public class MakeNodesVersionable extends AbstractPatch {

    private List<String> xPathNodes;
    private KoyaNodeService koyaNodeService;
    private VersionService versionService;

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void setXPathNodes(List<String> xPathNodes) {
        this.xPathNodes = xPathNodes;
    }

    public void setVersionService(VersionService versionService) {
        this.versionService = versionService;
    }

    @Override
    protected String applyInternal() throws Exception {

        for (String xPath : xPathNodes) {
            versionService.ensureVersioningEnabled(
                    koyaNodeService.xPath2NodeRef(xPath), new HashMap<QName, Serializable>() {
                        {
                            put(ContentModel.PROP_VERSION_LABEL, "1.0");
                        }
                    });
        }
        return "";

    }

}
