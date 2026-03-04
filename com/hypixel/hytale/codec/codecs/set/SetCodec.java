package com.hypixel.hytale.codec.codecs.set;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.WrappedCodec;
import com.hypixel.hytale.codec.exception.CodecException;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import org.bson.BsonArray;
import org.bson.BsonValue;

public class SetCodec<V, S extends Set<V>> implements Codec<Set<V>>, WrappedCodec<V> {
   private final Codec<V> codec;
   private final Supplier<S> supplier;
   private final boolean unmodifiable;

   public SetCodec(Codec<V> codec, Supplier<S> supplier, boolean unmodifiable) {
      this.codec = codec;
      this.supplier = supplier;
      this.unmodifiable = unmodifiable;
   }

   public Set<V> decode(@Nonnull BsonValue bsonValue, @Nonnull ExtraInfo extraInfo) {
      BsonArray list = bsonValue.asArray();
      if (list.isEmpty()) {
         return this.unmodifiable ? Collections.emptySet() : this.supplier.get();
      } else {
         S out = this.supplier.get();

         for (int i = 0; i < list.size(); i++) {
            BsonValue value = list.get(i);
            extraInfo.pushIntKey(i);

            try {
               V decoded = this.codec.decode(value, extraInfo);
               if (!out.add(decoded)) {
                  throw new CodecException("The value is already in the set:" + decoded);
               }
            } catch (Exception var11) {
               throw new CodecException("Failed to decode", value, extraInfo, var11);
            } finally {
               extraInfo.popKey();
            }
         }

         return this.unmodifiable ? Collections.unmodifiableSet(out) : out;
      }
   }

   public Set<V> decodeJson(@Nonnull RawJsonReader reader, @Nonnull ExtraInfo extraInfo) throws IOException {
      reader.expect('[');
      reader.consumeWhiteSpace();
      if (reader.tryConsume(']')) {
         return this.unmodifiable ? Collections.emptySet() : this.supplier.get();
      } else {
         int i = 0;
         S out = this.supplier.get();

         while (true) {
            extraInfo.pushIntKey(i, reader);

            try {
               V decoded = this.codec.decodeJson(reader, extraInfo);
               if (!out.add(decoded)) {
                  throw new CodecException("The value is already in the set:" + decoded);
               }

               i++;
            } catch (Exception var9) {
               throw new CodecException("Failed to decode", reader, extraInfo, var9);
            } finally {
               extraInfo.popKey();
            }

            reader.consumeWhiteSpace();
            if (reader.tryConsumeOrExpect(']', ',')) {
               return this.unmodifiable ? Collections.unmodifiableSet(out) : out;
            }

            reader.consumeWhiteSpace();
         }
      }
   }

   @Nonnull
   public BsonValue encode(@Nonnull Set<V> vs, @Nonnull ExtraInfo extraInfo) {
      BsonArray out = new BsonArray();
      int key = 0;

      for (V v : vs) {
         extraInfo.pushIntKey(key++);

         try {
            out.add(this.codec.encode(v, extraInfo));
         } finally {
            extraInfo.popKey();
         }
      }

      return out;
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      ArraySchema schema = new ArraySchema();
      schema.setTitle("Set");
      schema.setItem(context.refDefinition(this.codec));
      schema.setUniqueItems(true);
      return schema;
   }

   @Override
   public Codec<V> getChildCodec() {
      return this.codec;
   }
}
