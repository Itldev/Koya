package fr.itldev.koya.alfservice;

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.Content;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.log4j.Logger;

public class KoyaAclService {

    private static final String ROLE_SITE_CONSUMER = "SiteConsumer";
    private static final String ROLE_SITE_COLLABORATOR = "SiteCollaborator";
    private static final String ROLE_SITE_CONTRIBUTOR = "SiteContributor";
    private static final String ROLE_SITE_MANAGER = "SiteManager";

    private final Logger logger = Logger.getLogger(this.getClass());

    private UserService userService;
    private PermissionService permissionService;
    private SiteService siteService;

    private KoyaNodeService koyaNodeService;
    private AuthenticationService authenticationService;

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
     * Set or unset read Acces to a secured item
     *
     * @param userName
     * @param item
     * @param set
     * @param recursive
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public void setReadAccess(String userName, SecuredItem item, Boolean set, Boolean recursive) throws KoyaServiceException {

        logger.trace("set read acces for user '" + userName + "' on " + item.getName() + " (" + item.getClass().getSimpleName() + ")");

        //TODO grant acces to other elements
        if (item.getClass().equals(Dossier.class)) {
            grantPermission(userName, (Dossier) item, PermissionService.READ, set);
        } else {
            logger.error("Unsupported sharing type " + item.getClass().getSimpleName());
        }

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
     * ================= grant acces by type ===========================
     */
    /**
     * Set or unset permission to a company
     *
     * @param userName
     * @param c
     * @param permission
     * @param set
     * @param recursive
     */
    public void grantPermission(String userName, Company c, String permission, Boolean set, Boolean recursive) {
        //todo process recursive         
        permissionService.setPermission(c.getNodeRefasObject(), userName, permission, true);
        if (!recursive) {
            //if non recursive permission -> add user to group site_compname
            siteService.setMembership(c.getName(), userName, permission);
        } else {

            //else add user to specific group site_compname_role
        }

    }

    public void setSiteAccess(String userName, Company c) {
        siteService.setMembership(c.getName(), userName, ROLE_SITE_CONSUMER);
    }

    /**
     * Set or unset read Acces to a space
     *
     * @param userName
     * @param s
     * @param permission
     * @param set
     * @param recursive
     */
    public void grantPermission(String userName, Space s, String permission, Boolean set, Boolean recursive) {
        //todo process recursive         
        permissionService.setPermission(s.getNodeRefasObject(), userName, permission, true);
    }

    /**
     * Set or unset read Acces to a company
     *
     * @param userName
     * @param d
     * @param permission
     * @param set
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public void grantPermission(String userName, Dossier d, String permission, Boolean set) throws KoyaServiceException {

        permissionService.setPermission(d.getNodeRefasObject(), userName, PermissionService.READ, true);

        //give read acces to a dossier means give non recursive read access to parents company and spaces.
        for (SecuredItem si : koyaNodeService.getParentsList(d, userName)) {

            if (si.getClass().equals(Space.class)) {
                grantPermission(userName, (Space) si, permission, set, Boolean.FALSE);
            } else if (si.getClass().equals(Company.class)) {
                setSiteAccess(userName, (Company) si);
            }
        }
    }

    /**
     * Set or unset read Acces to a company
     *
     * @param userName
     * @param c
     * @param set
     */
    public void grantPermission(String userName, Content c, Boolean set) {

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

}
