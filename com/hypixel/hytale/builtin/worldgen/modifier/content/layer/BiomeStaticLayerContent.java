package com.hypixel.hytale.builtin.worldgen.modifier.content.layer;

import com.hypixel.hytale.builtin.worldgen.modifier.content.Codecs;
import com.hypixel.hytale.builtin.worldgen.modifier.content.Content;
import com.hypixel.hytale.builtin.worldgen.modifier.content.common.NoiseMask;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvent;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvents;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.math.range.IntRange;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.procedurallib.supplier.DoubleRange;
import com.hypixel.hytale.procedurallib.supplier.DoubleRangeNoiseSupplier;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.container.LayerContainer;
import javax.annotation.Nonnull;

public class BiomeStaticLayerContent extends LayerContent {
   public static final String ID = "BiomeStaticLayer";
   public static final BuilderCodec<BiomeStaticLayerContent> CODEC = BuilderCodec.builder(
         BiomeStaticLayerContent.class, BiomeStaticLayerContent::new, LayerContent.CODEC
      )
      .documentation("Define a layer of material that generates between a static height range")
      .<BiomeStaticLayerContent.StaticEntry[]>append(
         new KeyedCodec<>("Entries", BiomeStaticLayerContent.StaticEntry.ARRAY_CODEC), (t, v) -> t.entries = v, t -> t.entries
      )
      .documentation("The entries contained within the layer")
      .add()
      .build();
   protected BiomeStaticLayerContent.StaticEntry[] entries = BiomeStaticLayerContent.StaticEntry.EMPTY_ARRAY;

   @Override
   public Content.Type type() {
      return Content.Type.BIOME_STATIC_LAYER;
   }

   @Override
   public <T> void applyTo(ModifyEvent<T> event) throws Exception {
      if (this.entries.length != 0) {
         if (event instanceof ModifyEvents.BiomeStaticLayers container) {
            container.entries().add(new LayerContainer.StaticLayer(this.buildEntries(event.seed()), this.noiseMask.build(event.seed()), 0));
         }
      }
   }

   protected LayerContainer.StaticLayerEntry[] buildEntries(@Nonnull SeedString<SeedStringResource> seed) {
      LayerContainer.StaticLayerEntry[] layerEntries = new LayerContainer.StaticLayerEntry[this.entries.length];

      for (int i = 0; i < this.entries.length; i++) {
         layerEntries[i] = this.entries[i].build(seed);
      }

      return layerEntries;
   }

   public static class StaticEntry extends LayerContent.Entry {
      public static final BuilderCodec<BiomeStaticLayerContent.StaticEntry> CODEC = BuilderCodec.builder(
            BiomeStaticLayerContent.StaticEntry.class, BiomeStaticLayerContent.StaticEntry::new, LayerContent.Entry.CODEC
         )
         .documentation("Define an entry that generates within the static layer")
         .<IntRange>append(new KeyedCodec<>("MinY", Codecs.INT_RANGE), (t, v) -> t.minY = v, t -> t.minY)
         .documentation("The range for the minimum y-height of the layer")
         .add()
         .<NoiseMask>append(new KeyedCodec<>("MinYNoise", NoiseMask.CODEC), (t, v) -> t.minYNoise = v, t -> t.minYNoise)
         .documentation("The noise for modulating the minimum height of the layer")
         .add()
         .<IntRange>append(new KeyedCodec<>("MaxY", Codecs.INT_RANGE), (t, v) -> t.maxY = v, t -> t.maxY)
         .documentation("The range for the maximum y-height of the layer")
         .add()
         .<NoiseMask>append(new KeyedCodec<>("MaxYNoise", NoiseMask.CODEC), (t, v) -> t.maxYNoise = v, t -> t.maxYNoise)
         .documentation("The noise for modulating the maximum height of the layer")
         .add()
         .build();
      public static final ArrayCodec<BiomeStaticLayerContent.StaticEntry> ARRAY_CODEC = new ArrayCodec<>(CODEC, BiomeStaticLayerContent.StaticEntry[]::new);
      protected static final BiomeStaticLayerContent.StaticEntry[] EMPTY_ARRAY = new BiomeStaticLayerContent.StaticEntry[0];
      protected static final IntRange DEFAULT_MIN = new IntRange(0, 0);
      protected static final IntRange DEFAULT_MAX = new IntRange(319, 319);
      protected IntRange minY = DEFAULT_MIN;
      protected NoiseMask minYNoise = NoiseMask.ZERO;
      protected IntRange maxY = DEFAULT_MAX;
      protected NoiseMask maxYNoise = NoiseMask.ZERO;

      public LayerContainer.StaticLayerEntry build(SeedString<SeedStringResource> seed) {
         return new LayerContainer.StaticLayerEntry(
            NoiseBlockEntry.buildArray(seed, this.blocks),
            this.entryNoiseMask.build(seed),
            new DoubleRangeNoiseSupplier(new DoubleRange.Normal(this.minY.getInclusiveMin(), this.minY.getInclusiveMax()), this.minYNoise.buildNoise(seed)),
            new DoubleRangeNoiseSupplier(new DoubleRange.Normal(this.maxY.getInclusiveMin(), this.maxY.getInclusiveMax()), this.maxYNoise.buildNoise(seed))
         );
      }
   }
}
