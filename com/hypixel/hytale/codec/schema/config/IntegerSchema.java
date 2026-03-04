package com.hypixel.hytale.codec.schema.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonValue;

public class IntegerSchema extends Schema {
   public static final BuilderCodec<IntegerSchema> CODEC = BuilderCodec.builder(IntegerSchema.class, IntegerSchema::new, Schema.BASE_CODEC)
      .addField(new KeyedCodec<>("minimum", IntegerSchema.IntegerOrSchema.INSTANCE, false, true), (o, i) -> o.minimum = i, o -> o.minimum)
      .addField(
         new KeyedCodec<>("exclusiveMinimum", IntegerSchema.IntegerOrSchema.INSTANCE, false, true), (o, i) -> o.exclusiveMinimum = i, o -> o.exclusiveMinimum
      )
      .addField(new KeyedCodec<>("maximum", IntegerSchema.IntegerOrSchema.INSTANCE, false, true), (o, i) -> o.maximum = i, o -> o.maximum)
      .addField(
         new KeyedCodec<>("exclusiveMaximum", IntegerSchema.IntegerOrSchema.INSTANCE, false, true), (o, i) -> o.exclusiveMaximum = i, o -> o.exclusiveMaximum
      )
      .addField(new KeyedCodec<>("enum", Codec.INT_ARRAY, false, true), (o, i) -> o.enum_ = i, o -> o.enum_)
      .addField(new KeyedCodec<>("const", Codec.INTEGER, false, true), (o, i) -> o.const_ = i, o -> o.const_)
      .addField(new KeyedCodec<>("default", Codec.INTEGER, false, true), (o, i) -> o.default_ = i, o -> o.default_)
      .build();
   private Object minimum;
   private Object exclusiveMinimum;
   private Object maximum;
   private Object exclusiveMaximum;
   private int[] enum_;
   private Integer const_;
   private Integer default_;

   @Nullable
   public Object getMinimum() {
      return this.minimum;
   }

   public void setMinimum(int minimum) {
      this.minimum = minimum;
   }

   @Nullable
   public Object getExclusiveMinimum() {
      return this.exclusiveMinimum;
   }

   public void setExclusiveMinimum(int exclusiveMinimum) {
      this.exclusiveMinimum = exclusiveMinimum;
   }

   @Nullable
   public Object getMaximum() {
      return this.maximum;
   }

   public void setMaximum(int maximum) {
      this.maximum = maximum;
   }

   @Nullable
   public Object getExclusiveMaximum() {
      return this.exclusiveMaximum;
   }

   public void setExclusiveMaximum(int exclusiveMaximum) {
      this.exclusiveMaximum = exclusiveMaximum;
   }

   public void setMinimum(Schema minimum) {
      this.minimum = minimum;
   }

   public void setExclusiveMinimum(Schema exclusiveMinimum) {
      this.exclusiveMinimum = exclusiveMinimum;
   }

   public void setMaximum(Schema maximum) {
      this.maximum = maximum;
   }

   public void setExclusiveMaximum(Schema exclusiveMaximum) {
      this.exclusiveMaximum = exclusiveMaximum;
   }

   public int[] getEnum() {
      return this.enum_;
   }

   public void setEnum(int[] enum_) {
      this.enum_ = enum_;
   }

   @Nullable
   public Integer getConst() {
      return this.const_;
   }

   public void setConst(Integer const_) {
      this.const_ = const_;
   }

   public Integer getDefault() {
      return this.default_;
   }

   public void setDefault(Integer default_) {
      this.default_ = default_;
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
         IntegerSchema that = (IntegerSchema)o;
         if (this.minimum != null ? this.minimum.equals(that.minimum) : that.minimum == null) {
            if (this.exclusiveMinimum != null ? this.exclusiveMinimum.equals(that.exclusiveMinimum) : that.exclusiveMinimum == null) {
               if (this.maximum != null ? this.maximum.equals(that.maximum) : that.maximum == null) {
                  if (this.exclusiveMaximum != null ? this.exclusiveMaximum.equals(that.exclusiveMaximum) : that.exclusiveMaximum == null) {
                     if (!Arrays.equals(this.enum_, that.enum_)) {
                        return false;
                     } else if (this.const_ != null ? this.const_.equals(that.const_) : that.const_ == null) {
                        return this.default_ != null ? this.default_.equals(that.default_) : that.default_ == null;
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
      result = 31 * result + (this.minimum != null ? this.minimum.hashCode() : 0);
      result = 31 * result + (this.exclusiveMinimum != null ? this.exclusiveMinimum.hashCode() : 0);
      result = 31 * result + (this.maximum != null ? this.maximum.hashCode() : 0);
      result = 31 * result + (this.exclusiveMaximum != null ? this.exclusiveMaximum.hashCode() : 0);
      result = 31 * result + Arrays.hashCode(this.enum_);
      result = 31 * result + (this.const_ != null ? this.const_.hashCode() : 0);
      return 31 * result + (this.default_ != null ? this.default_.hashCode() : 0);
   }

   @Nonnull
   public static Schema constant(int c) {
      IntegerSchema s = new IntegerSchema();
      s.setConst(c);
      return s;
   }

   @Deprecated
   private static class IntegerOrSchema implements Codec<Object> {
      private static final IntegerSchema.IntegerOrSchema INSTANCE = new IntegerSchema.IntegerOrSchema();

      @Override
      public Object decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
         return bsonValue.isNumber() ? Codec.INTEGER.decode(bsonValue, extraInfo) : Schema.CODEC.decode(bsonValue, extraInfo);
      }

      @Override
      public BsonValue encode(Object o, ExtraInfo extraInfo) {
         return o instanceof Integer ? Codec.INTEGER.encode((Integer)o, extraInfo) : Schema.CODEC.encode((Schema)o, extraInfo);
      }

      @Nonnull
      @Override
      public Schema toSchema(@Nonnull SchemaContext context) {
         return Schema.anyOf(new IntegerSchema(), Schema.CODEC.toSchema(context));
      }
   }
}
