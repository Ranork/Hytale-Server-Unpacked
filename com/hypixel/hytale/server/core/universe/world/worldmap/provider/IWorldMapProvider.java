package com.hypixel.hytale.server.core.universe.world.worldmap.provider;

import com.hypixel.hytale.codec.lookup.BuilderCodecMapCodec;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldmap.IWorldMap;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapLoadException;

public interface IWorldMapProvider {
   BuilderCodecMapCodec<IWorldMapProvider> CODEC = new BuilderCodecMapCodec<>("Type", true);

   IWorldMap getGenerator(World var1) throws WorldMapLoadException;
}
