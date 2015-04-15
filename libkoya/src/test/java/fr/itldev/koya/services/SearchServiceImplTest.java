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
import fr.itldev.koya.services.impl.SearchServiceImpl;
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
        companyTests = companyService.create(admin, "company" + new Random().nextInt(1000000),
                companyService.listSalesOffer(admin).get(0).getName(), "default");
        spaceFinancial = spaceService.create(admin, new Space("financial"), companyTests);
        dossierTestsOne = dossierService.create(admin, spaceFinancial, "one doss");

        Resource toUpload = applicationContext.getResource("classpath:docs/test financial report.txt");
        koyaContentService.upload(admin, dossierTestsOne.getNodeRefasObject(), toUpload);

        dossierTestsTwo = dossierService.create(admin, spaceFinancial, "two doss");
        dossierTestsThree = dossierService.create(admin, spaceFinancial, "three financial");

        spaceBuildings = spaceService.create(admin, new Space("buildings"), companyTests);
        dossierTestsFirst = dossierService.create(admin, spaceBuildings, "first test");
        dossierTestsSecond = dossierService.create(admin, spaceBuildings, "second test");
        dossierTestsThird = dossierService.create(admin, spaceBuildings, "third");

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
        assertEquals(4, searchService.search(admin, companyTests, searchTerm).size());

        /* 
         in Space 'financial' Scope : should return 2 elements : 
         Document test financial report.txt and Dossier three financial 
         */
        //TODO results returned out of search context
        assertEquals(2,searchService.search(admin, spaceFinancial, searchTerm).size());

        /* 
         in Space 'financial' Scope : should return 2 elements : 
         Document test financial report.txt and Dossier three financial 
         */
        //TODO results returned out of search context
        assertEquals(1, searchService.search(admin, dossierTestsOne, searchTerm).size());
    }

    @Test
    public void testSearch2() {
        String searchTerm = "fi test";
        /* 
         in Company Scope : should return 2 elements : 
         Document test financial report.txt and Dossier first test
         */
        assertEquals(2,searchService.search(admin, companyTests, searchTerm).size());

        /* 
         in Space 'financial' Scope : should return 1 elements : 
         Document financial report.txt 
         */
        //TODO out of context results 
        assertEquals(1,searchService.search(admin, spaceFinancial, searchTerm).size());

    }

    /*
     TODO test access with different users 
     search inside contents
     search on types
    
    
    
     */
}
