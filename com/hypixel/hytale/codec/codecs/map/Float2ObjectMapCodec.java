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
import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.bson.BsonDocument;
import org.bson.BsonValue;

public class Float2ObjectMapCodec<T> implements Codec<Float2ObjectMap<T>>, WrappedCodec<T> {
   public static final Pattern FLOAT_PATTERN = Pattern.compile("^[-+]?[0-9]+(.[0-9]+)?$");
   private final Codec<T> valueCodec;
   private final Supplier<Float2ObjectMap<T>> supplier;
   private final boolean unmodifiable;

   public Float2ObjectMapCodec(Codec<T> valueCodec, Supplier<Float2ObjectMap<T>> supplier, boolean unmodifiable) {
      this.valueCodec = valueCodec;
      this.supplier = supplier;
      this.unmodifiable = unmodifiable;
   }

   public Float2ObjectMapCodec(Codec<T> valueCodec, Supplier<Float2ObjectMap<T>> supplier) {
      this(valueCodec, supplier, true);
   }

   @Override
   public Codec<T> getChildCodec() {
      return this.valueCodec;
   }

   public Float2ObjectMap<T> decode(@Nonnull BsonValue bsonValue, @Nonnull ExtraInfo extraInfo) {
      BsonDocument bsonDocument = bsonValue.asDocument();
      Float2ObjectMap<T> map = this.supplier.get();

      for (Entry<String, BsonValue> entry : bsonDocument.entrySet()) {
         String key = entry.getKey();
         BsonValue value = entry.getValue();
         extraInfo.pushKey(key);

         try {
            float decodedKey = Float.parseFloat(key);
            map.put(decodedKey, this.valueCodec.decode(value, extraInfo));
         } catch (Exception var13) {
            throw new CodecException("Failed to decode", value, extraInfo, var13);
         } finally {
            extraInfo.popKey();
         }
      }

      if (this.unmodifiable) {
         map = Float2ObjectMaps.unmodifiable(map);
      }

      return map;
   }

   @Nonnull
   public BsonValue encode(@Nonnull Float2ObjectMap<T> map, ExtraInfo extraInfo) {
      BsonDocument bsonDocument = new BsonDocument();
      ObjectIterator var4 = map.float2ObjectEntrySet().iterator();

      while (var4.hasNext()) {
         it.unimi.dsi.fastutil.floats.Float2ObjectMap.Entry<T> entry = (it.unimi.dsi.fastutil.floats.Float2ObjectMap.Entry<T>)var4.next();
         bsonDocument.put(Float.toString(entry.getFloatKey()), this.valueCodec.encode((T)entry.getValue(), extraInfo));
      }

      return bsonDocument;
   }

   public Float2ObjectMap<T> decodeJson(@Nonnull RawJsonReader reader, @Nonnull ExtraInfo extraInfo) throws IOException {
      reader.expect('{');
      reader.consumeWhiteSpace();
      Float2ObjectMap<T> map = this.supplier.get();
      if (reader.tryConsume('}')) {
         if (this.unmodifiable) {
            map = Float2ObjectMaps.unmodifiable(map);
         }

         return map;
      } else {
         while (true) {
            String key = reader.readString();
            reader.consumeWhiteSpace();
            reader.expect(':');
            reader.consumeWhiteSpace();
            extraInfo.pushKey(key, reader);

            try {
               float decodedKey = Float.parseFloat(key);
               map.put(decodedKey, this.valueCodec.decodeJson(reader, extraInfo));
            } catch (Exception var9) {
               throw new CodecException("Failed to decode", reader, extraInfo, var9);
            } finally {
               extraInfo.popKey();
            }

            reader.consumeWhiteSpace();
            if (reader.tryConsumeOrExpect('}', ',')) {
               if (this.unmodifiable) {
                  map = Float2ObjectMaps.unmodifiable(map);
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
      StringSchema name = new StringSchema();
      name.setPattern(FLOAT_PATTERN);
      name.setMarkdownDescription("A string that contains any floating point number");
      s.setPropertyNames(name);
      s.setAdditionalProperties(context.refDefinition(this.valueCodec));
      return s;
   }
}
