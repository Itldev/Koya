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
package fr.itldev.koya.services.impl;

import fr.itldev.koya.model.impl.Notification;
import fr.itldev.koya.model.impl.Preferences;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.AuthTicket;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.services.UserService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.http.client.CommonsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class UserServiceImpl extends AlfrescoRestService implements UserService, BeanFactoryAware {

    private Logger logger = Logger.getLogger(this.getClass());

    private static final String REST_GET_LOGIN = "/s/api/login.json?u={username}&pw={password}";
    private static final String REST_POST_PERSONFROMMAIL = "/s/fr/itldev/koya/user/getbyauthkey?alf_ticket={alf_ticket}";
    private static final String REST_DEL_LOGOUT = "/s/api/login/ticket/{ticket}";
    private static final String REST_POST_MODIFYDETAILS = "/s/fr/itldev/koya/user/modifydetails";
    private static final String REST_GET_FINDUSERS = "/s/fr/itldev/koya/user/find/{query}/{maxresults}";
    private static final String REST_GET_CHANGEPASSWORD = "/s/fr/itldev/koya/user/changepassword/{oldpwd}/{newpwd}";

    //===== Preferences
    private static final String REST_GET_PREFERENCES = "/s/api/people/{userid}/preferences";
    private static final String REST_POST_PREFERENCES = "/s/api/people/{userid}/preferences";
    private static final String REST_DELETE_PREFERENCES = "/s/api/people/{userid}/preferences?pf={preferencefilter?}";

    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory bf) throws BeansException {
        this.beanFactory = bf;
    }

    /**
     * Authenticates user with authentication key that could be his login or
     * email.
     *
     *
     * TODO give md5 or other secured password instead of clear.
     *
     * @param authKey
     * @param password
     * @return
     * @throws fr.itldev.koya.services.exceptions.AlfrescoServiceException
     */
    @Override
    public User login(String authKey, String password) throws RestClientException, AlfrescoServiceException {

        //call rest ticket
        AuthTicket ticket = getTemplate().getForObject(getAlfrescoServerUrl() + REST_GET_LOGIN, AuthTicket.class, authKey, password);
        //Get User Object
        Map emailPostWrapper = new HashMap();
        emailPostWrapper.put("authKey", authKey);
        User user = null;
        ItlAlfrescoServiceWrapper ret = getTemplate().postForObject(
                getAlfrescoServerUrl() + REST_POST_PERSONFROMMAIL, emailPostWrapper, ItlAlfrescoServiceWrapper.class, ticket);

        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK) && ret.getNbitems() == 1) {
            user = (User) ret.getItems().get(0);
        } else {
            throw new AlfrescoServiceException(ret.getMessage());
        }
        //Authentication ticket integration
        user.setTicketAlfresco(ticket.toString());
        //set users authenticated rest template
        user.setRestTemplate(getAuthenticatedRestTemplate(user.getLogin(), password));
        //load users rest prefrences
        loadPreferences(user);
        //
        user.setPassword(password);
        return user;
    }

    @Override
    public Boolean logout(User user) {

        getTemplate().delete(REST_DEL_LOGOUT, user.getTicketAlfresco());

        //TODO treat returns
        return null;

    }

    @Override
    public void createUser(User userAdmin, User toCreate) {
        //exception if doesn't work
    }

    /**
     * Authenticated RestTemplate Factory.
     *
     * @return
     */
    private RestTemplate getAuthenticatedRestTemplate(String login, String password) {

        HttpState httpState = new HttpState();
        httpState.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(login, password));

        HttpClient httpClient = new HttpClient();
        httpClient.setState(httpState);

        CommonsClientHttpRequestFactory httpClientFactory = new CommonsClientHttpRequestFactory(httpClient);

        RestTemplate userRestTemplate = new RestTemplate(httpClientFactory);
        List<HttpMessageConverter<?>> msgConverters = new ArrayList<>();
        msgConverters.add((HttpMessageConverter<?>) beanFactory.getBean("stringHttpMessageConverter"));
        msgConverters.add((HttpMessageConverter<?>) beanFactory.getBean("jsonHttpMessageConverter"));
        msgConverters.add((HttpMessageConverter<?>) beanFactory.getBean("formHttpMessageConverter"));
        userRestTemplate.setMessageConverters(msgConverters);
        userRestTemplate.setErrorHandler((ResponseErrorHandler) beanFactory.getBean("alfrescoRestErrorHandler"));

        return userRestTemplate;
    }

    /**
     * Updates users preferences from alfresco server. Erases unsaved local
     * prefrences.
     *
     * @param user
     */
    @Override
    public void loadPreferences(User user) {
        loadPreferences(user, user);
    }

    @Override
    public void loadPreferences(User userLog, User userToGetPrefs) {
        Preferences preferences = userLog.getRestTemplate().getForObject(getAlfrescoServerUrl() + REST_GET_PREFERENCES, Preferences.class, userToGetPrefs.getLogin());
        userToGetPrefs.setPreferences(preferences);
    }

    /**
     * Writes local preferences to alfresco server.
     *
     * @param user
     * @throws fr.itldev.koya.services.exceptions.AlfrescoServiceException
     */
    @Override
    public void commitPreferences(User user) throws AlfrescoServiceException {
        commitPreferences(user, user);
    }

    @Override
    public void commitPreferences(User userLog, User userToCommitPrefs) throws AlfrescoServiceException {

        if (userToCommitPrefs.getPreferences() != null) {
            // 1 - send new and modified keys
            userLog.getRestTemplate().postForObject(getAlfrescoServerUrl() + REST_POST_PREFERENCES, userToCommitPrefs.getPreferences(), Preferences.class, userToCommitPrefs.getLogin());

            // 2 - updates preferences from server
            Preferences prefsToCommit = userToCommitPrefs.getPreferences();
            loadPreferences(userLog, userToCommitPrefs);

            // 3 - if less preferences to commit than updates --> some keys have to be deleted.
            if (prefsToCommit.size() < userToCommitPrefs.getPreferences().size()) {

                String deleteFilter = "";
                String sep = "";
                for (String k : userToCommitPrefs.getPreferences().keySet()) {
                    if (!prefsToCommit.keySet().contains(k)) {
                        deleteFilter += sep + k;
                        sep = ",";
                    }
                }
                userLog.getRestTemplate().delete(getAlfrescoServerUrl() + REST_DELETE_PREFERENCES, userToCommitPrefs.getLogin(), deleteFilter);
                loadPreferences(userLog, userToCommitPrefs);
            }

        } else {
            throw new AlfrescoServiceException("Aucune préférence à mettre à jour pour cet utilisateur");
        }

    }

    @Override
    public void commitProperties(User user) throws AlfrescoServiceException {
        commitProperties(user, user);
    }

    @Override
    public void commitProperties(User userLog, User userToCommitProps) throws AlfrescoServiceException {
        ItlAlfrescoServiceWrapper ret = userLog.getRestTemplate().postForObject(getAlfrescoServerUrl() + REST_POST_MODIFYDETAILS, userToCommitProps, ItlAlfrescoServiceWrapper.class);
        if (!ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            throw new AlfrescoServiceException(ret.getMessage());
        }
    }

    @Override
    public void changePassword(User userLog, String oldPassword, String newPassword) throws AlfrescoServiceException {
        ItlAlfrescoServiceWrapper ret = userLog.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_CHANGEPASSWORD, ItlAlfrescoServiceWrapper.class, oldPassword, newPassword);
        if (!ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            throw new AlfrescoServiceException(ret.getMessage());
        }
    }

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
    @Override
    public List<User> find(User userLog, String query, Integer maxResults) throws AlfrescoServiceException {
        ItlAlfrescoServiceWrapper ret
                = userLog.getRestTemplate().getForObject(getAlfrescoServerUrl() + REST_GET_FINDUSERS, ItlAlfrescoServiceWrapper.class, query, maxResults);
        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            return (List<User>) ret.getItems();
        } else {
            throw new AlfrescoServiceException(ret.getMessage());
        }
    }

    @Override
    public List<Notification> getNotifications(User userLog) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        //cf alfresco activity service
    }

}
