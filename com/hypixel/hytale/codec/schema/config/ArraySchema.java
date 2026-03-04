package com.hypixel.hytale.codec.schema.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonValue;

public class ArraySchema extends Schema {
   public static final BuilderCodec<ArraySchema> CODEC = BuilderCodec.builder(ArraySchema.class, ArraySchema::new, Schema.BASE_CODEC)
      .addField(new KeyedCodec<>("items", new ArraySchema.ItemOrItems(), false, true), (o, i) -> o.items = i, o -> o.items)
      .addField(new KeyedCodec<>("minItems", Codec.INTEGER, false, true), (o, i) -> o.minItems = i, o -> o.minItems)
      .addField(new KeyedCodec<>("maxItems", Codec.INTEGER, false, true), (o, i) -> o.maxItems = i, o -> o.maxItems)
      .addField(new KeyedCodec<>("uniqueItems", Codec.BOOLEAN, false, true), (o, i) -> o.uniqueItems = i, o -> o.uniqueItems)
      .build();
   private Object items;
   private Integer minItems;
   private Integer maxItems;
   private Boolean uniqueItems;

   public ArraySchema() {
   }

   public ArraySchema(Schema item) {
      this.setItem(item);
   }

   @Nullable
   public Object getItems() {
      return this.items;
   }

   public void setItem(Schema items) {
      this.items = items;
   }

   public void setItems(Schema... items) {
      this.items = items;
   }

   @Nullable
   public Integer getMinItems() {
      return this.minItems;
   }

   public void setMinItems(Integer minItems) {
      this.minItems = minItems;
   }

   @Nullable
   public Integer getMaxItems() {
      return this.maxItems;
   }

   public void setMaxItems(Integer maxItems) {
      this.maxItems = maxItems;
   }

   public boolean getUniqueItems() {
      return this.uniqueItems;
   }

   public void setUniqueItems(boolean uniqueItems) {
      this.uniqueItems = uniqueItems;
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
         ArraySchema that = (ArraySchema)o;
         if (this.items != null ? this.items.equals(that.items) : that.items == null) {
            if (this.minItems != null ? this.minItems.equals(that.minItems) : that.minItems == null) {
               if (this.maxItems != null ? this.maxItems.equals(that.maxItems) : that.maxItems == null) {
                  return this.uniqueItems != null ? this.uniqueItems.equals(that.uniqueItems) : that.uniqueItems == null;
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
      result = 31 * result + (this.items != null ? this.items.hashCode() : 0);
      result = 31 * result + (this.minItems != null ? this.minItems.hashCode() : 0);
      result = 31 * result + (this.maxItems != null ? this.maxItems.hashCode() : 0);
      return 31 * result + (this.uniqueItems != null ? this.uniqueItems.hashCode() : 0);
   }

   @Deprecated
   private static class ItemOrItems implements Codec<Object> {
      @Nonnull
      private ArrayCodec<Schema> array = new ArrayCodec<>(Schema.CODEC, Schema[]::new);

      @Override
      public Object decode(@Nonnull BsonValue bsonValue, @Nonnull ExtraInfo extraInfo) {
         return bsonValue.isArray() ? this.array.decode(bsonValue, extraInfo) : Schema.CODEC.decode(bsonValue, extraInfo);
      }

      @Override
      public BsonValue encode(Object o, ExtraInfo extraInfo) {
         return o instanceof Schema[] ? this.array.encode((Schema[])o, extraInfo) : Schema.CODEC.encode((Schema)o, extraInfo);
      }

      @Nonnull
      @Override
      public Schema toSchema(@Nonnull SchemaContext context) {
         return Schema.anyOf(Schema.CODEC.toSchema(context), this.array.toSchema(context));
      }
   }
}
