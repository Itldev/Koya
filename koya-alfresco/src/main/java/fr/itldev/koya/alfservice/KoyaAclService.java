package fr.itldev.koya.alfservice;

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.Permissions;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

public class KoyaAclService {

    protected static final String ROLE_SITE_CONSUMER = "SiteConsumer";
    protected static final String ROLE_SITE_COLLABORATOR = "SiteCollaborator";
    protected static final String ROLE_SITE_CONTRIBUTOR = "SiteContributor";
    protected static final String ROLE_SITE_MANAGER = "SiteManager";

    protected static final String PERMISSION_READ = "Read";

    private final Logger logger = Logger.getLogger(this.getClass());

    protected PermissionService permissionService;
    protected KoyaNodeService koyaNodeService;
    protected NodeService nodeService;
    protected AuthenticationService authenticationService;
    protected SearchService searchService;

    // <editor-fold defaultstate="collapsed" desc="getters/setters">
    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
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
    public void setSpaceDossierDefaultAccess(NodeRef n) throws KoyaServiceException {
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
        p.canReadProperties(permissionService.hasPermission(n, PermissionService.READ_PROPERTIES).equals(AccessStatus.ALLOWED));
        p.canWriteProperties(permissionService.hasPermission(n, PermissionService.WRITE_PROPERTIES).equals(AccessStatus.ALLOWED));

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

    /**
     * Return every Readable by user SecuredItem Node found in application.
     *
     * If typesFilter is not null, filter in type class given.
     *
     *
     * @param u
     * @param typesFilter
     * @return
     */
    public List<SecuredItem> getReadableSecuredItem(User u, List<QName> typesFilter) {
        List<SecuredItem> readableSecuredItems = new ArrayList<>();

        /**
         * If a securedItem is readable, it returned by user lucene search. If
         * not, it's hidden.
         */
        String luceneRequest = "";

        if (typesFilter == null || typesFilter.isEmpty()) {
            typesFilter = new ArrayList<>();
            typesFilter.add(KoyaModel.TYPE_COMPANY);
            typesFilter.add(KoyaModel.TYPE_SPACE);
            typesFilter.add(KoyaModel.TYPE_DOSSIER);
        }
        //TODO check typesFilter given

        //build lucene request with filter types
        String orSep = "";
        for (QName t : typesFilter) {
            luceneRequest += orSep + "TYPE:\"" + KoyaModel.TYPES_SHORT_PREFIX.get(t) + "\"";
            orSep = " OR ";
        }

        logger.debug(luceneRequest);

        ResultSet rs = null;
        try {
            rs = searchService.query(
                    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                    SearchService.LANGUAGE_LUCENE, luceneRequest);
            for (ResultSetRow r : rs) {
                try {
                    readableSecuredItems.add(koyaNodeService.nodeRef2SecuredItem(r.getNodeRef()));
                } catch (KoyaServiceException ex) {

                }
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }

        return readableSecuredItems;
    }

}
