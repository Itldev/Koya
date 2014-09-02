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

package fr.itldev.koya.sample;

import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.User;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.impl.SiteLoadPatch;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.log4j.Logger;

/**
 *
 *
 */
public class KoyaSiteLoadPatch extends SiteLoadPatch {
    
    private static final String DEFAULT_DEMO_PASSWORD = "demo";
    
    private final Logger logger = Logger.getLogger(this.getClass());
    private UserService userService;
    private PersonService personService;
    
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }
    
    @Override
    protected String applyInternal() throws Exception {
        String ret = super.applyInternal();
        //create default authentication for all created users - password = demo
        ResultSet rs = null;
        try {
            rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_LUCENE, "TYPE:\"cm:person\"");
            for (ResultSetRow r : rs) {
                
                String username = nodeService.getProperty(r.getNodeRef(), ContentModel.PROP_USERNAME).toString();
              
                
                User userToCreate = new User();
                userToCreate.setUserName(username);
                userToCreate.setFirstName(nodeService.getProperty(r.getNodeRef(), ContentModel.PROP_FIRSTNAME).toString());
                userToCreate.setName(nodeService.getProperty(r.getNodeRef(), ContentModel.PROP_LASTNAME).toString());
                userToCreate.setEmail(nodeService.getProperty(r.getNodeRef(), ContentModel.PROP_EMAIL).toString());
                userToCreate.setPassword(DEFAULT_DEMO_PASSWORD);
                
                if (!personService.personExists(username)) {
                    //delete original node (person without authentication)                    
                    personService.deletePerson(r.getNodeRef());
                    
                    try {
                        userService.createUser(userToCreate);
                    } catch (KoyaServiceException kex) {
                    }
                }
                
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        
        return ret;
    }
    
}
