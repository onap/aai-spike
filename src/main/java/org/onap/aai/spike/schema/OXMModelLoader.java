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

import com.google.common.base.CaseFormat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.persistence.dynamic.DynamicType;
import org.eclipse.persistence.internal.oxm.mappings.Descriptor;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.nodes.NodeIngestor;
import org.onap.aai.setup.SchemaVersion;
import org.onap.aai.setup.Translator;
import org.onap.aai.spike.exception.SpikeException;
import org.onap.aai.spike.logging.SpikeMsgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class contains all of the logic for importing OXM model schemas from the available OXM
 * schema files.
 */
@Component
public class OXMModelLoader {

    private static Translator translator;
    private static NodeIngestor nodeIngestor;

    private static Map<String, DynamicJAXBContext> versionContextMap = new ConcurrentHashMap<>();
    private static Map<String, HashMap<String, DynamicType>> xmlElementLookup = new ConcurrentHashMap<>();

    final static Pattern versionPattern = Pattern.compile("(?i)v(\\d*)");

    private static org.onap.aai.cl.api.Logger logger =
            LoggerFactory.getInstance().getLogger(OXMModelLoader.class.getName());

    private OXMModelLoader() {}

    /**
     * This constructor presents an awkward marrying of Spring bean creation and static method use. This
     * is technical debt that will need fixing.
     *
     * @param translator contains schema versions configuration
     * @param nodeIngestor provides DynamicJAXBContext for the OXM version
     */
    @Autowired
    public OXMModelLoader(Translator translator, NodeIngestor nodeIngestor) {
        OXMModelLoader.translator = translator;
        OXMModelLoader.nodeIngestor = nodeIngestor;
    }

    /**
     * Finds all OXM model files
     *
     * @throws SpikeException
     * @throws IOException
     *
     */
    public synchronized static void loadModels() throws SpikeException {
        if (logger.isDebugEnabled()) {
            logger.debug("Loading OXM Models");
        }

        for (SchemaVersion oxmVersion : translator.getSchemaVersions().getVersions()) {
            DynamicJAXBContext jaxbContext = nodeIngestor.getContextForVersion(oxmVersion);
            if (jaxbContext != null) {
                loadModel(oxmVersion.toString(), jaxbContext);
            }
        }
    }

    private synchronized static void loadModel(String oxmVersion, DynamicJAXBContext jaxbContext) {
        versionContextMap.put(oxmVersion, jaxbContext);
        loadXmlLookupMap(oxmVersion, jaxbContext);
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

    public static void loadXmlLookupMap(String version, DynamicJAXBContext jaxbContext) {

        @SuppressWarnings("rawtypes")
        List<Descriptor> descriptorsList = jaxbContext.getXMLContext().getDescriptors();
        HashMap<String, DynamicType> types = new HashMap<String, DynamicType>();

        for (@SuppressWarnings("rawtypes")
        Descriptor desc : descriptorsList) {

            DynamicType entity = jaxbContext.getDynamicType(desc.getAlias());
            String entityName = desc.getDefaultRootElement();
            types.put(entityName, entity);
        }
        xmlElementLookup.put(version, types);
    }

    public static DynamicType getDynamicTypeForVersion(String version, String type) throws SpikeException {

        DynamicType dynamicType;
        // If we haven't already loaded in the available OXM models, then do so now.
        if (versionContextMap == null || versionContextMap.isEmpty()) {
            loadModels();
        } else if (!versionContextMap.containsKey(version)) {
            logger.error(SpikeMsgs.OXM_LOAD_ERROR, "Error loading oxm model: " + version);
            throw new SpikeException("Error loading oxm model: " + version);
        }

        // First try to match the Java-type based on hyphen to camel case
        // translation
        String javaTypeName = CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, type);
        dynamicType = versionContextMap.get(version).getDynamicType(javaTypeName);

        if (xmlElementLookup.containsKey(version)) {
            if (dynamicType == null) {
                // Try to lookup by xml root element by exact match
                dynamicType = xmlElementLookup.get(version).get(type);
            }

            if (dynamicType == null) {
                // Try to lookup by xml root element by lowercase
                dynamicType = xmlElementLookup.get(version).get(type.toLowerCase());
            }

            if (dynamicType == null) {
                // Direct lookup as java-type name
                dynamicType = versionContextMap.get(version).getDynamicType(type);
            }
        }

        return dynamicType;
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
            Matcher matcher = versionPattern.matcher(versionKey);
            if (matcher.find()) {
                versions.add("V" + matcher.group(1));
            }
        }
        return versions;
    }
}
