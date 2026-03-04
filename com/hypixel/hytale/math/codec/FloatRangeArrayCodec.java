package com.hypixel.hytale.math.codec;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.codecs.simple.FloatCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.NumberSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.codec.validation.ValidatableCodec;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.math.range.FloatRange;
import java.io.IOException;
import java.util.Set;
import javax.annotation.Nonnull;
import org.bson.BsonArray;
import org.bson.BsonDouble;
import org.bson.BsonValue;

public class FloatRangeArrayCodec implements Codec<FloatRange>, ValidatableCodec<FloatRange> {
   @Nonnull
   public FloatRange decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      BsonArray document = bsonValue.asArray();
      return new FloatRange(FloatCodec.decodeFloat(document.get(0)), FloatCodec.decodeFloat(document.get(1)));
   }

   @Nonnull
   public BsonValue encode(@Nonnull FloatRange floatRange, ExtraInfo extraInfo) {
      BsonArray array = new BsonArray();
      array.add(new BsonDouble(floatRange.getInclusiveMin()));
      array.add(new BsonDouble(floatRange.getInclusiveMax()));
      return array;
   }

   @Nonnull
   public FloatRange decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      reader.expect('[');
      reader.consumeWhiteSpace();
      float inclusiveMin = FloatCodec.readFloat(reader);
      reader.consumeWhiteSpace();
      reader.expect(',');
      reader.consumeWhiteSpace();
      float inclusiveMax = FloatCodec.readFloat(reader);
      reader.consumeWhiteSpace();
      reader.expect(']');
      reader.consumeWhiteSpace();
      return new FloatRange(inclusiveMin, inclusiveMax);
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      StringSchema stringSchema = new StringSchema();
      stringSchema.setPattern("^(-?Infinity|NaN)$");
      Schema choiceSchema = Schema.anyOf(new NumberSchema(), stringSchema);
      choiceSchema.getHytale().setType("Number");
      ArraySchema s = new ArraySchema();
      s.setTitle("FloatRange");
      s.setItems(choiceSchema, choiceSchema);
      s.setMinItems(2);
      s.setMaxItems(2);
      return s;
   }

   public void validate(@Nonnull FloatRange floatRange, @Nonnull ExtraInfo extraInfo) {
      if (floatRange.getInclusiveMin() > floatRange.getInclusiveMax()) {
         ValidationResults results = extraInfo.getValidationResults();
         results.fail(String.format("Min (%f) > Max (%f)", floatRange.getInclusiveMin(), floatRange.getInclusiveMax()));
         results._processValidationResults();
      }
   }

   @Override
   public void validateDefaults(ExtraInfo extraInfo, Set<Codec<?>> tested) {
   }
}
