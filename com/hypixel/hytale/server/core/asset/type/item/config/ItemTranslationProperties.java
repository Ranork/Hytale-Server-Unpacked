package com.hypixel.hytale.server.core.asset.type.item.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemTranslationProperties implements NetworkSerializable<com.hypixel.hytale.protocol.ItemTranslationProperties> {
   public static final BuilderCodec<ItemTranslationProperties> CODEC = BuilderCodec.builder(ItemTranslationProperties.class, ItemTranslationProperties::new)
      .appendInherited(new KeyedCodec<>("Name", Codec.STRING), (data, s) -> data.name = s, data -> data.name, (o, p) -> o.name = p.name)
      .documentation("The translation key for the name of this item.")
      .metadata(new UIEditor(new UIEditor.LocalizationKeyField("server.items.{assetId}.name", true)))
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("Description", Codec.STRING), (data, s) -> data.description = s, data -> data.description, (o, p) -> o.description = p.description
      )
      .documentation("The translation key for the description of this item.")
      .metadata(new UIEditor(new UIEditor.LocalizationKeyField("server.items.{assetId}.description")))
      .add()
      .build();
   @Nullable
   private String name;
   @Nullable
   private String description;

   ItemTranslationProperties() {
   }

   public ItemTranslationProperties(@Nonnull String name, @Nonnull String description) {
      this.name = name;
      this.description = description;
   }

   @Nullable
   public String getName() {
      return this.name;
   }

   @Nullable
   public String getDescription() {
      return this.description;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ItemTranslationProperties toPacket() {
      com.hypixel.hytale.protocol.ItemTranslationProperties packet = new com.hypixel.hytale.protocol.ItemTranslationProperties();
      packet.name = this.name;
      packet.description = this.description;
      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ItemTranslationProperties{name=" + this.name + ", description=" + this.description + "}";
   }
}
