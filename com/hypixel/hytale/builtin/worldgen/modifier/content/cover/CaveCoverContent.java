package com.hypixel.hytale.builtin.worldgen.modifier.content.cover;

import com.hypixel.hytale.builtin.worldgen.modifier.content.Content;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvent;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvents;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.CaveNodeType;
import com.hypixel.hytale.server.worldgen.util.BlockFluidEntry;
import com.hypixel.hytale.server.worldgen.util.condition.RandomCoordinateCondition;
import javax.annotation.Nonnull;

public class CaveCoverContent extends CoverContent {
   public static final String ID = "CaveCover";
   public static final BuilderCodec<CaveCoverContent> CODEC = BuilderCodec.builder(CaveCoverContent.class, CaveCoverContent::new, CoverContent.CODEC)
      .documentation("Define a block that will be placed on the floor or ceiling of a cave")
      .<CaveNodeType.CaveNodeCoverType>append(
         new KeyedCodec<>("Placement", new EnumCodec<>(CaveNodeType.CaveNodeCoverType.class)), (c, v) -> c.placement = v, c -> c.placement
      )
      .documentation("The side of the cave where the cover should be placed")
      .add()
      .build();
   protected CaveNodeType.CaveNodeCoverType placement = CaveNodeType.CaveNodeCoverType.FLOOR;

   @Override
   public Content.Type type() {
      return Content.Type.CAVE_COVER;
   }

   @Override
   public <T> void applyTo(ModifyEvent<T> event) throws Exception {
      if (this.entries.length != 0) {
         if (event instanceof ModifyEvents.CaveCovers container) {
            container.entries().add(this.buildEntry(event.seed()));
         }
      }
   }

   protected CaveNodeType.CaveNodeCoverEntry buildEntry(@Nonnull SeedString<SeedStringResource> seed) {
      return new CaveNodeType.CaveNodeCoverEntry(
         this.buildEntries(CaveNodeType.CaveNodeCoverEntry.Entry.EMPTY_ARRAY, CaveCoverContent::createEntry),
         this.heightMask.getCondition(),
         this.noiseMask.build(seed),
         new RandomCoordinateCondition(this.chance),
         this.parentMask.getCondition(),
         this.placement
      );
   }

   protected static CaveNodeType.CaveNodeCoverEntry.Entry createEntry(@Nonnull CoverContent.Entry entry, @Nonnull BlockFluidEntry block) {
      return new CaveNodeType.CaveNodeCoverEntry.Entry(block, entry.offset);
   }
}
