package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.camera.CameraEffect;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.asset.type.particle.config.WorldParticle;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.asset.type.soundevent.validator.SoundEventValidators;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DamageEffects implements NetworkSerializable<com.hypixel.hytale.protocol.DamageEffects> {
   public static final BuilderCodec<DamageEffects> CODEC = BuilderCodec.builder(DamageEffects.class, DamageEffects::new)
      .appendInherited(
         new KeyedCodec<>("ModelParticles", ModelParticle.ARRAY_CODEC),
         (damageEffects, s) -> damageEffects.modelParticles = s,
         damageEffects -> damageEffects.modelParticles,
         (damageEffects, parent) -> damageEffects.modelParticles = parent.modelParticles
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("WorldParticles", WorldParticle.ARRAY_CODEC),
         (damageEffects, s) -> damageEffects.worldParticles = s,
         damageEffects -> damageEffects.worldParticles,
         (damageEffects, parent) -> damageEffects.worldParticles = parent.worldParticles
      )
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("LocalSoundEventId", Codec.STRING),
         (damageEffects, s) -> damageEffects.localSoundEventId = s,
         damageEffects -> damageEffects.localSoundEventId,
         (damageEffects, parent) -> damageEffects.localSoundEventId = parent.localSoundEventId
      )
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("WorldSoundEventId", Codec.STRING),
         (damageEffects, s) -> damageEffects.worldSoundEventId = s,
         damageEffects -> damageEffects.worldSoundEventId,
         (damageEffects, parent) -> damageEffects.worldSoundEventId = parent.worldSoundEventId
      )
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .addValidator(SoundEventValidators.MONO)
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("PlayerSoundEventId", Codec.STRING),
         (damageEffects, s) -> damageEffects.playerSoundEventId = s,
         damageEffects -> damageEffects.playerSoundEventId,
         (damageEffects, parent) -> damageEffects.playerSoundEventId = parent.playerSoundEventId
      )
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .documentation("The sound to play to a player receiving the damage.")
      .add()
      .appendInherited(
         new KeyedCodec<>("ViewDistance", Codec.DOUBLE),
         (damageEffects, s) -> damageEffects.viewDistance = s,
         damageEffects -> damageEffects.viewDistance,
         (damageEffects, parent) -> damageEffects.viewDistance = parent.viewDistance
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Knockback", Knockback.CODEC),
         (damageEffects, s) -> damageEffects.knockback = s,
         damageEffects -> damageEffects.knockback,
         (damageEffects, parent) -> damageEffects.knockback = parent.knockback
      )
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("CameraEffect", CameraEffect.CHILD_ASSET_CODEC),
         (damageEffects, s) -> damageEffects.cameraEffectId = s,
         damageEffects -> damageEffects.cameraEffectId,
         (damageEffects, parent) -> damageEffects.cameraEffectId = parent.cameraEffectId
      )
      .addValidator(CameraEffect.VALIDATOR_CACHE.getValidator())
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("StaminaDrainMultiplier", Codec.FLOAT),
         (o, i) -> o.staminaDrainMultiplier = i,
         o -> o.staminaDrainMultiplier,
         (o, p) -> o.staminaDrainMultiplier = p.staminaDrainMultiplier
      )
      .documentation("A multiplier to apply to any stamina drain caused by this damage.")
      .add()
      .afterDecode(DamageEffects::processConfig)
      .build();
   protected ModelParticle[] modelParticles;
   protected WorldParticle[] worldParticles;
   @Nullable
   protected String localSoundEventId = null;
   protected transient int localSoundEventIndex;
   @Nullable
   protected String worldSoundEventId = null;
   protected transient int worldSoundEventIndex;
   @Nullable
   protected String playerSoundEventId = null;
   protected transient int playerSoundEventIndex;
   protected double viewDistance = 75.0;
   protected Knockback knockback;
   protected String cameraEffectId;
   protected int cameraEffectIndex = Integer.MIN_VALUE;
   protected float staminaDrainMultiplier = 1.0F;

   public DamageEffects(
      ModelParticle[] modelParticles,
      WorldParticle[] worldParticles,
      String localSoundEventId,
      String worldSoundEventId,
      double viewDistance,
      Knockback knockback
   ) {
      this.modelParticles = modelParticles;
      this.worldParticles = worldParticles;
      this.localSoundEventId = localSoundEventId;
      this.worldSoundEventId = worldSoundEventId;
      this.viewDistance = viewDistance;
      this.knockback = knockback;
      this.processConfig();
   }

   protected DamageEffects() {
   }

   public ModelParticle[] getModelParticles() {
      return this.modelParticles;
   }

   public WorldParticle[] getWorldParticles() {
      return this.worldParticles;
   }

   @Nullable
   public String getWorldSoundEventId() {
      return this.worldSoundEventId;
   }

   public int getWorldSoundEventIndex() {
      return this.worldSoundEventIndex;
   }

   @Nullable
   public String getLocalSoundEventId() {
      return this.localSoundEventId;
   }

   public int getLocalSoundEventIndex() {
      return this.localSoundEventIndex;
   }

   public double getViewDistance() {
      return this.viewDistance;
   }

   public Knockback getKnockback() {
      return this.knockback;
   }

   public String getCameraEffectId() {
      return this.cameraEffectId;
   }

   protected void processConfig() {
      if (this.localSoundEventId != null) {
         this.localSoundEventIndex = SoundEvent.getAssetMap().getIndex(this.localSoundEventId);
      }

      if (this.worldSoundEventId != null) {
         this.worldSoundEventIndex = SoundEvent.getAssetMap().getIndex(this.worldSoundEventId);
      }

      if (this.playerSoundEventId != null) {
         this.playerSoundEventIndex = SoundEvent.getAssetMap().getIndex(this.playerSoundEventId);
      }

      if (this.cameraEffectId != null) {
         this.cameraEffectIndex = CameraEffect.getAssetMap().getIndex(this.cameraEffectId);
      }
   }

   public void addToDamage(@Nonnull Damage damageEvent) {
      if (this.worldSoundEventIndex != 0) {
         damageEvent.putMetaObject(Damage.IMPACT_SOUND_EFFECT, new Damage.SoundEffect(this.worldSoundEventIndex));
      }

      if (this.playerSoundEventIndex != 0) {
         damageEvent.putMetaObject(Damage.PLAYER_IMPACT_SOUND_EFFECT, new Damage.SoundEffect(this.playerSoundEventIndex));
      }

      if (this.worldParticles != null || this.modelParticles != null) {
         damageEvent.putMetaObject(Damage.IMPACT_PARTICLES, new Damage.Particles(this.modelParticles, this.worldParticles, this.viewDistance));
      }

      if (this.cameraEffectId != null) {
         damageEvent.putMetaObject(Damage.CAMERA_EFFECT, new Damage.CameraEffect(this.cameraEffectIndex));
      }

      if (this.staminaDrainMultiplier != 1.0F) {
         damageEvent.putMetaObject(Damage.STAMINA_DRAIN_MULTIPLIER, this.staminaDrainMultiplier);
      }
   }

   public void spawnAtEntity(@Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Ref<EntityStore> ref) {
      TransformComponent transformComponent = commandBuffer.getComponent(ref, TransformComponent.getComponentType());
      if (transformComponent != null) {
         Vector3d position = transformComponent.getPosition();
         if (this.worldParticles != null) {
            SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = commandBuffer.getResource(EntityModule.get().getPlayerSpatialResourceType());
            ObjectList<Ref<EntityStore>> playerRefs = SpatialResource.getThreadLocalReferenceList();
            playerSpatialResource.getSpatialStructure().collect(position, this.viewDistance, playerRefs);
            ParticleUtil.spawnParticleEffects(this.worldParticles, position, null, playerRefs, commandBuffer);
         }

         if (this.worldSoundEventIndex != 0) {
            SoundUtil.playSoundEvent3d(ref, this.worldSoundEventIndex, position, commandBuffer);
         }

         if (this.playerSoundEventIndex != 0) {
            PlayerRef playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
            if (playerRef != null) {
               SoundUtil.playSoundEvent2dToPlayer(playerRef, this.playerSoundEventIndex, SoundCategory.SFX);
            }
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "DamageEffects{modelParticles="
         + Arrays.toString((Object[])this.modelParticles)
         + ", worldParticles="
         + Arrays.toString((Object[])this.worldParticles)
         + ", localSoundEventId='"
         + this.localSoundEventId
         + "', localSoundEventIndex="
         + this.localSoundEventIndex
         + ", worldSoundEventId='"
         + this.worldSoundEventId
         + "', worldSoundEventIndex="
         + this.worldSoundEventIndex
         + ", viewDistance="
         + this.viewDistance
         + ", knockback="
         + this.knockback
         + ", cameraShakeId='"
         + this.cameraEffectId
         + "'}";
   }

   @Nonnull
   public com.hypixel.hytale.protocol.DamageEffects toPacket() {
      com.hypixel.hytale.protocol.ModelParticle[] modelParticlesProtocol = null;
      if (!org.bouncycastle.util.Arrays.isNullOrEmpty(this.modelParticles)) {
         modelParticlesProtocol = new com.hypixel.hytale.protocol.ModelParticle[this.modelParticles.length];

         for (int i = 0; i < this.modelParticles.length; i++) {
            modelParticlesProtocol[i] = this.modelParticles[i].toPacket();
         }
      }

      com.hypixel.hytale.protocol.WorldParticle[] worldParticlesProtocol = null;
      if (!org.bouncycastle.util.Arrays.isNullOrEmpty(this.worldParticles)) {
         worldParticlesProtocol = new com.hypixel.hytale.protocol.WorldParticle[this.worldParticles.length];

         for (int i = 0; i < this.worldParticles.length; i++) {
            worldParticlesProtocol[i] = this.worldParticles[i].toPacket();
         }
      }

      return new com.hypixel.hytale.protocol.DamageEffects(
         modelParticlesProtocol, worldParticlesProtocol, this.localSoundEventIndex != 0 ? this.localSoundEventIndex : this.worldSoundEventIndex
      );
   }
}
