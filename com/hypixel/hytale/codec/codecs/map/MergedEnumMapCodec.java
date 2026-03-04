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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;
import org.bson.BsonValue;

public class MergedEnumMapCodec<K extends Enum<K>, V, M extends Enum<M>> implements Codec<Map<K, V>>, WrappedCodec<V> {
   @Nonnull
   private final Class<K> clazz;
   private final K[] enumConstants;
   @Nonnull
   private final String[] enumKeys;
   @Nonnull
   private final Class<M> mergeClazz;
   private final M[] mergeEnumConstants;
   @Nonnull
   private final String[] mergeEnumKeys;
   private final Function<M, K[]> unmergeFunction;
   private final BiFunction<V, V, V> mergeResultFunction;
   private final EnumCodec.EnumStyle enumStyle;
   private final Codec<V> codec;
   private final Supplier<EnumMap<K, V>> supplier;
   private final boolean unmodifiable;

   public MergedEnumMapCodec(
      @Nonnull Class<K> clazz, @Nonnull Class<M> mergeClass, Function<M, K[]> unmergeFunction, BiFunction<V, V, V> mergeResultFunction, Codec<V> codec
   ) {
      this(clazz, EnumCodec.EnumStyle.CAMEL_CASE, mergeClass, unmergeFunction, mergeResultFunction, codec, () -> new EnumMap<>(clazz), true);
   }

   public MergedEnumMapCodec(
      @Nonnull Class<K> clazz,
      @Nonnull Class<M> mergeClass,
      Function<M, K[]> unmergeFunction,
      BiFunction<V, V, V> mergeResultFunction,
      Codec<V> codec,
      Supplier<EnumMap<K, V>> supplier
   ) {
      this(clazz, EnumCodec.EnumStyle.CAMEL_CASE, mergeClass, unmergeFunction, mergeResultFunction, codec, supplier, true);
   }

   public MergedEnumMapCodec(
      @Nonnull Class<K> clazz,
      @Nonnull Class<M> mergeClass,
      Function<M, K[]> unmergeFunction,
      BiFunction<V, V, V> mergeResultFunction,
      Codec<V> codec,
      Supplier<EnumMap<K, V>> supplier,
      boolean unmodifiable
   ) {
      this(clazz, EnumCodec.EnumStyle.CAMEL_CASE, mergeClass, unmergeFunction, mergeResultFunction, codec, supplier, unmodifiable);
   }

   public MergedEnumMapCodec(
      @Nonnull Class<K> clazz,
      EnumCodec.EnumStyle enumStyle,
      @Nonnull Class<M> mergeClass,
      Function<M, K[]> unmergeFunction,
      BiFunction<V, V, V> mergeResultFunction,
      Codec<V> codec,
      Supplier<EnumMap<K, V>> supplier,
      boolean unmodifiable
   ) {
      this.clazz = clazz;
      this.enumConstants = clazz.getEnumConstants();
      this.mergeClazz = mergeClass;
      this.mergeEnumConstants = mergeClass.getEnumConstants();
      this.unmergeFunction = unmergeFunction;
      this.enumStyle = enumStyle;
      this.mergeResultFunction = mergeResultFunction;
      this.codec = codec;
      this.supplier = supplier;
      this.unmodifiable = unmodifiable;
      EnumCodec.EnumStyle currentStyle = EnumCodec.EnumStyle.detect(this.enumConstants);
      this.enumKeys = new String[this.enumConstants.length];

      for (int i = 0; i < this.enumConstants.length; i++) {
         K e = this.enumConstants[i];
         this.enumKeys[i] = currentStyle.formatCamelCase(e.name());
      }

      EnumCodec.EnumStyle currentMergeStyle = EnumCodec.EnumStyle.detect(this.mergeEnumConstants);
      this.mergeEnumKeys = new String[this.mergeEnumConstants.length];

      for (int i = 0; i < this.mergeEnumConstants.length; i++) {
         M e = this.mergeEnumConstants[i];
         this.mergeEnumKeys[i] = currentMergeStyle.formatCamelCase(e.name());
      }
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
         extraInfo.pushKey(key);

         try {
            V decode = this.codec.decode(value, extraInfo);
            this.put0(map, key, decode);
         } catch (Exception var13) {
            throw new CodecException("Failed to decode", value, extraInfo, var13);
         } finally {
            extraInfo.popKey();
         }
      }

      if (this.unmodifiable) {
         map = Collections.unmodifiableMap(map);
      }

      return map;
   }

   private void put0(@Nonnull Map<K, V> map, String key, V decode) {
      K k = this.getEnum(key);
      if (k != null) {
         V v = map.get(k);
         if (v == null) {
            map.put(k, decode);
         } else {
            map.put(k, this.mergeResultFunction.apply(v, decode));
         }
      } else {
         K[] mergedEnum = this.getMergedEnum(key);
         if (mergedEnum != null) {
            for (K merged : mergedEnum) {
               V v = map.get(merged);
               if (v == null) {
                  map.put(merged, decode);
               } else {
                  map.put(merged, this.mergeResultFunction.apply(v, decode));
               }
            }
         }
      }
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
         return (Map<K, V>)(this.unmodifiable ? Collections.emptyMap() : this.supplier.get());
      } else {
         Map<K, V> map = this.supplier.get();

         while (true) {
            String key = reader.readString();
            reader.consumeWhiteSpace();
            reader.expect(':');
            reader.consumeWhiteSpace();
            extraInfo.pushKey(key, reader);

            try {
               V decode = this.codec.decodeJson(reader, extraInfo);
               this.put0(map, key, decode);
            } catch (Exception var9) {
               throw new CodecException("Failed to decode", reader, extraInfo, var9);
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
      schema.setTitle("Merged map of " + this.clazz.getSimpleName() + " and " + this.mergeClazz.getSimpleName());
      StringSchema values = new StringSchema();
      schema.setPropertyNames(values);
      Schema childSchema = context.refDefinition(this.codec);
      Map<String, Schema> properties = new Object2ObjectLinkedOpenHashMap();
      schema.setProperties(properties);
      schema.setAdditionalProperties(childSchema);
      String[] enum_ = new String[this.enumKeys.length + this.mergeEnumKeys.length];

      for (int i = 0; i < this.enumKeys.length; i++) {
         String entry = this.enumKeys[i];
         enum_[i] = entry;
         properties.put(entry, childSchema);
      }

      for (int i = 0; i < this.mergeEnumKeys.length; i++) {
         String entry = this.mergeEnumKeys[i];
         enum_[this.enumConstants.length + i] = entry;
         properties.put(entry, childSchema);
      }

      values.setEnum(enum_);
      return schema;
   }

   @Nullable
   protected K getEnum(String value) {
      return this.enumStyle.match(this.enumConstants, this.enumKeys, value, true);
   }

   protected K[] getMergedEnum(String value) {
      M m = this.enumStyle.match(this.mergeEnumConstants, this.mergeEnumKeys, value, true);
      return (K[])((Enum[])this.unmergeFunction.apply(m));
   }
}
