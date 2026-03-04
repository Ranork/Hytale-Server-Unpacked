package com.hypixel.hytale.codec.codecs.map;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.WrappedCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.IntegerSchema;
import com.hypixel.hytale.codec.schema.config.ObjectSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.BsonValue;

public class Object2IntMapCodec<T> implements Codec<Object2IntMap<T>>, WrappedCodec<T> {
   private final Codec<T> keyCodec;
   private final Supplier<Object2IntMap<T>> supplier;
   private final boolean unmodifiable;

   public Object2IntMapCodec(Codec<T> keyCodec, Supplier<Object2IntMap<T>> supplier, boolean unmodifiable) {
      this.keyCodec = keyCodec;
      this.supplier = supplier;
      this.unmodifiable = unmodifiable;
   }

   public Object2IntMapCodec(Codec<T> keyCodec, Supplier<Object2IntMap<T>> supplier) {
      this(keyCodec, supplier, true);
   }

   @Override
   public Codec<T> getChildCodec() {
      return this.keyCodec;
   }

   public Object2IntMap<T> decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      BsonDocument bsonDocument = bsonValue.asDocument();
      Object2IntMap<T> map = this.supplier.get();

      for (Entry<String, BsonValue> entry : bsonDocument.entrySet()) {
         T decodedKey = this.keyCodec.decode(new BsonString(entry.getKey()), extraInfo);
         map.put(decodedKey, entry.getValue().asInt32().intValue());
      }

      if (this.unmodifiable) {
         map = Object2IntMaps.unmodifiable(map);
      }

      return map;
   }

   @Nonnull
   public BsonValue encode(@Nonnull Object2IntMap<T> map, ExtraInfo extraInfo) {
      BsonDocument bsonDocument = new BsonDocument();
      ObjectIterator var4 = map.keySet().iterator();

      while (var4.hasNext()) {
         T key = (T)var4.next();
         String encodedKey = this.keyCodec.encode(key, extraInfo).asString().getValue();
         bsonDocument.put(encodedKey, new BsonInt32(map.getInt(key)));
      }

      return bsonDocument;
   }

   public Object2IntMap<T> decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      reader.expect('{');
      reader.consumeWhiteSpace();
      Object2IntMap<T> map = this.supplier.get();
      if (reader.tryConsume('}')) {
         if (this.unmodifiable) {
            map = Object2IntMaps.unmodifiable(map);
         }

         return map;
      } else {
         while (true) {
            T key = this.keyCodec.decodeJson(reader, extraInfo);
            reader.consumeWhiteSpace();
            reader.expect(':');
            reader.consumeWhiteSpace();
            map.put(key, reader.readIntValue());
            reader.consumeWhiteSpace();
            if (reader.tryConsumeOrExpect('}', ',')) {
               if (this.unmodifiable) {
                  map = Object2IntMaps.unmodifiable(map);
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

      s.setTitle("Map of " + title + " to integer");
      s.setPropertyNames(key);
      s.setAdditionalProperties(new IntegerSchema());
      return s;
   }
}
