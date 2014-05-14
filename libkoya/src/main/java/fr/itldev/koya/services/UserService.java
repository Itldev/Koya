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

import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.Notification;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;
import org.springframework.web.client.RestClientException;

public interface UserService {

    User login(String login, String md5password) throws RestClientException;

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
     * Share specified Secured item to specified mail user. If user doesn't
     * exists, alfresco backend creates it.
     *
     * @param userLog
     * @param sharedItem
     * @param userMail
     * @throws AlfrescoServiceException
     */
    void grantAccessSecuredItem(User userLog, SecuredItem sharedItem, String userMail) throws AlfrescoServiceException;

    /**
     * Share or Unshare specified Secured item to specified mail user. If user
     * doesn't exists, alfresco backend creates it.
     *
     * @param userLog
     * @param sharedItem
     * @param userMail
     * @param revoke
     * @throws AlfrescoServiceException
     */
    void grantAccessSecuredItem(User userLog, SecuredItem sharedItem, String userMail, Boolean revoke) throws AlfrescoServiceException;

    /**
     * find users list wich first/last name or email starts with query. Return
     * list limitated by maxResults.
     *
     * @param userLog
     * @param query
     * @param maxResults
     * @return
     * @throws AlfrescoServiceException
     */
    List<User> find(User userLog, String query, Integer maxResults) throws AlfrescoServiceException;

    List<Notification> getNotifications(User userLog);

}
