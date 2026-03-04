package com.hypixel.hytale.server.core.codec.protocol;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.exception.CodecException;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.protocol.ColorAlpha;
import com.hypixel.hytale.server.core.asset.util.ColorParseUtil;
import java.io.IOException;
import javax.annotation.Nonnull;
import org.bson.BsonString;
import org.bson.BsonValue;

public class ColorAlphaCodec implements Codec<ColorAlpha> {
   @Nonnull
   public BsonValue encode(ColorAlpha colorAlpha, ExtraInfo extraInfo) {
      return new BsonString(ColorParseUtil.colorToHexAlphaString(colorAlpha));
   }

   @Nonnull
   public ColorAlpha decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      ColorAlpha colorAlpha = ColorParseUtil.parseColorAlpha(bsonValue.asString().getValue());
      if (colorAlpha != null) {
         return colorAlpha;
      } else {
         throw new CodecException("Invalid color format, expected: #RGBA, #RRGGBBAA, rgba(#RGB,A), rgba(#RRGGBB,A) or rgba(R,G,B,A)");
      }
   }

   @Nonnull
   public ColorAlpha decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      reader.expect('"');
      ColorAlpha colorAlpha = ColorParseUtil.readColorAlpha(reader);
      reader.expect('"');
      if (colorAlpha != null) {
         return colorAlpha;
      } else {
         throw new CodecException("Invalid color format, expected: #RGBA, #RRGGBBAA, rgba(#RGB,A), rgba(#RRGGBB,A) or rgba(R,G,B,A)");
      }
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      StringSchema hex = new StringSchema();
      hex.setPattern(ColorParseUtil.HEX_COLOR_PATTERN);
      StringSchema hexAlpha = new StringSchema();
      hexAlpha.setPattern(ColorParseUtil.HEX_ALPHA_COLOR_PATTERN);
      StringSchema rgbaHex = new StringSchema();
      rgbaHex.setPattern(ColorParseUtil.RGBA_HEX_COLOR_PATTERN);
      StringSchema rgba = new StringSchema();
      rgba.setPattern(ColorParseUtil.RGBA_COLOR_PATTERN);
      Schema s = Schema.anyOf(hex, hexAlpha, rgbaHex, rgba);
      s.setTitle("Color RGBA");
      s.getHytale().setType("ColorAlpha");
      return s;
   }
}
