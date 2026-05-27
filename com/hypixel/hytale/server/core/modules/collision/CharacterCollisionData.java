package com.hypixel.hytale.server.core.modules.collision;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class CharacterCollisionData extends BasicCollisionData {
   public final Vector3d sourcePosition = new Vector3d();
   public final Vector3d targetPosition = new Vector3d();
   public Ref<EntityStore> entityReference;
   public boolean isPlayer;

   public void assign(
      @Nonnull Vector3dc sourcePosition, @Nonnull Vector3dc targetPosition, double collisionVectorScale, Ref<EntityStore> entityReference, boolean isPlayer
   ) {
      this.sourcePosition.set(sourcePosition);
      this.targetPosition.set(targetPosition);
      this.collisionStart = collisionVectorScale;
      this.entityReference = entityReference;
      this.isPlayer = isPlayer;
   }
}
