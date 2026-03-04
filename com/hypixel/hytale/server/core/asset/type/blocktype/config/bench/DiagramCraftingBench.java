package com.hypixel.hytale.server.core.asset.type.blocktype.config.bench;

import com.hypixel.hytale.codec.builder.BuilderCodec;

public class DiagramCraftingBench extends CraftingBench {
   public static final BuilderCodec<DiagramCraftingBench> CODEC = BuilderCodec.builder(
         DiagramCraftingBench.class, DiagramCraftingBench::new, CraftingBench.CODEC
      )
      .build();
}
