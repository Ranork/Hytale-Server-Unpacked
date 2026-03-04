package com.hypixel.hytale.codec.function;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.WrappedCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import java.util.Objects;
import java.util.function.BiFunction;
import javax.annotation.Nonnull;
import org.bson.BsonValue;

@Deprecated
public class BsonFunctionCodec<T> implements Codec<T>, WrappedCodec<T> {
   @Nonnull
   private final Codec<T> codec;
   @Nonnull
   private final BiFunction<T, BsonValue, T> decode;
   @Nonnull
   private final BiFunction<BsonValue, T, BsonValue> encode;

   public BsonFunctionCodec(Codec<T> codec, BiFunction<T, BsonValue, T> decode, BiFunction<BsonValue, T, BsonValue> encode) {
      this.codec = Objects.requireNonNull(codec, "codec parameter can't be null");
      this.decode = Objects.requireNonNull(decode, "decode parameter can't be null");
      this.encode = Objects.requireNonNull(encode, "encode parameter can't be null");
   }

   @Override
   public T decode(BsonValue bsonValue, ExtraInfo extraInfo) {
      return this.decode.apply(this.codec.decode(bsonValue, extraInfo), bsonValue);
   }

   @Override
   public BsonValue encode(T r, ExtraInfo extraInfo) {
      return this.encode.apply(this.codec.encode(r, extraInfo), r);
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      return this.codec.toSchema(context);
   }

   @Nonnull
   @Override
   public Codec<T> getChildCodec() {
      return this.codec;
   }
}
