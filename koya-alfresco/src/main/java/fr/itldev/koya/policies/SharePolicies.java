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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.itldev.koya.policies;

import fr.itldev.koya.model.KoyaModel;
import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public interface SharePolicies {

    public static final String NAMESPACE = KoyaModel.KOYA_URI;

    public interface BeforeSharePolicy extends ClassPolicy {

        public static final String NAMESPACE = KoyaModel.KOYA_URI;

        public static final QName QNAME = QName.createQName(KoyaModel.KOYA_URI, "beforeShareItem");

        /**
         * Called before an item is shared.
         *
         * @param nodeRef the reference to the item about to be shared
         * @param username username the item is about to be shared whith
         */
        public void beforeShareItem(NodeRef nodeRef, String username);
    }

    public interface AfterSharePolicy extends ClassPolicy {

        public static final String NAMESPACE = KoyaModel.KOYA_URI;

        public static final QName QNAME = QName.createQName(KoyaModel.KOYA_URI, "afterShareItem");

        /**
         * Called after an item has been shared.
         *
         * @param nodeRef the reference to the item has been shared
         * @param username username the item has been shared whith
         */
        public void afterShareItem(NodeRef nodeRef, String username);
    }

    public interface BeforeUnsharePolicy extends ClassPolicy {

        public static final String NAMESPACE = KoyaModel.KOYA_URI;

        public static final QName QNAME = QName.createQName(KoyaModel.KOYA_URI, "beforeUnshareItem");

        /**
         * Called before an item is unshared.
         *
         * @param nodeRef the reference to the item about to be unshared
         * @param username username the item is about to be unshared from
         */
        public void beforeUnshareItem(NodeRef nodeRef, String username);
    }

    public interface AfterUnsharePolicy extends ClassPolicy {

        public static final String NAMESPACE = KoyaModel.KOYA_URI;

        public static final QName QNAME = QName.createQName(KoyaModel.KOYA_URI, "afterUnshareItem");

        /**
         * Called after an item has been unshared.
         *
         * @param nodeRef the reference to the item has been unshared
         * @param username username the item is about has been unshared from
         */
        public void afterUnshareItem(NodeRef nodeRef, String username);
    }
}
