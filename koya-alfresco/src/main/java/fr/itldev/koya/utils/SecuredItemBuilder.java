package fr.itldev.koya.utils;

import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Directory;
import fr.itldev.koya.model.impl.Document;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import java.util.Date;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 *
 *
 */
public class SecuredItemBuilder {

    private final NodeService nodeService;
    private final KoyaNodeService koyaNodeService;

    public SecuredItemBuilder(NodeService nodeService, KoyaNodeService koyaNodeService) {
        this.nodeService = nodeService;
        this.koyaNodeService = koyaNodeService;
    }

    public SecuredItem build(NodeRef nodeRef) throws KoyaServiceException {

        QName type = nodeService.getType(nodeRef);
        SecuredItem si;
        if (type.equals(KoyaModel.TYPE_COMPANY)) {
            si = companyBuilder(nodeRef);
        } else if (type.equals(KoyaModel.TYPE_SPACE)) {
            si = nodeSpaceBuilder(nodeRef);
        } else if (type.equals(KoyaModel.TYPE_DOSSIER)) {
            si = nodeDossierBuilder(nodeRef);
        } else if (type.equals(ContentModel.TYPE_FOLDER) && (koyaNodeService.getFirstParentOfType(nodeRef, Dossier.class) != null)) {
            si = nodeDirBuilder(nodeRef);
        } else if (type.equals(ContentModel.TYPE_CONTENT) && (koyaNodeService.getFirstParentOfType(nodeRef, Dossier.class) != null)) {
            si = nodeDocumentBuilder(nodeRef);
        } else {
            throw new KoyaServiceException(KoyaErrorCodes.INVALID_SECUREDITEM_NODEREF);
        }
        return si;
    }

    private Company companyBuilder(NodeRef n) throws KoyaServiceException {
        Company c = Company.newInstance();
        c.setName((String) nodeService.getProperty(n, ContentModel.PROP_NAME));
        c.setTitle((String) nodeService.getProperty(n, ContentModel.PROP_TITLE));
        c.setNodeRefasObject(n);
        return c;
    }

    private Space nodeSpaceBuilder(final NodeRef spaceNodeRef) throws KoyaServiceException {
        Space s = Space.newInstance();
        s.setNodeRef(spaceNodeRef.toString());
        s.setName((String) nodeService.getProperty(spaceNodeRef, ContentModel.PROP_NAME));
        s.setTitle((String) nodeService.getProperty(spaceNodeRef, ContentModel.PROP_TITLE));
        return s;
    }

    private Dossier nodeDossierBuilder(final NodeRef dossierNodeRef) {
        Dossier d = Dossier.newInstance();
        d.setNodeRef(dossierNodeRef.toString());
        d.setName((String) nodeService.getProperty(dossierNodeRef, ContentModel.PROP_NAME));
        d.setLastModifiedDate((Date) nodeService.getProperty(dossierNodeRef, KoyaModel.PROP_LASTMODIFICATIONDATE));
        d.setTitle((String) nodeService.getProperty(dossierNodeRef, ContentModel.PROP_TITLE));
        return d;
    }

    private Directory nodeDirBuilder(NodeRef dirNodeRef) {
        Directory dir = Directory.newInstance();
        dir.setNodeRef(dirNodeRef.toString());
        dir.setName((String) nodeService.getProperty(dirNodeRef, ContentModel.PROP_NAME));
        dir.setTitle((String) nodeService.getProperty(dirNodeRef, ContentModel.PROP_TITLE));
        return dir;
    }

    private Document nodeDocumentBuilder(final NodeRef docNodeRef) {
        Document doc = Document.newInsance();
        doc.setNodeRef(docNodeRef.toString());
        doc.setName((String) nodeService.getProperty(docNodeRef, ContentModel.PROP_NAME));
        doc.setTitle((String) nodeService.getProperty(docNodeRef, ContentModel.PROP_TITLE));
        doc.setByteSize(AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork< Long>() {
            @Override
            public Long doWork() throws Exception {
                return koyaNodeService.getByteSize(docNodeRef);
            }
        }));

        ContentData contentData = (ContentData) nodeService.getProperty(docNodeRef, ContentModel.PROP_CONTENT);
        doc.setMimeType(contentData.getMimetype());
        return doc;
    }

}
