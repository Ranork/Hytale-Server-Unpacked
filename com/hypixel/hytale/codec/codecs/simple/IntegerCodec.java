package com.hypixel.hytale.codec.codecs.simple;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.PrimitiveCodec;
import com.hypixel.hytale.codec.RawJsonCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.IntegerSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonInt32;
import org.bson.BsonValue;

public class IntegerCodec implements Codec<Integer>, RawJsonCodec<Integer>, PrimitiveCodec {
   @Nonnull
   public Integer decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      int intValue = bsonValue.asNumber().intValue();
      if (intValue != bsonValue.asNumber().doubleValue()) {
         throw new IllegalArgumentException("Expected an int but got a decimal!");
      } else {
         return intValue;
      }
   }

   @Nonnull
   public BsonValue encode(Integer t, ExtraInfo extraInfo) {
      return new BsonInt32(t);
   }

   @Nonnull
   public Integer decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      return reader.readIntValue();
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      return new IntegerSchema();
   }

   @Nonnull
   public Schema toSchema(@Nonnull SchemaContext context, @Nullable Integer def) {
      IntegerSchema s = new IntegerSchema();
      if (def != null) {
         s.setDefault(def);
      }

      return s;
   }
}
