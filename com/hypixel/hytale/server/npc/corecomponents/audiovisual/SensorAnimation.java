package com.hypixel.hytale.server.npc.corecomponents.audiovisual;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.ActiveAnimationComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.animations.NPCAnimationSlot;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.audiovisual.builders.BuilderSensorAnimation;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SensorAnimation extends SensorBase {
   protected final NPCAnimationSlot slot;
   protected final String animationId;

   public SensorAnimation(@Nonnull BuilderSensorAnimation builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.slot = builder.getAnimationSlot(support);
      this.animationId = builder.getAnimationId(support);
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      ActiveAnimationComponent activeAnimationComponent = store.getComponent(ref, ActiveAnimationComponent.getComponentType());

      assert activeAnimationComponent != null;

      return super.matches(ref, role, dt, store)
         && Objects.equals(activeAnimationComponent.getActiveAnimations()[this.slot.getMappedSlot().ordinal()], this.animationId);
   }

   @Override
   public InfoProvider getSensorInfo() {
      return null;
   }
}
