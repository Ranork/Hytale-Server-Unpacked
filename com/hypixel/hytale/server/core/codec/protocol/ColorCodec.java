package com.hypixel.hytale.server.core.codec.protocol;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.exception.CodecException;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.asset.util.ColorParseUtil;
import java.io.IOException;
import javax.annotation.Nonnull;
import org.bson.BsonString;
import org.bson.BsonValue;

public class ColorCodec implements Codec<Color> {
   @Nonnull
   public BsonValue encode(Color color, ExtraInfo extraInfo) {
      return new BsonString(ColorParseUtil.colorToHexString(color));
   }

   @Nonnull
   public Color decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      Color color = ColorParseUtil.parseColor(bsonValue.asString().getValue());
      if (color != null) {
         return color;
      } else {
         throw new CodecException("Invalid color format, expected: #RGB, #RRGGBB or rgb(R,G,B)");
      }
   }

   @Nonnull
   public Color decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      reader.expect('"');
      Color color = ColorParseUtil.readColor(reader);
      reader.expect('"');
      if (color != null) {
         return color;
      } else {
         throw new CodecException("Invalid color format, expected: #RGB, #RRGGBB or rgb(R,G,B)");
      }
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      StringSchema hex = new StringSchema();
      hex.setPattern(ColorParseUtil.HEX_COLOR_PATTERN);
      StringSchema rgb = new StringSchema();
      rgb.setPattern(ColorParseUtil.RGB_COLOR_PATTERN);
      Schema s = Schema.anyOf(hex, rgb);
      s.setTitle("Color RGB");
      s.getHytale().setType("Color");
      return s;
   }
}
