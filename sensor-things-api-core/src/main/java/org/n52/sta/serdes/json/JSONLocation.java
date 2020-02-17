/*
 * Copyright (C) 2018-2020 52°North Initiative for Geospatial Open Source
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
import com.fasterxml.jackson.databind.JsonNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

@SuppressWarnings("VisibilityModifier")
@SuppressFBWarnings({"NM_FIELD_NAMING_CONVENTION", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class JSONLocation extends JSONBase.JSONwithIdNameDescription<LocationEntity> implements AbstractJSONEntity {

    private static final String COULD_NOT_PARSE = "Could not parse location to GeoJSON. Error was: ";
    // JSON Properties. Matched by Annotation or variable name
    public String encodingType;
    public JsonNode location;

    @JsonManagedReference
    public JSONThing[] Things;
    @JsonManagedReference
    public JSONHistoricalLocation[] HistoricalLocations;

    private final String ENCODINGTYPE_GEOJSON = "application/vnd.geo+json";

    private final GeometryFactory factory =
            new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

    public JSONLocation() {
        self = new LocationEntity();
    }

    @Override
    public LocationEntity toEntity(JSONBase.EntityType type) {
        GeoJsonReader reader = new GeoJsonReader(factory);
        switch (type) {
            case FULL:
                Assert.notNull(name, INVALID_INLINE_ENTITY + "name");
                Assert.notNull(description, INVALID_INLINE_ENTITY + "description");
                Assert.notNull(encodingType, INVALID_INLINE_ENTITY + "encodingType");
                Assert.notNull(location, INVALID_INLINE_ENTITY + "location");
                //Assert.notNull(location.type, INVALID_INLINE_ENTITY + "location->type");
                //Assert.notNull(location.geometry, INVALID_INLINE_ENTITY + "location->geometry");
                final String INVALID_ENCODINGTYPE =
                        "Invalid encodingType supplied. Only GeoJSON (application/vnd.geo+json) is supported!";
                Assert.notNull(encodingType, INVALID_ENCODINGTYPE);
                Assert.state(encodingType.equals(ENCODINGTYPE_GEOJSON), INVALID_ENCODINGTYPE);

                self.setIdentifier(identifier);
                self.setName(name);
                self.setDescription(description);
                self.setLocationEncoding(new FormatEntity().setFormat(encodingType));

                try {
                    self.setGeometry(reader.read(location.toString()));
                } catch (ParseException e) {
                    Assert.notNull(null, COULD_NOT_PARSE + e.getMessage());
                }

                if (Things != null) {
                    self.setThings(Arrays.stream(Things)
                            .map(thing -> thing.toEntity(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE))
                            .collect(Collectors.toSet()));
                }
                if (HistoricalLocations != null) {
                    self.setHistoricalLocations(Arrays.stream(HistoricalLocations)
                            .map(loc -> loc.toEntity(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE))
                            .collect(Collectors.toSet()));
                }

                if (backReference != null) {
                    if (backReference instanceof JSONThing) {
                        if (self.getThings() != null) {
                            self.getThings().add(((JSONThing) backReference).getEntity());
                        } else {
                            self.setThings(Collections.singleton(((JSONThing) backReference).getEntity()));
                        }
                    } else {
                        self.addHistoricalLocation(((JSONHistoricalLocation) backReference).getEntity());
                    }
                }
                return self;

            case PATCH:
                self.setIdentifier(identifier);
                self.setName(name);
                self.setDescription(description);
                self.setLocationEncoding(new FormatEntity().setFormat(encodingType));

                try {
                    self.setGeometry(reader.read(location.toString()));
                } catch (ParseException e) {
                    Assert.notNull(null, COULD_NOT_PARSE + e.getMessage());
                }

                if (Things != null) {
                    self.setThings(Arrays.stream(Things)
                            .map(thing -> thing.toEntity(JSONBase.EntityType.REFERENCE))
                            .collect(Collectors.toSet()));
                }
                if (HistoricalLocations != null) {
                    self.setHistoricalLocations(Arrays.stream(HistoricalLocations)
                            .map(loc -> loc.toEntity(JSONBase.EntityType.REFERENCE))
                            .collect(Collectors.toSet()));
                }
                return self;

            case REFERENCE:
                Assert.isNull(name, INVALID_REFERENCED_ENTITY);
                Assert.isNull(description, INVALID_REFERENCED_ENTITY);
                Assert.isNull(encodingType, INVALID_REFERENCED_ENTITY);
                Assert.isNull(location, INVALID_REFERENCED_ENTITY);
                Assert.isNull(Things, INVALID_REFERENCED_ENTITY);

                self.setIdentifier(identifier);
                return self;
            default:
                return null;
        }
    }
}