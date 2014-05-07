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
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.log4j.Logger;

/**
 *
 */
public class UserService {

    private final Logger logger = Logger.getLogger(this.getClass());

    protected NodeService nodeService;
    private PersonService personService;
    protected SearchService searchService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * User creation method.
     *
     * @param userLog
     * @param mail
     */
    public void createUser(User userLog, String mail) {

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
        if (personService.personExists(userToModify.getLogin())) {
            NodeRef userNr = personService.getPerson(userToModify.getLogin());

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
     * return users list that matches query . email, lastname or first name
     * starts with query String.
     *
     *
     * TODO limit results to users company users (ie one can not get all
     * alfresco users mails way)
     *
     * @param query
     * @param maxResults - 0 = no limit
     * @return
     */
    public List<User> find(String query, int maxResults) {

        String luceneRequest = "TYPE:\"cm:person\" AND (@cm\\:lastName:\"" + query + "*\" OR @cm\\:firstName:\"" + query + "*\" OR @cm\\:email:\"" + query + "*\" )";

        logger.trace(luceneRequest);
        List<User> users = new ArrayList<>();

        ResultSet rs = null;
        try {
            rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_LUCENE, luceneRequest);
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

        logger.trace(users.size() + " resultats trouvés");

        return users;
    }

    public User buildUser(String username) {
        if (personService.personExists(username)) {
            return buildUser(personService.getPerson(username));
        } else {
            return null;
        }

    }

    public User buildUser(NodeRef userNodeRef) {
        User u = new User();

        //TODO complete build with all properties
        u.setLogin((String) nodeService.getProperty(userNodeRef, ContentModel.PROP_USERNAME));
        u.setFirstName((String) nodeService.getProperty(userNodeRef, ContentModel.PROP_FIRSTNAME));
        u.setName((String) nodeService.getProperty(userNodeRef, ContentModel.PROP_LASTNAME));
        u.setEmail((String) nodeService.getProperty(userNodeRef, ContentModel.PROP_EMAIL));

        return u;
    }

}
