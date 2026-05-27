package com.hypixel.hytale.server.core.asset.type.audiostate.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDisplayMode;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.FadeCurve;
import com.hypixel.hytale.protocol.StateTransition;
import com.hypixel.hytale.protocol.SyncPoint;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StateTransitionConfig {
   public static final String WILDCARD = "*";
   public static final int WILDCARD_INDEX = -1;
   @Nonnull
   public static final BuilderCodec<StateTransitionConfig> CODEC = BuilderCodec.builder(StateTransitionConfig.class, StateTransitionConfig::new)
      .metadata(UIDisplayMode.COMPACT)
      .<String>append(new KeyedCodec<>("From", Codec.STRING), (t, s) -> t.fromValueName = s, t -> t.fromValueName)
      .documentation("State value name this edge transitions from. '*' for wildcard. Unused when used as an override.")
      .add()
      .<String>append(new KeyedCodec<>("To", Codec.STRING), (t, s) -> t.toValueName = s, t -> t.toValueName)
      .documentation("State value name this edge transitions to. '*' for wildcard. Unused when used as an override.")
      .add()
      .<Float>append(new KeyedCodec<>("DurationMs", Codec.FLOAT), (t, f) -> t.durationMs = f, t -> t.durationMs)
      .addValidator(Validators.min(0.0F))
      .documentation("Transition duration in milliseconds.")
      .add()
      .<FadeCurve>append(new KeyedCodec<>("Curve", AudioStateCodecs.FADE_CURVE), (t, c) -> t.curve = c, t -> t.curve)
      .documentation("Interpolation curve shape. Linear if omitted.")
      .add()
      .<SyncPoint>append(new KeyedCodec<>("SyncTo", AudioStateCodecs.SYNC_POINT), (t, s) -> t.syncTo = s, t -> t.syncTo)
      .documentation("Optional per-edge sync override. Axis DefaultSyncTo is used if omitted.")
      .add()
      .build();
   @Nullable
   protected String fromValueName;
   @Nullable
   protected String toValueName;
   protected float durationMs;
   @Nonnull
   protected FadeCurve curve = FadeCurve.Linear;
   @Nullable
   protected SyncPoint syncTo;
   transient int fromValueIndex = -1;
   transient int toValueIndex = -1;

   public StateTransition toPacket() {
      StateTransition packet = new StateTransition();
      packet.fromValueIndex = this.fromValueIndex;
      packet.toValueIndex = this.toValueIndex;
      packet.durationMs = this.durationMs;
      packet.curve = this.curve;
      packet.syncTo = this.syncTo;
      return packet;
   }
}
