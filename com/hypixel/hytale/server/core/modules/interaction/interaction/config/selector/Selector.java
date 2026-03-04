package com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.function.consumer.TriIntConsumer;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector4d;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Selector {
   static void selectNearbyBlocks(
      @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Ref<EntityStore> attackerRef, double range, @Nonnull TriIntConsumer consumer
   ) {
      TransformComponent transformComponent = commandBuffer.getComponent(attackerRef, TransformComponent.getComponentType());
      if (!<unrepresentable>.$assertionsDisabled && transformComponent == null) {
         throw new AssertionError();
      } else {
         ModelComponent modelComponent = commandBuffer.getComponent(attackerRef, ModelComponent.getComponentType());
         if (!<unrepresentable>.$assertionsDisabled && modelComponent == null) {
            throw new AssertionError();
         } else {
            Vector3d position = transformComponent.getPosition();
            Model model = modelComponent.getModel();
            selectNearbyBlocks(position.x, position.y + model.getEyeHeight(attackerRef, commandBuffer), position.z, range, consumer);
         }
      }
   }

   static void selectNearbyBlocks(@Nonnull Vector3d position, double range, @Nonnull TriIntConsumer consumer) {
      selectNearbyBlocks(position.x, position.y, position.z, range, consumer);
   }

   static void selectNearbyBlocks(double xPos, double yPos, double zPos, double range, @Nonnull TriIntConsumer consumer) {
      int xStart = MathUtil.floor(xPos - range);
      int yStart = MathUtil.floor(yPos - range);
      int zStart = MathUtil.floor(zPos - range);
      int xEnd = MathUtil.floor(xPos + range);
      int yEnd = MathUtil.floor(yPos + range);
      int zEnd = MathUtil.floor(zPos + range);

      for (int x = xStart; x < xEnd; x++) {
         for (int y = yStart; y < yEnd; y++) {
            for (int z = zStart; z < zEnd; z++) {
               consumer.accept(x, y, z);
            }
         }
      }
   }

   static void selectNearbyEntities(
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull Ref<EntityStore> attacker,
      double range,
      @Nonnull Consumer<Ref<EntityStore>> consumer,
      @Nonnull Predicate<Ref<EntityStore>> filter
   ) {
      TransformComponent transformComponent = commandBuffer.getComponent(attacker, TransformComponent.getComponentType());
      if (!<unrepresentable>.$assertionsDisabled && transformComponent == null) {
         throw new AssertionError();
      } else {
         ModelComponent modelComponent = commandBuffer.getComponent(attacker, ModelComponent.getComponentType());
         if (!<unrepresentable>.$assertionsDisabled && modelComponent == null) {
            throw new AssertionError();
         } else {
            Vector3d attackerPosition = transformComponent.getPosition();
            Model model = modelComponent.getModel();
            Vector3d position = attackerPosition.clone().add(0.0, model.getEyeHeight(attacker, commandBuffer), 0.0);
            selectNearbyEntities(commandBuffer, position, range, consumer, filter);
         }
      }
   }

   static void selectNearbyEntities(
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      @Nonnull Vector3d position,
      double range,
      @Nonnull Consumer<Ref<EntityStore>> consumer,
      @Nullable Predicate<Ref<EntityStore>> filter
   ) {
      ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
      SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = componentAccessor.getResource(EntityModule.get().getPlayerSpatialResourceType());
      playerSpatialResource.getSpatialStructure().collect(position, range, results);
      SpatialResource<Ref<EntityStore>, EntityStore> entitySpatialResource = componentAccessor.getResource(EntityModule.get().getEntitySpatialResourceType());
      entitySpatialResource.getSpatialStructure().collect(position, range, results);
      SpatialResource<Ref<EntityStore>, EntityStore> itemSpatialResource = componentAccessor.getResource(EntityModule.get().getItemSpatialResourceType());
      itemSpatialResource.getSpatialStructure().collect(position, range, results);
      ObjectListIterator var10 = results.iterator();

      while (var10.hasNext()) {
         Ref<EntityStore> ref = (Ref<EntityStore>)var10.next();
         if (ref != null && ref.isValid() && (filter == null || filter.test(ref))) {
            consumer.accept(ref);
         }
      }
   }

   void tick(@Nonnull CommandBuffer<EntityStore> var1, @Nonnull Ref<EntityStore> var2, float var3, float var4);

   void selectTargetEntities(
      @Nonnull CommandBuffer<EntityStore> var1, @Nonnull Ref<EntityStore> var2, BiConsumer<Ref<EntityStore>, Vector4d> var3, Predicate<Ref<EntityStore>> var4
   );

   void selectTargetBlocks(@Nonnull CommandBuffer<EntityStore> var1, @Nonnull Ref<EntityStore> var2, @Nonnull TriIntConsumer var3);

   static {
      if (<unrepresentable>.$assertionsDisabled) {
      }
   }
}
