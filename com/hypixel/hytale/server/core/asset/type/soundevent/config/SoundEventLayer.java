package com.hypixel.hytale.server.core.asset.type.soundevent.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.codec.validation.validator.ArrayValidator;
import com.hypixel.hytale.common.util.AudioUtil;
import com.hypixel.hytale.protocol.SoundEventLayerRandomSettings;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.asset.common.OggVorbisInfoCache;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class SoundEventLayer implements NetworkSerializable<com.hypixel.hytale.protocol.SoundEventLayer> {
   public static final Codec<SoundEventLayer> CODEC = BuilderCodec.builder(SoundEventLayer.class, SoundEventLayer::new)
      .append(
         new KeyedCodec<>("Volume", Codec.FLOAT),
         (soundEventLayer, f) -> soundEventLayer.volume = AudioUtil.decibelsToLinearGain(f),
         soundEventLayer -> AudioUtil.linearGainToDecibels(soundEventLayer.volume)
      )
      .metadata(new UIEditor(new UIEditor.FormattedNumber(null, " dB", null)))
      .addValidator(Validators.range(-100.0F, 10.0F))
      .documentation("Volume offset for this layer in decibels.")
      .add()
      .<Float>append(
         new KeyedCodec<>("StartDelay", Codec.FLOAT), (soundEventLayer, f) -> soundEventLayer.startDelay = f, soundEventLayer -> soundEventLayer.startDelay
      )
      .documentation("A delay in seconds from when the sound event starts after which this layer should begin.")
      .add()
      .<Boolean>append(
         new KeyedCodec<>("Looping", Codec.BOOLEAN), (soundEventLayer, b) -> soundEventLayer.looping = b, soundEventLayer -> soundEventLayer.looping
      )
      .documentation("Whether this layer loops.")
      .add()
      .<Integer>append(
         new KeyedCodec<>("Probability", Codec.INTEGER),
         (soundEventLayer, i) -> soundEventLayer.probability = i,
         soundEventLayer -> soundEventLayer.probability
      )
      .documentation("The probability of this layer being played when the sound event is triggered in percentage.")
      .add()
      .<Float>append(
         new KeyedCodec<>("ProbabilityRerollDelay", Codec.FLOAT),
         (soundEventLayer, f) -> soundEventLayer.probabilityRerollDelay = f,
         soundEventLayer -> soundEventLayer.probabilityRerollDelay
      )
      .documentation("A delay in seconds before the probability of this layer playing can be rerolled to see if it will now play (or not play) again.")
      .add()
      .<SoundEventLayer.RandomSettings>append(
         new KeyedCodec<>("RandomSettings", SoundEventLayer.RandomSettings.CODEC),
         (soundEventLayer, o) -> soundEventLayer.randomSettings = o,
         soundEventLayer -> soundEventLayer.randomSettings
      )
      .documentation("Randomization settings for parameters of this layer.")
      .add()
      .<String[]>append(
         new KeyedCodec<>("Files", Codec.STRING_ARRAY), (soundEventLayer, s) -> soundEventLayer.files = s, soundEventLayer -> soundEventLayer.files
      )
      .addValidator(Validators.nonEmptyArray())
      .addValidator(new ArrayValidator<>(CommonAssetValidator.SOUNDS))
      .documentation("The list of possible sound files for this layer. One will be chosen at random.")
      .add()
      .<Integer>append(
         new KeyedCodec<>("RoundRobinHistorySize", Codec.INTEGER),
         (soundEventLayer, i) -> soundEventLayer.roundRobinHistorySize = i,
         soundEventLayer -> soundEventLayer.roundRobinHistorySize
      )
      .addValidator(Validators.range(0, 32))
      .documentation("The same sound file will not repeat within this many plays. 0 disables round-robin behavior.")
      .add()
      .afterDecode(layer -> {
         if (layer.files != null) {
            for (String file : layer.files) {
               OggVorbisInfoCache.OggVorbisInfo info = OggVorbisInfoCache.getNow(file);
               if (info != null && info.channels > layer.highestNumberOfChannels) {
                  layer.highestNumberOfChannels = info.channels;
               }
            }
         }
      })
      .build();
   protected transient float volume = 1.0F;
   protected float startDelay = 0.0F;
   protected boolean looping = false;
   protected int probability = 100;
   protected float probabilityRerollDelay = 1.0F;
   protected SoundEventLayer.RandomSettings randomSettings = SoundEventLayer.RandomSettings.DEFAULT;
   protected String[] files;
   protected int roundRobinHistorySize = 0;
   protected transient int highestNumberOfChannels = 0;

   public SoundEventLayer(
      float volume,
      float startDelay,
      boolean looping,
      int probability,
      float probabilityRerollDelay,
      SoundEventLayer.RandomSettings randomSettings,
      String[] files,
      int roundRobinHistorySize
   ) {
      this.volume = volume;
      this.startDelay = startDelay;
      this.looping = looping;
      this.probability = probability;
      this.probabilityRerollDelay = probabilityRerollDelay;
      this.randomSettings = randomSettings;
      this.files = files;
      this.roundRobinHistorySize = roundRobinHistorySize;
   }

   protected SoundEventLayer() {
   }

   public float getVolume() {
      return this.volume;
   }

   public float getStartDelay() {
      return this.startDelay;
   }

   public boolean isLooping() {
      return this.looping;
   }

   public int getProbability() {
      return this.probability;
   }

   public float getProbabilityRerollDelay() {
      return this.probabilityRerollDelay;
   }

   public SoundEventLayer.RandomSettings getRandomSettings() {
      return this.randomSettings;
   }

   public String[] getFiles() {
      return this.files;
   }

   public int getRoundRobinHistorySize() {
      return this.roundRobinHistorySize;
   }

   public int getHighestNumberOfChannels() {
      return this.highestNumberOfChannels;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.SoundEventLayer toPacket() {
      com.hypixel.hytale.protocol.SoundEventLayer packet = new com.hypixel.hytale.protocol.SoundEventLayer();
      packet.volume = this.volume;
      packet.startDelay = this.startDelay;
      packet.looping = this.looping;
      packet.probability = this.probability;
      packet.probabilityRerollDelay = this.probabilityRerollDelay;
      packet.randomSettings = new SoundEventLayerRandomSettings();
      packet.randomSettings.minVolume = this.randomSettings.minVolume;
      packet.randomSettings.maxVolume = this.randomSettings.maxVolume;
      packet.randomSettings.minPitch = this.randomSettings.minPitch;
      packet.randomSettings.maxPitch = this.randomSettings.maxPitch;
      packet.randomSettings.maxStartOffset = this.randomSettings.maxStartOffset;
      packet.files = this.files;
      packet.roundRobinHistorySize = this.roundRobinHistorySize;
      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "SoundEventLayer{, volume="
         + this.volume
         + ", startDelay="
         + this.startDelay
         + ", looping="
         + this.looping
         + ", probability="
         + this.probability
         + ", probabilityRerollDelay="
         + this.probabilityRerollDelay
         + ", randomSettings="
         + this.randomSettings
         + ", files="
         + Arrays.toString((Object[])this.files)
         + ", roundRobinHistorySize="
         + this.roundRobinHistorySize
         + ", highestNumberOfChannels="
         + this.highestNumberOfChannels
         + "}";
   }

   public static class RandomSettings {
      public static final Codec<SoundEventLayer.RandomSettings> CODEC = BuilderCodec.builder(
            SoundEventLayer.RandomSettings.class, SoundEventLayer.RandomSettings::new
         )
         .append(
            new KeyedCodec<>("MinVolume", Codec.FLOAT),
            (soundEventLayer, f) -> soundEventLayer.minVolume = AudioUtil.decibelsToLinearGain(f),
            soundEventLayer -> AudioUtil.linearGainToDecibels(soundEventLayer.minVolume)
         )
         .addValidator(Validators.range(-100.0F, 0.0F))
         .documentation("Minimum additional random volume offset in decibels.")
         .add()
         .<Float>append(
            new KeyedCodec<>("MaxVolume", Codec.FLOAT),
            (soundEventLayer, f) -> soundEventLayer.maxVolume = AudioUtil.decibelsToLinearGain(f),
            soundEventLayer -> AudioUtil.linearGainToDecibels(soundEventLayer.maxVolume)
         )
         .addValidator(Validators.range(0.0F, 10.0F))
         .documentation("Maximum additional random volume offset in decibels.")
         .add()
         .<Float>append(
            new KeyedCodec<>("MinPitch", Codec.FLOAT),
            (soundEventLayer, f) -> soundEventLayer.minPitch = AudioUtil.semitonesToLinearPitch(f),
            soundEventLayer -> AudioUtil.linearPitchToSemitones(soundEventLayer.minPitch)
         )
         .addValidator(Validators.range(-12.0F, 0.0F))
         .documentation("Minimum additional random pitch offset in semitones.")
         .add()
         .<Float>append(
            new KeyedCodec<>("MaxPitch", Codec.FLOAT),
            (soundEventLayer, f) -> soundEventLayer.maxPitch = AudioUtil.semitonesToLinearPitch(f),
            soundEventLayer -> AudioUtil.linearPitchToSemitones(soundEventLayer.maxPitch)
         )
         .addValidator(Validators.range(0.0F, 12.0F))
         .documentation("Maximum additional random pitch offset in semitones.")
         .add()
         .<Float>append(
            new KeyedCodec<>("MaxStartOffset", Codec.FLOAT),
            (soundEventLayer, f) -> soundEventLayer.maxStartOffset = f,
            soundEventLayer -> soundEventLayer.maxStartOffset
         )
         .addValidator(Validators.range(0.0F, Float.MAX_VALUE))
         .documentation(
            "Maximum amount by which to offset the start of this sound event (e.g. start up to x seconds into the sound). This should only really be used for looping sounds to prevent phasing issues."
         )
         .add()
         .build();
      public static final SoundEventLayer.RandomSettings DEFAULT = new SoundEventLayer.RandomSettings();
      protected transient float minVolume = 1.0F;
      protected transient float maxVolume = 1.0F;
      protected transient float minPitch = 1.0F;
      protected transient float maxPitch = 1.0F;
      protected float maxStartOffset;

      public float getMinVolume() {
         return this.minVolume;
      }

      public float getMaxVolume() {
         return this.maxVolume;
      }

      public float getMinPitch() {
         return this.minPitch;
      }

      public float getMaxPitch() {
         return this.maxPitch;
      }

      public float getMaxStartOffset() {
         return this.maxStartOffset;
      }

      @Nonnull
      @Override
      public String toString() {
         return "RandomSettings{, minVolume="
            + this.minVolume
            + ", maxVolume="
            + this.maxVolume
            + ", minPitch="
            + this.minPitch
            + ", maxPitch="
            + this.maxPitch
            + ", maxStartOffset="
            + this.maxStartOffset
            + "}";
      }
   }
}
