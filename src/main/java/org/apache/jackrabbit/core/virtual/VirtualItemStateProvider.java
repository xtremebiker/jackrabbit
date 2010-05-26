/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.core.virtual;

import org.apache.jackrabbit.core.ItemId;
import org.apache.jackrabbit.core.NodeId;
import org.apache.jackrabbit.core.state.ItemStateManager;
import org.apache.jackrabbit.core.state.NodeReferences;
import org.apache.jackrabbit.core.state.ItemStateListener;
import org.apache.jackrabbit.spi.Name;

import javax.jcr.RepositoryException;

/**
 * This Interface defines a virtual item state provider.
 */
public interface VirtualItemStateProvider extends ItemStateManager {

    /**
     * Checks if the id refers to the root of a virtual tree.
     *
     * @param id
     * @return <code>true</code> if it is the root
     */
    boolean isVirtualRoot(ItemId id);

    /**
     * Returns the id of the root node of the virtual tree.
     *
     * @return the id of the root node of the virtual tree.
     */
    NodeId getVirtualRootId();

    /**
     * Creats a new virtual property state
     *
     * @param parent
     * @param name
     * @param type
     * @param multiValued
     * @return
     * @throws RepositoryException
     */
    VirtualPropertyState createPropertyState(VirtualNodeState parent,
                                             Name name, int type,
                                             boolean multiValued)
            throws RepositoryException;

    /**
     * Creates a new virtual node state
     *
     * @param parent
     * @param name
     * @param id
     * @param nodeTypeName
     * @return
     * @throws RepositoryException
     */
    VirtualNodeState createNodeState(VirtualNodeState parent, Name name,
                                     NodeId id, Name nodeTypeName)
        throws RepositoryException;

    /**
     * Informs this provider that the node references to one of its states has
     * changed.
     *
     * @param refs
     * @return <code>true</code> if the reference target is one of its items.
     */
    boolean setNodeReferences(NodeReferences refs);


    /**
     * Add an <code>ItemStateListener</code>
     * @param listener the new listener to be informed on modifications
     */
    public void addListener(ItemStateListener listener);

    /**
     * Remove an <code>ItemStateListener</code>
     *
     * @param listener an existing listener
     */
    public void removeListener(ItemStateListener listener);
}