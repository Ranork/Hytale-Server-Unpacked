package com.hypixel.hytale.builtin.deployables.config;

import com.hypixel.hytale.builtin.deployables.DeployablesUtils;
import com.hypixel.hytale.builtin.deployables.component.DeployableComponent;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.asset.type.soundevent.validator.SoundEventValidators;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollisionConfig;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public abstract class DeployableConfig implements NetworkSerializable<com.hypixel.hytale.protocol.DeployableConfig> {
   @Nonnull
   public static final CodecMapCodec<DeployableConfig> CODEC = new CodecMapCodec<>("Type");
   @Nonnull
   public static final BuilderCodec<DeployableConfig> BASE_CODEC = BuilderCodec.abstractBuilder(DeployableConfig.class)
      .appendInherited(new KeyedCodec<>("Id", Codec.STRING), (o, i) -> o.id = i, o -> o.id, (o, p) -> o.id = p.id)
      .documentation("Used to identify this deployable for uses such as MaxLiveCount")
      .add()
      .<Integer>appendInherited(
         new KeyedCodec<>("MaxLiveCount", Codec.INTEGER), (o, i) -> o.maxLiveCount = i, o -> o.maxLiveCount, (o, p) -> o.maxLiveCount = p.maxLiveCount
      )
      .documentation("The maximum amount of this deployable that can be live at once")
      .add()
      .<String>appendInherited(new KeyedCodec<>("Model", Codec.STRING), (o, i) -> o.model = i, o -> o.model, (o, p) -> o.model = p.model)
      .addValidator(Validators.nonNull())
      .addValidator(ModelAsset.VALIDATOR_CACHE.getValidator())
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("ModelPreview", Codec.STRING), (o, i) -> o.modelPreview = i, o -> o.modelPreview, (o, p) -> o.modelPreview = p.modelPreview
      )
      .addValidator(ModelAsset.VALIDATOR_CACHE.getValidator())
      .add()
      .appendInherited(new KeyedCodec<>("ModelScale", Codec.FLOAT), (o, i) -> o.modelScale = i, o -> o.modelScale, (o, p) -> o.modelScale = p.modelScale)
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("LiveDuration", Codec.FLOAT), (o, i) -> o.liveDuration = i, o -> o.liveDuration, (o, p) -> o.liveDuration = p.liveDuration
      )
      .documentation("The duration of the lifetime of the deployable in seconds")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("Invulnerable", Codec.BOOLEAN), (o, i) -> o.invulnerable = i, o -> o.invulnerable, (o, p) -> o.invulnerable = p.invulnerable
      )
      .documentation("Whether this deployable is invulnerable to damage or not")
      .add()
      .<Map>appendInherited(
         new KeyedCodec<>("Stats", new MapCodec<>(DeployableConfig.StatConfig.CODEC, Object2ObjectOpenHashMap::new)),
         (o, i) -> o.statValues = i,
         o -> o.statValues,
         (i, o) -> i.statValues = o.statValues
      )
      .documentation("The default stat configuration for the deployable")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("DeploySoundEventId", Codec.STRING),
         (o, i) -> o.deploySoundEventId = i,
         o -> o.deploySoundEventId,
         (i, o) -> i.deploySoundEventId = o.deploySoundEventId
      )
      .documentation("The ID of the sound to play upon deployment (at deployment location)")
      .addValidator(SoundEventValidators.ONESHOT)
      .addValidator(SoundEventValidators.MONO)
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("DespawnSoundEventId", Codec.STRING),
         (o, i) -> o.despawnSoundEventId = i,
         o -> o.despawnSoundEventId,
         (i, o) -> i.despawnSoundEventId = o.despawnSoundEventId
      )
      .documentation("The ID of the sound to play when despawning")
      .addValidator(SoundEventValidators.ONESHOT)
      .addValidator(SoundEventValidators.MONO)
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("DieSoundEventId", Codec.STRING),
         (o, i) -> o.dieSoundEventId = i,
         o -> o.dieSoundEventId,
         (i, o) -> i.dieSoundEventId = o.dieSoundEventId
      )
      .documentation("The ID of the sound to play when despawning due to death")
      .addValidator(SoundEventValidators.ONESHOT)
      .addValidator(SoundEventValidators.MONO)
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("AmbientSoundEventId", Codec.STRING),
         (o, i) -> o.ambientSoundEventId = i,
         o -> o.ambientSoundEventId,
         (i, o) -> i.ambientSoundEventId = o.ambientSoundEventId
      )
      .documentation("The ID of the sound to play ambiently from the deployable while it's in the world")
      .addValidator(SoundEventValidators.LOOPING)
      .addValidator(SoundEventValidators.MONO)
      .add()
      .<ModelParticle[]>appendInherited(
         new KeyedCodec<>("SpawnParticles", ModelParticle.ARRAY_CODEC),
         (o, i) -> o.spawnParticles = i,
         o -> o.spawnParticles,
         (i, o) -> i.spawnParticles = o.spawnParticles
      )
      .documentation("A collection of model particles to play when this deployable is spawned.")
      .add()
      .<ModelParticle[]>appendInherited(
         new KeyedCodec<>("DespawnParticles", ModelParticle.ARRAY_CODEC),
         (o, i) -> o.despawnParticles = i,
         o -> o.despawnParticles,
         (i, o) -> i.despawnParticles = o.despawnParticles
      )
      .documentation("A collection of model particles to play when this deployable is despawned.")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("DebugVisuals", Codec.BOOLEAN), (o, i) -> o.debugVisuals = i, o -> o.debugVisuals, (i, o) -> i.debugVisuals = o.debugVisuals
      )
      .documentation("Whether or not to display debug visuals.")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("AllowPlaceOnWalls", Codec.BOOLEAN),
         (o, i) -> o.allowPlaceOnWalls = i,
         o -> o.allowPlaceOnWalls,
         (i, o) -> i.allowPlaceOnWalls = o.allowPlaceOnWalls
      )
      .documentation("Whether or not this deployable can be placed on walls.")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("WireframeDebugVisuals", Codec.BOOLEAN),
         (o, i) -> o.wireframeDebugVisuals = i,
         o -> o.wireframeDebugVisuals,
         (i, o) -> i.wireframeDebugVisuals = o.wireframeDebugVisuals
      )
      .documentation("Whether debug visuals will be wireframe or have color.")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("HitboxCollisionConfig", Codec.STRING),
         (playerConfig, s) -> playerConfig.hitboxCollisionConfigId = s,
         playerConfig -> playerConfig.hitboxCollisionConfigId,
         (playerConfig, parent) -> playerConfig.hitboxCollisionConfigId = parent.hitboxCollisionConfigId
      )
      .documentation("The HitboxCollision config to apply to the deployable.")
      .addValidator(HitboxCollisionConfig.VALIDATOR_CACHE.getValidator())
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("CountTowardsGlobalLimit", Codec.BOOLEAN),
         (o, i) -> o.countTowardsGlobalLimit = i,
         o -> o.countTowardsGlobalLimit,
         (i, o) -> i.countTowardsGlobalLimit = o.countTowardsGlobalLimit
      )
      .documentation("Whether or not this deployable counts towards global deployable limit")
      .add()
      .afterDecode(DeployableConfig::processConfig)
      .build();
   protected Map<String, DeployableConfig.StatConfig> statValues;
   protected String deploySoundEventId;
   protected String despawnSoundEventId;
   protected String dieSoundEventId;
   protected String ambientSoundEventId;
   protected ModelParticle[] spawnParticles;
   protected ModelParticle[] despawnParticles;
   protected transient int deploySoundEventIndex = 0;
   protected transient int despawnSoundEventIndex = 0;
   protected transient int dieSoundEventIndex = 0;
   protected transient int ambientSoundEventIndex = 0;
   protected Model generatedModel;
   protected Model generatedModelPreview;
   protected String hitboxCollisionConfigId;
   protected int hitboxCollisionConfigIndex = -1;
   private String id;
   private int maxLiveCount = Integer.MAX_VALUE;
   private String model;
   private String modelPreview;
   private float modelScale = 1.0F;
   private float liveDuration = 1.0F;
   private boolean invulnerable;
   private boolean debugVisuals;
   private boolean allowPlaceOnWalls;
   private boolean wireframeDebugVisuals;
   private boolean countTowardsGlobalLimit = true;

   protected DeployableConfig() {
   }

   private static void processConfig(@Nonnull DeployableConfig config) {
      if (config.deploySoundEventId != null) {
         config.deploySoundEventIndex = SoundEvent.getAssetMap().getIndex(config.deploySoundEventId);
      }

      if (config.despawnSoundEventId != null) {
         config.despawnSoundEventIndex = SoundEvent.getAssetMap().getIndex(config.despawnSoundEventId);
      }

      if (config.dieSoundEventId != null) {
         config.dieSoundEventIndex = SoundEvent.getAssetMap().getIndex(config.dieSoundEventId);
      }

      if (config.ambientSoundEventId != null) {
         config.ambientSoundEventIndex = SoundEvent.getAssetMap().getIndex(config.ambientSoundEventId);
      }

      if (config.generatedModel != null) {
         config.generatedModel = Model.createScaledModel(ModelAsset.getAssetMap().getAsset(config.model), config.modelScale);
      }

      if (config.generatedModelPreview != null) {
         config.generatedModelPreview = Model.createScaledModel(ModelAsset.getAssetMap().getAsset(config.modelPreview), config.modelScale);
      }

      if (config.hitboxCollisionConfigId != null) {
         config.hitboxCollisionConfigIndex = HitboxCollisionConfig.getAssetMap().getIndexOrDefault(config.hitboxCollisionConfigId, -1);
      }
   }

   protected static void playAnimation(
      @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull DeployableConfig config, @Nonnull String animationSetKey
   ) {
      NetworkId networkIdComponent = store.getComponent(ref, NetworkId.getComponentType());
      if (networkIdComponent != null) {
         DeployablesUtils.playAnimation(store, networkIdComponent.getId(), ref, config, AnimationSlot.Action, null, animationSetKey);
      }
   }

   protected static void stopAnimation(@Nonnull Store<EntityStore> store, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, int index) {
      Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
      if (ref.isValid()) {
         NetworkId networkIdComponent = archetypeChunk.getComponent(index, NetworkId.getComponentType());
         if (networkIdComponent != null) {
            DeployablesUtils.stopAnimation(store, networkIdComponent.getId(), ref, AnimationSlot.Action);
         }
      }
   }

   public Model getModel() {
      if (this.generatedModel != null) {
         return this.generatedModel;
      } else {
         this.generatedModel = Model.createScaledModel(ModelAsset.getAssetMap().getAsset(this.model), this.modelScale);
         return this.generatedModel;
      }
   }

   public Model getModelPreview() {
      if (this.modelPreview == null) {
         return null;
      } else if (this.generatedModelPreview != null) {
         return this.generatedModelPreview;
      } else {
         this.generatedModelPreview = Model.createScaledModel(ModelAsset.getAssetMap().getAsset(this.modelPreview), this.modelScale);
         return this.generatedModelPreview;
      }
   }

   public int getHitboxCollisionConfigIndex() {
      return this.hitboxCollisionConfigIndex;
   }

   public long getLiveDurationInMillis() {
      return (long)(this.liveDuration * 1000.0F);
   }

   public float getLiveDuration() {
      return this.liveDuration;
   }

   public String getId() {
      return this.id;
   }

   public int getMaxLiveCount() {
      return this.maxLiveCount;
   }

   public boolean getInvulnerable() {
      return this.invulnerable;
   }

   public Map<String, DeployableConfig.StatConfig> getStatValues() {
      return this.statValues;
   }

   public int getDespawnSoundEventIndex() {
      return this.despawnSoundEventIndex;
   }

   public int getDeploySoundEventIndex() {
      return this.deploySoundEventIndex;
   }

   public int getDieSoundEventIndex() {
      return this.dieSoundEventIndex;
   }

   public int getAmbientSoundEventIndex() {
      return this.ambientSoundEventIndex;
   }

   public ModelParticle[] getSpawnParticles() {
      return this.spawnParticles;
   }

   public ModelParticle[] getDespawnParticles() {
      return this.despawnParticles;
   }

   public boolean getDebugVisuals() {
      return this.debugVisuals;
   }

   public boolean getAllowPlaceOnWalls() {
      return this.allowPlaceOnWalls;
   }

   public boolean getWireframeDebugVisuals() {
      return this.wireframeDebugVisuals;
   }

   public boolean getCountTowardsGlobalLimit() {
      return this.countTowardsGlobalLimit;
   }

   public void tick(
      @Nonnull DeployableComponent deployableComponent,
      float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
   }

   public void firstTick(
      @Nonnull DeployableComponent deployableComponent,
      float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.DeployableConfig toPacket() {
      com.hypixel.hytale.protocol.DeployableConfig config = new com.hypixel.hytale.protocol.DeployableConfig();
      config.model = this.getModel().toPacket();
      if (this.modelPreview != null) {
         config.modelPreview = this.getModelPreview().toPacket();
      }

      config.allowPlaceOnWalls = this.allowPlaceOnWalls;
      return config;
   }

   @Override
   public String toString() {
      return "DeployableConfig{}";
   }

   public static class StatConfig {
      @Nonnull
      private static final BuilderCodec<DeployableConfig.StatConfig> CODEC = BuilderCodec.builder(
            DeployableConfig.StatConfig.class, DeployableConfig.StatConfig::new
         )
         .documentation("Initial and maximum values for a stat.")
         .<Float>append(new KeyedCodec<>("Max", Codec.FLOAT), (config, f) -> config.max = f, config -> config.max)
         .addValidator(Validators.nonNull())
         .addValidator(Validators.greaterThan(0.0F))
         .documentation("The maximum value for the stat.")
         .add()
         .<Float>append(new KeyedCodec<>("Initial", Codec.FLOAT), (config, f) -> config.initial = f, config -> config.initial)
         .documentation("The initial value for the stat. If omitted, will be set to max.")
         .add()
         .build();
      private float max;
      private float initial = Float.MAX_VALUE;

      private StatConfig() {
      }

      public float getMax() {
         return this.max;
      }

      public float getInitial() {
         return this.initial;
      }
   }
}
