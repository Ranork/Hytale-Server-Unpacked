package com.hypixel.hytale.builtin.worldgen.modifier.content.tint;

import com.hypixel.hytale.builtin.worldgen.modifier.content.Content;
import com.hypixel.hytale.builtin.worldgen.modifier.content.common.NoiseMask;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvent;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvents;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.common.map.WeightedMap;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.asset.util.ColorParseUtil;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.worldgen.container.TintContainer;

public class BiomeTintContent implements Content {
   public static final String ID = "BiomeTint";
   public static final BuilderCodec<BiomeTintContent> CODEC = BuilderCodec.builder(BiomeTintContent.class, BiomeTintContent::new)
      .documentation("Define a set of tints that should be applied to parts of the biome")
      .<BiomeTintContent.Entry[]>append(
         new KeyedCodec<>("Entries", new ArrayCodec<>(BiomeTintContent.Entry.CODEC, BiomeTintContent.Entry[]::new)), (t, v) -> t.entries = v, t -> t.entries
      )
      .documentation("The list of weighted colors that will be selected from in order to tint parts of the world")
      .add()
      .<NoiseMask>append(new KeyedCodec<>("Noise", NoiseMask.CODEC), (t, v) -> t.noise = v, t -> t.noise)
      .documentation("The noise used to select a random tint from the entries list")
      .add()
      .<NoiseMask>append(new KeyedCodec<>("NoiseMask", NoiseMask.CODEC), (t, v) -> t.noiseMask = v, t -> t.noiseMask)
      .documentation("The noise mask determining where the tint should be applied in the world")
      .add()
      .build();
   protected BiomeTintContent.Entry[] entries = BiomeTintContent.Entry.EMPTY_ARRAY;
   protected NoiseMask noise = NoiseMask.DEFAULT;
   protected NoiseMask noiseMask = NoiseMask.DEFAULT;

   @Override
   public Content.Type type() {
      return Content.Type.BIOME_TINT;
   }

   @Override
   public <T> void applyTo(ModifyEvent<T> event) throws Exception {
      if (this.entries.length != 0) {
         if (event instanceof ModifyEvents.BiomeTints container) {
            container.entries()
               .add(new TintContainer.TintContainerEntry(this.buildColors(), this.noise.buildNoise(event.seed()), this.noiseMask.build(event.seed())));
         }
      }
   }

   protected IWeightedMap<Integer> buildColors() {
      WeightedMap.Builder<Integer> map = WeightedMap.builder(ArrayUtil.EMPTY_INTEGER_ARRAY);

      for (BiomeTintContent.Entry entry : this.entries) {
         map.put(entry.color(), entry.weight);
      }

      return map.build();
   }

   public static class Entry {
      public static final BuilderCodec<BiomeTintContent.Entry> CODEC = BuilderCodec.builder(BiomeTintContent.Entry.class, BiomeTintContent.Entry::new)
         .documentation("Define a weighted color entry for biome tinting")
         .<Color>append(new KeyedCodec<>("Color", ProtocolCodecs.COLOR), (e, v) -> e.color = v, e -> e.color)
         .documentation("The color value")
         .add()
         .<Double>append(new KeyedCodec<>("Weight", Codec.DOUBLE), (e, v) -> e.weight = v, e -> e.weight)
         .addValidator(Validators.range(0.0, 100.0))
         .documentation("The relative weight of this color entry")
         .add()
         .build();
      public static final BiomeTintContent.Entry[] EMPTY_ARRAY = new BiomeTintContent.Entry[0];
      protected static final Color DEFAULT_COLOR = new Color();
      protected Color color = DEFAULT_COLOR;
      protected double weight = 1.0;

      public Integer color() {
         return ColorParseUtil.colorToARGBInt(this.color);
      }
   }
}
