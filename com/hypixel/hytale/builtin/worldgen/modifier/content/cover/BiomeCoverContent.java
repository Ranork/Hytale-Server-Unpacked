package com.hypixel.hytale.builtin.worldgen.modifier.content.cover;

import com.hypixel.hytale.builtin.worldgen.modifier.content.Content;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvent;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvents;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.container.CoverContainer;
import com.hypixel.hytale.server.worldgen.util.BlockFluidEntry;
import javax.annotation.Nonnull;

public class BiomeCoverContent extends CoverContent {
   public static final String ID = "BiomeCover";
   public static final BuilderCodec<BiomeCoverContent> CODEC = BuilderCodec.builder(BiomeCoverContent.class, BiomeCoverContent::new, CoverContent.CODEC)
      .documentation("Define a block that will be placed on the surface of the terrain")
      .<Boolean>append(new KeyedCodec<>("PlaceOnFluids", Codec.BOOLEAN), (c, v) -> c.placeOnFluids = v, c -> c.placeOnFluids)
      .documentation("Whether the cover should be placed on fluids")
      .add()
      .build();
   protected boolean placeOnFluids = false;

   @Override
   public Content.Type type() {
      return Content.Type.BIOME_COVER;
   }

   @Override
   public <T> void applyTo(ModifyEvent<T> event) throws Exception {
      if (this.entries.length != 0) {
         if (event instanceof ModifyEvents.BiomeCovers container) {
            container.entries().add(this.build(event.seed()));
         }
      }
   }

   protected CoverContainer.CoverContainerEntry build(@Nonnull SeedString<SeedStringResource> seed) {
      return new CoverContainer.CoverContainerEntry(
         this.buildEntries(CoverContainer.CoverContainerEntry.CoverContainerEntryPart.EMPTY_ARRAY, BiomeCoverContent::createEntry),
         this.noiseMask.build(seed),
         this.heightMask.getCondition(),
         this.parentMask.getCondition(),
         this.chance,
         this.placeOnFluids
      );
   }

   protected static CoverContainer.CoverContainerEntry.CoverContainerEntryPart createEntry(@Nonnull CoverContent.Entry entry, @Nonnull BlockFluidEntry block) {
      return new CoverContainer.CoverContainerEntry.CoverContainerEntryPart(block, entry.offset);
   }
}
