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

import fr.itldev.koya.model.impl.Utilisateur;
import fr.itldev.koya.services.exceptions.AlfrescoAuthenticationException;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.io.IOException;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestClientException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:koya-services-tests.xml")
public class UserServiceImplTest extends TestCase {

    private Logger logger = Logger.getLogger(this.getClass());

    @Autowired
    private UserService userService;

    @Test
    public void testAdminLogin() {
        Utilisateur admin = null;
        try {
            admin = userService.login("admin", "admin");
        } catch (RestClientException ex) {
            fail();
        }
        assertNotNull(admin);
        assertTrue(admin.isAdmin());
    }

    @Test(expected = AlfrescoAuthenticationException.class)
    public void testUnknownLogin() throws Exception {
        Utilisateur userUnknown = userService.login("unknown", "unknown");
    }

    @Test
    public void testGetPrefs() throws IOException {

        Utilisateur admin = null;
        try {
            admin = userService.login("admin", "admin");
        } catch (RestClientException ex) {
            fail();
        }
        System.out.println(admin.getPreferences());

    }

    @Test
    public void testSetPrefs() throws IOException, AlfrescoServiceException {
        String testKey = "fr.itldev.test";
        Utilisateur admin = null;
        try {
            admin = userService.login("admin", "admin");
        } catch (RestClientException ex) {
            fail();
        }
        int nbPrefs = admin.getPreferences().size();
        System.out.println(admin.getPreferences());

        /* =============== Ajout d'un préférence de tests ===========*/
        admin.getPreferences().put(testKey, "OK_PREF");
        userService.commitPreferences(admin);

        //on a une préférence en +
        assertEquals(nbPrefs + 1, admin.getPreferences().size());
        System.out.println(admin.getPreferences());

        /* =============== Suppression de la preference de tests =====*/
        admin.getPreferences().remove(testKey);
        userService.commitPreferences(admin);

        assertEquals(nbPrefs, admin.getPreferences().size());
        /* ============= etat final ============= */
        userService.updatePreferences(admin);
        System.out.println(admin.getPreferences());

    }
}
