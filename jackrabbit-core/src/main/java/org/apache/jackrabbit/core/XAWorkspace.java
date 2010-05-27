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
package org.apache.jackrabbit.core;

import org.apache.jackrabbit.core.config.WorkspaceConfig;
import org.apache.jackrabbit.core.state.SharedItemStateManager;
import org.apache.jackrabbit.core.state.XAItemStateManager;
import org.apache.jackrabbit.core.state.LocalItemStateManager;

/**
 * Workspace extension that works in an XA environment.
 */
public class XAWorkspace extends WorkspaceImpl {

    /**
     * Protected constructor.
     *
     * @param wspConfig The workspace configuration
     * @param stateMgr  The shared item state manager
     * @param repositoryContext repository context
     * @param session   The session
     */
    protected XAWorkspace(
            WorkspaceConfig wspConfig, SharedItemStateManager stateMgr,
            RepositoryContext repositoryContext, SessionImpl session) {
        super(wspConfig, stateMgr, repositoryContext, session);
    }

    /**
     * {@inheritDoc}
     */
    protected LocalItemStateManager createItemStateManager(SharedItemStateManager shared) {
        return XAItemStateManager.createInstance(shared, this, null, rep.getItemStateCacheFactory());
    }
}