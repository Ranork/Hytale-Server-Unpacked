package com.hypixel.hytale.server.core.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import javax.annotation.Nonnull;

public class ItemGridSlot {
   public static final BuilderCodec<ItemGridSlot> CODEC = BuilderCodec.builder(ItemGridSlot.class, ItemGridSlot::new)
      .addField(new KeyedCodec<>("ItemStack", ItemStack.CODEC), (p, t) -> p.itemStack = t, p -> p.itemStack)
      .addField(new KeyedCodec<>("Background", ValueCodec.PATCH_STYLE), (p, t) -> p.background = t, p -> p.background)
      .addField(new KeyedCodec<>("Overlay", ValueCodec.PATCH_STYLE), (p, t) -> p.overlay = t, p -> p.overlay)
      .addField(new KeyedCodec<>("Icon", ValueCodec.PATCH_STYLE), (p, t) -> p.icon = t, p -> p.icon)
      .addField(new KeyedCodec<>("IsItemIncompatible", Codec.BOOLEAN), (p, t) -> p.isItemIncompatible = t, p -> p.isItemIncompatible)
      .addField(new KeyedCodec<>("Name", Codec.STRING), (p, t) -> p.name = t, p -> p.name)
      .addField(new KeyedCodec<>("Description", Codec.STRING), (p, t) -> p.description = t, p -> p.description)
      .addField(new KeyedCodec<>("SkipItemQualityBackground", Codec.BOOLEAN), (p, t) -> p.skipItemQualityBackground = t, p -> p.skipItemQualityBackground)
      .addField(new KeyedCodec<>("IsActivatable", Codec.BOOLEAN), (p, t) -> p.isActivatable = t, p -> p.isActivatable)
      .addField(new KeyedCodec<>("IsItemUncraftable", Codec.BOOLEAN), (p, t) -> p.isItemUncraftable = t, p -> p.isItemUncraftable)
      .build();
   private ItemStack itemStack;
   private Value<PatchStyle> background;
   private Value<PatchStyle> overlay;
   private Value<PatchStyle> icon;
   private boolean isItemIncompatible;
   private String name;
   private String description;
   private boolean skipItemQualityBackground;
   private boolean isActivatable;
   private boolean isItemUncraftable;

   public ItemGridSlot() {
   }

   public ItemGridSlot(ItemStack itemStack) {
      this.itemStack = itemStack;
   }

   @Nonnull
   public ItemGridSlot setItemStack(ItemStack itemStack) {
      this.itemStack = itemStack;
      return this;
   }

   @Nonnull
   public ItemGridSlot setBackground(Value<PatchStyle> background) {
      this.background = background;
      return this;
   }

   @Nonnull
   public ItemGridSlot setOverlay(Value<PatchStyle> overlay) {
      this.overlay = overlay;
      return this;
   }

   @Nonnull
   public ItemGridSlot setIcon(Value<PatchStyle> icon) {
      this.icon = icon;
      return this;
   }

   @Nonnull
   public ItemGridSlot setItemIncompatible(boolean itemIncompatible) {
      this.isItemIncompatible = itemIncompatible;
      return this;
   }

   @Nonnull
   public ItemGridSlot setName(String name) {
      this.name = name;
      return this;
   }

   @Nonnull
   public ItemGridSlot setDescription(String description) {
      this.description = description;
      return this;
   }

   public boolean isItemUncraftable() {
      return this.isItemUncraftable;
   }

   public void setItemUncraftable(boolean itemUncraftable) {
      this.isItemUncraftable = itemUncraftable;
   }

   public boolean isActivatable() {
      return this.isActivatable;
   }

   public void setActivatable(boolean activatable) {
      this.isActivatable = activatable;
   }

   public boolean isSkipItemQualityBackground() {
      return this.skipItemQualityBackground;
   }

   public void setSkipItemQualityBackground(boolean skipItemQualityBackground) {
      this.skipItemQualityBackground = skipItemQualityBackground;
   }
}
