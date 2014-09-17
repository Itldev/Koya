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

import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.model.permissions.SitePermission;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.impl.UserRole;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteDoesNotExistException;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

public class CompanyAclService {

    private Logger logger = Logger.getLogger(this.getClass());

    protected SiteService siteService;
    protected UserService userService;
    protected InvitationService invitationService;
    protected AuthorityService authorityService;
    protected AuthenticationService authenticationService;
    protected ActionService actionService;

    // <editor-fold defaultstate="collapsed" desc="getters/setters">
    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setInvitationService(InvitationService invitationService) {
        this.invitationService = invitationService;
    }
    //</editor-fold>

    //TODO refine by userTypes : Collaborators Roles, Client Roles
    public List<UserRole> getAvailableRoles(Company c) throws KoyaServiceException {
        try {
            List<UserRole> userRoles = new ArrayList<>();
            for (String r : SitePermission.getAllAsString()) {
                userRoles.add(new UserRole(r));
            }
            return userRoles;
        } catch (SiteDoesNotExistException ex) {
            throw new KoyaServiceException(KoyaErrorCodes.COMPANY_SITE_NOT_FOUND);
        }
    }

    /**
     * List both validated users and pending invitation users.
     *
     * @param companyName
     * @param permissionsFilter
     * @return
     */
    public List<User> listMembers(String companyName, List<SitePermission> permissionsFilter) {
        List<User> members = new ArrayList<>();
        members.addAll(listMembersValidated(companyName, permissionsFilter));
        members.addAll(listMembersPendingInvitation(companyName, permissionsFilter));
        return members;
    }

    /**
     * List members of the company already validated.
     *
     * Automaticly excludes Alfresco administrators.
     *
     * @param companyName
     * @param permissionsFilter
     * @return
     */
    public List<User> listMembersValidated(String companyName, List<SitePermission> permissionsFilter) {

        List<User> usersOfCompanyValidated = new ArrayList<>();
        if (permissionsFilter == null || permissionsFilter.isEmpty()) {
            permissionsFilter = SitePermission.getAll();
        }

        for (SitePermission sp : permissionsFilter) {

            Map<String, String> members = siteService.listMembers(companyName, null,
                    sp.toString(), 0);
            for (String userName : members.keySet()) {
                if (!authorityService.isAdminAuthority(userName)
                        && invitationService.listPendingInvitationsForInvitee(userName).isEmpty()) {
                    usersOfCompanyValidated.add(userService.getUserByUsername(userName));
                }
            }

        }

        return usersOfCompanyValidated;

    }

    /**
     * List Members of the company with pending invitation.
     *
     * @param companyName
     * @param permissionsFilter
     * @return
     */
    public List<User> listMembersPendingInvitation(String companyName, List<SitePermission> permissionsFilter) {
        List<User> usersOfCompanyPendingInvitation = new ArrayList<>();
        if (permissionsFilter == null || permissionsFilter.isEmpty()) {
            permissionsFilter = SitePermission.getAll();
        }

        for (SitePermission sp : permissionsFilter) {
            Map<String, String> members = siteService.listMembers(companyName, null,
                    sp.toString(), 0);
            for (String userName : members.keySet()) {
                if (!authorityService.isAdminAuthority(userName)
                        && !invitationService.listPendingInvitationsForInvitee(userName).isEmpty()) {
                    usersOfCompanyPendingInvitation.add(userService.getUserByUsername(userName));
                }
            }
        }

        for (Invitation i : invitationService.listPendingInvitationsForResource(
                Invitation.ResourceType.WEB_SITE, companyName)) {
            User u = userService.getUserByUsername(i.getInviteeUserName());
            for (SitePermission sp : permissionsFilter) {
                if (sp.equals(i.getRoleName())
                        && !usersOfCompanyPendingInvitation.contains(u)) {
                    usersOfCompanyPendingInvitation.add(u);
                }
            }
        }

        return usersOfCompanyPendingInvitation;
    }

