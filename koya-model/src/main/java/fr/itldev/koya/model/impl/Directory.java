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
package fr.itldev.koya.model.impl;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.interfaces.KoyaContent;

public final class Directory extends KoyaNode implements KoyaContent{

	@JsonProperty("childdir")
	private List<Directory> childDir = new ArrayList<>();

	@JsonProperty("childdoc")
	private List<Document> childDoc = new ArrayList<>();

	@JsonIgnore
	public List<KoyaContent> getChildren() {
		List<KoyaContent> content = new ArrayList<>();
		content.addAll(childDir);
		content.addAll(childDoc);
		return content;
	}

	public void setChildren(List<KoyaNode> children) {
		for (KoyaNode c : children) {
			if (Directory.class.isAssignableFrom(c.getClass())) {
				childDir.add((Directory) c);
			} else if (Document.class.isAssignableFrom(c.getClass())) {
				childDoc.add((Document) c);
			}
		}
	}

	// <editor-fold defaultstate="collapsed" desc="Getters/Setters">
	public List<Directory> getChildDir() {
		return childDir;
	}

	public void setChildDir(List<Directory> childDir) {
		this.childDir = childDir;
	}

	public List<Document> getChildDoc() {
		return childDoc;
	}

	public void setChildDoc(List<Document> childDoc) {
		this.childDoc = childDoc;
	}

	// </editor-fold>

	private Directory() {
	}
	
	public static Directory newInstance() {
		return new Directory();
	}
	
	public static Directory newInstance(String title) {
		Directory d = new Directory();
		d.setTitle(title);
		return d;
	}

}
