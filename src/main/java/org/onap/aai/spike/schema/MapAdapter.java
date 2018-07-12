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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.naming.OperationNotSupportedException;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.namespace.QName;


public class MapAdapter extends XmlAdapter<MapAdapter.AdaptedMap, Map<String, Object>> {

    public static class AdaptedMap {
        @XmlAnyElement
        List elements;
    }

    @Override
    public Map<String, Object> unmarshal(AdaptedMap map) throws Exception {
        throw new OperationNotSupportedException(); // really??
    }

    @Override
    public AdaptedMap marshal(Map<String, Object> map) throws Exception {

        AdaptedMap adaptedMap = new AdaptedMap();
        List elements = new ArrayList();
        for (Map.Entry<String, Object> property : map.entrySet()) {

            if (property.getValue() instanceof Map) {
                elements.add(new JAXBElement<AdaptedMap>(new QName(property.getKey()), MapAdapter.AdaptedMap.class,
                        marshal((Map) property.getValue())));

            } else {

                elements.add(new JAXBElement<String>(new QName(property.getKey()), String.class,
                        property.getValue().toString()));
            }
        }
        adaptedMap.elements = elements;
        return adaptedMap;
    }
}
