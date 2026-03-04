package com.hypixel.hytale.server.core.modules.interaction.interaction.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.codecs.map.EnumMapCodec;
import com.hypixel.hytale.codec.exception.CodecException;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.protocol.PrioritySlot;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.bson.BsonInt32;
import org.bson.BsonValue;

public class InteractionPriorityCodec implements Codec<InteractionPriority> {
   private static final EnumMapCodec<PrioritySlot, Integer> MAP_CODEC = new EnumMapCodec<>(
      PrioritySlot.class, Codec.INTEGER, () -> new EnumMap<>(PrioritySlot.class), false
   );

   @Nonnull
   public InteractionPriority decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      if (bsonValue.isInt32()) {
         return new InteractionPriority(bsonValue.asInt32().getValue());
      } else if (bsonValue.isDocument()) {
         return new InteractionPriority(MAP_CODEC.decode(bsonValue, extraInfo));
      } else {
         throw new CodecException("Expected integer or object for InteractionPriority, got: " + bsonValue.getBsonType());
      }
   }

   @Nonnull
   public BsonValue encode(@Nonnull InteractionPriority priority, ExtraInfo extraInfo) {
      Map<PrioritySlot, Integer> values = priority.values();
      if (values != null && !values.isEmpty()) {
         return (BsonValue)(values.size() == 1 && values.containsKey(PrioritySlot.Default)
            ? new BsonInt32(values.get(PrioritySlot.Default))
            : MAP_CODEC.encode(values, extraInfo));
      } else {
         return new BsonInt32(0);
      }
   }

   @Nonnull
   public InteractionPriority decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      reader.consumeWhiteSpace();
      int peek = reader.peek();
      if (peek == 123) {
         return new InteractionPriority(MAP_CODEC.decodeJson(reader, extraInfo));
      } else if (peek != 45 && !Character.isDigit(peek)) {
         throw new CodecException("Expected integer or object for InteractionPriority, got: " + (char)peek, reader, extraInfo, null);
      } else {
         return new InteractionPriority(reader.readIntValue());
      }
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      Schema schema = MAP_CODEC.toSchema(context);
      schema.setTitle("InteractionPriority");
      schema.setDescription("Either an integer (default for all types) or an object with named priorities (e.g., 'MainHand', 'OffHand', 'Default').");
      return schema;
   }
}
