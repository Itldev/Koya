package fr.itldev.koya.model.json;

import java.util.List;

import org.alfresco.util.Pair;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import fr.itldev.koya.model.interfaces.KoyaContent;
import fr.itldev.koya.model.json.util.PaginatedContentListDeserializer;

@JsonDeserialize(using = PaginatedContentListDeserializer.class)
public class PaginatedContentList {

	private List<KoyaContent> children;
	private Pair<Integer, Integer> totalValues;

	public List<KoyaContent> getChildren() {
		return children;
	}

	public void setChildren(List<KoyaContent> children) {
		this.children = children;
	}

	public Pair<Integer, Integer> getTotalValues() {
		return totalValues;
	}

	public void setTotalValues(Pair<Integer, Integer> totalValues) {
		this.totalValues = totalValues;
	}

}
