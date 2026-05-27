package com.hypixel.hytale.server.core.asset.type.musiccontainer.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.asset.common.SoundFileValidators;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SingleTrackMusicContainer extends MusicContainer {
   @Nonnull
   public static final BuilderCodec<SingleTrackMusicContainer> CODEC = BuilderCodec.builder(
         SingleTrackMusicContainer.class, SingleTrackMusicContainer::new, MusicContainer.ABSTRACT_CODEC
      )
      .documentation(
         "A music container which plays a single audio track. Wrap in a Segment for overlapping loop transitions, pre-entry placement, or vertical layering."
      )
      .<String>appendInherited(new KeyedCodec<>("Track", Codec.STRING), (mc, v) -> mc.track = v, mc -> mc.track, (mc, parent) -> mc.track = parent.track)
      .addValidator(CommonAssetValidator.MUSIC)
      .addValidator(SoundFileValidators.MUSIC_SAMPLE_RATE)
      .add()
      .build();
   @Nullable
   protected String track;

   protected SingleTrackMusicContainer() {
   }

   public SingleTrackMusicContainer(@Nonnull String id) {
      super(id);
   }

   @Nullable
   public String getTrack() {
      return this.track;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.MusicContainer toPacket() {
      com.hypixel.hytale.protocol.SingleTrackMusicContainer packet = new com.hypixel.hytale.protocol.SingleTrackMusicContainer();
      this.fillBasePacketFields(packet);
      packet.track = this.track;
      return packet;
   }
}
