package com.hypixel.hytale.builtin.worldgen.modifier.content.layer;

import com.hypixel.hytale.builtin.worldgen.modifier.content.Content;
import com.hypixel.hytale.builtin.worldgen.modifier.content.common.HeightMask;
import com.hypixel.hytale.builtin.worldgen.modifier.content.common.NoiseMask;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvent;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvents;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.procedurallib.supplier.DoubleRange;
import com.hypixel.hytale.procedurallib.supplier.DoubleRangeNoiseSupplier;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.container.LayerContainer;
import javax.annotation.Nonnull;

public class BiomeDynamicLayerContent extends LayerContent {
   public static final String ID = "BiomeDynamicLayer";
   public static final BuilderCodec<BiomeDynamicLayerContent> CODEC = BuilderCodec.builder(
         BiomeDynamicLayerContent.class, BiomeDynamicLayerContent::new, LayerContent.CODEC
      )
      .documentation("Define a layer that is placed relative to terrain surfaces and any other dynamic layers that apply before it")
      .<BiomeDynamicLayerContent.DynamicEntry[]>append(
         new KeyedCodec<>("Entries", BiomeDynamicLayerContent.DynamicEntry.ARRAY_CODEC), (t, v) -> t.entries = v, t -> t.entries
      )
      .documentation("The entries contained within the layer")
      .add()
      .<HeightMask>append(new KeyedCodec<>("Offset", HeightMask.CODEC), (t, v) -> t.offset = v, t -> t.offset)
      .documentation("The depth offset for the dynamic layer")
      .add()
      .<NoiseMask>append(new KeyedCodec<>("OffsetNoise", NoiseMask.CODEC), (t, v) -> t.offsetNoise = v, t -> t.offsetNoise)
      .documentation("The noise used to modulate the depth offset")
      .add()
      .build();
   protected BiomeDynamicLayerContent.DynamicEntry[] entries = BiomeDynamicLayerContent.DynamicEntry.EMPTY_ARRAY;
   protected HeightMask offset = HeightMask.DEFAULT_ONE;
   protected NoiseMask offsetNoise = NoiseMask.ZERO;

   @Override
   public Content.Type type() {
      return Content.Type.BIOME_DYNAMIC_LAYER;
   }

   @Override
   public <T> void applyTo(ModifyEvent<T> event) throws Exception {
      if (this.entries.length != 0) {
         if (event instanceof ModifyEvents.BiomeDynamicLayers container) {
            container.entries()
               .add(
                  new LayerContainer.DynamicLayer(
                     this.buildEntries(event.seed()),
                     this.noiseMask.build(event.seed()),
                     0,
                     new DoubleRangeNoiseSupplier(
                        new DoubleRange.Normal(this.offset.range().getInclusiveMin(), this.offset.range().getInclusiveMax()),
                        this.offsetNoise.buildNoise(event.seed())
                     )
                  )
               );
         }
      }
   }

   protected LayerContainer.DynamicLayerEntry[] buildEntries(@Nonnull SeedString<SeedStringResource> seed) {
      LayerContainer.DynamicLayerEntry[] layerEntries = new LayerContainer.DynamicLayerEntry[this.entries.length];

      for (int i = 0; i < this.entries.length; i++) {
         layerEntries[i] = this.entries[i].build(seed);
      }

      return layerEntries;
   }

   public static class DynamicEntry extends LayerContent.Entry {
      public static final BuilderCodec<BiomeDynamicLayerContent.DynamicEntry> CODEC = BuilderCodec.builder(
            BiomeDynamicLayerContent.DynamicEntry.class, BiomeDynamicLayerContent.DynamicEntry::new, LayerContent.Entry.CODEC
         )
         .documentation("Define an entry that generates within the dynamic layer")
         .build();
      public static final ArrayCodec<BiomeDynamicLayerContent.DynamicEntry> ARRAY_CODEC = new ArrayCodec<>(CODEC, BiomeDynamicLayerContent.DynamicEntry[]::new);
      protected static final BiomeDynamicLayerContent.DynamicEntry[] EMPTY_ARRAY = new BiomeDynamicLayerContent.DynamicEntry[0];

      public LayerContainer.DynamicLayerEntry build(SeedString<SeedStringResource> seed) {
         return new LayerContainer.DynamicLayerEntry(NoiseBlockEntry.buildArray(seed, this.blocks), this.entryNoiseMask.build(seed));
      }
   }
}
