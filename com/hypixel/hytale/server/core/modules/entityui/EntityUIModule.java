package com.hypixel.hytale.server.core.modules.entityui;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.entityui.asset.CombatTextUIComponent;
import com.hypixel.hytale.server.core.modules.entityui.asset.CombatTextUIComponentAnimationEvent;
import com.hypixel.hytale.server.core.modules.entityui.asset.CombatTextUIComponentOpacityAnimationEvent;
import com.hypixel.hytale.server.core.modules.entityui.asset.CombatTextUIComponentPositionAnimationEvent;
import com.hypixel.hytale.server.core.modules.entityui.asset.CombatTextUIComponentScaleAnimationEvent;
import com.hypixel.hytale.server.core.modules.entityui.asset.EntityStatUIComponent;
import com.hypixel.hytale.server.core.modules.entityui.asset.EntityUIComponent;
import com.hypixel.hytale.server.core.modules.entityui.asset.EntityUIComponentPacketGenerator;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class EntityUIModule extends JavaPlugin {
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(EntityUIModule.class).depends(EntityStatsModule.class).build();
   private static EntityUIModule instance;
   private ComponentType<EntityStore, UIComponentList> uiComponentListType;

   public static EntityUIModule get() {
      return instance;
   }

   public EntityUIModule(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   public ComponentType<EntityStore, UIComponentList> getUIComponentListType() {
      return this.uiComponentListType;
   }

   @Override
   protected void setup() {
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                              EntityUIComponent.class, new IndexedLookupTableAssetMap<>(EntityUIComponent[]::new)
                           )
                           .setPath("Entity/UI"))
                        .setCodec(EntityUIComponent.CODEC))
                     .setKeyFunction(EntityUIComponent::getId))
                  .setPacketGenerator(new EntityUIComponentPacketGenerator())
                  .setReplaceOnRemove(EntityUIComponent::getUnknownFor))
               .loadsAfter(EntityStatType.class))
            .build()
      );
      this.getCodecRegistry(EntityUIComponent.CODEC).register("EntityStat", EntityStatUIComponent.class, EntityStatUIComponent.CODEC);
      this.getCodecRegistry(EntityUIComponent.CODEC).register("CombatText", CombatTextUIComponent.class, CombatTextUIComponent.CODEC);
      this.getCodecRegistry(CombatTextUIComponentAnimationEvent.CODEC)
         .register("Scale", CombatTextUIComponentScaleAnimationEvent.class, CombatTextUIComponentScaleAnimationEvent.CODEC);
      this.getCodecRegistry(CombatTextUIComponentAnimationEvent.CODEC)
         .register("Position", CombatTextUIComponentPositionAnimationEvent.class, CombatTextUIComponentPositionAnimationEvent.CODEC);
      this.getCodecRegistry(CombatTextUIComponentAnimationEvent.CODEC)
         .register("Opacity", CombatTextUIComponentOpacityAnimationEvent.class, CombatTextUIComponentOpacityAnimationEvent.CODEC);
      this.uiComponentListType = this.getEntityStoreRegistry().registerComponent(UIComponentList.class, "UIComponentList", UIComponentList.CODEC);
      ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType = EntityModule.get().getVisibleComponentType();
      this.getEntityStoreRegistry().registerSystem(new UIComponentSystems.Setup(this.uiComponentListType));
      this.getEntityStoreRegistry().registerSystem(new UIComponentSystems.Update(visibleComponentType, this.uiComponentListType));
      this.getEntityStoreRegistry().registerSystem(new UIComponentSystems.Remove(visibleComponentType, this.uiComponentListType));
      this.getEventRegistry().register(LoadedAssetsEvent.class, EntityUIComponent.class, this::onLoadedAssetsEvent);
   }

   private void onLoadedAssetsEvent(LoadedAssetsEvent<String, EntityUIComponent, IndexedLookupTableAssetMap<String, EntityUIComponent>> event) {
      Universe.get()
         .getWorlds()
         .forEach(
            (s, world) -> world.execute(
               () -> {
                  Store<EntityStore> store = world.getEntityStore().getStore();
                  store.forEachEntityParallel(
                     UIComponentList.getComponentType(),
                     (index, archetypeChunk, commandBuffer) -> archetypeChunk.getComponent(index, UIComponentList.getComponentType()).update()
                  );
               }
            )
         );
   }
}
