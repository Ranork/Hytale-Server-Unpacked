package com.hypixel.hytale.server.core.universe.world.chunk;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.Int2ObjectMapCodec;
import com.hypixel.hytale.codec.store.StoredCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
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
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.server.core.modules.LegacyModule;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockComponentChunk implements Component<ChunkStore> {
   public static final BuilderCodec<BlockComponentChunk> CODEC = BuilderCodec.builder(BlockComponentChunk.class, BlockComponentChunk::new)
      .addField(
         new KeyedCodec<>("BlockComponents", new Int2ObjectMapCodec<>(new StoredCodec<>(ChunkStore.HOLDER_CODEC_KEY), Int2ObjectOpenHashMap::new)),
         (entityChunk, map) -> {
            entityChunk.entityHolders.clear();
            entityChunk.entityHolders.putAll(map);
         },
         entityChunk -> {
            if (entityChunk.entityReferences.isEmpty()) {
               return entityChunk.entityHolders;
            } else {
               Int2ObjectMap<Holder<ChunkStore>> map = new Int2ObjectOpenHashMap(entityChunk.entityHolders.size() + entityChunk.entityReferences.size());
               map.putAll(entityChunk.entityHolders);
               Iterator i$ = entityChunk.entityReferences.int2ObjectEntrySet().iterator();

               while (i$.hasNext()) {
                  Entry<Ref<ChunkStore>> entry = (Entry<Ref<ChunkStore>>)i$.next();
                  Ref<ChunkStore> reference = (Ref<ChunkStore>)entry.getValue();
                  Store<ChunkStore> store = reference.getStore();
                  if (store.getArchetype(reference).hasSerializableComponents(store.getRegistry().getData())) {
                     map.put(entry.getIntKey(), store.copySerializableEntity(reference));
                  }
               }

               return map;
            }
         }
      )
      .build();
   @Nonnull
   private final Int2ObjectMap<Holder<ChunkStore>> entityHolders;
   @Nonnull
   private final Int2ObjectMap<Ref<ChunkStore>> entityReferences;
   @Nonnull
   private final Int2ObjectMap<Holder<ChunkStore>> entityHoldersUnmodifiable;
   @Nonnull
   private final Int2ObjectMap<Ref<ChunkStore>> entityReferencesUnmodifiable;
   private boolean needsSaving;

   public static ComponentType<ChunkStore, BlockComponentChunk> getComponentType() {
      return LegacyModule.get().getBlockComponentChunkComponentType();
   }

   public BlockComponentChunk() {
      this.entityHolders = new Int2ObjectOpenHashMap();
      this.entityReferences = new Int2ObjectOpenHashMap();
      this.entityHoldersUnmodifiable = Int2ObjectMaps.unmodifiable(this.entityHolders);
      this.entityReferencesUnmodifiable = Int2ObjectMaps.unmodifiable(this.entityReferences);
   }

   public BlockComponentChunk(@Nonnull Int2ObjectMap<Holder<ChunkStore>> entityHolders, @Nonnull Int2ObjectMap<Ref<ChunkStore>> entityReferences) {
      this.entityHolders = entityHolders;
      this.entityReferences = entityReferences;
      this.entityHoldersUnmodifiable = Int2ObjectMaps.unmodifiable(entityHolders);
      this.entityReferencesUnmodifiable = Int2ObjectMaps.unmodifiable(entityReferences);
   }

   @Nonnull
   @Override
   public Component<ChunkStore> clone() {
      Int2ObjectOpenHashMap<Holder<ChunkStore>> entityHoldersClone = new Int2ObjectOpenHashMap(this.entityHolders.size() + this.entityReferences.size());
      ObjectIterator var2 = this.entityHolders.int2ObjectEntrySet().iterator();

      while (var2.hasNext()) {
         Entry<Holder<ChunkStore>> entry = (Entry<Holder<ChunkStore>>)var2.next();
         entityHoldersClone.put(entry.getIntKey(), ((Holder)entry.getValue()).clone());
      }

      var2 = this.entityReferences.int2ObjectEntrySet().iterator();

      while (var2.hasNext()) {
         Entry<Ref<ChunkStore>> entry = (Entry<Ref<ChunkStore>>)var2.next();
         Ref<ChunkStore> reference = (Ref<ChunkStore>)entry.getValue();
         entityHoldersClone.put(entry.getIntKey(), reference.getStore().copyEntity(reference));
      }

      return new BlockComponentChunk(entityHoldersClone, new Int2ObjectOpenHashMap());
   }

   @Nonnull
   @Override
   public Component<ChunkStore> cloneSerializable() {
      ComponentRegistry.Data<ChunkStore> data = ChunkStore.REGISTRY.getData();
      Int2ObjectOpenHashMap<Holder<ChunkStore>> entityHoldersClone = new Int2ObjectOpenHashMap(this.entityHolders.size() + this.entityReferences.size());
      ObjectIterator var3 = this.entityHolders.int2ObjectEntrySet().iterator();

      while (var3.hasNext()) {
         Entry<Holder<ChunkStore>> entry = (Entry<Holder<ChunkStore>>)var3.next();
         Holder<ChunkStore> holder = (Holder<ChunkStore>)entry.getValue();
         if (holder.getArchetype().hasSerializableComponents(data)) {
            entityHoldersClone.put(entry.getIntKey(), holder.cloneSerializable(data));
         }
      }

      var3 = this.entityReferences.int2ObjectEntrySet().iterator();

      while (var3.hasNext()) {
         Entry<Ref<ChunkStore>> entry = (Entry<Ref<ChunkStore>>)var3.next();
         Ref<ChunkStore> reference = (Ref<ChunkStore>)entry.getValue();
         Store<ChunkStore> store = reference.getStore();
         if (store.getArchetype(reference).hasSerializableComponents(data)) {
            entityHoldersClone.put(entry.getIntKey(), store.copySerializableEntity(reference));
         }
      }

      return new BlockComponentChunk(entityHoldersClone, new Int2ObjectOpenHashMap());
   }

   @Nonnull
   public Int2ObjectMap<Holder<ChunkStore>> getEntityHolders() {
      return this.entityHoldersUnmodifiable;
   }

   @Nullable
   public Holder<ChunkStore> getEntityHolder(int index) {
      return (Holder<ChunkStore>)this.entityHolders.get(index);
   }

   public void addEntityHolder(int index, @Nonnull Holder<ChunkStore> holder) {
      if (this.entityReferences.containsKey(index)) {
         throw new IllegalArgumentException("Duplicate block components at: " + index);
      } else if (this.entityHolders.putIfAbsent(index, Objects.requireNonNull(holder)) != null) {
         throw new IllegalArgumentException("Duplicate block components (entity holder) at: " + index);
      } else {
         this.markNeedsSaving();
      }
   }

   public void storeEntityHolder(int index, @Nonnull Holder<ChunkStore> holder) {
      if (this.entityHolders.putIfAbsent(index, Objects.requireNonNull(holder)) != null) {
         throw new IllegalArgumentException("Duplicate block components (entity holder) at: " + index);
      }
   }

   @Nullable
   public Holder<ChunkStore> removeEntityHolder(int index) {
      Holder<ChunkStore> reference = (Holder<ChunkStore>)this.entityHolders.remove(index);
      if (reference != null) {
         this.markNeedsSaving();
      }

      return reference;
   }

   @Nonnull
   public Int2ObjectMap<Ref<ChunkStore>> getEntityReferences() {
      return this.entityReferencesUnmodifiable;
   }

   @Nullable
   public Ref<ChunkStore> getEntityReference(int index) {
      return (Ref<ChunkStore>)this.entityReferences.get(index);
   }

   public void addEntityReference(int index, @Nonnull Ref<ChunkStore> reference) {
      reference.validate();
      if (this.entityHolders.containsKey(index)) {
         throw new IllegalArgumentException("Duplicate block components at: " + index);
      } else if (this.entityReferences.putIfAbsent(index, Objects.requireNonNull(reference)) != null) {
         throw new IllegalArgumentException("Duplicate block components (entity reference) at: " + index);
      } else {
         this.markNeedsSaving();
      }
   }

   public void loadEntityReference(int index, @Nonnull Ref<ChunkStore> reference) {
      reference.validate();
      if (this.entityHolders.containsKey(index)) {
         throw new IllegalArgumentException("Duplicate block components at: " + index);
      } else if (this.entityReferences.putIfAbsent(index, Objects.requireNonNull(reference)) != null) {
         throw new IllegalArgumentException("Duplicate block components (entity reference) at: " + index);
      }
   }

   public void removeEntityReference(int index, Ref<ChunkStore> reference) {
      if (this.entityReferences.remove(index, reference)) {
         this.markNeedsSaving();
      }
   }

   public void unloadEntityReference(int index, Ref<ChunkStore> reference) {
      this.entityReferences.remove(index, reference);
   }

   @Nullable
   public Int2ObjectMap<Holder<ChunkStore>> takeEntityHolders() {
      if (this.entityHolders.isEmpty()) {
         return null;
      } else {
         Int2ObjectOpenHashMap<Holder<ChunkStore>> holders = new Int2ObjectOpenHashMap(this.entityHolders);
         this.entityHolders.clear();
         return holders;
      }
   }

   @Nullable
   public Int2ObjectMap<Ref<ChunkStore>> takeEntityReferences() {
      if (this.entityReferences.isEmpty()) {
         return null;
      } else {
         Int2ObjectOpenHashMap<Ref<ChunkStore>> holders = new Int2ObjectOpenHashMap(this.entityReferences);
         this.entityReferences.clear();
         return holders;
      }
   }

   @Nullable
   public <T extends Component<ChunkStore>> T getComponent(int index, @Nonnull ComponentType<ChunkStore, T> componentType) {
      Ref<ChunkStore> reference = (Ref<ChunkStore>)this.entityReferences.get(index);
      if (reference != null) {
         return reference.getStore().getComponent(reference, componentType);
      } else {
         Holder<ChunkStore> holder = (Holder<ChunkStore>)this.entityHolders.get(index);
         return holder != null ? holder.getComponent(componentType) : null;
      }
   }

   public boolean hasComponents(int index) {
      return this.entityReferences.containsKey(index) || this.entityHolders.containsKey(index);
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

   public static class BlockComponentChunkLoadingSystem extends RefChangeSystem<ChunkStore, NonTicking<ChunkStore>> {
      private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
      private final Archetype<ChunkStore> archetype = Archetype.of(WorldChunk.getComponentType(), BlockComponentChunk.getComponentType());

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
         BlockComponentChunk blockComponentChunk = store.getComponent(ref, BlockComponentChunk.getComponentType());
         Int2ObjectMap<Ref<ChunkStore>> entityReferences = blockComponentChunk.takeEntityReferences();
         if (entityReferences != null) {
            int size = entityReferences.size();
            int[] indexes = new int[size];
            Ref<ChunkStore>[] references = new Ref[size];
            int j = 0;

            for (ObjectIterator data = entityReferences.int2ObjectEntrySet().iterator(); data.hasNext(); j++) {
               Entry<Ref<ChunkStore>> entry = (Entry<Ref<ChunkStore>>)data.next();
               indexes[j] = entry.getIntKey();
               references[j] = (Ref<ChunkStore>)entry.getValue();
            }

            ComponentRegistry.Data<ChunkStore> data = ChunkStore.REGISTRY.getData();

            for (int i = 0; i < size; i++) {
               if (store.getArchetype(references[i]).hasSerializableComponents(data)) {
                  Holder<ChunkStore> holder = ChunkStore.REGISTRY.newHolder();
                  commandBuffer.removeEntity(references[i], holder, RemoveReason.UNLOAD);
                  blockComponentChunk.storeEntityHolder(indexes[i], holder);
               } else {
                  commandBuffer.removeEntity(references[i], RemoveReason.UNLOAD);
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
         WorldChunk chunk = store.getComponent(ref, WorldChunk.getComponentType());
         BlockComponentChunk blockComponentChunk = store.getComponent(ref, BlockComponentChunk.getComponentType());
         Int2ObjectMap<Holder<ChunkStore>> entityHolders = blockComponentChunk.takeEntityHolders();
         if (entityHolders != null) {
            int holderCount = entityHolders.size();
            int[] indexes = new int[holderCount];
            Holder<ChunkStore>[] holders = new Holder[holderCount];
            int j = 0;

            for (ObjectIterator i = entityHolders.int2ObjectEntrySet().iterator(); i.hasNext(); j++) {
               Entry<Holder<ChunkStore>> entry = (Entry<Holder<ChunkStore>>)i.next();
               indexes[j] = entry.getIntKey();
               holders[j] = (Holder<ChunkStore>)entry.getValue();
            }

            for (int i = holderCount - 1; i >= 0; i--) {
               Holder<ChunkStore> holder = holders[i];
               if (holder.getArchetype().isEmpty()) {
                  LOGGER.at(Level.SEVERE).log("Empty archetype entity holder: %s (#%d)", holder, i);
                  holders[i] = holders[--holderCount];
                  holders[holderCount] = holder;
                  chunk.markNeedsSaving();
               } else {
                  int index = indexes[i];
                  int x = ChunkUtil.xFromBlockInColumn(index);
                  int y = ChunkUtil.yFromBlockInColumn(index);
                  int z = ChunkUtil.zFromBlockInColumn(index);
                  holder.putComponent(BlockModule.BlockStateInfo.getComponentType(), new BlockModule.BlockStateInfo(index, ref));
                  BlockState state = BlockState.getBlockState(holder);
                  if (state != null) {
                     state.setPosition(chunk, new Vector3i(x, y, z));
                  }
               }
            }

            commandBuffer.addEntities(holders, AddReason.LOAD);
         }
      }
   }

   public static class LoadBlockComponentPacketSystem extends ChunkStore.LoadPacketDataQuerySystem {
      private final ComponentType<ChunkStore, BlockComponentChunk> componentType;

      public LoadBlockComponentPacketSystem(ComponentType<ChunkStore, BlockComponentChunk> blockComponentChunkComponentType) {
         this.componentType = blockComponentChunkComponentType;
      }

      @Override
      public Query<ChunkStore> getQuery() {
         return this.componentType;
      }

      public void fetch(
         int index,
         @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
         @Nonnull Store<ChunkStore> store,
         CommandBuffer<ChunkStore> commandBuffer,
         PlayerRef player,
         @Nonnull List<ToClientPacket> results
      ) {
         BlockComponentChunk component = archetypeChunk.getComponent(index, this.componentType);
         ObjectCollection<Ref<ChunkStore>> references = component.entityReferences.values();
         Store<ChunkStore> componentStore = store.getExternalData().getWorld().getChunkStore().getStore();
         componentStore.fetch(references, ChunkStore.LOAD_PACKETS_DATA_QUERY_SYSTEM_TYPE, player, results);
      }
   }

   public static class UnloadBlockComponentPacketSystem extends ChunkStore.UnloadPacketDataQuerySystem {
      private final ComponentType<ChunkStore, BlockComponentChunk> componentType;

      public UnloadBlockComponentPacketSystem(ComponentType<ChunkStore, BlockComponentChunk> blockComponentChunkComponentType) {
         this.componentType = blockComponentChunkComponentType;
      }

      @Override
      public Query<ChunkStore> getQuery() {
         return this.componentType;
      }

      public void fetch(
         int index,
         @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
         @Nonnull Store<ChunkStore> store,
         CommandBuffer<ChunkStore> commandBuffer,
         PlayerRef player,
         @Nonnull List<ToClientPacket> results
      ) {
         BlockComponentChunk component = archetypeChunk.getComponent(index, this.componentType);
         ObjectCollection<Ref<ChunkStore>> references = component.entityReferences.values();
         Store<ChunkStore> componentStore = store.getExternalData().getWorld().getChunkStore().getStore();
         componentStore.fetch(references, ChunkStore.UNLOAD_PACKETS_DATA_QUERY_SYSTEM_TYPE, player, results);
      }
   }
}
