package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class ForceKnockback extends Knockback {
   public static final BuilderCodec<ForceKnockback> CODEC = BuilderCodec.builder(ForceKnockback.class, ForceKnockback::new, Knockback.BASE_CODEC)
      .appendInherited(
         new KeyedCodec<>("Direction", Vector3dUtil.CODEC), (o, i) -> o.direction.set(i), o -> o.direction, (o, p) -> o.direction.set(p.direction)
      )
      .addValidator(Validators.nonNull())
      .add()
      .afterDecode(i -> i.direction.normalize())
      .build();
   private final Vector3d direction = new Vector3d(Vector3dUtil.UP);

   @Nonnull
   @Override
   public Vector3d calculateVector(Vector3d source, float yaw, Vector3d target) {
      Vector3d vel = new Vector3d(this.direction);
      vel.rotateY(yaw);
      vel.mul(this.force);
      return vel;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ForceKnockback{direction=" + this.direction + "} " + super.toString();
   }
}
