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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.itldev.koya.repo.security.authentication;

import org.alfresco.repo.security.authentication.UserNameGenerator;
import org.apache.commons.lang.RandomStringUtils;

/**
 *
 * @author nico
 */
public class EmailBasedNameGenerator implements UserNameGenerator {

    @Override
    public String generateUserName(String firstName, String lastName, String emailAddress, int seed) {

        String userName = emailAddress.toLowerCase().trim().replaceAll("[^a-z1-9]", "_");
        if (seed > 0) {
            userName = userName + RandomStringUtils.randomNumeric(3);

        }
        return userName;
    }

}
