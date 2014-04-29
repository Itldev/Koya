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

import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;
import java.util.Random;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestClientException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:koya-services-tests.xml")
public class SpaceServiceImplTest extends TestCase {

    private Logger logger = Logger.getLogger(this.getClass());

    @Autowired
    UserService userService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private SpaceService spaceService;

    private Company companyTests;
    private User admin;

    @Before
    public void createCompany() throws RestClientException, AlfrescoServiceException {
        admin = userService.login("admin", "admin");
        companyTests = companyService.create(admin, new Company("societe" + new Random().nextInt(1000), companyService.listSalesOffer(admin).get(0)), "default");
    }

    @After
    public void deleteSociete() throws RestClientException, AlfrescoServiceException {
        companyService.delete(admin, companyTests);
    }

    @Test
    public void testCreateSpace() throws RestClientException, AlfrescoServiceException {

        Space eTocreate = new Space("espace1", companyTests);
        Space eCreated = spaceService.create(admin, eTocreate);
        assertNotNull("erreur de creation de l'espace 'espace1'", eCreated);

    }

    @Test
    public void testCreateSubSpace() throws RestClientException, AlfrescoServiceException {

        Space eCreated = spaceService.create(admin, new Space("espaceParent", companyTests));
        assertNotNull("erreur de creation de l'espace 'espace Parent'", eCreated);

        Space eEnfant = spaceService.create(admin, new Space("espaceEnfant", eCreated));
        assertNotNull("erreur de creation de l'espace 'espace Enfant'", eEnfant);

    }

    @Test
    public void testSpaceToggleActive() throws RestClientException, AlfrescoServiceException {

        Space eCreated = spaceService.create(admin, new Space("espaceParent", companyTests));

        spaceService.create(admin, new Space("espaceEnfant", eCreated));

        for (Space e : spaceService.list(admin, companyTests)) {
            assertTrue("les espaces doivent etre actifs à l'initialisation", e.getActive());
            spaceService.disable(admin, e);
        }

        for (Space e : spaceService.list(admin, companyTests)) {
            assertTrue("les espaces devraient tous etre désactivés", !e.getActive());
            spaceService.enable(admin, e);
        }

        for (Space e : spaceService.list(admin, companyTests)) {
            assertTrue("les espaces devraient tous etre réactivés", e.getActive());
        }

    }

    @Test
    public void testListSpaces() throws RestClientException, AlfrescoServiceException {

        Space eParent1 = spaceService.create(admin, new Space("espaceParent1", companyTests));

        spaceService.create(admin, new Space("espaceEnfant11", eParent1));

        spaceService.create(admin, new Space("espaceEnfant12", eParent1));

        Space eParent2 = spaceService.create(admin, new Space("espaceParent2", companyTests));

        spaceService.create(admin, new Space("espaceEnfant21", eParent2));

        spaceService.create(admin, new Space("espaceEnfant22", eParent2));

        List<Space> lstArboEspaces = spaceService.list(admin, companyTests);

        assertEquals(2, lstArboEspaces.size());

        for (Space e : lstArboEspaces) {
            assertEquals(2, e.getChildren().size());
        }

    }

    @Test
    public void testMoveSpaces() throws RestClientException, AlfrescoServiceException {

        Space parentSpace = spaceService.create(admin, new Space("parentSpace", companyTests));

        Space childSpace = spaceService.create(admin, new Space("childSpace", parentSpace));

        assertEquals(1, spaceService.list(admin, companyTests).size());
        //move
        spaceService.move(admin, childSpace, companyTests);

        assertEquals(2, spaceService.list(admin, companyTests).size());
    }

    @Test
    public void testDelSpace() throws RestClientException, AlfrescoServiceException {

        Space space1 = spaceService.create(admin, new Space("space1", companyTests));

        Space space2 = spaceService.create(admin, new Space("space2", companyTests));

        assertEquals(2, spaceService.list(admin, companyTests).size());
        //del
        spaceService.del(admin, space2);

        assertEquals(1, spaceService.list(admin, companyTests).size());
    }

}
