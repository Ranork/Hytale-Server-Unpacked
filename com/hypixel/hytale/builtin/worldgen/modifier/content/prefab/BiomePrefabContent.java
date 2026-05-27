package com.hypixel.hytale.builtin.worldgen.modifier.content.prefab;

import com.hypixel.hytale.builtin.worldgen.modifier.content.common.BlockMask;
import com.hypixel.hytale.builtin.worldgen.modifier.content.common.HeightMask;
import com.hypixel.hytale.builtin.worldgen.modifier.content.common.PointGrid;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvent;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvents;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.container.PrefabContainer;
import com.hypixel.hytale.server.worldgen.prefab.PrefabBaseCheck;
import com.hypixel.hytale.server.worldgen.prefab.PrefabCategory;
import com.hypixel.hytale.server.worldgen.prefab.PrefabPatternGenerator;
import javax.annotation.Nonnull;

public class BiomePrefabContent extends PrefabContent {
   public static final String ID = "BiomePrefab";
   public static final BuilderCodec<BiomePrefabContent> CODEC = BuilderCodec.builder(BiomePrefabContent.class, BiomePrefabContent::new, PrefabContent.CODEC)
      .documentation("Define a set of one or more prefabs that should be generated in the target biome(s)")
      .<PointGrid>append(new KeyedCodec<>("PointGrid", PointGrid.CODEC), (t, v) -> t.grid = v, t -> t.grid)
      .documentation("The point grid for the prefab")
      .add()
      .<Boolean>append(new KeyedCodec<>("FitHeightmap", Codec.BOOLEAN), (t, v) -> t.fitHeightmap = v, t -> t.fitHeightmap)
      .documentation("Whether the prefab should fit to the heightmap")
      .add()
      .<Boolean>append(new KeyedCodec<>("Submerge", Codec.BOOLEAN), (t, v) -> t.submerge = v, t -> t.submerge)
      .documentation("Whether the prefab should be submerged if placed underwater")
      .add()
      .<PrefabBaseCheck[]>append(new KeyedCodec<>("BaseChecks", PrefabBaseCheck.ARRAY_CODEC), (p, v) -> p.baseChecks = v, p -> p.baseChecks)
      .documentation("List of prefab base-check rules")
      .add()
      .<BlockMask>append(new KeyedCodec<>("ParentMask", BlockMask.CODEC), (p, v) -> p.parentMask = v, p -> p.parentMask)
      .documentation("Define which blocks the prefab can be placed on")
      .add()
      .afterDecode(PrefabContent::rebuild)
      .build();
   protected static final int DEFAULT_MAX_SIZE = 24;
   protected static final int DEFAULT_EXCLUSION_RADIUS = 0;
   protected static final boolean DEFAULT_DEEP_SEARCH = false;
   protected static final boolean DEFAULT_ON_FLUID = false;
   protected boolean fitHeightmap = false;
   protected boolean submerge = false;
   protected PointGrid grid = PointGrid.DEFAULT;
   protected BlockMask parentMask = BlockMask.REPLACE_SOLID;
   protected PrefabBaseCheck[] baseChecks = PrefabBaseCheck.EMPTY_ARRAY;

   @Override
   public <T> void applyTo(ModifyEvent<T> event) throws Exception {
      if (this.prefabs.length != 0) {
         if (event instanceof ModifyEvents.BiomePrefabs container) {
            container.entries()
               .add(
                  new PrefabContainer.PrefabContainerEntry(
                     this.buildPrefabs(event.seed()), this.buildPattern(event.seed()), Environment.getAssetMap().getIndex(this.environment.getId())
                  )
               );
         }
      }
   }

   protected PrefabPatternGenerator buildPattern(@Nonnull SeedString<SeedStringResource> seed) {
      return new PrefabPatternGenerator(
         seed.hashCode(),
         PrefabCategory.NONE,
         this.grid.build(seed),
         this.heightMask.getCondition(),
         HeightMask.DEFAULT_HEIGHT_THRESHOLD,
         this.blockMask.getBlockMask(),
         this.noiseMask.build(seed),
         this.parentMask.getCondition(),
         this.rotations,
         this.offset,
         this.fitHeightmap,
         false,
         false,
         this.submerge,
         24,
         0,
         this.baseChecks
      );
   }
}
