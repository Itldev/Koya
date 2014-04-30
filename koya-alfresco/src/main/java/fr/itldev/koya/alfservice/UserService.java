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
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.log4j.Logger;

/**
 *
 */
public class UserService {

    private Logger logger = Logger.getLogger(this.getClass());

    protected NodeService nodeService;
    private PersonService personService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
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

}
