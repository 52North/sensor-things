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
package org.n52.sta.data.query;

import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.QProcedureEntity;
import org.n52.series.db.beans.sta.QDatastreamEntity;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
public class SensorQuerySpecifications extends EntityQuerySpecifications {

    private final static QProcedureEntity qsensor = QProcedureEntity.procedureEntity;

    public JPQLQuery<ProcedureEntity> toSubquery(final BooleanExpression filter) {
        return JPAExpressions.selectFrom(qsensor)
                             .where(filter);
    }

    public <T> BooleanExpression selectFrom(JPQLQuery<T> subquery) {
        return qsensor.id.in(subquery.select(qsensor.id));
    }

    public BooleanExpression matchesId(Long id) {
        return qsensor.id.eq(id);
    }

    /**
     * Assures that Entity is valid.
     * Entity is valid if:
     * - has Datastream associated with it
     * 
     * @return BooleanExpression evaluating to true if Entity is valid
     */
    public BooleanExpression isValidEntity() {
        return qsensor.id.in(dQS.toSubquery(qdatastream.procedure.eq(qsensor)).select(qsensor.id));
    }
}
