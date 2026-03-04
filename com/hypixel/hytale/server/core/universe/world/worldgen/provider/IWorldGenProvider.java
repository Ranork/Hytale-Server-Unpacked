package com.hypixel.hytale.server.core.universe.world.worldgen.provider;

import com.hypixel.hytale.codec.lookup.BuilderCodecMapCodec;
import com.hypixel.hytale.server.core.universe.world.worldgen.IWorldGen;
import com.hypixel.hytale.server.core.universe.world.worldgen.WorldGenLoadException;

public interface IWorldGenProvider {
   BuilderCodecMapCodec<IWorldGenProvider> CODEC = new BuilderCodecMapCodec<>("Type", true);

   IWorldGen getGenerator() throws WorldGenLoadException;
}
