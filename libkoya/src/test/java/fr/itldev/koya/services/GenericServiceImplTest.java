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

import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Directory;
import fr.itldev.koya.model.impl.Document;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.MetaInfos;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:koya-services-tests.xml")
public class GenericServiceImplTest extends TestCase {

    @SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(this.getClass());

    @Autowired
    UserService userService;

    @Autowired
    private AlfrescoService alfrescoService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private DossierService dossierService;

    @Autowired
    private KoyaContentService koyaContentService;

    @Autowired
    private KoyaNodeService koyaNodeService;

    @Autowired
    private ApplicationContext applicationContext;

    private Company companyTests;
    private Space spaceTests;
    private Dossier dossierTests;
    private User admin;

    @Before
    public void createDossier() throws RestClientException, AlfrescoServiceException {
        admin = userService.login("admin", "admin");
        companyTests = companyService.create(admin,
                "company" + new Random().nextInt(1000000), companyService.listSalesOffer(admin).get(0).getName(), "default");
        spaceTests = spaceService.create(admin,companyTests,"testSpace");
        dossierTests = dossierService.create(admin, spaceTests, "doss1");
    }

    @After
    public void deleteCompany() throws RestClientException {
        try {
            companyService.delete(admin, companyTests);
        } catch (AlfrescoServiceException aex) {
            System.err.println("error deleting company '" + companyTests.getTitle() + "' : " + aex.getKoyaErrorCode() + " - " + aex.getMessage());
        }
    }

    @Test
    public void testgetParent() throws AlfrescoServiceException {
        Directory dir = koyaContentService.createDir(admin, dossierTests, "dir");
        System.err.println(dir);
        assertTrue(koyaNodeService.getParent(admin, (KoyaNode) dir).getName().equals("doss1"));
    }

    @Test
    public void testgetParents1() throws AlfrescoServiceException {
        Directory dir = koyaContentService.createDir(admin, dossierTests, "dir2");
        assertEquals(koyaNodeService.getParents(admin, (KoyaNode) dir).size(), 3);
    }

    @Test
    public void testgetParents2() throws AlfrescoServiceException {
        Directory dir = koyaContentService.createDir(admin, dossierTests, "dir");
        Directory subDir = koyaContentService.createDir(admin, dir, "subdir");

        Resource toUpload = applicationContext.getResource("classpath:docs/testupload.txt");
        Document doc = koyaContentService.upload(admin, subDir.getNodeRef(), toUpload);

        assertEquals(koyaNodeService.getParents(admin, doc).size(), 5);
    }

    @Test
    public void testgetServerInfos() throws AlfrescoServiceException {
        MetaInfos infos = alfrescoService.getServerInfos(admin);

        for (String k : infos.getKoyaInfos().stringPropertyNames()) {
            System.out.println(k + "= " + infos.getKoyaInfos().getProperty(k));
        }
        System.out.println("===");

        for (String k : infos.getServerInfos().stringPropertyNames()) {
            System.out.println(k + "= " + infos.getServerInfos().getProperty(k));
        }

        assertEquals(infos.getKoyaInfos().getProperty("module.id"), "koya-alfresco");

    }

    @Test
    public void checkMatchTest() {
        assertTrue(alfrescoService.checkLibVersionMatch());
    }

}
