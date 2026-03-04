package com.hypixel.hytale.codec.codecs.map;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.WrappedCodec;
import com.hypixel.hytale.codec.exception.CodecException;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ObjectSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import org.bson.BsonDocument;
import org.bson.BsonValue;

@Deprecated
public class ObjectMapCodec<K, V, M extends Map<K, V>> implements Codec<Map<K, V>>, WrappedCodec<V> {
   private final Codec<V> codec;
   private final Supplier<M> supplier;
   private final Function<K, String> keyToString;
   private final Function<String, K> stringToKey;
   private final boolean unmodifiable;

   public ObjectMapCodec(Codec<V> codec, Supplier<M> supplier, Function<K, String> keyToString, Function<String, K> stringToKey) {
      this(codec, supplier, keyToString, stringToKey, true);
   }

   public ObjectMapCodec(Codec<V> codec, Supplier<M> supplier, Function<K, String> keyToString, Function<String, K> stringToKey, boolean unmodifiable) {
      this.codec = codec;
      this.supplier = supplier;
      this.keyToString = keyToString;
      this.stringToKey = stringToKey;
      this.unmodifiable = unmodifiable;
   }

   @Override
   public Codec<V> getChildCodec() {
      return this.codec;
   }

   public Map<K, V> decode(@Nonnull BsonValue bsonValue, @Nonnull ExtraInfo extraInfo) {
      BsonDocument bsonDocument = bsonValue.asDocument();
      Map<K, V> map = this.supplier.get();

      for (Entry<String, BsonValue> entry : bsonDocument.entrySet()) {
         String key = entry.getKey();
         BsonValue value = entry.getValue();
         K decodedKey = this.stringToKey.apply(key);
         extraInfo.pushKey(key);

         try {
            map.put(decodedKey, this.codec.decode(value, extraInfo));
         } catch (Exception var14) {
            throw new CodecException("Failed to decode", value, extraInfo, var14);
         } finally {
            extraInfo.popKey();
         }
      }

      if (this.unmodifiable) {
         map = Collections.unmodifiableMap(map);
      }

      return map;
   }

   @Nonnull
   public BsonValue encode(@Nonnull Map<K, V> map, ExtraInfo extraInfo) {
      BsonDocument bsonDocument = new BsonDocument();

      for (Entry<K, V> entry : map.entrySet()) {
         BsonValue value = this.codec.encode(entry.getValue(), extraInfo);
         if (value != null && !value.isNull() && (!value.isDocument() || !value.asDocument().isEmpty()) && (!value.isArray() || !value.asArray().isEmpty())) {
            bsonDocument.put(this.keyToString.apply(entry.getKey()), value);
         }
      }

      return bsonDocument;
   }

   public Map<K, V> decodeJson(@Nonnull RawJsonReader reader, @Nonnull ExtraInfo extraInfo) throws IOException {
      reader.expect('{');
      reader.consumeWhiteSpace();
      if (reader.tryConsume('}')) {
         return this.unmodifiable ? Collections.emptyMap() : this.supplier.get();
      } else {
         Map<K, V> map = this.supplier.get();

         while (true) {
            String key = reader.readString();
            K decodedKey = this.stringToKey.apply(key);
            reader.consumeWhiteSpace();
            reader.expect(':');
            reader.consumeWhiteSpace();
            extraInfo.pushKey(key, reader);

            try {
               map.put(decodedKey, this.codec.decodeJson(reader, extraInfo));
            } catch (Exception var10) {
               throw new CodecException("Failed to decode", reader, extraInfo, var10);
            } finally {
               extraInfo.popKey();
            }

            reader.consumeWhiteSpace();
            if (reader.tryConsumeOrExpect('}', ',')) {
               if (this.unmodifiable) {
                  map = Collections.unmodifiableMap(map);
               }

               return map;
            }

            reader.consumeWhiteSpace();
         }
      }
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      ObjectSchema s = new ObjectSchema();
      s.setPropertyNames(new StringSchema());
      s.setAdditionalProperties(context.refDefinition(this.codec));
      return s;
   }
}
