package fr.itldev.koya.model.json;

import java.util.List;

import org.alfresco.util.Pair;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.services.impl.util.PaginatedContentListDeserializer;

@JsonDeserialize(using = PaginatedContentListDeserializer.class)
public class PaginatedContentList {

	private List<SecuredItem> children;
	private Pair<Integer, Integer> totalValues;

	public List<SecuredItem> getChildren() {
		return children;
	}

	public void setChildren(List<SecuredItem> children) {
		this.children = children;
	}

	public Pair<Integer, Integer> getTotalValues() {
		return totalValues;
	}

	public void setTotalValues(Pair<Integer, Integer> totalValues) {
		this.totalValues = totalValues;
	}

}
