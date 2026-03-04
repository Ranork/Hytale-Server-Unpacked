package com.hypixel.hytale.server.npc.corecomponents.debug;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.debug.builders.BuilderActionLog;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class ActionLog extends ActionBase {
   protected final String text;

   public ActionLog(@Nonnull BuilderActionLog builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.text = builder.getText(support);
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      return super.canExecute(ref, role, sensorInfo, dt, store) && this.text != null;
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.INFO).atMostEvery(1, TimeUnit.SECONDS))
         .log("[%d]<%s>: %s", ref.getIndex(), role.getRoleName(), this.text);
      return true;
   }
}
