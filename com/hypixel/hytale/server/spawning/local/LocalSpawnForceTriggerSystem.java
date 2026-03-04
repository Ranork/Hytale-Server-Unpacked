package com.hypixel.hytale.server.spawning.local;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class LocalSpawnForceTriggerSystem extends EntityTickingSystem<EntityStore> {
   private static final double[] RERUN_TIME_RANGE = new double[]{0.0, 5.0};
   private final Archetype<EntityStore> archetype;
   private final ComponentType<EntityStore, PlayerRef> playerRefComponentType = PlayerRef.getComponentType();
   private final ComponentType<EntityStore, LocalSpawnController> spawnControllerComponentType;
   private final ResourceType<EntityStore, LocalSpawnState> localSpawnStateResourceType;

   public LocalSpawnForceTriggerSystem(
      ComponentType<EntityStore, LocalSpawnController> spawnControllerComponentType, ResourceType<EntityStore, LocalSpawnState> localSpawnStateResourceType
   ) {
      this.spawnControllerComponentType = spawnControllerComponentType;
      this.localSpawnStateResourceType = localSpawnStateResourceType;
      this.archetype = Archetype.of(this.playerRefComponentType, spawnControllerComponentType);
   }

   @Override
   public Query<EntityStore> getQuery() {
      return this.archetype;
   }

   @Override
   public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
      LocalSpawnState state = store.getResource(this.localSpawnStateResourceType);
      if (state.pollForceTriggerControllers()) {
         store.tick(this, dt, systemIndex);
      }
   }

   @Override
   public void tick(
      float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      PlayerRef playerRefComponent = archetypeChunk.getComponent(index, this.playerRefComponentType);

      assert playerRefComponent != null;

      LocalSpawnController controller = archetypeChunk.getComponent(index, this.spawnControllerComponentType);
      double seconds = RandomExtra.randomRange(RERUN_TIME_RANGE);
      controller.setTimeToNextRunSeconds(seconds);
      HytaleLogger.Api context = SpawningPlugin.get().getLogger().at(Level.FINE);
      if (context.isEnabled()) {
         context.log("Force running local spawn controller for %s in %f seconds", playerRefComponent.getUsername(), seconds);
      }
   }
}
