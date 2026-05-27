package com.hypixel.hytale.builtin.worldgen.modifier.content.prefab;

import com.hypixel.hytale.builtin.worldgen.modifier.content.Codecs;
import com.hypixel.hytale.builtin.worldgen.modifier.content.Content;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvent;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvents;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.math.range.IntRange;
import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.procedurallib.condition.ConstantIntCondition;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.procedurallib.supplier.DoubleRange;
import com.hypixel.hytale.procedurallib.supplier.IDoubleCoordinateHashSupplier;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.CavePrefabPlacement;
import com.hypixel.hytale.server.worldgen.cave.prefab.CavePrefabContainer;
import javax.annotation.Nonnull;

public class CavePrefabContent extends PrefabContent {
   public static final String ID = "CavePrefab";
   public static final BuilderCodec<CavePrefabContent> CODEC = BuilderCodec.builder(CavePrefabContent.class, CavePrefabContent::new, PrefabContent.CODEC)
      .documentation("Define a set of one or more prefabs that should be generated in the target cave(s)")
      .<CavePrefabPlacement>append(new KeyedCodec<>("Placement", new EnumCodec<>(CavePrefabPlacement.class)), (t, v) -> t.placement = v, t -> t.placement)
      .documentation("The cave prefab placement mode")
      .add()
      .<IntRange>append(new KeyedCodec<>("Iterations", Codecs.INT_RANGE), (t, v) -> t.iterations = v, t -> t.iterations)
      .documentation("The iteration range for cave prefab placement")
      .add()
      .build();
   protected CavePrefabPlacement placement = CavePrefabPlacement.FLOOR;
   protected IntRange iterations = new IntRange(1, 1);

   @Override
   public Content.Type type() {
      return Content.Type.CAVE_PREFAB;
   }

   @Override
   public <T> void applyTo(ModifyEvent<T> event) throws Exception {
      if (this.prefabs.length != 0) {
         if (event instanceof ModifyEvents.CavePrefabs container) {
            container.entries().add(new CavePrefabContainer.CavePrefabEntry(this.buildPrefabs(event.seed()), this.buildConfig(event.seed())));
         }
      }
   }

   protected CavePrefabContainer.CavePrefabEntry.CavePrefabConfig buildConfig(@Nonnull SeedString<SeedStringResource> seed) {
      return new CavePrefabContainer.CavePrefabEntry.CavePrefabConfig(
         this.rotations,
         this.placement,
         ConstantIntCondition.DEFAULT_TRUE,
         this.blockMask.getBlockMask(),
         this.buildIterations(),
         this.buildDisplacementModifier(),
         this.noiseMask.build(seed),
         this.heightMask.getCondition()
      );
   }

   protected IDoubleCoordinateHashSupplier buildDisplacementModifier() {
      return (seed, x, y, hash) -> HashUtil.random(seed, x, y, hash) * this.offsetY;
   }

   protected IDoubleRange buildIterations() {
      return (IDoubleRange)(this.iterations.getInclusiveMax() == this.iterations.getInclusiveMin()
         ? new DoubleRange.Constant(this.iterations.getInclusiveMin())
         : new DoubleRange.Normal(this.iterations.getInclusiveMin(), this.iterations.getInclusiveMax()));
   }
}
