package com.hypixel.hytale.server.core.universe.world.spawn;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.World;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IndividualSpawnProvider implements ISpawnProvider {
   @Nonnull
   public static BuilderCodec<IndividualSpawnProvider> CODEC = BuilderCodec.builder(IndividualSpawnProvider.class, IndividualSpawnProvider::new)
      .documentation(
         "A spawn provider that selects a spawn point from a list based on the player being spawned in. This gives random but consistent spawn points for players."
      )
      .<Transform[]>append(
         new KeyedCodec<>("SpawnPoints", new ArrayCodec<>(Transform.CODEC, Transform[]::new)), (o, i) -> o.spawnPoints = i, o -> o.spawnPoints
      )
      .documentation("The list of spawn points to select from.")
      .add()
      .build();
   private Transform[] spawnPoints;

   public IndividualSpawnProvider() {
   }

   public IndividualSpawnProvider(@Nonnull Transform spawnPoint) {
      this.spawnPoints = new Transform[1];
      this.spawnPoints[0] = spawnPoint;
   }

   public IndividualSpawnProvider(@Nonnull Transform[] spawnPoints) {
      this.spawnPoints = spawnPoints;
   }

   @Override
   public Transform getSpawnPoint(@Nonnull World world, @Nonnull UUID uuid) {
      return this.spawnPoints[Math.abs((int)HashUtil.hashUuid(uuid)) % this.spawnPoints.length].clone();
   }

   @Override
   public Transform[] getSpawnPoints() {
      return this.spawnPoints;
   }

   @Nullable
   public Transform getFirstSpawnPoint() {
      return this.spawnPoints.length == 0 ? null : this.spawnPoints[0];
   }

   @Override
   public boolean isWithinSpawnDistance(@Nonnull Vector3d position, double distance) {
      double distanceSquared = distance * distance;

      for (Transform point : this.spawnPoints) {
         if (position.distanceSquaredTo(point.getPosition()) < distanceSquared) {
            return true;
         }
      }

      return false;
   }
}
