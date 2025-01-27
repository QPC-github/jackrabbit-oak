/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.jackrabbit.oak.index;

import org.apache.jackrabbit.oak.plugins.index.IndexConstants;
import org.junit.After;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.File;
import java.io.IOException;

import static org.apache.jackrabbit.commons.JcrUtils.getOrCreateByPath;

public abstract class AbstractIndexTestCommand {

    public static final String TEST_INDEX_PATH = "/oak:index/fooIndex";
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder(new File("target"));
    protected IndexRepositoryFixture fixture;

    @After
    public void cleaup() throws IOException {
        if (fixture != null) {
            fixture.close();
        }
    }

    protected void createTestData(boolean asyncIndex) throws IOException, RepositoryException {
        createTestData("/testNode/a", "foo", 100, "nt:base", asyncIndex);
    }

    protected void createTestData(String basePath, String propName, int count, String nodeType, boolean asyncIndex)
            throws IOException, RepositoryException {
        if (fixture == null) {
            this.fixture = getRepositoryFixture(temporaryFolder.newFolder());
        }
        indexIndexDefinitions();
        createIndex(nodeType, propName, asyncIndex);
        addTestContent(fixture, basePath, propName, count);
    }

    protected void addTestContent(IndexRepositoryFixture fixture, String basePath, String propName, int count)
            throws IOException, RepositoryException {
        Session session = fixture.getAdminSession();
        for (int i = 0; i < count; i++) {
            getOrCreateByPath(basePath+i,
                    "oak:Unstructured", session).setProperty(propName, "bar");
        }
        session.save();
        session.logout();
    }

    private void indexIndexDefinitions() throws IOException, RepositoryException {
        //By default Oak index definitions are not indexed
        //so add them to declaringNodeTypes
        Session session = fixture.getAdminSession();
        Node nodeType = session.getNode("/oak:index/nodetype");
        nodeType.setProperty(IndexConstants.DECLARING_NODE_TYPES, new String[] {"oak:QueryIndexDefinition"}, PropertyType.NAME);
        session.save();
        session.logout();
    }

    protected abstract IndexRepositoryFixture getRepositoryFixture(File dir);

    protected abstract void createIndex(String nodeType, String propName, boolean asyncIndex) throws IOException,
            RepositoryException;

}
