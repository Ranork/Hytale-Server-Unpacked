package com.hypixel.hytale.server.core.modules.collision;

public interface IBlockCollisionEvaluator {
   double getCollisionStart();

   void setCollisionData(BlockCollisionData var1, CollisionConfig var2, int var3);
}
