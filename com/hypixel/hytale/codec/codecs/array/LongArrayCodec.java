package com.hypixel.hytale.codec.codecs.array;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.RawJsonCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.IntegerSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import java.util.Arrays;
import javax.annotation.Nonnull;
import org.bson.BsonArray;
import org.bson.BsonInt64;
import org.bson.BsonValue;

public class LongArrayCodec implements Codec<long[]>, RawJsonCodec<long[]> {
   public static final long[] EMPTY_LONG_ARRAY = new long[0];

   public long[] decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      BsonArray array = bsonValue.asArray();
      long[] longs = new long[array.size()];

      for (int i = 0; i < longs.length; i++) {
         longs[i] = array.get(i).asInt64().getValue();
      }

      return longs;
   }

   @Nonnull
   public BsonValue encode(@Nonnull long[] longs, ExtraInfo extraInfo) {
      BsonArray array = new BsonArray();

      for (int i = 0; i < longs.length; i++) {
         array.add(new BsonInt64(longs[i]));
      }

      return array;
   }

   public long[] decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      reader.expect('[');
      reader.consumeWhiteSpace();
      if (reader.tryConsume(']')) {
         return EMPTY_LONG_ARRAY;
      } else {
         int i = 0;
         long[] arr = new long[10];

         while (true) {
            if (i == arr.length) {
               long[] temp = new long[i + 1 + (i >> 1)];
               System.arraycopy(arr, 0, temp, 0, i);
               arr = temp;
            }

            arr[i++] = reader.readLongValue();
            reader.consumeWhiteSpace();
            if (reader.tryConsumeOrExpect(']', ',')) {
               if (arr.length == i) {
                  return arr;
               }

               return Arrays.copyOf(arr, i);
            }

            reader.consumeWhiteSpace();
         }
      }
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      ArraySchema s = new ArraySchema();
      s.setItem(new IntegerSchema());
      return s;
   }
}
