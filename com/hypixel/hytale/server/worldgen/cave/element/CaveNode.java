package com.hypixel.hytale.server.worldgen.cave.element;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.worldgen.cave.CaveNodeType;
import com.hypixel.hytale.server.worldgen.cave.shape.CaveNodeShape;
import com.hypixel.hytale.server.worldgen.util.bounds.IWorldBounds;
import com.hypixel.hytale.server.worldgen.util.bounds.WorldBounds;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.LongConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CaveNode implements CaveElement {
   private final CaveNodeType caveNodeType;
   @Nonnull
   private final CaveNodeShape shape;
   @Nonnull
   private final WorldBounds bounds;
   private final int seedOffset;
   private final float pitch;
   private final float yaw;
   @Nullable
   private List<CavePrefab> rawCavePrefabs;
   private CavePrefab[] cavePrefabs;

   public CaveNode(int seedOffset, CaveNodeType caveNodeType, @Nonnull CaveNodeShape shape, float yaw, float pitch) {
      this.seedOffset = seedOffset;
      this.rawCavePrefabs = new ArrayList<>();
      this.caveNodeType = caveNodeType;
      this.shape = shape;
      this.yaw = yaw;
      this.pitch = pitch;
      this.bounds = new WorldBounds(shape.getBounds());
   }

   public int getSeedOffset() {
      return this.seedOffset;
   }

   public CaveNodeType getCaveNodeType() {
      return this.caveNodeType;
   }

   @Nonnull
   public CaveNodeShape getShape() {
      return this.shape;
   }

   public CavePrefab[] getCavePrefabs() {
      return this.cavePrefabs;
   }

   public float getYaw() {
      return this.yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public Vector3d getEnd() {
      return this.shape.getEnd();
   }

   public void addPrefab(@Nonnull CavePrefab prefab) {
      this.rawCavePrefabs.add(prefab);
      this.bounds.include(prefab.getBounds());
   }

   @Nonnull
   @Override
   public IWorldBounds getBounds() {
      return this.bounds;
   }

   public int getFloorPosition(int seed, double x, double z) {
      return MathUtil.floor(this.shape.getFloorPosition(seed, x, z));
   }

   public int getCeilingPosition(int seed, double x, double z) {
      return MathUtil.floor(this.shape.getCeilingPosition(seed, x, z));
   }

   public void forEachChunk(@Nonnull LongConsumer consumer) {
      int lowZ = this.bounds.getLowChunkZ();
      int highX = this.bounds.getHighChunkX();
      int highZ = this.bounds.getHighChunkZ();

      for (int x = this.bounds.getLowChunkX(); x <= highX; x++) {
         for (int z = lowZ; z <= highZ; z++) {
            consumer.accept(ChunkUtil.indexChunk(x, z));
         }
      }
   }

   public void compile() {
      this.cavePrefabs = this.rawCavePrefabs.toArray(CavePrefab[]::new);
      this.rawCavePrefabs = null;
   }

   @Nonnull
   @Override
   public String toString() {
      return "CaveNode{cavePrefabs="
         + Arrays.toString((Object[])this.cavePrefabs)
         + "rawCavePrefabs="
         + Arrays.toString((Object[])this.cavePrefabs)
         + ", caveNodeType="
         + this.caveNodeType
         + ", shape="
         + this.shape
         + ", bounds="
         + this.bounds
         + ", seedOffset="
         + this.seedOffset
         + ", pitch="
         + this.pitch
         + ", yaw="
         + this.yaw
         + "}";
   }
}
