package fr.itldev.koya.alfservice;

import fr.itldev.koya.alfservice.security.CompanyAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.interfaces.SubSpace;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

/**
 *
 *
 */
public class DebuggerService {

    private final Logger logger = Logger.getLogger(this.getClass());

    protected ServiceRegistry serviceRegistry;
    protected PermissionService permissionService;
    protected KoyaNodeService koyaNodeService;
    protected NodeService nodeService;
    protected CompanyAclService companyAclService;

    // <editor-fold defaultstate="collapsed" desc="getters/setters">
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setCompanyAclService(CompanyAclService companyAclService) {
        this.companyAclService = companyAclService;
    }

    //</editor-fold>
    public void dumpUsersPermissions(String companyName) throws KoyaServiceException {

        SiteService siteService = serviceRegistry.getSiteService();
        logger.debug(" ===== Company " + companyName + " Members =====");

        Company c = koyaNodeService.companyBuilder(companyName);

        for (User uValid : companyAclService.listMembersValidated(companyName, null)) {
            logger.debug(uValid.getUserName() + " = " + companyAclService.getSitePermission(c, uValid));
        }

        logger.debug(" ===== Company " + companyName + " Invitees (not validated) =====");

        for (User uNotValid : companyAclService.listMembersPendingInvitation(companyName, null)) {
            logger.debug(uNotValid.getUserName() + " = " + companyAclService.getSitePermission(c, uNotValid));
        }

        logger.debug(" ===============================================");

    }

    private static final Set<QName> SUBSPACES_TYPES = Collections.unmodifiableSet(new HashSet() {
        {
            add(KoyaModel.TYPE_SPACE);
            add(KoyaModel.TYPE_DOSSIER);
        }
    });

    /**
     * Debug method used to Dump all SubSpaces Elements permissions in log.
     *
     *
     * @param root
     * @param indent
     * @throws KoyaServiceException
     */
    public void dumpSubSpacesPermissions(SubSpace root, String... indent) throws KoyaServiceException {

        String identation = "";
        if (indent.length == 1) {
            identation = indent[0];
        }

        logWithIndent("=== " + root.getTitle() + " (" + root.getClass().getSimpleName() + ")" + " = " + root.getNodeRef(), identation);

        //for each available types, list autorities
        for (AccessPermission perm : permissionService.getAllSetPermissions(root.getNodeRefasObject())) {
            logWithIndent(" + " + perm.getAuthority() + " => " + perm.getPermission(), identation);
        }

        /**
         * GO TO CHILD
         */
        for (ChildAssociationRef car : nodeService.getChildAssocs(root.getNodeRefasObject(), SUBSPACES_TYPES)) {
            dumpSubSpacesPermissions(
                    (SubSpace) koyaNodeService.nodeRef2SecuredItem(car.getChildRef()), identation + "   ");
        }

    }

    private void logWithIndent(String log, String indent) {
        logger.debug(indent + " " + log);
    }

}
