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

package org.n52.sta.serdes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.sta.data.service.CollectionWrapper;
import org.n52.sta.serdes.util.ElementWithQueryOptions;

import java.io.IOException;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class CollectionSer extends StdSerializer<CollectionWrapper> {

    public CollectionSer(Class<CollectionWrapper> t) {
        super(t);
    }

    @Override public void serialize(CollectionWrapper value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeStartObject();

        gen.writeNumberField("@iot.count", value.getTotalEntityCount());

        // We have multiple pages
        if (value.hasNextPage()) {
            QueryOptions queryOptions = value.getEntities().get(0).getQueryOptions();
            Long oldTop = queryOptions.getTopOption().getValue();
            Long oldSkip = queryOptions.hasSkipOption() ? queryOptions.getSkipOption().getValue() : 0;
            gen.writeStringField("@iot.nextLink",
                                 value.getRequestURL()
                                         + "?$top="
                                         + oldTop
                                         + "&$skip="
                                         + (oldTop + oldSkip)
            );
        }

        gen.writeArrayFieldStart("value");
        for (ElementWithQueryOptions element : value.getEntities()) {
            provider.defaultSerializeValue(element, gen);
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }
}