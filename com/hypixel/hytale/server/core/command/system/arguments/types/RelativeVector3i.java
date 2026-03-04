package com.hypixel.hytale.server.core.command.system.arguments.types;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public class RelativeVector3i {
   @Nonnull
   public static final RelativeVector3i ZERO = new RelativeVector3i(new RelativeInteger(0, false), new RelativeInteger(0, false), new RelativeInteger(0, false));
   @Nonnull
   public static final BuilderCodec<RelativeVector3i> CODEC = BuilderCodec.builder(RelativeVector3i.class, RelativeVector3i::new)
      .append(new KeyedCodec<>("X", RelativeInteger.CODEC), (vec, val) -> vec.x = val, vec -> vec.x)
      .addValidator(Validators.nonNull())
      .add()
      .<RelativeInteger>append(new KeyedCodec<>("Y", RelativeInteger.CODEC), (vec, val) -> vec.y = val, vec -> vec.y)
      .addValidator(Validators.nonNull())
      .add()
      .<RelativeInteger>append(new KeyedCodec<>("Z", RelativeInteger.CODEC), (vec, val) -> vec.z = val, vec -> vec.z)
      .addValidator(Validators.nonNull())
      .add()
      .build();
   private RelativeInteger x;
   private RelativeInteger y;
   private RelativeInteger z;

   public RelativeVector3i(RelativeInteger x, RelativeInteger y, RelativeInteger z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   protected RelativeVector3i() {
   }

   @Nonnull
   public Vector3i resolve(int xBase, int yBase, int zBase) {
      return new Vector3i(this.x.resolve(xBase), this.y.resolve(yBase), this.z.resolve(zBase));
   }

   @Nonnull
   public Vector3i resolve(@Nonnull Vector3i base) {
      return this.resolve(base.x, base.y, base.z);
   }

   @Nonnull
   @Override
   public String toString() {
      return "{" + this.x + ", " + this.y + ", " + this.z + "}";
   }

   public boolean isRelativeX() {
      return this.x.isRelative();
   }

   public boolean isRelativeY() {
      return this.y.isRelative();
   }

   public boolean isRelativeZ() {
      return this.z.isRelative();
   }
}
