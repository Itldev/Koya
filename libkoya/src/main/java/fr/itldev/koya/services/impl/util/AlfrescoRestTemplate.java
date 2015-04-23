package fr.itldev.koya.services.impl.util;

import java.io.IOException;
import java.net.URI;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class AlfrescoRestTemplate extends RestTemplate {
	
	public AlfrescoRestTemplate(ClientHttpRequestFactory requestFactory) {
		super(requestFactory);
	}
	
	protected <T> T doExecute(URI url, HttpMethod method,
			RequestCallback requestCallback,
			ResponseExtractor<T> responseExtractor) throws RestClientException {

		Assert.notNull(url, "'url' must not be null");
		Assert.notNull(method, "'method' must not be null");
		ClientHttpResponse response = null;
		try {
			ClientHttpRequest request = createRequest(url, method);
			if (requestCallback != null) {
				requestCallback.doWithRequest(request);
			}
			response = request.execute();
			if (!getErrorHandler().hasError(response)) {
				logResponseStatus(method, url, response);
			} else {
				handleResponseError(method, url, response);
			}
			if (responseExtractor != null) {
				return responseExtractor.extractData(response);
			} else {
				return null;
			}
		} catch (IOException ex) {
			throw new ResourceAccessException("I/O error: " + ex.getMessage(),
					ex);
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	private void logResponseStatus(HttpMethod method, URI url,
			ClientHttpResponse response) {
		if (logger.isDebugEnabled()) {
			try {
				logger.debug(method.name() + " request for \"" + url
						+ "\" resulted in " + response.getStatusCode() + " ("
						+ response.getStatusText() + ")");
			} catch (IOException e) {
				// ignore
			}
		}
	}

	private void handleResponseError(HttpMethod method, URI url,
			ClientHttpResponse response) throws IOException {
		if (logger.isWarnEnabled()
				&& response.getStatusCode() != HttpStatus.FORBIDDEN) {
			try {
				logger.warn(method.name() + " request for \"" + url
						+ "\" resulted in " + response.getStatusCode() + " ("
						+ response.getStatusText()
						+ "); invoking error handler");
			} catch (IOException e) {
				// ignore
			}
		}
		getErrorHandler().handleError(response);
	}

}
