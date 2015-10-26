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

package fr.itldev.koya.services.exceptions;

import org.springframework.web.client.RestClientException;

public class AlfrescoServiceException extends RestClientException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer koyaErrorCode;
	private Integer httpErrorCode;

	public Integer getKoyaErrorCode() {
		return koyaErrorCode;
	}

	public Integer getHttpErrorCode() {
		return httpErrorCode;
	}

	public void setHttpErrorCode(Integer httpErrorCode) {
		this.httpErrorCode = httpErrorCode;
	}

	public AlfrescoServiceException() {
		super("");
	}

	public AlfrescoServiceException(String message) {
		super(message);
	}

	public AlfrescoServiceException(String string, Throwable ex) {
		super(string, ex);
	}

	public AlfrescoServiceException(Integer koyaErrorCode) {
		super("");
		this.koyaErrorCode = koyaErrorCode;
	}

	public AlfrescoServiceException(String string, Integer koyaErrorCode) {
		super(string);
		this.koyaErrorCode = koyaErrorCode;
	}

	public AlfrescoServiceException(String string, Throwable ex,
			Integer koyaErrorCode) {
		super(string, ex);
		this.koyaErrorCode = koyaErrorCode;
	}

}
