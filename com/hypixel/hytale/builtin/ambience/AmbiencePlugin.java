package com.hypixel.hytale.builtin.ambience;

import com.hypixel.hytale.assetstore.AssetReferences;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedAssetMap;
import com.hypixel.hytale.builtin.ambience.commands.AmbienceCommands;
import com.hypixel.hytale.builtin.ambience.components.AmbientEmitterComponent;
import com.hypixel.hytale.builtin.ambience.systems.AmbientEmitterSystems;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.asset.type.ambiencefx.config.AmbienceFX;
import com.hypixel.hytale.server.core.asset.type.ambiencefx.config.AmbienceFXMusic;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.musiccontainer.config.MusicContainer;
import com.hypixel.hytale.server.core.asset.type.musiccontainer.config.RandomMusicContainer;
import com.hypixel.hytale.server.core.modules.entity.component.AudioComponent;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.prefab.PrefabCopyableComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class AmbiencePlugin extends JavaPlugin {
   @Nonnull
   private static final String DEFAULT_AMBIENT_EMITTER_MODEL = "NPC_Spawn_Marker";
   private static AmbiencePlugin instance;
   private ComponentType<EntityStore, AmbientEmitterComponent> ambientEmitterComponentType;
   @Nonnull
   private final Config<AmbiencePlugin.AmbiencePluginConfig> config = this.withConfig("AmbiencePlugin", AmbiencePlugin.AmbiencePluginConfig.CODEC);
   private Model ambientEmitterModel;

   public static AmbiencePlugin get() {
      return instance;
   }

   public AmbiencePlugin(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();
      this.ambientEmitterComponentType = entityStoreRegistry.registerComponent(AmbientEmitterComponent.class, "AmbientEmitter", AmbientEmitterComponent.CODEC);
      ComponentType<EntityStore, TransformComponent> transformComponentType = TransformComponent.getComponentType();
      ComponentType<EntityStore, NetworkId> networkIdComponentType = NetworkId.getComponentType();
      ComponentType<EntityStore, Intangible> intangibleComponentType = Intangible.getComponentType();
      ComponentType<EntityStore, PrefabCopyableComponent> prefabCopyableComponentType = PrefabCopyableComponent.getComponentType();
      ComponentType<EntityStore, AudioComponent> audioComponentType = AudioComponent.getComponentType();
      entityStoreRegistry.registerSystem(
         new AmbientEmitterSystems.EntityAdded(
            this.ambientEmitterComponentType, transformComponentType, networkIdComponentType, intangibleComponentType, prefabCopyableComponentType
         )
      );
      entityStoreRegistry.registerSystem(
         new AmbientEmitterSystems.EntityRefAdded(
            this.ambientEmitterComponentType, transformComponentType, audioComponentType, networkIdComponentType, intangibleComponentType
         )
      );
      entityStoreRegistry.registerSystem(new AmbientEmitterSystems.Ticking(this.ambientEmitterComponentType, transformComponentType));
      this.getCommandRegistry().registerCommand(new AmbienceCommands());
      this.getEventRegistry().register(LoadedAssetsEvent.class, AmbienceFX.class, this::onAmbienceFXLoaded);
   }

   private void onAmbienceFXLoaded(@Nonnull LoadedAssetsEvent<String, AmbienceFX, IndexedAssetMap<String, AmbienceFX>> event) {
      Map<MusicContainer, List<AssetReferences<?, ?>>> generatedContainers = new HashMap<>();

      for (Entry<String, AmbienceFX> entry : event.getLoadedAssets().entrySet()) {
         AmbienceFX ambienceFX = entry.getValue();
         AmbienceFXMusic legacy = ambienceFX.consumeLegacyMusic();
         if (legacy != null) {
            String syntheticId = "_legacy_" + ambienceFX.getId();
            RandomMusicContainer container = RandomMusicContainer.fromLegacy(syntheticId, legacy, generatedContainers);
            container.setId(syntheticId);
            container.setAudioCategory(ambienceFX.getAudioCategoryId(), ambienceFX.getAudioCategoryIndex());
            AssetReferences<String, AmbienceFX> parentRef = new AssetReferences<>(AmbienceFX.class, Set.of(entry.getKey()));
            generatedContainers.put(container, List.of(parentRef));
         }
      }

      if (!generatedContainers.isEmpty()) {
         MusicContainer.getAssetStore().loadAssetsWithReferences("Hytale:Hytale", generatedContainers);
      }
   }

   @Override
   protected void start() {
      AmbiencePlugin.AmbiencePluginConfig config = this.config.get();
      String ambientEmitterModelId = config.ambientEmitterModel;
      DefaultAssetMap<String, ModelAsset> modelAssetMap = ModelAsset.getAssetMap();
      ModelAsset modelAsset = modelAssetMap.getAsset(ambientEmitterModelId);
      if (modelAsset == null) {
         this.getLogger().at(Level.SEVERE).log("Ambient emitter model %s does not exist", ambientEmitterModelId);
         modelAsset = modelAssetMap.getAsset("NPC_Spawn_Marker");
         if (modelAsset == null) {
            throw new IllegalStateException(String.format("Default ambient emitter marker '%s' not found", "NPC_Spawn_Marker"));
         }
      }

      this.ambientEmitterModel = Model.createUnitScaleModel(modelAsset);
   }

   public ComponentType<EntityStore, AmbientEmitterComponent> getAmbientEmitterComponentType() {
      return this.ambientEmitterComponentType;
   }

   public Model getAmbientEmitterModel() {
      return this.ambientEmitterModel;
   }

   public static class AmbiencePluginConfig {
      @Nonnull
      public static final BuilderCodec<AmbiencePlugin.AmbiencePluginConfig> CODEC = BuilderCodec.builder(
            AmbiencePlugin.AmbiencePluginConfig.class, AmbiencePlugin.AmbiencePluginConfig::new
         )
         .append(new KeyedCodec<>("AmbientEmitterModel", Codec.STRING), (o, i) -> o.ambientEmitterModel = i, o -> o.ambientEmitterModel)
         .add()
         .build();
      private String ambientEmitterModel = "NPC_Spawn_Marker";
   }
}
