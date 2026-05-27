package com.hypixel.hytale.server.npc.movement.controllers;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.collision.CollisionConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;

public final class RailStepConfig {
   @Nullable
   public Predicate<CollisionConfig> ignoredBlockFilter;
   public boolean ignoredBlocksFireTriggers;
   public boolean stopOnEntityHit;
   @Nullable
   public List<Ref<EntityStore>> candidateEntities;
}
