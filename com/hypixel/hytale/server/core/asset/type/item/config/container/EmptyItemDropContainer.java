package com.hypixel.hytale.server.core.asset.type.item.config.container;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDrop;
import java.util.List;
import java.util.Set;
import java.util.function.DoubleSupplier;
import javax.annotation.Nonnull;

public class EmptyItemDropContainer extends ItemDropContainer {
   public static final BuilderCodec<EmptyItemDropContainer> CODEC = BuilderCodec.builder(
         EmptyItemDropContainer.class, EmptyItemDropContainer::new, ItemDropContainer.DEFAULT_CODEC
      )
      .build();

   @Override
   protected void populateDrops(List<ItemDrop> drops, @Nonnull DoubleSupplier chanceProvider, Set<String> droplistReferences) {
   }

   @Override
   public List<ItemDrop> getAllDrops(List<ItemDrop> list) {
      return list;
   }

   @Override
   public String toString() {
      return "EmptyItemDropContainer{}";
   }
}
