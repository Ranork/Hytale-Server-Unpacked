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
import org.bson.BsonInt32;
import org.bson.BsonValue;

public class IntArrayCodec implements Codec<int[]>, RawJsonCodec<int[]> {
   public static final int[] EMPTY_INT_ARRAY = new int[0];

   public int[] decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      BsonArray array = bsonValue.asArray();
      int[] ints = new int[array.size()];

      for (int i = 0; i < ints.length; i++) {
         ints[i] = array.get(i).asInt32().getValue();
      }

      return ints;
   }

   @Nonnull
   public BsonValue encode(@Nonnull int[] ints, ExtraInfo extraInfo) {
      BsonArray array = new BsonArray();

      for (int i = 0; i < ints.length; i++) {
         array.add(new BsonInt32(ints[i]));
      }

      return array;
   }

   public int[] decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      reader.expect('[');
      reader.consumeWhiteSpace();
      if (reader.tryConsume(']')) {
         return EMPTY_INT_ARRAY;
      } else {
         int i = 0;
         int[] arr = new int[10];

         while (true) {
            if (i == arr.length) {
               int[] temp = new int[i + 1 + (i >> 1)];
               System.arraycopy(arr, 0, temp, 0, i);
               arr = temp;
            }

            arr[i++] = reader.readIntValue();
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
