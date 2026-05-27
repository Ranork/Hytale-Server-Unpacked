package com.hypixel.hytale.server.core.asset.type.musiccontainer.config;

import com.hypixel.hytale.assetstore.map.IndexedAssetMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.MusicTransitionType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HorizontalMusicContainer extends MusicContainer {
   @Nonnull
   public static final BuilderCodec<HorizontalMusicContainer> CODEC = BuilderCodec.builder(
         HorizontalMusicContainer.class, HorizontalMusicContainer::new, MusicContainer.ABSTRACT_CODEC
      )
      .documentation("A music container designed for horizontal dynamic music, where transitions go from segment to segment.")
      .<MusicTransitionType>appendInherited(
         new KeyedCodec<>("DefaultPhaseTransitionType", new EnumCodec<>(MusicTransitionType.class)),
         (mc, v) -> mc.defaultPhaseTransitionType = v,
         mc -> mc.defaultPhaseTransitionType,
         (mc, parent) -> mc.defaultPhaseTransitionType = parent.defaultPhaseTransitionType
      )
      .documentation("Default transition type for all phase transitions. Children can override via their own TransitionType.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("DefaultPhaseTransitionDuration", Codec.FLOAT),
         (mc, v) -> mc.defaultPhaseTransitionDuration = v,
         mc -> mc.defaultPhaseTransitionDuration,
         (mc, parent) -> mc.defaultPhaseTransitionDuration = parent.defaultPhaseTransitionDuration
      )
      .documentation("Default transition duration for all phase transitions.")
      .add()
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
   @Nonnull
   protected MusicTransitionType defaultPhaseTransitionType = MusicTransitionType.Crossfade;
   protected float defaultPhaseTransitionDuration;
   @Nullable
   protected String[] children;

   protected HorizontalMusicContainer() {
   }

   public HorizontalMusicContainer(@Nonnull String id) {
      super(id);
   }

   @Nonnull
   @Override
   public String[] getChildIds() {
      return this.children != null ? this.children : new String[0];
   }

   @Nonnull
   public com.hypixel.hytale.protocol.MusicContainer toPacket() {
      com.hypixel.hytale.protocol.HorizontalMusicContainer packet = new com.hypixel.hytale.protocol.HorizontalMusicContainer();
      this.fillBasePacketFields(packet);
      packet.defaultPhaseTransitionType = this.defaultPhaseTransitionType;
      packet.defaultPhaseTransitionDuration = this.defaultPhaseTransitionDuration;
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
