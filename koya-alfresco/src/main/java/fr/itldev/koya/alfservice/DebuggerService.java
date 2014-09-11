package fr.itldev.koya.alfservice;

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.interfaces.SubSpace;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationService;
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

    //</editor-fold>
    public void dumpUsersPermissions(String companyName) {
        
        SiteService siteService = serviceRegistry.getSiteService();
         logger.debug(" ===== Company " + companyName + " Members =====");
        Map<String, String> members = siteService.listMembers(companyName, null, null, 0);
        for (String k : members.keySet()) {
            logger.debug(k + " = " + members.get(k));
        }
        
        logger.debug(" ===== Company " + companyName + " Invitees (not validated) =====");
        InvitationService invitationService = serviceRegistry.getInvitationService();
        for (Invitation i : invitationService.listPendingInvitationsForResource(Invitation.ResourceType.WEB_SITE, companyName)) {
            logger.debug(i.getInviteeUserName() + " = " + i.getRoleName());
        }
        logger.debug(" ===============================================");
        
    }

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
        
        logWithIndent("=== " + root.getName() + " (" + root.getClass().getSimpleName() + ")" + " = " + root.getNodeRef(), identation);

        //for each available types, list autorities
        for (AccessPermission perm : permissionService.getAllSetPermissions(root.getNodeRefasObject())) {
            logWithIndent(" + " + perm.getAuthority() + " => " + perm.getPermission(), identation);
        }

        /**
         * GO TO CHILD
         */
        Set<QName> types = new HashSet<>();
        types.add(KoyaModel.TYPE_SPACE);
        types.add(KoyaModel.TYPE_DOSSIER);
        for (ChildAssociationRef car : nodeService.getChildAssocs(root.getNodeRefasObject(), types)) {
            dumpSubSpacesPermissions(
                    (SubSpace) koyaNodeService.nodeRef2SecuredItem(car.getChildRef()), identation + "   ");
        }
        
    }
    
    private void logWithIndent(String log, String indent) {
        logger.debug(indent + " " + log);
    }
    
}
