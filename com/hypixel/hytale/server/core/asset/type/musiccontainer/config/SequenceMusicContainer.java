package com.hypixel.hytale.server.core.asset.type.musiccontainer.config;

import com.hypixel.hytale.assetstore.map.IndexedAssetMap;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SequenceMusicContainer extends MusicContainer {
   @Nonnull
   public static final BuilderCodec<SequenceMusicContainer> CODEC = BuilderCodec.builder(
         SequenceMusicContainer.class, SequenceMusicContainer::new, MusicContainer.ABSTRACT_CODEC
      )
      .documentation("A music container which plays its tracks in sequence.")
      .<String[]>appendInherited(
         new KeyedCodec<>("Children", new ArrayCodec<>(MusicContainer.CHILD_ASSET_CODEC, String[]::new)),
         (mc, v) -> mc.children = v,
         mc -> mc.children,
         (mc, parent) -> mc.children = parent.children
      )
      .addValidator(Validators.nonEmptyArray())
      .addValidatorLate(() -> MusicContainer.VALIDATOR_CACHE.getArrayValidator().late())
      .add()
      .build();
   @Nullable
   protected String[] children;

   protected SequenceMusicContainer() {
   }

   public SequenceMusicContainer(@Nonnull String id) {
      super(id);
   }

   @Nonnull
   @Override
   public String[] getChildIds() {
      return this.children != null ? this.children : new String[0];
   }

   @Nonnull
   public com.hypixel.hytale.protocol.MusicContainer toPacket() {
      com.hypixel.hytale.protocol.SequenceMusicContainer packet = new com.hypixel.hytale.protocol.SequenceMusicContainer();
      this.fillBasePacketFields(packet);
      if (this.children != null && this.children.length > 0) {
         IndexedAssetMap<String, MusicContainer> assetMap = MusicContainer.getAssetMap();
         packet.children = new int[this.children.length];

         for (int i = 0; i < this.children.length; i++) {
            packet.children[i] = assetMap.getIndex(this.children[i]);
         }
      }

      return packet;
   }
}
