package com.hypixel.hytale.codec.store;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonValue;

public class StoredCodec<T> implements Codec<T> {
   private final CodecKey<T> key;

   public StoredCodec(CodecKey<T> key) {
      this.key = key;
   }

   @Override
   public T decode(BsonValue bsonValue, @Nonnull ExtraInfo extraInfo) {
      Codec<T> codec = extraInfo.getCodecStore().getCodec(this.key);
      if (codec == null) {
         throw new IllegalArgumentException("Failed to find codec for " + this.key);
      } else {
         return codec.decode(bsonValue, extraInfo);
      }
   }

   @Override
   public BsonValue encode(T t, @Nonnull ExtraInfo extraInfo) {
      Codec<T> codec = extraInfo.getCodecStore().getCodec(this.key);
      if (codec == null) {
         throw new IllegalArgumentException("Failed to find codec for " + this.key);
      } else {
         return codec.encode(t, extraInfo);
      }
   }

   @Nullable
   @Override
   public T decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      Codec<T> codec = extraInfo.getCodecStore().getCodec(this.key);
      if (codec == null) {
         throw new IllegalArgumentException("Failed to find codec for " + this.key);
      } else {
         return codec.decodeJson(reader, extraInfo);
      }
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      Codec<T> codec = CodecStore.STATIC.getCodec(this.key);
      if (codec == null) {
         throw new IllegalArgumentException("Failed to find codec for " + this.key);
      } else {
         return context.refDefinition(codec);
      }
   }
}
