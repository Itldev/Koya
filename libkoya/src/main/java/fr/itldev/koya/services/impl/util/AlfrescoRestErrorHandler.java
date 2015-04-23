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
package fr.itldev.koya.services.impl.util;

import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

public class AlfrescoRestErrorHandler implements ResponseErrorHandler {

    private static final Pattern ERRORCODEPATTERN = Pattern.compile("KoyaError\\s*:\\s*(\\d*)");

    private final Logger logger = Logger.getLogger(this.getClass());

    private static final List<HttpStatus> statusOK = new ArrayList<>(
            Arrays.asList(HttpStatus.OK, HttpStatus.NO_CONTENT));

    @Override
    public void handleError(ClientHttpResponse clienthttpresponse) throws IOException {

        if (!statusOK.contains(clienthttpresponse.getStatusCode())) {
            AlfrescoServiceException ex;
            if (clienthttpresponse.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
                java.util.Scanner s = new java.util.Scanner(clienthttpresponse.getBody()).useDelimiter("\\A");
                String message = s.hasNext() ? s.next() : "";

                /*
                 Try to get any Koya Error code if exists
                 */
                Integer koyaErrorCode = null;

                Matcher matcher = ERRORCODEPATTERN.matcher(message);
                if (matcher.find()) {
                    koyaErrorCode = Integer.valueOf(matcher.group(1));
                }

                ex = new AlfrescoServiceException("Erreur "
                        + clienthttpresponse.getStatusCode() + " : "
                        + clienthttpresponse.getStatusText(), koyaErrorCode);
                ex.setHttpErrorCode(clienthttpresponse.getStatusCode().value());
            } else if (clienthttpresponse.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
                ex = new AlfrescoServiceException("Acces Denied");
                ex.setHttpErrorCode(clienthttpresponse.getStatusCode().value());

            } else {
                ex = new AlfrescoServiceException();
                ex.setHttpErrorCode(clienthttpresponse.getStatusCode().value());
                throw ex;
            }
            throw ex;

        }
    }

	@Override
	public boolean hasError(ClientHttpResponse clienthttpresponse)
			throws IOException {
		HttpStatus responseStatus = clienthttpresponse.getStatusCode();

		if (responseStatus == HttpStatus.OK) {
			return false;
		}
		
		if (responseStatus == HttpStatus.FORBIDDEN) {		
			return true;
		}

		logger.warn("Status code: " + clienthttpresponse.getStatusCode()
				+ " - " + clienthttpresponse.getBody());
		return true;
	}

}
