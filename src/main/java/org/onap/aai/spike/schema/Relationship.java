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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import com.google.gson.annotations.SerializedName;


/**
 * This class represents a relationship instance that can be used for marshalling and unmarshalling
 * to/from a model compliant representation.
 */
@XmlRootElement
public class Relationship {

    /** Unique identifier for this relationship. */
    private String id;

    /** Relationship type. */
    private String type;

    /** The source vertex for this edge. */
    @SerializedName("source-node-type")
    private String sourceNodeType;

    /** The target vertex for this edge. */
    @SerializedName("target-node-type")
    private String targetNodeType;

    /** The properties assigned to this edge. */
    private Map<String, Object> properties = new HashMap<String, Object>();


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSourceNodeType() {
        return sourceNodeType;
    }

    @XmlElement(name = "source-node-type")
    public void setSourceNodeType(String sourceNodeType) {
        this.sourceNodeType = sourceNodeType;
    }

    public String getTargetNodeType() {
        return targetNodeType;
    }

    @XmlElement(name = "target-node-type")
    public void setTargetNodeType(String targetNodeType) {
        this.targetNodeType = targetNodeType;
    }

    @XmlJavaTypeAdapter(MapAdapter.class)
    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public void setProperty(String key, Object property) {
        properties.put(key, property);
    }
}
