package com.hypixel.hytale.server.npc.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import javax.annotation.Nonnull;

public class NPCAppearanceCommand extends NPCWorldCommandBase {
   @Nonnull
   private final RequiredArg<ModelAsset> modelArg = this.withRequiredArg("model", "server.commands.npc.appearance.model.desc", ArgTypes.MODEL_ASSET);

   public NPCAppearanceCommand() {
      super("appearance", "server.commands.npc.appearance.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull NPCEntity npc, @Nonnull World world, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref
   ) {
      ModelAsset model = this.modelArg.get(context);
      npc.setAppearance(ref, model, store);
   }
}
