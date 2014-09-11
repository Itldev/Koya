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
import java.util.ArrayList;
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

    @Autowired
    private SecuService secuService;

    private Company companyTests;
    private Space spaceTests;
    User admin;

    private List<User> testUsers;

    @Before
    public void init() throws RestClientException, AlfrescoServiceException {
        admin = userService.login("admin", "admin");
        companyTests = companyService.create(admin, new Company("company" + new Random().nextInt(1000), companyService.listSalesOffer(admin).get(0)), "default");
        spaceTests = spaceService.create(admin, new Space("testSpace"), companyTests);

        //create to 2 test users if they don't exists (ie while nb users < 3)
        List<User> users = userService.find(admin, null, 10, companyTests, null);
        while (users.size() < 3) {

            try {
                String randomPart = Integer.toString(new Random().nextInt(1000));
                String mail = "userdossiertest" + randomPart + "@test.com";
                String userName = "userdossiertest" + randomPart + "_test_com";

                secuService.inviteUser(admin, companyTests, mail,
                        "SiteManager", "", "", "");
                secuService.setUserRole(admin, companyTests, userName, "SiteManager");
                users = userService.find(admin, null, 10, companyTests, null);
            } catch (Exception ex) {
                //silent catch any exception to execute a new try
            }
        }

        //select 2 test users = 2 users that are not admin
        testUsers = new ArrayList<>();
        for (User u : users) {
            if (!u.getUserName().equals("admin")) {
                testUsers.add(u);
            }
        }

    }

    @After
    public void deleteCompany() throws RestClientException, AlfrescoServiceException {
        companyService.delete(admin, companyTests);
    }

    @Test
    public void testCreateDossier() throws AlfrescoServiceException {
        Dossier created = dossierService.create(admin, new Dossier("doss1"), spaceTests);
        assertNotNull("error creating 'child dossier'", created);

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

    @Test
    public void testListResponsibles() throws AlfrescoServiceException {
        Dossier d = dossierService.create(admin, new Dossier("dossLstResp"), spaceTests);
        List<User> resp = dossierService.listResponsibles(admin, d);
        assertEquals(resp.size(), 1);
        assertEquals(resp.get(0), admin);

    }

    @Test
    public void testAddDelResponsibles() throws AlfrescoServiceException {

        Dossier d = dossierService.create(admin, new Dossier("dossAddDelResp"), spaceTests);
        List<User> resp = dossierService.listResponsibles(admin, d);
        assertEquals(resp.size(), 1);//creator automaticly set as responsible

        User u1 = testUsers.get(0);
        User u2 = testUsers.get(1);

        //Add a new responsibles --> now 2 reponsibles
        dossierService.addResponsible(admin, d, u1);
        assertEquals(2, dossierService.listResponsibles(admin, d).size());
        //adding twice same user shouldn't be taken in account
        dossierService.addResponsible(admin, d, u1);
        assertEquals(2, dossierService.listResponsibles(admin, d).size());

        //del a responsible --> now only 1
        dossierService.delMemberOrResponsible(admin, d, admin);
        assertEquals(1, dossierService.listResponsibles(admin, d).size());
        assertEquals(u1, dossierService.listResponsibles(admin, d).get(0));
        //remove non responsive shouldn't have impact
        dossierService.delMemberOrResponsible(admin, d, admin);
        assertEquals(1, dossierService.listResponsibles(admin, d).size());

        //Add / del collection test
        dossierService.addResponsible(admin, d, testUsers);
        assertEquals(2, dossierService.listResponsibles(admin, d).size());
        //2 because u1 is already in responsibles list

        for (User u : testUsers) {
            dossierService.delMemberOrResponsible(admin, d, u);
        }
        assertEquals(0, dossierService.listResponsibles(admin, d).size());

    }

}
