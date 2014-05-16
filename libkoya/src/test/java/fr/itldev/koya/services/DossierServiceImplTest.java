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
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
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
public class DossierServiceImplTest extends TestCase {

    private Logger logger = Logger.getLogger(this.getClass());

    @Autowired
    UserService userService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private DossierService dossierService;

    private Company companyTests;
    private Space spaceTests;
    User admin;

    @Before
    public void createSpace() throws RestClientException, AlfrescoServiceException {
        admin = userService.login("admin", "admin");
        companyTests = companyService.create(admin, new Company("societe" + new Random().nextInt(1000), companyService.listSalesOffer(admin).get(0)), "default");
        spaceTests = spaceService.create(admin, new Space("Esptests"), companyTests);
    }

    @After
    public void deleteCompany() throws RestClientException, AlfrescoServiceException {
        companyService.delete(admin, companyTests);
    }

    @Test
    public void testCreateDossier() throws AlfrescoServiceException {
        Dossier cree = dossierService.create(admin, new Dossier("doss1"), spaceTests);
        assertNotNull("erreur de creation de l'espace 'espace Enfant'", cree);

        dossierService.list(admin, spaceTests);
        //  dossierService.supprimer(admin, cree);
    }

    @Test
    public void testListDossiers() throws AlfrescoServiceException {
        dossierService.create(admin, new Dossier("doss1"), spaceTests);
        dossierService.create(admin, new Dossier("doss2"), spaceTests);
        dossierService.create(admin, new Dossier("doss3"), spaceTests);
        dossierService.create(admin, new Dossier("doss4"), spaceTests);
        assertEquals(4, dossierService.list(admin, spaceTests).size());
    }
}
