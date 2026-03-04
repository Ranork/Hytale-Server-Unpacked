package com.hypixel.hytale.server.npc.corecomponents.interaction;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.interaction.builders.BuilderActionSetInteractable;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionSetInteractable extends ActionBase {
   protected final boolean setTo;
   @Nullable
   protected final String hint;
   protected final boolean showPrompt;

   public ActionSetInteractable(@Nonnull BuilderActionSetInteractable builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.setTo = builder.getSetTo(support);
      this.hint = builder.getHint();
      this.showPrompt = builder.getShowPrompt();
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      return super.canExecute(ref, role, sensorInfo, dt, store) && role.getStateSupport().getInteractionIterationTarget() != null;
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      StateSupport stateSupport = role.getStateSupport();
      stateSupport.setInteractable(ref, stateSupport.getInteractionIterationTarget(), this.setTo, this.hint, this.showPrompt, store);
      return true;
   }
}
