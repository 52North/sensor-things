package org.n52.sta.serdes.json;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.sta.data.service.ServiceUtils;
import org.springframework.util.Assert;

@SuppressWarnings("VisibilityModifier")
public class JSONFeatureOfInterest extends JSONBase.JSONwithIdNameDescription<FeatureEntity>
        implements AbstractJSONEntity {

    // JSON Properties. Matched by Annotation or variable name
    public String encodingType;
    public JSONNestedFeature feature;
    @JsonManagedReference
    public JSONObservation[] Observations;

    private final String ENCODINGTYPE_GEOJSON = "application/vnd.geo+json";
    private final GeometryFactory factory =
            new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);


    public JSONFeatureOfInterest() {
        self = new FeatureEntity();
    }

    public FeatureEntity toEntity(boolean validate) {
        if (!generatedId && name == null && validate) {
            Assert.isNull(name, INVALID_REFERENCED_ENTITY);
            Assert.isNull(description, INVALID_REFERENCED_ENTITY);
            Assert.isNull(encodingType, INVALID_REFERENCED_ENTITY);
            Assert.isNull(feature, INVALID_REFERENCED_ENTITY);
            Assert.isNull(Observations, INVALID_REFERENCED_ENTITY);

            self.setIdentifier(identifier);
            return self;
        } else {
            if (validate) {
                Assert.notNull(name, INVALID_INLINE_ENTITY + "name");
                Assert.notNull(description, INVALID_INLINE_ENTITY + "description");
                Assert.notNull(feature, INVALID_INLINE_ENTITY + "feature");
                Assert.state(encodingType.equals(ENCODINGTYPE_GEOJSON),
                        "Invalid encodingType supplied. Only GeoJSON (application/vnd.geo+json) is supported!");
                Assert.notNull(feature.geometry, INVALID_INLINE_ENTITY + "feature->geometry");
                Assert.notNull(feature.type, INVALID_INLINE_ENTITY + "feature->type");
                Assert.state(feature.type.equals("Feature"), "Invalid Featuretype.");
            }

            self.setIdentifier(identifier);
            self.setName(name);
            self.setDescription(description);

            if (feature != null) {
                GeoJsonReader reader = new GeoJsonReader(factory);
                try {
                    self.setGeometry(reader.read(feature.geometry.toString()));
                } catch (ParseException e) {
                    Assert.notNull(null, "Could not parse feature to GeoJSON. Error was:" + e.getMessage());
                }
                self.setFeatureType(ServiceUtils.createFeatureType(self.getGeometry()));
            }

            //TODO: handle nested observations
            if (backReference != null) {
                // TODO: link feature to observations?
                // throw new NotImplementedException();
            }

            return self;
        }
    }

    private static class JSONNestedFeature {
        public String type;
        public JsonNode geometry;
    }
}
