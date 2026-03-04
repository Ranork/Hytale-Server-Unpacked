package com.hypixel.hytale.server.core.modules;

import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.store.StoredCodec;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.RootDependency;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.modules.migrations.ChunkColumnMigrationSystem;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.EntityChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.environment.EnvironmentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.blockpositions.BlockPositionProvider;
import com.hypixel.hytale.server.core.universe.world.chunk.systems.ChunkSystems;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class LegacyModule extends JavaPlugin {
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(LegacyModule.class).build();
   private static LegacyModule instance;
   private ComponentType<ChunkStore, WorldChunk> worldChunkComponentType;
   private ComponentType<ChunkStore, BlockChunk> blockChunkComponentType;
   private ComponentType<ChunkStore, EntityChunk> entityChunkComponentType;
   private ComponentType<ChunkStore, BlockComponentChunk> blockComponentChunkComponentType;
   private ComponentType<ChunkStore, EnvironmentChunk> environmentChunkComponentType;
   private ComponentType<ChunkStore, ChunkColumn> chunkColumnComponentType;
   private ComponentType<ChunkStore, ChunkSection> chunkSectionComponentType;
   private ComponentType<ChunkStore, BlockSection> blockSectionComponentType;
   private ComponentType<ChunkStore, FluidSection> fluidSectionComponentType;
   private ComponentType<ChunkStore, BlockPositionProvider> blockPositionProviderComponentType;

   public static LegacyModule get() {
      return instance;
   }

   public LegacyModule(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      this.worldChunkComponentType = this.getChunkStoreRegistry().registerComponent(WorldChunk.class, "WorldChunk", WorldChunk.CODEC);
      this.blockChunkComponentType = this.getChunkStoreRegistry().registerComponent(BlockChunk.class, "BlockChunk", BlockChunk.CODEC);
      this.entityChunkComponentType = this.getChunkStoreRegistry().registerComponent(EntityChunk.class, "EntityChunk", EntityChunk.CODEC);
      this.blockComponentChunkComponentType = this.getChunkStoreRegistry()
         .registerComponent(BlockComponentChunk.class, "BlockComponentChunk", BlockComponentChunk.CODEC);
      this.environmentChunkComponentType = this.getChunkStoreRegistry().registerComponent(EnvironmentChunk.class, "EnvironmentChunk", EnvironmentChunk.CODEC);
      this.chunkColumnComponentType = this.getChunkStoreRegistry().registerComponent(ChunkColumn.class, "ChunkColumn", ChunkColumn.CODEC);
      this.chunkSectionComponentType = this.getChunkStoreRegistry().registerComponent(ChunkSection.class, "ChunkSection", ChunkSection.CODEC);
      this.blockSectionComponentType = this.getChunkStoreRegistry().registerComponent(BlockSection.class, "Block", BlockSection.CODEC);
      this.fluidSectionComponentType = this.getChunkStoreRegistry().registerComponent(FluidSection.class, "Fluid", FluidSection.CODEC);
      this.blockPositionProviderComponentType = this.getChunkStoreRegistry().registerComponent(BlockPositionProvider.class, () -> {
         throw new UnsupportedOperationException("BlockPositionProvider cannot be constructed");
      });
      this.getChunkStoreRegistry().registerSystem(new ChunkSystems.OnNewChunk());
      this.getChunkStoreRegistry().registerSystem(new ChunkSystems.OnChunkLoad());
      this.getChunkStoreRegistry().registerSystem(new ChunkSystems.OnNonTicking());
      this.getChunkStoreRegistry().registerSystem(new ChunkSystems.EnsureBlockSection());
      this.getChunkStoreRegistry().registerSystem(new LegacyModule.MigrateLegacySections());
      this.getChunkStoreRegistry().registerSystem(new ChunkSystems.LoadBlockSection());
      this.getChunkStoreRegistry().registerSystem(new ChunkSystems.ReplicateChanges());
      this.getChunkStoreRegistry().registerSystem(new BlockChunk.LoadBlockChunkPacketSystem(this.blockChunkComponentType));
      this.getChunkStoreRegistry().registerSystem(new EntityChunk.EntityChunkLoadingSystem());
      this.getChunkStoreRegistry().registerSystem(new BlockComponentChunk.BlockComponentChunkLoadingSystem());
      this.getChunkStoreRegistry().registerSystem(new BlockComponentChunk.LoadBlockComponentPacketSystem(this.blockComponentChunkComponentType));
      this.getChunkStoreRegistry().registerSystem(new BlockComponentChunk.UnloadBlockComponentPacketSystem(this.blockComponentChunkComponentType));
      ComponentType<ChunkStore, LegacyModule.LegacyBlockStateChunk> legacyBlockStateComponentType = this.getChunkStoreRegistry()
         .registerComponent(LegacyModule.LegacyBlockStateChunk.class, "BlockStateChunk", LegacyModule.LegacyBlockStateChunk.CODEC, true);
      this.getChunkStoreRegistry()
         .registerSystem(new LegacyModule.MigrateLegacyBlockStateChunkSystem(legacyBlockStateComponentType, this.blockComponentChunkComponentType));
      this.getEventRegistry().register(LoadedAssetsEvent.class, GameplayConfig.class, event -> WorldMapManager.sendSettingsToAllWorlds());
   }

   public ComponentType<ChunkStore, WorldChunk> getWorldChunkComponentType() {
      return this.worldChunkComponentType;
   }

   public ComponentType<ChunkStore, BlockChunk> getBlockChunkComponentType() {
      return this.blockChunkComponentType;
   }

   public ComponentType<ChunkStore, EntityChunk> getEntityChunkComponentType() {
      return this.entityChunkComponentType;
   }

   public ComponentType<ChunkStore, BlockComponentChunk> getBlockComponentChunkComponentType() {
      return this.blockComponentChunkComponentType;
   }

   public ComponentType<ChunkStore, EnvironmentChunk> getEnvironmentChunkComponentType() {
      return this.environmentChunkComponentType;
   }

   public ComponentType<ChunkStore, ChunkColumn> getChunkColumnComponentType() {
      return this.chunkColumnComponentType;
   }

   public ComponentType<ChunkStore, ChunkSection> getChunkSectionComponentType() {
      return this.chunkSectionComponentType;
   }

   public ComponentType<ChunkStore, BlockSection> getBlockSectionComponentType() {
      return this.blockSectionComponentType;
   }

   public ComponentType<ChunkStore, FluidSection> getFluidSectionComponentType() {
      return this.fluidSectionComponentType;
   }

   public ComponentType<ChunkStore, BlockPositionProvider> getBlockPositionProviderComponentType() {
      return this.blockPositionProviderComponentType;
   }

   private static class LegacyBlockStateChunk implements Component<ChunkStore> {
      public static final BuilderCodec<LegacyModule.LegacyBlockStateChunk> CODEC = BuilderCodec.builder(
            LegacyModule.LegacyBlockStateChunk.class, LegacyModule.LegacyBlockStateChunk::new
         )
         .addField(
            new KeyedCodec<>("States", new ArrayCodec<>(new StoredCodec<>(ChunkStore.HOLDER_CODEC_KEY), Holder[]::new)),
            (entityChunk, array) -> entityChunk.holders = array,
            entityChunk -> {
               throw new UnsupportedOperationException("Serialise is not allowed for BlockStateChunk");
            }
         )
         .build();
      public Holder<ChunkStore>[] holders;

      public LegacyBlockStateChunk() {
      }

      public LegacyBlockStateChunk(Holder<ChunkStore>[] holders) {
         this.holders = holders;
      }

      @Nonnull
      @Override
      public Component<ChunkStore> clone() {
         Holder<ChunkStore>[] newHolders = new Holder[this.holders.length];

         for (int i = 0; i < this.holders.length; i++) {
            newHolders[i] = this.holders[i].clone();
         }

         return new LegacyModule.LegacyBlockStateChunk(newHolders);
      }
   }

   private static class MigrateLegacyBlockStateChunkSystem extends ChunkColumnMigrationSystem {
      private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
      private final ComponentType<ChunkStore, LegacyModule.LegacyBlockStateChunk> legacyComponentType;
      private final ComponentType<ChunkStore, BlockComponentChunk> componentType;
      private final Archetype<ChunkStore> archetype;

      public MigrateLegacyBlockStateChunkSystem(
         ComponentType<ChunkStore, LegacyModule.LegacyBlockStateChunk> legacyComponentType, ComponentType<ChunkStore, BlockComponentChunk> componentType
      ) {
         this.legacyComponentType = legacyComponentType;
         this.componentType = componentType;
         this.archetype = Archetype.of(legacyComponentType, WorldChunk.getComponentType());
      }

      @Override
      public Query<ChunkStore> getQuery() {
         return this.archetype;
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<ChunkStore> holder, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store) {
         LegacyModule.LegacyBlockStateChunk component = holder.getComponent(this.legacyComponentType);

         assert component != null;

         holder.removeComponent(this.legacyComponentType);
         Int2ObjectOpenHashMap<Holder<ChunkStore>> holders = new Int2ObjectOpenHashMap();

         for (Holder<ChunkStore> blockComponentHolder : component.holders) {
            BlockState blockState = BlockState.getBlockState(blockComponentHolder);
            Vector3i position = blockState.__internal_getPosition();
            if (position == null) {
               LOGGER.at(Level.SEVERE).log("Skipping migration for BlockState with null position!", blockComponentHolder);
            } else {
               holders.put(blockState.getIndex(), blockComponentHolder);
            }
         }

         BlockComponentChunk blockComponentChunk = new BlockComponentChunk(holders, new Int2ObjectOpenHashMap());
         holder.addComponent(this.componentType, blockComponentChunk);
         holder.getComponent(WorldChunk.getComponentType()).setBlockComponentChunk(blockComponentChunk);
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<ChunkStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store) {
      }

      @Nonnull
      @Override
      public Set<Dependency<ChunkStore>> getDependencies() {
         return RootDependency.firstSet();
      }
   }

   @Deprecated(forRemoval = true)
   public static class MigrateLegacySections extends ChunkColumnMigrationSystem {
      private final Query<ChunkStore> QUERY = Query.and(ChunkColumn.getComponentType(), BlockChunk.getComponentType());
      private final Set<Dependency<ChunkStore>> DEPENDENCIES = Set.of(
         new SystemDependency<>(Order.AFTER, ChunkSystems.OnNewChunk.class), RootDependency.first()
      );

      @Override
      public void onEntityAdd(@Nonnull Holder<ChunkStore> holder, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store) {
         ChunkColumn column = holder.getComponent(ChunkColumn.getComponentType());

         assert column != null;

         BlockChunk blockChunk = holder.getComponent(BlockChunk.getComponentType());

         assert blockChunk != null;

         Holder<ChunkStore>[] sections = column.getSectionHolders();
         BlockSection[] migratedSections = blockChunk.takeMigratedSections();
         if (migratedSections != null) {
            for (int i = 0; i < sections.length; i++) {
               Holder<ChunkStore> section = sections[i];
               BlockSection blockSection = migratedSections[i];
               if (section != null && blockSection != null) {
                  section.putComponent(BlockSection.getComponentType(), blockSection);
                  blockChunk.markNeedsSaving();
               }
            }
         }
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<ChunkStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store) {
      }

      @Nonnull
      @Override
      public Query<ChunkStore> getQuery() {
         return this.QUERY;
      }

      @Nonnull
      @Override
      public Set<Dependency<ChunkStore>> getDependencies() {
         return this.DEPENDENCIES;
      }
   }
}
