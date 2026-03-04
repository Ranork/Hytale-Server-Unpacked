package com.hypixel.hytale.codec.codecs;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.exception.CodecException;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.bson.BsonBinary;
import org.bson.BsonBinarySubType;
import org.bson.BsonValue;

public class UUIDBinaryCodec implements Codec<UUID> {
   @Nonnull
   public UUID decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      BsonBinary bsonBinary = bsonValue.asBinary();
      byte subType = bsonBinary.getType();
      if (subType != BsonBinarySubType.UUID_STANDARD.getValue()) {
         throw new CodecException("Unexpected BsonBinarySubType");
      } else {
         byte[] bytes = bsonBinary.getData();
         return uuidFromBytes(bytes);
      }
   }

   @Nonnull
   public BsonValue encode(@Nonnull UUID uuid, ExtraInfo extraInfo) {
      byte[] binaryData = new byte[16];
      writeLongToArrayBigEndian(binaryData, 0, uuid.getMostSignificantBits());
      writeLongToArrayBigEndian(binaryData, 8, uuid.getLeastSignificantBits());
      return new BsonBinary(BsonBinarySubType.UUID_STANDARD, binaryData);
   }

   @Nonnull
   public UUID decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      if (reader.peekFor('"')) {
         return uuidFromHex(reader.readString());
      } else {
         reader.expect('{');
         reader.consumeWhiteSpace();
         UUID uuid = null;

         while (true) {
            String key = reader.readString();
            reader.consumeWhiteSpace();
            reader.expect(':');
            reader.consumeWhiteSpace();
            switch (key) {
               case "$binary":
                  uuid = uuidFromHex(reader.readString());
                  break;
               case "$type":
                  reader.expect('"');
                  reader.expect('0');
                  reader.expect('4');
                  reader.expect('"');
                  break;
               default:
                  throw new IOException("Unknown field '" + key + "' when decoding UUID!");
            }

            reader.consumeWhiteSpace();
            if (reader.tryConsumeOrExpect('}', ',')) {
               if (uuid == null) {
                  throw new IOException("Expected to find '$binary' field when decoding UUID!");
               }

               return uuid;
            }

            reader.consumeWhiteSpace();
         }
      }
   }

   public static void writeLongToArrayBigEndian(@Nonnull byte[] bytes, int offset, long x) {
      bytes[offset + 7] = (byte)(255L & x);
      bytes[offset + 6] = (byte)(255L & x >> 8);
      bytes[offset + 5] = (byte)(255L & x >> 16);
      bytes[offset + 4] = (byte)(255L & x >> 24);
      bytes[offset + 3] = (byte)(255L & x >> 32);
      bytes[offset + 2] = (byte)(255L & x >> 40);
      bytes[offset + 1] = (byte)(255L & x >> 48);
      bytes[offset] = (byte)(255L & x >> 56);
   }

   public static long readLongFromArrayBigEndian(@Nonnull byte[] bytes, int offset) {
      long x = 0L;
      x |= 255L & bytes[offset + 7];
      x |= (255L & bytes[offset + 6]) << 8;
      x |= (255L & bytes[offset + 5]) << 16;
      x |= (255L & bytes[offset + 4]) << 24;
      x |= (255L & bytes[offset + 3]) << 32;
      x |= (255L & bytes[offset + 2]) << 40;
      x |= (255L & bytes[offset + 1]) << 48;
      return x | (255L & bytes[offset]) << 56;
   }

   @Nonnull
   public static UUID uuidFromBytes(@Nonnull byte[] bytes) {
      if (bytes.length != 16) {
         throw new CodecException(String.format("Expected length to be 16, not %d.", bytes.length));
      } else {
         return new UUID(readLongFromArrayBigEndian(bytes, 0), readLongFromArrayBigEndian(bytes, 8));
      }
   }

   @Nonnull
   public static UUID uuidFromHex(String src) {
      return uuidFromBytes(Base64.getDecoder().decode(src));
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      StringSchema hexUUID = new StringSchema();
      hexUUID.setMinLength(24);
      hexUUID.setMaxLength(24);
      hexUUID.setPattern(Codec.BASE64_PATTERN);
      hexUUID.setTitle("UUID Binary");
      return hexUUID;
   }
}
