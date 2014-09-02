/**
 * Koya is an alfresco module that provides a corporate orientated dataroom.
 *
 * Copyright (C) Itl Developpement 2014
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see `<http://www.gnu.org/licenses/>`.
 */

package fr.itldev.koya.alfservice;

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.SharingWrapper;
import fr.itldev.koya.policies.SharePolicies.AfterSharePolicy;
import fr.itldev.koya.policies.SharePolicies.AfterUnsharePolicy;
import fr.itldev.koya.policies.SharePolicies.BeforeSharePolicy;
import fr.itldev.koya.policies.SharePolicies.BeforeUnsharePolicy;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

public class KoyaShareService extends KoyaAclService {

    private Logger logger = Logger.getLogger(KoyaShareService.class);

    private SpaceService spaceService;
    private DossierService dossierService;
    private UserService userService;
    private SiteService siteService;
    private FileFolderService fileFolderService;
    private InvitationService invitationService;
    /**
     * controls policy delegates
     */
    private PolicyComponent policyComponent;
    /*
     * Policy delegates
     */
    private ClassPolicyDelegate<BeforeSharePolicy> beforeShareDelegate;
    private ClassPolicyDelegate<AfterSharePolicy> afterShareDelegate;
    private ClassPolicyDelegate<BeforeUnsharePolicy> beforeUnshareDelegate;
    private ClassPolicyDelegate<AfterUnsharePolicy> afterUnshareDelegate;

    /**
     * Registers the share policies
     */
    public void init() {
        // Register the various policies
        beforeShareDelegate = policyComponent.registerClassPolicy(BeforeSharePolicy.class);
        afterShareDelegate = policyComponent.registerClassPolicy(AfterSharePolicy.class);
        beforeUnshareDelegate = policyComponent.registerClassPolicy(BeforeUnsharePolicy.class);
        afterUnshareDelegate = policyComponent.registerClassPolicy(AfterUnsharePolicy.class);
    }

