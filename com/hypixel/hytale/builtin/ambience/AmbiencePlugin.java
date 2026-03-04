package com.hypixel.hytale.builtin.ambience;

import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.builtin.ambience.commands.AmbienceCommands;
import com.hypixel.hytale.builtin.ambience.components.AmbienceTracker;
import com.hypixel.hytale.builtin.ambience.components.AmbientEmitterComponent;
import com.hypixel.hytale.builtin.ambience.resources.AmbienceResource;
import com.hypixel.hytale.builtin.ambience.systems.AmbientEmitterSystems;
import com.hypixel.hytale.builtin.ambience.systems.ForcedMusicSystems;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.AudioComponent;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.prefab.PrefabCopyableComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class AmbiencePlugin extends JavaPlugin {
   @Nonnull
   private static final String DEFAULT_AMBIENT_EMITTER_MODEL = "NPC_Spawn_Marker";
   private static AmbiencePlugin instance;
   private ComponentType<EntityStore, AmbienceTracker> ambienceTrackerComponentType;
   private ComponentType<EntityStore, AmbientEmitterComponent> ambientEmitterComponentType;
   private ResourceType<EntityStore, AmbienceResource> ambienceResourceType;
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
      this.ambienceTrackerComponentType = entityStoreRegistry.registerComponent(AmbienceTracker.class, AmbienceTracker::new);
      this.ambientEmitterComponentType = entityStoreRegistry.registerComponent(AmbientEmitterComponent.class, "AmbientEmitter", AmbientEmitterComponent.CODEC);
      this.ambienceResourceType = entityStoreRegistry.registerResource(AmbienceResource.class, AmbienceResource::new);
      ComponentType<EntityStore, TransformComponent> transformComponentType = TransformComponent.getComponentType();
      ComponentType<EntityStore, NetworkId> networkIdComponentType = NetworkId.getComponentType();
      ComponentType<EntityStore, Intangible> intangibleComponentType = Intangible.getComponentType();
      ComponentType<EntityStore, PrefabCopyableComponent> prefabCopyableComponentType = PrefabCopyableComponent.getComponentType();
      ComponentType<EntityStore, AudioComponent> audioComponentType = AudioComponent.getComponentType();
      ComponentType<EntityStore, Player> playerComponentType = Player.getComponentType();
      ComponentType<EntityStore, PlayerRef> playerRefComponentType = PlayerRef.getComponentType();
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
      entityStoreRegistry.registerSystem(
         new ForcedMusicSystems.Tick(playerComponentType, playerRefComponentType, this.ambienceTrackerComponentType, this.ambienceResourceType)
      );
      entityStoreRegistry.registerSystem(new ForcedMusicSystems.PlayerAdded(playerRefComponentType, this.ambienceTrackerComponentType));
      this.getCommandRegistry().registerCommand(new AmbienceCommands());
   }

   @Override
   protected void start() {
      AmbiencePlugin.AmbiencePluginConfig config = this.config.get();
      String ambientEmitterModelId = config.ambientEmitterModel;
      DefaultAssetMap<String, ModelAsset> modelAssetMap = ModelAsset.getAssetMap();
      ModelAsset modelAsset = modelAssetMap.getAsset(ambientEmitterModelId);
      if (modelAsset == null) {
         this.getLogger().at(Level.SEVERE).log("Ambient emitter model %s does not exist");
         modelAsset = modelAssetMap.getAsset("NPC_Spawn_Marker");
         if (modelAsset == null) {
            throw new IllegalStateException(String.format("Default ambient emitter marker '%s' not found", "NPC_Spawn_Marker"));
         }
      }

      this.ambientEmitterModel = Model.createUnitScaleModel(modelAsset);
   }

   public ComponentType<EntityStore, AmbienceTracker> getAmbienceTrackerComponentType() {
      return this.ambienceTrackerComponentType;
   }

   public ComponentType<EntityStore, AmbientEmitterComponent> getAmbientEmitterComponentType() {
      return this.ambientEmitterComponentType;
   }

   public ResourceType<EntityStore, AmbienceResource> getAmbienceResourceType() {
      return this.ambienceResourceType;
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
