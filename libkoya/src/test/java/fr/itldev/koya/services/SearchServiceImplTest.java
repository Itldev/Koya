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

package fr.itldev.koya.services;

import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import fr.itldev.koya.services.impl.SearchServiceImpl;
import java.util.List;
import java.util.Random;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestClientException;

/**
 *
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:koya-services-tests.xml")
public class SearchServiceImplTest extends TestCase {

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
    private SearchServiceImpl searchService;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private KoyaContentService koyaContentService;

    private Company companyTests;
    private Space spaceFinancial;
    private Dossier dossierTestsOne;
    private Dossier dossierTestsTwo;
    private Dossier dossierTestsThree;

    private Space spaceBuildings;
    private Dossier dossierTestsFirst;
    private Dossier dossierTestsSecond;
    private Dossier dossierTestsThird;

    private User admin;

    @Before
    public void createEnv() throws RestClientException, AlfrescoServiceException {
        admin = userService.login("admin", "admin");
        companyTests = companyService.create(admin, new Company("company" + new Random().nextInt(1000), companyService.listSalesOffer(admin).get(0)), "default");
        spaceFinancial = spaceService.create(admin, new Space("financial"), companyTests);
        dossierTestsOne = dossierService.create(admin, new Dossier("one doss"), spaceFinancial);

        Resource toUpload = applicationContext.getResource("classpath:docs/test financial report.txt");
        koyaContentService.upload(admin, toUpload, dossierTestsOne);

        dossierTestsTwo = dossierService.create(admin, new Dossier("two doss"), spaceFinancial);
        dossierTestsThree = dossierService.create(admin, new Dossier("three financial"), spaceFinancial);

        spaceBuildings = spaceService.create(admin, new Space("buildings"), companyTests);
        dossierTestsFirst = dossierService.create(admin, new Dossier("first test"), spaceBuildings);
        dossierTestsSecond = dossierService.create(admin, new Dossier("second test"), spaceBuildings);
        dossierTestsThird = dossierService.create(admin, new Dossier("third"), spaceBuildings);

    }

    @After
    public void deleteCompany() throws RestClientException, AlfrescoServiceException {
        companyService.delete(admin, companyTests);
    }

    @Test
    public void testSearch1() {
        String searchTerm = "fi";
        /* 
         in Company Scope : should return 4 elements : 
         Space financial, Document test financial report.txt
         Dossier three financial and Dossier first test
         */
        assertEquals(searchService.search(admin, companyTests, searchTerm).size(), 4);

        /* 
         in Space 'financial' Scope : should return 2 elements : 
         Document test financial report.txt and Dossier three financial 
         */
        assertEquals(searchService.search(admin, spaceFinancial, searchTerm).size(), 2);

        /* 
         in Space 'financial' Scope : should return 2 elements : 
         Document test financial report.txt and Dossier three financial 
         */
        assertEquals(searchService.search(admin, dossierTestsOne, searchTerm).size(), 1);
    }

    @Test
    public void testSearch2() {
        String searchTerm = "fi test";
        /* 
         in Company Scope : should return 2 elements : 
         Document test financial report.txt and Dossier first test
         */
        assertEquals(searchService.search(admin, companyTests, searchTerm).size(), 2);

        /* 
         in Space 'financial' Scope : should return 1 elements : 
         Document financial report.txt 
         */
        assertEquals(searchService.search(admin, spaceFinancial, searchTerm).size(), 1);

        
    }

    /*
     TODO test access with different users 
     search inside contents
     search on types
    
    
    
     */
}
