package com.hypixel.hytale.server.npc.corecomponents;

import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionWithDelay;
import com.hypixel.hytale.server.npc.role.support.EntitySupport;
import javax.annotation.Nonnull;

public abstract class ActionWithDelay extends ActionBase {
   private final double[] delayRange;
   private double delay;
   private boolean isDelaying;

   public ActionWithDelay(@Nonnull BuilderActionWithDelay builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.delayRange = builder.getDelayRange(support);
   }

   @Override
   public boolean processDelay(float dt) {
      if (!this.isDelaying) {
         return true;
      } else {
         if ((this.delay -= dt) <= 0.0) {
            this.isDelaying = false;
         }

         return !this.isDelaying;
      }
   }

   protected boolean isDelaying() {
      return this.isDelaying;
   }

   protected boolean isDelayPrepared() {
      return this.delay > 0.0;
   }

   protected void prepareDelay() {
      this.delay = RandomExtra.randomRange(this.delayRange);
      this.isDelaying = false;
   }

   protected void clearDelay() {
      this.delay = 0.0;
      this.isDelaying = false;
   }

   protected void startDelay(@Nonnull EntitySupport support) {
      support.registerDelay(this);
      this.isDelaying = true;
   }
}
