package com.hypixel.hytale.server.spawning.jobs;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.config.FlockAsset;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.spawning.ISpawnableWithModel;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NPCBeaconSpawnJob extends SpawnJob {
   protected int roleIndex = Integer.MIN_VALUE;
   @Nullable
   private Ref<EntityStore> player;
   private int spawnsThisRound;
   private int flockSize;
   @Nullable
   private FlockAsset flockAsset;

   public int getRoleIndex() {
      return this.roleIndex;
   }

   @Nullable
   public Ref<EntityStore> getPlayer() {
      return this.player;
   }

   public int getSpawnsThisRound() {
      return this.spawnsThisRound;
   }

   public int getFlockSize() {
      return this.flockSize;
   }

   @Nullable
   public FlockAsset getFlockAsset() {
      return this.flockAsset;
   }

   @Override
   public boolean shouldTerminate() {
      return !this.player.isValid();
   }

   @Override
   public boolean budgetAvailable() {
      return true;
   }

   @Override
   public void reset() {
      super.reset();
      this.roleIndex = Integer.MIN_VALUE;
      this.flockAsset = null;
   }

   @Nullable
   @Override
   public ISpawnableWithModel getSpawnable() {
      Builder<Role> role = NPCPlugin.get().tryGetCachedValidRole(this.roleIndex);
      if (role == null) {
         return null;
      } else if (!role.isSpawnable()) {
         throw new IllegalArgumentException("Spawn job: Role must be a spawnable (non-abstract) type for spawning: " + NPCPlugin.get().getName(this.roleIndex));
      } else if (!(role instanceof ISpawnableWithModel)) {
         throw new IllegalArgumentException("Spawn job: Need ISpawnableWithModel interface for spawning: " + NPCPlugin.get().getName(this.roleIndex));
      } else {
         return (ISpawnableWithModel)role;
      }
   }

   @Nullable
   @Override
   public String getSpawnableName() {
      return NPCPlugin.get().getName(this.roleIndex);
   }

   public void beginProbing(@Nonnull PlayerRef targetPlayer, int spawnsThisRound, int roleIndex, @Nullable FlockAsset flockDefinition) {
      super.beginProbing();
      this.player = targetPlayer.getReference();
      this.spawnsThisRound = spawnsThisRound;
      this.roleIndex = roleIndex;
      this.flockAsset = flockDefinition;
      this.flockSize = flockDefinition != null ? flockDefinition.pickFlockSize() : 1;
   }
}
