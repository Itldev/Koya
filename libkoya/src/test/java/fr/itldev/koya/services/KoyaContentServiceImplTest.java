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

import fr.itldev.koya.model.Content;
import fr.itldev.koya.model.impl.Document;
import fr.itldev.koya.model.impl.Case;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.Directory;
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
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestClientException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:koya-services-tests.xml")
public class KoyaContentServiceImplTest extends TestCase {

    private Logger logger = Logger.getLogger(this.getClass());

    @Autowired
    UserService userService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private CaseService caseService;

    @Autowired
    private KoyaContentService koyaContentService;

    @Autowired
    private ApplicationContext applicationContext;

    private Company companyTests;
    private Space spaceTests;
    private Case caseTests;
    private User admin;

    @Before
    public void createSpace() throws RestClientException, AlfrescoServiceException {
        admin = userService.login("admin", "admin");
        companyTests = companyService.create(admin, new Company("societe" + new Random().nextInt(1000), companyService.listSalesOffer(admin).get(0)));
        spaceTests = spaceService.create(admin, new Space("Esptests", companyTests));
        caseTests = caseService.create(admin, new Case("doss1", spaceTests));
    }

    @After
    public void deleteCompany() throws RestClientException, AlfrescoServiceException {
        companyService.delete(admin, companyTests);
    }

    @Test
    public void testCreateDir() throws AlfrescoServiceException {

        Content rep = koyaContentService.create(admin, new Directory("rep", caseTests));
        assertNotNull("erreur de creation du répertoire", rep);
        koyaContentService.delete(admin, rep);
    }

    @Test
    public void testListDir() throws AlfrescoServiceException {
        Content rep = koyaContentService.create(admin, new Directory("rep", caseTests));
        koyaContentService.create(admin, new Directory("sousrep", (Directory) rep));
        koyaContentService.create(admin, new Directory("rep2", caseTests));

        List<Content> lst = koyaContentService.list(admin, caseTests);

        assertEquals(3, lst.size());

    }

    @Test
    public void testUpload() throws AlfrescoServiceException {

        Integer sizeBefore = koyaContentService.list(admin, caseTests).size();

        Resource toUpload = applicationContext.getResource("classpath:docs/testupload.txt");
        koyaContentService.upload(admin, toUpload, caseTests);

        List<Content> lstC = koyaContentService.list(admin, caseTests);
        //il doit y avoir un element en plus
        assertEquals(sizeBefore + 1, lstC.size());

    }
    @Test
    public void testMoveFile() throws AlfrescoServiceException {

        Directory rep = (Directory) koyaContentService.create(admin, new Directory("rep1", caseTests));

        Integer sizeBefore = koyaContentService.list(admin, caseTests).size();

        Resource toUpload = applicationContext.getResource("classpath:docs/testupload.txt");
        Document doc = koyaContentService.upload(admin, toUpload, rep);

        assertEquals(1 + sizeBefore, koyaContentService.list(admin, caseTests).size());

        //deplacement du doc dans la racine
        koyaContentService.move(admin, doc, caseTests);

        //il doit y avoir un element en plus
        //TODO pas probant car la liste retourne l'ensemble des elements recursivement
        //et donc ca ne change rien .... TODO idem avec structure hiérarchique
        assertEquals(sizeBefore + 1, koyaContentService.list(admin, caseTests).size());

    }
}
