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
package org.n52.sta.data.service;

import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.ID_ANNOTATION;

import java.util.List;
import java.util.OptionalLong;
import java.util.Optional;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.server.api.uri.UriParameter;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.QLocationEntity;
import org.n52.series.db.beans.sta.ThingEntity;
import org.n52.sta.data.repositories.LocationRepository;
import org.n52.sta.mapping.LocationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
public class LocationService implements AbstractSensorThingsEntityService {

    @Autowired
    private LocationRepository repository;

    @Autowired
    private LocationMapper mapper;

    @Autowired
    private ThingService thingService;

    private static QLocationEntity qloc = QLocationEntity.locationEntity;

    @Override
    public EntityCollection getEntityCollection() {
        EntityCollection retEntitySet = new EntityCollection();
        repository.findAll().forEach(t -> retEntitySet.getEntities().add(mapper.createEntity(t)));
        return retEntitySet;
    }

    @Override
    public Entity getEntity(Long id) {
        return getEntityForId(String.valueOf(id));
    }

    @Override
    public Entity getRelatedEntity(Entity sourceEntity) {
        //TODO: implement
        return null;
    }

    @Override
    public Entity getRelatedEntity(Entity sourceEntity, List<UriParameter> keyPredicates) {
        //TODO: implement
        return null;
    }

    @Override
    public EntityCollection getRelatedEntityCollection(Entity sourceEntity) {
        Iterable<LocationEntity> locations;
        switch (sourceEntity.getType()) {
            case "iot.Thing": {
                Long thingId = (Long) sourceEntity.getProperty(ID_ANNOTATION).getValue();

                // Source Entity should always exists (checked beforehand in Request Handler)
                Optional<ThingEntity> thing = thingService.getRawEntityForId(thingId);
                locations = repository.findAll(qloc.in(thing.get().getLocationEntities()));
                break;
            }
            default:
                return null;
        }
        EntityCollection retEntitySet = new EntityCollection();
        locations.forEach(t -> retEntitySet.getEntities().add(mapper.createEntity(t)));
        return retEntitySet;
    }

    private Entity getEntityForId(String id) {
        Optional<LocationEntity> entity = getRawEntityForId(Long.valueOf(id));
        return entity.isPresent() ? mapper.createEntity(entity.get()) : null;
    }

    protected Optional<LocationEntity> getRawEntityForId(Long id) {
        return repository.findById(id);
    }

    @Override
    public boolean existsEntity(Long id) {
        return true;
    }

    @Override
    public boolean existsRelatedEntity(Long sourceId, EdmEntityType sourceEntityType) {
        return true;
    }

    @Override
    public boolean existsRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        return true;
    }

    @Override
    public EntityCollection getRelatedEntityCollection(Long sourceId, EdmEntityType sourceEntityType) {
        return getEntityCollection();
    }

    @Override
    public OptionalLong getIdForRelatedEntity(Long sourceId, EdmEntityType sourceEntityType) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public OptionalLong getIdForRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Entity getRelatedEntity(Long sourceId, EdmEntityType sourceEntityType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Entity getRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
