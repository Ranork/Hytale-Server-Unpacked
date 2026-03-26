package com.hypixel.hytale.server.core.universe.world.chunk;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.collection.Flags;
import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickManager;
import com.hypixel.hytale.server.core.asset.type.blocktick.config.TickProcedure;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.blocktype.component.BlockPhysics;
import com.hypixel.hytale.server.core.modules.LegacyModule;
import com.hypixel.hytale.server.core.modules.block.BlockEntity;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.accessor.BlockAccessor;
import com.hypixel.hytale.server.core.universe.world.accessor.ChunkAccessor;
import com.hypixel.hytale.server.core.universe.world.chunk.environment.EnvironmentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.StampedLock;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldChunk implements BlockAccessor, Component<ChunkStore> {
   public static final int KEEP_ALIVE_DEFAULT = 15;
   public static final BuilderCodec<WorldChunk> CODEC = BuilderCodec.builder(WorldChunk.class, WorldChunk::new).build();
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private World world;
   private final StampedLock flagsLock = new StampedLock();
   private final Flags<ChunkFlag> flags;
   private Ref<ChunkStore> reference;
   @Nullable
   private BlockChunk blockChunk;
   @Nullable
   private BlockComponentChunk blockComponentChunk;
   @Nullable
   private EntityChunk entityChunk;
   private int keepAlive = 15;
   private int activeTimer = 15;
   private boolean needsSaving;
   private boolean isSaving;
   private final AtomicInteger keepLoaded = new AtomicInteger();
   private boolean lightingUpdatesEnabled = true;
   @Deprecated
   public final AtomicLong chunkLightTiming = new AtomicLong();

   public static ComponentType<ChunkStore, WorldChunk> getComponentType() {
      return LegacyModule.get().getWorldChunkComponentType();
   }

   private WorldChunk() {
      this.flags = new Flags<>();
   }

   private WorldChunk(World world, Flags<ChunkFlag> flags) {
      this.world = world;
      this.flags = flags;
   }

   public WorldChunk(World world, Flags<ChunkFlag> state, BlockChunk blockChunk, BlockComponentChunk blockComponentChunk, EntityChunk entityChunk) {
      this(world, state);
      this.blockChunk = blockChunk;
      this.blockComponentChunk = blockComponentChunk;
      this.entityChunk = entityChunk;
   }

   @Nonnull
   public Holder<ChunkStore> toHolder() {
      if (this.reference != null && this.reference.isValid() && this.world != null) {
         Holder<ChunkStore> holder = ChunkStore.REGISTRY.newHolder();
         Store<ChunkStore> componentStore = this.world.getChunkStore().getStore();
         Archetype<ChunkStore> archetype = componentStore.getArchetype(this.reference);

         for (int i = archetype.getMinIndex(); i < archetype.length(); i++) {
            ComponentType componentType = archetype.get(i);
            if (componentType != null) {
               holder.addComponent(componentType, componentStore.getComponent(this.reference, componentType));
            }
         }

         return holder;
      } else {
         Holder<ChunkStore> holder = ChunkStore.REGISTRY.newHolder();
         holder.addComponent(getComponentType(), this);
         holder.addComponent(BlockChunk.getComponentType(), this.blockChunk);
         holder.addComponent(EnvironmentChunk.getComponentType(), this.blockChunk.getEnvironmentChunk());
         holder.addComponent(EntityChunk.getComponentType(), this.entityChunk);
         holder.addComponent(BlockComponentChunk.getComponentType(), this.blockComponentChunk);
         return holder;
      }
   }

   @Deprecated
   public void setReference(Ref<ChunkStore> reference) {
      if (this.reference != null && this.reference.isValid()) {
         throw new IllegalArgumentException("Chunk already has a valid EntityReference: " + this.reference + " new reference " + reference);
      } else {
         this.reference = reference;
      }
   }

   public Ref<ChunkStore> getReference() {
      return this.reference;
   }

   @Nonnull
   @Override
   public Component<ChunkStore> clone() {
      return new WorldChunk();
   }

   public boolean is(@Nonnull ChunkFlag flag) {
      long stamp = this.flagsLock.tryOptimisticRead();
      boolean value = this.flags.is(flag);
      if (this.flagsLock.validate(stamp)) {
         return value;
      } else {
         stamp = this.flagsLock.readLock();

         boolean var5;
         try {
            var5 = this.flags.is(flag);
         } finally {
            this.flagsLock.unlockRead(stamp);
         }

         return var5;
      }
   }

   public boolean not(@Nonnull ChunkFlag flag) {
      long stamp = this.flagsLock.tryOptimisticRead();
      boolean value = this.flags.not(flag);
      if (this.flagsLock.validate(stamp)) {
         return value;
      } else {
         stamp = this.flagsLock.readLock();

         boolean var5;
         try {
            var5 = this.flags.not(flag);
         } finally {
            this.flagsLock.unlockRead(stamp);
         }

         return var5;
      }
   }

   public void setFlag(@Nonnull ChunkFlag flag, boolean value) {
      long lock = this.flagsLock.writeLock();

      boolean isInit;
      try {
         if (!this.flags.set(flag, value)) {
            return;
         }

         isInit = this.flags.is(ChunkFlag.INIT);
      } finally {
         this.flagsLock.unlockWrite(lock);
      }

      if (isInit) {
         this.updateFlag(flag, value);
      }

      LOGGER.at(Level.FINER).log("[%d, %d] updated chunk flag (init: %s): %s, %s ", this.getX(), this.getZ(), isInit, flag, value);
   }

   public boolean toggleFlag(@Nonnull ChunkFlag flag) {
      long lock = this.flagsLock.writeLock();

      boolean value;
      boolean isInit;
      try {
         value = this.flags.toggle(flag);
         isInit = this.flags.is(ChunkFlag.INIT);
      } finally {
         this.flagsLock.unlockWrite(lock);
      }

      if (isInit) {
         this.updateFlag(flag, value);
      }

      LOGGER.at(Level.FINER).log("[%d, %d] updated chunk flag (init: %s): %s, %s ", this.getX(), this.getZ(), isInit, flag, value);
      return value;
   }

   @Deprecated
   public void loadFromHolder(World world, int x, int z, @Nonnull Holder<ChunkStore> holder) {
      this.world = world;
      this.blockChunk = holder.getComponent(BlockChunk.getComponentType());
      this.blockChunk.setEnvironmentChunk(holder.getComponent(EnvironmentChunk.getComponentType()));
      this.blockComponentChunk = holder.getComponent(BlockComponentChunk.getComponentType());
      this.entityChunk = holder.getComponent(EntityChunk.getComponentType());
      this.blockChunk.load(x, z);
   }

   public void initFlags() {
      this.world.debugAssertInTickingThread();
      if (!this.is(ChunkFlag.START_INIT)) {
         throw new IllegalArgumentException("START_INIT hasn't been set!");
      } else if (this.is(ChunkFlag.INIT)) {
         throw new IllegalArgumentException("INIT is already set!");
      } else {
         for (int i = 0; i < ChunkFlag.VALUES.length; i++) {
            ChunkFlag flag = ChunkFlag.VALUES[i];
            this.updateFlag(flag, this.is(flag));
         }

         this.setFlag(ChunkFlag.INIT, true);
      }
   }

   private void updateFlag(ChunkFlag flag, boolean value) {
      if (flag == ChunkFlag.TICKING) {
         this.world.debugAssertInTickingThread();
         this.resetKeepAlive();
         if (value) {
            this.startsTicking();
         } else {
            this.stopsTicking();
         }
      }
   }

   private void startsTicking() {
      this.world.debugAssertInTickingThread();
      LOGGER.at(Level.FINER).log("Chunk started ticking %s", this);
      Store<ChunkStore> componentStore = this.world.getChunkStore().getStore();
      componentStore.tryRemoveComponent(this.reference, ChunkStore.REGISTRY.getNonTickingComponentType());
   }

   private void stopsTicking() {
      this.world.debugAssertInTickingThread();
      LOGGER.at(Level.FINER).log("Chunk stopped ticking %s", this);
      Store<ChunkStore> componentStore = this.world.getChunkStore().getStore();
      componentStore.ensureComponent(this.reference, ChunkStore.REGISTRY.getNonTickingComponentType());
   }

   @Nullable
   public BlockChunk getBlockChunk() {
      return this.blockChunk;
   }

   @Nullable
   public BlockComponentChunk getBlockComponentChunk() {
      return this.blockComponentChunk;
   }

   @Nullable
   public EntityChunk getEntityChunk() {
      return this.entityChunk;
   }

   public boolean shouldKeepLoaded() {
      return this.keepLoaded.get() > 0;
   }

   public void addKeepLoaded() {
      this.keepLoaded.incrementAndGet();
   }

   public void removeKeepLoaded() {
      this.keepLoaded.decrementAndGet();
   }

   public int pollKeepAlive(int pollCount) {
      return this.keepAlive = Math.max(this.keepAlive - pollCount, 0);
   }

   public void resetKeepAlive() {
      this.keepAlive = 15;
   }

   public int pollActiveTimer(int pollCount) {
      return this.activeTimer = Math.max(this.activeTimer - pollCount, 0);
   }

   public void resetActiveTimer() {
      this.activeTimer = 15;
   }

   @Override
   public ChunkAccessor getChunkAccessor() {
      return this.world;
   }

   @Override
   public int getBlock(int x, int y, int z) {
      return y >= 0 && y < 320 ? this.blockChunk.getBlock(x, y, z) : 0;
   }

   @Override
   public boolean setBlock(int x, int y, int z, int id, @Nonnull BlockType blockType, int rotation, int filler, int settings) {
      if (y >= 0 && y < 320) {
         short oldHeight = this.blockChunk.getHeight(x, z);
         BlockSection blockSection = this.blockChunk.getSectionAtBlockY(y);
         int oldRotation = blockSection.getRotationIndex(x, y, z);
         int oldFiller = blockSection.getFiller(x, y, z);
         int oldBlock = blockSection.get(x, y, z);
         boolean changed = (oldBlock != id || rotation != oldRotation) && blockSection.set(x, y, z, id, rotation, filler);
         if (changed || (settings & 64) != 0) {
            int worldX = (this.getX() << 5) + (x & 31);
            int worldZ = (this.getZ() << 5) + (z & 31);
            if ((settings & 64) != 0) {
               blockSection.invalidateBlock(x, y, z);
            }

            short newHeight = oldHeight;
            if ((settings & 512) == 0) {
               newHeight = BlockOperations.updateBlockHeight(this.blockChunk, id, blockType, x, y, z, oldHeight);
            }

            if ((settings & 4) == 0) {
               BlockOperations.spawnBlockParticles(this.world.getChunkStore(), oldBlock, id, worldX, y, worldZ, (settings & 32) != 0);
            }

            if ((settings & 2) == 0) {
               Holder<ChunkStore> blockEntity = blockType.getBlockEntity();
               if (blockEntity != null && filler == 0) {
                  Holder<ChunkStore> newComponents = blockEntity.clone();
                  this.setState(x, y, z, blockType, rotation, newComponents);
               } else {
                  this.setState(x, y, z, blockType, rotation, null);
               }
            }

            if (this.lightingUpdatesEnabled) {
               this.world.getChunkLighting().invalidateLightAtBlock(this.world.getChunkStore(), x, y, z, blockType, oldHeight, newHeight);
            }

            TickProcedure tickProcedure = BlockTickManager.getBlockTickProvider().getTickProcedure(id);
            this.blockChunk.setTicking(x, y, z, tickProcedure != null);
            FillerBlockUtil.ChangeReason changeReason = FillerBlockUtil.ChangeReason.NONE;
            if ((settings & 4) == 0) {
               changeReason = (settings & 32) != 0 ? FillerBlockUtil.ChangeReason.BY_PHYSICS : FillerBlockUtil.ChangeReason.NORMAL;
            }

            if ((settings & 16) == 0) {
               FillerBlockUtil.removeFillerBlocksAt(
                  this.world.getChunkStore().getStore(), blockSection, worldX, y, worldZ, oldBlock, oldFiller, oldRotation, changeReason
               );
            }

            if ((settings & 8) == 0 && filler == 0) {
               Store<ChunkStore> store = this.reference.getStore();
               ChunkColumn column = store.getComponent(this.reference, ChunkColumn.getComponentType());

               assert column != null;

               Ref<ChunkStore> section = column.getSection(ChunkUtil.chunkCoordinate(y));

               assert section != null;

               FillerBlockUtil.setFillerBlocksAt(store, section, blockSection, worldX, y, worldZ, id, filler, rotation, changeReason);
            }

            if ((settings & 256) != 0) {
               BlockOperations.updateBlockArea(this.world.getChunkStore(), blockSection, blockType, rotation, x, y, z);
            }

            if (this.reference != null && this.reference.isValid()) {
               if (this.world.isInThread()) {
                  this.setBlockPhysics(x, y, z, blockType);
               } else {
                  CompletableFutureUtil._catch(CompletableFuture.runAsync(() -> this.setBlockPhysics(x, y, z, blockType), this.world));
               }
            }
         }

         return changed;
      } else {
         return false;
      }
   }

   private void setBlockPhysics(int x, int y, int z, @Nonnull BlockType blockType) {
      Store<ChunkStore> store = this.reference.getStore();
      ChunkColumn column = store.getComponent(this.reference, ChunkColumn.getComponentType());
      Ref<ChunkStore> section = column.getSection(ChunkUtil.chunkCoordinate(y));
      if (section != null) {
         if (!blockType.hasSupport()) {
            BlockPhysics.clear(store, section, x, y, z);
         } else {
            BlockPhysics.reset(store, section, x, y, z);
         }
      }
   }

   @Deprecated(forRemoval = true)
   @Override
   public int getFiller(int x, int y, int z) {
      return y >= 0 && y < 320 ? this.blockChunk.getSectionAtBlockY(y).getFiller(x, y, z) : 0;
   }

   @Deprecated(forRemoval = true)
   @Override
   public int getRotationIndex(int x, int y, int z) {
      return y >= 0 && y < 320 ? this.blockChunk.getSectionAtBlockY(y).getRotationIndex(x, y, z) : 0;
   }

   @Override
   public boolean setTicking(int x, int y, int z, boolean ticking) {
      return this.blockChunk.setTicking(x, y, z, ticking);
   }

   @Override
   public boolean isTicking(int x, int y, int z) {
      return this.blockChunk.isTicking(x, y, z);
   }

   public short getHeight(int x, int z) {
      return this.blockChunk.getHeight(x, z);
   }

   public short getHeight(int index) {
      return this.blockChunk.getHeight(index);
   }

   public int getTint(int x, int z) {
      return this.blockChunk.getTint(x, z);
   }

   @Nullable
   public Ref<ChunkStore> getBlockComponentEntity(int x, int y, int z) {
      if (y < 0 || y >= 320) {
         return null;
      } else if (!this.world.isInThread()) {
         return CompletableFuture.<Ref<ChunkStore>>supplyAsync(() -> this.getBlockComponentEntity(x, y, z), this.world).join();
      } else {
         int index = ChunkUtil.indexBlockInColumn(x, y, z);
         return this.blockComponentChunk.getEntityReference(index);
      }
   }

   @Nullable
   @Override
   public Holder<ChunkStore> getBlockComponentHolder(int x, int y, int z) {
      if (y < 0 || y >= 320) {
         return null;
      } else if (!this.world.isInThread()) {
         return CompletableFuture.<Holder<ChunkStore>>supplyAsync(() -> this.getBlockComponentHolder(x, y, z), this.world).join();
      } else {
         int index = ChunkUtil.indexBlockInColumn(x, y, z);
         Ref<ChunkStore> reference = this.blockComponentChunk.getEntityReference(index);
         if (reference != null) {
            return reference.getStore().copyEntity(reference);
         } else {
            Holder<ChunkStore> holder = this.blockComponentChunk.getEntityHolder(index);
            return holder != null ? holder.clone() : null;
         }
      }
   }

   @Deprecated(forRemoval = true)
   @Override
   public int getFluidId(int x, int y, int z) {
      Ref<ChunkStore> columnRef = this.getReference();
      Store<ChunkStore> store = columnRef.getStore();
      ChunkColumn column = store.getComponent(columnRef, ChunkColumn.getComponentType());
      Ref<ChunkStore> section = column.getSection(ChunkUtil.chunkCoordinate(y));
      if (section == null) {
         return Integer.MIN_VALUE;
      } else {
         FluidSection fluidSection = store.getComponent(section, FluidSection.getComponentType());
         return fluidSection.getFluidId(x, y, z);
      }
   }

   @Deprecated(forRemoval = true)
   @Override
   public byte getFluidLevel(int x, int y, int z) {
      Ref<ChunkStore> columnRef = this.getReference();
      Store<ChunkStore> store = columnRef.getStore();
      ChunkColumn column = store.getComponent(columnRef, ChunkColumn.getComponentType());
      Ref<ChunkStore> section = column.getSection(ChunkUtil.chunkCoordinate(y));
      if (section == null) {
         return 0;
      } else {
         FluidSection fluidSection = store.getComponent(section, FluidSection.getComponentType());
         return fluidSection.getFluidLevel(x, y, z);
      }
   }

   @Deprecated(forRemoval = true)
   @Override
   public int getSupportValue(int x, int y, int z) {
      Ref<ChunkStore> columnRef = this.getReference();
      Store<ChunkStore> store = columnRef.getStore();
      ChunkColumn column = store.getComponent(columnRef, ChunkColumn.getComponentType());
      Ref<ChunkStore> section = column.getSection(ChunkUtil.chunkCoordinate(y));
      if (section == null) {
         return 0;
      } else {
         BlockPhysics blockPhysics = store.getComponent(section, BlockPhysics.getComponentType());
         return blockPhysics != null ? blockPhysics.get(x, y, z) : 0;
      }
   }

   @Deprecated
   public void setState(int x, int y, int z, BlockType blockType, int rotation, @Nullable Holder<ChunkStore> holder) {
      if (y >= 0 && y < 320 && blockType != null) {
         if (!this.world.isInThread()) {
            CompletableFutureUtil._catch(CompletableFuture.runAsync(() -> this.setState(x, y, z, blockType, rotation, holder), this.world));
         } else {
            BlockEntity.setBlockEntity(this.world.getChunkStore().getStore(), this.reference, this.blockComponentChunk, x, y, z, blockType, rotation, holder);
         }
      }
   }

   public void markNeedsSaving() {
      this.needsSaving = true;
   }

   public boolean getNeedsSaving() {
      return this.needsSaving || this.blockChunk.getNeedsSaving() || this.blockComponentChunk.getNeedsSaving() || this.entityChunk.getNeedsSaving();
   }

   public boolean consumeNeedsSaving() {
      boolean out = this.needsSaving;
      if (this.blockChunk.consumeNeedsSaving()) {
         out = true;
      }

      if (this.blockComponentChunk.consumeNeedsSaving()) {
         out = true;
      }

      if (this.entityChunk.consumeNeedsSaving()) {
         out = true;
      }

      this.needsSaving = false;
      return out;
   }

   public boolean isSaving() {
      return this.isSaving;
   }

   public void setSaving(boolean saving) {
      this.isSaving = saving;
   }

   public long getIndex() {
      return this.blockChunk.getIndex();
   }

   @Override
   public int getX() {
      return this.blockChunk.getX();
   }

   @Override
   public int getZ() {
      return this.blockChunk.getZ();
   }

   public void setLightingUpdatesEnabled(boolean enableLightUpdates) {
      this.lightingUpdatesEnabled = enableLightUpdates;
   }

   public boolean isLightingUpdatesEnabled() {
      return this.lightingUpdatesEnabled;
   }

   public World getWorld() {
      return this.world;
   }

   @Nonnull
   @Override
   public String toString() {
      return "WorldChunk{x=" + this.blockChunk.getX() + ", z=" + this.blockChunk.getZ() + ", flags=" + this.flags + "}";
   }
}
