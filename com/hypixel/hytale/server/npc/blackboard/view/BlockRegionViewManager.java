package com.hypixel.hytale.server.npc.blackboard.view;

import com.hypixel.fastutil.longs.Long2ObjectConcurrentHashMap;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

public abstract class BlockRegionViewManager<Type extends BlockRegionView<Type>> implements IBlackboardViewManager<Type> {
   @Nonnull
   protected Long2ObjectConcurrentHashMap<Type> views = new Long2ObjectConcurrentHashMap<>(true, ChunkUtil.NOT_FOUND);
   @Nonnull
   protected LongArrayFIFOQueue removalQueue = new LongArrayFIFOQueue();

   public Type get(@Nonnull Ref<EntityStore> ref, Blackboard blackboard, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      return this.get(transformComponent.getPosition(), blackboard);
   }

   public Type get(@Nonnull Vector3d position, Blackboard blackboard) {
      long index = BlockRegionView.indexViewFromWorldPosition(position);
      return this.get(index, blackboard);
   }

   public Type get(int chunkX, int chunkZ, Blackboard blackboard) {
      long index = BlockRegionView.indexViewFromChunkCoordinates(chunkX, chunkZ);
      return this.get(index, blackboard);
   }

   public Type get(long index, Blackboard blackboard) {
      Type view = this.views.getOrDefault(index, null);
      if (view == null) {
         view = this.createView(index, blackboard);
         this.views.put(index, view);
      }

      return view;
   }

   protected abstract Type createView(long var1, Blackboard var3);

   public Type getIfExists(long index) {
      return this.views.get(index);
   }

   @Override
   public void cleanup() {
      this.views.forEach((index, entry, viewManager) -> {
         if (viewManager.shouldCleanup((Type)entry)) {
            viewManager.removalQueue.enqueue(index);
         }
      }, this);

      while (!this.removalQueue.isEmpty()) {
         this.views.remove(this.removalQueue.dequeueLong());
      }
   }

   protected abstract boolean shouldCleanup(Type var1);

   @Override
   public void onWorldRemoved() {
   }

   @Override
   public void forEachView(@Nonnull Consumer<Type> consumer) {
      this.views.forEach((index, view) -> consumer.accept((Type)view));
   }

   @Override
   public void clear() {
      this.views.clear();
   }
}
