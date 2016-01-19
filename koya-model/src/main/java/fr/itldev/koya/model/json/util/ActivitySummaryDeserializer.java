package fr.itldev.koya.model.json.util;

import java.io.IOException;
import java.util.HashMap;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;

public class ActivitySummaryDeserializer extends
		JsonDeserializer<HashMap<String, String>> {
	@SuppressWarnings("deprecation")
	@Override
	public HashMap<String, String> deserialize(JsonParser jp,
			DeserializationContext ctxt) throws IOException,
			JsonProcessingException {

		JsonNode node = new ObjectMapper().readTree(jp);
		String inSummary = node.toString().replace("\"{", "{")
				.replace("}\"", "}").replace("\\\"", "\"");

		return new ObjectMapper().readValue(inSummary,
				TypeFactory.mapType(HashMap.class, String.class, String.class));

	}
}
