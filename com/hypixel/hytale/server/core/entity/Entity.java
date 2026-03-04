package com.hypixel.hytale.server.core.entity;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.DirectDecodeCodec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.NonSerialized;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.entity.EntityRemoveEvent;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;
import org.bson.BsonString;

public abstract class Entity implements Component<EntityStore> {
   @Nonnull
   public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   public static final int VERSION = 5;
   @Nonnull
   public static final KeyedCodec<Model.ModelReference> MODEL = new KeyedCodec<>("Model", Model.ModelReference.CODEC);
   @Nonnull
   public static final KeyedCodec<String> DISPLAY_NAME = new KeyedCodec<>("DisplayName", Codec.STRING);
   @Nonnull
   public static final KeyedCodec<UUID> UUID = new KeyedCodec<>("UUID", Codec.UUID_BINARY);
   @Nonnull
   public static final BuilderCodec<Entity> CODEC = BuilderCodec.abstractBuilder(Entity.class)
      .legacyVersioned()
      .codecVersion(5)
      .append(DISPLAY_NAME, (entity, o) -> entity.legacyDisplayName = o, entity -> entity.legacyDisplayName)
      .add()
      .append(UUID, (entity, o) -> entity.legacyUuid = o, entity -> entity.legacyUuid)
      .add()
      .build();
   public static final int UNASSIGNED_ID = -1;
   protected int networkId = -1;
   @Nullable
   protected UUID legacyUuid;
   @Nullable
   protected World world;
   @Nullable
   protected Ref<EntityStore> reference;
   @Deprecated
   private TransformComponent transformComponent;
   @Deprecated(forRemoval = true)
   protected String legacyDisplayName;
   @Nonnull
   protected final AtomicBoolean wasRemoved = new AtomicBoolean();
   @Nullable
   protected Throwable removedBy;

   @Deprecated
   public Entity(@Nullable World world) {
      this();
      this.networkId = world != null ? world.getEntityStore().takeNextNetworkId() : -1;
      this.world = world;
   }

   public Entity() {
   }

   @Deprecated(forRemoval = true)
   public void markNeedsSave() {
      if (this.transformComponent != null) {
         WorldChunk chunk = this.transformComponent.getChunk();
         if (chunk != null) {
            chunk.getEntityChunk().markNeedsSaving();
         }
      }
   }

   public void setLegacyUUID(@Nullable UUID uuid) {
      this.legacyUuid = uuid;
   }

   public boolean remove() {
      this.world.debugAssertInTickingThread();
      if (this.wasRemoved.getAndSet(true)) {
         return false;
      } else {
         this.removedBy = new Throwable();

         try {
            String key = this.world != null ? this.world.getName() : null;
            IEventDispatcher<EntityRemoveEvent, EntityRemoveEvent> dispatcher = HytaleServer.get().getEventBus().dispatchFor(EntityRemoveEvent.class, key);
            if (dispatcher.hasListener()) {
               dispatcher.dispatch(new EntityRemoveEvent(this));
            }

            if (this.reference.isValid()) {
               this.world.getEntityStore().getStore().removeEntity(this.reference, RemoveReason.REMOVE);
            }
         } catch (Throwable var3) {
            this.wasRemoved.set(false);
         }

         return true;
      }
   }

   public void loadIntoWorld(@Nonnull World world) {
      if (this.world != null) {
         throw new IllegalArgumentException("Entity is already in a world! " + this);
      } else {
         this.world = world;
         if (this.networkId == -1) {
            this.networkId = world.getEntityStore().takeNextNetworkId();
         }
      }
   }

   public void unloadFromWorld() {
      if (this.world == null) {
         throw new IllegalArgumentException("Entity is already not in a world! " + this);
      } else {
         this.networkId = -1;
         this.world = null;
      }
   }

   @Deprecated(forRemoval = true)
   public int getNetworkId() {
      return this.networkId;
   }

   @Deprecated(forRemoval = true)
   public String getLegacyDisplayName() {
      return this.legacyDisplayName;
   }

   @Nullable
   @Deprecated(forRemoval = true)
   public UUID getUuid() {
      return this.legacyUuid;
   }

   @Deprecated(forRemoval = true)
   public void setTransformComponent(TransformComponent transform) {
      this.transformComponent = transform;
   }

   @Deprecated(forRemoval = true)
   public TransformComponent getTransformComponent() {
      if (this.world == null || this.reference == null) {
         throw new IllegalStateException("Called before entity was init");
      } else if (!this.world.isInThread()) {
         ((HytaleLogger.Api)((HytaleLogger.Api)LOGGER.at(Level.WARNING).atMostEvery(5, TimeUnit.MINUTES)).withCause(new Throwable()))
            .log("getPositionComponent called async");
         return this.transformComponent;
      } else {
         Store<EntityStore> store = this.world.getEntityStore().getStore();
         TransformComponent transformComponent = store.getComponent(this.reference, TransformComponent.getComponentType());

         assert transformComponent != null;

         return transformComponent;
      }
   }

