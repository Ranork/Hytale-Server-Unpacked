package com.hypixel.hytale.server.core.asset.type.musiccontainer.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.event.RemovedAssetsEvent;
import com.hypixel.hytale.assetstore.map.IndexedAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDisplayMode;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.AudioUtil;
import com.hypixel.hytale.protocol.MusicTransitionType;
import com.hypixel.hytale.protocol.Rangef;
import com.hypixel.hytale.protocol.TempoSettings;
import com.hypixel.hytale.server.core.asset.type.audiocategory.config.AudioCategory;
import com.hypixel.hytale.server.core.asset.type.audiostate.config.AudioState;
import com.hypixel.hytale.server.core.asset.type.audiostate.config.AudioStateResolver;
import com.hypixel.hytale.server.core.asset.type.audiostate.config.StateBindingConfig;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class MusicContainer
   implements JsonAssetWithMap<String, IndexedAssetMap<String, MusicContainer>>,
   NetworkSerializable<com.hypixel.hytale.protocol.MusicContainer> {
   @Nonnull
   public static final AssetCodecMapCodec<String, MusicContainer> CODEC = new AssetCodecMapCodec<>(
      Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data
   );
   @Nonnull
   public static final Codec<String> CHILD_ASSET_CODEC = new ContainedAssetCodec<>(MusicContainer.class, CODEC);
   @Nonnull
   public static final BuilderCodec<TempoSettings> TEMPO_CODEC = BuilderCodec.builder(TempoSettings.class, TempoSettings::new)
      .metadata(UIDisplayMode.COMPACT)
      .<Float>append(new KeyedCodec<>("Bpm", Codec.FLOAT, true), (ts, f) -> ts.bpm = f, ts -> ts.bpm)
      .addValidator(Validators.range(1.0F, 1000.0F))
      .add()
      .<Integer>append(new KeyedCodec<>("BeatsPerBar", Codec.INTEGER, true), (ts, i) -> ts.beatsPerBar = i, ts -> ts.beatsPerBar)
      .addValidator(Validators.range(1, 32))
      .add()
      .<Integer>append(new KeyedCodec<>("BeatValue", Codec.INTEGER, true), (ts, i) -> ts.beatValue = i, ts -> ts.beatValue)
      .addValidator(Validators.range(1, 32))
      .add()
      .build();
   @Nonnull
   public static final BuilderCodec<MusicContainer> ABSTRACT_CODEC = BuilderCodec.abstractBuilder(MusicContainer.class)
      .appendInherited(
         new KeyedCodec<>("Volume", Codec.FLOAT),
         (mc, f) -> mc.volume = AudioUtil.decibelsToLinearGain(f),
         mc -> AudioUtil.linearGainToDecibels(mc.volume),
         (mc, parent) -> mc.volume = parent.volume
      )
      .metadata(new UIEditor(new UIEditor.FormattedNumber(null, " dB", null)))
      .addValidator(Validators.range(-100.0F, 10.0F))
      .add()
      .<Integer>appendInherited(
         new KeyedCodec<>("LoopCount", Codec.INTEGER), (mc, v) -> mc.loopCount = v, mc -> mc.loopCount, (mc, parent) -> mc.loopCount = parent.loopCount
      )
      .documentation("Number of times to loop. 0 = infinite.")
      .addValidator(Validators.min(0))
      .add()
      .<Float>appendInherited(new KeyedCodec<>("Weight", Codec.FLOAT), (mc, v) -> mc.weight = v, mc -> mc.weight, (mc, parent) -> mc.weight = parent.weight)
      .documentation("Relative weight for random selection when parent is a Random container.")
      .addValidator(Validators.min(0.001F))
      .add()
      .<Rangef>appendInherited(
         new KeyedCodec<>("SilenceAfter", ProtocolCodecs.RANGEF),
         (mc, v) -> mc.silenceAfter = v,
         mc -> mc.silenceAfter,
         (mc, parent) -> mc.silenceAfter = parent.silenceAfter
      )
      .documentation("Random silence range (seconds) after this container completes as a child.")
      .add()
      .<Rangef>appendInherited(
         new KeyedCodec<>("ExitSilence", ProtocolCodecs.RANGEF),
         (mc, v) -> mc.exitSilence = v,
         mc -> mc.exitSilence,
         (mc, parent) -> mc.exitSilence = parent.exitSilence
      )
      .documentation("Random silence range (seconds) when this top-level container is replaced by another. Only valid in top-level containers.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("FadeInDuration", Codec.FLOAT),
         (mc, v) -> mc.fadeInDuration = v,
         mc -> mc.fadeInDuration,
         (mc, parent) -> mc.fadeInDuration = parent.fadeInDuration
      )
      .documentation("Fade-in duration in seconds.")
      .addValidator(Validators.min(0.0F))
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("FadeOutDuration", Codec.FLOAT),
         (mc, v) -> mc.fadeOutDuration = v,
         mc -> mc.fadeOutDuration,
         (mc, parent) -> mc.fadeOutDuration = parent.fadeOutDuration
      )
      .documentation("Fade-out duration in seconds.")
      .addValidator(Validators.min(0.0F))
      .add()
      .<MusicTransitionType>appendInherited(
         new KeyedCodec<>("TransitionType", new EnumCodec<>(MusicTransitionType.class)),
         (mc, v) -> mc.transitionType = v,
         mc -> mc.transitionType,
         (mc, parent) -> mc.transitionType = parent.transitionType
      )
      .documentation("Per-child override for how a Horizontal parent transitions into this container.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("TransitionDuration", Codec.FLOAT),
         (mc, v) -> mc.transitionDuration = v,
         mc -> mc.transitionDuration,
         (mc, parent) -> mc.transitionDuration = parent.transitionDuration
      )
      .documentation("Per-child override for transition duration. 0 = use parent's default.")
      .addValidator(Validators.min(0.0F))
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("PlayToCompletion", Codec.BOOLEAN),
         (mc, v) -> mc.playToCompletion = v,
         mc -> mc.playToCompletion,
         (mc, parent) -> mc.playToCompletion = parent.playToCompletion
      )
      .documentation("If true, this container plays to completion before yielding to higher-priority music.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("ResumeMemoryDuration", Codec.FLOAT),
         (mc, v) -> mc.resumeMemoryDuration = v,
         mc -> mc.resumeMemoryDuration,
         (mc, parent) -> mc.resumeMemoryDuration = parent.resumeMemoryDuration
      )
      .documentation("How long to retain this container's playback state after leaving so a return resumes mid-track instead of restarting.")
      .addValidator(Validators.min(0.0F))
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("NameTranslationKey", Codec.STRING),
         (mc, s) -> mc.nameTranslationKey = s,
         mc -> mc.nameTranslationKey,
         (mc, parent) -> mc.nameTranslationKey = parent.nameTranslationKey
      )
      .documentation("Translation key for the display name of this music container.")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("AudioCategory", Codec.STRING),
         (mc, s) -> mc.audioCategoryId = s,
         mc -> mc.audioCategoryId,
         (mc, parent) -> mc.audioCategoryId = parent.audioCategoryId
      )
      .addValidator(AudioCategory.VALIDATOR_CACHE.getValidator())
      .documentation("Audio category for volume routing. Only valid in top-level containers.")
      .add()
      .<TempoSettings>appendInherited(new KeyedCodec<>("Tempo", TEMPO_CODEC), (mc, v) -> mc.tempo = v, mc -> mc.tempo, (mc, parent) -> mc.tempo = parent.tempo)
      .documentation("Tempo and beat grid settings.")
      .add()
      .<StateBindingConfig[]>append(new KeyedCodec<>("StateBindings", StateBindingConfig.CODEC_ARRAY), (mc, v) -> mc.stateBindings = v, mc -> mc.stateBindings)
      .documentation("Subscribe this container to one or more AudioState axes. Per-state volume deltas will be applied to the container and its descendants.")
      .add()
      .afterDecode(mc -> {
         if (mc.audioCategoryId != null) {
            mc.audioCategoryIndex = AudioCategory.getAssetMap().getIndex(mc.audioCategoryId);
         }

         AudioStateResolver.resolveBindings(mc.stateBindings);
      })
      .validator((mc, results) -> AudioStateResolver.validateBindings(mc.stateBindings, "MusicContainer '" + mc.id + "'", results))
      .build();
   public static final int EMPTY_ID = 0;
   public static final MusicContainer EMPTY = new SingleTrackMusicContainer("Empty");
   private static AssetStore<String, MusicContainer, IndexedAssetMap<String, MusicContainer>> ASSET_STORE;
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(MusicContainer::getAssetStore));
   protected AssetExtraInfo.Data data;
   protected String id;
   protected float volume = 1.0F;
   protected int loopCount = 1;
   protected float weight = 1.0F;
   @Nullable
   protected Rangef silenceAfter;
   @Nullable
   protected Rangef exitSilence;
   protected float fadeInDuration;
   protected float fadeOutDuration;
   @Nonnull
   protected MusicTransitionType transitionType = MusicTransitionType.Crossfade;
   protected float transitionDuration;
   protected boolean playToCompletion;
   protected float resumeMemoryDuration = 30.0F;
   @Nullable
   protected String nameTranslationKey;
   @Nullable
   protected String audioCategoryId;
   protected transient int audioCategoryIndex = 0;
   @Nullable
   protected TempoSettings tempo;
   @Nullable
   protected StateBindingConfig[] stateBindings;

   public static AssetStore<String, MusicContainer, IndexedAssetMap<String, MusicContainer>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(MusicContainer.class);
      }

      return ASSET_STORE;
   }

   public static IndexedAssetMap<String, MusicContainer> getAssetMap() {
      return (IndexedAssetMap<String, MusicContainer>)getAssetStore().getAssetMap();
   }

   protected MusicContainer() {
   }

   public MusicContainer(@Nonnull String id) {
      this.id = id;
   }

   public String getId() {
      return this.id;
   }

   public void setId(@Nonnull String id) {
      this.id = id;
   }

   public float getVolume() {
      return this.volume;
   }

   public int getLoopCount() {
      return this.loopCount;
   }

   public void setAudioCategory(@Nullable String audioCategoryId, int audioCategoryIndex) {
      this.audioCategoryId = audioCategoryId;
      this.audioCategoryIndex = audioCategoryIndex;
   }

   @Nonnull
   public String[] getChildIds() {
      return new String[0];
   }

   public void refreshAudioStateResolution() {
      AudioStateResolver.resolveBindings(this.stateBindings);
   }

   public static void onAudioStateLoaded(@Nonnull LoadedAssetsEvent<String, AudioState, IndexedLookupTableAssetMap<String, AudioState>> event) {
      if (!event.isInitial()) {
         refreshAllAudioStateResolutions();
      }
   }

   public static void onAudioStateRemoved(@Nonnull RemovedAssetsEvent<String, AudioState, IndexedLookupTableAssetMap<String, AudioState>> event) {
      refreshAllAudioStateResolutions();
   }

   private static void refreshAllAudioStateResolutions() {
      for (MusicContainer mc : getAssetMap().getAssetMap().values()) {
         if (mc != null) {
            mc.refreshAudioStateResolution();
         }
      }
   }

   protected void fillBasePacketFields(@Nonnull com.hypixel.hytale.protocol.MusicContainer packet) {
      packet.volume = this.volume;
      packet.loopCount = this.loopCount;
      packet.weight = this.weight;
      packet.silenceAfter = this.silenceAfter;
      packet.exitSilence = this.exitSilence;
      packet.fadeInDuration = this.fadeInDuration;
      packet.fadeOutDuration = this.fadeOutDuration;
      packet.transitionType = this.transitionType;
      packet.transitionDuration = this.transitionDuration;
      packet.playToCompletion = this.playToCompletion;
      packet.resumeMemoryDuration = this.resumeMemoryDuration;
      packet.nameTranslationKey = this.nameTranslationKey;
      packet.audioCategoryIndex = this.audioCategoryIndex;
      packet.tempo = this.tempo;
      packet.stateBindings = AudioStateResolver.toPacketArray(this.stateBindings);
   }

   @Nonnull
   @Override
   public String toString() {
      return this.getClass().getSimpleName() + "{id='" + this.id + "'}";
   }

   static {
      CODEC.register("SingleTrack", SingleTrackMusicContainer.class, SingleTrackMusicContainer.CODEC);
      CODEC.register("Random", RandomMusicContainer.class, RandomMusicContainer.CODEC);
      CODEC.register("Sequence", SequenceMusicContainer.class, SequenceMusicContainer.CODEC);
      CODEC.register("Horizontal", HorizontalMusicContainer.class, HorizontalMusicContainer.CODEC);
      CODEC.register("Segment", SegmentMusicContainer.class, SegmentMusicContainer.CODEC);
   }
}
