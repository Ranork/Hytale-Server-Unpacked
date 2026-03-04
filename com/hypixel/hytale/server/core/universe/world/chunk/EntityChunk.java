package com.hypixel.hytale.server.core.universe.world.chunk;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.store.StoredCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.NonTicking;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.LegacyModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityChunk implements Component<ChunkStore> {
   @Nonnull
   public static final BuilderCodec<EntityChunk> CODEC = BuilderCodec.builder(EntityChunk.class, EntityChunk::new)
      .addField(new KeyedCodec<>("Entities", new ArrayCodec<>(new StoredCodec<>(EntityStore.HOLDER_CODEC_KEY), Holder[]::new)), (entityChunk, array) -> {
         entityChunk.entityHolders.clear();
         Collections.addAll(entityChunk.entityHolders, array);
      }, entityChunk -> {
         if (entityChunk.entityReferences.isEmpty()) {
            return entityChunk.entityHolders.toArray(new Holder[entityChunk.entityHolders.size()]);
         } else {
            Holder<EntityStore>[] array = new Holder[entityChunk.entityHolders.size() + entityChunk.entityReferences.size()];
            array = entityChunk.entityHolders.toArray(array);
            int index = entityChunk.entityHolders.size();

            for (Ref<EntityStore> reference : entityChunk.entityReferences) {
               Store<EntityStore> store = reference.getStore();
               if (store.getArchetype(reference).hasSerializableComponents(store.getRegistry().getData())) {
                  array[index++] = store.copyEntity(reference);
               }
            }

            return index == array.length ? array : Arrays.copyOfRange(array, 0, index);
         }
      })
      .build();
   @Nonnull
   private final List<Holder<EntityStore>> entityHolders;
   @Nonnull
   private final Set<Ref<EntityStore>> entityReferences;
   @Nonnull
   private final List<Holder<EntityStore>> entityHoldersUnmodifiable;
   @Nonnull
   private final Set<Ref<EntityStore>> entityReferencesUnmodifiable;
   private boolean needsSaving;

   @Nonnull
   public static ComponentType<ChunkStore, EntityChunk> getComponentType() {
      return LegacyModule.get().getEntityChunkComponentType();
   }

   public EntityChunk() {
      this.entityHolders = new ObjectArrayList();
      this.entityReferences = new HashSet<>();
      this.entityHoldersUnmodifiable = Collections.unmodifiableList(this.entityHolders);
      this.entityReferencesUnmodifiable = Collections.unmodifiableSet(this.entityReferences);
   }

   public EntityChunk(@Nonnull List<Holder<EntityStore>> entityHolders, @Nonnull Set<Ref<EntityStore>> entityReferences) {
      this.entityHolders = entityHolders;
      this.entityReferences = entityReferences;
      this.entityHoldersUnmodifiable = Collections.unmodifiableList(entityHolders);
      this.entityReferencesUnmodifiable = Collections.unmodifiableSet(entityReferences);
   }

   @Nonnull
   @Override
   public Component<ChunkStore> clone() {
      ObjectArrayList<Holder<EntityStore>> entityHoldersClone = new ObjectArrayList(this.entityHolders.size() + this.entityReferences.size());

      for (Holder<EntityStore> entityHolder : this.entityHolders) {
         entityHoldersClone.add(entityHolder.clone());
      }

      for (Ref<EntityStore> reference : this.entityReferences) {
         entityHoldersClone.add(reference.getStore().copyEntity(reference));
      }

      return new EntityChunk(entityHoldersClone, new HashSet<>());
   }

   @Nonnull
   @Override
   public Component<ChunkStore> cloneSerializable() {
      ComponentRegistry.Data<EntityStore> data = EntityStore.REGISTRY.getData();
      ObjectArrayList<Holder<EntityStore>> entityHoldersClone = new ObjectArrayList(this.entityHolders.size() + this.entityReferences.size());

      for (Holder<EntityStore> entityHolder : this.entityHolders) {
         if (entityHolder.getArchetype().hasSerializableComponents(data)) {
            entityHoldersClone.add(entityHolder.cloneSerializable(data));
         }
      }

      for (Ref<EntityStore> reference : this.entityReferences) {
         Store<EntityStore> store = reference.getStore();
         if (store.getArchetype(reference).hasSerializableComponents(data)) {
            entityHoldersClone.add(store.copySerializableEntity(reference));
         }
      }

      return new EntityChunk(entityHoldersClone, new HashSet<>());
   }

   @Nonnull
   public List<Holder<EntityStore>> getEntityHolders() {
      return this.entityHoldersUnmodifiable;
   }

   public void addEntityHolder(@Nonnull Holder<EntityStore> holder) {
      this.entityHolders.add(Objects.requireNonNull(holder));
      this.markNeedsSaving();
   }

   public void storeEntityHolder(@Nonnull Holder<EntityStore> holder) {
      this.entityHolders.add(Objects.requireNonNull(holder));
   }

   @Nonnull
   public Set<Ref<EntityStore>> getEntityReferences() {
      return this.entityReferencesUnmodifiable;
   }

   public void addEntityReference(@Nonnull Ref<EntityStore> reference) {
      this.entityReferences.add(Objects.requireNonNull(reference));
      this.markNeedsSaving();
   }

   public void loadEntityReference(@Nonnull Ref<EntityStore> reference) {
      this.entityReferences.add(Objects.requireNonNull(reference));
   }

   public void removeEntityReference(@Nonnull Ref<EntityStore> reference) {
      this.entityReferences.remove(Objects.requireNonNull(reference));
      this.markNeedsSaving();
   }

   public void unloadEntityReference(@Nonnull Ref<EntityStore> reference) {
      this.entityReferences.remove(Objects.requireNonNull(reference));
   }

   @Nullable
   public Holder<EntityStore>[] takeEntityHolders() {
      if (this.entityHolders.isEmpty()) {
         return null;
      } else {
         Holder<EntityStore>[] holders = this.entityHolders.toArray(Holder[]::new);
         this.entityHolders.clear();
         return holders;
      }
   }

   @Nullable
   public Ref<EntityStore>[] takeEntityReferences() {
      if (this.entityReferences.isEmpty()) {
         return null;
      } else {
         Ref<EntityStore>[] holders = this.entityReferences.toArray(Ref[]::new);
         this.entityReferences.clear();
         return holders;
      }
   }

   public boolean getNeedsSaving() {
      return this.needsSaving;
   }

   public void markNeedsSaving() {
      this.needsSaving = true;
   }

   public boolean consumeNeedsSaving() {
      boolean out = this.needsSaving;
      this.needsSaving = false;
      return out;
   }

   public static class EntityChunkLoadingSystem extends RefChangeSystem<ChunkStore, NonTicking<ChunkStore>> {
      @Nonnull
      private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
      @Nonnull
      private final Archetype<ChunkStore> archetype = Archetype.of(WorldChunk.getComponentType(), EntityChunk.getComponentType());

      @Override
      public Query<ChunkStore> getQuery() {
         return this.archetype;
      }

      @Nonnull
      @Override
      public ComponentType<ChunkStore, NonTicking<ChunkStore>> componentType() {
         return ChunkStore.REGISTRY.getNonTickingComponentType();
      }

      public void onComponentAdded(
         @Nonnull Ref<ChunkStore> ref,
         @Nonnull NonTicking<ChunkStore> component,
         @Nonnull Store<ChunkStore> store,
         @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         World world = store.getExternalData().getWorld();
         EntityChunk entityChunkComponent = store.getComponent(ref, EntityChunk.getComponentType());

         assert entityChunkComponent != null;

         Ref<EntityStore>[] references = entityChunkComponent.takeEntityReferences();
         if (references != null) {
            Store<EntityStore> entityStore = world.getEntityStore().getStore();
            Holder<EntityStore>[] holders = entityStore.removeEntities(references, RemoveReason.UNLOAD);
            ComponentRegistry.Data<EntityStore> data = EntityStore.REGISTRY.getData();

            for (int i = 0; i < holders.length; i++) {
               Holder<EntityStore> holder = holders[i];
               if (holder.hasSerializableComponents(data)) {
                  entityChunkComponent.storeEntityHolder(holder);
               }
            }
         }
      }

      public void onComponentSet(
         @Nonnull Ref<ChunkStore> ref,
         NonTicking<ChunkStore> oldComponent,
         @Nonnull NonTicking<ChunkStore> newComponent,
         @Nonnull Store<ChunkStore> store,
         @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
      }

      public void onComponentRemoved(
         @Nonnull Ref<ChunkStore> ref,
         @Nonnull NonTicking<ChunkStore> component,
         @Nonnull Store<ChunkStore> store,
         @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         World world = store.getExternalData().getWorld();
         WorldChunk worldChunkComponent = store.getComponent(ref, WorldChunk.getComponentType());

         assert worldChunkComponent != null;

         EntityChunk entityChunkComponent = store.getComponent(ref, EntityChunk.getComponentType());

         assert entityChunkComponent != null;

         Store<EntityStore> entityStore = world.getEntityStore().getStore();
         Holder<EntityStore>[] holders = entityChunkComponent.takeEntityHolders();
         if (holders != null) {
            int holderCount = holders.length;

            for (int i = holderCount - 1; i >= 0; i--) {
               Holder<EntityStore> holder = holders[i];
               Archetype<EntityStore> archetype = holder.getArchetype();

               assert archetype != null;

               if (archetype.isEmpty()) {
                  LOGGER.at(Level.SEVERE).log("Empty archetype entity holder: %s (#%d)", holder, i);
                  holders[i] = holders[--holderCount];
                  holders[holderCount] = holder;
                  worldChunkComponent.markNeedsSaving();
               } else if (archetype.count() == 1 && archetype.contains(Nameplate.getComponentType())) {
                  LOGGER.at(Level.SEVERE).log("Nameplate only entity holder: %s (#%d)", holder, i);
                  holders[i] = holders[--holderCount];
                  holders[holderCount] = holder;
                  worldChunkComponent.markNeedsSaving();
               } else {
                  TransformComponent transformComponent = holder.getComponent(TransformComponent.getComponentType());
                  if (transformComponent != null) {
                     transformComponent.setChunkLocation(ref, worldChunkComponent);
                  }
               }
            }

            Ref<EntityStore>[] refs = entityStore.addEntities(holders, 0, holderCount, AddReason.LOAD);

            for (int i = 0; i < refs.length; i++) {
               Ref<EntityStore> entityRef = refs[i];
               if (!entityRef.isValid()) {
                  break;
               }

               entityChunkComponent.loadEntityReference(entityRef);
            }
         }
      }
   }
}
