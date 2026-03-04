package com.hypixel.hytale.server.core.universe.world.spawn;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import java.util.UUID;
import javax.annotation.Nonnull;

public class FitToHeightMapSpawnProvider implements ISpawnProvider {
   @Nonnull
   public static BuilderCodec<FitToHeightMapSpawnProvider> CODEC = BuilderCodec.builder(FitToHeightMapSpawnProvider.class, FitToHeightMapSpawnProvider::new)
      .documentation(
         "A spawn provider that takes a spawn point from another provider and attempts to fit it to the heightmap of the world whenever the spawn point would place the player out of bounds."
      )
      .<ISpawnProvider>append(new KeyedCodec<>("SpawnProvider", ISpawnProvider.CODEC), (o, i) -> o.spawnProvider = i, o -> o.spawnProvider)
      .documentation("The target spawn provider to take the initial spawn point from.")
      .add()
      .build();
   private ISpawnProvider spawnProvider;

   protected FitToHeightMapSpawnProvider() {
   }

   public FitToHeightMapSpawnProvider(@Nonnull ISpawnProvider spawnProvider) {
      this.spawnProvider = spawnProvider;
   }

   @Nonnull
   @Override
   public Transform getSpawnPoint(@Nonnull World world, @Nonnull UUID uuid) {
      Transform spawnPoint = this.spawnProvider.getSpawnPoint(world, uuid);
      Vector3d position = spawnPoint.getPosition();
      if (position.getY() < 0.0) {
         WorldChunk worldChunk = world.getNonTickingChunk(ChunkUtil.indexChunkFromBlock(position.getX(), position.getZ()));
         if (worldChunk != null) {
            int x = MathUtil.floor(position.getX());
            int z = MathUtil.floor(position.getZ());
            position.setY(worldChunk.getHeight(x, z) + 1);
         }
      }

      return spawnPoint;
   }

   @Override
   public Transform[] getSpawnPoints() {
      return this.spawnProvider.getSpawnPoints();
   }

   @Override
   public boolean isWithinSpawnDistance(@Nonnull Vector3d position, double distance) {
      return this.spawnProvider.isWithinSpawnDistance(position, distance);
   }
}
