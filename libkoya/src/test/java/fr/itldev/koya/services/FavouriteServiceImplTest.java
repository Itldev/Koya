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

import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Document;
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
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestClientException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:koya-services-tests.xml")
public class FavouriteServiceImplTest extends TestCase {

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
    private KoyaContentService koyaContentService;

    @Autowired
    private FavouriteService favouriteService;

    @Autowired
    private ApplicationContext applicationContext;

    private Company companyTests;
    private Space spaceTests;
    private Dossier dossierTests;
    private User admin;

    @Before
    public void createSpace() throws RestClientException, AlfrescoServiceException {
        admin = userService.login("admin", "admin");
        companyTests = companyService.create(admin, new Company("societe" + new Random().nextInt(1000), companyService.listSalesOffer(admin).get(0)));
        spaceTests = spaceService.create(admin, new Space("Esptests", companyTests));
        dossierTests = dossierService.create(admin, new Dossier("doss1", spaceTests));
    }

    @After
    public void deleteCompany() throws RestClientException, AlfrescoServiceException {
        companyService.delete(admin, companyTests);
    }

    @Test
    public void ReadFavouritesTest() throws AlfrescoServiceException {
        assertNotNull(favouriteService.getFavourites(admin));
    }

    @Test
    public void addDelFavouriteDossierTest() throws AlfrescoServiceException {
        favouriteService.setFavouriteValue(admin, dossierTests, Boolean.TRUE);
        assertTrue(dossierService.list(admin, spaceTests).get(0).isUserFavourite());
        favouriteService.setFavouriteValue(admin, dossierTests, Boolean.FALSE);
        assertFalse(dossierService.list(admin, spaceTests).get(0).isUserFavourite());
    }

    @Test
    public void addDelFavouriteDocTest() throws AlfrescoServiceException {
        Resource toUpload = applicationContext.getResource("classpath:docs/testupload.txt");
        Document doc = koyaContentService.upload(admin, toUpload, dossierTests);
        favouriteService.setFavouriteValue(admin, doc, Boolean.TRUE);
        assertTrue(koyaContentService.list(admin, dossierTests).get(0).isUserFavourite());
        favouriteService.setFavouriteValue(admin, doc, Boolean.FALSE);
        assertFalse(koyaContentService.list(admin, dossierTests).get(0).isUserFavourite());
    }

    @Test
    public void addDelFavourite2Test() throws AlfrescoServiceException {

        assertEquals(0, favouriteService.getFavourites(admin).size());
        favouriteService.setFavouriteValue(admin, dossierTests, Boolean.TRUE);
        favouriteService.setFavouriteValue(admin, spaceTests, Boolean.TRUE);
        favouriteService.setFavouriteValue(admin, companyTests, Boolean.TRUE);
        assertEquals(3, favouriteService.getFavourites(admin).size());
        favouriteService.setFavouriteValue(admin, dossierTests, Boolean.FALSE);
        assertEquals(2, favouriteService.getFavourites(admin).size());

        Resource toUpload = applicationContext.getResource("classpath:docs/testupload.txt");
        Document doc = koyaContentService.upload(admin, toUpload, dossierTests);
        favouriteService.setFavouriteValue(admin, doc, Boolean.TRUE);
        assertEquals(3, favouriteService.getFavourites(admin).size());

    }
}
