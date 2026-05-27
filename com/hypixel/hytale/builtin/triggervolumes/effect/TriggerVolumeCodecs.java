package com.hypixel.hytale.builtin.triggervolumes.effect;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.logger.HytaleLogger;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonString;
import org.bson.BsonValue;

public final class TriggerVolumeCodecs {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   public static final ArrayCodec<TriggerEffect> TOLERANT_EFFECTS = tolerantArray("trigger effect", TriggerEffect.CODEC, TriggerEffect[]::new);
   @Nonnull
   public static final ArrayCodec<TriggerCondition> TOLERANT_CONDITIONS = tolerantArray("trigger condition", TriggerCondition.CODEC, TriggerCondition[]::new);
   @Nonnull
   public static final MapCodec<String, Map<String, String>> TAGS = new MapCodec<>(new TriggerVolumeCodecs.TagValueCodec(), HashMap::new, false);

   private TriggerVolumeCodecs() {
   }

   @Nonnull
   private static <T> ArrayCodec<T> tolerantArray(@Nonnull final String label, @Nonnull Codec<T> codec, @Nonnull IntFunction<T[]> arrayFactory) {
      return new ArrayCodec<T>(codec, arrayFactory) {
         @Nullable
         @Override
         protected T decodeJsonElement(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
            return (T)this.decodeElement(RawJsonReader.readBsonValue(reader), extraInfo);
         }

         @Nullable
         @Override
         protected T decodeElement(@Nonnull BsonValue value, ExtraInfo extraInfo) {
            try {
               return (T)super.decodeElement(value, extraInfo);
            } catch (Exception var4) {
               TriggerVolumeCodecs.LOGGER.at(Level.WARNING).log("Skipping unrecognized %s: %s", label, var4.getMessage());
               return null;
            }
         }
      };
   }

   private static final class TagValueCodec implements Codec<String> {
      @Nonnull
      public String decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
         return bsonValue.isArray()
            ? Arrays.stream(Codec.STRING_ARRAY.decode(bsonValue, extraInfo))
               .filter(value -> value != null && !value.isBlank())
               .map(String::trim)
               .filter(value -> !value.isEmpty())
               .findFirst()
               .orElse("Empty")
            : TriggerVolumeCodecs.TaggedValue.normalize(Codec.STRING.decode(bsonValue, extraInfo));
      }

      @Nonnull
      public BsonValue encode(@Nonnull String value, ExtraInfo extraInfo) {
         return new BsonString(TriggerVolumeCodecs.TaggedValue.normalize(value));
      }

      @Nonnull
      public String decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
         return this.decode(RawJsonReader.readBsonValue(reader), extraInfo);
      }

      @Nonnull
      @Override
      public Schema toSchema(@Nonnull SchemaContext context) {
         return Codec.STRING.toSchema(context);
      }
   }

   public static final class TaggedValue {
      public static final String EMPTY = "Empty";

      private TaggedValue() {
      }

      @Nonnull
      public static String normalize(@Nullable String value) {
         return value != null && !value.isBlank() ? value.trim() : "Empty";
      }
   }
}
