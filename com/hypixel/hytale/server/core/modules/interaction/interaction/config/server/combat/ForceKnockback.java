package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class ForceKnockback extends Knockback {
   public static final BuilderCodec<ForceKnockback> CODEC = BuilderCodec.builder(ForceKnockback.class, ForceKnockback::new, Knockback.BASE_CODEC)
      .appendInherited(new KeyedCodec<>("Direction", Vector3d.CODEC), (o, i) -> o.direction = i, o -> o.direction, (o, p) -> o.direction = p.direction)
      .addValidator(Validators.nonNull())
      .add()
      .afterDecode(i -> i.direction.normalize())
      .build();
   private Vector3d direction = Vector3d.UP;

   @Nonnull
   @Override
   public Vector3d calculateVector(Vector3d source, float yaw, Vector3d target) {
      Vector3d vel = this.direction.clone();
      vel.rotateY(yaw);
      vel.scale(this.force);
      return vel;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ForceKnockback{direction=" + this.direction + "} " + super.toString();
   }
}
