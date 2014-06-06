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
