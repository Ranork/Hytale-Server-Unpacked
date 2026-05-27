package com.hypixel.hytale.builtin.worldgen.modifier.content.layer;

import com.hypixel.hytale.builtin.worldgen.modifier.content.Content;
import com.hypixel.hytale.builtin.worldgen.modifier.content.common.NoiseMask;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public abstract class LayerContent implements Content {
   public static final BuilderCodec<LayerContent> CODEC = BuilderCodec.abstractBuilder(LayerContent.class)
      .append(new KeyedCodec<>("NoiseMask", NoiseMask.CODEC), (t, v) -> t.noiseMask = v, t -> t.noiseMask)
      .documentation("The noise mask used to determine where the layer should be placed")
      .add()
      .build();
   protected NoiseMask noiseMask = NoiseMask.DEFAULT;

   public static class Entry {
      public static final BuilderCodec<LayerContent.Entry> CODEC = BuilderCodec.builder(LayerContent.Entry.class, LayerContent.Entry::new)
         .documentation("An entry within a layer containing blocks and a noise mask")
         .<NoiseBlockEntry[]>append(new KeyedCodec<>("Blocks", NoiseBlockEntry.ARRAY_CODEC), (t, v) -> t.blocks = v, t -> t.blocks)
         .documentation("The collection of blocks to use for this entry")
         .add()
         .<NoiseMask>append(new KeyedCodec<>("NoiseMask", NoiseMask.CODEC), (t, v) -> t.entryNoiseMask = v, t -> t.entryNoiseMask)
         .documentation("The noise mask used to determine where the blocks should be placed")
         .add()
         .build();
      protected NoiseBlockEntry[] blocks = NoiseBlockEntry.EMPTY_ARRAY;
      protected NoiseMask entryNoiseMask = NoiseMask.DEFAULT;
   }
}
