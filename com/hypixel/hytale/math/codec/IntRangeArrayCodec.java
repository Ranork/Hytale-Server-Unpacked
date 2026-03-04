package com.hypixel.hytale.math.codec;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.IntegerSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.codec.validation.ValidatableCodec;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.math.range.IntRange;
import java.io.IOException;
import java.util.Set;
import javax.annotation.Nonnull;
import org.bson.BsonArray;
import org.bson.BsonDouble;
import org.bson.BsonValue;

public class IntRangeArrayCodec implements Codec<IntRange>, ValidatableCodec<IntRange> {
   @Nonnull
   public IntRange decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      BsonArray document = bsonValue.asArray();
      return new IntRange(document.get(0).asNumber().intValue(), document.get(1).asNumber().intValue());
   }

   @Nonnull
   public BsonValue encode(@Nonnull IntRange t, ExtraInfo extraInfo) {
      BsonArray array = new BsonArray();
      array.add(new BsonDouble(t.getInclusiveMin()));
      array.add(new BsonDouble(t.getInclusiveMax()));
      return array;
   }

   @Nonnull
   public IntRange decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      reader.expect('[');
      reader.consumeWhiteSpace();
      int inclusiveMin = reader.readIntValue();
      reader.consumeWhiteSpace();
      reader.expect(',');
      reader.consumeWhiteSpace();
      int inclusiveMax = reader.readIntValue();
      reader.consumeWhiteSpace();
      reader.expect(']');
      reader.consumeWhiteSpace();
      return new IntRange(inclusiveMin, inclusiveMax);
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      ArraySchema s = new ArraySchema();
      s.setTitle("IntRange");
      s.setItems(new IntegerSchema(), new IntegerSchema());
      s.setMinItems(2);
      s.setMaxItems(2);
      return s;
   }

   public void validate(@Nonnull IntRange range, @Nonnull ExtraInfo extraInfo) {
      if (range.getInclusiveMin() > range.getInclusiveMax()) {
         ValidationResults results = extraInfo.getValidationResults();
         results.fail(String.format("Min (%d) > Max (%d)", range.getInclusiveMin(), range.getInclusiveMax()));
         results._processValidationResults();
      }
   }

   @Override
   public void validateDefaults(ExtraInfo extraInfo, Set<Codec<?>> tested) {
   }
}
