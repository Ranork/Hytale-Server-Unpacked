package com.hypixel.hytale.codec.codecs;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.exception.CodecException;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonValue;

public class EnumCodec<T extends Enum<T>> implements Codec<T> {
   @Nonnull
   private final Class<T> clazz;
   @Nonnull
   private final T[] enumConstants;
   @Nonnull
   private final String[] enumKeys;
   private final EnumCodec.EnumStyle enumStyle;
   @Nonnull
   private final EnumMap<T, String> documentation;

   public EnumCodec(@Nonnull Class<T> clazz) {
      this(clazz, EnumCodec.EnumStyle.CAMEL_CASE);
   }

   public EnumCodec(@Nonnull Class<T> clazz, EnumCodec.EnumStyle enumStyle) {
      this.clazz = clazz;
      this.enumConstants = clazz.getEnumConstants();
      this.enumStyle = enumStyle;
      this.documentation = new EnumMap<>(clazz);
      EnumCodec.EnumStyle currentStyle = EnumCodec.EnumStyle.detect(this.enumConstants);
      this.enumKeys = new String[this.enumConstants.length];

      for (int i = 0; i < this.enumConstants.length; i++) {
         T e = this.enumConstants[i];
         this.enumKeys[i] = currentStyle.formatCamelCase(e.name());
      }
   }

   @Nonnull
   public EnumCodec<T> documentKey(T key, String doc) {
      this.documentation.put(key, doc);
      return this;
   }

   @Nonnull
   public T decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      String decode = STRING.decode(bsonValue, extraInfo);
      T value = this.getEnum(decode);
      if (value == null) {
         throw new IllegalArgumentException("Failed to apply function to '" + decode + "' decoded from '" + bsonValue + "'!");
      } else {
         return value;
      }
   }

   @Nonnull
   public BsonValue encode(@Nonnull T r, ExtraInfo extraInfo) {
      return switch (this.enumStyle) {
         case LEGACY -> STRING.encode(r.name(), extraInfo);
         case CAMEL_CASE -> STRING.encode(this.enumKeys[r.ordinal()], extraInfo);
      };
   }

   @Nonnull
   public T decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      String decode = STRING.decodeJson(reader, extraInfo);
      T value = this.getEnum(decode);
      if (value == null) {
         throw new IllegalArgumentException("Failed to apply function to '" + decode + "'!");
      } else {
         return value;
      }
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      return this.toSchema(context, null);
   }

   @Nonnull
   public Schema toSchema(@Nonnull SchemaContext context, @Nullable T def) {
      StringSchema enumSchema = new StringSchema();
      enumSchema.setTitle(this.clazz.getSimpleName());
      enumSchema.setEnum(this.enumKeys);
      enumSchema.getHytale().setType("Enum");
      String[] documentation = new String[this.enumKeys.length];

      for (int i = 0; i < this.enumKeys.length; i++) {
         String desc = this.documentation.get(this.enumConstants[i]);
         documentation[i] = Objects.requireNonNullElse(desc, "");
      }

      enumSchema.setMarkdownEnumDescriptions(documentation);
      if (def != null) {
         enumSchema.setDefault(this.enumKeys[def.ordinal()]);
      }

      return enumSchema;
   }

   @Nullable
   private T getEnum(String value) {
      return this.enumStyle.match(this.enumConstants, this.enumKeys, value);
   }

   public static enum EnumStyle {
      @Deprecated
      LEGACY,
      CAMEL_CASE;

      @Nullable
      public <T extends Enum<T>> T match(@Nonnull T[] enumConstants, @Nonnull String[] enumKeys, String value) {
         return this.match(enumConstants, enumKeys, value, false);
      }

      @Nullable
      public <T extends Enum<T>> T match(@Nonnull T[] enumConstants, @Nonnull String[] enumKeys, String value, boolean allowInvalid) {
         switch (this) {
            case LEGACY:
               for (int ix = 0; ix < enumConstants.length; ix++) {
                  T e = enumConstants[ix];
                  if (e.name().equalsIgnoreCase(value)) {
                     return e;
                  }
               }
            case CAMEL_CASE:
               for (int i = 0; i < enumKeys.length; i++) {
                  String key = enumKeys[i];
                  if (key.equals(value)) {
                     return enumConstants[i];
                  }
               }
         }

         if (allowInvalid) {
            return null;
         } else {
            throw new CodecException("Failed to find enum value for " + value);
         }
      }

      @Nonnull
      public String formatCamelCase(@Nonnull String name) {
         return switch (this) {
            case LEGACY -> {
               StringBuilder nameParts = new StringBuilder();

               for (String part : name.split("_")) {
                  nameParts.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1).toLowerCase());
               }

               yield nameParts.toString();
            }
            case CAMEL_CASE -> name;
         };
      }

      @Nonnull
      public static <T extends Enum<T>> EnumCodec.EnumStyle detect(@Nonnull T[] enumConstants) {
         for (T e : enumConstants) {
            String name = e.name();
            if (name.length() <= 1 || !Character.isUpperCase(name.charAt(1))) {
               return CAMEL_CASE;
            }

            for (int i = 1; i < name.length(); i++) {
               char c = name.charAt(i);
               if (Character.isLetter(c) && Character.isLowerCase(c)) {
                  return CAMEL_CASE;
               }
            }
         }

         return LEGACY;
      }
   }
}
