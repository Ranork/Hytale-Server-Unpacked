package com.hypixel.hytale.server.core.asset.type.item.config.container;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDrop;
import java.util.List;
import java.util.Set;
import java.util.function.DoubleSupplier;
import javax.annotation.Nonnull;

public class SingleItemDropContainer extends ItemDropContainer {
   public static final BuilderCodec<SingleItemDropContainer> CODEC = BuilderCodec.builder(
         SingleItemDropContainer.class, SingleItemDropContainer::new, ItemDropContainer.DEFAULT_CODEC
      )
      .append(
         new KeyedCodec<>("Item", ItemDrop.CODEC),
         (singleItemDropContainer, itemDrop) -> singleItemDropContainer.drop = itemDrop,
         singleItemDropContainer -> singleItemDropContainer.drop
      )
      .addValidator(Validators.nonNull())
      .add()
      .build();
   @Nonnull
   protected ItemDrop drop;

   public SingleItemDropContainer(@Nonnull ItemDrop drop, double chance) {
      super(chance);
      this.drop = drop;
   }

   protected SingleItemDropContainer() {
   }

   @Nonnull
   public ItemDrop getDrop() {
      return this.drop;
   }

   @Override
   protected void populateDrops(@Nonnull List<ItemDrop> drops, DoubleSupplier chanceProvider, Set<String> droplistReferences) {
      drops.add(this.drop);
   }

   @Nonnull
   @Override
   public List<ItemDrop> getAllDrops(@Nonnull List<ItemDrop> list) {
      list.add(this.drop);
      return list;
   }

   @Nonnull
   @Override
   public String toString() {
      return "SingleItemDropContainer{drop=" + this.drop + ", weight=" + this.weight + "}";
   }
}
