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

package fr.itldev.koya.services.impl;

import fr.itldev.koya.model.impl.Preferences;
import fr.itldev.koya.model.impl.Utilisateur;
import fr.itldev.koya.model.json.AuthTicket;
import fr.itldev.koya.services.UserService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.http.client.CommonsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class UserServiceImpl extends AlfrescoRestService implements UserService, BeanFactoryAware {//cf services : http://localhost:18080/alfresco-koya-webapp/s/index/family/Authentication

    private static final String REST_GET_LOGIN = "/s/api/login.json?u={username}&pw={password}";
    private static final String REST_GET_PERSONDETAILS = "/s/api/people/{userName}?alf_ticket={alf_ticket}";
    private static final String REST_DEL_LOGOUT = "/s/api/login/ticket/{ticket}";

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
     * TODO passer le mot de passe en md5 ou autre méthode plus sécu !!
     *
     * @param login
     * @param password
     * @return
     */
    @Override
    public Utilisateur login(String login, String password) throws RestClientException {
        //appel rest ticket
        AuthTicket ticket = getTemplate().getForObject(getAlfrescoServerUrl() + REST_GET_LOGIN, AuthTicket.class, login, password);
        //appel rest infos utilisateur
        Utilisateur user = getTemplate().getForObject(getAlfrescoServerUrl() + REST_GET_PERSONDETAILS, Utilisateur.class, login, ticket.toString());
        //intégration du ticket d'authentification
        user.setTicketAlfresco(ticket.toString());
        //mise en place du template rest authentifié pour cet utilisateur
        user.setRestTemplate(getAuthenticatedRestTemplate(login, password));
        //appel rest preferences
        updatePreferences(user);
        //
        user.setPassword(password);

        return user;
    }

    @Override
    public Boolean logout(Utilisateur user) {

        getTemplate().delete(REST_DEL_LOGOUT, user.getTicketAlfresco());

        //TODO gerer les retours 
        return null;

    }

    @Override
    public void creerUtilisateur(Utilisateur userAdmin, Utilisateur userACreer) {
        //retour en exception si ca ne marche pas
    }

    /**
     * Factory d'un RestTemplate Authentifié
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
        List<HttpMessageConverter<?>> msgConverters = new ArrayList<HttpMessageConverter<?>>();
        msgConverters.add((HttpMessageConverter<?>) beanFactory.getBean("stringHttpMessageConverter"));
        msgConverters.add((HttpMessageConverter<?>) beanFactory.getBean("jsonHttpMessageConverter"));
        msgConverters.add((HttpMessageConverter<?>) beanFactory.getBean("formHttpMessageConverter"));
        userRestTemplate.setMessageConverters(msgConverters);
        userRestTemplate.setErrorHandler((ResponseErrorHandler) beanFactory.getBean("alfrescoRestErrorHandler"));

        return userRestTemplate;
    }

    /**
     * Mise à jour de préférences depuis le serveur Alfresco . Attention cela
     * écrase les modifications locales non enregistrées.
     *
     * @param user
     */
    @Override
    public void updatePreferences(Utilisateur user) {
        updatePreferences(user, user);
    }

    @Override
    public void updatePreferences(Utilisateur userLog, Utilisateur userToGetPrefs) {
        Preferences preferences = userLog.getRestTemplate().getForObject(getAlfrescoServerUrl() + REST_GET_PREFERENCES, Preferences.class, userToGetPrefs.getLogin());
        userToGetPrefs.setPreferences(preferences);
    }

    /**
     * Ecriture des préférences locales sur le serveur Alfresco.
     *
     * @param user
     * @throws
     * fr.itldev.koya.services.exceptions.AlfrescoServiceException
     */
    @Override
    public void commitPreferences(Utilisateur user) throws AlfrescoServiceException {
        commitPreferences(user, user);
    }

    @Override
    public void commitPreferences(Utilisateur userLog, Utilisateur userToCommitPrefs) throws AlfrescoServiceException {

        if (userToCommitPrefs.getPreferences() != null) {
            // 1 - envoyer les nouvelles clés et celles modifiées 
            userLog.getRestTemplate().postForObject(getAlfrescoServerUrl() + REST_POST_PREFERENCES, userToCommitPrefs.getPreferences(), Preferences.class, userToCommitPrefs.getLogin());

            // 2 - update des prefs depuis le serveur
            Preferences prefsToCommit = userToCommitPrefs.getPreferences();
            updatePreferences(userLog, userToCommitPrefs);

            // 3 - s'il y a moins de prefs a commiter que de prefs updatées --> supressions a effectuer
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
                updatePreferences(userLog, userToCommitPrefs);
            }

        } else {
            throw new AlfrescoServiceException("Aucune préférence à mettre à jour pour cet utilisateur");
        }

    }
    
    
    //TODO création / deactivation et gestion de compte 
}
