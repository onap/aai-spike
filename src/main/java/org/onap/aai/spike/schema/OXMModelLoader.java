/**
 * ﻿============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * Copyright © 2017-2018 Amdocs
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.spike.schema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.nodes.NodeIngestor;
import org.onap.aai.setup.ConfigTranslator;
import org.onap.aai.setup.SchemaLocationsBean;
import org.onap.aai.setup.Version;
import org.onap.aai.spike.exception.SpikeException;
import org.onap.aai.spike.logging.SpikeMsgs;
import org.onap.aai.spike.util.SchemaIngestPropertiesReader;

/**
 * This class contains all of the logic for importing OXM model schemas from the available OXM
 * schema files.
 */
public class OXMModelLoader {

    private static Map<String, DynamicJAXBContext> versionContextMap =
            new ConcurrentHashMap<String, DynamicJAXBContext>();

    final static Pattern p = Pattern.compile("aai_oxm_(.*).xml");
    final static Pattern versionPattern = Pattern.compile("V(\\d*)");

    private static org.onap.aai.cl.api.Logger logger =
            LoggerFactory.getInstance().getLogger(OXMModelLoader.class.getName());

    /**
     * Finds all OXM model files
     * 
     * @throws SpikeException
     * @throws IOException
     *
     */
    public synchronized static void loadModels() throws SpikeException {
        SchemaIngestPropertiesReader schemaIngestPropReader = new SchemaIngestPropertiesReader();
        SchemaLocationsBean schemaLocationsBean = new SchemaLocationsBean();
        schemaLocationsBean.setNodeDirectory(schemaIngestPropReader.getNodeDir());
        schemaLocationsBean.setEdgeDirectory(schemaIngestPropReader.getEdgeDir());
        ConfigTranslator configTranslator = new OxmConfigTranslator(schemaLocationsBean);
        NodeIngestor nodeIngestor = new NodeIngestor(configTranslator);

        if (logger.isDebugEnabled()) {
            logger.debug("Loading OXM Models");
        }

        for (Version oxmVersion : Version.values()) {
            DynamicJAXBContext jaxbContext = nodeIngestor.getContextForVersion(oxmVersion);
            if (jaxbContext != null) {
                loadModel(oxmVersion.toString(), jaxbContext);
            }
        }
    }


    private synchronized static void loadModel(String oxmVersion, DynamicJAXBContext jaxbContext) {
        versionContextMap.put(oxmVersion, jaxbContext);
        logger.info(SpikeMsgs.LOADED_OXM_FILE, oxmVersion);
    }

    /**
     * Retrieves the JAXB context for the specified OXM model version.
     *
     * @param version - The OXM version that we want the JAXB context for.
     *
     * @return - A JAXB context derived from the OXM model schema.
     *
     * @throws SpikeException
     */
    public static DynamicJAXBContext getContextForVersion(String version) throws SpikeException {

        // If we haven't already loaded in the available OXM models, then do so now.
        if (versionContextMap == null || versionContextMap.isEmpty()) {
            loadModels();
        } else if (!versionContextMap.containsKey(version)) {
            throw new SpikeException("Error loading oxm model: " + version);
        }

        return versionContextMap.get(version);
    }

    public static String getLatestVersion() throws SpikeException {

        // If we haven't already loaded in the available OXM models, then do so now.
        if (versionContextMap == null || versionContextMap.isEmpty()) {
            loadModels();
        }

        // If there are still no models available, then there's not much we can do...
        if (versionContextMap.isEmpty()) {
            throw new SpikeException("No available OXM schemas to get latest version for.");
        }

        // Iterate over the available model versions to determine which is the most
        // recent.
        Integer latestVersion = null;
        String latestVersionStr = null;
        for (String versionKey : versionContextMap.keySet()) {

            Matcher matcher = versionPattern.matcher(versionKey);
            if (matcher.find()) {

                int currentVersion = Integer.valueOf(matcher.group(1));

                if ((latestVersion == null) || (currentVersion > latestVersion)) {
                    latestVersion = currentVersion;
                    latestVersionStr = versionKey;
                }
            }
        }

        return latestVersionStr;
    }

    /**
     * Retrieves the map of all JAXB context objects that have been created by importing the available
     * OXM model schemas.
     *
     * @return - Map of JAXB context objects.
     */
    public static Map<String, DynamicJAXBContext> getVersionContextMap() {
        return versionContextMap;
    }

    /**
     * Assigns the map of all JAXB context objects.
     *
     * @param versionContextMap
     */
    public static void setVersionContextMap(Map<String, DynamicJAXBContext> versionContextMap) {
        OXMModelLoader.versionContextMap = versionContextMap;
    }

    /**
     * Retrieves the list of all Loaded OXM versions.
     *
     * @return - A List of Strings of all loaded OXM versions.
     *
     * @throws SpikeException
     */
    public static List<String> getLoadedOXMVersions() throws SpikeException {
        // If we haven't already loaded in the available OXM models, then do so now.
        if (versionContextMap == null || versionContextMap.isEmpty()) {
            loadModels();
        }
        // If there are still no models available, then there's not much we can do...
        if (versionContextMap.isEmpty()) {
            logger.error(SpikeMsgs.OXM_LOAD_ERROR, "No available OXM schemas to get versions for.");
            throw new SpikeException("No available OXM schemas to get latest version for.");
        }
        List<String> versions = new ArrayList<String>();
        for (String versionKey : versionContextMap.keySet()) {
            Matcher matcher = versionPattern.matcher(versionKey.toUpperCase());
            if (matcher.find()) {
                versions.add("V" + matcher.group(1));
            }
        }
        return versions;
    }
}