    public void setSpaceService(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    public void setDossierService(DossierService dossierService) {
        this.dossierService = dossierService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    public void setInvitationService(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    /**
     *
     *
     * @param sharingWrapper
     * @throws KoyaServiceException
     */
    public void shareItems(SharingWrapper sharingWrapper) throws KoyaServiceException {

        List<SecuredItem> sharedItems = getSharedItems(sharingWrapper);
        String serverPath = sharingWrapper.getServerPath();
        String acceptUrl = sharingWrapper.getAcceptUrl();
        String rejectUrl = sharingWrapper.getRejectUrl();

        Assert.notNull(serverPath, "serverPath is null");
        Assert.notNull(acceptUrl, "acceptUrl is null");
        Assert.notNull(rejectUrl, "rejectUrl is null");

        //share elements to users specified by email
        for (String userMail : sharingWrapper.getSharingUsersMails()) {
            User u = null;

            try {
                u = userService.getUser(userMail);
            } catch (KoyaServiceException kex) {
                //do nothing if exception thrown
            }

            if (u == null) {
                u = createUserForSharing(sharedItems, userMail, serverPath, acceptUrl, rejectUrl);//TODO user creation process
            }

            logger.debug("share " + sharedItems.size() + " elements to existing : " + u.getEmail());

            //give permissions to user on nodes
            for (SecuredItem si : sharedItems) {
                beforeShareDelegate.get(nodeService.getType(si.getNodeRefasObject())).beforeShareItem(si.getNodeRefasObject(), u.getUserName());
                if (Dossier.class.isAssignableFrom(si.getClass())) {
                    grantDossierShare((Dossier) si, u);
                } else {
                    logger.error("Unsupported sharing type " + si.getClass().getSimpleName());
                }
                afterShareDelegate.get(nodeService.getType(si.getNodeRefasObject())).afterShareItem(si.getNodeRefasObject(), u.getUserName());
            }

        }
    }

    public void unShareItems(SharingWrapper sharingWrapper) throws KoyaServiceException {

        List<SecuredItem> sharedItems = getSharedItems(sharingWrapper);

        //share elements to users specified by email
        for (String userMail : sharingWrapper.getSharingUsersMails()) {

//            try {
                User u = userService.getUser(userMail);
                for (SecuredItem si : sharedItems) {
                    beforeUnshareDelegate.get(nodeService.getType(si.getNodeRefasObject())).beforeUnshareItem(si.getNodeRefasObject(), u.getUserName());
                    if (Dossier.class.isAssignableFrom(si.getClass())) {
                        revokeDossierShare((Dossier) si, u);
                    } else {
                        logger.error("Unsupported unsharing type " + si.getClass().getSimpleName());
                    }
                    afterUnshareDelegate.get(nodeService.getType(si.getNodeRefasObject())).afterUnshareItem(si.getNodeRefasObject(), u.getUserName());
                }
//            } catch (KoyaServiceException kex) {
//                //do nothing if exception thrown
//            }
        }

    }

    /**
     * List all secured items shared with user on the system.
     *
     *
     * @param u
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public List<SecuredItem> listItemsShared(User u) throws KoyaServiceException {
        List<SecuredItem> items = new ArrayList<>();
        for (SiteInfo si : siteService.listSites(u.getUserName())) {
            items.addAll(listItemsShared(u.getUserName(), si.getShortName()));
        }
        return items;
    }

    /**
     * List shared elements with specified user. ie user has Read permissions on
     * each element
     *
     * @param userName
     * @param companyName
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public List<SecuredItem> listItemsShared(String userName, String companyName) throws KoyaServiceException {
        return listItemSharedRecursive(userName, spaceService.list(companyName, Integer.MAX_VALUE));
    }

    private List<SecuredItem> listItemSharedRecursive(String userName, List<Space> spaces) throws KoyaServiceException {
        List<SecuredItem> items = new ArrayList<>();

        for (Space s : spaces) {
            items.addAll(listItemSharedRecursive(userName, s.getChildSpaces()));
            //check if current space is shared with user as site consumer
            for (AccessPermission ap : permissionService.getAllSetPermissions(s.getNodeRefasObject())) {
                if (ap.getAuthority().equals(userName) && ap.getPermission().equals(PERMISSION_READ)) {
                    items.add(s);
                }
            }

            //check if current space children (ie dossiers) are shared with user as site consumer
            for (Dossier d : dossierService.list(s.getNodeRefasObject())) {
                for (AccessPermission ap : permissionService.getAllSetPermissions(d.getNodeRefasObject())) {
                    if (ap.getAuthority().equals(userName) && ap.getPermission().equals(PERMISSION_READ)) {
                        items.add(d);
                    }
                }
            }
        }
        return items;
    }

    /**
     * List all users who can access specified SecuredItem.
     *
     *
     * @param s
     * @return
     */
    public List<User> listUsersAccessShare(SecuredItem s) {
        return listUsersAccessShare(s.getNodeRefasObject());
    }

    /**
     * List all users who can access specified SecuredItem.
     *
     *
     * TODO add users who belong to groups listed by getAllAuthorities.
     * currently lists only public share access
     *
     * TODO check inherance possibilities
     *
     * @param n
     * @return
     */
    public List<User> listUsersAccessShare(NodeRef n) {
        List<User> users = new ArrayList<>();
        for (AccessPermission ap : permissionService.getAllSetPermissions(n)) {
            User u = userService.getUserByUsername(ap.getAuthority());
            if (u != null) {
                users.add(u);
            }
        }
        return users;
    }

    private User createUserForSharing(List<SecuredItem> sharedItems,
            String newUserMail, String serverPath, String acceptUrl, String rejectUrl) throws KoyaServiceException {
        logger.error("create user : " + newUserMail + " and share " + sharedItems.size() + " elements");

        Set<Company> invitedTo = new HashSet<>();
        User u = null;
        for (SecuredItem si : sharedItems) {
            Company company;
            try {
                //Faster than instanceof
                company = (Company) si;
            } catch (ClassCastException cce) {
                company = koyaNodeService.getNodeCompany(si.getNodeRefasObject());
            }
            if (company != null && invitedTo.add(company)) {
                Invitation invitation = invitationService.inviteNominated(
                        null, newUserMail, newUserMail,
                        Invitation.ResourceType.WEB_SITE, company.getName(),
                        ROLE_SITE_CONSUMER, serverPath, acceptUrl, rejectUrl);
                u = userService.getUserByUsername(invitation.getInviteeUserName());
            }
        }

        return u;
    }

    private List<SecuredItem> getSharedItems(SharingWrapper sharingWrapper) {
        List<SecuredItem> sharedItems = new ArrayList<>();
        //extract shared elements
        for (String n : sharingWrapper.getSharedNodeRefs()) {
            try {
                sharedItems.add(koyaNodeService.nodeRef2SecuredItem(n));
            } catch (KoyaServiceException kex) {
                logger.error("Error creating element for sharing : " + kex.toString());
            }
        }
        return sharedItems;

    }

    /**
     *
     * ====== Dossier Specific sharing methods ===========
     *
     */
    private void grantDossierShare(Dossier dossier, User user) throws KoyaServiceException {
        grantShare(dossier, user.getUserName());
        for (SecuredItem si : koyaNodeService.getParentsList(dossier.getNodeRefasObject(), KoyaNodeService.NB_ANCESTOR_INFINTE)) {
            grantShare(si, user.getUserName());
        }
    }

    private void revokeDossierShare(Dossier dossier, User user) throws KoyaServiceException {
        revokeShare(dossier, user.getUserName());
        for (SecuredItem si : koyaNodeService.getParentsList(dossier.getNodeRefasObject(), KoyaNodeService.NB_ANCESTOR_INFINTE)) {
            if (listChildrenItemsShared(si, user.getUserName()).isEmpty()) {
                revokeShare(si, user.getUserName());
            } else {
                return;
            }
        }
    }

    /**
     * ======== Basic sharing specific methods =============
     *
     */
    /**
     *
     * @param si
     * @param userName
     */
    private void grantShare(SecuredItem si, String userName) {
        if (Company.class.isAssignableFrom(si.getClass())) {
            siteService.setMembership(si.getName(), userName, ROLE_SITE_CONSUMER);
        } else if (Space.class.isAssignableFrom(si.getClass())
                || Dossier.class.isAssignableFrom(si.getClass())) {
            permissionService.setPermission(si.getNodeRefasObject(), userName, PermissionService.READ, true);
        } else {
            //Nothing to do for other types
        }
    }

    /**
     *
     * @param si
     * @param userName
     */
    private void revokeShare(SecuredItem si, String userName) {
        if (Company.class.isAssignableFrom(si.getClass())) {
            siteService.removeMembership(si.getName(), userName);
        } else if (Space.class.isAssignableFrom(si.getClass())
                || Dossier.class.isAssignableFrom(si.getClass())) {
            permissionService.deletePermission(si.getNodeRefasObject(), userName, PermissionService.READ);
        } else {
            //Nothing to do for other types
        }
    }

    /**
     * Lists securedItem Childrens shared with user.
     *
     * Shared means user has specific read permission
     *
     * @param s
     * @param userName
     * @return
     */
    private List<SecuredItem> listChildrenItemsShared(final SecuredItem s, String userName) throws KoyaServiceException {

        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork< List<SecuredItem>>() {
            @Override
            public List<SecuredItem> doWork() throws Exception {
                List<SecuredItem> items = new ArrayList<>();
                if (Company.class.isAssignableFrom(s.getClass())) {
                    items.addAll(spaceService.list(s.getName(), 1));
                } else {
                    for (FileInfo fi : fileFolderService.list(s.getNodeRefasObject())) {
                        try {
                            items.add(koyaNodeService.nodeRef2SecuredItem(fi.getNodeRef()));
                        } catch (KoyaServiceException kex) {

                        }
                    }
                }
                return items;
            }
        }, userName);

    }

}
