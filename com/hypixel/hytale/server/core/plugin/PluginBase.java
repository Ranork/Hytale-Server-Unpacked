package com.hypixel.hytale.server.core.plugin;

import com.hypixel.hytale.assetstore.JsonAsset;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.lookup.MapKeyMapCodec;
import com.hypixel.hytale.codec.lookup.StringCodecMapCodec;
import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.function.consumer.BooleanConsumer;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.metrics.MetricProvider;
import com.hypixel.hytale.metrics.MetricsRegistry;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.command.system.CommandOwner;
import com.hypixel.hytale.server.core.command.system.CommandRegistry;
import com.hypixel.hytale.server.core.modules.entity.EntityRegistry;
import com.hypixel.hytale.server.core.plugin.registry.AssetRegistry;
import com.hypixel.hytale.server.core.plugin.registry.CodecMapRegistry;
import com.hypixel.hytale.server.core.plugin.registry.IRegistry;
import com.hypixel.hytale.server.core.plugin.registry.MapKeyMapRegistry;
import com.hypixel.hytale.server.core.registry.ClientFeatureRegistry;
import com.hypixel.hytale.server.core.task.TaskRegistry;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateRegistry;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class PluginBase implements CommandOwner {
   @Nonnull
   public static final MetricsRegistry<PluginBase> METRICS_REGISTRY = new MetricsRegistry<>(MetricProvider.maybe(Function.identity()))
      .register("Identifier", plugin -> plugin.identifier.toString(), Codec.STRING)
      .register("Type", PluginBase::getType, new EnumCodec<>(PluginType.class))
      .register("Manifest", plugin -> plugin.manifest, PluginManifest.CODEC)
      .register("State", plugin -> plugin.state, new EnumCodec<>(PluginState.class))
      .register("Builtin", plugin -> plugin instanceof JavaPlugin jp && jp.getClassLoader().isInServerClassPath(), Codec.BOOLEAN);
   @Nonnull
   private final HytaleLogger logger;
   @Nonnull
   private final PluginIdentifier identifier;
   @Nonnull
   private final PluginManifest manifest;
   @Nonnull
   private final Path dataDirectory;
   @Nonnull
   private final List<Config<?>> configs = new CopyOnWriteArrayList<>();
   @Nonnull
   private PluginState state = PluginState.NONE;
   private final String notEnabledString = "The plugin " + this.getIdentifier() + " is not enabled!";
   @Nonnull
   private final CopyOnWriteArrayList<BooleanConsumer> shutdownTasks = new CopyOnWriteArrayList<>();
   private final ClientFeatureRegistry clientFeatureRegistry = new ClientFeatureRegistry(
      this.shutdownTasks, () -> this.state != PluginState.NONE && this.state != PluginState.DISABLED, this.notEnabledString, this
   );
   private final CommandRegistry commandRegistry = new CommandRegistry(
      this.shutdownTasks, () -> this.state != PluginState.NONE && this.state != PluginState.DISABLED, this.notEnabledString, this
   );
   private final EventRegistry eventRegistry = new EventRegistry(
      this.shutdownTasks, () -> this.state != PluginState.NONE && this.state != PluginState.DISABLED, this.notEnabledString, HytaleServer.get().getEventBus()
   );
   private final BlockStateRegistry blockStateRegistry = new BlockStateRegistry(
      this.shutdownTasks, () -> this.state != PluginState.NONE && this.state != PluginState.DISABLED, this.notEnabledString
   );
   private final EntityRegistry entityRegistry = new EntityRegistry(
      this.shutdownTasks, () -> this.state != PluginState.NONE && this.state != PluginState.DISABLED, this.notEnabledString
   );
   private final TaskRegistry taskRegistry = new TaskRegistry(
      this.shutdownTasks, () -> this.state != PluginState.NONE && this.state != PluginState.DISABLED, this.notEnabledString
   );
   private final ComponentRegistryProxy<EntityStore> entityStoreRegistry = new ComponentRegistryProxy<>(this.shutdownTasks, EntityStore.REGISTRY);
   private final ComponentRegistryProxy<ChunkStore> chunkStoreRegistry = new ComponentRegistryProxy<>(this.shutdownTasks, ChunkStore.REGISTRY);
   private final AssetRegistry assetRegistry = new AssetRegistry(this.shutdownTasks);
   private final Map<Codec<?>, IRegistry> codecMapRegistries = new ConcurrentHashMap<>();
   @Nonnull
   private final String basePermission;
   private Throwable failureCause;

   public PluginBase(@Nonnull PluginInit init) {
      PluginManifest pluginManifest = init.getPluginManifest();
      String pluginName = pluginManifest.getName();
      boolean isPlugin = this.getType() == PluginType.PLUGIN;
      this.logger = HytaleLogger.get(pluginName + (isPlugin ? "|P" : "|A"));
      this.dataDirectory = init.getDataDirectory();
      this.identifier = new PluginIdentifier(pluginManifest);
      this.manifest = pluginManifest;
      if (!init.isInServerClassPath()) {
         this.logger.setPropagatesSentryToParent(false);
      }

      this.basePermission = (pluginManifest.getGroup() + "." + pluginName).toLowerCase();
   }

   @Nonnull
   protected final <T> Config<T> withConfig(@Nonnull BuilderCodec<T> configCodec) {
      return this.withConfig("config", configCodec);
   }

   @Nonnull
   protected final <T> Config<T> withConfig(@Nonnull String name, @Nonnull BuilderCodec<T> configCodec) {
      if (this.state != PluginState.NONE) {
         throw new IllegalStateException("Must be called before setup");
      } else {
         Config<T> config = new Config<>(this.dataDirectory, name, configCodec);
         this.configs.add(config);
         return config;
      }
   }

   @Nullable
   public CompletableFuture<Void> preLoad() {
      if (this.configs.isEmpty()) {
         return null;
      } else {
         CompletableFuture<?>[] futures = new CompletableFuture[this.configs.size()];

         for (int i = 0; i < this.configs.size(); i++) {
            futures[i] = this.configs.get(i).load();
         }

         return CompletableFuture.allOf(futures);
      }
   }

   @Nonnull
   @Override
   public String getName() {
      return this.identifier.toString();
   }

   @Nonnull
   public HytaleLogger getLogger() {
      return this.logger;
   }

   @Nonnull
   public PluginIdentifier getIdentifier() {
      return this.identifier;
   }

   @Nonnull
   public PluginManifest getManifest() {
      return this.manifest;
   }

   @Nonnull
   public Path getDataDirectory() {
      return this.dataDirectory;
   }

   @Nonnull
   public PluginState getState() {
      return this.state;
   }

   @Nonnull
   public ClientFeatureRegistry getClientFeatureRegistry() {
      return this.clientFeatureRegistry;
   }

   @Nonnull
   public CommandRegistry getCommandRegistry() {
      return this.commandRegistry;
   }

   @Nonnull
   public EventRegistry getEventRegistry() {
      return this.eventRegistry;
   }

   @Nonnull
   public BlockStateRegistry getBlockStateRegistry() {
      return this.blockStateRegistry;
   }

   @Nonnull
   public EntityRegistry getEntityRegistry() {
      return this.entityRegistry;
   }

   @Nonnull
   public TaskRegistry getTaskRegistry() {
      return this.taskRegistry;
   }

   @Nonnull
   public ComponentRegistryProxy<EntityStore> getEntityStoreRegistry() {
      return this.entityStoreRegistry;
   }

   @Nonnull
   public ComponentRegistryProxy<ChunkStore> getChunkStoreRegistry() {
      return this.chunkStoreRegistry;
   }

   @Nonnull
   public AssetRegistry getAssetRegistry() {
      return this.assetRegistry;
   }

   @Nonnull
   public <T, C extends Codec<? extends T>> CodecMapRegistry<T, C> getCodecRegistry(@Nonnull StringCodecMapCodec<T, C> mapCodec) {
      IRegistry registry = this.codecMapRegistries.computeIfAbsent(mapCodec, v -> new CodecMapRegistry<>(this.shutdownTasks, mapCodec));
      return (CodecMapRegistry<T, C>)registry;
   }

   @Nonnull
   public <K, T extends JsonAsset<K>> CodecMapRegistry.Assets<T, ?> getCodecRegistry(@Nonnull AssetCodecMapCodec<K, T> mapCodec) {
      IRegistry registry = this.codecMapRegistries.computeIfAbsent(mapCodec, v -> new CodecMapRegistry.Assets<>(this.shutdownTasks, mapCodec));
      return (CodecMapRegistry.Assets<T, ?>)registry;
   }

   @Nonnull
   public <V> MapKeyMapRegistry<V> getCodecRegistry(@Nonnull MapKeyMapCodec<V> mapCodec) {
      IRegistry registry = this.codecMapRegistries.computeIfAbsent(mapCodec, v -> new MapKeyMapRegistry<>(this.shutdownTasks, mapCodec));
      return (MapKeyMapRegistry<V>)registry;
   }

   @Nonnull
   public final String getBasePermission() {
      return this.basePermission;
   }

   public boolean isDisabled() {
      return this.state == PluginState.NONE || this.state == PluginState.DISABLED || this.state == PluginState.SHUTDOWN || this.state == PluginState.FAILED;
   }

   public boolean isEnabled() {
      return !this.isDisabled();
   }

   protected void setup0() {
      if (this.state != PluginState.NONE && this.state != PluginState.DISABLED) {
         throw new IllegalArgumentException(String.valueOf(this.state));
      } else {
         this.state = PluginState.SETUP;

         try {
            this.setup();
         } catch (Throwable var2) {
            ((HytaleLogger.Api)this.logger.at(Level.SEVERE).withCause(var2)).log("Failed to setup plugin %s", this.identifier);
            this.state = PluginState.FAILED;
            this.failureCause = var2;
         }
      }
   }

   protected void setup() {
   }

   protected void start0() {
      if (this.state != PluginState.SETUP) {
         throw new IllegalArgumentException(String.valueOf(this.state));
      } else {
         this.state = PluginState.START;

         try {
            this.start();
            this.state = PluginState.ENABLED;
         } catch (Throwable var2) {
            ((HytaleLogger.Api)this.logger.at(Level.SEVERE).withCause(var2)).log("Failed to start %s", this.identifier);
            this.state = PluginState.FAILED;
            this.failureCause = var2;
         }
      }
   }

   protected void start() {
   }

   protected void shutdown0(boolean shutdown) {
      this.state = PluginState.SHUTDOWN;

      try {
         this.shutdown();
         this.state = this.failureCause == null ? PluginState.DISABLED : PluginState.FAILED;
      } catch (Throwable var3) {
         ((HytaleLogger.Api)this.logger.at(Level.SEVERE).withCause(var3)).log("Failed to shutdown %s", this.identifier);
         this.state = PluginState.FAILED;
         if (this.failureCause == null) {
            this.failureCause = var3;
         }
      }

      this.cleanup(shutdown);
   }

   protected void shutdown() {
   }

   void cleanup(boolean shutdown) {
      this.commandRegistry.shutdown();
      this.eventRegistry.shutdown();
      this.clientFeatureRegistry.shutdown();
      this.blockStateRegistry.shutdown();
      this.taskRegistry.shutdown();
      this.entityStoreRegistry.shutdown();
      this.chunkStoreRegistry.shutdown();
      this.codecMapRegistries.forEach((k, v) -> v.shutdown());
      this.assetRegistry.shutdown();

      for (int i = this.shutdownTasks.size() - 1; i >= 0; i--) {
         this.shutdownTasks.get(i).accept(shutdown);
      }
   }

   Throwable getFailureCause() {
      return this.failureCause;
   }

   void setFailureCause(Throwable t) {
      this.failureCause = t;
   }

   @Nonnull
   public abstract PluginType getType();
}
