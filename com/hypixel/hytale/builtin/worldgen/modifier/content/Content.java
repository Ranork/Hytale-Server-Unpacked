package com.hypixel.hytale.builtin.worldgen.modifier.content;

import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvent;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;

public interface Content {
   String TYPE_KEY = "Type";
   Content[] EMPTY_ARRAY = new Content[0];
   CodecMapCodec<Content> TYPE_CODEC = new CodecMapCodec<>("Type");
   ArrayCodec<Content> ARRAY_CODEC = new ArrayCodec<>(TYPE_CODEC, Content[]::new);

   Content.Type type();

   <T> void applyTo(ModifyEvent<T> var1) throws Exception;

   public static enum Type {
      NONE,
      BIOME_COVER,
      BIOME_ENVIRONMENT,
      BIOME_FLUID,
      BIOME_DYNAMIC_LAYER,
      BIOME_STATIC_LAYER,
      BIOME_PREFAB,
      BIOME_TINT,
      CAVE_TYPE,
      CAVE_COVER,
      CAVE_PREFAB;

      public static final EnumCodec<Content.Type> CODEC = new EnumCodec<>(Content.Type.class);
      public static final String KEY = "ContentType";
      public static final Content.Type[] VALUES = values();
   }
}
