package com.hypixel.hytale.server.worldgen.cave.shape;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.hypixel.hytale.server.worldgen.cave.Cave;
import com.hypixel.hytale.server.worldgen.cave.CaveNodeType;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import com.hypixel.hytale.server.worldgen.cave.element.CaveNode;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGeneratorExecution;
import com.hypixel.hytale.server.worldgen.loader.WorldGenPrefabSupplier;
import com.hypixel.hytale.server.worldgen.prefab.PrefabPasteUtil;
import com.hypixel.hytale.server.worldgen.util.bounds.IChunkBounds;
import com.hypixel.hytale.server.worldgen.util.bounds.IWorldBounds;
import com.hypixel.hytale.server.worldgen.util.condition.BlockMaskCondition;
import java.util.List;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrefabCaveNodeShape implements CaveNodeShape, IWorldBounds {
   private final CaveType caveType;
   @Nonnull
   private final Vector3d o;
   private final Vector3d e;
   @Nonnull
   private final WorldGenPrefabSupplier prefabSupplier;
   @Nonnull
   private final PrefabRotation rotation;
   private final BlockMaskCondition configuration;
   private final int lowBoundX;
   private final int lowBoundY;
   private final int lowBoundZ;
   private final int highBoundX;
   private final int highBoundY;
   private final int highBoundZ;

   public PrefabCaveNodeShape(
      CaveType caveType,
      @Nonnull Vector3d o,
      Vector3d e,
      @Nonnull WorldGenPrefabSupplier prefabSupplier,
      @Nonnull PrefabRotation rotation,
      BlockMaskCondition configuration
   ) {
      this.caveType = caveType;
      this.o = o;
      this.e = e;
      this.prefabSupplier = prefabSupplier;
      this.rotation = rotation;
      this.configuration = configuration;
      IPrefabBuffer prefab = prefabSupplier.get();
      IChunkBounds bounds = prefabSupplier.getBounds(prefab);
      this.lowBoundX = MathUtil.floor(o.x + bounds.getLowBoundX(rotation));
      this.lowBoundY = MathUtil.floor(o.y + prefab.getMinY());
      this.lowBoundZ = MathUtil.floor(o.z + bounds.getLowBoundZ(rotation));
      this.highBoundX = MathUtil.ceil(o.x + bounds.getHighBoundX(rotation));
      this.highBoundY = MathUtil.ceil(o.y + prefab.getMaxY());
      this.highBoundZ = MathUtil.ceil(o.z + bounds.getHighBoundZ(rotation));
   }

   public CaveType getCaveType() {
      return this.caveType;
   }

   @Nonnull
   public PrefabRotation getPrefabRotation() {
      return this.rotation;
   }

   @Nonnull
   public Vector3d getO() {
      return this.o;
   }

   @Nonnull
   @Override
   public Vector3d getStart() {
      return this.o.clone();
   }

   @Nonnull
   @Override
   public Vector3d getEnd() {
      return this.o.clone().add(this.e);
   }

   @Nonnull
   @Override
   public Vector3d getAnchor(@Nonnull Vector3d vector, double tx, double ty, double tz) {
      Vector3d anchor = CaveNodeShapeUtils.getBoxAnchor(vector, this, tx, ty, tz);
      double x = MathUtil.floor(anchor.x) + 0.5;
      double y = MathUtil.floor(anchor.y) + 0.5;
      double z = MathUtil.floor(anchor.z) + 0.5;
      return anchor.assign(x, y, z);
   }

   @Nonnull
   @Override
   public IWorldBounds getBounds() {
      return this;
   }

   @Override
   public int getLowBoundX() {
      return this.lowBoundX;
   }

   @Override
   public int getLowBoundZ() {
      return this.lowBoundZ;
   }

   @Override
   public int getHighBoundX() {
      return this.highBoundX;
   }

   @Override
   public int getHighBoundZ() {
      return this.highBoundZ;
   }

   @Override
   public int getLowBoundY() {
      return this.lowBoundY;
   }

   @Override
   public int getHighBoundY() {
      return this.highBoundY;
   }

   @Override
   public boolean shouldReplace(int seed, double x, double z, int y) {
      return false;
   }

   @Override
   public double getFloorPosition(int seed, double x, double z) {
      x -= this.o.x;
      z -= this.o.z;
      return this.prefabSupplier.get().getMinYAt(this.rotation, (int)x, (int)z);
   }

   @Override
   public double getCeilingPosition(int seed, double x, double z) {
      x -= this.o.x;
      z -= this.o.z;
      return this.prefabSupplier.get().getMaxYAt(this.rotation, (int)x, (int)z);
   }

   @Override
   public void populateChunk(int seed, @Nonnull ChunkGeneratorExecution execution, @Nonnull Cave cave, @Nonnull CaveNode node, Random random) {
      int x = MathUtil.floor(this.o.x);
      int y = MathUtil.floor(this.o.y);
      int z = MathUtil.floor(this.o.z);
      int cx = x - ChunkUtil.minBlock(execution.getX());
      int cz = z - ChunkUtil.minBlock(execution.getZ());
      long externalSeed = HashUtil.hash(Double.doubleToLongBits(this.o.x), Double.doubleToLongBits(this.o.z)) * 1406794441L;
      PrefabPasteUtil.PrefabPasteBuffer buffer = ChunkGenerator.getResource().prefabBuffer;
      buffer.setSeed(seed, externalSeed);
      buffer.execution = execution;
      buffer.blockMask = this.configuration;
      buffer.environmentId = node.getCaveNodeType().hasEnvironment() ? node.getCaveNodeType().getEnvironment() : this.caveType.getEnvironment();
      buffer.priority = 8;
      if (execution.getChunkGenerator().getBenchmark().isEnabled() && ChunkUtil.isInsideChunkRelative(cx, cz)) {
         execution.getChunkGenerator()
            .getBenchmark()
            .registerPrefab("CaveNode: " + cave.getCaveType().getName() + "\t" + node.getCaveNodeType().getName() + "\t" + this.prefabSupplier.getName());
      }

      PrefabPasteUtil.generate(buffer, this.rotation, this.prefabSupplier, x, y, z, cx, cz);
   }

   @Nonnull
   @Override
   public String toString() {
      return "PrefabCaveNodeShape{caveType="
         + this.caveType
         + ", o="
         + this.o
         + ", e="
         + this.e
         + ", prefabSupplier=\"prefab\", rotation="
         + this.rotation
         + ", configuration="
         + this.configuration
         + ", lowBoundX="
         + this.lowBoundX
         + ", lowBoundY="
         + this.lowBoundY
         + ", lowBoundZ="
         + this.lowBoundZ
         + ", highBoundX="
         + this.highBoundX
         + ", highBoundY="
         + this.highBoundY
         + ", highBoundZ="
         + this.highBoundZ
         + "}";
   }

   public static class PrefabCaveNodeShapeGenerator implements CaveNodeShapeEnum.CaveNodeShapeGenerator {
      private final List<WorldGenPrefabSupplier> prefabs;
      private final BlockMaskCondition configuration;

      public PrefabCaveNodeShapeGenerator(List<WorldGenPrefabSupplier> prefabs, BlockMaskCondition configuration) {
         this.prefabs = prefabs;
         this.configuration = configuration;
      }

      @Nonnull
      @Override
      public CaveNodeShape generateCaveNodeShape(
         @Nonnull Random random,
         CaveType caveType,
         @Nullable CaveNode parentNode,
         @Nonnull CaveNodeType.CaveNodeChildEntry childEntry,
         @Nonnull Vector3d origin,
         float yaw,
         float pitch
      ) {
         WorldGenPrefabSupplier prefab = this.prefabs.get(random.nextInt(this.prefabs.size()));
         if (parentNode == null) {
            PrefabRotation rotation = PrefabRotation.VALUES[random.nextInt(PrefabRotation.VALUES.length)];
            return new PrefabCaveNodeShape(caveType, origin, Vector3d.ZERO, prefab, rotation, this.configuration);
         } else {
            Vector3d offset = childEntry.getOffset().clone();
            PrefabRotation rotation = childEntry.getRotation(random);
            if (parentNode.getShape() instanceof PrefabCaveNodeShape parentShape) {
               PrefabRotation parentRotation = parentShape.getPrefabRotation();
               parentRotation.rotate(offset);
               rotation = rotation.add(parentRotation);
            }

            origin.add(offset);
            return new PrefabCaveNodeShape(caveType, origin, Vector3d.ZERO, prefab, rotation, this.configuration);
         }
      }
   }
}
