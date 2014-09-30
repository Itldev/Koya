/**
 * Koya is an alfresco module that provides a corporate orientated dataroom.
 *
 * Copyright (C) Itl Developpement 2014
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see `<http://www.gnu.org/licenses/>`.
 */
package fr.itldev.koya.alfservice.security;

import fr.itldev.koya.alfservice.CompanyService;
import fr.itldev.koya.alfservice.DossierService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.SpaceService;
import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.Permissions;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.interfaces.SubSpace;
import fr.itldev.koya.model.permissions.KoyaPermission;
import fr.itldev.koya.model.permissions.KoyaPermissionCollaborator;
import fr.itldev.koya.model.permissions.SitePermission;
import fr.itldev.koya.policies.KoyaPermissionsPolicies;
import fr.itldev.koya.policies.SharePolicies;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

/**
 * Subspaces Permissions Service
 *
 */
public class SubSpaceAclService {

    private final Logger logger = Logger.getLogger(this.getClass());

    protected PermissionService permissionService;
    protected KoyaNodeService koyaNodeService;
    protected NodeService nodeService;
    protected AuthenticationService authenticationService;
    protected SearchService searchService;
    protected AuthorityService authorityService;
    protected SiteService siteService;
    protected InvitationService invitationService;
    protected UserService userService;
    protected PolicyComponent policyComponent;
    protected CompanyService companyService;
    protected SpaceService spaceService;
    protected DossierService dossierService;
    protected CompanyAclService companyAclService;

