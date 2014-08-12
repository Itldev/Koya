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
package fr.itldev.koya.services;

import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Notification;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;
import org.springframework.web.client.RestClientException;

public interface UserService {

    User login(String authKey, String md5password) throws RestClientException, AlfrescoServiceException;

    Boolean logout(User user);

    void createUser(User userAdmin, User toCreate);

    /**
     * Load users preference from alfresco server.
     *
     * @param user
     */
    void loadPreferences(User user);

    /**
     * Load spcified users preferences from alfresco server.
     *
     * @param userLog
     * @param userToGetPrefs
     */
    void loadPreferences(User userLog, User userToGetPrefs);

    /**
     * Write users preferences to alfresco server.
     *
     * @param user
     * @throws AlfrescoServiceException
     */
    void commitPreferences(User user) throws AlfrescoServiceException;

    /**
     * Write specified user preferences to alfresco server.
     *
     * @param userLog
     * @param userToCommitPrefs
     * @throws AlfrescoServiceException
     */
    void commitPreferences(User userLog, User userToCommitPrefs) throws AlfrescoServiceException;

    /**
     * Write users properties to alfresco server.
     *
     * @param user
     * @throws AlfrescoServiceException
     */
    void commitProperties(User user) throws AlfrescoServiceException;

    /**
     * Write specified user properties to alfresco server.
     *
     * @param userLog
     * @param userToCommitProps
     * @throws AlfrescoServiceException
     */
    void commitProperties(User userLog, User userToCommitProps) throws AlfrescoServiceException;

    /**
     *
     * @param userLog
     * @param oldPassword
     * @param newPassword
     * @throws AlfrescoServiceException
     */
    void changePassword(User userLog, String oldPassword, String newPassword) throws AlfrescoServiceException;

    /**
     * find users list wich first/last name or email starts with query. Return
     * list limitated by maxResults.
     *
     * if company is not null limit results to company scope. rolesFilter can
     * refine results in this company context (not taken in account if no
     * company is set)
     *
     * @param userLog
     * @param query
     * @param maxResults
     * @param company
     * @param rolesFilter
     * @return
     * @throws AlfrescoServiceException
     */
    List<User> find(User userLog, String query, Integer maxResults,
            Company company, List<String> rolesFilter) throws AlfrescoServiceException;

    List<Notification> getNotifications(User userLog);

    /**
     * Validate invitation giving user modifications;
     *
     * @param user
     * @param inviteId
     * @param inviteTicket
     * @throws AlfrescoServiceException
     */
    void validateInvitation(User user, String inviteId, String inviteTicket) throws AlfrescoServiceException;

    /**
     * Get user Object from email.
     *
     * @param user
     * @param email
     * @return
     * @throws AlfrescoServiceException
     */
    User getUserFromEmail(User user, String email) throws AlfrescoServiceException;

}
