package com.hypixel.hytale.server.core.asset.type.item.config.container;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.common.map.IWeightedElement;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDrop;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.DoubleSupplier;

public abstract class ItemDropContainer implements IWeightedElement {
   public static final BuilderCodec<ItemDropContainer> DEFAULT_CODEC = BuilderCodec.abstractBuilder(ItemDropContainer.class)
      .addField(
         new KeyedCodec<>("Weight", Codec.DOUBLE),
         (itemDropContainer, aDouble) -> itemDropContainer.weight = aDouble,
         itemDropContainer -> itemDropContainer.weight
      )
      .build();
   public static final CodecMapCodec<ItemDropContainer> CODEC = new CodecMapCodec<>("Type");
   public static final ItemDropContainer[] EMPTY_ARRAY = new ItemDropContainer[0];
   protected double weight = 100.0;

   public ItemDropContainer(double weight) {
      this.weight = weight;
   }

   protected ItemDropContainer() {
   }

   @Override
   public double getWeight() {
      return this.weight;
   }

   public void populateDrops(List<ItemDrop> drops, DoubleSupplier chanceProvider, String droplistId) {
      Set<String> droplistReferences = new HashSet<>();
      droplistReferences.add(droplistId);
      this.populateDrops(drops, chanceProvider, droplistReferences);
   }

   protected abstract void populateDrops(List<ItemDrop> var1, DoubleSupplier var2, Set<String> var3);

   public abstract List<ItemDrop> getAllDrops(List<ItemDrop> var1);

   static {
      CODEC.register("Multiple", MultipleItemDropContainer.class, MultipleItemDropContainer.CODEC);
      CODEC.register("Choice", ChoiceItemDropContainer.class, ChoiceItemDropContainer.CODEC);
      CODEC.register("Single", SingleItemDropContainer.class, SingleItemDropContainer.CODEC);
      CODEC.register("Droplist", DroplistItemDropContainer.class, DroplistItemDropContainer.CODEC);
      CODEC.register("Empty", EmptyItemDropContainer.class, EmptyItemDropContainer.CODEC);
   }
}
