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

public class ByteCodec implements Codec<Byte>, RawJsonCodec<Byte>, PrimitiveCodec {
   @Nonnull
   public Byte decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      int intValue = bsonValue.asNumber().intValue();
      if (intValue >= -128 && intValue <= 127) {
         return (byte)intValue;
      } else {
         throw new IllegalArgumentException("Expected a value between -128 and 127");
      }
   }

   @Nonnull
   public BsonValue encode(Byte t, ExtraInfo extraInfo) {
      return new BsonInt32(t);
   }

   @Nonnull
   public Byte decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      int intValue = reader.readIntValue();
      if (intValue >= -128 && intValue <= 127) {
         return (byte)intValue;
      } else {
         throw new IllegalArgumentException("Expected a value between -128 and 127");
      }
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      return new IntegerSchema();
   }

   @Nonnull
   public Schema toSchema(@Nonnull SchemaContext context, @Nullable Byte def) {
      IntegerSchema s = new IntegerSchema();
      if (def != null) {
         s.setDefault(def.intValue());
      }

      return s;
   }
}
