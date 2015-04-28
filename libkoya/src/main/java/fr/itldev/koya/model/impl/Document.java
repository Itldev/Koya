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

import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.interfaces.KoyaContent;

public final class Document extends KoyaNode implements KoyaContent{

    private Long byteSize;
    private String mimeType;

    private Document() {
    }

    public Long getByteSize() {
        return byteSize;
    }

    public void setByteSize(Long tailleOctets) {
        this.byteSize = tailleOctets;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }   

    public static Document newInsance() {
        return new Document();
    }

}
