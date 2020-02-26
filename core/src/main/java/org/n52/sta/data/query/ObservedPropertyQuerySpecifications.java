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
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class ObservedPropertyQuerySpecifications extends EntityQuerySpecifications<PhenomenonEntity> {

    public Specification<PhenomenonEntity> withDatastreamIdentifier(final String datastreamIdentifier) {
        return (root, query, builder) -> {
            final Join<PhenomenonEntity, DatastreamEntity> join =
                    root.join(DatastreamEntity.PROPERTY_OBSERVABLE_PROPERTY, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_IDENTIFIER), datastreamIdentifier);
        };
    }

    @Override
    public Specification<String> getIdSubqueryWithFilter(Specification filter) {
        return this.toSubquery(PhenomenonEntity.class, PhenomenonEntity.PROPERTY_IDENTIFIER, filter);
    }

    @Override
    public Specification<PhenomenonEntity> getFilterForProperty(String propertyName,
                                                                Expression<?> propertyValue,
                                                                FilterConstants.ComparisonOperator operator,
                                                                boolean switched)
            throws STAInvalidFilterExpressionException {
        if (propertyName.equals(DATASTREAMS)) {
            return handleRelatedPropertyFilter(propertyName, propertyValue);
        } else if (propertyName.equals("id")) {
            return (root, query, builder) -> {
                try {
                    return handleDirectStringPropertyFilter(root.get(PhenomenonEntity.PROPERTY_IDENTIFIER),
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

    private Specification<PhenomenonEntity> handleRelatedPropertyFilter(String propertyName,
                                                                        Expression<?> propertyValue) {
        return (root, query, builder) -> {
            final Join<PhenomenonEntity, DatastreamEntity> join =
                    root.join(DatastreamEntity.PROPERTY_OBSERVABLE_PROPERTY, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_IDENTIFIER), propertyValue);
        };
    }

    private Specification<PhenomenonEntity> handleDirectPropertyFilter(String propertyName,
                                                                       Expression<?> propertyValue,
                                                                       FilterConstants.ComparisonOperator operator,
                                                                       boolean switched) {
        return (Specification<PhenomenonEntity>) (root, query, builder) -> {
            try {
                switch (propertyName) {
                case "name":
                    return handleDirectStringPropertyFilter(root.get(DescribableEntity.PROPERTY_NAME),
                                                            propertyValue, operator, builder, switched);
                case "description":
                    return handleDirectStringPropertyFilter(
                            root.get(DescribableEntity.PROPERTY_DESCRIPTION),
                            propertyValue,
                            operator,
                            builder,
                            switched);
                case "definition":
                case "identifier":
                    return handleDirectStringPropertyFilter(
                            root.get(DescribableEntity.PROPERTY_IDENTIFIER),
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
