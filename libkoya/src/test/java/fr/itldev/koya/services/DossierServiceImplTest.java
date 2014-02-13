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

import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Espace;
import fr.itldev.koya.model.impl.Societe;
import fr.itldev.koya.model.impl.Utilisateur;
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
    private SocieteService societeService;

    @Autowired
    private EspaceService espacesService;

    @Autowired
    private DossierService dossierService;

    private Societe societeTests;
    private Espace espaceTests;
    Utilisateur admin;

    @Before
    public void createEspace() throws RestClientException, AlfrescoServiceException {
        admin = userService.login("admin", "admin");
        societeTests = societeService.creerNouvelle(admin, new Societe("societe" + new Random().nextInt(1000), societeService.listerOffresCommerciales(admin).get(0)));
        espaceTests = espacesService.creerNouveau(admin, new Espace("Esptests", societeTests));
    }

    @After
    public void deleteSociete() throws RestClientException, AlfrescoServiceException {
        societeService.supprimer(admin, societeTests);
    }

    @Test
    public void testCreaDossier() throws AlfrescoServiceException {
        Dossier cree = dossierService.creerNouveau(admin, new Dossier("doss1", espaceTests));
        assertNotNull("erreur de creation de l'espace 'espace Enfant'", cree);

        dossierService.lister(admin, espaceTests);
        //  dossierService.supprimer(admin, cree);
    }

    @Test
    public void testListDossiers() throws AlfrescoServiceException {
        dossierService.creerNouveau(admin, new Dossier("doss1", espaceTests));
        dossierService.creerNouveau(admin, new Dossier("doss2", espaceTests));
        dossierService.creerNouveau(admin, new Dossier("doss3", espaceTests));
        dossierService.creerNouveau(admin, new Dossier("doss4", espaceTests));
        assertEquals(4, dossierService.lister(admin, espaceTests).size());
    }

//    @Test
//    public void testTaille() throws AlfrescoServiceException, AlfrescoFtpException {
//        Dossier doss1 = dossierService.creerNouveau(admin, new Dossier("doss1", espaceTests));
//
//        //verifier la taille des fichiers a envoyer
//        long fileSize = 0;
//
//        //TODO quand le service d'upload de fichiers sera actif
//        //verifier que taille dossier = Somme taille fichier uploadés
//        assertEquals(fileSize, dossierService.getTailleOctet(admin, doss1));
//
//        System.out.println("le dossier '" + doss1 + "' fait " + societeService.getTailleString(admin, doss1));
//    }
}
