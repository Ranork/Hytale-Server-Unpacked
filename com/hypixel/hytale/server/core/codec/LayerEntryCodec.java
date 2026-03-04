package com.hypixel.hytale.server.core.codec;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;

public class LayerEntryCodec {
   public static final BuilderCodec<LayerEntryCodec> CODEC = BuilderCodec.builder(LayerEntryCodec.class, LayerEntryCodec::new)
      .append(new KeyedCodec<>("Left", Codec.INTEGER), (entry, depth) -> entry.depth = depth, entry -> entry.depth)
      .addValidator(Validators.nonNull())
      .add()
      .<String>append(new KeyedCodec<>("Right", Codec.STRING), (entry, material) -> entry.material = material, entry -> entry.material)
      .addValidator(Validators.nonNull())
      .add()
      .append(
         new KeyedCodec<>("UseToolArg", Codec.BOOLEAN), (entry, useToolArg) -> entry.useToolArg = useToolArg != null && useToolArg, entry -> entry.useToolArg
      )
      .add()
      .append(new KeyedCodec<>("Skip", Codec.BOOLEAN), (entry, skip) -> entry.skip = skip != null && skip, entry -> entry.skip)
      .add()
      .build();
   private Integer depth;
   private String material;
   private boolean useToolArg = false;
   private boolean skip = false;

   public LayerEntryCodec() {
   }

   public LayerEntryCodec(Integer depth, String material, boolean useToolArg) {
      this.depth = depth;
      this.material = material;
      this.useToolArg = useToolArg;
   }

   @Nonnull
   public Integer getDepth() {
      return this.depth;
   }

   @Nonnull
   public String getMaterial() {
      return this.material;
   }

   public boolean isUseToolArg() {
      return this.useToolArg;
   }

   public boolean isSkip() {
      return this.skip;
   }
}
