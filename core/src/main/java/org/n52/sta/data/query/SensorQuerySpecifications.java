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

package org.n52.sta.data.query;

import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class SensorQuerySpecifications extends EntityQuerySpecifications<ProcedureEntity> {

    public Specification<ProcedureEntity> withDatastreamIdentifier(final String datastreamIdentifier) {
        return (root, query, builder) -> {
            Subquery<ProcedureEntity> sq = query.subquery(ProcedureEntity.class);
            Root<DatastreamEntity> datastream = sq.from(DatastreamEntity.class);
            Join<DatastreamEntity, ProcedureEntity> join = datastream.join(DatastreamEntity.PROPERTY_SENSOR);
            sq.select(join)
              .where(builder.equal(datastream.get(DescribableEntity.PROPERTY_IDENTIFIER), datastreamIdentifier));
            return builder.in(root).value(sq);
        };
    }

    @Override
    public Specification<String> getIdSubqueryWithFilter(Specification filter) {
        return this.toSubquery(ProcedureEntity.class, ProcedureEntity.PROPERTY_IDENTIFIER, filter);
    }

    @Override
    public Specification<ProcedureEntity> getFilterForProperty(String propertyName,
                                                               Expression<?> propertyValue,
                                                               FilterConstants.ComparisonOperator operator,
                                                               boolean switched) {
        if (propertyName.equals(DATASTREAMS)) {
            return handleRelatedPropertyFilter(propertyName, propertyValue);
        } else if (propertyName.equals("id")) {
            return (root, query, builder) -> {
                try {
                    return handleDirectStringPropertyFilter(root.get(ProcedureEntity.PROPERTY_IDENTIFIER),
                                                            propertyValue,
                                                            operator,
                                                            builder,
                                                            false);
                } catch (STAInvalidFilterExpressionException e) {
                    throw new RuntimeException(e);
                }
                //
            };
        } else {
            return handleDirectPropertyFilter(propertyName, propertyValue, operator, switched);
        }
    }

    private Specification<ProcedureEntity> handleRelatedPropertyFilter(String propertyName,
                                                                       Expression<?> propertyValue) {
        return (root, query, builder) -> {
            Subquery<ProcedureEntity> sq = query.subquery(ProcedureEntity.class);
            Root<DatastreamEntity> datastream = sq.from(DatastreamEntity.class);
            Join<DatastreamEntity, ProcedureEntity> join = datastream.join(DatastreamEntity.PROPERTY_SENSOR);
            sq.select(join).where(builder.equal(datastream.get(DescribableEntity.PROPERTY_IDENTIFIER), propertyValue));
            return builder.in(root).value(sq);
        };
        // throws ExpressionVisitException {
        // return qsensor.id.in(dQS.toSubquery(qdatastream,
        // qdatastream.procedure.id,
        // qdatastream.id.eq(propertyValue)));
    }

    private Specification<ProcedureEntity> handleDirectPropertyFilter(String propertyName,
                                                                      Expression<?> propertyValue,
                                                                      FilterConstants.ComparisonOperator operator,
                                                                      boolean switched) {
        return (Specification<ProcedureEntity>) (root, query, builder) -> {
            try {
                switch (propertyName) {
                case "name":
                    return handleDirectStringPropertyFilter(root.get(DescribableEntity.PROPERTY_NAME),
                                                            propertyValue,
                                                            operator,
                                                            builder,
                                                            switched);
                case "description":
                    return handleDirectStringPropertyFilter(
                            root.get(DescribableEntity.PROPERTY_DESCRIPTION),
                            propertyValue,
                            operator,
                            builder,
                            switched);
                case "format":
                case "encodingType":
                    Join<ProcedureEntity, FormatEntity> join =
                            root.join(ProcedureEntity.PROPERTY_PROCEDURE_DESCRIPTION_FORMAT);
                    return handleDirectStringPropertyFilter(join.get(FormatEntity.FORMAT),
                                                            propertyValue,
                                                            operator,
                                                            builder,
                                                            switched);
                case "metadata":
                    return handleDirectStringPropertyFilter(
                            root.get(ProcedureEntity.PROPERTY_DESCRIPTION_FILE),
                            propertyValue,
                            operator,
                            builder,
                            switched);
                default:
                    throw new RuntimeException("Error getting filter for Property: \"" + propertyName
                                                       + "\". No such property in Entity.");
                }
            } catch (STAInvalidFilterExpressionException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
