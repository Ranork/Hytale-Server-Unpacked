package com.hypixel.hytale.codec.schema.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import java.util.Arrays;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StringSchema extends Schema {
   public static final BuilderCodec<StringSchema> CODEC = BuilderCodec.builder(StringSchema.class, StringSchema::new, Schema.BASE_CODEC)
      .addField(new KeyedCodec<>("pattern", Codec.STRING, false, true), (o, i) -> o.pattern = i, o -> o.pattern)
      .addField(new KeyedCodec<>("enum", Codec.STRING_ARRAY, false, true), (o, i) -> o.enum_ = i, o -> o.enum_)
      .addField(new KeyedCodec<>("const", Codec.STRING, false, true), (o, i) -> o.const_ = i, o -> o.const_)
      .addField(new KeyedCodec<>("default", Codec.STRING, false, true), (o, i) -> o.default_ = i, o -> o.default_)
      .addField(new KeyedCodec<>("minLength", Codec.INTEGER, false, true), (o, i) -> o.minLength = i, o -> o.minLength)
      .addField(new KeyedCodec<>("maxLength", Codec.INTEGER, false, true), (o, i) -> o.maxLength = i, o -> o.maxLength)
      .addField(new KeyedCodec<>("hytaleCommonAsset", StringSchema.CommonAsset.CODEC, false, true), (o, i) -> o.hytaleCommonAsset = i, o -> o.hytaleCommonAsset)
      .addField(new KeyedCodec<>("hytaleCosmeticAsset", Codec.STRING, false, true), (o, i) -> o.hytaleCosmeticAsset = i, o -> o.hytaleCosmeticAsset)
      .build();
   private String pattern;
   private String[] enum_;
   private String const_;
   private String default_;
   private Integer minLength;
   private Integer maxLength;
   private StringSchema.CommonAsset hytaleCommonAsset;
   private String hytaleCosmeticAsset;

   public String getPattern() {
      return this.pattern;
   }

   public void setPattern(String pattern) {
      this.pattern = pattern;
   }

   public void setPattern(@Nonnull Pattern pattern) {
      if (pattern.flags() != 0) {
         throw new IllegalArgumentException("Pattern has flags set. Flags are not supported in schema.");
      } else {
         this.pattern = pattern.pattern();
      }
   }

   public Integer getMinLength() {
      return this.minLength;
   }

   public void setMinLength(int minLength) {
      this.minLength = minLength;
   }

   public Integer getMaxLength() {
      return this.maxLength;
   }

   public void setMaxLength(int maxLength) {
      this.maxLength = maxLength;
   }

   public String[] getEnum() {
      return this.enum_;
   }

   public void setEnum(String[] enum_) {
      this.enum_ = enum_;
   }

   public String getConst() {
      return this.const_;
   }

   public void setConst(String const_) {
      this.const_ = const_;
   }

   public String getDefault() {
      return this.default_;
   }

   public void setDefault(String default_) {
      this.default_ = default_;
   }

   public StringSchema.CommonAsset getHytaleCommonAsset() {
      return this.hytaleCommonAsset;
   }

   public void setHytaleCommonAsset(StringSchema.CommonAsset hytaleCommonAsset) {
      this.hytaleCommonAsset = hytaleCommonAsset;
   }

   public String getHytaleCosmeticAsset() {
      return this.hytaleCosmeticAsset;
   }

   public void setHytaleCosmeticAsset(String hytaleCosmeticAsset) {
      this.hytaleCosmeticAsset = hytaleCosmeticAsset;
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o == null || this.getClass() != o.getClass()) {
         return false;
      } else if (!super.equals(o)) {
         return false;
      } else {
         StringSchema that = (StringSchema)o;
         if (this.pattern != null ? this.pattern.equals(that.pattern) : that.pattern == null) {
            if (!Arrays.equals((Object[])this.enum_, (Object[])that.enum_)) {
               return false;
            } else if (this.const_ != null ? this.const_.equals(that.const_) : that.const_ == null) {
               if (this.default_ != null ? this.default_.equals(that.default_) : that.default_ == null) {
                  if (this.minLength != null ? this.minLength.equals(that.minLength) : that.minLength == null) {
                     if (this.maxLength != null ? this.maxLength.equals(that.maxLength) : that.maxLength == null) {
                        if (this.hytaleCommonAsset != null ? this.hytaleCommonAsset.equals(that.hytaleCommonAsset) : that.hytaleCommonAsset == null) {
                           return this.hytaleCosmeticAsset != null
                              ? this.hytaleCosmeticAsset.equals(that.hytaleCosmeticAsset)
                              : that.hytaleCosmeticAsset == null;
                        } else {
                           return false;
                        }
                     } else {
                        return false;
                     }
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + (this.pattern != null ? this.pattern.hashCode() : 0);
      result = 31 * result + Arrays.hashCode((Object[])this.enum_);
      result = 31 * result + (this.const_ != null ? this.const_.hashCode() : 0);
      result = 31 * result + (this.default_ != null ? this.default_.hashCode() : 0);
      result = 31 * result + (this.minLength != null ? this.minLength.hashCode() : 0);
      result = 31 * result + (this.maxLength != null ? this.maxLength.hashCode() : 0);
      result = 31 * result + (this.hytaleCommonAsset != null ? this.hytaleCommonAsset.hashCode() : 0);
      return 31 * result + (this.hytaleCosmeticAsset != null ? this.hytaleCosmeticAsset.hashCode() : 0);
   }

   @Nonnull
   public static Schema constant(String c) {
      StringSchema s = new StringSchema();
      s.setConst(c);
      return s;
   }

   public static class CommonAsset {
      public static final BuilderCodec<StringSchema.CommonAsset> CODEC = BuilderCodec.builder(StringSchema.CommonAsset.class, StringSchema.CommonAsset::new)
         .addField(new KeyedCodec<>("requiredRoots", Codec.STRING_ARRAY, false, true), (o, i) -> o.requiredRoots = i, o -> o.requiredRoots)
         .addField(new KeyedCodec<>("requiredExtension", Codec.STRING, false, true), (o, i) -> o.requiredExtension = i, o -> o.requiredExtension)
         .addField(new KeyedCodec<>("isUIAsset", Codec.BOOLEAN, false, true), (o, i) -> o.isUIAsset = i, o -> o.isUIAsset)
         .build();
      private String[] requiredRoots;
      private String requiredExtension;
      private boolean isUIAsset;

      public CommonAsset(String requiredExtension, boolean isUIAsset, String... requiredRoots) {
         this.requiredRoots = requiredRoots;
         this.requiredExtension = requiredExtension;
         this.isUIAsset = isUIAsset;
      }

      protected CommonAsset() {
      }

      public String[] getRequiredRoots() {
         return this.requiredRoots;
      }

      public String getRequiredExtension() {
         return this.requiredExtension;
      }

      public boolean isUIAsset() {
         return this.isUIAsset;
      }
   }
}
