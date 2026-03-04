package com.hypixel.hytale.server.core.modules.entity.player;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.RootDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.component.system.tick.RunWhenPausedSystem;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerPingSystem extends EntityTickingSystem<EntityStore> implements RunWhenPausedSystem<EntityStore> {
   @Nonnull
   private static final ComponentType<EntityStore, PlayerRef> PLAYER_REF_COMPONENT_TYPE = PlayerRef.getComponentType();

   @Override
   public Query<EntityStore> getQuery() {
      return PLAYER_REF_COMPONENT_TYPE;
   }

   @Nonnull
   @Override
   public Set<Dependency<EntityStore>> getDependencies() {
      return RootDependency.lastSet();
   }

   @Nullable
   @Override
   public SystemGroup<EntityStore> getGroup() {
      return EntityStore.SEND_PACKET_GROUP;
   }

   @Override
   public boolean isParallel(int archetypeChunkSize, int taskCount) {
      return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
   }

   @Override
   public void tick(
      float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      PlayerRef playerRefComponent = archetypeChunk.getComponent(index, PLAYER_REF_COMPONENT_TYPE);

      assert playerRefComponent != null;

      playerRefComponent.getPacketHandler().tickPing(dt);
   }
}
