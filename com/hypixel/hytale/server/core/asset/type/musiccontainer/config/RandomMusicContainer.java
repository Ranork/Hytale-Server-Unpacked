package com.hypixel.hytale.server.core.asset.type.musiccontainer.config;

import com.hypixel.hytale.assetstore.AssetReferences;
import com.hypixel.hytale.assetstore.map.IndexedAssetMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.RandomMode;
import com.hypixel.hytale.server.core.asset.type.ambiencefx.config.AmbienceFXMusic;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RandomMusicContainer extends MusicContainer {
   @Nonnull
   public static final BuilderCodec<RandomMusicContainer> CODEC = BuilderCodec.builder(
         RandomMusicContainer.class, RandomMusicContainer::new, MusicContainer.ABSTRACT_CODEC
      )
      .documentation("A randomized music container.")
      .<RandomMode>appendInherited(
         new KeyedCodec<>("Mode", new EnumCodec<>(RandomMode.class)), (mc, v) -> mc.mode = v, mc -> mc.mode, (mc, parent) -> mc.mode = parent.mode
      )
      .documentation("Shuffle = even distribution. Random = weighted per-pick selection.")
      .add()
      .<Integer>appendInherited(
         new KeyedCodec<>("AvoidRepeatCount", Codec.INTEGER),
         (mc, v) -> mc.avoidRepeatCount = v,
         mc -> mc.avoidRepeatCount,
         (mc, parent) -> mc.avoidRepeatCount = parent.avoidRepeatCount
      )
      .documentation("How many recently played children to avoid re-selecting.")
      .addValidator(Validators.min(0))
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
   protected RandomMode mode = RandomMode.Shuffle;
   protected int avoidRepeatCount;
   @Nullable
   protected String[] children;

   protected RandomMusicContainer() {
      this.resumeMemoryDuration = 600.0F;
   }

   public RandomMusicContainer(@Nonnull String id) {
      super(id);
      this.resumeMemoryDuration = 600.0F;
   }

   @Nonnull
   public static RandomMusicContainer fromLegacy(
      @Nonnull String syntheticId, @Nonnull AmbienceFXMusic legacy, @Nonnull Map<MusicContainer, List<AssetReferences<?, ?>>> containerMap
   ) {
      RandomMusicContainer container = new RandomMusicContainer();
      container.volume = legacy.getVolume();
      container.loopCount = 0;
      container.mode = RandomMode.Shuffle;
      container.avoidRepeatCount = 1;
      container.resumeMemoryDuration = 600.0F;
      String[] tracks = legacy.getTracks();
      if (tracks != null && tracks.length > 0) {
         container.children = new String[tracks.length];

         for (int i = 0; i < tracks.length; i++) {
            String childId = syntheticId + "_" + i;
            SingleTrackMusicContainer singleTrack = new SingleTrackMusicContainer();
            singleTrack.id = childId;
            singleTrack.track = tracks[i];
            singleTrack.volume = 1.0F;
            singleTrack.loopCount = 1;
            singleTrack.weight = 1.0F;
            container.children[i] = childId;
            containerMap.put(singleTrack, Collections.emptyList());
         }
      }

      return container;
   }

   @Nonnull
   @Override
   public String[] getChildIds() {
      return this.children != null ? this.children : new String[0];
   }

   @Nonnull
   public com.hypixel.hytale.protocol.MusicContainer toPacket() {
      com.hypixel.hytale.protocol.RandomMusicContainer packet = new com.hypixel.hytale.protocol.RandomMusicContainer();
      this.fillBasePacketFields(packet);
      packet.mode = this.mode;
      packet.avoidRepeatCount = this.avoidRepeatCount;
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
