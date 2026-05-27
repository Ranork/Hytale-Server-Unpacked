package com.hypixel.hytale.builtin.worldgen.modifier.content.cave;

import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import javax.annotation.Nonnull;

public interface CaveTypeGenerator {
   String TYPE_KEY = "Type";
   CodecMapCodec<CaveTypeGenerator> TYPE_CODEC = new CodecMapCodec<>("Type");

   @Nonnull
   CaveType create(@Nonnull SeedString<SeedStringResource> var1, @Nonnull CaveTypeContent var2);
}
