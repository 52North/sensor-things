/*
 * Copyright (C) 2012-2018 52°North Initiative for Geospatial Open Source
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
package org.n52.sta.mapping;

import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_DEFINITION;
import static org.n52.sta.edm.provider.entities.ObservedPropertyEntityProvider.ES_OBSERVED_PROPERTIES_NAME;
import static org.n52.sta.edm.provider.entities.ObservedPropertyEntityProvider.ET_OBSERVED_PROPERTY_FQN;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.n52.series.db.beans.PhenomenonEntity;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_ID;
import org.n52.sta.utils.EntityCreationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
public class ObservedPropertyMapper extends AbstractMapper<PhenomenonEntity> {

    @Autowired
    EntityCreationHelper entityCreationHelper;

    public Entity createEntity(PhenomenonEntity observedProperty) {
        Entity entity = new Entity();
        entity.addProperty(new Property(null, PROP_ID, ValueType.PRIMITIVE, observedProperty.getId()));
        addNameDescriptionProperties(entity, observedProperty);
        entity.addProperty(new Property(null, PROP_DEFINITION, ValueType.PRIMITIVE, observedProperty.getIdentifier()));

        entity.setType(ET_OBSERVED_PROPERTY_FQN.getFullQualifiedNameAsString());
        entity.setId(entityCreationHelper.createId(entity, ES_OBSERVED_PROPERTIES_NAME, PROP_ID));

        return entity;
    }

}
