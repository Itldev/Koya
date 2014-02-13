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

import fr.itldev.koya.model.impl.Espace;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestClientException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:koya-services-tests.xml")
public class EspaceServiceImplTest extends TestCase {

    private Logger logger = Logger.getLogger(this.getClass());

    @Autowired
    UserService userService;

    @Autowired
    private SocieteService societeService;

    @Autowired
    private EspaceService espacesService;

    private Societe societeTests;
    private Utilisateur admin;

    @Before
    public void createSociete() throws RestClientException, AlfrescoServiceException {
        admin = userService.login("admin", "admin");
        societeTests = societeService.creerNouvelle(admin, new Societe("societe" + new Random().nextInt(1000), societeService.listerOffresCommerciales(admin).get(0)));
    }

    @After
    public void deleteSociete() throws RestClientException, AlfrescoServiceException {
        societeService.supprimer(admin, societeTests);
    }

    @Test
    public void testCreationEspace() throws RestClientException, AlfrescoServiceException {

        Espace eTocreate = new Espace("espace1", societeTests);
        Espace eCreated = espacesService.creerNouveau(admin, eTocreate);
        assertNotNull("erreur de creation de l'espace 'espace1'", eCreated);

    }

    @Test
    public void testCreationSousEspace() throws RestClientException, AlfrescoServiceException {

        Espace eCreated = espacesService.creerNouveau(admin, new Espace("espaceParent", societeTests));
        assertNotNull("erreur de creation de l'espace 'espace Parent'", eCreated);

        Espace eEnfant = espacesService.creerNouveau(admin, new Espace("espaceEnfant", eCreated));
        assertNotNull("erreur de creation de l'espace 'espace Enfant'", eEnfant);

    }

    @Test
    public void testListEspaces() throws RestClientException, AlfrescoServiceException {

        Espace eCreated = espacesService.creerNouveau(admin, new Espace("espaceParent", societeTests));
        espacesService.creerNouveau(admin, new Espace("espaceEnfant", eCreated));

        assertEquals("Aucun espace dans la société : " + societeTests.getNom(), 2, espacesService.listEspaces(admin, societeTests).size());

    }

    @Test
    public void testchangeActiveEspaces() throws RestClientException, AlfrescoServiceException {

        Espace eCreated = espacesService.creerNouveau(admin, new Espace("espaceParent", societeTests));

        espacesService.creerNouveau(admin, new Espace("espaceEnfant", eCreated));

        for (Espace e : espacesService.listEspaces(admin, societeTests)) {
            assertTrue("les espaces doivent etre actifs à l'initialisation", e.getActive());
            espacesService.desactiver(admin, e);
        }

        for (Espace e : espacesService.listEspaces(admin, societeTests)) {
            assertTrue("les espaces devraient tous etre désactivés", !e.getActive());
            espacesService.activer(admin, e);
        }

        for (Espace e : espacesService.listEspaces(admin, societeTests)) {
            assertTrue("les espaces devraient tous etre réactivés", e.getActive());
        }

    }

    @Test
    public void testListeArboEspaces() throws RestClientException, AlfrescoServiceException {

        Espace eParent1 = espacesService.creerNouveau(admin, new Espace("espaceParent1", societeTests));

        espacesService.creerNouveau(admin, new Espace("espaceEnfant11", eParent1));

        espacesService.creerNouveau(admin, new Espace("espaceEnfant12", eParent1));

        System.out.println("Societe Node Ref = " + societeTests);
        Espace eParent2 = espacesService.creerNouveau(admin, new Espace("espaceParent2", societeTests));

        espacesService.creerNouveau(admin, new Espace("espaceEnfant21", eParent2));

        espacesService.creerNouveau(admin, new Espace("espaceEnfant22", eParent2));

        List<Espace> lstArboEspaces = espacesService.listEspacesArbo(admin, societeTests);

        assertEquals(2, lstArboEspaces.size());

        for (Espace e : lstArboEspaces) {
            assertEquals(2, e.getFils().size());
        }

    }

}
