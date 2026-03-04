package com.hypixel.hytale.codec.codecs.array;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.RawJsonCodec;
import com.hypixel.hytale.codec.WrappedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.exception.CodecException;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.metadata.Metadata;
import com.hypixel.hytale.codec.util.RawJsonReader;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonArray;
import org.bson.BsonNull;
import org.bson.BsonValue;

public class ArrayCodec<T> implements Codec<T[]>, RawJsonCodec<T[]>, WrappedCodec<T> {
   private final Codec<T> codec;
   private final IntFunction<T[]> arrayConstructor;
   @Nullable
   private final Supplier<T> defaultValue;
   private List<Metadata> metadata;
   private T[] emptyArray;

   public ArrayCodec(Codec<T> codec, IntFunction<T[]> arrayConstructor) {
      this(codec, arrayConstructor, null);
   }

   public ArrayCodec(Codec<T> codec, IntFunction<T[]> arrayConstructor, @Nullable Supplier<T> defaultValue) {
      this.codec = codec;
      this.arrayConstructor = arrayConstructor;
      this.defaultValue = defaultValue;
   }

   @Override
   public Codec<T> getChildCodec() {
      return this.codec;
   }

   public T[] decode(@Nonnull BsonValue bsonValue, @Nonnull ExtraInfo extraInfo) {
      BsonArray bsonArray = bsonValue.asArray();
      T[] array = (T[])((Object[])this.arrayConstructor.apply(bsonArray.size()));
      int i = 0;

      for (int size = bsonArray.size(); i < size; i++) {
         BsonValue value = bsonArray.get(i);
         extraInfo.pushIntKey(i);

         try {
            array[i] = this.decodeElement(value, extraInfo);
         } catch (Exception var12) {
            throw new CodecException("Failed to decode", value, extraInfo, var12);
         } finally {
            extraInfo.popKey();
         }
      }

      return array;
   }

   @Nonnull
   public BsonValue encode(@Nonnull T[] array, ExtraInfo extraInfo) {
      BsonArray bsonArray = new BsonArray();

      for (T t : array) {
         if (t == null) {
            bsonArray.add(new BsonNull());
         } else {
            bsonArray.add(this.codec.encode(t, extraInfo));
         }
      }

      return bsonArray;
   }

   public T[] decodeJson(@Nonnull RawJsonReader reader, @Nonnull ExtraInfo extraInfo) throws IOException {
      reader.expect('[');
      reader.consumeWhiteSpace();
      if (reader.tryConsume(']')) {
         if (this.emptyArray == null) {
            this.emptyArray = (T[])((Object[])this.arrayConstructor.apply(0));
         }

         return this.emptyArray;
      } else {
         int i = 0;
         T[] arr = (T[])((Object[])this.arrayConstructor.apply(10));

         while (true) {
            if (i == arr.length) {
               arr = (T[])Arrays.copyOf(arr, i + 1 + (i >> 1));
            }

            extraInfo.pushIntKey(i, reader);

            try {
               arr[i] = this.decodeJsonElement(reader, extraInfo);
               i++;
            } catch (Exception var9) {
               throw new CodecException("Failed to decode", reader, extraInfo, var9);
            } finally {
               extraInfo.popKey();
            }

            reader.consumeWhiteSpace();
            if (reader.tryConsumeOrExpect(']', ',')) {
               if (arr.length == i) {
                  return arr;
               }

               return (T[])Arrays.copyOf(arr, i);
            }

            reader.consumeWhiteSpace();
         }
      }
   }

   @Nonnull
   public ArrayCodec<T> metadata(Metadata metadata) {
      if (this.metadata == null) {
         this.metadata = new ObjectArrayList();
      }

      this.metadata.add(metadata);
      return this;
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      ArraySchema arraySchema = new ArraySchema();
      Schema childSchema = context.refDefinition(this.codec);
      if (this.metadata != null) {
         for (int i = 0; i < this.metadata.size(); i++) {
            Metadata meta = this.metadata.get(i);
            meta.modify(childSchema);
         }
      }

      arraySchema.setItem(childSchema);
      return arraySchema;
   }

   @Nullable
   public Supplier<T> getDefaultSupplier() {
      return this.defaultValue;
   }

   @Nullable
   protected T decodeElement(@Nonnull BsonValue value, ExtraInfo extraInfo) {
      if (!value.isNull()) {
         return this.codec.decode(value, extraInfo);
      } else {
         return this.defaultValue == null ? null : this.defaultValue.get();
      }
   }

   @Nullable
   protected T decodeJsonElement(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      if (!reader.tryConsume("null")) {
         return this.codec.decodeJson(reader, extraInfo);
      } else {
         return this.defaultValue == null ? null : this.defaultValue.get();
      }
   }

   @Nonnull
   public static <T> ArrayCodec<T> ofBuilderCodec(@Nonnull BuilderCodec<T> codec, IntFunction<T[]> arrayConstructor) {
      return new ArrayCodec<>(codec, arrayConstructor, codec.getSupplier());
   }
}
