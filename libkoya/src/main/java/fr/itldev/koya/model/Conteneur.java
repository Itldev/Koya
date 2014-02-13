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

package fr.itldev.koya.model;

import java.util.List;

/**
 * Interface définssant les objets typés koya ayant des fils de type
 * ElementSecurisé.
 *
 * Cette notion n'est pas retransctite coté Alfresco car elle est implicite.
 *
 */
public interface Conteneur {

    List<? extends ElementSecurise> getFils();

    void setFils(List<? extends ElementSecurise> fils);

    String getNodeRef();

    Long getTailleOctets();

    void setTailleOctets(Long tailleOctets);
}
