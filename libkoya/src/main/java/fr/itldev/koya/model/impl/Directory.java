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

package fr.itldev.koya.model.impl;

import fr.itldev.koya.model.Container;
import fr.itldev.koya.model.Content;
import fr.itldev.koya.model.SecuredItem;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.annotate.JsonIgnore;

public final class Directory extends Content implements Container {

    @JsonIgnore
    private List<Content> children = new ArrayList<>();

    private Long byteSize;

    @Override
    public List<Content> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<? extends SecuredItem> children) {
        this.children = (List<Content>) children;
    }

    public Directory() {
    }

    public Directory(String name, Directory pere) {
        super(name, pere);
    }

    public Directory(String name, Dossier pere) {
        super(name, pere);
    }

    public Directory(String nodeRef, String path, String name, String parentNodeRef) {
        super(nodeRef, path, name, parentNodeRef);
    }

    @Override
    public Long getByteSize() {
        return byteSize;
    }

    @Override
    public void setByteSize(Long tailleOctets) {
        this.byteSize = tailleOctets;
    }

}
