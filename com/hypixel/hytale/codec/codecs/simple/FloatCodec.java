package com.hypixel.hytale.codec.codecs.simple;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.PrimitiveCodec;
import com.hypixel.hytale.codec.RawJsonCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.NumberSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDouble;
import org.bson.BsonValue;

public class FloatCodec implements Codec<Float>, RawJsonCodec<Float>, PrimitiveCodec {
   public static final String STRING_SCHEMA_PATTERN = "^(-?Infinity|NaN)$";

   @Nonnull
   public Float decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      return decodeFloat(bsonValue);
   }

   @Nonnull
   public BsonValue encode(Float t, ExtraInfo extraInfo) {
      return new BsonDouble(t.floatValue());
   }

   @Nonnull
   public Float decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      return readFloat(reader);
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      StringSchema stringSchema = new StringSchema();
      stringSchema.setPattern("^(-?Infinity|NaN)$");
      return Schema.anyOf(new NumberSchema(), stringSchema);
   }

   @Nonnull
   public Schema toSchema(@Nonnull SchemaContext context, @Nullable Float def) {
      StringSchema stringSchema = new StringSchema();
      stringSchema.setPattern("^(-?Infinity|NaN)$");
      NumberSchema numberSchema = new NumberSchema();
      if (def != null) {
         if (!def.isNaN() && !def.isInfinite()) {
            numberSchema.setDefault(def.doubleValue());
         } else {
            stringSchema.setDefault(def.toString());
         }
      }

      Schema schema = Schema.anyOf(numberSchema, stringSchema);
      schema.getHytale().setType("Number");
      return schema;
   }

   public static float decodeFloat(@Nonnull BsonValue value) {
      if (value.isString()) {
         String var1 = value.asString().getValue();
         switch (var1) {
            case "NaN":
               return Float.NaN;
            case "Infinity":
               return Float.POSITIVE_INFINITY;
            case "-Infinity":
               return Float.NEGATIVE_INFINITY;
         }
      }

      return (float)value.asNumber().doubleValue();
   }

   public static float readFloat(@Nonnull RawJsonReader reader) throws IOException {
      if (reader.peekFor('"')) {
         String str = reader.readString();

         return switch (str) {
            case "NaN" -> Float.NaN;
            case "Infinity" -> Float.POSITIVE_INFINITY;
            case "-Infinity" -> Float.NEGATIVE_INFINITY;
            default -> throw new IOException("Unexpected string: \"" + str + "\", expected NaN, Infinity, -Infinity");
         };
      } else {
         return (float)reader.readDoubleValue();
      }
   }
}
