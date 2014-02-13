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

import fr.itldev.koya.model.Contenu;
import fr.itldev.koya.model.impl.Document;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Espace;
import fr.itldev.koya.model.impl.Repertoire;
import fr.itldev.koya.model.impl.Societe;
import fr.itldev.koya.model.impl.Utilisateur;
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
public class ContenuServiceImplTest extends TestCase {

    private Logger logger = Logger.getLogger(this.getClass());

    @Autowired
    UserService userService;

    @Autowired
    private SocieteService societeService;

    @Autowired
    private EspaceService espacesService;

    @Autowired
    private DossierService dossierService;

    @Autowired
    private ContenuService contenuService;

    @Autowired
    private ApplicationContext applicationContext;

    private Societe societeTests;
    private Espace espaceTests;
    private Dossier dossierTests;
    private Utilisateur admin;

    @Before
    public void createEspace() throws RestClientException, AlfrescoServiceException {
        admin = userService.login("admin", "admin");
        societeTests = societeService.creerNouvelle(admin, new Societe("societe" + new Random().nextInt(1000), societeService.listerOffresCommerciales(admin).get(0)));
        espaceTests = espacesService.creerNouveau(admin, new Espace("Esptests", societeTests));
        dossierTests = dossierService.creerNouveau(admin, new Dossier("doss1", espaceTests));
    }

    @After
    public void deleteSociete() throws RestClientException, AlfrescoServiceException {
        societeService.supprimer(admin, societeTests);
    }

    @Test
    public void testCreaRepertoire() throws AlfrescoServiceException {

        Contenu rep = contenuService.creerContenu(admin, new Repertoire("rep", dossierTests));
        assertNotNull("erreur de creation du répertoire", rep);
        contenuService.supprimer(admin, rep);
    }

    @Test
    public void testListeRepertoire() throws AlfrescoServiceException {
        Contenu rep = contenuService.creerContenu(admin, new Repertoire("rep", dossierTests));
        contenuService.creerContenu(admin, new Repertoire("sousrep", (Repertoire) rep));
        contenuService.creerContenu(admin, new Repertoire("rep2", dossierTests));

        List<Contenu> lst = contenuService.lister(admin, dossierTests);

        assertEquals(3, lst.size());

    }

    @Test
    public void testUpload() throws AlfrescoServiceException {

        Integer sizeBefore = contenuService.lister(admin, dossierTests).size();

        Resource toUpload = applicationContext.getResource("classpath:docs/testupload.txt");
        contenuService.envoyerDocument(admin, toUpload, dossierTests);

        List<Contenu> lstC = contenuService.lister(admin, dossierTests);
        //il doit y avoir un element en plus
        assertEquals(sizeBefore + 1, lstC.size());

    }
    @Test
    public void testMoveFile() throws AlfrescoServiceException {

        Repertoire rep = (Repertoire) contenuService.creerContenu(admin, new Repertoire("rep1", dossierTests));

        Integer sizeBefore = contenuService.lister(admin, dossierTests).size();

        Resource toUpload = applicationContext.getResource("classpath:docs/testupload.txt");
        Document doc = contenuService.envoyerDocument(admin, toUpload, rep);

        assertEquals(1 + sizeBefore, contenuService.lister(admin, dossierTests).size());

        //deplacement du doc dans la racine
        contenuService.deplacer(admin, doc, dossierTests);

        //il doit y avoir un element en plus
        //TODO pas probant car la liste retourne l'ensemble des elements recursivement
        //et donc ca ne change rien .... TODO idem avec structure hiérarchique
        assertEquals(sizeBefore + 1, contenuService.lister(admin, dossierTests).size());

    }
}
