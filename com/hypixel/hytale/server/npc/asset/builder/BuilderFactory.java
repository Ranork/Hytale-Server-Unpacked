package com.hypixel.hytale.server.npc.asset.builder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.codec.schema.NamedSchema;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.SchemaConvertable;
import com.hypixel.hytale.codec.schema.config.ObjectSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderFactory<T> implements SchemaConvertable<Void>, NamedSchema {
   public static final String DEFAULT_TYPE = "Type";
   public static final String COMPONENT_TYPE = "Component";
   private final String typeTag;
   private final Supplier<Builder<T>> defaultBuilder;
   private final Class<T> category;
   private final Map<String, Supplier<Builder<T>>> buildersSuppliers = new HashMap<>();

   public BuilderFactory(Class<T> category, String typeTag) {
      this(category, typeTag, null);
   }

   public BuilderFactory(Class<T> category, String typeTag, Supplier<Builder<T>> defaultBuilder) {
      this.category = category;
      this.typeTag = typeTag;
      this.defaultBuilder = defaultBuilder;
      this.add("Component", () -> new BuilderComponent<>(category));
   }

   @Nonnull
   public BuilderFactory<T> add(String name, Supplier<Builder<T>> builder) {
      if (this.buildersSuppliers.containsKey(name)) {
         throw new IllegalArgumentException(String.format("Builder with name %s already exists", name));
      } else if (this.typeTag.isEmpty()) {
         throw new IllegalArgumentException("Can't add named builder to array builder factory");
      } else {
         this.buildersSuppliers.put(name, builder);
         return this;
      }
   }

   public Class<T> getCategory() {
      return this.category;
   }

   public Builder<T> createBuilder(@Nonnull JsonElement config) {
      if (!config.isJsonObject()) {
         if (this.defaultBuilder == null) {
            throw new IllegalArgumentException(String.format("Array builder must have default builder defined: %s", config));
         } else {
            return this.defaultBuilder.get();
         }
      } else {
         return this.createBuilder(config.getAsJsonObject(), this.typeTag);
      }
   }

   public String getKeyName(@Nonnull JsonElement config) {
      if (!config.isJsonObject()) {
         return "-";
      } else {
         JsonElement element = config.getAsJsonObject().get(this.typeTag);
         return element != null ? element.getAsString() : "???";
      }
   }

   @Nonnull
   public Builder<T> createBuilder(String name) {
      if (!this.buildersSuppliers.containsKey(name)) {
         throw new IllegalArgumentException(String.format("Builder %s does not exist", name));
      } else {
         Builder<T> builder = this.buildersSuppliers.get(name).get();
         if (builder.category() != this.getCategory()) {
            throw new IllegalArgumentException(
               String.format("Builder %s has category %s which does not match %s", name, builder.category().getName(), this.getCategory().getName())
            );
         } else {
            builder.setTypeName(name);
            return builder;
         }
      }
   }

   @Nullable
   public Builder<T> tryCreateDefaultBuilder() {
      return this.defaultBuilder != null ? this.defaultBuilder.get() : null;
   }

   @Nonnull
   public List<String> getBuilderNames() {
      return new ObjectArrayList(this.buildersSuppliers.keySet());
   }

   private Builder<T> createBuilder(@Nonnull JsonObject config, @Nonnull String tag) {
      if (config == null) {
         throw new IllegalArgumentException("JSON config cannot be null when creating builder");
      } else if (tag != null && !tag.trim().isEmpty()) {
         JsonElement element = config.get(tag);
         if (element == null && this.defaultBuilder != null) {
            return this.defaultBuilder.get();
         } else if (element == null) {
            throw new IllegalArgumentException(String.format("Builder tag of type %s must be supplied if no default is defined in %s", tag, config));
         } else {
            return this.createBuilder(element.getAsString());
         }
      } else {
         throw new IllegalArgumentException(String.format("Tag cannot be null or empty when creating builder with content %s", config));
      }
   }

   @Nonnull
   @Override
   public String getSchemaName() {
      return "NPCType:" + this.getCategory().getSimpleName();
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      return this.toSchema(context, false);
   }

   @Nonnull
   public Schema toSchema(@Nonnull SchemaContext context, boolean isRoot) {
      int index = 0;
      Schema[] schemas = new Schema[this.getBuilderNames().size()];
      ObjectSchema check = new ObjectSchema();
      check.setRequired(this.typeTag);
      StringSchema keys = new StringSchema();
      keys.setEnum(this.getBuilderNames().toArray(String[]::new));
      check.setProperties(Map.of(this.typeTag, keys));
      Schema root = new Schema();
      if (this.defaultBuilder == null && this.getBuilderNames().isEmpty()) {
         root.setAnyOf(schemas);
      } else {
         root.setIf(check);
         root.setThen(Schema.anyOf(schemas));
      }

      for (String builderName : this.getBuilderNames()) {
         Builder<T> builder = this.createBuilder(builderName);
         Schema schemaRef = context.refDefinition(builder);
         ObjectSchema schema = (ObjectSchema)context.getRawDefinition(builder);
         LinkedHashMap<String, Schema> newProps = new LinkedHashMap<>();
         Schema type = StringSchema.constant(builderName);
         if (builder instanceof BuilderBase) {
            type.setDescription(((BuilderBase)builder).getLongDescription());
         }

         newProps.put(this.typeTag, type);
         if (isRoot) {
            newProps.put("TestType", new StringSchema());
            newProps.put("FailReason", new StringSchema());
            newProps.put("Parameters", BuilderParameters.toSchema(context));
         }

         newProps.putAll(schema.getProperties());
         schema.setProperties(newProps);
         Schema cond = new Schema();
         ObjectSchema checkType = new ObjectSchema();
         checkType.setProperties(Map.of(this.typeTag, StringSchema.constant(builderName)));
         checkType.setRequired(this.typeTag);
         cond.setIf(checkType);
         cond.setThen(schemaRef);
         cond.setElse(false);
         schemas[index++] = cond;
      }

      if (this.defaultBuilder != null) {
         Builder<T> builderx = this.defaultBuilder.get();
         Schema schemaRefx = context.refDefinition(builderx);
         root.setElse(schemaRefx);
      } else {
         root.setElse(false);
      }

      root.setHytaleSchemaTypeField(new Schema.SchemaTypeField(this.typeTag, null, this.getBuilderNames().toArray(String[]::new)));
      root.setTitle(this.getCategory().getSimpleName());
      return root;
   }
}
