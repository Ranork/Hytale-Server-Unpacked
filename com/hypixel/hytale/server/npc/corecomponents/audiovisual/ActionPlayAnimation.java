package com.hypixel.hytale.server.npc.corecomponents.audiovisual;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.animations.NPCAnimationSlot;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.audiovisual.builders.BuilderActionPlayAnimation;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionPlayAnimation extends ActionBase {
   protected final NPCAnimationSlot slot;
   @Nullable
   protected String animationId;

   public ActionPlayAnimation(@Nonnull BuilderActionPlayAnimation builderActionPlayAnimation, @Nonnull BuilderSupport support) {
      super(builderActionPlayAnimation);
      this.slot = builderActionPlayAnimation.getSlot();
      this.animationId = builderActionPlayAnimation.getAnimationId(support);
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      NPCEntity npcComponent = store.getComponent(ref, NPCEntity.getComponentType());

      assert npcComponent != null;

      npcComponent.playAnimation(ref, this.slot.getMappedSlot(), this.animationId, store);
      return true;
   }

   protected void setAnimationId(String animationId) {
      this.animationId = animationId;
   }
}
