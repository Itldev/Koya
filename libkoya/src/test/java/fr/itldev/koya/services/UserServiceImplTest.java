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

import fr.itldev.koya.model.impl.User;
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
    public void testAdminLogin() throws AlfrescoServiceException {
        User admin = null;
        try {
            admin = userService.login("admin", "admin");
        } catch (RestClientException ex) {
            fail();
        }
        assertNotNull(admin);
    }

    @Test(expected = AlfrescoAuthenticationException.class)
    public void testUnknownLogin() throws Exception {
        User userUnknown = userService.login("unknown", "unknown");
    }

    @Test
    public void testGetPrefs() throws IOException, AlfrescoServiceException {

        User admin = null;
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
        User admin = null;
        try {
            admin = userService.login("admin", "admin");
        } catch (RestClientException ex) {
            fail();
        }
        int nbPrefs = admin.getPreferences().size();
        System.out.println(admin.getPreferences());

        /* =============== Add a test preference ===========*/
        admin.getPreferences().put(testKey, "OK_PREF");
        userService.commitPreferences(admin);

        //one more preference
        assertEquals(nbPrefs + 1, admin.getPreferences().size());
        //   System.out.println(admin.getPreferences());

        /* =============== Del test preference =====*/
        admin.getPreferences().remove(testKey);
        userService.commitPreferences(admin);

        assertEquals(nbPrefs, admin.getPreferences().size());
        /* ============= final state ============= */
        userService.loadPreferences(admin);
        //   System.out.println(admin.getPreferences());

    }

    @Test
    public void testModifyDetails() throws IOException, AlfrescoServiceException {
        User userTest = userService.login("admin", "admin");

        userTest.setName("tester");

        try {
            userService.commitProperties(userTest);
        } catch (AlfrescoServiceException ex) {
            fail();
        }
    }
    @Test
    public void testFindUsers() throws IOException, AlfrescoServiceException {
        User user = userService.login("admin", "admin");

        try {
            userService.find(user, "l", 10);
        } catch (AlfrescoServiceException ex) {
            fail();
        }
    }
}
