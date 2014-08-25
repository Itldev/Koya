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
package fr.itldev.koya.alfservice;

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.impl.UserConnection;
import fr.itldev.koya.model.json.InviteWrapper;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.PropertyMap;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

/**
 *
 */
public class UserService {

    private final Logger logger = Logger.getLogger(this.getClass());

    protected NodeService nodeService;
    protected PersonService personService;
    protected SearchService searchService;
    protected MutableAuthenticationService authenticationService;
    protected SiteService siteService;
    protected InvitationService invitationService;
    protected KoyaNodeService koyaNodeService;
    protected ActionService actionService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setAuthenticationService(MutableAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setInvitationService(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    /**
     * User creation method.
     *
     * @param userToCreate
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public void createUser(User userToCreate) throws KoyaServiceException {

        PropertyMap propsUser = new PropertyMap();
        propsUser.put(ContentModel.PROP_USERNAME, userToCreate.getUserName());
        propsUser.put(ContentModel.PROP_FIRSTNAME, userToCreate.getFirstName());
        propsUser.put(ContentModel.PROP_LASTNAME, userToCreate.getName());
        propsUser.put(ContentModel.PROP_EMAIL, userToCreate.getEmail());
        if (!personService.personExists(userToCreate.getUserName())) {
            authenticationService.createAuthentication(userToCreate.getUserName(), userToCreate.getPassword().toCharArray());
            personService.createPerson(propsUser);
        } else {
            throw new KoyaServiceException(KoyaErrorCodes.LOGIN_ALREADY_EXISTS);
        }
    }

    /**
     * Modify user fields according to user object.
     *
     * @param userToModify
     * @throws KoyaServiceException
     */
    public void modifyUser(User userToModify) throws KoyaServiceException {

        //TODO check who request user modification : user can only modify his own information
        //admin can modify everyone informations
        if (personService.personExists(userToModify.getUserName())) {
            NodeRef userNr = personService.getPerson(userToModify.getUserName());

            //update 4 fields : firstname,lastname,email,password            
            nodeService.setProperty(userNr, ContentModel.PROP_FIRSTNAME, userToModify.getFirstName());
            nodeService.setProperty(userNr, ContentModel.PROP_LASTNAME, userToModify.getName());
            nodeService.setProperty(userNr, ContentModel.PROP_EMAIL, userToModify.getEmail());

            //TODO change password if necessary + uncrypted password
            //nodeService.setProperty(userNr, ContentModel.PROP_PASSWORD, userToModify.getPassword());
        } else {
            throw new KoyaServiceException(KoyaErrorCodes.UNKNOWN_USER);
        }

    }

    /**
     * Change users password
     *
     * @param oldPassword
     * @param newPassword
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public void changePassword(String oldPassword, String newPassword) throws KoyaServiceException {
        try {
            authenticationService.updateAuthentication(authenticationService.getCurrentUserName(), oldPassword.toCharArray(), newPassword.toCharArray());
        } catch (AuthenticationException aex) {
            throw new KoyaServiceException(KoyaErrorCodes.CANT_MODIFY_USER_PASSWORD);
        }
    }

    /**
     * return users list that matches query . email, lastname or first name
     * starts with query String.
     *
     *
     *
     * @param queryStartsWith
     * @param maxResults - 0 = no limit
     * @param companyName
     * @param companyRolesFilter
     * @return
     */
    public List<User> find(String queryStartsWith, int maxResults,
            String companyName, List<String> companyRolesFilter) {
        List<User> users = new ArrayList<>();

        if (queryStartsWith == null) {
            queryStartsWith = "";
        }
        queryStartsWith = queryStartsWith.toLowerCase();

        //application global search
        if (companyName == null || companyName.isEmpty()) {

            String luceneRequest = "TYPE:\"cm:person\" AND (@cm\\:lastName:\""
                    + queryStartsWith + "*\" OR @cm\\:firstName:\""
                    + queryStartsWith + "*\" OR @cm\\:email:\""
                    + queryStartsWith + "*\" )";

            logger.trace(luceneRequest);
            ResultSet rs = null;
            try {
                rs = searchService.query(
                        StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                        SearchService.LANGUAGE_LUCENE, luceneRequest);
                for (ResultSetRow r : rs) {
                    users.add(buildUser(r.getNodeRef()));
                    if (users.size() >= maxResults) {
                        break;
                    }
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
        } else {

            //TODO apply user filter
            Map<String, String> companyMembers = new HashMap<>();
            if (companyRolesFilter != null && companyRolesFilter.size() > 0) {
                for (String role : companyRolesFilter) {
                    companyMembers.putAll(siteService.listMembers(
                            companyName, null, role, 0, true));
                }
            } else {
                companyMembers.putAll(siteService.listMembers(
                        companyName, null, null, 0, true));
            }

            for (String userName : companyMembers.keySet()) {
                //remove from results where query is not name|firstname|email substring
                //---> prevent display changed mail adress (username not changed )
                User u = buildUser(personService.getPerson(userName));
                if (u.getName().toLowerCase().startsWith(queryStartsWith)
                        || u.getFirstName().toLowerCase().startsWith(queryStartsWith)
                        || u.getEmail().toLowerCase().startsWith(queryStartsWith)) {
                    users.add(buildUser(personService.getPerson(userName)));
                }

                if (users.size() >= maxResults) {
                    break;
                }
            }

        }

        logger.trace(users.size() + " results found");

        return users;
    }

    public User buildUser(NodeRef userNodeRef) {
        User u = new User();

        //TODO complete build with all properties
        u.setUserName((String) nodeService.getProperty(userNodeRef, ContentModel.PROP_USERNAME));
        u.setFirstName((String) nodeService.getProperty(userNodeRef, ContentModel.PROP_FIRSTNAME));
        u.setName((String) nodeService.getProperty(userNodeRef, ContentModel.PROP_LASTNAME));
        u.setEmail((String) nodeService.getProperty(userNodeRef, ContentModel.PROP_EMAIL));
        u.setNodeRef(userNodeRef.toString());

        return u;
    }

    public User getUserByUsername(final String username) {
        if (personService.personExists(username)) {
            return buildUser(personService.getPerson(username));
        } else {
            return null;
        }
    }

    /**
     * Get User by authenticationKey that could mail address or username.
     *
     * @param authKey
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public User getUser(final String authKey) throws KoyaServiceException {

        if (personService.personExists(authKey)) {
            return buildUser(personService.getPerson(authKey));
        } else {
            String luceneRequest = "TYPE:\"cm:person\" AND @cm\\:email:\"" + authKey + "\" ";
            List<User> users = new ArrayList<>();
            ResultSet rs = null;
            try {
                rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_LUCENE, luceneRequest);
                for (ResultSetRow r : rs) {
                    users.add(buildUser(r.getNodeRef()));
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            if (users.isEmpty()) {
                throw new KoyaServiceException(KoyaErrorCodes.NO_SUCH_USER_IDENTIFIED_BY_AUTHKEY, authKey);
            } else if (users.size() > 1) {
                throw new KoyaServiceException(KoyaErrorCodes.MANY_USERS_IDENTIFIED_BY_AUTHKEY, authKey);
            } else {
                return users.get(0);
            }
        }
    }

    /**
     * Invite user identified by username to company with defined roleName
     *
     *
     * @param inviteWrapper
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public void invite(InviteWrapper inviteWrapper) throws KoyaServiceException {

        String userMail = inviteWrapper.getEmail();
        String companyName = inviteWrapper.getCompanyName();
        String roleName = inviteWrapper.getRoleName();
        String serverPath = inviteWrapper.getServerPath();
        String acceptUrl = inviteWrapper.getAcceptUrl();
        String rejectUrl = inviteWrapper.getRejectUrl();

        Assert.notNull(serverPath, "serverPath is null");
        Assert.notNull(acceptUrl, "acceptUrl is null");
        Assert.notNull(rejectUrl, "rejectUrl is null");

        Company c = koyaNodeService.companyBuilder(companyName);

        User u = null;
        try {
            u = getUser(userMail);
        } catch (KoyaServiceException kex) {
            //do nothing if exception thrown
        }

        if (u == null) {
            invitationService.inviteNominated(null, userMail, userMail,
                    Invitation.ResourceType.WEB_SITE, c.getName(),
                    roleName, serverPath, acceptUrl, rejectUrl);

        } else {

            //custom invitation process --> TODO replace with workflow procedure
            // 1 - Attribute role to existing user 
            //TODO check previously existing role in site
            siteService.setMembership(c.getName(), u.getUserName(), roleName);
            // 2 - Send  template mail.
        }
    }

    /**
     * Revoke user access to defined company.
     *
     * @param userName
     * @param companyName
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public void revoke(String userName, String companyName) throws KoyaServiceException {
        siteService.removeMembership(companyName, userName);

        //Launch backend action that cleans all users specific permissions in company
        //Only delete specific permissions on dossiers 
        try {
            Map<String, Serializable> paramsClean = new HashMap<>();
            paramsClean.put("userName", userName);
            Action cleanUserAuth = actionService.createAction("cleanPermissions", paramsClean);
            cleanUserAuth.setExecuteAsynchronously(true);
            actionService.executeAction(cleanUserAuth, siteService.getSite(companyName).getNodeRef());
        } catch (InvalidNodeRefException ex) {
            throw new KoyaServiceException(0, "");//TODO 
        }

    }

    public List<UserConnection> getConnectionLog(String userName, List<String> companyFilter, Integer maxResults) {
        List<UserConnection> connectionLog = new ArrayList<>();
        //TODO build full connection log
        return connectionLog;
    }

}
