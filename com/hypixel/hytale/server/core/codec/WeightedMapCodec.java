package com.hypixel.hytale.server.core.codec;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.WrappedCodec;
import com.hypixel.hytale.codec.exception.CodecException;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.common.map.IWeightedElement;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.common.map.WeightedMap;
import java.io.IOException;
import javax.annotation.Nonnull;
import org.bson.BsonArray;
import org.bson.BsonValue;

public class WeightedMapCodec<T extends IWeightedElement> implements Codec<IWeightedMap<T>>, WrappedCodec<T> {
   private final Codec<T> codec;
   private final T[] emptyKeys;

   public WeightedMapCodec(Codec<T> codec, T[] emptyKeys) {
      this.codec = codec;
      this.emptyKeys = emptyKeys;
   }

   @Override
   public Codec<T> getChildCodec() {
      return this.codec;
   }

   public IWeightedMap<T> decode(@Nonnull BsonValue bsonValue, @Nonnull ExtraInfo extraInfo) {
      BsonArray array = bsonValue.asArray();
      WeightedMap.Builder<T> mapBuilder = WeightedMap.builder(this.emptyKeys);
      mapBuilder.ensureCapacity(array.size());

      for (int i = 0; i < array.size(); i++) {
         BsonValue value = array.get(i);
         extraInfo.pushIntKey(i);

         try {
            T element = this.codec.decode(value, extraInfo);
            mapBuilder.put(element, element.getWeight());
         } catch (Exception var11) {
            throw new CodecException("Failed to decode", value, extraInfo, var11);
         } finally {
            extraInfo.popKey();
         }
      }

      return mapBuilder.build();
   }

   @Nonnull
   public BsonValue encode(@Nonnull IWeightedMap<T> map, ExtraInfo extraInfo) {
      BsonArray array = new BsonArray();
      map.forEach(element -> array.add(this.codec.encode(element, extraInfo)));
      return array;
   }

   public IWeightedMap<T> decodeJson(@Nonnull RawJsonReader reader, @Nonnull ExtraInfo extraInfo) throws IOException {
      reader.expect('[');
      reader.consumeWhiteSpace();
      WeightedMap.Builder<T> mapBuilder = WeightedMap.builder(this.emptyKeys);
      if (reader.tryConsume(']')) {
         return mapBuilder.build();
      } else {
         int i = 0;

         while (true) {
            extraInfo.pushIntKey(i, reader);

            try {
               T element = this.codec.decodeJson(reader, extraInfo);
               mapBuilder.put(element, element.getWeight());
            } catch (Exception var9) {
               throw new CodecException("Failed to decode", reader, extraInfo, var9);
            } finally {
               extraInfo.popKey();
            }

            reader.consumeWhiteSpace();
            if (reader.tryConsumeOrExpect(']', ',')) {
               return mapBuilder.build();
            }

            reader.consumeWhiteSpace();
         }
      }
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      ArraySchema s = new ArraySchema();
      s.setTitle("WeightedMap");
      s.setItem(context.refDefinition(this.codec));
      return s;
   }
}