    /*
     * Policy delegates
     */
    //Grant Permission on node delegate
    private ClassPolicyDelegate<KoyaPermissionsPolicies.AfterGrantKoyaPermissionPolicy> afterGrantKoyaPermissionDelegate;
    private ClassPolicyDelegate<KoyaPermissionsPolicies.BeforeRevokeKoyaPermissionPolicy> beforeRevokeKoyaPermissionDelegate;
    //sharing delegates
    private ClassPolicyDelegate<SharePolicies.BeforeSharePolicy> beforeShareDelegate;
    private ClassPolicyDelegate<SharePolicies.AfterSharePolicy> afterShareDelegate;
    private ClassPolicyDelegate<SharePolicies.BeforeUnsharePolicy> beforeUnshareDelegate;
    private ClassPolicyDelegate<SharePolicies.AfterUnsharePolicy> afterUnshareDelegate;

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

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setInvitationService(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setCompanyService(CompanyService companyService) {
        this.companyService = companyService;
    }

    public void setSpaceService(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    public void setDossierService(DossierService dossierService) {
        this.dossierService = dossierService;
    }

    public void setCompanyAclService(CompanyAclService companyAclService) {
        this.companyAclService = companyAclService;
    }

    //</editor-fold>
    /**
     * Registers the share policies
     */
    public void init() {
        // Register the various policies
        afterGrantKoyaPermissionDelegate = policyComponent.registerClassPolicy(KoyaPermissionsPolicies.AfterGrantKoyaPermissionPolicy.class);
        beforeRevokeKoyaPermissionDelegate = policyComponent.registerClassPolicy(KoyaPermissionsPolicies.BeforeRevokeKoyaPermissionPolicy.class);
        beforeShareDelegate = policyComponent.registerClassPolicy(SharePolicies.BeforeSharePolicy.class);
        afterShareDelegate = policyComponent.registerClassPolicy(SharePolicies.AfterSharePolicy.class);
        beforeUnshareDelegate = policyComponent.registerClassPolicy(SharePolicies.BeforeUnsharePolicy.class);
        afterUnshareDelegate = policyComponent.registerClassPolicy(SharePolicies.AfterUnsharePolicy.class);

    }

    /**
     * Set default subspaces permissions.
     *
     * @param subSpaceItem
     * @throws KoyaServiceException
     */
    public void initSubSpaceWithDefaultPermissions(SubSpace subSpaceItem) throws KoyaServiceException {

        Company c = koyaNodeService.getFirstParentOfType(subSpaceItem.getNodeRefasObject(), Company.class);
        // Clear the node inherited permissions
        permissionService.setInheritParentPermissions(subSpaceItem.getNodeRefasObject(), false);
        /*
         Setting default permissions on node       
         */
        //Sitemanager keeps manager permissions
        permissionService.setPermission(subSpaceItem.getNodeRefasObject(),
                siteService.getSiteRoleGroup(c.getName(), SitePermission.MANAGER.toString()),
                SitePermission.MANAGER.toString(), true);

        /*SiteCollaborator gets contributor permission on Space in order to create
         Sub Spaces or Dossiers.
         
         SiteCollaborator gets consumer permission on Dossier in order to read content.
            
        
         */
        if (Space.class.isAssignableFrom(subSpaceItem.getClass())) {
            permissionService.setPermission(subSpaceItem.getNodeRefasObject(),
                    siteService.getSiteRoleGroup(c.getName(), SitePermission.COLLABORATOR.toString()),
                    SitePermission.CONTRIBUTOR.toString(), true);
        } else {
            permissionService.setPermission(subSpaceItem.getNodeRefasObject(),
                    siteService.getSiteRoleGroup(c.getName(), SitePermission.COLLABORATOR.toString()),
                    SitePermission.CONSUMER.toString(), true);
        }

        //Consumer has no default permission
    }

    /**
     *
     * @param subSpace
     * @param authority
     * @param permission
     */
    public void grantSubSpacePermission(final SubSpace subSpace, final String authority, KoyaPermission permission) {
        logger.debug("Grant permission '" + permission.toString() + "' to '" + authority + "' on '" + subSpace.getName() + "'");

        permissionService.setPermission(subSpace.getNodeRefasObject(), authority, permission.toString(), true);

        afterGrantKoyaPermissionDelegate.get(
                nodeService.getType(subSpace.getNodeRefasObject()))
                .afterGrantKoyaPermission(subSpace, authority, permission);

    }

    /**
     * Revoke specified permissions on node.
     *
     * Revoke permission on node and, if parent other childs doesn't have the
     * same permission, do it for the parents
     *
     * @param subSpace
     * @param authority
     * @param permission
     *
     */
    public void revokeSubSpacePermission(SubSpace subSpace, String authority, KoyaPermission permission) {
        logger.debug("Revoke permission '" + permission.toString() + "' to '" + authority + "' on '" + subSpace.getName() + "'");
        beforeRevokeKoyaPermissionDelegate.get(
                nodeService.getType(subSpace.getNodeRefasObject()))
                .beforeRevokeKoyaPermission(subSpace, authority, permission);

        permissionService.deletePermission(subSpace.getNodeRefasObject(),
                authority, permission.toString());//>>> NPE dans les TU ???

        //TODO after hook that check if user needs to stay company member
    }

    /**
     *
     * /**
     *
     * @param subSpace
     * @param userMail
     * @param perm
     * @param serverPath
     * @param acceptUrl
     * @param rejectUrl
     * @throws KoyaServiceException
     */
    public void shareSecuredItem(SubSpace subSpace, String userMail, KoyaPermission perm,
            String serverPath, String acceptUrl, String rejectUrl) throws KoyaServiceException {

        User inviter = userService.getUserByUsername(authenticationService.getCurrentUserName());

        beforeShareDelegate.get(nodeService.getType(subSpace.getNodeRefasObject()))
                .beforeShareItem(subSpace.getNodeRefasObject(), userMail, inviter);
        Invitation invitation = shareSecuredItemImpl(subSpace, userMail, perm, serverPath, acceptUrl, rejectUrl);
        afterShareDelegate.get(nodeService.getType(subSpace.getNodeRefasObject()))
                .afterShareItem(subSpace.getNodeRefasObject(), userMail, invitation, inviter);

    }

    protected Invitation shareSecuredItemImpl(SubSpace subSpace, String userMail, KoyaPermission perm,
            String serverPath, String acceptUrl, String rejectUrl) throws KoyaServiceException {
        throw new KoyaServiceException(0);//TODO errror code - shoulnever be called
    }

    public void unShareSecuredItem(SubSpace subSpace, String userMail, KoyaPermission perm)
            throws KoyaServiceException {

        User revoker = userService.getUserByUsername(authenticationService.getCurrentUserName());
        beforeUnshareDelegate.get(nodeService.getType(subSpace.getNodeRefasObject()))
                .beforeUnshareItem(subSpace.getNodeRefasObject(), userMail, revoker);
        logger.debug("Unshare " + subSpace.getName() + " for " + userMail + " permission = " + perm.toString());

        //Gets the user involved in unsharing - throws execption if not found
        User u = userService.getUser(userMail);

        if (Dossier.class.isAssignableFrom(subSpace.getClass())) {
            revokeSubSpacePermission(subSpace, u.getUserName(), perm);
        } else {
            logger.error("Unsupported unsharing type " + subSpace.getClass().getSimpleName());
        }
        afterUnshareDelegate.get(nodeService.getType(subSpace.getNodeRefasObject()))
                .afterUnshareItem(subSpace.getNodeRefasObject(), userMail, revoker);

    }

    /**
     * Removes all given subspace koya specific permissions
     *
     * @param subSpace
     */
    public void cleanAllKoyaSubSpacePermissions(SubSpace subSpace) {

        logger.error("clean all perm on " + subSpace.getName());

        for (KoyaPermission p : KoyaPermission.getAll()) {
            for (AccessPermission ap : permissionService.getAllSetPermissions(subSpace.getNodeRefasObject())) {
                if (p.equals(ap.getPermission())) {
                    revokeSubSpacePermission(subSpace, ap.getAuthority(), p);
                }
            }
        }
    }

    public List<User> listUsers(SecuredItem s, final KoyaPermission permission) {
        return listUsers(s, new ArrayList<KoyaPermission>() {
            {
                add(permission);
            }
        });

    }

    /**
     *
     * List Users who have a specific KoyaPermission defined in filter on the
     * node.
     *
     * If no filter or empty, list all KoyaPermissions
     *
     * @param s
     * @param permissions
     * @return
     */
    public List<User> listUsers(SecuredItem s, List<KoyaPermission> permissions) {
        List<User> users = new ArrayList<>();

        if (permissions == null || permissions.isEmpty()) {
            permissions = KoyaPermission.getAll();
        }

        for (AccessPermission ap : permissionService.getAllSetPermissions(s.getNodeRefasObject())) {

            try {
                if (permissions.contains(KoyaPermission.valueOf(ap.getPermission()))) {
                    User u = userService.getUserByUsername(ap.getAuthority());
                    if (u != null) {
                        users.add(u);
                    }
                }
            } catch (IllegalArgumentException iex) {

            }
        }
        return users;
    }

    /**
     *
     * List all secured Items a user has specific KoyaPermission setted in a
     * company context.
     *
     *
     * @param c
     * @param u
     * @param permissions
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public List<SecuredItem> listSecuredItems(Company c, User u, List<KoyaPermission> permissions) throws KoyaServiceException {
        if (permissions == null || permissions.isEmpty()) {
            permissions = KoyaPermission.getAll();
        }
        return listItemSharedRecursive(spaceService.list(c.getName(), Integer.MAX_VALUE), u, permissions);
    }

    private List<SecuredItem> listItemSharedRecursive(List<Space> spaces, User u, List<KoyaPermission> permissions) throws KoyaServiceException {
        List<SecuredItem> items = new ArrayList<>();

        for (Space s : spaces) {
            items.addAll(listItemSharedRecursive(s.getChildSpaces(), u, permissions));
            //check if current space is shared with user as site consumer
            for (AccessPermission ap : permissionService.getAllSetPermissions(s.getNodeRefasObject())) {
                if (ap.getAuthority().equals(u.getUserName()) && permissions.contains(ap.getPermission())) {
                    items.add(s);
                }
            }

            //check if current space children (ie dossiers) are shared with user as site consumer
            for (Dossier d : dossierService.list(s.getNodeRefasObject())) {
                for (AccessPermission ap : permissionService.getAllSetPermissions(d.getNodeRefasObject())) {
                    if (ap.getAuthority().equals(u.getUserName()) && permissions.contains(ap.getPermission())) {
                        items.add(d);
                    }
                }
            }
        }
        return items;
    }

    /**
     * Builds Koya permissions on given NodeRef for authenticated user.
     *
     * @param n
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public Permissions getPermissions(NodeRef n) throws KoyaServiceException {
        return getPermissions(userService.getUserByUsername(authenticationService.getCurrentUserName()), n);
    }

    /**
     * Builds Koya permissions on given NodeRef for specified user.
     *
     * @param u
     * @param n
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public Permissions getPermissions(User u, NodeRef n) throws KoyaServiceException {

        Permissions p = new Permissions(u.getUserName(), n);
        /**
         * TODO get permissions method with user and node parameters search in
         * alfresco API
         *
         */
        List<String> userPermissions = new ArrayList<>();
        for (AccessPermission ap : permissionService.getAllSetPermissions(n)) {
            try {
                if (ap.getAuthority().equals(u.getUserName())) {
                    userPermissions.add(ap.getPermission());
                }
            } catch (IllegalArgumentException iex) {
            }
        }

        //
        p.canReadProperties(permissionService.hasPermission(n, PermissionService.READ_PROPERTIES).equals(AccessStatus.ALLOWED));
        p.canWriteProperties(permissionService.hasPermission(n, PermissionService.WRITE_PROPERTIES).equals(AccessStatus.ALLOWED));
        //
        p.canCreateChildren(permissionService.hasPermission(n, PermissionService.CREATE_CHILDREN).equals(AccessStatus.ALLOWED));
        p.canDeleteChildren(permissionService.hasPermission(n, PermissionService.DELETE_CHILDREN).equals(AccessStatus.ALLOWED));
        p.canReadChildren(permissionService.hasPermission(n, PermissionService.READ_CHILDREN).equals(AccessStatus.ALLOWED));
        p.canLinkChildren(permissionService.hasPermission(n, PermissionService.LINK_CHILDREN).equals(AccessStatus.ALLOWED));
        //
        p.canReadContent(permissionService.hasPermission(n, PermissionService.READ_CONTENT).equals(AccessStatus.ALLOWED));
        p.canWriteContent(permissionService.hasPermission(n, PermissionService.WRITE_CONTENT).equals(AccessStatus.ALLOWED));
        p.canExecuteContent(permissionService.hasPermission(n, PermissionService.EXECUTE_CONTENT).equals(AccessStatus.ALLOWED));
        //
        p.canDeleteNode(permissionService.hasPermission(n, PermissionService.DELETE_NODE).equals(AccessStatus.ALLOWED));
        //
        p.canDeleteAssociations(permissionService.hasPermission(n, PermissionService.DELETE_ASSOCIATIONS).equals(AccessStatus.ALLOWED));
        p.canReadAssociations(permissionService.hasPermission(n, PermissionService.READ_ASSOCIATIONS).equals(AccessStatus.ALLOWED));
        p.canCreateAssociations(permissionService.hasPermission(n, PermissionService.CREATE_ASSOCIATIONS).equals(AccessStatus.ALLOWED));
        //
        p.canReadPermissions(permissionService.hasPermission(n, PermissionService.READ_PERMISSIONS).equals(AccessStatus.ALLOWED));
        p.canChangePermissions(permissionService.hasPermission(n, PermissionService.CHANGE_PERMISSIONS).equals(AccessStatus.ALLOWED));
        /*
         ======= Koya specific permissions ========
         */
        p.canShareWithCustomer(p.getCanChangePermissions() || userPermissions.contains(KoyaPermissionCollaborator.MEMBER.toString()));

        return p;

    }

    /**
     *
     * Returns user's securedItems filtered by type that have one of
     * KoyaPermission filter item set.
     *
     * This method return application wide nodes. no matter the company.
     *
     * @param u
     * @param typesFilter
     * @param koyaPermisssionFilter
     * @return
     */
    public List<SecuredItem> getUsersSecuredItemWithKoyaPermissions(User u,
            List<QName> typesFilter, List<KoyaPermission> koyaPermisssionFilter) {

        List<SecuredItem> securedItemsWithKoyaPermissions = new ArrayList<>();

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

        List<String> koyaPermisssionFilterString = new ArrayList<>();
        if (koyaPermisssionFilter == null || koyaPermisssionFilter.isEmpty()) {
            koyaPermisssionFilterString = KoyaPermission.getAllAsString();
        } else {
            for (KoyaPermission k : koyaPermisssionFilter) {
                koyaPermisssionFilterString.add(k.toString());
            }
        }

        //build lucene request with filter types
        String orSep = "";
        for (QName t : typesFilter) {
            luceneRequest += orSep + "TYPE:\"" + KoyaModel.TYPES_SHORT_PREFIX.get(t) + "\"";
            orSep = " OR ";
        }

        ResultSet rs = null;
        try {
            rs = searchService.query(
                    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                    SearchService.LANGUAGE_LUCENE, luceneRequest);
            for (ResultSetRow r : rs) {
                try {

                    for (AccessPermission ap : permissionService.getAllSetPermissions(r.getNodeRef())) {
                        if (koyaPermisssionFilterString.contains(ap.getPermission())
                                && ap.getAuthority().equals(u.getUserName())) {
                            securedItemsWithKoyaPermissions.add(koyaNodeService.nodeRef2SecuredItem(r.getNodeRef()));
                            break;
                        }
                    }
                } catch (KoyaServiceException ex) {

                }
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }

        return securedItemsWithKoyaPermissions;
    }

}