   @Deprecated
   public void moveTo(@Nonnull Ref<EntityStore> ref, double locX, double locY, double locZ, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      transformComponent.getPosition().assign(locX, locY, locZ);
   }

   @Nullable
   public World getWorld() {
      return this.world;
   }

   public boolean wasRemoved() {
      return this.wasRemoved.get();
   }

   public boolean isCollidable() {
      return true;
   }

   @Override
   public int hashCode() {
      int result = this.networkId;
      return 31 * result + (this.world != null ? this.world.hashCode() : 0);
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Entity entity = (Entity)o;
         if (this.networkId != entity.networkId) {
            return false;
         } else {
            return this.world != null ? this.world.equals(entity.world) : entity.world == null;
         }
      } else {
         return false;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "Entity{id="
         + this.networkId
         + ", uuid="
         + this.legacyUuid
         + ", reference='"
         + this.reference
         + "', world="
         + (this.world != null ? this.world.getName() : null)
         + ", displayName='"
         + this.legacyDisplayName
         + "', wasRemoved='"
         + this.wasRemoved
         + "', removedBy='"
         + (this.removedBy != null ? this.removedBy + "\n" + Arrays.toString((Object[])this.removedBy.getStackTrace()) : null)
         + "'}";
   }

   public boolean isHiddenFromLivingEntity(
      @Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      return false;
   }

   public void setReference(@Nonnull Ref<EntityStore> reference) {
      if (this.reference != null && this.reference.isValid()) {
         throw new IllegalArgumentException("Entity already has a valid EntityReference: " + this.reference + " new reference " + reference);
      } else {
         this.reference = reference;
      }
   }

   @Nullable
   public Ref<EntityStore> getReference() {
      return this.reference;
   }

   @Deprecated
   public void clearReference() {
      this.reference = null;
   }

   @Override
   public Component<EntityStore> clone() {
      DirectDecodeCodec<Entity> codec = EntityModule.get().getCodec((Class<Entity>)this.getClass());
      Function<World, Entity> constructor = EntityModule.get().getConstructor((Class<Entity>)this.getClass());
      BsonDocument document = codec.encode(this, ExtraInfo.THREAD_LOCAL.get()).asDocument();
      document.put("EntityType", new BsonString(EntityModule.get().getIdentifier((Class<? extends Entity>)this.getClass())));
      Entity t = constructor.apply(null);
      codec.decode(document, t, ExtraInfo.THREAD_LOCAL.get());
      return t;
   }

   public Holder<EntityStore> toHolder() {
      if (this.reference != null && this.reference.isValid() && this.world != null) {
         if (!this.world.isInThread()) {
            return CompletableFuture.supplyAsync(this::toHolder, this.world).join();
         } else {
            Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
            Store<EntityStore> componentStore = this.world.getEntityStore().getStore();
            Archetype<EntityStore> archetype = componentStore.getArchetype(this.reference);

            for (int i = archetype.getMinIndex(); i < archetype.length(); i++) {
               ComponentType componentType = archetype.get(i);
               if (componentType != null) {
                  Component component = componentStore.getComponent(this.reference, componentType);

                  assert component != null;

                  holder.addComponent(componentType, component);
               }
            }

            return holder;
         }
      } else {
         Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
         if (this instanceof Player) {
            holder.addComponent(Player.getComponentType(), (Player)this);
         } else {
            ComponentType<EntityStore, ? extends Entity> componentType = EntityModule.get().getComponentType((Class<? extends Entity>)this.getClass());
            holder.addComponent(componentType, this);
         }

         DirectDecodeCodec<? extends Entity> codec = EntityModule.get().getCodec((Class<? extends Entity>)this.getClass());
         if (codec == null) {
            holder.addComponent(EntityStore.REGISTRY.getNonSerializedComponentType(), NonSerialized.get());
         }

         return holder;
      }
   }

   public static class DefaultAnimations {
      @Nonnull
      public static final String DEATH = "Death";
      @Nonnull
      public static final String HURT = "Hurt";
      @Nonnull
      public static final String DESPAWN = "Despawn";
      @Nonnull
      public static final String SWIM_SUFFIX = "Swim";
      @Nonnull
      public static final String FLY_SUFFIX = "Fly";

      @Nonnull
      public static String[] getHurtAnimationIds(@Nonnull MovementStates movementStates, @Nonnull DamageCause damageCause) {
         String animationId = damageCause.getAnimationId();
         if (movementStates.swimming) {
            return new String[]{animationId + "Swim", animationId, "Hurt"};
         } else {
            return movementStates.flying ? new String[]{animationId + "Fly", animationId, "Hurt"} : new String[]{animationId, "Hurt"};
         }
      }

      @Nonnull
      public static String[] getDeathAnimationIds(@Nonnull MovementStates movementStates, @Nonnull DamageCause damageCause) {
         String animationId = damageCause.getDeathAnimationId();
         if (movementStates.swimming) {
            return new String[]{animationId + "Swim", animationId, "Death"};
         } else {
            return movementStates.flying ? new String[]{animationId + "Fly", animationId, "Death"} : new String[]{animationId, "Death"};
         }
      }
   }
}
