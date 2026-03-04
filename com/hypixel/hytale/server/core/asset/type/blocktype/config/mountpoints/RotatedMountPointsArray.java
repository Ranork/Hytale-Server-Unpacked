package com.hypixel.hytale.server.core.asset.type.blocktype.config.mountpoints;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.function.FunctionCodec;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nullable;

public class RotatedMountPointsArray {
   private static final ArrayCodec<BlockMountPoint> CHILD = new ArrayCodec<>(BlockMountPoint.CODEC, BlockMountPoint[]::new);
   public static final Codec<RotatedMountPointsArray> CODEC = new FunctionCodec<>(CHILD, RotatedMountPointsArray::new, RotatedMountPointsArray::getRaw);
   private BlockMountPoint[] raw;
   private transient BlockMountPoint[][] rotated;

   public RotatedMountPointsArray() {
   }

   public RotatedMountPointsArray(BlockMountPoint[] raw) {
      this.raw = raw;
   }

   public int size() {
      return this.raw == null ? 0 : this.raw.length;
   }

   public BlockMountPoint[] getRaw() {
      return this.raw;
   }

   @Nullable
   public BlockMountPoint[] getRotated(int rotationIndex) {
      if (this.raw != null && rotationIndex != 0) {
         if (this.rotated == null) {
            this.rotated = new BlockMountPoint[RotationTuple.VALUES.length][];
         }

         BlockMountPoint[] value = this.rotated[rotationIndex];
         if (value == null) {
            RotationTuple rotation = RotationTuple.get(rotationIndex);
            List<BlockMountPoint> list = new ObjectArrayList();

            for (BlockMountPoint mountPoint : this.raw) {
               BlockMountPoint rotated = mountPoint.rotate(rotation.yaw(), rotation.pitch(), rotation.roll());
               list.add(rotated);
            }

            value = list.toArray(BlockMountPoint[]::new);
            this.rotated[rotationIndex] = value;
         }

         return value;
      } else {
         return this.raw;
      }
   }
}
