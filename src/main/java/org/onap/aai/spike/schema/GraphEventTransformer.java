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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.google.common.base.CaseFormat;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.eclipse.persistence.dynamic.DynamicType;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.oxm.XMLField;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.spike.event.incoming.GizmoEdge;
import org.onap.aai.spike.event.incoming.GizmoGraphEvent;
import org.onap.aai.spike.event.incoming.GizmoVertex;
import org.onap.aai.spike.exception.SpikeException;

/**
 * This class is responsible for transforming raw graph entities (such as vertices and edges) into
 * representations which correspond to the OXM models.
 */
public class GraphEventTransformer {

    private static org.onap.aai.cl.api.Logger logger =
            LoggerFactory.getInstance().getLogger(GraphEventTransformer.class.getName());
    private static final String AAI_UUID = "aai-uuid";

    /**
     * 
     * @param rawVertex
     * @throws SpikeException
     */
    public static void validateVertexModel(GizmoVertex rawVertex) throws SpikeException {

        validateVertexModel(OXMModelLoader.getLatestVersion(), rawVertex);
    }

    public static void populateUUID(GizmoGraphEvent event) throws SpikeException {
        try {
            if (event.getVertex() != null) {
                if (event.getVertex().getProperties().getAsJsonObject().has(AAI_UUID)) {
                    event.getVertex()
                            .setId(event.getVertex().getProperties().getAsJsonObject().get(AAI_UUID).getAsString());
                }
            } else if (event.getRelationship() != null) {
                if (event.getRelationship().getProperties().getAsJsonObject().has(AAI_UUID)) {
                    event.getRelationship().setId(
                            event.getRelationship().getProperties().getAsJsonObject().get(AAI_UUID).getAsString());
                }

                if (event.getRelationship().getSource().getProperties().getAsJsonObject().has(AAI_UUID)) {
                    event.getRelationship().getSource().setId(event.getRelationship().getSource().getProperties()
                            .getAsJsonObject().get(AAI_UUID).getAsString());
                }
                if (event.getRelationship().getTarget().getProperties().getAsJsonObject().has(AAI_UUID)) {
                    event.getRelationship().getTarget().setId(event.getRelationship().getTarget().getProperties()
                            .getAsJsonObject().get(AAI_UUID).getAsString());
                }
            }
        } catch (Exception ex) {
            throw new SpikeException("Unable to parse uuid in incoming event");
        }
    }

    /**
     * 
     * @param version
     * @param rawVertex
     * @throws SpikeException
     */
    public static void validateVertexModel(String version, GizmoVertex rawVertex) throws SpikeException {

        try {

            DynamicJAXBContext jaxbContext = OXMModelLoader.getContextForVersion(version);
            String modelObjectClass = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL,
                    CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, rawVertex.getType()));
            final DynamicType modelObjectType = jaxbContext.getDynamicType(modelObjectClass);
            final DynamicType reservedType = jaxbContext.getDynamicType("ReservedPropNames");

            Set<Map.Entry<String, JsonElement>> vertexEntriesSet =
                    rawVertex.getProperties().getAsJsonObject().entrySet();
            Map<String, JsonElement> vertexEntriesMap = new HashMap<String, JsonElement>();
            for (Map.Entry<String, JsonElement> entry : vertexEntriesSet) {
                vertexEntriesMap.put(entry.getKey(), entry.getValue());
            }

            JsonObject modelJsonElement = new JsonObject();
            // Iterate over all of the attributes specified in the model schema,
            // populating
            // our dynamic instance with the corresponding values supplied in
            // our raw vertex.
            for (DatabaseMapping mapping : modelObjectType.getDescriptor().getMappings()) {
                if (mapping.isAbstractDirectMapping()) {
                    DatabaseField f = mapping.getField();
                    String keyName = f.getName().substring(0, f.getName().indexOf("/"));

                    String defaultValue = mapping.getProperties().get("defaultValue") == null ? ""
                            : mapping.getProperties().get("defaultValue").toString();

                    if (((XMLField) f).isRequired() && !vertexEntriesMap.containsKey(keyName)
                            && !defaultValue.isEmpty()) {
                        modelJsonElement.addProperty(keyName, defaultValue);

                    }
                    // If this is a required field, but is not present in the
                    // raw vertex, reject this
                    // as an invalid input since we can't build a valid object
                    // from what we were provided.
                    if (((XMLField) f).isRequired() && !vertexEntriesMap.containsKey(keyName)
                            && defaultValue.isEmpty()) {
                        throw new SpikeException("Missing required field: " + keyName);
                    }

                    // If this is a non-required field, then set it if a value
                    // was provided in the
                    // raw vertex.
                    if (vertexEntriesMap.containsKey(keyName)) {
                        validateFieldType(vertexEntriesMap.get(keyName), f.getType());
                        modelJsonElement.add(keyName, vertexEntriesMap.get(keyName));
                    }
                }
            }

