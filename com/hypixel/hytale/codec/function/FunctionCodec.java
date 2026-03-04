package com.hypixel.hytale.codec.function;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.bson.BsonValue;

@Deprecated
public class FunctionCodec<T, R> implements Codec<R> {
   @Nonnull
   private final Codec<T> codec;
   @Nonnull
   private final Function<T, R> decode;
   @Nonnull
   private final Function<R, T> encode;

   public FunctionCodec(Codec<T> codec, Function<T, R> decode, Function<R, T> encode) {
      this.codec = Objects.requireNonNull(codec, "codec parameter can't be null");
      this.decode = Objects.requireNonNull(decode, "decode parameter can't be null");
      this.encode = Objects.requireNonNull(encode, "encode parameter can't be null");
   }

   @Nonnull
   @Override
   public R decode(BsonValue bsonValue, ExtraInfo extraInfo) {
      T decode = this.codec.decode(bsonValue, extraInfo);
      R value = this.decode.apply(decode);
      if (value == null) {
         throw new IllegalArgumentException("Failed to apply function to '" + decode + "' decoded from '" + bsonValue + "'!");
      } else {
         return value;
      }
   }

   @Override
   public BsonValue encode(R r, ExtraInfo extraInfo) {
      return this.codec.encode(this.encode.apply(r), extraInfo);
   }

   @Nonnull
   @Override
   public R decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      T decode = this.codec.decodeJson(reader, extraInfo);
      R value = this.decode.apply(decode);
      if (value == null) {
         throw new IllegalArgumentException("Failed to apply function to '" + decode + "'!");
      } else {
         return value;
      }
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      return this.codec.toSchema(context);
   }
}
