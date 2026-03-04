package com.hypixel.hytale.codec.codecs.map;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.WrappedCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.exception.CodecException;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ObjectSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;
import org.bson.BsonValue;

public class EnumMapCodec<K extends Enum<K>, V> implements Codec<Map<K, V>>, WrappedCodec<V> {
   @Nonnull
   private final Class<K> clazz;
   private final K[] enumConstants;
   @Nonnull
   private final String[] enumKeys;
   private final EnumCodec.EnumStyle enumStyle;
   private final Codec<V> codec;
   private final Supplier<Map<K, V>> supplier;
   private final boolean unmodifiable;
   @Nonnull
   private final EnumMap<K, String> keyDocumentation;

   public EnumMapCodec(@Nonnull Class<K> clazz, Codec<V> codec) {
      this(clazz, codec, true);
   }

   public EnumMapCodec(@Nonnull Class<K> clazz, Codec<V> codec, boolean unmodifiable) {
      this(clazz, EnumCodec.EnumStyle.CAMEL_CASE, codec, () -> new EnumMap<>(clazz), unmodifiable);
   }

   public EnumMapCodec(@Nonnull Class<K> clazz, Codec<V> codec, Supplier<Map<K, V>> supplier) {
      this(clazz, EnumCodec.EnumStyle.CAMEL_CASE, codec, supplier, true);
   }

   public EnumMapCodec(@Nonnull Class<K> clazz, Codec<V> codec, Supplier<Map<K, V>> supplier, boolean unmodifiable) {
      this(clazz, EnumCodec.EnumStyle.CAMEL_CASE, codec, supplier, unmodifiable);
   }

   public EnumMapCodec(@Nonnull Class<K> clazz, EnumCodec.EnumStyle enumStyle, Codec<V> codec, Supplier<Map<K, V>> supplier, boolean unmodifiable) {
      this.clazz = clazz;
      this.enumConstants = clazz.getEnumConstants();
      this.enumStyle = enumStyle;
      this.codec = codec;
      this.supplier = supplier;
      this.unmodifiable = unmodifiable;
      this.keyDocumentation = new EnumMap<>(clazz);
      EnumCodec.EnumStyle currentStyle = EnumCodec.EnumStyle.detect(this.enumConstants);
      this.enumKeys = new String[this.enumConstants.length];

      for (int i = 0; i < this.enumConstants.length; i++) {
         K e = this.enumConstants[i];
         this.enumKeys[i] = currentStyle.formatCamelCase(e.name());
      }
   }

   @Nonnull
   public EnumMapCodec<K, V> documentKey(K key, String doc) {
      this.keyDocumentation.put(key, doc);
      return this;
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
         K enumKey = this.getEnum(key);
         extraInfo.pushKey(key);

         try {
            map.put(enumKey, this.codec.decode(value, extraInfo));
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
            String key = switch (this.enumStyle) {
               case CAMEL_CASE -> this.enumKeys[entry.getKey().ordinal()];
               case LEGACY -> entry.getKey().name();
            };
            bsonDocument.put(key, value);
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
            K enumKey = this.getEnum(key);
            reader.consumeWhiteSpace();
            reader.expect(':');
            reader.consumeWhiteSpace();
            extraInfo.pushKey(key, reader);

            try {
               map.put(enumKey, this.codec.decodeJson(reader, extraInfo));
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
      ObjectSchema schema = new ObjectSchema();
      schema.getHytale().setType("EnumMap");
      schema.setTitle("Map of " + this.clazz.getSimpleName());
      StringSchema values = new StringSchema();
      schema.setPropertyNames(values);
      Map<String, Schema> properties = new Object2ObjectLinkedOpenHashMap();
      schema.setProperties(properties);
      Schema childSchema = context.refDefinition(this.codec);
      schema.setAdditionalProperties(childSchema);

      for (int i = 0; i < this.enumConstants.length; i++) {
         Schema subSchema = context.refDefinition(this.codec);
         subSchema.setMarkdownDescription(this.keyDocumentation.get(this.enumConstants[i]));
         properties.put(this.enumKeys[i], subSchema);
      }

      values.setEnum(this.enumKeys);
      return schema;
   }

   @Nullable
   protected K getEnum(String value) {
      return this.enumStyle.match(this.enumConstants, this.enumKeys, value);
   }
}
