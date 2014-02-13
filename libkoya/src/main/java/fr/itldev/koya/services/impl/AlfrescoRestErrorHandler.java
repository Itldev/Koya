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

package fr.itldev.koya.services.impl;

import fr.itldev.koya.services.exceptions.AlfrescoAuthenticationException;
import fr.itldev.koya.services.exceptions.AlfrescoServerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

public class AlfrescoRestErrorHandler implements ResponseErrorHandler {

    private final Logger logger = Logger.getLogger(this.getClass());

    private static List<HttpStatus> statusOK = new ArrayList<HttpStatus>(
            Arrays.asList(HttpStatus.OK, HttpStatus.NO_CONTENT));

    @Override
    public void handleError(ClientHttpResponse clienthttpresponse) throws IOException {

        if (clienthttpresponse.getStatusCode() == HttpStatus.FORBIDDEN) {
            throw new AlfrescoAuthenticationException("Erreur 403 : Acces refusé ");
        }

        if (!statusOK.contains(clienthttpresponse.getStatusCode())) {
            throw new AlfrescoServerException("Erreur "
                    + clienthttpresponse.getStatusCode() + " : "
                    + clienthttpresponse.getStatusText());
        }
    }

    @Override
    public boolean hasError(ClientHttpResponse clienthttpresponse) throws IOException {
        if (clienthttpresponse.getStatusCode() != HttpStatus.OK) {
            logger.warn("Status code: " + clienthttpresponse.getStatusCode() + " - " + clienthttpresponse.getBody());
            return true;
        }
        return false;
    }

}
