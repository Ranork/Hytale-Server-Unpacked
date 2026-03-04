package com.hypixel.hytale.server.worldgen.cave;

import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.procedurallib.condition.ConstantIntCondition;
import com.hypixel.hytale.procedurallib.condition.DefaultCoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.ICoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.IIntCondition;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.worldgen.cave.element.CaveNode;
import com.hypixel.hytale.server.worldgen.cave.element.CavePrefab;
import com.hypixel.hytale.server.worldgen.cave.prefab.CavePrefabContainer;
import com.hypixel.hytale.server.worldgen.cave.shape.CaveNodeShape;
import com.hypixel.hytale.server.worldgen.cave.shape.PrefabCaveNodeShape;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import com.hypixel.hytale.server.worldgen.chunk.ZoneBiomeResult;
import com.hypixel.hytale.server.worldgen.loader.WorldGenPrefabSupplier;
import com.hypixel.hytale.server.worldgen.util.ArrayUtli;
import com.hypixel.hytale.server.worldgen.util.condition.flag.Int2FlagsCondition;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CaveGenerator {
   private final CaveType[] caveTypes;

   public CaveGenerator(CaveType[] caveTypes) {
      this.caveTypes = caveTypes;
   }

   public CaveType[] getCaveTypes() {
      return this.caveTypes;
   }

   @Nonnull
   public Cave generate(int seed, @Nonnull ChunkGenerator chunkGenerator, @Nonnull CaveType caveType, int x, int y, int z) {
      int seedOffset = (int)HashUtil.rehash(seed, x, y, z);
      Random random = new FastRandom(seedOffset);
      Cave cave = this.newCave(caveType);
      Vector3d origin = new Vector3d(x, y, z);
      origin.y = caveType.getModifiedStartHeight(seed + seedOffset, x, y, z, random);
      this.startCave(seed, chunkGenerator, cave, origin, random);
      cave.compile();
      return cave;
   }

   @Nonnull
   protected Cave newCave(CaveType caveType) {
      return new Cave(caveType);
   }

   protected void startCave(int seed, @Nonnull ChunkGenerator chunkGenerator, @Nonnull Cave cave, @Nonnull Vector3d origin, @Nonnull Random random) {
      Int2FlagsCondition biomeMask = cave.getCaveType().getBiomeMask();
      int startBiomeMaskResult = this.getBiomeMaskResult(seed, chunkGenerator, biomeMask, origin);
      if (CaveBiomeMaskFlags.canGenerate(startBiomeMaskResult)) {
         CaveType caveType = cave.getCaveType();
         int depth = caveType.getStartDepth(random);
         CaveNodeType type = caveType.getEntryNode();
         float yaw = caveType.getStartYaw(random);
         float pitch = caveType.getStartPitch(random);
         int seedOffset = random.nextInt();
         CaveNodeShape shape = type.generateCaveNodeShape(random, caveType, null, null, origin, yaw, pitch);
         int endBiomeMaskResult = this.getBiomeMaskResult(seed, chunkGenerator, biomeMask, shape.getEnd());
         if (CaveBiomeMaskFlags.canGenerate(endBiomeMaskResult)) {
            CaveNode node = new CaveNode(seed + seedOffset, type, shape, yaw, pitch);
            if (shape.hasGeometry() && CaveBiomeMaskFlags.canPopulate(startBiomeMaskResult) && CaveBiomeMaskFlags.canPopulate(endBiomeMaskResult)) {
               cave.addNode(node);
            }

            this.continueNode(seed, chunkGenerator, cave, node, depth, random);
         }
      }
   }

   protected void continueNode(
      int seed, @Nonnull ChunkGenerator chunkGenerator, @Nonnull Cave cave, @Nonnull CaveNode parent, int depth, @Nonnull Random random
   ) {
      if (depth > 0) {
         Int2FlagsCondition biomeMask = cave.getCaveType().getBiomeMask();
         CaveNodeType.CaveNodeChildEntry[] childEntries = this.getChildEntriesRandomized(parent.getCaveNodeType(), random);
         int childrenCount = this.getChildrenCount(parent.getCaveNodeType(), random);
         int generatedChildren = 0;

         for (CaveNodeType.CaveNodeChildEntry childEntry : childEntries) {
            int repeat = this.getRepeatCounter(childEntry, random);

            for (int j = 0; j < repeat; j++) {
               if (this.shouldGenerateChild(childEntry, random)) {
                  if (generatedChildren >= childrenCount) {
                     return;
                  }

                  PrefabRotation parentRotation = this.getRotation(parent);
                  Vector3d origin = this.getChildOrigin(parent, parentRotation, childEntry);
                  CaveNodeType type = childEntry.getTypes().get(random);
                  if (this.isMatchingHeight(seed, origin, type.getHeightCondition())) {
                     float yaw = this.getChildYaw(parent, parentRotation, childEntry, random);
                     float pitch = childEntry.getPitchModifier().calc(parent.getPitch(), random);
                     int hash = random.nextInt();
                     CaveNodeShape shape = type.generateCaveNodeShape(random, cave.getCaveType(), parent, childEntry, origin, yaw, pitch);
                     if (this.isMatchingHeight(seed, shape.getEnd(), type.getHeightCondition())) {
                        int biomeMaskResult = this.getBiomeMaskResult(seed, chunkGenerator, biomeMask, shape.getEnd());
                        if (!CaveBiomeMaskFlags.canGenerate(biomeMaskResult)) {
                           if (!CaveBiomeMaskFlags.canContinue(biomeMaskResult)) {
                              break;
                           }
                        } else {
                           CaveNode node = new CaveNode(hash, type, shape, yaw, pitch);
                           if (shape.hasGeometry() && CaveBiomeMaskFlags.canPopulate(biomeMaskResult)) {
                              this.generatePrefabs(seed, chunkGenerator, parent, node);
                              cave.addNode(node);
                           }

                           int nextDepth = this.getNextDepth(childEntry, depth, random);
                           this.continueNode(seed, chunkGenerator, cave, node, nextDepth, random);
                           generatedChildren++;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   protected int getChildrenCount(@Nonnull CaveNodeType type, Random random) {
      IDoubleRange countArray = type.getChildrenCountBounds();
      return countArray == null ? Integer.MAX_VALUE : MathUtil.floor(countArray.getValue(random));
   }

   @Nonnull
   protected CaveNodeType.CaveNodeChildEntry[] getChildEntriesRandomized(@Nonnull CaveNodeType type, @Nonnull Random random) {
      CaveNodeType.CaveNodeChildEntry[] childEntries = type.getChildren();
      if (type.getChildrenCountBounds() != null && childEntries.length != 0) {
         CaveNodeType.CaveNodeChildEntry[] randomized = new CaveNodeType.CaveNodeChildEntry[childEntries.length];
         System.arraycopy(childEntries, 0, randomized, 0, randomized.length);
         ArrayUtli.shuffleArray((CaveNodeType.CaveNodeChildEntry[])randomized, random);
         return randomized;
      } else {
         return childEntries;
      }
   }

   protected int getRepeatCounter(@Nonnull CaveNodeType.CaveNodeChildEntry entry, Random random) {
      return MathUtil.floor(entry.getRepeat().getValue(random));
   }

   @Nullable
   protected PrefabRotation getRotation(@Nonnull CaveNode caveNode) {
      CaveNodeShape shape = caveNode.getShape();
      return shape instanceof PrefabCaveNodeShape ? ((PrefabCaveNodeShape)shape).getPrefabRotation() : null;
   }

   protected Vector3d getChildOrigin(@Nonnull CaveNode parentNode, @Nullable PrefabRotation parentRotation, @Nonnull CaveNodeType.CaveNodeChildEntry childEntry) {
      Vector3d vector = parentNode.getEnd();
      Vector3d anchor = childEntry.getAnchor();
      if (anchor == Vector3d.ZERO) {
         return vector;
      } else {
         vector.assign(anchor);
         if (parentRotation != null && parentRotation != PrefabRotation.ROTATION_0) {
            vector.subtract(0.5, 0.5, 0.5);
            parentRotation.rotate(vector);
            vector.add(0.5, 0.5, 0.5);
         }

         return parentNode.getShape().getAnchor(vector, vector.x, vector.y, vector.z);
      }
   }

   protected float getChildYaw(
      @Nonnull CaveNode parentNode, @Nullable PrefabRotation parentRotation, @Nonnull CaveNodeType.CaveNodeChildEntry childEntry, Random random
   ) {
      float yaw = childEntry.getYawMode().combine(parentNode.getYaw(), parentRotation);
      return childEntry.getYawModifier().calc(yaw, random);
   }

   protected boolean shouldGenerateChild(@Nonnull CaveNodeType.CaveNodeChildEntry entry, @Nonnull Random random) {
      return random.nextDouble() < entry.getChance();
   }

   protected boolean isMatchingHeight(int seed, @Nonnull Vector3d vec, @Nonnull ICoordinateCondition condition) {
      if (condition == DefaultCoordinateCondition.DEFAULT_TRUE) {
         return true;
      } else if (condition == DefaultCoordinateCondition.DEFAULT_FALSE) {
         return false;
      } else {
         int x = MathUtil.floor(vec.x);
         int y = MathUtil.floor(vec.y);
         int z = MathUtil.floor(vec.z);
         return condition.eval(seed, x, y, z);
      }
   }

   protected int getNextDepth(@Nonnull CaveNodeType.CaveNodeChildEntry entry, int depth, Random random) {
      int nextDepth = depth - 1;
      if (entry.getChildrenLimit() != null) {
         int limit = MathUtil.floor(entry.getChildrenLimit().getValue(random));
         if (limit < nextDepth) {
            return limit;
         }
      }

      return nextDepth;
   }

   protected void generatePrefabs(int seed, @Nonnull ChunkGenerator chunkGenerator, CaveNode parent, @Nonnull CaveNode node) {
      Random random = ChunkGenerator.getResource().getRandom();
      random.setSeed(seed + node.getSeedOffset());
      CavePrefabContainer container = node.getCaveNodeType().getPrefabContainer();
      if (container != null) {
         for (CavePrefabContainer.CavePrefabEntry entry : container.getEntries()) {
            this.generatePrefab(seed, chunkGenerator, parent, node, entry, random);
         }
      }
   }

   protected void generatePrefab(
      int seed,
      @Nonnull ChunkGenerator chunkGenerator,
      @Nullable CaveNode parent,
      @Nonnull CaveNode caveNode,
      @Nonnull CavePrefabContainer.CavePrefabEntry entry,
      @Nonnull Random random
   ) {
      CavePrefabContainer.CavePrefabEntry.CavePrefabConfig config = entry.getConfig();
      int iterations = config.getIterations(random.nextDouble());

      for (int i = 0; i < iterations; i++) {
         int x = caveNode.getBounds().randomX(random);
         int z = caveNode.getBounds().randomZ(random);
         if (this.isMatchingBiome(seed, chunkGenerator, config.getBiomeMask(), x, z) && config.isMatchingNoiseDensity(seed, x, z)) {
            int y = config.getHeight(seed, x, z, caveNode);
            if (y != -1 && config.isMatchingHeight(seed, x, y, z, random) && (parent == null || !parent.getShape().shouldReplace(seed, x, z, y))) {
               WorldGenPrefabSupplier prefab = entry.getPrefab(random.nextDouble());
               PrefabRotation rotation = config.getRotation(random);
               CavePrefab entity = new CavePrefab(prefab, rotation, config.getBiomeMask(), config.getBlockMask(), x, y, z);
               caveNode.addPrefab(entity);
            }
         }
      }
   }

   protected boolean isMatchingBiome(int seed, @Nonnull ChunkGenerator chunkGenerator, @Nonnull IIntCondition condition, int x, int z) {
      if (condition == ConstantIntCondition.DEFAULT_TRUE) {
         return true;
      } else if (condition == ConstantIntCondition.DEFAULT_FALSE) {
         return false;
      } else {
         ZoneBiomeResult biomeResult = chunkGenerator.getZoneBiomeResultAt(seed, x, z);
         return condition.eval(biomeResult.getBiome().getId());
      }
   }

   protected int getBiomeMaskResult(int seed, @Nonnull ChunkGenerator chunkGenerator, @Nonnull Int2FlagsCondition mask, @Nonnull Vector3d vec) {
      if (mask == CaveBiomeMaskFlags.DEFAULT_ALLOW) {
         return 7;
      } else if (mask == CaveBiomeMaskFlags.DEFAULT_DENY) {
         return 0;
      } else {
         int x = MathUtil.floor(vec.getX());
         int z = MathUtil.floor(vec.getZ());
         ZoneBiomeResult biomeResult = chunkGenerator.getZoneBiomeResultAt(seed, x, z);
         return mask.eval(biomeResult.getBiome().getId());
      }
   }
}
