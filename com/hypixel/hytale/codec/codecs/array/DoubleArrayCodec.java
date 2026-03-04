package com.hypixel.hytale.codec.codecs.array;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.RawJsonCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.NumberSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import java.util.Arrays;
import javax.annotation.Nonnull;
import org.bson.BsonArray;
import org.bson.BsonDouble;
import org.bson.BsonValue;

public class DoubleArrayCodec implements Codec<double[]>, RawJsonCodec<double[]> {
   public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];

   public double[] decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      BsonArray array = bsonValue.asArray();
      double[] doubles = new double[array.size()];

      for (int i = 0; i < doubles.length; i++) {
         doubles[i] = array.get(i).asNumber().doubleValue();
      }

      return doubles;
   }

   @Nonnull
   public BsonValue encode(@Nonnull double[] doubles, ExtraInfo extraInfo) {
      BsonArray array = new BsonArray();

      for (int i = 0; i < doubles.length; i++) {
         array.add(new BsonDouble(doubles[i]));
      }

      return array;
   }

   public double[] decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      reader.expect('[');
      reader.consumeWhiteSpace();
      if (reader.tryConsume(']')) {
         return EMPTY_DOUBLE_ARRAY;
      } else {
         int i = 0;
         double[] arr = new double[10];

         while (true) {
            if (i == arr.length) {
               double[] temp = new double[i + 1 + (i >> 1)];
               System.arraycopy(arr, 0, temp, 0, i);
               arr = temp;
            }

            arr[i++] = reader.readDoubleValue();
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
      s.setItem(new NumberSchema());
      return s;
   }
}
