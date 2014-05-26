package fr.itldev.koya.alfservice;

import fr.itldev.koya.model.Permissions;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.log4j.Logger;

public class KoyaAclService {

    protected static final String ROLE_SITE_CONSUMER = "SiteConsumer";
    protected static final String ROLE_SITE_COLLABORATOR = "SiteCollaborator";
    protected static final String ROLE_SITE_CONTRIBUTOR = "SiteContributor";
    protected static final String ROLE_SITE_MANAGER = "SiteManager";

    private final Logger logger = Logger.getLogger(this.getClass());

    protected UserService userService;
    protected PermissionService permissionService;
    protected SiteService siteService;
    protected KoyaNodeService koyaNodeService;
    protected NodeService nodeService;
    protected FileFolderService fileFolderService;
    protected AuthenticationService authenticationService;

    // <editor-fold defaultstate="collapsed" desc="getters/setters">
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    //</editor-fold>
    /**
     * Grant Access for user with specified mail to item.
     *
     *
     * @param userLog
     * @param sharedItem
     * @param userMail
     */
    public void grantAccess(User userLog, SecuredItem sharedItem, String userMail) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

    }

    /**
     *
     * @param userLog
     * @param sharedItem
     * @param userMail
     */
    public void revokeAccess(User userLog, SecuredItem sharedItem, String userMail) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * ================================================================
     */
    /**
     * Set default Space Or Dossier Access on nodeRef
     *
     * Default is : no inherance.
     *
     *
     * @param n
     */
    public void setSpaceDossierDefaultAccess(NodeRef n) {
        permissionService.setInheritParentPermissions(n, false);

        Company c = koyaNodeService.getNodeCompany(n);

        permissionService.setPermission(n, buildCompanyAuthorityName(c, ROLE_SITE_COLLABORATOR), ROLE_SITE_COLLABORATOR, true);
        permissionService.setPermission(n, buildCompanyAuthorityName(c, ROLE_SITE_CONTRIBUTOR), ROLE_SITE_CONTRIBUTOR, true);
        permissionService.setPermission(n, buildCompanyAuthorityName(c, ROLE_SITE_MANAGER), ROLE_SITE_MANAGER, true);
    }

    /**
     *
     */
    /**
     *
     * @param c
     * @param roleName
     * @return
     */
    public String buildCompanyAuthorityName(Company c, String roleName) {
        return "GROUP_site_" + c.getName() + "_" + roleName;
    }

    /**
     * Builds Koya permissions on given NodeRef.
     *
     * @param n
     * @return
     */
    public Permissions getPermissions(NodeRef n) {

        Permissions p = new Permissions(authenticationService.getCurrentUserName());

        p.canAddChild(permissionService.hasPermission(n, PermissionService.ADD_CHILDREN).equals(AccessStatus.ALLOWED));
        p.canDelete(permissionService.hasPermission(n, PermissionService.DELETE_NODE).equals(AccessStatus.ALLOWED));
        p.canReadProperties(permissionService.hasPermission(n, PermissionService.WRITE).equals(AccessStatus.ALLOWED));

        /*
         * TODO define here these extra permissions policy.
         * OR 
         * define permission definition in a config file.        
         *  ex : only element owner and company administrator can rename ....
         *  
         */
        p.canRename(permissionService.hasPermission(n, PermissionService.WRITE).equals(AccessStatus.ALLOWED));
        p.canDownload(permissionService.hasPermission(n, PermissionService.WRITE).equals(AccessStatus.ALLOWED));
        //user can share if he's domain admin         
        p.canShare(permissionService.hasPermission(n, PermissionService.DELETE).equals(AccessStatus.ALLOWED));

        return p;

    }

}
