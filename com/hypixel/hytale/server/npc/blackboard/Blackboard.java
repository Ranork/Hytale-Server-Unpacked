package com.hypixel.hytale.server.npc.blackboard;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.blackboard.view.IBlackboardView;
import com.hypixel.hytale.server.npc.blackboard.view.IBlackboardViewManager;
import com.hypixel.hytale.server.npc.blackboard.view.SingletonBlackboardViewManager;
import com.hypixel.hytale.server.npc.blackboard.view.attitude.AttitudeView;
import com.hypixel.hytale.server.npc.blackboard.view.blocktype.BlockTypeView;
import com.hypixel.hytale.server.npc.blackboard.view.blocktype.BlockTypeViewManager;
import com.hypixel.hytale.server.npc.blackboard.view.event.block.BlockEventView;
import com.hypixel.hytale.server.npc.blackboard.view.event.entity.EntityEventView;
import com.hypixel.hytale.server.npc.blackboard.view.interaction.InteractionView;
import com.hypixel.hytale.server.npc.blackboard.view.resource.ResourceView;
import com.hypixel.hytale.server.npc.blackboard.view.resource.ResourceViewManager;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

public class Blackboard implements Resource<EntityStore> {
   public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private final ConcurrentHashMap<Class<? extends IBlackboardView<?>>, IBlackboardViewManager<?>> views = new ConcurrentHashMap<>();

   public static ResourceType<EntityStore, Blackboard> getResourceType() {
      return NPCPlugin.get().getBlackboardResourceType();
   }

   public void init(@Nonnull World world) {
      this.registerViewType(BlockTypeView.class, new BlockTypeViewManager());
      this.registerViewType(BlockEventView.class, new SingletonBlackboardViewManager<>(new BlockEventView(world)));
      this.registerViewType(EntityEventView.class, new SingletonBlackboardViewManager<>(new EntityEventView(world)));
      this.registerViewType(ResourceView.class, new ResourceViewManager());
      this.registerViewType(AttitudeView.class, new SingletonBlackboardViewManager<>(new AttitudeView(world)));
      this.registerViewType(InteractionView.class, new SingletonBlackboardViewManager<>(new InteractionView(world)));
   }

   public void onEntityDamageBlock(@Nonnull Ref<EntityStore> ref, @Nonnull DamageBlockEvent event) {
      for (IBlackboardViewManager<?> manager : this.views.values()) {
         manager.forEachView(view -> {
            if (view instanceof BlockEventView blockEventView) {
               blockEventView.onEntityDamageBlock(ref, event);
            }
         });
      }
   }

   public void onEntityBreakBlock(@Nonnull Ref<EntityStore> ref, @Nonnull BreakBlockEvent event) {
      for (IBlackboardViewManager<?> manager : this.views.values()) {
         manager.forEachView(view -> {
            if (view instanceof BlockEventView blockEventView) {
               blockEventView.onEntityBreakBlock(ref, event);
            }
         });
      }
   }

   private <View extends IBlackboardView<View>> void registerViewType(@Nonnull Class<View> clazz, @Nonnull IBlackboardViewManager<View> holder) {
      this.views.put(clazz, holder);
   }

   public void cleanupViews() {
      this.views.forEach((clazz, manager) -> manager.cleanup());
   }

   public void clear() {
      this.views.forEach((clazz, manager) -> manager.clear());
   }

   public void onWorldRemoved() {
      this.views.forEach((clazz, manager) -> manager.onWorldRemoved());
   }

   public <View extends IBlackboardView<View>> void forEachView(Class<View> viewTypeClass, Consumer<View> consumer) {
      this.getViewManager(viewTypeClass).forEachView(consumer);
   }

   public <View extends IBlackboardView<View>> View getView(Class<View> viewTypeClass, Ref<EntityStore> ref, ComponentAccessor<EntityStore> componentAccessor) {
      return this.getViewManager(viewTypeClass).get(ref, this, componentAccessor);
   }

   public <View extends IBlackboardView<View>> View getView(Class<View> viewTypeClass, int chunkX, int chunkZ) {
      return this.getViewManager(viewTypeClass).get(chunkX, chunkZ, this);
   }

   public <View extends IBlackboardView<View>> View getView(Class<View> viewTypeClass, long index) {
      return this.getViewManager(viewTypeClass).get(index, this);
   }

   public <View extends IBlackboardView<View>> View getIfExists(Class<View> viewTypeClass, long index) {
      return this.getViewManager(viewTypeClass).getIfExists(index);
   }

   @Nonnull
   private <View extends IBlackboardView<View>> IBlackboardViewManager<View> getViewManager(Class<View> viewTypeClass) {
      return Objects.requireNonNull((IBlackboardViewManager<View>)this.views.get(viewTypeClass), "View type manager not registered!");
   }

   @Nonnull
   @Override
   public Resource<EntityStore> clone() {
      Blackboard blackboard = new Blackboard();
      blackboard.views.putAll(this.views);
      return blackboard;
   }
}
