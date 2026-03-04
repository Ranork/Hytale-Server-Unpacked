package com.hypixel.hytale.server.worldgen.cave;

import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.procedurallib.condition.IBlockFluidCondition;
import com.hypixel.hytale.procedurallib.condition.ICoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.ICoordinateRndCondition;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.worldgen.cave.element.CaveNode;
import com.hypixel.hytale.server.worldgen.cave.prefab.CavePrefabContainer;
import com.hypixel.hytale.server.worldgen.cave.shape.CaveNodeShape;
import com.hypixel.hytale.server.worldgen.cave.shape.CaveNodeShapeEnum;
import com.hypixel.hytale.server.worldgen.util.BlockFluidEntry;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CaveNodeType {
   public static final CaveNodeType[] EMPTY_ARRAY = new CaveNodeType[0];
   @Nonnull
   private final String name;
   @Nullable
   private final CavePrefabContainer prefabContainer;
   @Nonnull
   private final IWeightedMap<BlockFluidEntry> fillings;
   @Nonnull
   private final CaveNodeShapeEnum.CaveNodeShapeGenerator shapeGenerator;
   @Nonnull
   private final ICoordinateCondition heightCondition;
   @Nullable
   private final IDoubleRange childrenCountBounds;
   @Nonnull
   private final CaveNodeType.CaveNodeCoverEntry[] covers;
   private final int priority;
   private final int environment;
   private CaveNodeType.CaveNodeChildEntry[] children;

   public CaveNodeType(
      @Nonnull String name,
      @Nullable CavePrefabContainer prefabContainer,
      @Nonnull IWeightedMap<BlockFluidEntry> fillings,
      @Nonnull CaveNodeShapeEnum.CaveNodeShapeGenerator shapeGenerator,
      @Nonnull ICoordinateCondition heightCondition,
      @Nullable IDoubleRange childrenCountBounds,
      @Nonnull CaveNodeType.CaveNodeCoverEntry[] covers,
      int priority,
      int environment
   ) {
      this.name = name;
      this.prefabContainer = prefabContainer;
      this.fillings = fillings;
      this.shapeGenerator = shapeGenerator;
      this.heightCondition = heightCondition;
      this.childrenCountBounds = childrenCountBounds;
      this.covers = covers;
      this.priority = priority;
      this.environment = environment;
   }

   @Nonnull
   public String getName() {
      return this.name;
   }

   @Nullable
   public CavePrefabContainer getPrefabContainer() {
      return this.prefabContainer;
   }

   public void setChildren(@Nonnull CaveNodeType.CaveNodeChildEntry[] children) {
      this.children = children;
   }

   @Nonnull
   public ICoordinateCondition getHeightCondition() {
      return this.heightCondition;
   }

   @Nullable
   public IDoubleRange getChildrenCountBounds() {
      return this.childrenCountBounds;
   }

   @Nullable
   public BlockFluidEntry getFilling(@Nonnull Random random) {
      return this.fillings.get(random);
   }

   @Nonnull
   public CaveNodeShape generateCaveNodeShape(
      Random random, CaveType caveType, CaveNode parentNode, CaveNodeType.CaveNodeChildEntry childEntry, Vector3d origin, float yaw, float pitch
   ) {
      return this.shapeGenerator.generateCaveNodeShape(random, caveType, parentNode, childEntry, origin, yaw, pitch);
   }

   @Nonnull
   public CaveNodeType.CaveNodeCoverEntry[] getCovers() {
      return this.covers;
   }

   @Nonnull
   public CaveNodeType.CaveNodeChildEntry[] getChildren() {
      return this.children;
   }

   public int getPriority() {
      return this.priority;
   }

   public boolean hasEnvironment() {
      return this.environment != Integer.MIN_VALUE;
   }

   public int getEnvironment() {
      return this.environment;
   }

   public static class CaveNodeChildEntry {
      public static final CaveNodeType.CaveNodeChildEntry[] EMPTY_ARRAY = new CaveNodeType.CaveNodeChildEntry[0];
      @Nonnull
      private final IWeightedMap<CaveNodeType> types;
      @Nonnull
      private final Vector3d anchor;
      @Nonnull
      private final Vector3d offset;
      @Nonnull
      private final PrefabRotation[] rotation;
      @Nullable
      private final IDoubleRange childrenLimit;
      @Nonnull
      private final IDoubleRange repeat;
      @Nonnull
      private final CaveNodeType.CaveNodeChildEntry.OrientationModifier pitchModifier;
      @Nonnull
      private final CaveNodeType.CaveNodeChildEntry.OrientationModifier yawModifier;
      private final double chance;
      @Nonnull
      private final CaveYawMode yawMode;

      public CaveNodeChildEntry(
         @Nonnull IWeightedMap<CaveNodeType> types,
         @Nonnull Vector3d anchor,
         @Nonnull Vector3d offset,
         @Nonnull PrefabRotation[] rotation,
         @Nullable IDoubleRange childrenLimit,
         @Nonnull IDoubleRange repeat,
         @Nonnull CaveNodeType.CaveNodeChildEntry.OrientationModifier pitchModifier,
         @Nonnull CaveNodeType.CaveNodeChildEntry.OrientationModifier yawModifier,
         double chance,
         @Nonnull CaveYawMode yawMode
      ) {
         this.types = types;
         this.anchor = anchor;
         this.offset = offset;
         this.rotation = rotation;
         this.childrenLimit = childrenLimit;
         this.repeat = repeat;
         this.pitchModifier = pitchModifier;
         this.yawModifier = yawModifier;
         this.chance = chance;
         this.yawMode = yawMode;
      }

      @Nonnull
      public IWeightedMap<CaveNodeType> getTypes() {
         return this.types;
      }

      @Nonnull
      public Vector3d getAnchor() {
         return this.anchor;
      }

      @Nonnull
      public Vector3d getOffset() {
         return this.offset;
      }

      @Nonnull
      public PrefabRotation getRotation(@Nonnull Random random) {
         return this.rotation.length == 1 ? this.rotation[0] : this.rotation[random.nextInt(this.rotation.length)];
      }

      @Nullable
      public IDoubleRange getChildrenLimit() {
         return this.childrenLimit;
      }

      @Nonnull
      public IDoubleRange getRepeat() {
         return this.repeat;
      }

      @Nonnull
      public CaveNodeType.CaveNodeChildEntry.OrientationModifier getPitchModifier() {
         return this.pitchModifier;
      }

      @Nonnull
      public CaveNodeType.CaveNodeChildEntry.OrientationModifier getYawModifier() {
         return this.yawModifier;
      }

      public double getChance() {
         return this.chance;
      }

      @Nonnull
      public CaveYawMode getYawMode() {
         return this.yawMode;
      }

      @FunctionalInterface
      public interface OrientationModifier {
         float calc(float var1, Random var2);
      }
   }

   public static class CaveNodeCoverEntry {
      public static final CaveNodeType.CaveNodeCoverEntry[] EMPTY_ARRAY = new CaveNodeType.CaveNodeCoverEntry[0];
      @Nonnull
      protected final IWeightedMap<CaveNodeType.CaveNodeCoverEntry.Entry> entries;
      @Nonnull
      protected final ICoordinateRndCondition heightCondition;
      @Nonnull
      protected final ICoordinateCondition mapCondition;
      @Nonnull
      protected final ICoordinateCondition densityCondition;
      @Nonnull
      protected final IBlockFluidCondition parentCondition;
      @Nonnull
      protected final CaveNodeType.CaveNodeCoverType type;

      public CaveNodeCoverEntry(
         @Nonnull IWeightedMap<CaveNodeType.CaveNodeCoverEntry.Entry> entries,
         @Nonnull ICoordinateRndCondition heightCondition,
         @Nonnull ICoordinateCondition mapCondition,
         @Nonnull ICoordinateCondition densityCondition,
         @Nonnull IBlockFluidCondition parentCondition,
         @Nonnull CaveNodeType.CaveNodeCoverType type
      ) {
         this.entries = entries;
         this.heightCondition = heightCondition;
         this.mapCondition = mapCondition;
         this.densityCondition = densityCondition;
         this.type = type;
         this.parentCondition = parentCondition;
      }

      @Nullable
      public CaveNodeType.CaveNodeCoverEntry.Entry get(Random random) {
         return this.entries.get(random);
      }

      @Nonnull
      public ICoordinateRndCondition getHeightCondition() {
         return this.heightCondition;
      }

      @Nonnull
      public ICoordinateCondition getMapCondition() {
         return this.mapCondition;
      }

      @Nonnull
      public ICoordinateCondition getDensityCondition() {
         return this.densityCondition;
      }

      @Nonnull
      public IBlockFluidCondition getParentCondition() {
         return this.parentCondition;
      }

      @Nonnull
      public CaveNodeType.CaveNodeCoverType getType() {
         return this.type;
      }

      public static class Entry {
         public static final CaveNodeType.CaveNodeCoverEntry.Entry[] EMPTY_ARRAY = new CaveNodeType.CaveNodeCoverEntry.Entry[0];
         protected final BlockFluidEntry entry;
         protected final int offset;

         public Entry(BlockFluidEntry entry, int offset) {
            this.entry = entry;
            this.offset = offset;
         }

         public int getOffset() {
            return this.offset;
         }

         public BlockFluidEntry getEntry() {
            return this.entry;
         }

         @Nonnull
         @Override
         public String toString() {
            return "Entry{entry=" + this.entry + ", offset=" + this.offset + "}";
         }
      }
   }

   public static enum CaveNodeCoverType {
      FLOOR(-1),
      CEILING(1);

      public final int parentOffset;

      private CaveNodeCoverType(int parentOffset) {
         this.parentOffset = parentOffset;
      }
   }
}
