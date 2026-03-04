package com.hypixel.hytale.codec.schema;

import com.hypixel.hytale.codec.EmptyExtraInfo;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.config.NullSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SchemaContext {
   @Nonnull
   private final Map<String, Schema> definitions = new Object2ObjectLinkedOpenHashMap();
   @Nonnull
   private final Map<String, Schema> otherDefinitions = new Object2ObjectLinkedOpenHashMap();
   @Nonnull
   private final Map<Object, String> nameMap = new Object2ObjectOpenHashMap();
   @Nonnull
   private final Object2IntMap<String> nameCollisionCount = new Object2IntOpenHashMap();
   @Nonnull
   private final Map<SchemaConvertable<?>, String> fileReferences = new Object2ObjectOpenHashMap();

   public void addFileReference(@Nonnull String fileName, @Nonnull SchemaConvertable<?> codec) {
      this.fileReferences.put(codec, fileName + "#");
   }

   @Nullable
   public Schema getFileReference(@Nonnull SchemaConvertable<?> codec) {
      String file = this.fileReferences.get(codec);
      return file != null ? Schema.ref(file) : null;
   }

   @Nonnull
   public Schema refDefinition(@Nonnull SchemaConvertable<?> codec) {
      return this.refDefinition(codec, null);
   }

   @Nonnull
   public <T> Schema refDefinition(@Nonnull SchemaConvertable<T> convertable, @Nullable T def) {
      Schema ref = this.getFileReference(convertable);
      if (ref != null) {
         return ref;
      } else if (convertable instanceof BuilderCodec<T> builderCodec) {
         String name = this.resolveName(builderCodec);
         if (!this.definitions.containsKey(name)) {
            this.definitions.put(name, NullSchema.INSTANCE);
            this.definitions.put(name, convertable.toSchema(this));
         }

         Schema c = Schema.ref("common.json#/definitions/" + name);
         if (def != null) {
            c.setDefaultRaw(builderCodec.encode(def, EmptyExtraInfo.EMPTY));
         }

         return c;
      } else if (convertable instanceof NamedSchema namedSchema) {
         String namex = this.resolveName(namedSchema);
         if (!this.otherDefinitions.containsKey(namex)) {
            this.otherDefinitions.put(namex, NullSchema.INSTANCE);
            this.otherDefinitions.put(namex, convertable.toSchema(this));
         }

         return Schema.ref("other.json#/definitions/" + namex);
      } else {
         return convertable.toSchema(this, def);
      }
   }

   @Nullable
   public Schema getRawDefinition(@Nonnull BuilderCodec<?> codec) {
      String name = this.resolveName(codec);
      return this.definitions.get(name);
   }

   @Nullable
   public Schema getRawDefinition(@Nonnull NamedSchema namedSchema) {
      return this.otherDefinitions.get(this.resolveName(namedSchema));
   }

   @Nonnull
   public Map<String, Schema> getDefinitions() {
      return this.definitions;
   }

   @Nonnull
   public Map<String, Schema> getOtherDefinitions() {
      return this.otherDefinitions;
   }

   private String resolveName(@Nonnull NamedSchema namedSchema) {
      return this.nameMap.computeIfAbsent(namedSchema, key -> {
         String n = ((NamedSchema)key).getSchemaName();
         int count = this.nameCollisionCount.getInt(n);
         this.nameCollisionCount.put(n, count + 1);
         return count > 0 ? n + "@" + count : n;
      });
   }

   @Nonnull
   private String resolveName(@Nonnull BuilderCodec<?> codec) {
      return this.nameMap.computeIfAbsent(codec.getInnerClass(), key -> {
         String n = ((Class)key).getSimpleName();
         int count = this.nameCollisionCount.getInt(n);
         this.nameCollisionCount.put(n, count + 1);
         return count > 0 ? n + "@" + count : n;
      });
   }

   static {
      Schema.init();
   }
}
