package com.hypixel.hytale.server.core.modules.collision;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class CharacterCollisionData extends BasicCollisionData {
   public Ref<EntityStore> entityReference;
   public boolean isPlayer;

   public void assign(@Nonnull Vector3d collisionPoint, double collisionVectorScale, Ref<EntityStore> entityReference, boolean isPlayer) {
      this.collisionPoint.assign(collisionPoint);
      this.collisionStart = collisionVectorScale;
      this.entityReference = entityReference;
      this.isPlayer = isPlayer;
   }
}
