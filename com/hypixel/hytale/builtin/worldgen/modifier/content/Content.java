package com.hypixel.hytale.builtin.worldgen.modifier.content;

import com.google.gson.JsonElement;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import javax.annotation.Nonnull;

public interface Content {
   String TYPE_KEY = "Type";
   Content[] EMPTY_ARRAY = new Content[0];
   CodecMapCodec<Content> TYPE_CODEC = new CodecMapCodec<>("Type", true);
   ArrayCodec<Content> ARRAY_CODEC = new ArrayCodec<>(TYPE_CODEC, Content[]::new);

   @Nonnull
   JsonElement get();
}
