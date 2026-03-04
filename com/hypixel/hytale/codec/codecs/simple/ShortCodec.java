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
import org.bson.BsonInt32;
import org.bson.BsonValue;

public class ShortCodec implements Codec<Short>, RawJsonCodec<Short>, PrimitiveCodec {
   @Nonnull
   public Short decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      int intValue = bsonValue.asNumber().intValue();
      if (intValue >= -32768 && intValue <= 32767) {
         return (short)intValue;
      } else {
         throw new IllegalArgumentException("Expected a value between -32768 and 32767");
      }
   }

   @Nonnull
   public BsonValue encode(Short t, ExtraInfo extraInfo) {
      return new BsonInt32(t);
   }

   @Nonnull
   public Short decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      int intValue = reader.readIntValue();
      if (intValue >= -32768 && intValue <= 32767) {
         return (short)intValue;
      } else {
         throw new IllegalArgumentException("Expected a value between -32768 and 32767");
      }
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      return new IntegerSchema();
   }
}
