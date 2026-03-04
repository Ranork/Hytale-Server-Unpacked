package com.hypixel.hytale.server.npc.blackboard.view;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

public class SingletonBlackboardViewManager<View extends IBlackboardView<View>> implements IBlackboardViewManager<View> {
   private final View view;

   public SingletonBlackboardViewManager(View view) {
      this.view = view;
   }

   @Override
   public View get(Ref<EntityStore> ref, Blackboard blackboard, ComponentAccessor<EntityStore> componentAccessor) {
      return this.view;
   }

   @Override
   public View get(Vector3d position, Blackboard blackboard) {
      return this.view;
   }

   @Override
   public View get(int chunkX, int chunkZ, Blackboard blackboard) {
      return this.view;
   }

   @Override
   public View get(long index, Blackboard blackboard) {
      return this.view;
   }

   @Override
   public View getIfExists(long index) {
      return this.view;
   }

   @Override
   public void cleanup() {
      this.view.cleanup();
   }

   @Override
   public void onWorldRemoved() {
      this.view.onWorldRemoved();
   }

   @Override
   public void forEachView(@Nonnull Consumer<View> consumer) {
      consumer.accept(this.view);
   }

   @Override
   public void clear() {
   }
}
