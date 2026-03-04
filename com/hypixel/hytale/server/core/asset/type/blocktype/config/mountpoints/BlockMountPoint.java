package com.hypixel.hytale.server.core.asset.type.blocktype.config.mountpoints;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import javax.annotation.Nonnull;

public class BlockMountPoint {
   public static final BuilderCodec<BlockMountPoint> CODEC = BuilderCodec.builder(BlockMountPoint.class, BlockMountPoint::new)
      .appendInherited(new KeyedCodec<>("Offset", Vector3f.CODEC), (seat, i) -> seat.offset = i, seat -> seat.offset, (seat, p) -> seat.offset = p.offset)
      .documentation("Relative offset from the block center (the point at .5,.5,.5 in world). Forward on a chair is 0,0,0.3")
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("Yaw", Codec.DOUBLE),
         (seat, o) -> seat.yawOffSetDegrees = o.floatValue(),
         seat -> (double)seat.yawOffSetDegrees,
         (seat, p) -> seat.yawOffSetDegrees = p.yawOffSetDegrees
      )
      .documentation("Offset for the model sitting on this seat in DEGREES")
      .add()
      .build();
   public static final BlockMountPoint[] EMPTY_ARRAY = new BlockMountPoint[0];
   private Vector3f offset;
   private float yawOffSetDegrees;

   public BlockMountPoint() {
      this(new Vector3f(), 0.0F);
   }

   public BlockMountPoint(Vector3f offset, float yawOffSetDegrees) {
      this.offset = offset;
      this.yawOffSetDegrees = yawOffSetDegrees;
   }

   public Vector3f getOffset() {
      return this.offset;
   }

   public float getYawOffSetDegrees() {
      return this.yawOffSetDegrees;
   }

   @Nonnull
   public BlockMountPoint rotate(@Nonnull Rotation yaw, @Nonnull Rotation pitch, @Nonnull Rotation roll) {
      Vector3f rotatedOffset = Rotation.rotate(this.offset, yaw, pitch, roll);
      return new BlockMountPoint(rotatedOffset, this.yawOffSetDegrees);
   }

   @Nonnull
   public Vector3f computeWorldSpacePosition(@Nonnull Vector3i blockLoc) {
      return blockLoc.toVector3f().add(0.5F, 0.5F, 0.5F).add(this.offset.x, this.offset.y, this.offset.z);
   }

   @Nonnull
   public Vector3f computeRotationEuler(@Nonnull int rotationIndex) {
      RotationTuple rotationTuple = RotationTuple.get(rotationIndex);
      Vector3f rotation = new Vector3f(
         (float)rotationTuple.pitch().getRadians(), (float)rotationTuple.yaw().getRadians(), (float)rotationTuple.roll().getRadians()
      );
      rotation.addYaw((float) Math.PI);
      rotation.addYaw((float)Math.toRadians(this.yawOffSetDegrees));
      return rotation;
   }
}
