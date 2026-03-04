package com.hypixel.hytale.server.worldgen.cave.shape;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.worldgen.cave.Cave;
import com.hypixel.hytale.server.worldgen.cave.element.CaveNode;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGeneratorExecution;
import com.hypixel.hytale.server.worldgen.util.bounds.IWorldBounds;
import java.util.Random;

public interface CaveNodeShape {
   Vector3d getStart();

   Vector3d getEnd();

   Vector3d getAnchor(Vector3d var1, double var2, double var4, double var6);

   IWorldBounds getBounds();

   boolean shouldReplace(int var1, double var2, double var4, int var6);

   double getFloorPosition(int var1, double var2, double var4);

   double getCeilingPosition(int var1, double var2, double var4);

   void populateChunk(int var1, ChunkGeneratorExecution var2, Cave var3, CaveNode var4, Random var5);

   default boolean hasGeometry() {
      return true;
   }
}
