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
import org.bson.BsonInt64;
import org.bson.BsonValue;

public class LongCodec implements Codec<Long>, RawJsonCodec<Long>, PrimitiveCodec {
   @Nonnull
   public Long decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      long longValue = bsonValue.asNumber().longValue();
      if (longValue != bsonValue.asNumber().doubleValue()) {
         throw new IllegalArgumentException("Expected an long but got a decimal!");
      } else {
         return longValue;
      }
   }

   @Nonnull
   public BsonValue encode(Long t, ExtraInfo extraInfo) {
      return new BsonInt64(t);
   }

   @Nonnull
   public Long decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      return reader.readLongValue();
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      return new IntegerSchema();
   }

   @Nonnull
   public Schema toSchema(@Nonnull SchemaContext context, @Nullable Long def) {
      IntegerSchema s = new IntegerSchema();
      if (def != null) {
         s.setDefault(def.intValue());
      }

      return s;
   }
}
