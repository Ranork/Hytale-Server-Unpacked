package com.hypixel.hytale.codec.schema.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ObjectSchema extends Schema {
   public static final BuilderCodec<ObjectSchema> CODEC = BuilderCodec.builder(ObjectSchema.class, ObjectSchema::new, Schema.BASE_CODEC)
      .addField(new KeyedCodec<>("properties", new MapCodec<>(Schema.CODEC, LinkedHashMap::new), false, true), (o, i) -> o.properties = i, o -> o.properties)
      .addField(
         new KeyedCodec<>("additionalProperties", new Schema.BooleanOrSchema(), false, true), (o, i) -> o.additionalProperties = i, o -> o.additionalProperties
      )
      .addField(new KeyedCodec<>("propertyNames", StringSchema.CODEC, false, true), (o, i) -> o.propertyNames = i, o -> o.propertyNames)
      .build();
   private Map<String, Schema> properties;
   @Nullable
   private Object additionalProperties;
   private StringSchema propertyNames;
   private Schema unevaluatedProperties;

   public Map<String, Schema> getProperties() {
      return this.properties;
   }

   public void setProperties(Map<String, Schema> properties) {
      this.properties = properties;
   }

   @Nullable
   public Object getAdditionalProperties() {
      return this.additionalProperties;
   }

   public void setAdditionalProperties(boolean additionalProperties) {
      this.additionalProperties = additionalProperties;
   }

   public void setAdditionalProperties(Schema additionalProperties) {
      this.additionalProperties = additionalProperties;
   }

   public StringSchema getPropertyNames() {
      return this.propertyNames;
   }

   public void setPropertyNames(StringSchema propertyNames) {
      this.propertyNames = propertyNames;
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
         ObjectSchema that = (ObjectSchema)o;
         if (this.properties != null ? this.properties.equals(that.properties) : that.properties == null) {
            if (this.additionalProperties != null ? this.additionalProperties.equals(that.additionalProperties) : that.additionalProperties == null) {
               if (this.propertyNames != null ? this.propertyNames.equals(that.propertyNames) : that.propertyNames == null) {
                  return this.unevaluatedProperties != null
                     ? this.unevaluatedProperties.equals(that.unevaluatedProperties)
                     : that.unevaluatedProperties == null;
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
      result = 31 * result + (this.properties != null ? this.properties.hashCode() : 0);
      result = 31 * result + (this.additionalProperties != null ? this.additionalProperties.hashCode() : 0);
      result = 31 * result + (this.propertyNames != null ? this.propertyNames.hashCode() : 0);
      return 31 * result + (this.unevaluatedProperties != null ? this.unevaluatedProperties.hashCode() : 0);
   }

   @Nonnull
   @Override
   public String toString() {
      return "ObjectSchema{properties=" + this.properties + ", additionalProperties=" + this.additionalProperties + "} " + super.toString();
   }
}
