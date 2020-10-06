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

package org.n52.sta.data.service;

import org.n52.series.db.beans.FormatEntity;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.sta.data.MutexFactory;
import org.n52.sta.data.repositories.FormatRepository;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class FormatService {

    private final MutexFactory mutexFactory;
    private final FormatRepository formatRepository;

    public FormatService(MutexFactory mutexFactory,
                         FormatRepository formatRepository) {
        this.mutexFactory = mutexFactory;
        this.formatRepository = formatRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FormatEntity createOrFetchFormat(FormatEntity formatEntity) throws STACRUDException {
        synchronized (mutexFactory.getLock(formatEntity.getFormat())) {
            if (!formatRepository.existsByFormat(formatEntity.getFormat())) {
                return formatRepository.saveAndFlush(formatEntity);
            } else {
                return formatRepository.findByFormat(formatEntity.getFormat());
            }
        }
    }
}
