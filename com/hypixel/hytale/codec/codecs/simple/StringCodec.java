package com.hypixel.hytale.codec.codecs.simple;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.RawJsonCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonString;
import org.bson.BsonValue;

public class StringCodec implements Codec<String>, RawJsonCodec<String> {
   public String decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      return bsonValue.asString().getValue();
   }

   @Nonnull
   public BsonValue encode(@Nonnull String t, ExtraInfo extraInfo) {
      return new BsonString(t);
   }

   public String decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      return reader.readString();
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      return new StringSchema();
   }

   @Nonnull
   public Schema toSchema(@Nonnull SchemaContext context, @Nullable String def) {
      StringSchema s = new StringSchema();
      if (def != null) {
         s.setDefault(def);
      }

      return s;
   }
}
