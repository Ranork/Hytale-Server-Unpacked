package com.hypixel.hytale.builtin.model;

import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.builtin.model.commands.ModelCommand;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import javax.annotation.Nonnull;

public class ModelPlugin extends JavaPlugin {
   public ModelPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      this.getCommandRegistry().registerCommand(new ModelCommand());
      this.getEventRegistry().register(LoadedAssetsEvent.class, ModelAsset.class, this::updateModelAssets);
   }

   private void checkForModelUpdate(
      @Nonnull Map<String, ModelAsset> reloadedModelAssets,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      ModelComponent modelComponent = archetypeChunk.getComponent(index, ModelComponent.getComponentType());
      Model oldModel = modelComponent.getModel();
      ModelAsset newModel = reloadedModelAssets.get(oldModel.getModelAssetId());
      if (newModel != null) {
         Model model = Model.createScaledModel(newModel, oldModel.getScale(), oldModel.getRandomAttachmentIds());
         commandBuffer.putComponent(archetypeChunk.getReferenceTo(index), ModelComponent.getComponentType(), new ModelComponent(model));
      }
   }

   private void updateModelAssets(@Nonnull LoadedAssetsEvent<String, ModelAsset, DefaultAssetMap<String, ModelAsset>> event) {
      Map<String, ModelAsset> map = event.getLoadedAssets();
      Universe.get()
         .getWorlds()
         .forEach(
            (name, world) -> world.execute(
               () -> world.getEntityStore()
                  .getStore()
                  .forEachEntityParallel(
                     ModelComponent.getComponentType(),
                     (index, archetypeChunk, commandBuffer) -> this.checkForModelUpdate(map, index, archetypeChunk, commandBuffer)
                  )
            )
         );
   }
}
