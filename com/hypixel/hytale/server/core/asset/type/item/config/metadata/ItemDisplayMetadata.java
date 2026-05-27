package com.hypixel.hytale.server.core.asset.type.item.config.metadata;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.Message;
import javax.annotation.Nullable;

public class ItemDisplayMetadata {
   public static final String KEY = "ItemDisplay";
   public static final BuilderCodec<ItemDisplayMetadata> CODEC = BuilderCodec.builder(ItemDisplayMetadata.class, ItemDisplayMetadata::new)
      .appendInherited(new KeyedCodec<>("Name", Message.CODEC), (meta, m) -> meta.name = m, meta -> meta.name, (meta, parent) -> meta.name = parent.name)
      .add()
      .appendInherited(
         new KeyedCodec<>("Description", Message.CODEC),
         (meta, m) -> meta.description = m,
         meta -> meta.description,
         (meta, parent) -> meta.description = parent.description
      )
      .add()
      .build();
   public static final KeyedCodec<ItemDisplayMetadata> KEYED_CODEC = new KeyedCodec<>("ItemDisplay", CODEC);
   @Nullable
   private Message name;
   @Nullable
   private Message description;

   public ItemDisplayMetadata() {
   }

   public ItemDisplayMetadata(@Nullable Message name, @Nullable Message description) {
      this.name = name;
      this.description = description;
   }

   @Nullable
   public Message getName() {
      return this.name;
   }

   @Nullable
   public Message getDescription() {
      return this.description;
   }

   public void setName(@Nullable Message name) {
      this.name = name;
   }

   public void setDescription(@Nullable Message description) {
      this.description = description;
   }
}
