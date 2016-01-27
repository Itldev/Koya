package fr.itldev.koya.model.json.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.alfresco.util.Pair;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.interfaces.KoyaContent;
import fr.itldev.koya.model.json.PaginatedContentList;

public class PaginatedContentListDeserializer extends
		JsonDeserializer<PaginatedContentList> {

	private static TypeReference<List<KoyaNode>> TYPEREF_LSTKOYANODES = new TypeReference<List<KoyaNode>>() {
	};

	@SuppressWarnings("unchecked")
	@Override
	public PaginatedContentList deserialize(JsonParser jp,
			DeserializationContext dc) throws IOException,
			JsonProcessingException {

		PaginatedContentList pcl = new PaginatedContentList();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(jp);

		pcl.setChildren((List<KoyaContent>) mapper.readValue(node.get("children"),
				TYPEREF_LSTKOYANODES));

		HashMap<String, String> tm = mapper.readValue(node.get("totalValues"),
				new TypeReference<HashMap<String, String>>() {
				});

		Pair<Integer, Integer> p = new Pair<Integer, Integer>(
				Integer.valueOf(tm.get("first").toString()), Integer.valueOf(tm
						.get("second").toString()));
		pcl.setTotalValues(p);
		return pcl;
	}
}
