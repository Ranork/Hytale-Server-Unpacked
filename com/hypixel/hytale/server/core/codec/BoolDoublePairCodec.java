package com.hypixel.hytale.server.core.codec;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.NumberSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.common.tuple.BoolDoublePair;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.bson.BsonString;
import org.bson.BsonValue;

public class BoolDoublePairCodec implements Codec<BoolDoublePair> {
   private static final Pattern PATTERN = Pattern.compile("~?[0-9]*");

   @Nonnull
   public BoolDoublePair decode(BsonValue bsonValue, ExtraInfo extraInfo) {
      if (bsonValue instanceof BsonString) {
         String str = bsonValue.asString().getValue();
         if (str.charAt(0) == '~') {
            return str.length() == 1 ? BoolDoublePair.of(true, 0.0) : BoolDoublePair.of(true, Double.parseDouble(str.substring(1)));
         } else {
            return BoolDoublePair.of(false, Double.parseDouble(str));
         }
      } else {
         return BoolDoublePair.of(false, bsonValue.asDouble().getValue());
      }
   }

   @Nonnull
   public BsonValue encode(@Nonnull BoolDoublePair pair, ExtraInfo extraInfo) {
      return new BsonString((pair.getLeft() ? "~" : "") + pair.getRight());
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      StringSchema s = new StringSchema();
      s.setPattern(PATTERN);
      return Schema.anyOf(new NumberSchema(), s);
   }
}
