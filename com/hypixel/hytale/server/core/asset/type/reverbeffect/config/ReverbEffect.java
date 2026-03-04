package com.hypixel.hytale.server.core.asset.type.reverbeffect.config;

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

public class ReverbEffect
   implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, ReverbEffect>>,
   NetworkSerializable<com.hypixel.hytale.protocol.ReverbEffect> {
   public static final int EMPTY_ID = 0;
   public static final String EMPTY = "EMPTY";
   public static final ReverbEffect EMPTY_REVERB_EFFECT = new ReverbEffect("EMPTY");
   public static final AssetBuilderCodec<String, ReverbEffect> CODEC = AssetBuilderCodec.builder(
         ReverbEffect.class, ReverbEffect::new, Codec.STRING, (t, k) -> t.id = k, t -> t.id, (asset, data) -> asset.data = data, asset -> asset.data
      )
      .documentation("An asset used to define a reverb audio effect.")
      .metadata(new UIEditorPreview(UIEditorPreview.PreviewType.REVERB_EFFECT))
      .<Float>appendInherited(
         new KeyedCodec<>("DryGain", Codec.FLOAT),
         (reverb, f) -> reverb.dryGain = AudioUtil.decibelsToLinearGain(f),
         reverb -> AudioUtil.linearGainToDecibels(reverb.dryGain),
         (reverb, parent) -> reverb.dryGain = parent.dryGain
      )
      .metadata(new UIEditor(new UIEditor.FormattedNumber(null, " dB", null)))
      .addValidator(Validators.range(-100.0F, 10.0F))
      .documentation("Dry signal gain adjustment in decibels.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("ModalDensity", Codec.FLOAT),
         (reverb, f) -> reverb.modalDensity = f,
         reverb -> reverb.modalDensity,
         (reverb, parent) -> reverb.modalDensity = parent.modalDensity
      )
      .addValidator(Validators.range(0.0F, 1.0F))
      .documentation("Modal density of the reverb.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("Diffusion", Codec.FLOAT),
         (reverb, f) -> reverb.diffusion = f,
         reverb -> reverb.diffusion,
         (reverb, parent) -> reverb.diffusion = parent.diffusion
      )
      .addValidator(Validators.range(0.0F, 1.0F))
      .documentation("Diffusion of the reverb reflections.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("Gain", Codec.FLOAT),
         (reverb, f) -> reverb.gain = AudioUtil.decibelsToLinearGain(f),
         reverb -> AudioUtil.linearGainToDecibels(reverb.gain),
         (reverb, parent) -> reverb.gain = parent.gain
      )
      .metadata(new UIEditor(new UIEditor.FormattedNumber(null, " dB", null)))
      .addValidator(Validators.range(-100.0F, 0.0F))
      .documentation("Overall reverb gain in decibels.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("HighFrequencyGain", Codec.FLOAT),
         (reverb, f) -> reverb.highFrequencyGain = AudioUtil.decibelsToLinearGain(f),
         reverb -> AudioUtil.linearGainToDecibels(reverb.highFrequencyGain),
         (reverb, parent) -> reverb.highFrequencyGain = parent.highFrequencyGain
      )
      .metadata(new UIEditor(new UIEditor.FormattedNumber(null, " dB", null)))
      .addValidator(Validators.range(-100.0F, 0.0F))
      .documentation("High frequency gain in decibels.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("DecayTime", Codec.FLOAT),
         (reverb, f) -> reverb.decayTime = f,
         reverb -> reverb.decayTime,
         (reverb, parent) -> reverb.decayTime = parent.decayTime
      )
      .addValidator(Validators.range(0.1F, 20.0F))
      .documentation("Decay time in seconds.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("HighFrequencyDecayRatio", Codec.FLOAT),
         (reverb, f) -> reverb.highFrequencyDecayRatio = f,
         reverb -> reverb.highFrequencyDecayRatio,
         (reverb, parent) -> reverb.highFrequencyDecayRatio = parent.highFrequencyDecayRatio
      )
      .addValidator(Validators.range(0.1F, 2.0F))
      .documentation("High frequency decay ratio.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("ReflectionGain", Codec.FLOAT),
         (reverb, f) -> reverb.reflectionGain = AudioUtil.decibelsToLinearGain(f),
         reverb -> AudioUtil.linearGainToDecibels(reverb.reflectionGain),
         (reverb, parent) -> reverb.reflectionGain = parent.reflectionGain
      )
      .metadata(new UIEditor(new UIEditor.FormattedNumber(null, " dB", null)))
      .addValidator(Validators.range(-100.0F, 10.0F))
      .documentation("Early reflections gain in decibels.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("ReflectionDelay", Codec.FLOAT),
         (reverb, f) -> reverb.reflectionDelay = f,
         reverb -> reverb.reflectionDelay,
         (reverb, parent) -> reverb.reflectionDelay = parent.reflectionDelay
      )
      .addValidator(Validators.range(0.0F, 0.3F))
      .documentation("Early reflections delay in seconds.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("LateReverbGain", Codec.FLOAT),
         (reverb, f) -> reverb.lateReverbGain = AudioUtil.decibelsToLinearGain(f),
         reverb -> AudioUtil.linearGainToDecibels(reverb.lateReverbGain),
         (reverb, parent) -> reverb.lateReverbGain = parent.lateReverbGain
      )
      .metadata(new UIEditor(new UIEditor.FormattedNumber(null, " dB", null)))
      .addValidator(Validators.range(-100.0F, 20.0F))
      .documentation("Late reverb gain in decibels.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("LateReverbDelay", Codec.FLOAT),
         (reverb, f) -> reverb.lateReverbDelay = f,
         reverb -> reverb.lateReverbDelay,
         (reverb, parent) -> reverb.lateReverbDelay = parent.lateReverbDelay
      )
      .addValidator(Validators.range(0.0F, 0.1F))
      .documentation("Late reverb delay in seconds.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("RoomRolloffFactor", Codec.FLOAT),
         (reverb, f) -> reverb.roomRolloffFactor = f,
         reverb -> reverb.roomRolloffFactor,
         (reverb, parent) -> reverb.roomRolloffFactor = parent.roomRolloffFactor
      )
      .addValidator(Validators.range(0.0F, 10.0F))
      .documentation("Room rolloff factor.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("AirAbsorbptionHighFrequencyGain", Codec.FLOAT),
         (reverb, f) -> reverb.airAbsorptionHighFrequencyGain = AudioUtil.decibelsToLinearGain(f),
         reverb -> AudioUtil.linearGainToDecibels(reverb.airAbsorptionHighFrequencyGain),
         (reverb, parent) -> reverb.airAbsorptionHighFrequencyGain = parent.airAbsorptionHighFrequencyGain
      )
      .metadata(new UIEditor(new UIEditor.FormattedNumber(null, " dB", null)))
      .addValidator(Validators.range(-1.0F, 0.0F))
      .documentation("Air absorption high frequency gain in decibels.")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("LimitDecayHighFrequency", Codec.BOOLEAN),
         (reverb, b) -> reverb.limitDecayHighFrequency = b,
         reverb -> reverb.limitDecayHighFrequency,
         (reverb, parent) -> reverb.limitDecayHighFrequency = parent.limitDecayHighFrequency
      )
      .documentation("Whether to limit high frequency decay time.")
      .add()
      .build();
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(ReverbEffect::getAssetStore));
   private static AssetStore<String, ReverbEffect, IndexedLookupTableAssetMap<String, ReverbEffect>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected float dryGain = AudioUtil.decibelsToLinearGain(0.0F);
   protected float modalDensity = 1.0F;
   protected float diffusion = 1.0F;
   protected float gain = AudioUtil.decibelsToLinearGain(-10.0F);
   protected float highFrequencyGain = AudioUtil.decibelsToLinearGain(-1.0F);
   protected float decayTime = 1.49F;
   protected float highFrequencyDecayRatio = 0.83F;
   protected float reflectionGain = AudioUtil.decibelsToLinearGain(-26.0F);
   protected float reflectionDelay = 0.007F;
   protected float lateReverbGain = AudioUtil.decibelsToLinearGain(2.0F);
   protected float lateReverbDelay = 0.011F;
   protected float roomRolloffFactor = 0.0F;
   protected float airAbsorptionHighFrequencyGain = AudioUtil.decibelsToLinearGain(-0.05F);
   protected boolean limitDecayHighFrequency = true;
   private SoftReference<com.hypixel.hytale.protocol.ReverbEffect> cachedPacket;

   public static AssetStore<String, ReverbEffect, IndexedLookupTableAssetMap<String, ReverbEffect>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(ReverbEffect.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, ReverbEffect> getAssetMap() {
      return (IndexedLookupTableAssetMap<String, ReverbEffect>)getAssetStore().getAssetMap();
   }

   public ReverbEffect(String id) {
      this.id = id;
   }

   protected ReverbEffect() {
   }

   public String getId() {
      return this.id;
   }

   public float getDryGain() {
      return this.dryGain;
   }

   public float getModalDensity() {
      return this.modalDensity;
   }

   public float getDiffusion() {
      return this.diffusion;
   }

   public float getGain() {
      return this.gain;
   }

   public float getHighFrequencyGain() {
      return this.highFrequencyGain;
   }

   public float getDecayTime() {
      return this.decayTime;
   }

   public float getHighFrequencyDecayRatio() {
      return this.highFrequencyDecayRatio;
   }

   public float getReflectionGain() {
      return this.reflectionGain;
   }

   public float getReflectionDelay() {
      return this.reflectionDelay;
   }

   public float getLateReverbGain() {
      return this.lateReverbGain;
   }

   public float getLateReverbDelay() {
      return this.lateReverbDelay;
   }

   public float getRoomRolloffFactor() {
      return this.roomRolloffFactor;
   }

   public float getAirAbsorptionHighFrequencyGain() {
      return this.airAbsorptionHighFrequencyGain;
   }

   public boolean isLimitDecayHighFrequency() {
      return this.limitDecayHighFrequency;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ReverbEffect{id='"
         + this.id
         + "', dryGain="
         + this.dryGain
         + ", modalDensity="
         + this.modalDensity
         + ", diffusion="
         + this.diffusion
         + ", gain="
         + this.gain
         + ", highFrequencyGain="
         + this.highFrequencyGain
         + ", decayTime="
         + this.decayTime
         + ", highFrequencyDecayRatio="
         + this.highFrequencyDecayRatio
         + ", reflectionGain="
         + this.reflectionGain
         + ", reflectionDelay="
         + this.reflectionDelay
         + ", lateReverbGain="
         + this.lateReverbGain
         + ", lateReverbDelay="
         + this.lateReverbDelay
         + ", roomRolloffFactor="
         + this.roomRolloffFactor
         + ", airAbsorptionHighFrequencyGain="
         + this.airAbsorptionHighFrequencyGain
         + ", limitDecayHighFrequency="
         + this.limitDecayHighFrequency
         + "}";
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ReverbEffect toPacket() {
      com.hypixel.hytale.protocol.ReverbEffect cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.ReverbEffect packet = new com.hypixel.hytale.protocol.ReverbEffect();
         packet.id = this.id;
         packet.dryGain = this.dryGain;
         packet.modalDensity = this.modalDensity;
         packet.diffusion = this.diffusion;
         packet.gain = this.gain;
         packet.highFrequencyGain = this.highFrequencyGain;
         packet.decayTime = this.decayTime;
         packet.highFrequencyDecayRatio = this.highFrequencyDecayRatio;
         packet.reflectionGain = this.reflectionGain;
         packet.reflectionDelay = this.reflectionDelay;
         packet.lateReverbGain = this.lateReverbGain;
         packet.lateReverbDelay = this.lateReverbDelay;
         packet.roomRolloffFactor = this.roomRolloffFactor;
         packet.airAbsorptionHighFrequencyGain = this.airAbsorptionHighFrequencyGain;
         packet.limitDecayHighFrequency = this.limitDecayHighFrequency;
         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }
}
