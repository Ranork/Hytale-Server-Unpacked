package com.hypixel.hytale.server.core.universe.world.chunk.section.blockpositions;

import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;

public interface IBlockPositionData {
   BlockSection getChunkSection();

   int getBlockType();

   int getX();

   int getY();

   int getZ();

   double getXCentre();

   double getYCentre();

   double getZCentre();
}
