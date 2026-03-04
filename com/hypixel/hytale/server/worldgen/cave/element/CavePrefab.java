package com.hypixel.hytale.server.worldgen.cave.element;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.procedurallib.condition.IIntCondition;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.hypixel.hytale.server.worldgen.loader.WorldGenPrefabSupplier;
import com.hypixel.hytale.server.worldgen.util.bounds.IWorldBounds;
import com.hypixel.hytale.server.worldgen.util.bounds.WorldBounds;
import com.hypixel.hytale.server.worldgen.util.condition.BlockMaskCondition;
import javax.annotation.Nonnull;

public class CavePrefab implements CaveElement {
   @Nonnull
   private final WorldGenPrefabSupplier prefabSupplier;
   @Nonnull
   private final PrefabRotation rotation;
   private final IIntCondition biomeMask;
   private final BlockMaskCondition blockMask;
   @Nonnull
   private final IWorldBounds bounds;
   private final int x;
   private final int y;
   private final int z;

   public CavePrefab(
      @Nonnull WorldGenPrefabSupplier prefabSupplier,
      @Nonnull PrefabRotation rotation,
      IIntCondition biomeMask,
      BlockMaskCondition blockMask,
      int x,
      int y,
      int z
   ) {
      this.prefabSupplier = prefabSupplier;
      this.rotation = rotation;
      this.biomeMask = biomeMask;
      this.blockMask = blockMask;
      this.x = x;
      this.y = y;
      this.z = z;
      IPrefabBuffer prefab = prefabSupplier.get();
      this.bounds = new WorldBounds(
         MathUtil.floor(x + prefab.getMinX(rotation)),
         MathUtil.floor(y + prefab.getMinY()),
         MathUtil.floor(z + prefab.getMinZ(rotation)),
         MathUtil.ceil(x + prefab.getMaxX(rotation)),
         MathUtil.ceil(y + prefab.getMaxY()),
         MathUtil.ceil(z + prefab.getMaxZ(rotation))
      );
   }

   @Nonnull
   public WorldGenPrefabSupplier getPrefab() {
      return this.prefabSupplier;
   }

   @Nonnull
   public PrefabRotation getRotation() {
      return this.rotation;
   }

   public IIntCondition getBiomeMask() {
      return this.biomeMask;
   }

   public BlockMaskCondition getConfiguration() {
      return this.blockMask;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public int getZ() {
      return this.z;
   }

   @Nonnull
   @Override
   public IWorldBounds getBounds() {
      return this.bounds;
   }
}
