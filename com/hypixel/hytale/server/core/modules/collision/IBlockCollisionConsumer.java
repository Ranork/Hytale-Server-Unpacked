package com.hypixel.hytale.server.core.modules.collision;

import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;

public interface IBlockCollisionConsumer {
   IBlockCollisionConsumer.Result onCollision(int var1, int var2, int var3, Vector3d var4, BlockContactData var5, BlockData var6, Box var7);

   IBlockCollisionConsumer.Result probeCollisionDamage(int var1, int var2, int var3, Vector3d var4, BlockContactData var5, BlockData var6);

   void onCollisionDamage(int var1, int var2, int var3, Vector3d var4, BlockContactData var5, BlockData var6);

   IBlockCollisionConsumer.Result onCollisionSliceFinished();

   void onCollisionFinished();

   public static enum Result {
      CONTINUE,
      STOP,
      STOP_NOW;
   }
}
