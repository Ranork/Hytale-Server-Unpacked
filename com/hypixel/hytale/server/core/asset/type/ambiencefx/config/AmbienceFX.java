package com.hypixel.hytale.server.core.asset.type.ambiencefx.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.IndexedAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDefaultCollapsedState;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditorSectionStart;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.server.core.asset.type.audiocategory.config.AudioCategory;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AmbienceFX implements JsonAssetWithMap<String, IndexedAssetMap<String, AmbienceFX>>, NetworkSerializable<com.hypixel.hytale.protocol.AmbienceFX> {
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(AmbienceFX::getAssetStore));
   public static final AssetBuilderCodec<String, AmbienceFX> CODEC = AssetBuilderCodec.builder(
         AmbienceFX.class,
         AmbienceFX::new,
         Codec.STRING,
         (ambienceFX, k) -> ambienceFX.id = k,
         ambienceFX -> ambienceFX.id,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .appendInherited(
         new KeyedCodec<>("Conditions", AmbienceFXConditions.CODEC),
         (ambienceFX, l) -> ambienceFX.conditions = l,
         ambienceFX -> ambienceFX.conditions,
         (ambienceFX, parent) -> ambienceFX.conditions = parent.conditions
      )
      .metadata(new UIEditorSectionStart("Conditions"))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .<AmbienceFXSound[]>appendInherited(
         new KeyedCodec<>("Sounds", new ArrayCodec<>(AmbienceFXSound.CODEC, AmbienceFXSound[]::new)),
         (ambienceFX, l) -> ambienceFX.sounds = l,
         ambienceFX -> ambienceFX.sounds,
         (ambienceFX, parent) -> ambienceFX.sounds = parent.sounds
      )
      .metadata(new UIEditorSectionStart("Audio"))
      .add()
      .appendInherited(
         new KeyedCodec<>("Music", AmbienceFXMusic.CODEC),
         (ambienceFX, l) -> ambienceFX.music = l,
         ambienceFX -> ambienceFX.music,
         (ambienceFX, parent) -> ambienceFX.music = parent.music
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("AmbientBed", AmbienceFXAmbientBed.CODEC),
         (ambienceFX, l) -> ambienceFX.ambientBed = l,
         ambienceFX -> ambienceFX.ambientBed,
         (ambienceFX, parent) -> ambienceFX.ambientBed = parent.ambientBed
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("SoundEffect", AmbienceFXSoundEffect.CODEC),
         (ambienceFX, l) -> ambienceFX.soundEffect = l,
         ambienceFX -> ambienceFX.soundEffect,
         (ambienceFX, parent) -> ambienceFX.soundEffect = parent.soundEffect
      )
      .add()
      .<Integer>appendInherited(
         new KeyedCodec<>("Priority", Codec.INTEGER),
         (ambienceFX, i) -> ambienceFX.priority = i,
         ambienceFX -> ambienceFX.priority,
         (ambienceFX, parent) -> ambienceFX.priority = parent.priority
      )
      .addValidator(Validators.greaterThanOrEqual(0))
      .documentation("Priority for this AmbienceFX. Only applies to music and sound effect. Higher number means higher priority.")
      .add()
      .<String[]>appendInherited(
         new KeyedCodec<>("BlockedAmbienceFxIds", Codec.STRING_ARRAY),
         (ambienceFX, s) -> ambienceFX.blockedAmbienceFxIds = s,
         ambienceFX -> ambienceFX.blockedAmbienceFxIds,
         (ambienceFX, parent) -> ambienceFX.blockedAmbienceFxIds = parent.blockedAmbienceFxIds
      )
      .addValidatorLate(() -> VALIDATOR_CACHE.getArrayValidator().late())
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("AudioCategory", Codec.STRING),
         (ambienceFX, s) -> ambienceFX.audioCategoryId = s,
         ambienceFX -> ambienceFX.audioCategoryId,
         (ambienceFX, parent) -> ambienceFX.audioCategoryId = parent.audioCategoryId
      )
      .addValidator(AudioCategory.VALIDATOR_CACHE.getValidator())
      .documentation("Audio category to assign this ambienceFX to for additional property routing. Only affects ambient bed and music, not emitters.")
      .add()
      .afterDecode(ambienceFX -> {
         if (ambienceFX.audioCategoryId != null) {
            ambienceFX.audioCategoryIndex = AudioCategory.getAssetMap().getIndex(ambienceFX.audioCategoryId);
         }
      })
      .build();
   public static final int EMPTY_ID = 0;
   public static final AmbienceFX EMPTY = new AmbienceFX() {
      {
         this.id = "Empty";
      }
   };
   private static AssetStore<String, AmbienceFX, IndexedAssetMap<String, AmbienceFX>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected AmbienceFXConditions conditions;
   protected AmbienceFXSound[] sounds;
   protected AmbienceFXMusic music;
   protected AmbienceFXAmbientBed ambientBed;
   protected AmbienceFXSoundEffect soundEffect;
   protected int priority = 0;
   protected String[] blockedAmbienceFxIds;
   @Nullable
   protected String audioCategoryId = null;
   protected transient int audioCategoryIndex = 0;
   private SoftReference<com.hypixel.hytale.protocol.AmbienceFX> cachedPacket;

   public static AssetStore<String, AmbienceFX, IndexedAssetMap<String, AmbienceFX>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(AmbienceFX.class);
      }

      return ASSET_STORE;
   }

   public static IndexedAssetMap<String, AmbienceFX> getAssetMap() {
      return (IndexedAssetMap<String, AmbienceFX>)getAssetStore().getAssetMap();
   }

   public AmbienceFX(String id) {
      this.id = id;
   }

   protected AmbienceFX() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.AmbienceFX toPacket() {
      com.hypixel.hytale.protocol.AmbienceFX cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.AmbienceFX packet = new com.hypixel.hytale.protocol.AmbienceFX();
         packet.id = this.id;
         if (this.conditions != null) {
            packet.conditions = this.conditions.toPacket();
         }

         if (this.sounds != null && this.sounds.length > 0) {
            packet.sounds = ArrayUtil.copyAndMutate(this.sounds, AmbienceFXSound::toPacket, com.hypixel.hytale.protocol.AmbienceFXSound[]::new);
         }

         if (this.music != null) {
            packet.music = this.music.toPacket();
         }

         if (this.ambientBed != null) {
            packet.ambientBed = this.ambientBed.toPacket();
         }

         if (this.soundEffect != null) {
            packet.soundEffect = this.soundEffect.toPacket();
         }

         packet.priority = this.priority;
         if (this.blockedAmbienceFxIds != null) {
            packet.blockedAmbienceFxIndices = new int[this.blockedAmbienceFxIds.length];

            for (int i = 0; i < this.blockedAmbienceFxIds.length; i++) {
               packet.blockedAmbienceFxIndices[i] = getAssetMap().getIndex(this.blockedAmbienceFxIds[i]);
            }
         }

         packet.audioCategoryIndex = this.audioCategoryIndex;
         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }

   public String getId() {
      return this.id;
   }

   public AmbienceFXConditions getConditions() {
      return this.conditions;
   }

   public AmbienceFXSound[] getSounds() {
      return this.sounds;
   }

   public AmbienceFXMusic getMusic() {
      return this.music;
   }

   public AmbienceFXAmbientBed getAmbientBed() {
      return this.ambientBed;
   }

   public AmbienceFXSoundEffect getSoundEffect() {
      return this.soundEffect;
   }

   public int getPriority() {
      return this.priority;
   }

   public String[] getBlockedAmbienceFxIds() {
      return this.blockedAmbienceFxIds;
   }

   @Nonnull
   @Override
   public String toString() {
      return "AmbienceFX{id='"
         + this.id
         + "', conditions="
         + this.conditions
         + ", sounds="
         + Arrays.toString((Object[])this.sounds)
         + ", music="
         + this.music
         + ", ambientBed="
         + this.ambientBed
         + ", soundEffect='"
         + this.soundEffect
         + ", priority="
         + this.priority
         + "', blockedAmbienceFxIds="
         + Arrays.toString((Object[])this.blockedAmbienceFxIds)
         + ", audioCategoryId="
         + this.audioCategoryId
         + "}";
   }
}