    /**
     * Checks if current logged user is company manager on specified company.
     *
     * @param companyName
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public Boolean isCompanyManager(String companyName) throws KoyaServiceException {
        try {
            return SitePermission.MANAGER.equals(
                    siteService.getMembersRole(companyName, authenticationService.getCurrentUserName()));
        } catch (SiteDoesNotExistException ex) {
            throw new KoyaServiceException(KoyaErrorCodes.COMPANY_SITE_NOT_FOUND);
        }
    }

    public Boolean hasPendingInvitation(Company c, User u) throws KoyaServiceException {
        return listMembersPendingInvitation(c.getName(), null).contains(u);
    }

    public SitePermission getSitePermission(Company c, String userMail) {
        User u = userService.getUserByEmailFailOver(userMail);
        if (u != null) {
            return getSitePermission(c, u);
        }
        return null;
    }

    /**
     * Returns SitePermission of user on Company if exists.
     *
     * @param c
     * @param u
     * @return
     */
    public SitePermission getSitePermission(Company c, User u) {
        //try with validated members 
        String roleOfSite = siteService.getMembersRole(c.getName(), u.getUserName());
        if (roleOfSite != null) {
            return SitePermission.valueOf(roleOfSite);
        }
        //if not found, try with pending invitation users        
        for (Invitation i : invitationService.listPendingInvitationsForResource(
                Invitation.ResourceType.WEB_SITE, c.getName())) {
            if (i.getInviteeUserName().equals(u.getUserName())) {
                return SitePermission.valueOf(i.getRoleName());
            }
        }
        //if no reults return null;
        return null;
    }

    /**
     * Invite user to company with defined roleName.
     *
     * Returns invitation if processed
     *
     *
     * @param c
     * @param userMail
     * @param permission
     * @param serverPath
     * @param acceptUrl
     * @param rejectUrl
     * @return
     * @throws KoyaServiceException
     */
    public Invitation inviteMember(final Company c, final String userMail, final SitePermission permission,
            final String serverPath, final String acceptUrl, final String rejectUrl) throws KoyaServiceException {

        Assert.notNull(serverPath, "serverPath is null");
        Assert.notNull(acceptUrl, "acceptUrl is null");
        Assert.notNull(rejectUrl, "rejectUrl is null");

        User u = userService.getUserByEmailFailOver(userMail);
        if (u == null) {

//            return invitationService.inviteNominated(null, userMail, userMail,
//                    Invitation.ResourceType.WEB_SITE, c.getName(),
//                    permission.toString(), serverPath, acceptUrl, rejectUrl);
            /**
             * Workaround to resolve invite by user bug :
             *
             *
             *
             * https://forums.alfresco.com/forum/installation-upgrades-configuration-integration/configuration/site-invite-failures
             * https://issues.alfresco.com/jira/browse/ALF-20897
             * http://forums.alfresco.com/forum/installation-upgrades-configuration-integration/configuration/problem-invite-external-users
             *
             *
             */
            return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Invitation>() {
                @Override
                public Invitation doWork() throws Exception {
                    return invitationService.inviteNominated(null, userMail, userMail,
                            Invitation.ResourceType.WEB_SITE, c.getName(),
                            permission.toString(), serverPath, acceptUrl, rejectUrl);
                }
            }, "admin");

        } else {
            SitePermission sitePermission = getSitePermission(c, u);

            if (sitePermission == null) {
                //no setted permission -> do it
                siteService.setMembership(c.getName(), u.getUserName(), permission.toString());
            } else {
                //already site member ...
            }
        }

        return null;
    }

    /**
     * Set user identified by userName specified userRole in companyName
     * context.
     *
     * User MUST already be a member of the company
     *
     * @param c
     * @param u
     * @param role
     * @throws KoyaServiceException
     */
    public void setRole(Company c, User u, SitePermission role) throws KoyaServiceException {

        //checks if user is already a company member
        SitePermission roleSite = getSitePermission(c, u);

        if (roleSite == null) {
            throw new KoyaServiceException(KoyaErrorCodes.SECU_USER_MUSTBE_COMPANY_MEMBER_TO_CHANGE_COMPANYROLE);
        }
        /**
         * TODO check if pending invitation exists
         */

        try {
            siteService.setMembership(c.getName(), u.getUserName(), role.toString());
        } catch (SiteDoesNotExistException ex) {
            throw new KoyaServiceException(KoyaErrorCodes.COMPANY_SITE_NOT_FOUND);
        }

    }

    /**
     * Revoke user access to defined company.
     *
     * @param c
     * @param u
     * @throws KoyaServiceException
     */
    public void removeFromMembers(Company c, User u) throws KoyaServiceException {
        siteService.removeMembership(c.getName(), u.getUserName());

        //Launch backend action that cleans all users specific permissions in company
        //Only delete specific permissions on dossiers 
        try {
            Map<String, Serializable> paramsClean = new HashMap<>();
            paramsClean.put("userName", u.getUserName());
            Action cleanUserAuth = actionService.createAction("cleanPermissions", paramsClean);
            cleanUserAuth.setExecuteAsynchronously(true);
            actionService.executeAction(cleanUserAuth, siteService.getSite(c.getName()).getNodeRef());
        } catch (InvalidNodeRefException ex) {
            throw new KoyaServiceException(0, "");//TODO
        }

    }

}