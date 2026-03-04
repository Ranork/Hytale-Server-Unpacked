package com.hypixel.hytale.codec.codecs;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.bson.BsonValue;

public class InetSocketAddressCodec implements Codec<InetSocketAddress> {
   private static final Pattern ADDRESS_PATTERN = Pattern.compile("(.*?:)?[0-9]+");
   private final int defaultPort;

   public InetSocketAddressCodec(int defaultPort) {
      this.defaultPort = defaultPort;
   }

   @Nonnull
   public InetSocketAddress decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      String decode = STRING.decode(bsonValue, extraInfo);
      return decodeString(decode, this.defaultPort);
   }

   @Nonnull
   public BsonValue encode(@Nonnull InetSocketAddress r, ExtraInfo extraInfo) {
      return STRING.encode(r.getHostString() + ":" + r.getPort(), extraInfo);
   }

   @Nonnull
   public InetSocketAddress decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      String decode = STRING.decodeJson(reader, extraInfo);
      return decodeString(decode, this.defaultPort);
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      StringSchema s = new StringSchema();
      s.setPattern(ADDRESS_PATTERN);
      return s;
   }

   @Nonnull
   private static InetSocketAddress decodeString(@Nonnull String value, int defaultPort) {
      if (value.contains(":")) {
         String[] split = value.split(":", 2);
         return new InetSocketAddress(split[0], Integer.parseInt(split[1]));
      } else {
         try {
            return new InetSocketAddress(Integer.parseInt(value));
         } catch (NumberFormatException var3) {
            return new InetSocketAddress(value, defaultPort);
         }
      }
   }
}
