package com.hypixel.hytale.server.core.command.commands.world.entity.snapshot;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.entity.EntitySnapshot;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.SnapshotBuffer;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectList;
import javax.annotation.Nonnull;

public class EntitySnapshotHistoryCommand extends AbstractWorldCommand {
   public EntitySnapshotHistoryCommand() {
      super("history", "server.commands.entity.snapshot.history.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      ComponentType<EntityStore, SnapshotBuffer> snapshotBufferComponentType = SnapshotBuffer.getComponentType();
      store.forEachChunk(snapshotBufferComponentType, (chunk, cmdBuffer) -> {
         for (int idx = 0; idx < chunk.size(); idx++) {
            SnapshotBuffer snapshotBufferComponent = chunk.getComponent(idx, snapshotBufferComponentType);

            assert snapshotBufferComponent != null;

            if (!snapshotBufferComponent.isInitialized()) {
               return;
            }

            for (int i = snapshotBufferComponent.getOldestTickIndex(); i <= snapshotBufferComponent.getCurrentTickIndex(); i++) {
               EntitySnapshot snapshot = snapshotBufferComponent.getSnapshot(i);

               assert snapshot != null;

               Vector3d pos = snapshot.getPosition();
               SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = cmdBuffer.getResource(EntityModule.get().getPlayerSpatialResourceType());
               ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
               playerSpatialResource.getSpatialStructure().collect(pos, 75.0, results);
               ParticleUtil.spawnParticleEffect("Example_Simple", pos.x, pos.y, pos.z, results, cmdBuffer);
            }
         }
      });
   }
}
