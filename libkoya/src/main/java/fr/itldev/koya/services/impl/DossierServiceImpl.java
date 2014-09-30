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

import fr.itldev.koya.model.impl.Document;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.DossierService;
import fr.itldev.koya.services.KoyaContentService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import static fr.itldev.koya.services.impl.AlfrescoRestService.fromJSON;
import java.util.List;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.core.io.Resource;

public class DossierServiceImpl extends AlfrescoRestService implements DossierService {

    private static final String REST_GET_CREATEDOSSIER = "/s/fr/itldev/koya/dossier/create/{parentNodeRef}?title={title}";
    private static final String REST_POST_LISTCHILD = "/s/fr/itldev/koya/dossier/list?filter={filter}";
    private static final String REST_GET_LISTRESP = "/s/fr/itldev/koya/dossier/resp/list/{nodeRef}";

    private static final String REST_GET_LISTMEMBERS = "/s/fr/itldev/koya/dossier/member/list/{nodeRef}";

    private static final String REST_GET_ADDRESP = "/s/fr/itldev/koya/dossier/resp/add/{userName}/{nodeRef}";
    private static final String REST_GET_ADDMEMBER = "/s/fr/itldev/koya/dossier/member/add/{userName}/{nodeRef}";

    private static final String REST_GET_DELRESP = "/s/fr/itldev/koya/dossier/resp/del/{userName}/{nodeRef}";

    private KoyaContentService KoyaContentService;

    public void setKoyaContentService(KoyaContentService KoyaContentService) {
        this.KoyaContentService = KoyaContentService;
    }

    @Override
    public Dossier create(User user, Space parentSpace, String title) throws AlfrescoServiceException {
        return user.getRestTemplate().getForObject(getAlfrescoServerUrl()
                + REST_GET_CREATEDOSSIER, Dossier.class, parentSpace.getNodeRef(), title);
    }

    /**
     * Creates a new Dossier with content in a zip file
     *
     * TODO make this process atomic
     *
     * @param user
     * @param parentSpace
     * @param zipFile
     *
     *
     * @return
     * @throws AlfrescoServiceException
     */
    @Override
    public Dossier create(User user, Space parentSpace, String title, Resource zipFile) throws AlfrescoServiceException {
        Dossier d = create(user, parentSpace, title);
        Document zipDoc = KoyaContentService.upload(user, d.getNodeRefasObject(), zipFile);
        KoyaContentService.importZipedContent(user, zipDoc);
        return d;
    }

    /**
     *
     * @param user
     * @param dossier
     * @return
     * @throws AlfrescoServiceException
     */
    @Override
    public Dossier edit(User user, Dossier dossier) throws AlfrescoServiceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * List all Space Dossiers
     *
     * @param user
     * @param space
     * @throws AlfrescoServiceException
     */
    @Override
    public List<Dossier> list(User user, Space space, String... filter) throws AlfrescoServiceException {
        String filterStr = "";
        if (filter.length == 1) {
            filterStr = filter[0];
        }

        return fromJSON(new TypeReference<List<Dossier>>() {
        }, user.getRestTemplate().postForObject(
                getAlfrescoServerUrl() + REST_POST_LISTCHILD, space, String.class, filterStr));

    }

    /**
     * List all users in charge of specified Dossier.
     *
     * @param user
     * @param dossier
     * @return
     * @throws AlfrescoServiceException
     */
    @Override
    public List<User> listResponsibles(User user, Dossier dossier) throws AlfrescoServiceException {
        return fromJSON(new TypeReference<List<User>>() {
        }, user.getRestTemplate().
                getForObject(getAlfrescoServerUrl()
                        + REST_GET_LISTRESP, String.class, dossier.getNodeRef()));
    }

    /**
     * List all users in charge of specified Dossier.
     *
     * @param user
     * @param dossier
     * @return
     * @throws AlfrescoServiceException
     */
    @Override
    public List<User> listMembers(User user, Dossier dossier) throws AlfrescoServiceException {
        return fromJSON(new TypeReference<List<User>>() {
        }, user.getRestTemplate().
                getForObject(getAlfrescoServerUrl()
                        + REST_GET_LISTMEMBERS, String.class, dossier.getNodeRef()));
    }

    /**
     * Adds a user in charge of specified Dossier.
     *
     * @param user
     * @param dossier
     * @param responsible
     * @throws AlfrescoServiceException
     */
    @Override
    public void addResponsible(User user, Dossier dossier, User responsible) throws AlfrescoServiceException {
        user.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_ADDRESP, String.class,
                responsible.getUserName(), dossier.getNodeRef());
    }

    @Override
    public void addMember(User user, Dossier dossier, User responsible) throws AlfrescoServiceException {
        user.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_ADDMEMBER, String.class,
                responsible.getUserName(), dossier.getNodeRef());
    }

    /**
     * Add a list of users in charge of specified Dossier.
     *
     * @param user
     * @param dossier
     * @param responsibles
     * @throws AlfrescoServiceException
     */
    @Override
    public void addResponsible(User user, Dossier dossier, List<User> responsibles) throws AlfrescoServiceException {
        for (User u : responsibles) {
            addResponsible(user, dossier, u);
        }
    }

    /**
     * Removes any collaborator role set on dossier.
     *
     * @param user
     * @param dossier
     * @param collaborator
     * @throws AlfrescoServiceException
     */
    @Override
    public void removeKoyaCollaboratorRole(User user, Dossier dossier, User collaborator) throws AlfrescoServiceException {
        user.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_DELRESP,
                String.class, dossier.getNodeRef(), collaborator.getUserName()
        );

    }

    /**
     * Remove user member or responsible of specified Dossier.
     *
     * @param user
     * @param dossier
     * @param memberOrResp
     * @throws AlfrescoServiceException
     */
    @Override
    public void delMemberOrResponsible(User user, Dossier dossier, User memberOrResp) throws AlfrescoServiceException {
        user.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_DELRESP,
                String.class, memberOrResp.getUserName(), dossier.getNodeRef());
    }

}
