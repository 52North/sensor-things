/*
 * Copyright (C) 2018-2019 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.sta.serdes.json;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.joda.time.DateTime;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.ProcedureHistoryEntity;
import org.n52.series.db.beans.sta.SensorEntity;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("VisibilityModifier")
public class JSONSensor extends JSONBase.JSONwithIdNameDescription<SensorEntity> implements AbstractJSONEntity {

    private static final String STA_SENSORML_2 = "http://www.opengis.net/doc/IS/SensorML/2.0";
    private static final String SENSORML_2 = "http://www.opengis.net/sensorml/2.0";
    private static final String PDF = "application/pdf";

    // JSON Properties. Matched by Annotation or variable name
    public String properties;
    public String encodingType;
    public String metadata;

    @JsonManagedReference
    public JSONDatastream[] Datastreams;


    public JSONSensor() {
        self = new SensorEntity();
    }

    public SensorEntity toEntity(boolean validate) {
        if (!generatedId && name == null && validate) {
            Assert.isNull(name, INVALID_REFERENCED_ENTITY);
            Assert.isNull(description, INVALID_REFERENCED_ENTITY);
            Assert.isNull(encodingType, INVALID_REFERENCED_ENTITY);
            Assert.isNull(metadata, INVALID_REFERENCED_ENTITY);

            Assert.isNull(Datastreams, INVALID_REFERENCED_ENTITY);

            self.setIdentifier(identifier);
            return self;
        } else {
            if (validate) {
                Assert.notNull(name, INVALID_INLINE_ENTITY + "name");
                Assert.notNull(description, INVALID_INLINE_ENTITY + "description");
                Assert.notNull(encodingType, INVALID_INLINE_ENTITY + "encodingType");
                Assert.notNull(metadata, INVALID_INLINE_ENTITY + "metadata");
            }

            self.setIdentifier(identifier);
            self.setName(name);
            self.setDescription(description);

            if  (encodingType != null) {
                if (encodingType.equalsIgnoreCase(STA_SENSORML_2)) {
                    self.setFormat(new FormatEntity().setFormat(SENSORML_2));
                    ProcedureHistoryEntity procedureHistoryEntity = new ProcedureHistoryEntity();
                    procedureHistoryEntity.setProcedure(self);
                    procedureHistoryEntity.setFormat(self.getFormat());
                    procedureHistoryEntity.setStartTime(DateTime.now().toDate());
                    procedureHistoryEntity.setXml(metadata);
                    Set<ProcedureHistoryEntity> set = new LinkedHashSet<>();
                    set.add(procedureHistoryEntity);
                    self.setProcedureHistory(set);
                } else if (encodingType.equalsIgnoreCase(PDF)) {
                    self.setFormat(new FormatEntity().setFormat(PDF));
                    self.setDescriptionFile(metadata);
                } else {
                    Assert.notNull(null, "Invalid encodingType supplied. Only SensorML or PDF allowed.");
                }
            } else {
                // Used when PATCHing new metadata
                self.setDescriptionFile(metadata);
            }

            if (Datastreams != null) {
                self.setDatastreams(Arrays.stream(Datastreams)
                        .map(JSONDatastream::toEntity)
                        .collect(Collectors.toSet()));
            }

            // Deal with back reference during deep insert
            if (backReference != null) {
                self.addDatastream(((JSONDatastream) backReference).getEntity());
            }

            return self;
        }
    }
}
