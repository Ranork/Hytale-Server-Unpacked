package com.hypixel.hytale.codec.codecs.simple;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.PrimitiveCodec;
import com.hypixel.hytale.codec.RawJsonCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.NumberSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDouble;
import org.bson.BsonValue;

public class DoubleCodec implements Codec<Double>, RawJsonCodec<Double>, PrimitiveCodec {
   @Nonnull
   public Double decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      if (bsonValue.isString()) {
         String var3 = bsonValue.asString().getValue();
         switch (var3) {
            case "NaN":
               return Double.NaN;
            case "Infinity":
               return Double.POSITIVE_INFINITY;
            case "-Infinity":
               return Double.NEGATIVE_INFINITY;
         }
      }

      return bsonValue.asNumber().doubleValue();
   }

   @Nonnull
   public BsonValue encode(Double t, ExtraInfo extraInfo) {
      return new BsonDouble(t);
   }

   @Nonnull
   public Double decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      return reader.readDoubleValue();
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      return new NumberSchema();
   }

   @Nonnull
   public Schema toSchema(@Nonnull SchemaContext context, @Nullable Double def) {
      NumberSchema s = new NumberSchema();
      if (def != null && !def.isNaN() && !def.isInfinite()) {
         s.setDefault(def);
      }

      return s;
   }
}
