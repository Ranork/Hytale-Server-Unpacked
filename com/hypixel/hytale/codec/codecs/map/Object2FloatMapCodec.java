package com.hypixel.hytale.codec.codecs.map;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.WrappedCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.NumberSchema;
import com.hypixel.hytale.codec.schema.config.ObjectSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonString;
import org.bson.BsonValue;

public class Object2FloatMapCodec<T> implements Codec<Object2FloatMap<T>>, WrappedCodec<T> {
   private final Codec<T> keyCodec;
   private final Supplier<Object2FloatMap<T>> supplier;
   private final boolean unmodifiable;

   public Object2FloatMapCodec(Codec<T> keyCodec, Supplier<Object2FloatMap<T>> supplier, boolean unmodifiable) {
      this.keyCodec = keyCodec;
      this.supplier = supplier;
      this.unmodifiable = unmodifiable;
   }

   public Object2FloatMapCodec(Codec<T> keyCodec, Supplier<Object2FloatMap<T>> supplier) {
      this(keyCodec, supplier, true);
   }

   @Override
   public Codec<T> getChildCodec() {
      return this.keyCodec;
   }

   public Object2FloatMap<T> decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      BsonDocument bsonDocument = bsonValue.asDocument();
      Object2FloatMap<T> map = this.supplier.get();

      for (Entry<String, BsonValue> stringBsonValueEntry : bsonDocument.entrySet()) {
         T decodedKey = this.keyCodec.decode(new BsonString(stringBsonValueEntry.getKey()), extraInfo);
         map.put(decodedKey, (float)stringBsonValueEntry.getValue().asNumber().doubleValue());
      }

      if (this.unmodifiable) {
         map = Object2FloatMaps.unmodifiable(map);
      }

      return map;
   }

   @Nonnull
   public BsonValue encode(@Nonnull Object2FloatMap<T> map, ExtraInfo extraInfo) {
      BsonDocument bsonDocument = new BsonDocument();
      ObjectIterator var4 = map.keySet().iterator();

      while (var4.hasNext()) {
         T key = (T)var4.next();
         String encodedKey = this.keyCodec.encode(key, extraInfo).asString().getValue();
         bsonDocument.put(encodedKey, new BsonDouble(map.getFloat(key)));
      }

      return bsonDocument;
   }

   public Object2FloatMap<T> decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      reader.expect('{');
      reader.consumeWhiteSpace();
      Object2FloatMap<T> map = this.supplier.get();
      if (reader.tryConsume('}')) {
         if (this.unmodifiable) {
            map = Object2FloatMaps.unmodifiable(map);
         }

         return map;
      } else {
         while (true) {
            T key = this.keyCodec.decodeJson(reader, extraInfo);
            reader.consumeWhiteSpace();
            reader.expect(':');
            reader.consumeWhiteSpace();
            map.put(key, reader.readFloatValue());
            reader.consumeWhiteSpace();
            if (reader.tryConsumeOrExpect('}', ',')) {
               if (this.unmodifiable) {
                  map = Object2FloatMaps.unmodifiable(map);
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
      StringSchema key = (StringSchema)this.keyCodec.toSchema(context);
      String title = key.getTitle();
      if (title == null) {
         title = key.getHytale().getType();
      }

      s.setTitle("Map of " + title + " to float");
      s.setPropertyNames(key);
      s.setAdditionalProperties(new NumberSchema());
      return s;
   }
}
