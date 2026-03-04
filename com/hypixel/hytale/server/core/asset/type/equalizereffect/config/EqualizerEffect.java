package com.hypixel.hytale.server.core.asset.type.equalizereffect.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditorPreview;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.AudioUtil;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.lang.ref.SoftReference;
import javax.annotation.Nonnull;

public class EqualizerEffect
   implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, EqualizerEffect>>,
   NetworkSerializable<com.hypixel.hytale.protocol.EqualizerEffect> {
   public static final int EMPTY_ID = 0;
   public static final String EMPTY = "EMPTY";
   public static final EqualizerEffect EMPTY_EQUALIZER_EFFECT = new EqualizerEffect("EMPTY");
   public static final float MIN_GAIN_DB = -18.0F;
   public static final float MAX_GAIN_DB = 18.0F;
   public static final float MIN_WIDTH = 0.01F;
   public static final float MAX_WIDTH = 1.0F;
   public static final float LOW_FREQ_MIN = 50.0F;
   public static final float LOW_FREQ_MAX = 800.0F;
   public static final float LOW_MID_FREQ_MIN = 200.0F;
   public static final float LOW_MID_FREQ_MAX = 3000.0F;
   public static final float HIGH_MID_FREQ_MIN = 1000.0F;
   public static final float HIGH_MID_FREQ_MAX = 8000.0F;
   public static final float HIGH_FREQ_MIN = 4000.0F;
   public static final float HIGH_FREQ_MAX = 16000.0F;
   public static final AssetBuilderCodec<String, EqualizerEffect> CODEC = AssetBuilderCodec.builder(
         EqualizerEffect.class, EqualizerEffect::new, Codec.STRING, (t, k) -> t.id = k, t -> t.id, (asset, data) -> asset.data = data, asset -> asset.data
      )
      .documentation("An asset used to define a 4-band equalizer audio effect.")
      .metadata(new UIEditorPreview(UIEditorPreview.PreviewType.EQUALIZER_EFFECT))
      .<Float>appendInherited(
         new KeyedCodec<>("LowGain", Codec.FLOAT),
         (eq, f) -> eq.lowGain = AudioUtil.decibelsToLinearGain(f),
         eq -> AudioUtil.linearGainToDecibels(eq.lowGain),
         (eq, parent) -> eq.lowGain = parent.lowGain
      )
      .metadata(new UIEditor(new UIEditor.FormattedNumber(null, " dB", null)))
      .addValidator(Validators.range(-18.0F, 18.0F))
      .documentation("Low band gain in decibels.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("LowCutOff", Codec.FLOAT), (eq, f) -> eq.lowCutOff = f, eq -> eq.lowCutOff, (eq, parent) -> eq.lowCutOff = parent.lowCutOff
      )
      .addValidator(Validators.range(50.0F, 800.0F))
      .documentation("Low band cutoff frequency in Hz.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("LowMidGain", Codec.FLOAT),
         (eq, f) -> eq.lowMidGain = AudioUtil.decibelsToLinearGain(f),
         eq -> AudioUtil.linearGainToDecibels(eq.lowMidGain),
         (eq, parent) -> eq.lowMidGain = parent.lowMidGain
      )
      .metadata(new UIEditor(new UIEditor.FormattedNumber(null, " dB", null)))
      .addValidator(Validators.range(-18.0F, 18.0F))
      .documentation("Low-mid band gain in decibels.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("LowMidCenter", Codec.FLOAT),
         (eq, f) -> eq.lowMidCenter = f,
         eq -> eq.lowMidCenter,
         (eq, parent) -> eq.lowMidCenter = parent.lowMidCenter
      )
      .addValidator(Validators.range(200.0F, 3000.0F))
      .documentation("Low-mid band center frequency in Hz.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("LowMidWidth", Codec.FLOAT), (eq, f) -> eq.lowMidWidth = f, eq -> eq.lowMidWidth, (eq, parent) -> eq.lowMidWidth = parent.lowMidWidth
      )
      .addValidator(Validators.range(0.01F, 1.0F))
      .documentation("Low-mid band width.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("HighMidGain", Codec.FLOAT),
         (eq, f) -> eq.highMidGain = AudioUtil.decibelsToLinearGain(f),
         eq -> AudioUtil.linearGainToDecibels(eq.highMidGain),
         (eq, parent) -> eq.highMidGain = parent.highMidGain
      )
      .metadata(new UIEditor(new UIEditor.FormattedNumber(null, " dB", null)))
      .addValidator(Validators.range(-18.0F, 18.0F))
      .documentation("High-mid band gain in decibels.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("HighMidCenter", Codec.FLOAT),
         (eq, f) -> eq.highMidCenter = f,
         eq -> eq.highMidCenter,
         (eq, parent) -> eq.highMidCenter = parent.highMidCenter
      )
      .addValidator(Validators.range(1000.0F, 8000.0F))
      .documentation("High-mid band center frequency in Hz.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("HighMidWidth", Codec.FLOAT),
         (eq, f) -> eq.highMidWidth = f,
         eq -> eq.highMidWidth,
         (eq, parent) -> eq.highMidWidth = parent.highMidWidth
      )
      .addValidator(Validators.range(0.01F, 1.0F))
      .documentation("High-mid band width.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("HighGain", Codec.FLOAT),
         (eq, f) -> eq.highGain = AudioUtil.decibelsToLinearGain(f),
         eq -> AudioUtil.linearGainToDecibels(eq.highGain),
         (eq, parent) -> eq.highGain = parent.highGain
      )
      .metadata(new UIEditor(new UIEditor.FormattedNumber(null, " dB", null)))
      .addValidator(Validators.range(-18.0F, 18.0F))
      .documentation("High band gain in decibels.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("HighCutOff", Codec.FLOAT), (eq, f) -> eq.highCutOff = f, eq -> eq.highCutOff, (eq, parent) -> eq.highCutOff = parent.highCutOff
      )
      .addValidator(Validators.range(4000.0F, 16000.0F))
      .documentation("High band cutoff frequency in Hz.")
      .add()
      .build();
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(EqualizerEffect::getAssetStore));
   private static AssetStore<String, EqualizerEffect, IndexedLookupTableAssetMap<String, EqualizerEffect>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected float lowGain = 1.0F;
   protected float lowCutOff = 200.0F;
   protected float lowMidGain = 1.0F;
   protected float lowMidCenter = 500.0F;
   protected float lowMidWidth = 1.0F;
   protected float highMidGain = 1.0F;
   protected float highMidCenter = 3000.0F;
   protected float highMidWidth = 1.0F;
   protected float highGain = 1.0F;
   protected float highCutOff = 6000.0F;
   private SoftReference<com.hypixel.hytale.protocol.EqualizerEffect> cachedPacket;

   public static AssetStore<String, EqualizerEffect, IndexedLookupTableAssetMap<String, EqualizerEffect>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(EqualizerEffect.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, EqualizerEffect> getAssetMap() {
      return (IndexedLookupTableAssetMap<String, EqualizerEffect>)getAssetStore().getAssetMap();
   }

   public EqualizerEffect(String id) {
      this.id = id;
   }

   protected EqualizerEffect() {
   }

   public String getId() {
      return this.id;
   }

   public float getLowGain() {
      return this.lowGain;
   }

   public float getLowCutOff() {
      return this.lowCutOff;
   }

   public float getLowMidGain() {
      return this.lowMidGain;
   }

   public float getLowMidCenter() {
      return this.lowMidCenter;
   }

   public float getLowMidWidth() {
      return this.lowMidWidth;
   }

   public float getHighMidGain() {
      return this.highMidGain;
   }

   public float getHighMidCenter() {
      return this.highMidCenter;
   }

   public float getHighMidWidth() {
      return this.highMidWidth;
   }

   public float getHighGain() {
      return this.highGain;
   }

   public float getHighCutOff() {
      return this.highCutOff;
   }

   @Nonnull
   @Override
   public String toString() {
      return "EqualizerEffect{id='"
         + this.id
         + "', lowGain="
         + this.lowGain
         + ", lowCutOff="
         + this.lowCutOff
         + ", lowMidGain="
         + this.lowMidGain
         + ", lowMidCenter="
         + this.lowMidCenter
         + ", lowMidWidth="
         + this.lowMidWidth
         + ", highMidGain="
         + this.highMidGain
         + ", highMidCenter="
         + this.highMidCenter
         + ", highMidWidth="
         + this.highMidWidth
         + ", highGain="
         + this.highGain
         + ", highCutOff="
         + this.highCutOff
         + "}";
   }

   @Nonnull
   public com.hypixel.hytale.protocol.EqualizerEffect toPacket() {
      com.hypixel.hytale.protocol.EqualizerEffect cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.EqualizerEffect packet = new com.hypixel.hytale.protocol.EqualizerEffect();
         packet.id = this.id;
         packet.lowGain = this.lowGain;
         packet.lowCutOff = this.lowCutOff;
         packet.lowMidGain = this.lowMidGain;
         packet.lowMidCenter = this.lowMidCenter;
         packet.lowMidWidth = this.lowMidWidth;
         packet.highMidGain = this.highMidGain;
         packet.highMidCenter = this.highMidCenter;
         packet.highMidWidth = this.highMidWidth;
         packet.highGain = this.highGain;
         packet.highCutOff = this.highCutOff;
         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }
}