            // Ensure any of the reserved properties are added to the payload
            for (DatabaseMapping mapping : reservedType.getDescriptor().getMappings()) {
                if (mapping.isAbstractDirectMapping()) {
                    DatabaseField field = mapping.getField();
                    String keyName = field.getName().substring(0, field.getName().indexOf("/"));

                    if (vertexEntriesMap.containsKey(keyName)) {
                        validateFieldType(vertexEntriesMap.get(keyName), field.getType());
                        modelJsonElement.add(keyName, vertexEntriesMap.get(keyName));
                    }
                }
            }

            rawVertex.setProperties(modelJsonElement);
        } catch (Exception e) {
            throw new SpikeException(e.getMessage());
        }
    }

    /**
     * 
     * @param rawEdge
     * @throws SpikeException
     */
    public static void validateEdgeModel(GizmoEdge rawEdge) throws SpikeException {

        validateEdgeModel(EdgeRulesLoader.getLatestSchemaVersion(), rawEdge);
    }

    /**
     * 
     * @param version
     * @param rawEdge
     * @throws SpikeException
     */
    public static void validateEdgeModel(String version, GizmoEdge rawEdge) throws SpikeException {

        if (logger.isDebugEnabled()) {
            logger.debug("Convert edge: " + rawEdge.toString() + " to model version: " + version);
        }

        // Get the relationship schema for the supplied version.
        RelationshipSchema schema = EdgeRulesLoader.getSchemaForVersion(version);

        try {

            // Validate that our edge does have the necessary endpoints.
            if (rawEdge.getSource() == null || rawEdge.getTarget() == null) {
                throw new SpikeException("Source or target endpoint not specified");
            }

            // Create a key based on source:target:relationshipType
            String sourceNodeType = rawEdge.getSource().getType();
            String targetNodeType = rawEdge.getTarget().getType();
            String key = sourceNodeType + ":" + targetNodeType + ":" + rawEdge.getType();

            // Now, look up the specific schema model based on the key we just
            // constructed.
            Map<String, Class<?>> relationshipModel = schema.lookupRelation(key);
            if (relationshipModel == null || relationshipModel.isEmpty()) {
                throw new SpikeException("Invalid source/target/relationship type: " + key);
            }

            Set<Map.Entry<String, JsonElement>> edgeEntriesSet = rawEdge.getProperties().getAsJsonObject().entrySet();
            Map<String, JsonElement> edgeEntriesMap = new HashMap<String, JsonElement>();
            for (Map.Entry<String, JsonElement> entry : edgeEntriesSet) {
                edgeEntriesMap.put(entry.getKey(), entry.getValue());
            }

            JsonObject modelJsonElement = new JsonObject();

            for (String property : relationshipModel.keySet()) {

                if (!edgeEntriesMap.containsKey(property)) {
                    throw new SpikeException("Missing required field: " + property);
                }

                validateFieldType(edgeEntriesMap.get(property), relationshipModel.get(property));
                modelJsonElement.add(property, edgeEntriesMap.get(property));

            }

            rawEdge.setProperties(modelJsonElement);


        } catch (Exception ex) {
            throw new SpikeException(ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static Object validateFieldType(JsonElement value, Class clazz) throws SpikeException {
        try {
            if (clazz.isAssignableFrom(Integer.class)) {
                return value.getAsInt();
            } else if (clazz.isAssignableFrom(Long.class)) {
                return value.getAsLong();
            } else if (clazz.isAssignableFrom(Float.class)) {
                return value.getAsFloat();
            } else if (clazz.isAssignableFrom(Double.class)) {
                return value.getAsDouble();
            } else if (clazz.isAssignableFrom(Boolean.class)) {
                return value.getAsBoolean();
            } else {
                return value;
            }

        } catch (Exception e) {
            throw new SpikeException("Invalid property value: " + value);
        }
    }

    public static Object validateFieldType(String value, Class clazz) throws SpikeException {
        try {
            if (clazz.isAssignableFrom(Integer.class)) {
                return Integer.parseInt(value);
            } else if (clazz.isAssignableFrom(Long.class)) {
                return Long.parseLong(value);
            } else if (clazz.isAssignableFrom(Float.class)) {
                return Float.parseFloat(value);
            } else if (clazz.isAssignableFrom(Double.class)) {
                return Double.parseDouble(value);
            } else if (clazz.isAssignableFrom(Boolean.class)) {
                if (!value.equals("true") && !value.equals("false")) {
                    throw new SpikeException("Invalid property value: " + value);
                }
                return Boolean.parseBoolean(value);
            } else {
                return value;
            }
        } catch (Exception e) {
            throw new SpikeException("Invalid property value: " + value);
        }
    }

}
