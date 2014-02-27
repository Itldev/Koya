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

package fr.itldev.koya.testsuite;

import fr.itldev.koya.services.DossierServiceImplTest;
import fr.itldev.koya.services.CompanyServiceImplTest;
import fr.itldev.koya.services.KoyaContentServiceImplTest;
import fr.itldev.koya.services.SpaceServiceImplTest;
import fr.itldev.koya.services.UserServiceImplTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.test.context.ContextConfiguration;

@RunWith(Suite.class)
@SuiteClasses({UserServiceImplTest.class, CompanyServiceImplTest.class,
    SpaceServiceImplTest.class, DossierServiceImplTest.class, KoyaContentServiceImplTest.class})
@ContextConfiguration(locations = "classpath:koya-services-tests.xml")

public class AllTests {

}
