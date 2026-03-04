package com.hypixel.hytale.builtin.adventure.npcobjectives.npc;

import com.hypixel.hytale.builtin.adventure.npcobjectives.NPCObjectivesPlugin;
import com.hypixel.hytale.builtin.adventure.npcobjectives.npc.builders.BuilderActionStartObjective;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public class ActionStartObjective extends ActionBase {
   protected final String objectiveId;

   public ActionStartObjective(@Nonnull BuilderActionStartObjective builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.objectiveId = builder.getObjectiveId(support);
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      return super.canExecute(ref, role, sensorInfo, dt, store) && role.getStateSupport().getInteractionIterationTarget() != null;
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      Ref<EntityStore> interactionIterationTarget = role.getStateSupport().getInteractionIterationTarget();
      if (interactionIterationTarget == null) {
         return false;
      } else {
         NPCObjectivesPlugin.startObjective(interactionIterationTarget, this.objectiveId, store);
         return true;
      }
   }
}
