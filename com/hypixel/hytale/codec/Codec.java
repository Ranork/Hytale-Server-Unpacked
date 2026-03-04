package com.hypixel.hytale.codec;

import com.hypixel.hytale.codec.codecs.BsonDocumentCodec;
import com.hypixel.hytale.codec.codecs.UUIDBinaryCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.array.DoubleArrayCodec;
import com.hypixel.hytale.codec.codecs.array.FloatArrayCodec;
import com.hypixel.hytale.codec.codecs.array.IntArrayCodec;
import com.hypixel.hytale.codec.codecs.array.LongArrayCodec;
import com.hypixel.hytale.codec.codecs.simple.BooleanCodec;
import com.hypixel.hytale.codec.codecs.simple.ByteCodec;
import com.hypixel.hytale.codec.codecs.simple.DoubleCodec;
import com.hypixel.hytale.codec.codecs.simple.FloatCodec;
import com.hypixel.hytale.codec.codecs.simple.IntegerCodec;
import com.hypixel.hytale.codec.codecs.simple.LongCodec;
import com.hypixel.hytale.codec.codecs.simple.ShortCodec;
import com.hypixel.hytale.codec.codecs.simple.StringCodec;
import com.hypixel.hytale.codec.exception.CodecException;
import com.hypixel.hytale.codec.function.FunctionCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.SchemaConvertable;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonBinary;
import org.bson.BsonValue;

public interface Codec<T> extends RawJsonCodec<T>, SchemaConvertable<T> {
   @Deprecated
   BsonDocumentCodec BSON_DOCUMENT = new BsonDocumentCodec();
   StringCodec STRING = new StringCodec();
   BooleanCodec BOOLEAN = new BooleanCodec();
   DoubleCodec DOUBLE = new DoubleCodec();
   FloatCodec FLOAT = new FloatCodec();
   ByteCodec BYTE = new ByteCodec();
   ShortCodec SHORT = new ShortCodec();
   IntegerCodec INTEGER = new IntegerCodec();
   LongCodec LONG = new LongCodec();
   Pattern BASE64_PATTERN = Pattern.compile("^[0-9a-zA-Z+/]+$");
   @Deprecated
   Codec<byte[]> BYTE_ARRAY = new Codec<byte[]>() {
      public byte[] decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
         return bsonValue.asBinary().getData();
      }

      @Nonnull
      public BsonValue encode(@Nonnull byte[] bytes, ExtraInfo extraInfo) {
         return new BsonBinary(bytes);
      }

      @Nullable
      public byte[] decodeJson(RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
         reader.expect('[');
         reader.consumeWhiteSpace();
         if (reader.tryConsume(']')) {
            return new byte[0];
         } else {
            int i = 0;
            byte[] arr = new byte[10];

            while (true) {
               if (i == arr.length) {
                  arr = Arrays.copyOf(arr, i + 1 + (i >> 1));
               }

               extraInfo.pushIntKey(i, reader);

               try {
                  arr[i] = reader.readByteValue();
                  i++;
               } catch (Exception var9) {
                  throw new CodecException("Failed to decode", reader, extraInfo, var9);
               } finally {
                  extraInfo.popKey();
               }

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
         StringSchema base64 = new StringSchema();
         base64.setPattern(BASE64_PATTERN);
         base64.setTitle("Binary");
         return base64;
      }
   };
   DoubleArrayCodec DOUBLE_ARRAY = new DoubleArrayCodec();
   FloatArrayCodec FLOAT_ARRAY = new FloatArrayCodec();
   IntArrayCodec INT_ARRAY = new IntArrayCodec();
   LongArrayCodec LONG_ARRAY = new LongArrayCodec();
   ArrayCodec<String> STRING_ARRAY = new ArrayCodec<>(STRING, String[]::new);
   FunctionCodec<String, Path> PATH = new FunctionCodec<>(STRING, x$0 -> Paths.get(x$0), Path::toString);
   FunctionCodec<String, Instant> INSTANT = new FunctionCodec<>(STRING, Instant::parse, Instant::toString);
   FunctionCodec<String, Duration> DURATION = new FunctionCodec<>(STRING, Duration::parse, Duration::toString);
   FunctionCodec<Double, Duration> DURATION_SECONDS = new FunctionCodec<>(
      DOUBLE, v -> Duration.ofNanos((long)(v * TimeUnit.SECONDS.toNanos(1L))), v -> v == null ? null : (double)v.toNanos() / TimeUnit.SECONDS.toNanos(1L)
   );
   FunctionCodec<String, Level> LOG_LEVEL = new FunctionCodec<>(STRING, Level::parse, Level::toString);
   UUIDBinaryCodec UUID_BINARY = new UUIDBinaryCodec();
   FunctionCodec<String, UUID> UUID_STRING = new FunctionCodec<>(STRING, UUID::fromString, UUID::toString);

   @Nullable
   @Deprecated
   default T decode(BsonValue bsonValue) {
      return this.decode(bsonValue, EmptyExtraInfo.EMPTY);
   }

   @Nullable
   T decode(BsonValue var1, ExtraInfo var2);

   @Deprecated
   default BsonValue encode(T t) {
      return this.encode(t, EmptyExtraInfo.EMPTY);
   }

   BsonValue encode(T var1, ExtraInfo var2);

   @Nullable
   @Override
   default T decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      System.err.println("decodeJson: " + this.getClass());
      BsonValue bsonValue = RawJsonReader.readBsonValue(reader);
      return this.decode(bsonValue, extraInfo);
   }

   static boolean isNullBsonValue(@Nullable BsonValue bsonValue) {
      return bsonValue == null || bsonValue.isNull();
   }
}
