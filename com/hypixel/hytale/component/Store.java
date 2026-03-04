package com.hypixel.hytale.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.component.data.ForEachTaskData;
import com.hypixel.hytale.component.data.change.ChangeType;
import com.hypixel.hytale.component.data.change.ComponentChange;
import com.hypixel.hytale.component.data.change.DataChange;
import com.hypixel.hytale.component.data.change.SystemChange;
import com.hypixel.hytale.component.data.unknown.UnknownComponents;
import com.hypixel.hytale.component.event.EntityEventType;
import com.hypixel.hytale.component.event.WorldEventType;
import com.hypixel.hytale.component.metric.ArchetypeChunkData;
import com.hypixel.hytale.component.metric.SystemMetricData;
import com.hypixel.hytale.component.query.ExactArchetypeQuery;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.ArchetypeChunkSystem;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.component.system.MetricSystem;
import com.hypixel.hytale.component.system.QuerySystem;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.StoreSystem;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.component.system.data.ArchetypeDataSystem;
import com.hypixel.hytale.component.system.data.EntityDataSystem;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.component.system.tick.TickableSystem;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.component.task.ParallelRangeTask;
import com.hypixel.hytale.component.task.ParallelTask;
import com.hypixel.hytale.function.consumer.IntBiObjectConsumer;
import com.hypixel.hytale.metrics.MetricResults;
import com.hypixel.hytale.metrics.MetricsRegistry;
import com.hypixel.hytale.metrics.metric.HistoricMetric;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Store<ECS_TYPE> implements ComponentAccessor<ECS_TYPE> {
   public static final Store[] EMPTY_ARRAY = new Store[0];
   @Nonnull
   public static final MetricsRegistry<Store<?>> METRICS_REGISTRY = new MetricsRegistry<Store<?>>()
      .register("ArchetypeChunkCount", Store::getArchetypeChunkCount, Codec.INTEGER)
      .register("EntityCount", Store::getEntityCount, Codec.INTEGER)
      .register(
         "Systems",
         componentStore -> {
            ComponentRegistry.Data<?> data = componentStore.getRegistry().getData();
            HistoricMetric[] systemMetrics = componentStore.getSystemMetrics();
            SystemMetricData[] systemMetricData = new SystemMetricData[data.getSystemSize()];

            for (int systemIndex = 0; systemIndex < data.getSystemSize(); systemIndex++) {
               ISystem<?> system = data.getSystem(systemIndex);
               MetricResults metrics = null;
               if (system instanceof MetricSystem metricSystem) {
                  metrics = metricSystem.toMetricResults(componentStore);
               }

               systemMetricData[systemIndex] = new SystemMetricData(
                  system.getClass().getName(),
                  componentStore.getArchetypeChunkCountFor(systemIndex),
                  componentStore.getEntityCountFor(systemIndex),
                  system instanceof TickingSystem ? systemMetrics[systemIndex] : null,
                  metrics
               );
            }

            return systemMetricData;
         },
         new ArrayCodec<>(SystemMetricData.CODEC, SystemMetricData[]::new)
      )
      .register("ArchetypeChunks", Store::collectArchetypeChunkData, new ArrayCodec<>(ArchetypeChunkData.CODEC, ArchetypeChunkData[]::new));
   @Nonnull
   private final ComponentRegistry<ECS_TYPE> registry;
   @Nonnull
   private final ECS_TYPE externalData;
   @Nonnull
   private final IResourceStorage resourceStorage;
   private final Deque<CommandBuffer<ECS_TYPE>> commandBuffers = new ArrayDeque<>();
   private final Thread thread = Thread.currentThread();
   @Nonnull
   private final ParallelTask<EntityTickingSystem.SystemTaskData<ECS_TYPE>> parallelTask = new ParallelTask<>(EntityTickingSystem.SystemTaskData::new);
   @Nonnull
   private final ParallelTask<ForEachTaskData<ECS_TYPE>> forEachTask = new ParallelTask<>(ForEachTaskData::new);
   @Nonnull
   private final ParallelTask<EntityDataSystem.SystemTaskData<ECS_TYPE, ?, ?>> fetchTask = new ParallelTask<>(EntityDataSystem.SystemTaskData::new);
   @Nonnull
   private final Store.ProcessingCounter processing = new Store.ProcessingCounter();
   private boolean shutdown;
   int storeIndex;
   private int entitiesSize;
   @Nonnull
   private Ref<ECS_TYPE>[] refs = new Ref[16];
   @Nonnull
   private int[] entityToArchetypeChunk = new int[16];
   @Nonnull
   private int[] entityChunkIndex = new int[16];
   @Nonnull
   private BitSet[] systemIndexToArchetypeChunkIndexes = ArrayUtil.EMPTY_BITSET_ARRAY;
   @Nonnull
   private BitSet[] archetypeChunkIndexesToSystemIndex = ArrayUtil.EMPTY_BITSET_ARRAY;
   @Nonnull
   private final Object2IntMap<Archetype<ECS_TYPE>> archetypeToIndexMap = new Object2IntOpenHashMap();
   private int archetypeSize;
   @Nonnull
   private final BitSet archetypeChunkReuse = new BitSet();
   @Nonnull
   private ArchetypeChunk<ECS_TYPE>[] archetypeChunks = ArchetypeChunk.emptyArray();
   @Nonnull
   private Resource<ECS_TYPE>[] resources = Resource.EMPTY_ARRAY;
   @Nonnull
   private HistoricMetric[] systemMetrics = HistoricMetric.EMPTY_ARRAY;
   @Deprecated(forRemoval = true)
   private boolean disableProcessingAssert = false;

   Store(@Nonnull ComponentRegistry<ECS_TYPE> registry, int storeIndex, @Nonnull ECS_TYPE externalData, @Nonnull IResourceStorage resourceStorage) {
      this.registry = registry;
      this.storeIndex = storeIndex;
      this.externalData = externalData;
      this.resourceStorage = resourceStorage;
      this.archetypeToIndexMap.defaultReturnValue(Integer.MIN_VALUE);
      Arrays.fill(this.entityToArchetypeChunk, Integer.MIN_VALUE);
      Arrays.fill(this.entityChunkIndex, Integer.MIN_VALUE);
   }

   @Nonnull
   CommandBuffer<ECS_TYPE> takeCommandBuffer() {
      this.assertThread();
      if (this.commandBuffers.isEmpty()) {
         return new CommandBuffer<>(this);
      } else {
         CommandBuffer<ECS_TYPE> buffer = this.commandBuffers.pop();

         assert buffer.setThread();

         return buffer;
      }
   }

   void storeCommandBuffer(@Nonnull CommandBuffer<ECS_TYPE> commandBuffer) {
      this.assertThread();
      commandBuffer.validateEmpty();
      this.commandBuffers.add(commandBuffer);
   }

   public int getStoreIndex() {
      return this.storeIndex;
   }

   @Nonnull
   public ComponentRegistry<ECS_TYPE> getRegistry() {
      return this.registry;
   }

   @Nonnull
   @Override
   public ECS_TYPE getExternalData() {
      return this.externalData;
   }

   @Nonnull
   public IResourceStorage getResourceStorage() {
      return this.resourceStorage;
   }

   @Nonnull
   public ParallelTask<EntityTickingSystem.SystemTaskData<ECS_TYPE>> getParallelTask() {
      return this.parallelTask;
   }

   @Nonnull
   public ParallelTask<EntityDataSystem.SystemTaskData<ECS_TYPE, ?, ?>> getFetchTask() {
      return this.fetchTask;
   }

   @Nonnull
   public HistoricMetric[] getSystemMetrics() {
      this.assertThread();
      return this.systemMetrics;
   }

   public boolean isShutdown() {
      return this.shutdown;
   }

   void onAdd(@Nonnull ComponentRegistry.Data<ECS_TYPE> data) {
      this.updateArchetypeIndexes(data);
      int resourceSize = data.getResourceSize();
      this.resources = Arrays.copyOf(this.resources, resourceSize);

      for (int index = 0; index < resourceSize; index++) {
         ResourceType<ECS_TYPE, ? extends Resource<ECS_TYPE>> resourceType = (ResourceType<ECS_TYPE, ? extends Resource<ECS_TYPE>>)data.getResourceType(index);
         if (resourceType != null) {
            this.resources[index] = (Resource<ECS_TYPE>)this.resourceStorage.load(this, data, resourceType).join();
         }
      }

      for (int systemIndex = 0; systemIndex < data.getSystemSize(); systemIndex++) {
         this.updateData(data, data, new SystemChange<>(ChangeType.REGISTERED, data.getSystem(systemIndex)));
      }

      this.systemMetrics = Arrays.copyOf(this.systemMetrics, data.getSystemSize());
      SystemType<ECS_TYPE, TickableSystem<ECS_TYPE>> tickingSystemType = this.registry.getTickableSystemType();
      BitSet systemIndexes = data.getSystemIndexesForType(tickingSystemType);
      int systemIndex = -1;

      while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
         this.systemMetrics[systemIndex] = HistoricMetric.builder(33333333L, TimeUnit.NANOSECONDS)
            .addPeriod(1L, TimeUnit.SECONDS)
            .addPeriod(1L, TimeUnit.MINUTES)
            .addPeriod(5L, TimeUnit.MINUTES)
            .build();
      }
   }

   public void shutdown() {
      if (this.shutdown) {
         throw new IllegalStateException("Store is already shutdown!");
      } else {
         this.registry.removeStore(this);
      }
   }

   void shutdown0(@Nonnull ComponentRegistry.Data<ECS_TYPE> data) {
      if (this.thread.isAlive() && !this.thread.equals(Thread.currentThread())) {
         throw new IllegalArgumentException("Unable to shutdown store while thread is still running!");
      } else {
         for (int systemIndex = data.getSystemSize() - 1; systemIndex >= 0; systemIndex--) {
            this.updateData(data, data, new SystemChange<>(ChangeType.UNREGISTERED, data.getSystem(systemIndex)));
         }

         this.saveAllResources0(data).join();
         this.processing.lock();

         try {
            for (int i = 0; i < this.entitiesSize; i++) {
               this.refs[i].invalidate();
               this.refs[i] = null;
            }
         } finally {
            this.processing.unlock();
         }

         this.shutdown = true;
      }
   }

   @Nonnull
   public CompletableFuture<Void> saveAllResources() {
      return this.saveAllResources0(this.registry.getData());
   }

   @Nonnull
   private CompletableFuture<Void> saveAllResources0(@Nonnull ComponentRegistry.Data<ECS_TYPE> data) {
      int resourceSize = data.getResourceSize();
      CompletableFuture<Void>[] futures = new CompletableFuture[resourceSize];
      int idx = 0;

      for (int index = 0; index < resourceSize; index++) {
         ResourceType<ECS_TYPE, ? extends Resource<ECS_TYPE>> resourceType = (ResourceType<ECS_TYPE, ? extends Resource<ECS_TYPE>>)data.getResourceType(index);
         if (resourceType != null) {
            futures[idx++] = this.resourceStorage.save(this, data, resourceType, this.resources[index]);
         }
      }

      return CompletableFuture.allOf(Arrays.copyOf(futures, idx));
   }

   public int getEntityCount() {
      return this.entitiesSize;
   }

   public int getEntityCountFor(@Nonnull Query<ECS_TYPE> query) {
      this.assertThread();
      if (query instanceof ExactArchetypeQuery<ECS_TYPE> exactQuery) {
         int archetypeIndex = this.archetypeToIndexMap.getInt(exactQuery.getArchetype());
         return archetypeIndex != Integer.MIN_VALUE ? this.archetypeChunks[archetypeIndex].size() : 0;
      } else {
         int count = 0;

         for (int archetypeIndex = 0; archetypeIndex < this.archetypeSize; archetypeIndex++) {
            ArchetypeChunk<ECS_TYPE> archetypeChunk = this.archetypeChunks[archetypeIndex];
            if (archetypeChunk != null && query.test(archetypeChunk.getArchetype())) {
               count += archetypeChunk.size();
            }
         }

         return count;
      }
   }

   public int getEntityCountFor(int systemIndex) {
      this.assertThread();
      int count = 0;
      BitSet indexes = this.systemIndexToArchetypeChunkIndexes[systemIndex];
      int index = -1;

      while ((index = indexes.nextSetBit(index + 1)) >= 0) {
         count += this.archetypeChunks[index].size();
      }

      return count;
   }

   public int getArchetypeChunkCount() {
      this.assertThread();
      return this.archetypeSize;
   }

   @Nonnull
   public ArchetypeChunkData[] collectArchetypeChunkData() {
      this.assertThread();
      ObjectArrayList<ArchetypeChunkData> result = new ObjectArrayList(this.archetypeSize);

      for (int i = 0; i < this.archetypeSize; i++) {
         ArchetypeChunk<ECS_TYPE> chunk = this.archetypeChunks[i];
         if (chunk != null) {
            Archetype<ECS_TYPE> archetype = chunk.getArchetype();
            String[] componentTypeNames = new String[archetype.count()];
            int nameIndex = 0;

            for (int j = archetype.getMinIndex(); j < archetype.length(); j++) {
               ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType = (ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>)archetype.get(j);
               if (componentType != null) {
                  componentTypeNames[nameIndex++] = componentType.getTypeClass().getName();
               }
            }

            result.add(new ArchetypeChunkData(componentTypeNames, chunk.size()));
         }
      }

      return (ArchetypeChunkData[])result.toArray(ArchetypeChunkData[]::new);
   }

   public int getArchetypeChunkCountFor(int systemIndex) {
      this.assertThread();
      return this.systemIndexToArchetypeChunkIndexes[systemIndex].cardinality();
   }

   protected void setEntityChunkIndex(@Nonnull Ref<ECS_TYPE> ref, int newEntityChunkIndex) {
      if (ref.isValid()) {
         this.entityChunkIndex[ref.getIndex()] = newEntityChunkIndex;
      }
   }

   @Nullable
   public Ref<ECS_TYPE> addEntity(@Nonnull Archetype<ECS_TYPE> archetype, @Nonnull AddReason reason) {
      this.assertThread();
      this.assertWriteProcessing();
      Component<ECS_TYPE>[] entityComponents;
      if (archetype.isEmpty()) {
         entityComponents = Component.EMPTY_ARRAY;
      } else {
         ComponentRegistry.Data<ECS_TYPE> data = this.registry._internal_getData();
         entityComponents = new Component[archetype.length()];

         for (int i = archetype.getMinIndex(); i < archetype.length(); i++) {
            ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType = (ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>)archetype.get(i);
            if (componentType != null) {
               entityComponents[componentType.getIndex()] = data.createComponent((ComponentType<ECS_TYPE, Component<ECS_TYPE>>)componentType);
            }
         }
      }

      return this.addEntity(this.registry.newHolder(archetype, entityComponents), new Ref<>(this), reason);
   }

   @Nullable
   @Override
   public Ref<ECS_TYPE> addEntity(@Nonnull Holder<ECS_TYPE> holder, @Nonnull AddReason reason) {
      return this.addEntity(holder, new Ref<>(this), reason);
   }

   @Nullable
   public Ref<ECS_TYPE> addEntity(@Nonnull Holder<ECS_TYPE> holder, @Nonnull Ref<ECS_TYPE> ref, @Nonnull AddReason reason) {
      if (ref.isValid()) {
         throw new IllegalArgumentException("EntityReference is already in use!");
      } else if (ref.getStore() != this) {
         throw new IllegalArgumentException("EntityReference is not for this store!");
      } else {
         this.assertThread();
         this.assertWriteProcessing();
         CommandBuffer<ECS_TYPE> commandBuffer = this.takeCommandBuffer();
         ComponentRegistry.Data<ECS_TYPE> data = this.registry._internal_getData();
         this.processing.lock();

         try {
            SystemType<ECS_TYPE, HolderSystem<ECS_TYPE>> systemType = this.registry.getHolderSystemType();
            BitSet systemIndexes = data.getSystemIndexesForType(systemType);
            int systemIndex = -1;

            while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
               HolderSystem<ECS_TYPE> system = data.getSystem(systemIndex, systemType);
               if (system.test(this.registry, holder.getArchetype())) {
                  system.onEntityAdd(holder, reason, this);
               }
            }

            int entityIndex = this.entitiesSize++;
            int oldLength = this.refs.length;
            if (oldLength <= entityIndex) {
               systemIndex = ArrayUtil.grow(entityIndex);
               this.refs = Arrays.copyOf(this.refs, systemIndex);
               this.entityToArchetypeChunk = Arrays.copyOf(this.entityToArchetypeChunk, systemIndex);
               this.entityChunkIndex = Arrays.copyOf(this.entityChunkIndex, systemIndex);
               Arrays.fill(this.entityToArchetypeChunk, oldLength, systemIndex, Integer.MIN_VALUE);
               Arrays.fill(this.entityChunkIndex, oldLength, systemIndex, Integer.MIN_VALUE);
            }

            this.refs[entityIndex] = ref;
            ref.setIndex(entityIndex);
            systemIndex = this.findOrCreateArchetypeChunk(holder.getArchetype());
            ArchetypeChunk<ECS_TYPE> archetypeChunk = this.archetypeChunks[systemIndex];
            int chunkEntityRef = archetypeChunk.addEntity(ref, holder);
            this.entityToArchetypeChunk[entityIndex] = systemIndex;
            this.entityChunkIndex[entityIndex] = chunkEntityRef;
            SystemType<ECS_TYPE, RefSystem<ECS_TYPE>> systemTypex = this.registry.getRefSystemType();
            BitSet systemIndexesx = data.getSystemIndexesForType(systemTypex);
            BitSet entityProcessedBySystemIndexes = this.archetypeChunkIndexesToSystemIndex[systemIndex];
            commandBuffer.track(ref);
            int systemIndexx = -1;

            while ((systemIndexx = systemIndexesx.nextSetBit(systemIndexx + 1)) >= 0) {
               if (entityProcessedBySystemIndexes.get(systemIndexx)) {
                  RefSystem<ECS_TYPE> system = data.getSystem(systemIndexx, systemTypex);
                  boolean oldDisableProcessingAssert = this.disableProcessingAssert;
                  this.disableProcessingAssert = system instanceof DisableProcessingAssert;
                  system.onEntityAdded(ref, reason, this, commandBuffer);
                  this.disableProcessingAssert = oldDisableProcessingAssert;
                  if (commandBuffer.consumeWasTrackedRefRemoved()) {
                     break;
                  }
               }
            }
         } finally {
            this.processing.unlock();
         }

         commandBuffer.consume();
         return ref.isValid() ? ref : null;
      }
   }

   @Nonnull
   @Override
   public Ref<ECS_TYPE>[] addEntities(@Nonnull Holder<ECS_TYPE>[] holders, @Nonnull AddReason reason) {
      return this.addEntities(holders, 0, holders.length, reason);
   }

   @Nonnull
   public Ref<ECS_TYPE>[] addEntities(@Nonnull Holder<ECS_TYPE>[] holders, int start, int length, @Nonnull AddReason reason) {
      Ref<ECS_TYPE>[] refs = new Ref[length];

      for (int i = 0; i < length; i++) {
         refs[i] = new Ref<>(this);
      }

      this.addEntities(holders, start, refs, 0, length, reason);
      return refs;
   }

   public void addEntities(@Nonnull Holder<ECS_TYPE>[] holders, @Nonnull Ref<ECS_TYPE>[] refs, @Nonnull AddReason reason) {
      if (holders.length != refs.length) {
         throw new IllegalArgumentException("EntityHolder and EntityReference array length doesn't match!");
      } else {
         this.addEntities(holders, 0, refs, 0, holders.length, reason);
      }
   }

   public void addEntities(
      @Nonnull Holder<ECS_TYPE>[] holders, int holderStart, @Nonnull Ref<ECS_TYPE>[] refs, int refStart, int length, @Nonnull AddReason reason
   ) {
      int holderEnd = holderStart + length;
      int refEnd = refStart + length;
      if (holders.length < holderEnd) {
         throw new IllegalArgumentException("EntityHolder start and length exceed array length!");
      } else if (refs.length < refEnd) {
         throw new IllegalArgumentException("EntityReference start and length exceed array length!");
      } else {
         for (int i = refStart; i < refEnd; i++) {
            Ref<ECS_TYPE> ref = refs[i];
            if (ref.isValid()) {
               throw new IllegalArgumentException("EntityReference is already in use!");
            }

            if (ref.getStore() != this) {
               throw new IllegalArgumentException("EntityReference is not for this store!");
            }
         }

         this.assertThread();
         this.assertWriteProcessing();
         CommandBuffer<ECS_TYPE> commandBuffer = this.takeCommandBuffer();
         ComponentRegistry.Data<ECS_TYPE> data = this.registry._internal_getData();
         this.processing.lock();

         try {
            SystemType<ECS_TYPE, HolderSystem<ECS_TYPE>> systemType = this.registry.getHolderSystemType();
            BitSet systemIndexes = data.getSystemIndexesForType(systemType);
            int systemIndex = -1;

            while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
               HolderSystem<ECS_TYPE> system = data.getSystem(systemIndex, systemType);

               for (int i = holderStart; i < holderEnd; i++) {
                  if (system.test(this.registry, holders[i].getArchetype())) {
                     system.onEntityAdd(holders[i], reason, this);
                  }
               }
            }

            int firstIndex = this.entitiesSize;
            this.entitiesSize += length;
            int oldLength = this.refs.length;
            if (oldLength <= this.entitiesSize) {
               systemIndex = ArrayUtil.grow(this.entitiesSize);
               this.refs = Arrays.copyOf(this.refs, systemIndex);
               this.entityToArchetypeChunk = Arrays.copyOf(this.entityToArchetypeChunk, systemIndex);
               this.entityChunkIndex = Arrays.copyOf(this.entityChunkIndex, systemIndex);
               Arrays.fill(this.entityToArchetypeChunk, oldLength, systemIndex, Integer.MIN_VALUE);
               Arrays.fill(this.entityChunkIndex, oldLength, systemIndex, Integer.MIN_VALUE);
            }

            System.arraycopy(refs, refStart, this.refs, firstIndex, length);
            systemIndex = refStart;

            for (int entityIndex = firstIndex; systemIndex < refEnd; entityIndex++) {
               refs[systemIndex].setIndex(entityIndex);
               systemIndex++;
            }

            systemIndex = 0;

            for (int entityIndex = firstIndex; systemIndex < length; entityIndex++) {
               Ref<ECS_TYPE> refx = refs[refStart + systemIndex];
               Holder<ECS_TYPE> holder = holders[holderStart + systemIndex];
               int archetypeIndex = this.findOrCreateArchetypeChunk(holder.getArchetype());
               ArchetypeChunk<ECS_TYPE> archetypeChunk = this.archetypeChunks[archetypeIndex];
               int chunkEntityRef = archetypeChunk.addEntity(refx, holder);
               this.entityToArchetypeChunk[entityIndex] = archetypeIndex;
               this.entityChunkIndex[entityIndex] = chunkEntityRef;
               systemIndex++;
            }

            SystemType<ECS_TYPE, RefSystem<ECS_TYPE>> systemTypex = this.registry.getRefSystemType();
            BitSet systemIndexesx = data.getSystemIndexesForType(systemTypex);
            int systemIndexx = -1;

            while ((systemIndexx = systemIndexesx.nextSetBit(systemIndexx + 1)) >= 0) {
               for (int ix = refStart; ix < refEnd; ix++) {
                  Ref<ECS_TYPE> refx = refs[ix];
                  int archetypeIndex = this.entityToArchetypeChunk[refx.getIndex()];
                  BitSet entityProcessedBySystemIndexes = this.archetypeChunkIndexesToSystemIndex[archetypeIndex];
                  if (entityProcessedBySystemIndexes.get(systemIndexx)) {
                     RefSystem<ECS_TYPE> system = data.getSystem(systemIndexx, systemTypex);
                     boolean oldDisableProcessingAssert = this.disableProcessingAssert;
                     this.disableProcessingAssert = system instanceof DisableProcessingAssert;
                     commandBuffer.track(refx);
                     system.onEntityAdded(refx, reason, this, commandBuffer);
                     if (commandBuffer.consumeWasTrackedRefRemoved()) {
                        int remaining = refEnd - ix;
                        if (remaining > 1) {
                           System.arraycopy(refs, ix + 1, refs, ix, remaining - 1);
                           refs[refEnd - 1] = refx;
                           ix--;
                        }

                        refEnd--;
                        length--;
                     }

                     this.disableProcessingAssert = oldDisableProcessingAssert;
                  }
               }
            }
         } finally {
            this.processing.unlock();
         }

         commandBuffer.consume();
      }
   }

   @Nonnull
   public Holder<ECS_TYPE> copyEntity(@Nonnull Ref<ECS_TYPE> ref) {
      return this.copyEntity(ref, this.registry.newHolder());
   }

   @Nonnull
   public Holder<ECS_TYPE> copyEntity(@Nonnull Ref<ECS_TYPE> ref, @Nonnull Holder<ECS_TYPE> holder) {
      this.assertThread();
      ref.validate(this);
      int refIndex = ref.getIndex();
      int archetypeIndex = this.entityToArchetypeChunk[refIndex];
      return this.archetypeChunks[archetypeIndex].copyEntity(this.entityChunkIndex[refIndex], holder);
   }

   @Nonnull
   public Holder<ECS_TYPE> copySerializableEntity(@Nonnull Ref<ECS_TYPE> ref) {
      return this.copySerializableEntity(ref, this.registry.newHolder());
   }

   @Nonnull
   public Holder<ECS_TYPE> copySerializableEntity(@Nonnull Ref<ECS_TYPE> ref, @Nonnull Holder<ECS_TYPE> holder) {
      this.assertThread();
      ref.validate(this);
      int refIndex = ref.getIndex();
      int archetypeIndex = this.entityToArchetypeChunk[refIndex];
      return this.archetypeChunks[archetypeIndex].copySerializableEntity(this.registry.getData(), this.entityChunkIndex[refIndex], holder);
   }

   @Nonnull
   @Override
   public Archetype<ECS_TYPE> getArchetype(@Nonnull Ref<ECS_TYPE> ref) {
      this.assertThread();
      ref.validate(this);
      int archetypeIndex = this.entityToArchetypeChunk[ref.getIndex()];
      return this.archetypeChunks[archetypeIndex].getArchetype();
   }

   @Nonnull
   protected Archetype<ECS_TYPE> __internal_getArchetype(@Nonnull Ref<ECS_TYPE> ref) {
      ref.validate(this);
      int archetypeIndex = this.entityToArchetypeChunk[ref.getIndex()];
      return this.archetypeChunks[archetypeIndex].getArchetype();
   }

   @Nonnull
   public Holder<ECS_TYPE> removeEntity(@Nonnull Ref<ECS_TYPE> ref, @Nonnull RemoveReason reason) {
      return this.removeEntity(ref, this.registry.newHolder(), reason);
   }

   @Nonnull
   @Override
   public Holder<ECS_TYPE> removeEntity(@Nonnull Ref<ECS_TYPE> ref, @Nonnull Holder<ECS_TYPE> holder, @Nonnull RemoveReason reason) {
      return this.removeEntity(ref, holder, reason, null);
   }

   @Nonnull
   Holder<ECS_TYPE> removeEntity(@Nonnull Ref<ECS_TYPE> ref, @Nonnull Holder<ECS_TYPE> holder, @Nonnull RemoveReason reason, @Nullable Throwable proxyReason) {
      this.assertThread();
      this.assertWriteProcessing();
      ref.validate(this);
      CommandBuffer<ECS_TYPE> commandBuffer = this.takeCommandBuffer();
      int entityIndex = ref.getIndex();
      int archetypeIndex = this.entityToArchetypeChunk[entityIndex];
      int chunkEntityRef = this.entityChunkIndex[entityIndex];
      ComponentRegistry.Data<ECS_TYPE> data = this.registry._internal_getData();
      this.processing.lock();

      try {
         SystemType<ECS_TYPE, RefSystem<ECS_TYPE>> systemType = this.registry.getRefSystemType();
         BitSet systemIndexes = data.getSystemIndexesForType(systemType);
         BitSet entityProcessedBySystemIndexes = this.archetypeChunkIndexesToSystemIndex[archetypeIndex];
         int systemIndex = -1;

         while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
            if (entityProcessedBySystemIndexes.get(systemIndex)) {
               data.getSystem(systemIndex, systemType).onEntityRemove(ref, reason, this, commandBuffer);
            }
         }

         int lastIndex = this.entitiesSize - 1;
         if (entityIndex != lastIndex) {
            Ref<ECS_TYPE> lastEntityRef = this.refs[lastIndex];
            int lastSelfEntityRef = this.entityToArchetypeChunk[lastIndex];
            systemIndex = this.entityChunkIndex[lastIndex];
            lastEntityRef.setIndex(entityIndex);
            this.refs[entityIndex] = lastEntityRef;
            this.entityToArchetypeChunk[entityIndex] = lastSelfEntityRef;
            this.entityChunkIndex[entityIndex] = systemIndex;
         }

         this.refs[lastIndex] = null;
         this.entityToArchetypeChunk[lastIndex] = Integer.MIN_VALUE;
         this.entityChunkIndex[lastIndex] = Integer.MIN_VALUE;
         this.entitiesSize = lastIndex;
         ArchetypeChunk<ECS_TYPE> archetypeChunk = this.archetypeChunks[archetypeIndex];
         archetypeChunk.removeEntity(chunkEntityRef, holder);
         if (archetypeChunk.size() == 0) {
            this.removeArchetypeChunk(archetypeIndex);
         }

         ref.invalidate(proxyReason);
      } finally {
         this.processing.unlock();
      }

      commandBuffer.consume();
      this.processing.lock();

      try {
         SystemType<ECS_TYPE, HolderSystem<ECS_TYPE>> systemType = this.registry.getHolderSystemType();
         BitSet systemIndexes = data.getSystemIndexesForType(systemType);
         int systemIndex = -1;

         while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
            HolderSystem<ECS_TYPE> system = data.getSystem(systemIndex, systemType);
            if (system.test(this.registry, holder.getArchetype())) {
               system.onEntityRemoved(holder, reason, this);
            }
         }
      } finally {
         this.processing.unlock();
      }

      return holder;
   }

   @Nonnull
   public Holder<ECS_TYPE>[] removeEntities(@Nonnull Ref<ECS_TYPE>[] refs, @Nonnull RemoveReason reason) {
      return this.removeEntities(refs, 0, refs.length, reason);
   }

   @Nonnull
   public Holder<ECS_TYPE>[] removeEntities(@Nonnull Ref<ECS_TYPE>[] refs, int start, int length, @Nonnull RemoveReason reason) {
      Holder<ECS_TYPE>[] holders = new Holder[length];

      for (int i = 0; i < length; i++) {
         holders[i] = this.registry.newHolder();
      }

      return this.removeEntities(refs, start, holders, 0, length, reason);
   }

   @Nonnull
   public Holder<ECS_TYPE>[] removeEntities(@Nonnull Ref<ECS_TYPE>[] refs, @Nonnull Holder<ECS_TYPE>[] holders, @Nonnull RemoveReason reason) {
      if (refs.length != holders.length) {
         throw new IllegalArgumentException("EntityHolder and EntityReference array length doesn't match!");
      } else {
         return this.removeEntities(refs, 0, holders, 0, refs.length, reason);
      }
   }

   @Nonnull
   public Holder<ECS_TYPE>[] removeEntities(
      @Nonnull Ref<ECS_TYPE>[] refArr, int refStart, @Nonnull Holder<ECS_TYPE>[] holders, int holderStart, int length, @Nonnull RemoveReason reason
   ) {
      int refEnd = refStart + length;
      int holderEnd = holderStart + length;
      if (refArr.length < refEnd) {
         throw new IllegalArgumentException("EntityReference start and length exceed array length!");
      } else if (holders.length < holderEnd) {
         throw new IllegalArgumentException("EntityHolder start and length exceed array length!");
      } else {
         for (int i = refStart; i < refEnd; i++) {
            refArr[i].validate(this);
         }

         this.assertThread();
         this.assertWriteProcessing();
         CommandBuffer<ECS_TYPE> commandBuffer = this.takeCommandBuffer();
         ComponentRegistry.Data<ECS_TYPE> data = this.registry._internal_getData();
         this.processing.lock();

         try {
            SystemType<ECS_TYPE, RefSystem<ECS_TYPE>> systemType = this.registry.getRefSystemType();
            BitSet systemIndexes = data.getSystemIndexesForType(systemType);
            int systemIndex = -1;

            while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
               for (int i = refStart; i < refEnd; i++) {
                  Ref<ECS_TYPE> ref = refArr[i];
                  int archetypeIndex = this.entityToArchetypeChunk[ref.getIndex()];
                  BitSet entityProcessedBySystemIndexes = this.archetypeChunkIndexesToSystemIndex[archetypeIndex];
                  if (entityProcessedBySystemIndexes.get(systemIndex)) {
                     data.getSystem(systemIndex, systemType).onEntityRemove(refArr[i], reason, this, commandBuffer);
                  }
               }
            }

            for (int ix = 0; ix < length; ix++) {
               int entityIndex = refArr[refStart + ix].getIndex();
               systemIndex = this.entityToArchetypeChunk[entityIndex];
               int chunkEntityRef = this.entityChunkIndex[entityIndex];
               int lastIndex = this.entitiesSize - 1;
               if (entityIndex != lastIndex) {
                  Ref<ECS_TYPE> lastEntityRef = this.refs[lastIndex];
                  int lastSelfEntityRef = this.entityToArchetypeChunk[lastIndex];
                  int lastEntityChunkIndex = this.entityChunkIndex[lastIndex];
                  lastEntityRef.setIndex(entityIndex);
                  this.refs[entityIndex] = lastEntityRef;
                  this.entityToArchetypeChunk[entityIndex] = lastSelfEntityRef;
                  this.entityChunkIndex[entityIndex] = lastEntityChunkIndex;
               }

               this.refs[lastIndex] = null;
               this.entityToArchetypeChunk[lastIndex] = Integer.MIN_VALUE;
               this.entityChunkIndex[lastIndex] = Integer.MIN_VALUE;
               this.entitiesSize = lastIndex;
               Holder<ECS_TYPE> holder = holders[holderStart + ix];
               ArchetypeChunk<ECS_TYPE> archetypeChunk = this.archetypeChunks[systemIndex];
               archetypeChunk.removeEntity(chunkEntityRef, holder);
               if (archetypeChunk.size() == 0) {
                  this.removeArchetypeChunk(systemIndex);
               }
            }

            for (int ix = refStart; ix < refEnd; ix++) {
               refArr[ix].invalidate();
            }
         } finally {
            this.processing.unlock();
         }

         commandBuffer.consume();
         this.processing.lock();

         try {
            SystemType<ECS_TYPE, HolderSystem<ECS_TYPE>> systemType = this.registry.getHolderSystemType();
            BitSet systemIndexes = data.getSystemIndexesForType(systemType);
            int systemIndex = -1;

            while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
               HolderSystem<ECS_TYPE> system = data.getSystem(systemIndex, systemType);

               for (int ix = holderStart; ix < holderEnd; ix++) {
                  if (system.test(this.registry, holders[ix].getArchetype())) {
                     system.onEntityRemoved(holders[ix], reason, this);
                  }
               }
            }
         } finally {
            this.processing.unlock();
         }

         return holders;
      }
   }

   public <T extends Component<ECS_TYPE>> void ensureComponent(@Nonnull Ref<ECS_TYPE> ref, @Nonnull ComponentType<ECS_TYPE, T> componentType) {
      this.assertThread();
      this.assertWriteProcessing();
      componentType.validateRegistry(this.registry);
      componentType.validate();
      CommandBuffer<ECS_TYPE> commandBuffer = this.takeCommandBuffer();
      int archetypeIndex = this.entityToArchetypeChunk[ref.getIndex()];
      this.processing.lock();

      try {
         ArchetypeChunk<ECS_TYPE> archetypeChunk = this.archetypeChunks[archetypeIndex];
         if (!archetypeChunk.getArchetype().contains(componentType)) {
            T component = this.registry._internal_getData().createComponent(componentType);
            this.datachunk_addComponent(ref, archetypeIndex, componentType, component, commandBuffer);
         }
      } finally {
         this.processing.unlock();
      }

      commandBuffer.consume();
   }

   @Nonnull
   @Override
   public <T extends Component<ECS_TYPE>> T ensureAndGetComponent(@Nonnull Ref<ECS_TYPE> ref, @Nonnull ComponentType<ECS_TYPE, T> componentType) {
      this.assertThread();
      this.assertWriteProcessing();
      int refIndex = ref.getIndex();
      componentType.validateRegistry(this.registry);
      componentType.validate();
      CommandBuffer<ECS_TYPE> commandBuffer = this.takeCommandBuffer();
      int archetypeIndex = this.entityToArchetypeChunk[refIndex];
      this.processing.lock();

      T component;
      try {
         ArchetypeChunk<ECS_TYPE> archetypeChunk = this.archetypeChunks[archetypeIndex];
         component = archetypeChunk.getComponent(this.entityChunkIndex[refIndex], componentType);
         if (component == null) {
            component = this.registry._internal_getData().createComponent(componentType);
            this.datachunk_addComponent(ref, archetypeIndex, componentType, component, commandBuffer);
         }
      } finally {
         this.processing.unlock();
      }

      commandBuffer.consume();
      return component;
   }

   @Nonnull
   @Override
   public <T extends Component<ECS_TYPE>> T addComponent(@Nonnull Ref<ECS_TYPE> ref, @Nonnull ComponentType<ECS_TYPE, T> componentType) {
      this.assertThread();
      this.assertWriteProcessing();
      T component = this.registry._internal_getData().createComponent(componentType);
      this.addComponent(ref, componentType, component);
      return component;
   }

   @Override
   public <T extends Component<ECS_TYPE>> void addComponent(@Nonnull Ref<ECS_TYPE> ref, @Nonnull ComponentType<ECS_TYPE, T> componentType, @Nonnull T component) {
      this.assertThread();
      this.assertWriteProcessing();
      ref.validate(this);
      componentType.validateRegistry(this.registry);
      componentType.validate();
      Objects.requireNonNull(component);
      CommandBuffer<ECS_TYPE> commandBuffer = this.takeCommandBuffer();
      int archetypeIndex = this.entityToArchetypeChunk[ref.getIndex()];
      this.processing.lock();

      try {
         this.datachunk_addComponent(ref, archetypeIndex, componentType, component, commandBuffer);
      } finally {
         this.processing.unlock();
      }

      commandBuffer.consume();
   }

   public <T extends Component<ECS_TYPE>> void replaceComponent(
      @Nonnull Ref<ECS_TYPE> ref, @Nonnull ComponentType<ECS_TYPE, T> componentType, @Nonnull T component
   ) {
      this.assertThread();
      this.assertWriteProcessing();
      ref.validate(this);
      componentType.validateRegistry(this.registry);
      componentType.validate();
      Objects.requireNonNull(component);
      CommandBuffer<ECS_TYPE> commandBuffer = this.takeCommandBuffer();
      int archetypeIndex = this.entityToArchetypeChunk[ref.getIndex()];
      this.processing.lock();

      try {
         ArchetypeChunk<ECS_TYPE> archetypeChunk = this.archetypeChunks[archetypeIndex];
         int chunkEntityRef = this.entityChunkIndex[ref.getIndex()];
         T oldComponent = archetypeChunk.getComponent(chunkEntityRef, componentType);
         archetypeChunk.setComponent(chunkEntityRef, componentType, component);
         BitSet entityProcessedBySystemIndexes = this.archetypeChunkIndexesToSystemIndex[archetypeIndex];
         ComponentRegistry.Data<ECS_TYPE> data = this.registry._internal_getData();
         BitSet systemIndexes = data.getSystemIndexesForType(this.registry.getRefChangeSystemType());
         int systemIndex = -1;

         while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
            if (entityProcessedBySystemIndexes.get(systemIndex)) {
               RefChangeSystem<ECS_TYPE, T> system = (RefChangeSystem<ECS_TYPE, T>)data.getSystem(systemIndex);
               if (system.componentType().getIndex() == componentType.getIndex()) {
                  system.onComponentSet(ref, oldComponent, component, this, commandBuffer);
               }
            }
         }
      } finally {
         this.processing.unlock();
      }

      commandBuffer.consume();
   }

   @Override
   public <T extends Component<ECS_TYPE>> void putComponent(@Nonnull Ref<ECS_TYPE> ref, @Nonnull ComponentType<ECS_TYPE, T> componentType, @Nonnull T component) {
      this.assertThread();
      this.assertWriteProcessing();
      ref.validate(this);
      componentType.validateRegistry(this.registry);
      componentType.validate();
      Objects.requireNonNull(component);
      CommandBuffer<ECS_TYPE> commandBuffer = this.takeCommandBuffer();
      int archetypeIndex = this.entityToArchetypeChunk[ref.getIndex()];
      this.processing.lock();

      try {
         ArchetypeChunk<ECS_TYPE> archetypeChunk = this.archetypeChunks[archetypeIndex];
         if (archetypeChunk.getArchetype().contains(componentType)) {
            int chunkEntityRef = this.entityChunkIndex[ref.getIndex()];
            T oldComponent = archetypeChunk.getComponent(chunkEntityRef, componentType);
            archetypeChunk.setComponent(chunkEntityRef, componentType, component);
            BitSet entityProcessedBySystemIndexes = this.archetypeChunkIndexesToSystemIndex[archetypeIndex];
            ComponentRegistry.Data<ECS_TYPE> data = this.registry._internal_getData();
            BitSet systemIndexes = data.getSystemIndexesForType(this.registry.getRefChangeSystemType());
            int systemIndex = -1;

            while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
               if (entityProcessedBySystemIndexes.get(systemIndex)) {
                  RefChangeSystem<ECS_TYPE, T> system = (RefChangeSystem<ECS_TYPE, T>)data.getSystem(systemIndex);
                  if (system.componentType().getIndex() == componentType.getIndex()) {
                     system.onComponentSet(ref, oldComponent, component, this, commandBuffer);
                  }
               }
            }
         } else {
            this.datachunk_addComponent(ref, archetypeIndex, componentType, component, commandBuffer);
         }
      } finally {
         this.processing.unlock();
      }

      commandBuffer.consume();
   }

   @Override
   public <T extends Component<ECS_TYPE>> T getComponent(@Nonnull Ref<ECS_TYPE> ref, @Nonnull ComponentType<ECS_TYPE, T> componentType) {
      this.assertThread();
      return this.__internal_getComponent(ref, componentType);
   }

   @Nullable
   protected <T extends Component<ECS_TYPE>> T __internal_getComponent(@Nonnull Ref<ECS_TYPE> ref, @Nonnull ComponentType<ECS_TYPE, T> componentType) {
      ref.validate(this);
      componentType.validateRegistry(this.registry);
      componentType.validate();
      int archetypeIndex = this.entityToArchetypeChunk[ref.getIndex()];
      ArchetypeChunk<ECS_TYPE> archetypeChunk = this.archetypeChunks[archetypeIndex];
      return archetypeChunk.getComponent(this.entityChunkIndex[ref.getIndex()], componentType);
   }

   @Override
   public <T extends Component<ECS_TYPE>> void removeComponent(@Nonnull Ref<ECS_TYPE> ref, @Nonnull ComponentType<ECS_TYPE, T> componentType) {
      this.assertThread();
      this.assertWriteProcessing();
      ref.validate(this);
      componentType.validateRegistry(this.registry);
      componentType.validate();
      CommandBuffer<ECS_TYPE> commandBuffer = this.takeCommandBuffer();
      int entityIndex = ref.getIndex();
      int fromArchetypeIndex = this.entityToArchetypeChunk[entityIndex];
      this.processing.lock();

      try {
         ArchetypeChunk<ECS_TYPE> fromArchetypeChunk = this.archetypeChunks[fromArchetypeIndex];
         Holder<ECS_TYPE> holder = this.registry._internal_newEntityHolder();
         fromArchetypeChunk.removeEntity(this.entityChunkIndex[entityIndex], holder);
         T component = holder.getComponent(componentType);

         assert component != null;

         holder.removeComponent(componentType);
         int toArchetypeIndex = this.findOrCreateArchetypeChunk(holder.getArchetype());
         ArchetypeChunk<ECS_TYPE> toArchetypeChunk = this.archetypeChunks[toArchetypeIndex];
         int chunkEntityRef = toArchetypeChunk.addEntity(ref, holder);
         this.entityToArchetypeChunk[entityIndex] = toArchetypeIndex;
         this.entityChunkIndex[entityIndex] = chunkEntityRef;
         BitSet entityProcessedBySystemIndexes = this.archetypeChunkIndexesToSystemIndex[fromArchetypeIndex];
         ComponentRegistry.Data<ECS_TYPE> data = this.registry._internal_getData();
         BitSet systemIndexes = data.getSystemIndexesForType(this.registry.getRefChangeSystemType());
         int systemIndex = -1;

         while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
            if (entityProcessedBySystemIndexes.get(systemIndex)) {
               RefChangeSystem<ECS_TYPE, T> system = (RefChangeSystem<ECS_TYPE, T>)data.getSystem(systemIndex);
               if (system.componentType().getIndex() == componentType.getIndex()) {
                  system.onComponentRemoved(ref, component, this, commandBuffer);
               }
            }
         }

         if (fromArchetypeChunk.size() == 0) {
            this.removeArchetypeChunk(fromArchetypeIndex);
         }
      } finally {
         this.processing.unlock();
      }

      commandBuffer.consume();
   }

   @Override
   public <T extends Component<ECS_TYPE>> void tryRemoveComponent(@Nonnull Ref<ECS_TYPE> ref, @Nonnull ComponentType<ECS_TYPE, T> componentType) {
      this.removeComponentIfExists(ref, componentType);
   }

   public <T extends Component<ECS_TYPE>> boolean removeComponentIfExists(@Nonnull Ref<ECS_TYPE> ref, @Nonnull ComponentType<ECS_TYPE, T> componentType) {
      this.assertThread();
      this.assertWriteProcessing();
      ref.validate(this);
      componentType.validateRegistry(this.registry);
      componentType.validate();
      CommandBuffer<ECS_TYPE> commandBuffer = this.takeCommandBuffer();
      int entityIndex = ref.getIndex();
      int fromArchetypeIndex = this.entityToArchetypeChunk[entityIndex];
      this.processing.lock();

      boolean result;
      try {
         ArchetypeChunk<ECS_TYPE> fromArchetypeChunk = this.archetypeChunks[fromArchetypeIndex];
         if (!fromArchetypeChunk.getArchetype().contains(componentType)) {
            result = false;
         } else {
            Holder<ECS_TYPE> holder = this.registry._internal_newEntityHolder();
            fromArchetypeChunk.removeEntity(this.entityChunkIndex[entityIndex], holder);
            T component = holder.getComponent(componentType);

            assert component != null;

            holder.removeComponent(componentType);
            int toArchetypeIndex = this.findOrCreateArchetypeChunk(holder.getArchetype());
            ArchetypeChunk<ECS_TYPE> toArchetypeChunk = this.archetypeChunks[toArchetypeIndex];
            int chunkEntityRef = toArchetypeChunk.addEntity(ref, holder);
            this.entityToArchetypeChunk[entityIndex] = toArchetypeIndex;
            this.entityChunkIndex[entityIndex] = chunkEntityRef;
            BitSet entityProcessedBySystemIndexes = this.archetypeChunkIndexesToSystemIndex[fromArchetypeIndex];
            ComponentRegistry.Data<ECS_TYPE> data = this.registry._internal_getData();
            BitSet systemIndexes = data.getSystemIndexesForType(this.registry.getRefChangeSystemType());
            int systemIndex = -1;

            while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
               if (entityProcessedBySystemIndexes.get(systemIndex)) {
                  RefChangeSystem<ECS_TYPE, T> system = (RefChangeSystem<ECS_TYPE, T>)data.getSystem(systemIndex);
                  if (system.componentType().getIndex() == componentType.getIndex()) {
                     system.onComponentRemoved(ref, component, this, commandBuffer);
                  }
               }
            }

            if (fromArchetypeChunk.size() == 0) {
               this.removeArchetypeChunk(fromArchetypeIndex);
            }

            result = true;
         }
      } finally {
         this.processing.unlock();
      }

      commandBuffer.consume();
      return result;
   }

   public <T extends Resource<ECS_TYPE>> void replaceResource(@Nonnull ResourceType<ECS_TYPE, T> resourceType, @Nonnull T resource) {
      this.assertThread();
      resourceType.validateRegistry(this.registry);
      Objects.requireNonNull(resource);
      this.resources[resourceType.getIndex()] = resource;
   }

   @Nonnull
   @Override
   public <T extends Resource<ECS_TYPE>> T getResource(@Nonnull ResourceType<ECS_TYPE, T> resourceType) {
      resourceType.validateRegistry(this.registry);
      return (T)this.resources[resourceType.getIndex()];
   }

   @Nonnull
   protected <T extends Resource<ECS_TYPE>> T __internal_getResource(@Nonnull ResourceType<ECS_TYPE, T> resourceType) {
      resourceType.validateRegistry(this.registry);
      return (T)this.resources[resourceType.getIndex()];
   }

   public void forEachChunk(@Nonnull BiConsumer<ArchetypeChunk<ECS_TYPE>, CommandBuffer<ECS_TYPE>> consumer) {
      if (this.shutdown) {
         throw new IllegalStateException("Store is shutdown!");
      } else {
         this.assertThread();
         CommandBuffer<ECS_TYPE> commandBuffer = this.takeCommandBuffer();
         this.processing.lock();

         try {
            for (int archetypeIndex = 0; archetypeIndex < this.archetypeSize; archetypeIndex++) {
               ArchetypeChunk<ECS_TYPE> archetypeChunk = this.archetypeChunks[archetypeIndex];
               if (archetypeChunk != null) {
                  consumer.accept(archetypeChunk, commandBuffer);
               }
            }
         } finally {
            this.processing.unlock();
         }

         commandBuffer.consume();
      }
   }

   public boolean forEachChunk(@Nonnull BiPredicate<ArchetypeChunk<ECS_TYPE>, CommandBuffer<ECS_TYPE>> predicate) {
      if (this.shutdown) {
         throw new IllegalStateException("Store is shutdown!");
      } else {
         this.assertThread();
         CommandBuffer<ECS_TYPE> commandBuffer = this.takeCommandBuffer();
         boolean result = false;
         this.processing.lock();

         try {
            for (int archetypeIndex = 0; archetypeIndex < this.archetypeSize; archetypeIndex++) {
               ArchetypeChunk<ECS_TYPE> archetypeChunk = this.archetypeChunks[archetypeIndex];
               if (archetypeChunk != null && predicate.test(archetypeChunk, commandBuffer)) {
                  result = true;
                  break;
               }
            }
         } finally {
            this.processing.unlock();
         }

         commandBuffer.consume();
         return result;
      }
   }

   public void forEachChunk(Query<ECS_TYPE> query, @Nonnull BiConsumer<ArchetypeChunk<ECS_TYPE>, CommandBuffer<ECS_TYPE>> consumer) {
      if (this.shutdown) {
         throw new IllegalStateException("Store is shutdown!");
      } else {
         this.assertThread();
         CommandBuffer<ECS_TYPE> commandBuffer = this.takeCommandBuffer();
         this.processing.lock();

         try {
            if (query instanceof ExactArchetypeQuery<ECS_TYPE> exactQuery) {
               int archetypeIndex = this.archetypeToIndexMap.getInt(exactQuery.getArchetype());
               if (archetypeIndex != Integer.MIN_VALUE) {
                  consumer.accept(this.archetypeChunks[archetypeIndex], commandBuffer);
               }
            } else {
               for (int archetypeIndex = 0; archetypeIndex < this.archetypeSize; archetypeIndex++) {
                  ArchetypeChunk<ECS_TYPE> archetypeChunk = this.archetypeChunks[archetypeIndex];
                  if (archetypeChunk != null && query.test(archetypeChunk.getArchetype())) {
                     consumer.accept(this.archetypeChunks[archetypeIndex], commandBuffer);
                  }
               }
            }
         } finally {
            this.processing.unlock();
         }

         commandBuffer.consume();
      }
   }

   public boolean forEachChunk(Query<ECS_TYPE> query, @Nonnull BiPredicate<ArchetypeChunk<ECS_TYPE>, CommandBuffer<ECS_TYPE>> predicate) {
      if (this.shutdown) {
         throw new IllegalStateException("Store is shutdown!");
      } else {
         this.assertThread();
         CommandBuffer<ECS_TYPE> commandBuffer = this.takeCommandBuffer();
         boolean result = false;
         this.processing.lock();

         try {
            if (query instanceof ExactArchetypeQuery<ECS_TYPE> exactQuery) {
               int archetypeIndex = this.archetypeToIndexMap.getInt(exactQuery.getArchetype());
               if (archetypeIndex != Integer.MIN_VALUE) {
                  result = predicate.test(this.archetypeChunks[archetypeIndex], commandBuffer);
               }
            } else {
               for (int archetypeIndex = 0; archetypeIndex < this.archetypeSize; archetypeIndex++) {
                  ArchetypeChunk<ECS_TYPE> archetypeChunk = this.archetypeChunks[archetypeIndex];
                  if (archetypeChunk != null
                     && query.test(archetypeChunk.getArchetype())
                     && predicate.test(this.archetypeChunks[archetypeIndex], commandBuffer)) {
                     result = true;
                     break;
                  }
               }
            }
         } finally {
            this.processing.unlock();
         }

         commandBuffer.consume();
         return result;
      }
   }

   public void forEachChunk(int systemIndex, @Nonnull BiConsumer<ArchetypeChunk<ECS_TYPE>, CommandBuffer<ECS_TYPE>> consumer) {
      if (this.shutdown) {
         throw new IllegalStateException("Store is shutdown!");
      } else {
         this.assertThread();
         CommandBuffer<ECS_TYPE> commandBuffer = this.takeCommandBuffer();
         this.processing.lock();

         try {
            BitSet indexes = this.systemIndexToArchetypeChunkIndexes[systemIndex];
            int index = -1;

            while ((index = indexes.nextSetBit(index + 1)) >= 0) {
               consumer.accept(this.archetypeChunks[index], commandBuffer);
            }
         } finally {
            this.processing.unlock();
         }

         commandBuffer.consume();
      }
   }

   public boolean forEachChunk(int systemIndex, @Nonnull BiPredicate<ArchetypeChunk<ECS_TYPE>, CommandBuffer<ECS_TYPE>> predicate) {
      if (this.shutdown) {
         throw new IllegalStateException("Store is shutdown!");
      } else {
         this.assertThread();
         CommandBuffer<ECS_TYPE> commandBuffer = this.takeCommandBuffer();
         boolean result = false;
         this.processing.lock();

         try {
            BitSet indexes = this.systemIndexToArchetypeChunkIndexes[systemIndex];
            int index = -1;

            while ((index = indexes.nextSetBit(index + 1)) >= 0) {
               if (predicate.test(this.archetypeChunks[index], commandBuffer)) {
                  result = true;
                  break;
               }
            }
         } finally {
            this.processing.unlock();
         }

         commandBuffer.consume();
         return result;
      }
   }

   public void forEachEntityParallel(IntBiObjectConsumer<ArchetypeChunk<ECS_TYPE>, CommandBuffer<ECS_TYPE>> consumer) {
      if (this.shutdown) {
         throw new IllegalStateException("Store is shutdown!");
      } else {
         this.assertThread();
         CommandBuffer<ECS_TYPE> commandBuffer = this.takeCommandBuffer();
         this.forEachTask.init();
         this.processing.lock();

         try {
            for (int archetypeIndex = 0; archetypeIndex < this.archetypeSize; archetypeIndex++) {
               ArchetypeChunk<ECS_TYPE> archetypeChunk = this.archetypeChunks[archetypeIndex];
               if (archetypeChunk != null) {
                  int size = archetypeChunk.size();
                  if (size != 0) {
                     ParallelRangeTask<ForEachTaskData<ECS_TYPE>> systemTask = this.forEachTask.appendTask();
                     systemTask.init(0, size);
                     int i = 0;

                     for (int systemTaskSize = systemTask.size(); i < systemTaskSize; i++) {
                        systemTask.get(i).init(consumer, archetypeChunk, commandBuffer.fork());
                     }
                  }
               }
            }

            ForEachTaskData.invokeParallelTask(this.forEachTask, commandBuffer);
         } finally {
            this.processing.unlock();
         }

         commandBuffer.consume();
      }
   }

   public void forEachEntityParallel(Query<ECS_TYPE> query, IntBiObjectConsumer<ArchetypeChunk<ECS_TYPE>, CommandBuffer<ECS_TYPE>> consumer) {
      if (this.shutdown) {
         throw new IllegalStateException("Store is shutdown!");
      } else {
         this.assertThread();
         CommandBuffer<ECS_TYPE> commandBuffer = this.takeCommandBuffer();
         this.forEachTask.init();
         this.processing.lock();

         try {
            if (query instanceof ExactArchetypeQuery<ECS_TYPE> exactQuery) {
               int archetypeIndex = this.archetypeToIndexMap.getInt(exactQuery.getArchetype());
               if (archetypeIndex != Integer.MIN_VALUE) {
                  ArchetypeChunk<ECS_TYPE> archetypeChunk = this.archetypeChunks[archetypeIndex];
                  int archetypeChunkSize = archetypeChunk.size();
                  if (archetypeChunkSize != 0) {
                     ParallelRangeTask<ForEachTaskData<ECS_TYPE>> systemTask = this.forEachTask.appendTask();
                     systemTask.init(0, archetypeChunkSize);
                     int i = 0;

                     for (int systemSize = systemTask.size(); i < systemSize; i++) {
                        systemTask.get(i).init(consumer, archetypeChunk, commandBuffer.fork());
                     }
                  }
               }
            } else {
               for (int archetypeIndex = 0; archetypeIndex < this.archetypeSize; archetypeIndex++) {
                  ArchetypeChunk<ECS_TYPE> archetypeChunk = this.archetypeChunks[archetypeIndex];
                  if (archetypeChunk != null && query.test(archetypeChunk.getArchetype())) {
                     int archetypeChunkSize = archetypeChunk.size();
                     if (archetypeChunkSize != 0) {
                        ParallelRangeTask<ForEachTaskData<ECS_TYPE>> systemTask = this.forEachTask.appendTask();
                        systemTask.init(0, archetypeChunkSize);
                        int i = 0;

                        for (int systemTaskSize = systemTask.size(); i < systemTaskSize; i++) {
                           systemTask.get(i).init(consumer, archetypeChunk, commandBuffer.fork());
                        }
                     }
                  }
               }
            }

            ForEachTaskData.invokeParallelTask(this.forEachTask, commandBuffer);
         } finally {
            this.processing.unlock();
         }

         commandBuffer.consume();
      }
   }

   public <T extends ArchetypeDataSystem<ECS_TYPE, Q, R>, Q, R> void fetch(@Nonnull SystemType<ECS_TYPE, T> systemType, Q query, @Nonnull List<R> results) {
      if (this.shutdown) {
         throw new IllegalStateException("Store is shutdown!");
      } else {
         this.assertThread();
         ComponentRegistry.Data<ECS_TYPE> data = this.registry._internal_getData();
         CommandBuffer<ECS_TYPE> commandBuffer = this.takeCommandBuffer();
         this.fetchTask.init();
         BitSet systemIndexes = data.getSystemIndexesForType(systemType);
         this.processing.lock();

         try {
            int systemIndex = -1;

            while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
               T system = (T)data.getSystem(systemIndex, systemType);
               BitSet indexes = this.systemIndexToArchetypeChunkIndexes[systemIndex];
               int index = -1;

               while ((index = indexes.nextSetBit(index + 1)) >= 0) {
                  system.fetch(this.archetypeChunks[index], this, commandBuffer, query, results);
               }
            }

            EntityDataSystem.SystemTaskData.invokeParallelTask(this.fetchTask, commandBuffer, results);
         } finally {
            this.processing.unlock();
         }

         commandBuffer.consume();
      }
   }

   public <T extends EntityDataSystem<ECS_TYPE, Q, R>, Q, R> void fetch(
      @Nonnull Collection<Ref<ECS_TYPE>> refs, @Nonnull SystemType<ECS_TYPE, T> systemType, @Nonnull Q query, @Nonnull List<R> results
   ) {
      if (this.shutdown) {
         throw new IllegalStateException("Store is shutdown!");
      } else {
         this.assertThread();
         ComponentRegistry.Data<ECS_TYPE> data = this.registry._internal_getData();
         CommandBuffer<ECS_TYPE> commandBuffer = this.takeCommandBuffer();
         this.fetchTask.init();
         BitSet systemIndexes = data.getSystemIndexesForType(systemType);
         this.processing.lock();

         try {
            int systemIndex = -1;

            while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
               T system = (T)data.getSystem(systemIndex, systemType);

               for (Ref<ECS_TYPE> ref : refs) {
                  int entityIndex = ref.getIndex();
                  int archetypeIndex = this.entityToArchetypeChunk[entityIndex];
                  BitSet entityProcessedBySystemIndexes = this.archetypeChunkIndexesToSystemIndex[archetypeIndex];
                  if (entityProcessedBySystemIndexes.get(systemIndex)) {
                     int index = this.entityChunkIndex[entityIndex];
                     ArchetypeChunk<ECS_TYPE> archetypeChunk = this.archetypeChunks[archetypeIndex];
                     system.fetch(index, archetypeChunk, this, commandBuffer, query, results);
                  }
               }
            }

            EntityDataSystem.SystemTaskData.invokeParallelTask(this.fetchTask, commandBuffer, results);
         } finally {
            this.processing.unlock();
         }

         commandBuffer.consume();
      }
   }

   @Override
   public <Event extends EcsEvent> void invoke(@Nonnull Ref<ECS_TYPE> ref, @Nonnull Event param) {
      EntityEventType<ECS_TYPE, ?> eventType = this.registry.getEntityEventTypeForClass(param.getClass());
      if (eventType != null) {
         ((Store<ECS_TYPE>)this).invoke(eventType, ref, param);
      }
   }

   @Override
   public <Event extends EcsEvent> void invoke(@Nonnull EntityEventType<ECS_TYPE, Event> systemType, @Nonnull Ref<ECS_TYPE> ref, @Nonnull Event param) {
      if (this.shutdown) {
         throw new IllegalStateException("Store is shutdown!");
      } else {
         this.assertThread();
         ComponentRegistry.Data<ECS_TYPE> data = this.registry._internal_getData();
         CommandBuffer<ECS_TYPE> commandBuffer = this.takeCommandBuffer();
         commandBuffer.track(ref);
         BitSet systemIndexes = data.getSystemIndexesForType(systemType);
         this.processing.lock();

         try {
            int entityIndex = ref.getIndex();
            int archetypeIndex = this.entityToArchetypeChunk[entityIndex];
            BitSet entityProcessedBySystemIndexes = this.archetypeChunkIndexesToSystemIndex[archetypeIndex];
            ArchetypeChunk<ECS_TYPE> archetypeChunk = this.archetypeChunks[archetypeIndex];
            int index = this.entityChunkIndex[entityIndex];
            int systemIndex = -1;

            while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
               systemIndex = entityProcessedBySystemIndexes.nextSetBit(systemIndex);
               if (systemIndex < 0) {
                  break;
               }

               if (systemIndexes.get(systemIndex)) {
                  EntityEventSystem<ECS_TYPE, Event> system = data.getSystem(systemIndex, systemType);
                  system.handleInternal(index, archetypeChunk, this, commandBuffer, param);
                  if (commandBuffer.consumeWasTrackedRefRemoved()) {
                     break;
                  }
               }
            }
         } finally {
            this.processing.unlock();
         }

         commandBuffer.consume();
      }
   }

   @Override
   public <Event extends EcsEvent> void invoke(@Nonnull Event param) {
      WorldEventType<ECS_TYPE, ?> eventType = this.registry.getWorldEventTypeForClass(param.getClass());
      if (eventType != null) {
         ((Store<ECS_TYPE>)this).invoke(eventType, param);
      }
   }

   @Override
   public <Event extends EcsEvent> void invoke(@Nonnull WorldEventType<ECS_TYPE, Event> systemType, @Nonnull Event param) {
      if (this.shutdown) {
         throw new IllegalStateException("Store is shutdown!");
      } else {
         this.assertThread();
         ComponentRegistry.Data<ECS_TYPE> data = this.registry._internal_getData();
         CommandBuffer<ECS_TYPE> commandBuffer = this.takeCommandBuffer();
         BitSet systemIndexes = data.getSystemIndexesForType(systemType);
         this.processing.lock();

         try {
            int systemIndex = -1;

            while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
               WorldEventSystem<ECS_TYPE, Event> system = data.getSystem(systemIndex, systemType);
               system.handleInternal(this, commandBuffer, param);
            }
         } finally {
            this.processing.unlock();
         }

         commandBuffer.consume();
      }
   }

   protected <Event extends EcsEvent> void internal_invoke(CommandBuffer<ECS_TYPE> sourceCommandBuffer, Ref<ECS_TYPE> ref, Event param) {
      EntityEventType<ECS_TYPE, ?> eventType = this.registry.getEntityEventTypeForClass(param.getClass());
      if (eventType != null) {
         ((Store<ECS_TYPE>)this).internal_invoke(sourceCommandBuffer, eventType, ref, param);
      }
   }

   protected <Event extends EcsEvent> void internal_invoke(
      CommandBuffer<ECS_TYPE> sourceCommandBuffer, @Nonnull EntityEventType<ECS_TYPE, Event> systemType, Ref<ECS_TYPE> ref, Event param
   ) {
      ComponentRegistry.Data<ECS_TYPE> data = this.registry._internal_getData();
      CommandBuffer<ECS_TYPE> commandBuffer = sourceCommandBuffer.fork();
      commandBuffer.track(ref);
      BitSet systemIndexes = data.getSystemIndexesForType(systemType);
      int entityIndex = ref.getIndex();
      int archetypeIndex = this.entityToArchetypeChunk[entityIndex];
      BitSet entityProcessedBySystemIndexes = this.archetypeChunkIndexesToSystemIndex[archetypeIndex];
      ArchetypeChunk<ECS_TYPE> archetypeChunk = this.archetypeChunks[archetypeIndex];
      int index = this.entityChunkIndex[entityIndex];
      int systemIndex = -1;

      while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
         systemIndex = entityProcessedBySystemIndexes.nextSetBit(systemIndex);
         if (systemIndex < 0) {
            break;
         }

         if (systemIndexes.get(systemIndex)) {
            EntityEventSystem<ECS_TYPE, Event> system = data.getSystem(systemIndex, systemType);
            system.handleInternal(index, archetypeChunk, this, commandBuffer, param);
            if (commandBuffer.consumeWasTrackedRefRemoved()) {
               break;
            }
         }
      }

      commandBuffer.mergeParallel(sourceCommandBuffer);
   }

   protected <Event extends EcsEvent> void internal_invoke(CommandBuffer<ECS_TYPE> sourceCommandBuffer, Event param) {
      WorldEventType<ECS_TYPE, ?> eventType = this.registry.getWorldEventTypeForClass(param.getClass());
      if (eventType != null) {
         ((Store<ECS_TYPE>)this).internal_invoke(sourceCommandBuffer, eventType, param);
      }
   }

   protected <Event extends EcsEvent> void internal_invoke(
      CommandBuffer<ECS_TYPE> sourceCommandBuffer, @Nonnull WorldEventType<ECS_TYPE, Event> systemType, Event param
   ) {
      ComponentRegistry.Data<ECS_TYPE> data = this.registry._internal_getData();
      BitSet systemIndexes = data.getSystemIndexesForType(systemType);
      int systemIndex = -1;

      while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
         WorldEventSystem<ECS_TYPE, Event> system = data.getSystem(systemIndex, systemType);
         system.handleInternal(this, sourceCommandBuffer, param);
      }
   }

   public void tick(float dt) {
      this.tickInternal(dt, this.registry.getTickingSystemType());
   }

   public void pausedTick(float dt) {
      if (this.shutdown) {
         throw new IllegalStateException("Store is shutdown!");
      } else {
         this.assertThread();
         this.tickInternal(dt, this.registry.getRunWhenPausedSystemType());
      }
   }

   private <Tickable extends TickableSystem<ECS_TYPE>> void tickInternal(float dt, SystemType<ECS_TYPE, Tickable> tickingSystemType) {
      if (this.shutdown) {
         throw new IllegalStateException("Store is shutdown!");
      } else {
         this.assertThread();
         this.registry.getDataUpdateLock().readLock().lock();

         try {
            ComponentRegistry.Data<ECS_TYPE> data = this.registry.doDataUpdate();
            BitSet systemIndexes = data.getSystemIndexesForType(tickingSystemType);
            int systemIndex = -1;

            while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
               Tickable tickingSystem = (Tickable)data.getSystem(systemIndex, tickingSystemType);
               long start = System.nanoTime();
               tickingSystem.tick(dt, systemIndex, this);
               long end = System.nanoTime();
               this.systemMetrics[systemIndex].add(end, end - start);
            }
         } finally {
            this.registry.getDataUpdateLock().readLock().unlock();
         }
      }
   }

   public void tick(ArchetypeTickingSystem<ECS_TYPE> system, float dt, int systemIndex) {
      if (this.shutdown) {
         throw new IllegalStateException("Store is shutdown!");
      } else {
         this.assertThread();
         CommandBuffer<ECS_TYPE> commandBuffer = this.takeCommandBuffer();
         this.parallelTask.init();
         boolean oldDisableProcessingAssert = this.disableProcessingAssert;
         this.disableProcessingAssert = system instanceof DisableProcessingAssert;
         this.processing.lock();

         try {
            BitSet indexes = this.systemIndexToArchetypeChunkIndexes[systemIndex];
            int index = -1;

            while ((index = indexes.nextSetBit(index + 1)) >= 0) {
               system.tick(dt, this.archetypeChunks[index], this, commandBuffer);
            }

            EntityTickingSystem.SystemTaskData.invokeParallelTask(this.parallelTask, commandBuffer);
         } finally {
            this.processing.unlock();
            this.disableProcessingAssert = oldDisableProcessingAssert;
         }

         commandBuffer.consume();
      }
   }

   void updateData(@Nonnull ComponentRegistry.Data<ECS_TYPE> oldData, @Nonnull ComponentRegistry.Data<ECS_TYPE> data) {
      if (this.shutdown) {
         throw new IllegalStateException("Store is shutdown!");
      } else {
         int resourceSize = data.getResourceSize();
         this.resources = Arrays.copyOf(this.resources, resourceSize);

         for (int index = 0; index < this.resources.length; index++) {
            ResourceType<ECS_TYPE, ? extends Resource<ECS_TYPE>> resourceType = (ResourceType<ECS_TYPE, ? extends Resource<ECS_TYPE>>)data.getResourceType(
               index
            );
            if (this.resources[index] == null && resourceType != null) {
               this.resources[index] = (Resource<ECS_TYPE>)this.resourceStorage.load(this, data, resourceType).join();
            } else if (this.resources[index] != null && resourceType == null) {
               this.resources[index] = null;
            }
         }

         boolean systemChanged = false;

         for (int i = 0; i < data.getDataChangeCount(); i++) {
            DataChange dataChange = data.getDataChange(i);
            systemChanged |= dataChange instanceof SystemChange;
            this.updateData(oldData, data, dataChange);
         }

         HistoricMetric[] oldSystemMetrics = this.systemMetrics;
         this.systemMetrics = new HistoricMetric[data.getSystemSize()];
         SystemType<ECS_TYPE, TickableSystem<ECS_TYPE>> tickingSystemType = this.registry.getTickableSystemType();
         BitSet systemIndexes = data.getSystemIndexesForType(tickingSystemType);
         int systemIndex = -1;

         while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
            ISystem<ECS_TYPE> system = data.getSystem(systemIndex);
            int oldSystemIndex = oldData.indexOf(system);
            if (oldSystemIndex >= 0) {
               this.systemMetrics[systemIndex] = oldSystemMetrics[oldSystemIndex];
            } else {
               this.systemMetrics[systemIndex] = HistoricMetric.builder(33333333L, TimeUnit.NANOSECONDS)
                  .addPeriod(1L, TimeUnit.SECONDS)
                  .addPeriod(1L, TimeUnit.MINUTES)
                  .addPeriod(5L, TimeUnit.MINUTES)
                  .build();
            }
         }

         if (systemChanged) {
            this.updateArchetypeIndexes(data);
         }
      }
   }

   private void updateData(@Nonnull ComponentRegistry.Data<ECS_TYPE> oldData, @Nonnull ComponentRegistry.Data<ECS_TYPE> newData, DataChange dataChange) {
      this.processing.lock();

      try {
         this.updateData0(oldData, newData, dataChange);
      } finally {
         this.processing.unlock();
      }

      if (dataChange instanceof SystemChange<ECS_TYPE> systemChange) {
         ISystem<ECS_TYPE> system = systemChange.getSystem();
         switch (systemChange.getType()) {
            case REGISTERED:
               if (system instanceof StoreSystem) {
                  ((StoreSystem)system).onSystemAddedToStore(this);
               }
               break;
            case UNREGISTERED:
               if (system instanceof StoreSystem) {
                  ((StoreSystem)system).onSystemRemovedFromStore(this);
               }
         }
      }
   }

   private void updateData0(@Nonnull ComponentRegistry.Data<ECS_TYPE> oldData, @Nonnull ComponentRegistry.Data<ECS_TYPE> newData, DataChange dataChange) {
      if (dataChange instanceof ComponentChange<ECS_TYPE, ? extends Component<ECS_TYPE>> componentChange) {
         ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType = componentChange.getComponentType();
         ComponentType<ECS_TYPE, UnknownComponents<ECS_TYPE>> unknownComponentType = this.registry.getUnknownComponentType();
         switch (componentChange.getType()) {
            case REGISTERED:
               String componentId = newData.getComponentId(componentType);
               Codec<Component<ECS_TYPE>> componentCodec = newData.getComponentCodec((ComponentType<ECS_TYPE, Component<ECS_TYPE>>)componentType);
               if (componentCodec != null) {
                  Holder<ECS_TYPE> tempInternalEntityHolder = this.registry._internal_newEntityHolder();
                  int oldArchetypeSize = this.archetypeSize;

                  for (int archetypeIndexx = 0; archetypeIndexx < oldArchetypeSize; archetypeIndexx++) {
                     ArchetypeChunk<ECS_TYPE> archetypeChunk = this.archetypeChunks[archetypeIndexx];
                     if (archetypeChunk != null) {
                        Archetype<ECS_TYPE> archetype = archetypeChunk.getArchetype();
                        if (!archetype.contains(componentType) && archetype.contains(unknownComponentType)) {
                           Archetype<ECS_TYPE> newArchetype = Archetype.add(archetype, componentType);
                           int toArchetypeIndex = this.findOrCreateArchetypeChunk(newArchetype);
                           ArchetypeChunk<ECS_TYPE> toArchetypeChunk = this.archetypeChunks[toArchetypeIndex];
                           archetypeChunk.transferSomeTo(tempInternalEntityHolder, toArchetypeChunk, index -> {
                              UnknownComponents<ECS_TYPE> unknownComponents = archetypeChunk.getComponent(index, unknownComponentType);

                              assert unknownComponents != null;

                              return unknownComponents.contains(componentId);
                           }, entity -> {
                              UnknownComponents<ECS_TYPE> unknownComponents = entity.getComponent(unknownComponentType);

                              assert unknownComponents != null;

                              Component<ECS_TYPE> component = unknownComponents.removeComponent(componentId, componentCodec);
                              entity.addComponent(componentType, component);
                           }, (newChunkEntityRef, ref) -> {
                              this.entityToArchetypeChunk[ref.getIndex()] = toArchetypeIndex;
                              this.entityChunkIndex[ref.getIndex()] = newChunkEntityRef;
                           });
                           if (archetypeChunk.size() == 0) {
                              this.archetypeToIndexMap.removeInt(this.archetypeChunks[archetypeIndexx].getArchetype());
                              this.archetypeChunks[archetypeIndexx] = null;

                              for (int systemIndex = 0; systemIndex < oldData.getSystemSize(); systemIndex++) {
                                 this.systemIndexToArchetypeChunkIndexes[systemIndex].clear(archetypeIndexx);
                              }

                              this.archetypeChunkIndexesToSystemIndex[archetypeIndexx].clear();
                              this.archetypeChunkReuse.set(archetypeIndexx);
                           }

                           if (toArchetypeChunk.size() == 0) {
                              this.archetypeToIndexMap.removeInt(this.archetypeChunks[toArchetypeIndex].getArchetype());
                              this.archetypeChunks[toArchetypeIndex] = null;

                              for (int systemIndex = 0; systemIndex < oldData.getSystemSize(); systemIndex++) {
                                 this.systemIndexToArchetypeChunkIndexes[systemIndex].clear(toArchetypeIndex);
                              }

                              this.archetypeChunkIndexesToSystemIndex[toArchetypeIndex].clear();
                              this.archetypeChunkReuse.set(toArchetypeIndex);
                           }
                        }
                     }
                  }
               }
               break;
            case UNREGISTERED:
               Holder<ECS_TYPE> tempInternalEntityHolder = this.registry._internal_newEntityHolder();
               String componentId = oldData.getComponentId(componentType);
               Codec<Component<ECS_TYPE>> componentCodec = oldData.getComponentCodec((ComponentType<ECS_TYPE, Component<ECS_TYPE>>)componentType);
               int oldArchetypeSize = this.archetypeSize;

               for (int archetypeIndex = 0; archetypeIndex < oldArchetypeSize; archetypeIndex++) {
                  ArchetypeChunk<ECS_TYPE> archetypeChunk = this.archetypeChunks[archetypeIndex];
                  if (archetypeChunk != null) {
                     Archetype<ECS_TYPE> archetype = archetypeChunk.getArchetype();
                     if (archetype.contains(componentType)) {
                        this.archetypeToIndexMap.removeInt(this.archetypeChunks[archetypeIndex].getArchetype());
                        this.archetypeChunks[archetypeIndex] = null;

                        for (int systemIndex = 0; systemIndex < oldData.getSystemSize(); systemIndex++) {
                           this.systemIndexToArchetypeChunkIndexes[systemIndex].clear(archetypeIndex);
                        }

                        this.archetypeChunkIndexesToSystemIndex[archetypeIndex].clear();
                        this.archetypeChunkReuse.set(archetypeIndex);
                        Archetype<ECS_TYPE> newArchetype = Archetype.remove(archetype, componentType);
                        if (componentCodec != null && !newArchetype.contains(unknownComponentType)) {
                           newArchetype = Archetype.add(newArchetype, unknownComponentType);
                        }

                        int toArchetypeIndex = this.findOrCreateArchetypeChunk(newArchetype);
                        ArchetypeChunk<ECS_TYPE> toArchetypeChunk = this.archetypeChunks[toArchetypeIndex];
                        archetypeChunk.transferTo(tempInternalEntityHolder, toArchetypeChunk, entity -> {
                           if (componentCodec != null) {
                              UnknownComponents<ECS_TYPE> unknownComponents;
                              if (entity.getArchetype().contains(unknownComponentType)) {
                                 unknownComponents = entity.getComponent(unknownComponentType);

                                 assert unknownComponents != null;
                              } else {
                                 unknownComponents = new UnknownComponents<>();
                                 entity.addComponent(unknownComponentType, unknownComponents);
                              }

                              Component<ECS_TYPE> component = entity.getComponent((ComponentType<ECS_TYPE, Component<ECS_TYPE>>)componentType);
                              unknownComponents.addComponent(componentId, component, componentCodec);
                           }

                           entity.removeComponent(componentType);
                        }, (newChunkEntityRef, ref) -> {
                           this.entityToArchetypeChunk[ref.getIndex()] = toArchetypeIndex;
                           this.entityChunkIndex[ref.getIndex()] = newChunkEntityRef;
                        });
                     }
                  }
               }

               int highestUsedIndex = this.archetypeChunkReuse.previousClearBit(oldArchetypeSize - 1);
               this.archetypeSize = highestUsedIndex + 1;
               this.archetypeChunkReuse.clear(this.archetypeSize, oldArchetypeSize);
         }
      } else if (dataChange instanceof SystemChange<ECS_TYPE> systemChange) {
         ISystem<ECS_TYPE> system = systemChange.getSystem();
         switch (systemChange.getType()) {
            case REGISTERED:
               if (system instanceof ArchetypeChunkSystem<ECS_TYPE> archetypeChunkSystem) {
                  for (int archetypeIndexxxx = 0; archetypeIndexxxx < this.archetypeSize; archetypeIndexxxx++) {
                     ArchetypeChunk<ECS_TYPE> archetypeChunk = this.archetypeChunks[archetypeIndexxxx];
                     if (archetypeChunk != null && archetypeChunkSystem.test(this.registry, archetypeChunk.getArchetype())) {
                        archetypeChunkSystem.onSystemAddedToArchetypeChunk(archetypeChunk);
                     }
                  }
               }
               break;
            case UNREGISTERED:
               if (system instanceof ArchetypeChunkSystem<ECS_TYPE> archetypeChunkSystem) {
                  for (int archetypeIndexxx = 0; archetypeIndexxx < this.archetypeSize; archetypeIndexxx++) {
                     ArchetypeChunk<ECS_TYPE> archetypeChunk = this.archetypeChunks[archetypeIndexxx];
                     if (archetypeChunk != null && archetypeChunkSystem.test(this.registry, archetypeChunk.getArchetype())) {
                        archetypeChunkSystem.onSystemRemovedFromArchetypeChunk(archetypeChunk);
                     }
                  }
               }
         }
      }
   }

   private void updateArchetypeIndexes(@Nonnull ComponentRegistry.Data<ECS_TYPE> data) {
      int systemSize = data.getSystemSize();
      int oldLength = this.systemIndexToArchetypeChunkIndexes.length;
      if (oldLength < systemSize) {
         this.systemIndexToArchetypeChunkIndexes = Arrays.copyOf(this.systemIndexToArchetypeChunkIndexes, systemSize);

         for (int i = oldLength; i < systemSize; i++) {
            this.systemIndexToArchetypeChunkIndexes[i] = new BitSet(this.archetypeSize);
         }
      }

      for (int systemIndex = 0; systemIndex < oldLength; systemIndex++) {
         this.systemIndexToArchetypeChunkIndexes[systemIndex].clear();
      }

      for (int archetypeIndex = 0; archetypeIndex < this.archetypeSize; archetypeIndex++) {
         this.archetypeChunkIndexesToSystemIndex[archetypeIndex].clear();
      }

      SystemType<ECS_TYPE, QuerySystem<ECS_TYPE>> entityQuerySystemType = this.registry.getQuerySystemType();
      BitSet systemIndexes = data.getSystemIndexesForType(entityQuerySystemType);
      int systemIndex = -1;

      while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
         QuerySystem<ECS_TYPE> system = data.getSystem(systemIndex, entityQuerySystemType);
         BitSet archetypeChunkIndexes = this.systemIndexToArchetypeChunkIndexes[systemIndex];

         for (int archetypeIndex = 0; archetypeIndex < this.archetypeSize; archetypeIndex++) {
            ArchetypeChunk<ECS_TYPE> archetypeChunk = this.archetypeChunks[archetypeIndex];
            if (archetypeChunk != null && system.test(this.registry, archetypeChunk.getArchetype())) {
               archetypeChunkIndexes.set(archetypeIndex);
               this.archetypeChunkIndexesToSystemIndex[archetypeIndex].set(systemIndex);
            }
         }
      }
   }

   public void assertWriteProcessing() {
      if (this.processing.isHeld() && !this.disableProcessingAssert) {
         throw new IllegalStateException("Store is currently processing! Ensure you aren't calling a store method from a system.");
      }
   }

   @Deprecated
   public boolean isProcessing() {
      return this.processing.isHeld();
   }

   public void assertThread() {
      Thread currentThread = Thread.currentThread();
      if (!currentThread.equals(this.thread) && this.thread.isAlive()) {
         throw new IllegalStateException("Assert not in thread! " + this.thread + " but was in " + currentThread);
      }
   }

   public boolean isInThread() {
      return Thread.currentThread().equals(this.thread);
   }

   public boolean isAliveInDifferentThread() {
      return this.thread.isAlive() && !Thread.currentThread().equals(this.thread);
   }

   @Nonnull
   @Override
   public String toString() {
      return "Store{super()="
         + this.getClass()
         + "@"
         + this.hashCode()
         + ", registry="
         + this.registry.getClass()
         + "@"
         + this.registry.hashCode()
         + ", shutdown="
         + this.shutdown
         + ", storeIndex="
         + this.storeIndex
         + ", systemIndexToArchetypeChunkIndexes="
         + Arrays.toString((Object[])this.systemIndexToArchetypeChunkIndexes)
         + ", archetypeSize="
         + this.archetypeSize
         + ", archetypeChunks="
         + Arrays.toString((Object[])this.archetypeChunks)
         + "}";
   }

   private <T extends Component<ECS_TYPE>> void datachunk_addComponent(
      @Nonnull Ref<ECS_TYPE> ref,
      int fromArchetypeIndex,
      @Nonnull ComponentType<ECS_TYPE, T> componentType,
      @Nonnull T component,
      @Nonnull CommandBuffer<ECS_TYPE> commandBuffer
   ) {
      int entityIndex = ref.getIndex();
      ArchetypeChunk<ECS_TYPE> fromArchetypeChunk = this.archetypeChunks[fromArchetypeIndex];
      int oldChunkEntityRef = this.entityChunkIndex[entityIndex];
      Holder<ECS_TYPE> holder = this.registry._internal_newEntityHolder();
      fromArchetypeChunk.removeEntity(oldChunkEntityRef, holder);
      holder.addComponent(componentType, component);
      int toArchetypeIndex = this.findOrCreateArchetypeChunk(holder.getArchetype());
      ArchetypeChunk<ECS_TYPE> toArchetypeChunk = this.archetypeChunks[toArchetypeIndex];
      int chunkEntityRef = toArchetypeChunk.addEntity(ref, holder);
      this.entityToArchetypeChunk[entityIndex] = toArchetypeIndex;
      this.entityChunkIndex[entityIndex] = chunkEntityRef;
      BitSet entityProcessedBySystemIndexes = this.archetypeChunkIndexesToSystemIndex[toArchetypeIndex];
      ComponentRegistry.Data<ECS_TYPE> data = this.registry._internal_getData();
      BitSet systemIndexes = data.getSystemIndexesForType(this.registry.getRefChangeSystemType());
      int systemIndex = -1;

      while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
         if (entityProcessedBySystemIndexes.get(systemIndex)) {
            RefChangeSystem<ECS_TYPE, T> system = (RefChangeSystem<ECS_TYPE, T>)data.getSystem(systemIndex);
            if (system.componentType().getIndex() == componentType.getIndex()) {
               system.onComponentAdded(ref, component, this, commandBuffer);
            }
         }
      }

      if (fromArchetypeChunk.size() == 0) {
         this.removeArchetypeChunk(fromArchetypeIndex);
      }
   }

   private int findOrCreateArchetypeChunk(@Nonnull Archetype<ECS_TYPE> archetype) {
      int archetypeIndex = this.archetypeToIndexMap.getInt(archetype);
      if (archetypeIndex != Integer.MIN_VALUE) {
         return archetypeIndex;
      } else {
         ComponentRegistry.Data<ECS_TYPE> data = this.registry._internal_getData();
         if (this.archetypeChunkReuse.isEmpty()) {
            archetypeIndex = this.archetypeSize++;
         } else {
            archetypeIndex = this.archetypeChunkReuse.nextSetBit(0);
            this.archetypeChunkReuse.clear(archetypeIndex);
         }

         int oldLength = this.archetypeChunks.length;
         if (oldLength <= archetypeIndex) {
            int newLength = ArrayUtil.grow(archetypeIndex);
            this.archetypeChunks = Arrays.copyOf(this.archetypeChunks, newLength);
            this.archetypeChunkIndexesToSystemIndex = Arrays.copyOf(this.archetypeChunkIndexesToSystemIndex, newLength);
            int systemSize = data.getSystemSize();

            for (int i = oldLength; i < newLength; i++) {
               this.archetypeChunkIndexesToSystemIndex[i] = new BitSet(systemSize);
            }
         }

         ArchetypeChunk<ECS_TYPE> archetypeChunk = new ArchetypeChunk<>(this, archetype);
         this.archetypeChunks[archetypeIndex] = archetypeChunk;
         this.archetypeToIndexMap.put(archetype, archetypeIndex);
         BitSet archetypeChunkToSystemIndex = this.archetypeChunkIndexesToSystemIndex[archetypeIndex];
         SystemType<ECS_TYPE, QuerySystem<ECS_TYPE>> entityQuerySystemType = this.registry.getQuerySystemType();
         BitSet systemIndexes = data.getSystemIndexesForType(entityQuerySystemType);
         int systemIndex = -1;

         while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
            QuerySystem<ECS_TYPE> system = data.getSystem(systemIndex, entityQuerySystemType);
            if (system.test(this.registry, archetype)) {
               this.systemIndexToArchetypeChunkIndexes[systemIndex].set(archetypeIndex);
               archetypeChunkToSystemIndex.set(systemIndex);
               if (system instanceof ArchetypeChunkSystem) {
                  ((ArchetypeChunkSystem)system).onSystemAddedToArchetypeChunk(archetypeChunk);
               }
            }
         }

         return archetypeIndex;
      }
   }

   private void removeArchetypeChunk(int archetypeIndex) {
      ArchetypeChunk<ECS_TYPE> archetypeChunk = this.archetypeChunks[archetypeIndex];
      Archetype<ECS_TYPE> archetype = archetypeChunk.getArchetype();
      this.archetypeToIndexMap.removeInt(archetype);
      this.archetypeChunks[archetypeIndex] = null;
      this.archetypeChunkIndexesToSystemIndex[archetypeIndex].clear();
      ComponentRegistry.Data<ECS_TYPE> data = this.registry._internal_getData();
      BitSet systemIndexes = data.getSystemIndexesForType(this.registry.getQuerySystemType());
      int systemIndex = -1;

      while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
         this.systemIndexToArchetypeChunkIndexes[systemIndex].clear(archetypeIndex);
         if (data.getSystem(systemIndex) instanceof ArchetypeChunkSystem<ECS_TYPE> archetypeChunkSystem && archetypeChunkSystem.test(this.registry, archetype)) {
            archetypeChunkSystem.onSystemRemovedFromArchetypeChunk(archetypeChunk);
         }
      }

      if (archetypeIndex == this.archetypeSize - 1) {
         int highestUsedIndex = this.archetypeChunkReuse.previousClearBit(archetypeIndex - 1);
         this.archetypeSize = highestUsedIndex + 1;
         this.archetypeChunkReuse.clear(this.archetypeSize, archetypeIndex);
      } else {
         this.archetypeChunkReuse.set(archetypeIndex);
      }
   }

   private static class ProcessingCounter implements Lock {
      private int count = 0;

      public boolean isHeld() {
         return this.count > 0;
      }

      @Override
      public void lock() {
         this.count++;
      }

      @Override
      public void lockInterruptibly() {
         throw new UnsupportedOperationException("lockInterruptibly() is not supported");
      }

      @Override
      public boolean tryLock() {
         throw new UnsupportedOperationException("tryLock() is not supported");
      }

      @Override
      public boolean tryLock(long time, @Nonnull TimeUnit unit) {
         throw new UnsupportedOperationException("tryLock() is not supported");
      }

      @Override
      public void unlock() {
         this.count--;
      }

      @Nonnull
      @Override
      public Condition newCondition() {
         throw new UnsupportedOperationException("Conditions are not supported");
      }
   }
}
