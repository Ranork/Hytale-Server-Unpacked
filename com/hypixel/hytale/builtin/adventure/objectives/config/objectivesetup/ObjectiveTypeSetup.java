package com.hypixel.hytale.builtin.adventure.objectives.config.objectivesetup;

import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ObjectiveTypeSetup {
   @Nonnull
   public static final CodecMapCodec<ObjectiveTypeSetup> CODEC = new CodecMapCodec<>("Type");

   @Nullable
   public abstract String getObjectiveIdToStart();

   @Nullable
   public abstract Objective setup(@Nonnull Set<UUID> var1, @Nonnull UUID var2, @Nullable UUID var3, @Nonnull Store<EntityStore> var4);

   @Nonnull
   @Override
   public String toString() {
      return "ObjectiveTypeSetup{}";
   }

   static {
      CODEC.register("Objective", SetupObjective.class, SetupObjective.CODEC);
      CODEC.register("ObjectiveLine", SetupObjectiveLine.class, SetupObjectiveLine.CODEC);
   }
}
