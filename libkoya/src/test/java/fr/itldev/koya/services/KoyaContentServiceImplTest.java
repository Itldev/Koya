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

import fr.itldev.koya.model.interfaces.Content;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.Document;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.Directory;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
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
    private DossierService dossierService;

    @Autowired
    private KoyaContentService koyaContentService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SecuredItemService securedItemService;

    private Company companyTests;
    private Space spaceTests;
    private Dossier dossierTests;
    private User admin;

    @Before
    public void createSpace() throws RestClientException, AlfrescoServiceException {
        admin = userService.login("admin", "admin");
        companyTests = companyService.create(admin, new Company("company" + new Random().nextInt(1000), companyService.listSalesOffer(admin).get(0)), "default");
        spaceTests = spaceService.create(admin, new Space("testSpace"), companyTests);
        dossierTests = dossierService.create(admin, new Dossier("doss1"), spaceTests);
    }

    @After
    public void deleteCompany() throws RestClientException, AlfrescoServiceException {
        companyService.delete(admin, companyTests);
    }

    @Test
    public void testCreateDir() throws AlfrescoServiceException {
        Content dir = koyaContentService.create(admin, new Directory("dir"), dossierTests);
        assertNotNull("error creating directory", dir);
        securedItemService.delete(admin, (SecuredItem) dir);
    }

    @Test
    public void testCreateDirAlreadyExists() throws AlfrescoServiceException {
        Content dir = koyaContentService.create(admin, new Directory("dir"), dossierTests);
        assertNotNull("error creating directory", dir);

        try {
            koyaContentService.create(admin, new Directory("dir"), dossierTests);
            fail("should throw exception with error code : " + KoyaErrorCodes.DIR_CREATION_NAME_EXISTS);
        } catch (AlfrescoServiceException aex) {
            assertEquals(aex.getKoyaErrorCode(), KoyaErrorCodes.DIR_CREATION_NAME_EXISTS);
        } finally {
            securedItemService.delete(admin, (SecuredItem) dir);
        }
    }

    @Test
    public void testListDir() throws AlfrescoServiceException {
        Content dir = koyaContentService.create(admin, new Directory("dir"), dossierTests);
        koyaContentService.create(admin, new Directory("subdir"), (Directory) dir);
        koyaContentService.create(admin, new Directory("dir2"), dossierTests);
        List<Content> lst = koyaContentService.list(admin, dossierTests, true);
        assertEquals(2, lst.size());
    }

    @Test
    public void testListDirectChildren() throws AlfrescoServiceException {
        Content dir = koyaContentService.create(admin, new Directory("dir"), dossierTests);
        koyaContentService.create(admin, new Directory("subdir"), (Directory) dir);
        koyaContentService.create(admin, new Directory("dir2"), dossierTests);

        Resource toUpload = applicationContext.getResource("classpath:docs/testupload.txt");
        koyaContentService.upload(admin, toUpload, dossierTests);

        List<Content> lst = koyaContentService.list(admin, dossierTests, false, 2);

        assertEquals(3, lst.size());

        assertEquals(1, koyaContentService.list(admin, (Directory) dir, false, 2).size());

    }

    @Test
    public void testMoveDir() throws AlfrescoServiceException {
        Content dir = koyaContentService.create(admin, new Directory("dir"), dossierTests);
        Content sDir = koyaContentService.create(admin, new Directory("subdir"), (Directory) dir);
        Content dir2 = koyaContentService.create(admin, new Directory("dir2"), dossierTests);

        assertEquals(1, koyaContentService.list(admin, (Directory) dir, true, 1).size());
        assertEquals(0, koyaContentService.list(admin, (Directory) dir2, true, 1).size());
        koyaContentService.move(admin, (Content) sDir, (Directory) dir2);
        assertEquals(0, koyaContentService.list(admin, (Directory) dir, true, 1).size());
        assertEquals(1, koyaContentService.list(admin, (Directory) dir2, true, 1).size());
    }

    @Test
    public void testMoveDirAlreadyExists() throws AlfrescoServiceException {
        Content dir = koyaContentService.create(admin, new Directory("dir"), dossierTests);
        Content sDir = koyaContentService.create(admin, new Directory("subdir"), (Directory) dir);
        Content dir2 = koyaContentService.create(admin, new Directory("dir2"), dossierTests);
        Content sDir2 = koyaContentService.create(admin, new Directory("subdir"), (Directory) dir2);

        try {
            koyaContentService.move(admin, (Content) sDir, (Directory) dir2);
        } catch (AlfrescoServiceException aex) {
            assertEquals(aex.getKoyaErrorCode(), KoyaErrorCodes.MOVE_DESTINATION_NAME_ALREADY_EXISTS);
        }
    }

    @Test
    public void testUpload() throws AlfrescoServiceException {

        Integer sizeBefore = koyaContentService.list(admin, dossierTests, false).size();

        Resource toUpload = applicationContext.getResource("classpath:docs/testupload.txt");
        koyaContentService.upload(admin, toUpload, dossierTests);

        List<Content> lstC = koyaContentService.list(admin, dossierTests, false);
        //should contain one more element
        assertEquals(sizeBefore + 1, lstC.size());
    }

    @Test
    public void testUploadAlreadyExists() throws AlfrescoServiceException {

        Resource toUpload = applicationContext.getResource("classpath:docs/testupload.txt");
        koyaContentService.upload(admin, toUpload, dossierTests);

        try {
            koyaContentService.upload(admin, toUpload, dossierTests);
        } catch (AlfrescoServiceException aex) {

            System.out.println(aex.getKoyaErrorCode());
            assertEquals(aex.getKoyaErrorCode(), KoyaErrorCodes.MOVE_DESTINATION_NAME_ALREADY_EXISTS);
        }

    }

    @Test
    public void testMoveFile() throws AlfrescoServiceException {

        Directory dir = (Directory) koyaContentService.create(admin, new Directory("dir1"), dossierTests);

        Integer sizeBefore = koyaContentService.list(admin, dossierTests, false).size();

        Resource toUpload = applicationContext.getResource("classpath:docs/testupload.txt");
        Document doc = koyaContentService.upload(admin, toUpload, dir);

        assertEquals(sizeBefore.intValue(), koyaContentService.list(admin, dossierTests, false).size());

        //deplacement du doc dans la racine
        koyaContentService.move(admin, doc, dossierTests);

        //il doit y avoir un element en plus
        //TODO pas probant car la liste retourne l'ensemble des elements recursivement
        //et donc ca ne change rien .... TODO idem avec structure hiérarchique
        assertEquals(sizeBefore + 1, koyaContentService.list(admin, dossierTests, false).size());

    }

    @Test
    public void testGetParent() throws AlfrescoServiceException {
        Content dir3 = koyaContentService.create(admin, new Directory("dir3"), dossierTests);
        Content sdir = koyaContentService.create(admin, new Directory("sousrep"), (Directory) dir3);

        assertEquals(dir3, securedItemService.getParent(admin, (SecuredItem) sdir));
    }

    @Test
    public void testGetParentDoc() throws AlfrescoServiceException {

        Resource toUpload = applicationContext.getResource("classpath:docs/testupload.txt");
        Document doc = koyaContentService.upload(admin, toUpload, dossierTests);

        assertEquals(dossierTests, securedItemService.getParent(admin, doc));
    }

    @Test
    public void testDocByteSize() throws AlfrescoServiceException {

        Resource toUpload = applicationContext.getResource("classpath:docs/testupload.txt");
        Document doc = koyaContentService.upload(admin, toUpload, dossierTests);

        assertEquals(new Long(854), doc.getByteSize());
    }

    @Test
    public void testDirDiskSize() throws AlfrescoServiceException {

        Directory dir4 = (Directory) koyaContentService.create(admin, new Directory("dir4"), dossierTests);

        Directory dir5 = (Directory) koyaContentService.create(admin, new Directory("dir5"), dir4);

        Resource toUpload = applicationContext.getResource("classpath:docs/testupload.txt");
        Document doc1 = koyaContentService.upload(admin, toUpload, dir4);

        Document doc2 = koyaContentService.upload(admin, toUpload, dir5);

        assertEquals(new Long(854 * 2), koyaContentService.getDiskSize(admin, dir4));
    }

    @Test
    public void testDeleteDir() throws AlfrescoServiceException {

        int nbDir = koyaContentService.list(admin, dossierTests, false).size();
        Directory deldir = (Directory) koyaContentService.create(admin, new Directory("deldir"), dossierTests);

        assertEquals(nbDir + 1, koyaContentService.list(admin, dossierTests, false).size());

        securedItemService.delete(admin, deldir);

        assertEquals(nbDir, koyaContentService.list(admin, dossierTests, false).size());
    }

    @Test
    public void testDeleteContent() throws AlfrescoServiceException {

        int nbDir = koyaContentService.list(admin, dossierTests, false).size();

        Resource toUpload = applicationContext.getResource("classpath:docs/testupload.txt");
        Document upDoc = koyaContentService.upload(admin, toUpload, dossierTests);

        assertEquals(nbDir + 1, koyaContentService.list(admin, dossierTests, false).size());

        securedItemService.delete(admin, upDoc);

        assertEquals(nbDir, koyaContentService.list(admin, dossierTests, false).size());
    }

    @Test
    public void testRenameDir() throws AlfrescoServiceException {

        Directory renameDir = (Directory) koyaContentService.create(admin, new Directory("oldName"), dossierTests);
        for (Content c : koyaContentService.list(admin, dossierTests, false)) {
            if (c.getName().equals("newName")) {
                fail();
            }
        }
        securedItemService.rename(admin, renameDir, "newName");

        for (Content c : koyaContentService.list(admin, dossierTests, false)) {
            if (c.getName().equals("oldName")) {
                fail();
            }
        }

    }

    @Test
    public void testRenameContent() throws AlfrescoServiceException {

        Resource toUpload = applicationContext.getResource("classpath:docs/testupload.txt");
        Document upDoc = koyaContentService.upload(admin, toUpload, dossierTests);

        for (Content c : koyaContentService.list(admin, dossierTests, false)) {
            if (c.getName().equals("newName")) {
                fail();
            }
        }
        securedItemService.rename(admin, upDoc, "newName");

        for (Content c : koyaContentService.list(admin, dossierTests, false)) {
            if (c.getName().equals("testupload.txt")) {
                fail();
            }
        }

    }

    @Test
    public void testImportTreeAsZip() throws AlfrescoServiceException {

        Resource toUpload = applicationContext.getResource("classpath:docs/zippedtree.zip");
        Document upDoc = koyaContentService.upload(admin, toUpload, dossierTests);

        for (Content c : koyaContentService.list(admin, dossierTests, false)) {
            if (c.getName().equals("rootzip")) {
                fail();
            }
        }

        //TODO fix this test - generate faled to import zip file : should work ...
//        koyaContentService.importZipedContent(admin, upDoc);
//        boolean rootExists = false;
//        for (Content c : koyaContentService.list(admin, dossierTests, false)) {
//            if (c.getName().equals("rootzip")) {
//                rootExists = true;
//            }
//        }
//
//        if (!rootExists) {
//            fail();
//        }
    }
}
