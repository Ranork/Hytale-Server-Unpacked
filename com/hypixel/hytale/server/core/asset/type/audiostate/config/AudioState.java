package com.hypixel.hytale.server.core.asset.type.audiostate.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.AudioStateAuthority;
import com.hypixel.hytale.protocol.StateTransition;
import com.hypixel.hytale.protocol.SyncPoint;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.lang.ref.SoftReference;
import java.util.HashSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AudioState
   implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, AudioState>>,
   NetworkSerializable<com.hypixel.hytale.protocol.AudioState> {
   public static final int EMPTY_ID = 0;
   public static final String EMPTY = "EMPTY";
   public static final AudioState EMPTY_AUDIO_STATE = new AudioState("EMPTY");
   @Nonnull
   public static final AssetBuilderCodec<String, AudioState> CODEC = AssetBuilderCodec.builder(
         AudioState.class, AudioState::new, Codec.STRING, (t, k) -> t.id = k, t -> t.id, (asset, data) -> asset.data = data, asset -> asset.data
      )
      .documentation(
         "A named state axis that any audio object can subscribe to. Server-authority axes are driven by server logic and broadcast to clients. Client-authority axes are driven locally on the client (e.g. by AmbienceFX SetStates)."
      )
      .<AudioStateAuthority>append(new KeyedCodec<>("Authority", AudioStateCodecs.AUTHORITY, true), (a, v) -> a.authority = v, a -> a.authority)
      .documentation("Server: written on the server and broadcast. Client: written locally by AmbienceFX SetStates.")
      .add()
      .<String[]>append(new KeyedCodec<>("Values", new ArrayCodec<>(Codec.STRING, String[]::new), true), (a, v) -> a.values = v, a -> a.values)
      .addValidator(Validators.nonEmptyArray())
      .documentation("Ordered list of state value names on this axis.")
      .add()
      .<String>append(new KeyedCodec<>("DefaultValue", Codec.STRING, true), (a, v) -> a.defaultValue = v, a -> a.defaultValue)
      .documentation("State value name the axis reports as active before any explicit setting.")
      .add()
      .<SyncPoint>append(new KeyedCodec<>("DefaultSyncTo", AudioStateCodecs.SYNC_POINT), (a, v) -> a.defaultSyncTo = v, a -> a.defaultSyncTo)
      .documentation("Default sync point for transitions on this axis. Overridden per-edge or per-call.")
      .add()
      .<StateTransitionConfig>append(
         new KeyedCodec<>("DefaultTransition", StateTransitionConfig.CODEC), (a, v) -> a.defaultTransition = v, a -> a.defaultTransition
      )
      .documentation("Fallback transition used when no edge matches the from/to pair. Omission means no default is provided (transition immediately in 0 ms).")
      .add()
      .<StateTransitionConfig[]>append(
         new KeyedCodec<>("Transitions", new ArrayCodec<>(StateTransitionConfig.CODEC, StateTransitionConfig[]::new)),
         (a, v) -> a.transitions = v,
         a -> a.transitions
      )
      .documentation("Per-edge transition settings keyed by From/To state value names. '*' wildcards supported.")
      .add()
      .<Boolean>append(new KeyedCodec<>("RevertWhenInactive", Codec.BOOLEAN), (a, v) -> a.revertWhenInactive = v, a -> a.revertWhenInactive)
      .documentation(
         "When true, the axis reverts to DefaultValue when every active AmbienceFX that was writing it goes inactive and no new writer claims it. Only meaningful for Authority: Client axes."
      )
      .add()
      .validator(
         (audioState, results) -> {
            if (audioState.values != null) {
               HashSet<String> seen = new HashSet<>();

               for (String name : audioState.values) {
                  if (name != null && !name.isBlank()) {
                     if ("*".equals(name)) {
                        results.fail("AudioState value name '*' is reserved for wildcard matching in transitions");
                     }

                     if (!seen.add(name)) {
                        results.fail("Duplicate AudioState value name '" + name + "'");
                     }
                  } else {
                     results.fail("AudioState value name must be non-empty");
                  }
               }
            }

            if (audioState.defaultValue != null && AudioStateResolver.resolveValueIndex(audioState.defaultValue, audioState.values) < 0) {
               results.fail("DefaultValue '" + audioState.defaultValue + "' is not a declared value");
            }

            if (audioState.transitions != null) {
               for (StateTransitionConfig t : audioState.transitions) {
                  validateTransition(audioState, t, results);
               }
            }

            if (audioState.defaultTransition != null) {
               StateTransitionConfig dt = audioState.defaultTransition;
               if (!Float.isFinite(dt.durationMs) || dt.durationMs < 0.0F) {
                  results.fail("DefaultTransition DurationMs must be finite and >= 0");
               }
            }

            if (audioState.revertWhenInactive && audioState.authority != AudioStateAuthority.Client) {
               results.fail(
                  "RevertWhenInactive requires Authority: Client (got "
                     + audioState.authority
                     + "). Server-authority reverts should be driven by gameplay logic, not this flag"
               );
            }
         }
      )
      .build();
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(AudioState::getAssetStore));
   private static AssetStore<String, AudioState, IndexedLookupTableAssetMap<String, AudioState>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String id;
   @Nonnull
   protected AudioStateAuthority authority = AudioStateAuthority.Server;
   @Nullable
   protected String[] values;
   @Nullable
   protected String defaultValue;
   @Nonnull
   protected SyncPoint defaultSyncTo = SyncPoint.Immediate;
   @Nullable
   protected StateTransitionConfig defaultTransition;
   @Nullable
   protected StateTransitionConfig[] transitions;
   protected boolean revertWhenInactive;
   private SoftReference<com.hypixel.hytale.protocol.AudioState> cachedPacket;

   private static void validateTransition(@Nonnull AudioState asset, @Nonnull StateTransitionConfig t, @Nonnull ValidationResults results) {
      int fromIdx = AudioStateResolver.resolveValueIndex(t.fromValueName, asset.values);
      int toIdx = AudioStateResolver.resolveValueIndex(t.toValueName, asset.values);
      if (fromIdx == -2) {
         results.fail("Transition From '" + t.fromValueName + "' is not a declared value");
      }

      if (toIdx == -2) {
         results.fail("Transition To '" + t.toValueName + "' is not a declared value");
      }

      if (!Float.isFinite(t.durationMs) || t.durationMs < 0.0F) {
         results.fail("Transition DurationMs must be finite and >= 0");
      }
   }

   public static AssetStore<String, AudioState, IndexedLookupTableAssetMap<String, AudioState>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(AudioState.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, AudioState> getAssetMap() {
      return (IndexedLookupTableAssetMap<String, AudioState>)getAssetStore().getAssetMap();
   }

   public AudioState(@Nonnull String id) {
      this.id = id;
   }

   protected AudioState() {
   }

   public String getId() {
      return this.id;
   }

   @Nonnull
   public AudioStateAuthority getAuthority() {
      return this.authority;
   }

   @Nullable
   public String[] getValues() {
      return this.values;
   }

   @Nullable
   public String getDefaultValue() {
      return this.defaultValue;
   }

   public int getDefaultValueIndex() {
      if (this.defaultValue == null) {
         return 0;
      } else {
         int idx = AudioStateResolver.resolveValueIndex(this.defaultValue, this.values);
         return Math.max(idx, 0);
      }
   }

   @Nonnull
   public com.hypixel.hytale.protocol.AudioState toPacket() {
      com.hypixel.hytale.protocol.AudioState cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.AudioState packet = new com.hypixel.hytale.protocol.AudioState();
         packet.id = this.id;
         packet.authority = this.authority;
         packet.values = this.values;
         packet.defaultValueIndex = this.getDefaultValueIndex();
         packet.defaultSyncTo = this.defaultSyncTo;
         packet.defaultTransition = this.defaultTransition != null ? this.resolveAndToPacket(this.defaultTransition) : null;
         packet.revertWhenInactive = this.revertWhenInactive;
         if (this.transitions != null) {
            StateTransition[] protoTransitions = new StateTransition[this.transitions.length];

            for (int i = 0; i < this.transitions.length; i++) {
               protoTransitions[i] = this.resolveAndToPacket(this.transitions[i]);
            }

            packet.transitions = protoTransitions;
         }

         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }

   private StateTransition resolveAndToPacket(@Nonnull StateTransitionConfig t) {
      t.fromValueIndex = AudioStateResolver.resolveValueIndex(t.fromValueName, this.values);
      if (t.fromValueIndex == -2) {
         t.fromValueIndex = -1;
      }

      t.toValueIndex = AudioStateResolver.resolveValueIndex(t.toValueName, this.values);
      if (t.toValueIndex == -2) {
         t.toValueIndex = -1;
      }

      return t.toPacket();
   }

   @Nonnull
   @Override
   public String toString() {
      return "AudioState{id='" + this.id + "', authority=" + this.authority + "}";
   }
}
