package com.hypixel.hytale.server.core.modules.entity.damage;

import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public abstract class DamageEventSystem extends EntityEventSystem<EntityStore, Damage> {
   protected DamageEventSystem() {
      super(Damage.class);
   }
}
