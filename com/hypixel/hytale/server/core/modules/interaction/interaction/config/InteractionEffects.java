package com.hypixel.hytale.server.core.modules.interaction.interaction.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.protocol.ModelTrail;
import com.hypixel.hytale.server.core.asset.modifiers.MovementEffects;
import com.hypixel.hytale.server.core.asset.type.camera.CameraEffect;
import com.hypixel.hytale.server.core.asset.type.itemanimation.config.ItemPlayerAnimations;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.asset.type.soundevent.validator.SoundEventValidators;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class InteractionEffects implements NetworkSerializable<com.hypixel.hytale.protocol.InteractionEffects> {
   public static final BuilderCodec<InteractionEffects> CODEC = BuilderCodec.builder(InteractionEffects.class, InteractionEffects::new)
      .appendInherited(
         new KeyedCodec<>("Particles", ModelParticle.ARRAY_CODEC),
         (activationEffects, s) -> activationEffects.particles = s,
         activationEffects -> activationEffects.particles,
         (activationEffects, parent) -> activationEffects.particles = parent.particles
      )
      .documentation("Particles to play when triggering this interaction.")
      .add()
      .<ModelParticle[]>appendInherited(
         new KeyedCodec<>("FirstPersonParticles", ModelParticle.ARRAY_CODEC),
         (activationEffects, s) -> activationEffects.firstPersonParticles = s,
         activationEffects -> activationEffects.firstPersonParticles,
         (activationEffects, parent) -> activationEffects.firstPersonParticles = parent.firstPersonParticles
      )
      .documentation("Particles to play when triggering this interaction while in first person.")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("WorldSoundEventId", Codec.STRING),
         (activationEffects, s) -> activationEffects.worldSoundEventId = s,
         activationEffects -> activationEffects.worldSoundEventId,
         (activationEffects, parent) -> activationEffects.worldSoundEventId = parent.worldSoundEventId
      )
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .addValidator(SoundEventValidators.MONO)
      .documentation("Sound to play when triggering this interaction.")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("LocalSoundEventId", Codec.STRING),
         (activationEffects, s) -> activationEffects.localSoundEventId = s,
         activationEffects -> activationEffects.localSoundEventId,
         (activationEffects, parent) -> activationEffects.localSoundEventId = parent.localSoundEventId
      )
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .documentation("Sound to play when triggering this interaction but only to the player that triggered it.")
      .add()
      .<ModelTrail[]>appendInherited(
         new KeyedCodec<>("Trails", new ArrayCodec<>(ModelAsset.MODEL_TRAIL_CODEC, ModelTrail[]::new)),
         (activationEffects, s) -> activationEffects.trails = s,
         activationEffects -> activationEffects.trails,
         (activationEffects, parent) -> activationEffects.trails = parent.trails
      )
      .documentation("The model trails to create when triggering this interaction")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("WaitForAnimationToFinish", Codec.BOOLEAN),
         (activationEffects, s) -> activationEffects.waitForAnimationToFinish = s,
         activationEffects -> activationEffects.waitForAnimationToFinish,
         (activationEffects, parent) -> activationEffects.waitForAnimationToFinish = parent.waitForAnimationToFinish
      )
      .documentation(
         "Whether this interaction should hold until the animation is finished before continuing.\nGenerally this overrides the runtime of the interaction."
      )
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("ItemPlayerAnimationsId", ItemPlayerAnimations.CHILD_CODEC),
         (o, i) -> o.itemPlayerAnimationsId = i,
         o -> o.itemPlayerAnimationsId,
         (o, p) -> o.itemPlayerAnimationsId = p.itemPlayerAnimationsId
      )
      .addValidator(ItemPlayerAnimations.VALIDATOR_CACHE.getValidator())
      .documentation("The item animations set to use while this interaction is active")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("ItemAnimationId", Codec.STRING),
         (activationEffects, s) -> activationEffects.itemAnimationId = s,
         activationEffects -> activationEffects.itemAnimationId,
         (activationEffects, parent) -> activationEffects.itemAnimationId = parent.itemAnimationId
      )
      .documentation("The item animation to play when triggering this interaction.")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("ClearAnimationOnFinish", Codec.BOOLEAN),
         (activationEffects, s) -> activationEffects.clearAnimationOnFinish = s,
         activationEffects -> activationEffects.clearAnimationOnFinish,
         (activationEffects, parent) -> activationEffects.clearAnimationOnFinish = parent.clearAnimationOnFinish
      )
      .documentation("Whether any animations triggered by this interaction should be cleared when this interaction finishes.")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("ClearSoundEventOnFinish", Codec.BOOLEAN),
         (activationEffects, s) -> activationEffects.clearSoundEventOnFinish = s,
         activationEffects -> activationEffects.clearSoundEventOnFinish,
         (activationEffects, parent) -> activationEffects.clearSoundEventOnFinish = parent.clearSoundEventOnFinish
      )
      .documentation("Whether any sound events triggered by this interaction should be cleared when this interaction finishes.")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("CameraEffect", CameraEffect.CHILD_ASSET_CODEC),
         (activationEffects, s) -> activationEffects.cameraEffectId = s,
         activationEffects -> activationEffects.cameraEffectId,
         (activationEffects, parent) -> activationEffects.cameraEffectId = parent.cameraEffectId
      )
      .addValidator(CameraEffect.VALIDATOR_CACHE.getValidator())
      .documentation("The camera effects to trigger while this interaction is active.")
      .add()
      .<MovementEffects>appendInherited(
         new KeyedCodec<>("MovementEffects", MovementEffects.CODEC),
         (activationEffects, s) -> activationEffects.movementEffects = s,
         activationEffects -> activationEffects.movementEffects,
         (activationEffects, parent) -> activationEffects.movementEffects = parent.movementEffects
      )
      .documentation("The movement effects to apply while this interaction is active.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("StartDelay", Codec.FLOAT),
         (activationEffects, f) -> activationEffects.startDelay = f,
         activationEffects -> activationEffects.startDelay,
         (activationEffects, parent) -> activationEffects.startDelay = parent.startDelay
      )
      .documentation("An optional delay on applying any interaction effects.")
      .add()
      .afterDecode(InteractionEffects::processConfig)
      .build();
   protected ModelParticle[] particles;
   protected ModelParticle[] firstPersonParticles;
   protected String worldSoundEventId;
   protected transient int worldSoundEventIndex = 0;
   protected String localSoundEventId;
   protected transient int localSoundEventIndex = 0;
   protected String onFinishLocalSoundEventId;
   protected transient int onFinishLocalSoundEventIndex = 0;
   protected ModelTrail[] trails;
   protected boolean waitForAnimationToFinish;
   protected String itemPlayerAnimationsId;
   protected String itemAnimationId;
   protected boolean clearAnimationOnFinish;
   protected boolean clearSoundEventOnFinish;
   protected String cameraEffectId;
   protected int cameraEffectIndex = Integer.MIN_VALUE;
   protected MovementEffects movementEffects;
   protected float startDelay = 0.0F;

   protected InteractionEffects() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.InteractionEffects toPacket() {
      com.hypixel.hytale.protocol.InteractionEffects packet = new com.hypixel.hytale.protocol.InteractionEffects();
      if (this.particles != null && this.particles.length > 0) {
         packet.particles = new com.hypixel.hytale.protocol.ModelParticle[this.particles.length];

         for (int i = 0; i < this.particles.length; i++) {
            packet.particles[i] = this.particles[i].toPacket();
         }
      }

      if (this.firstPersonParticles != null && this.firstPersonParticles.length > 0) {
         packet.firstPersonParticles = new com.hypixel.hytale.protocol.ModelParticle[this.firstPersonParticles.length];

         for (int i = 0; i < this.firstPersonParticles.length; i++) {
            packet.firstPersonParticles[i] = this.firstPersonParticles[i].toPacket();
         }
      }

      if (this.cameraEffectIndex != Integer.MIN_VALUE) {
         CameraEffect cameraShakeEffect = CameraEffect.getAssetMap().getAsset(this.cameraEffectIndex);
         if (cameraShakeEffect != null) {
            packet.cameraShake = cameraShakeEffect.createCameraShakePacket();
         }
      }

      packet.worldSoundEventIndex = this.worldSoundEventIndex;
      packet.localSoundEventIndex = this.localSoundEventIndex != 0 ? this.localSoundEventIndex : this.worldSoundEventIndex;
      packet.trails = this.trails;
      packet.waitForAnimationToFinish = this.waitForAnimationToFinish;
      packet.itemPlayerAnimationsId = this.itemPlayerAnimationsId;
      packet.itemAnimationId = this.itemAnimationId;
      packet.clearAnimationOnFinish = this.clearAnimationOnFinish;
      packet.clearSoundEventOnFinish = this.clearSoundEventOnFinish;
      packet.startDelay = this.startDelay;
      if (this.movementEffects != null) {
         packet.movementEffects = this.movementEffects.toPacket();
      }

      return packet;
   }

   public ModelParticle[] getParticles() {
      return this.particles;
   }

   public String getWorldSoundEventId() {
      return this.worldSoundEventId;
   }

   public int getWorldSoundEventIndex() {
      return this.worldSoundEventIndex;
   }

   public String getLocalSoundEventId() {
      return this.localSoundEventId;
   }

   public int getLocalSoundEventIndex() {
      return this.localSoundEventIndex;
   }

   public ModelTrail[] getTrails() {
      return this.trails;
   }

   public boolean isWaitForAnimationToFinish() {
      return this.waitForAnimationToFinish;
   }

   public String getItemPlayerAnimationsId() {
      return this.itemPlayerAnimationsId;
   }

   public String getItemAnimationId() {
      return this.itemAnimationId;
   }

   public boolean isClearAnimationOnFinish() {
      return this.clearAnimationOnFinish;
   }

   public float getStartDelay() {
      return this.startDelay;
   }

   public MovementEffects getMovementEffects() {
      return this.movementEffects;
   }

   protected void processConfig() {
      if (this.worldSoundEventId != null) {
         this.worldSoundEventIndex = SoundEvent.getAssetMap().getIndex(this.worldSoundEventId);
      }

      if (this.localSoundEventId != null) {
         this.localSoundEventIndex = SoundEvent.getAssetMap().getIndex(this.localSoundEventId);
      }

      if (this.cameraEffectId != null) {
         this.cameraEffectIndex = CameraEffect.getAssetMap().getIndex(this.cameraEffectId);
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "InteractionEffects{particles="
         + Arrays.toString((Object[])this.particles)
         + ", firstPersonParticles="
         + Arrays.toString((Object[])this.firstPersonParticles)
         + ", worldSoundEventId='"
         + this.worldSoundEventId
         + "', worldSoundEventIndex="
         + this.worldSoundEventIndex
         + ", localSoundEventId='"
         + this.localSoundEventId
         + "', localSoundEventIndex="
         + this.localSoundEventIndex
         + ", trails="
         + Arrays.toString((Object[])this.trails)
         + ", waitForAnimationToFinish="
         + this.waitForAnimationToFinish
         + ", itemPlayerAnimationsId='"
         + this.itemPlayerAnimationsId
         + "', itemAnimationId='"
         + this.itemAnimationId
         + "', clearAnimationOnFinish="
         + this.clearAnimationOnFinish
         + ", clearSoundEventOnFinish="
         + this.clearSoundEventOnFinish
         + ", cameraShakeEffectId='"
         + this.cameraEffectId
         + "', cameraShakeEffectIndex="
         + this.cameraEffectIndex
         + ", movementEffects="
         + this.movementEffects
         + ", startDelay="
         + this.startDelay
         + "}";
   }
}
