package com.hypixel.hytale.codec.codecs.simple;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.BooleanSchema;
import com.hypixel.hytale.codec.schema.config.NullSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonBoolean;
import org.bson.BsonNull;
import org.bson.BsonValue;

public class NullableBooleanCodec implements Codec<Boolean> {
   @Nullable
   public Boolean decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      return bsonValue.isNull() ? null : bsonValue.asBoolean().getValue();
   }

   @Nonnull
   public BsonValue encode(Boolean t, ExtraInfo extraInfo) {
      return (BsonValue)(t == null ? BsonNull.VALUE : new BsonBoolean(t));
   }

   @Nullable
   public Boolean decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      if (reader.peekFor('n')) {
         if (!reader.tryConsume("null")) {
            throw new IllegalArgumentException("Invalid null value");
         } else {
            return null;
         }
      } else {
         return reader.readBooleanValue();
      }
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      return Schema.anyOf(new BooleanSchema(), new NullSchema());
   }

   @Nonnull
   public Schema toSchema(@Nonnull SchemaContext context, @Nullable Boolean def) {
      BooleanSchema s = new BooleanSchema();
      if (def != null) {
         s.setDefault(def);
      }

      return Schema.anyOf(s, new NullSchema());
   }
}
