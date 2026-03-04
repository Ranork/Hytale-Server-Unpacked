package com.hypixel.hytale.server.npc.corecomponents.audiovisual;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.audiovisual.builders.BuilderActionPlaySound;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public class ActionPlaySound extends ActionBase {
   protected final int soundEventIndex;

   public ActionPlaySound(@Nonnull BuilderActionPlaySound builderActionPlaySound, @Nonnull BuilderSupport support) {
      super(builderActionPlaySound);
      this.soundEventIndex = builderActionPlaySound.getSoundEventIndex(support);
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      NPCEntity npcComponent = store.getComponent(ref, NPCEntity.getComponentType());

      assert npcComponent != null;

      Vector3d position = transformComponent.getPosition();
      SoundUtil.playSoundEvent3d(ref, this.soundEventIndex, position.getX(), position.getY(), position.getZ(), false, store);
      return true;
   }
}
