package com.hypixel.hytale.server.core.modules.entity.stamina;

import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class StaminaModule extends JavaPlugin {
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(StaminaModule.class)
      .depends(EntityModule.class)
      .depends(EntityStatsModule.class)
      .build();
   private static StaminaModule instance;
   private ResourceType<EntityStore, SprintStaminaRegenDelay> sprintRegenDelayResourceType;

   public StaminaModule(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      this.sprintRegenDelayResourceType = this.getEntityStoreRegistry().registerResource(SprintStaminaRegenDelay.class, SprintStaminaRegenDelay::new);
      this.getEntityStoreRegistry().registerSystem(new StaminaSystems.SprintStaminaEffectSystem());
      this.getCodecRegistry(GameplayConfig.PLUGIN_CODEC).register(StaminaGameplayConfig.class, "Stamina", StaminaGameplayConfig.CODEC);
      this.getEventRegistry().register(LoadedAssetsEvent.class, GameplayConfig.class, StaminaModule::onGameplayConfigsLoaded);
   }

   public ResourceType<EntityStore, SprintStaminaRegenDelay> getSprintRegenDelayResourceType() {
      return this.sprintRegenDelayResourceType;
   }

   protected static void onGameplayConfigsLoaded(LoadedAssetsEvent<String, GameplayConfig, AssetMap<String, GameplayConfig>> event) {
      SprintStaminaRegenDelay.invalidateResources();
   }

   public static StaminaModule get() {
      return instance;
   }
}
