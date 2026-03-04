package com.hypixel.hytale.server.npc.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EntityWrappedArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.components.messaging.BeaconSupport;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.Pair;
import javax.annotation.Nonnull;

public class NPCMessageCommand extends AbstractPlayerCommand {
   @Nonnull
   private final RequiredArg<String> messageArg = this.withRequiredArg("message", "server.commands.npc.message.message.desc", ArgTypes.STRING);
   @Nonnull
   private final OptionalArg<Double> expirationTimeArg = this.withOptionalArg("expiration", "server.commands.npc.message.expiration", ArgTypes.DOUBLE);
   @Nonnull
   private final FlagArg allArg = this.withFlagArg("all", "server.commands.npc.message.all");
   @Nonnull
   private final EntityWrappedArg entityArg = this.withOptionalArg("entity", "server.commands.entity.entity.desc", ArgTypes.ENTITY_ID);

   public NPCMessageCommand() {
      super("message", "server.commands.npc.message.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      String msg = this.messageArg.get(context);
      double expiration = this.expirationTimeArg.provided(context) ? this.expirationTimeArg.get(context) : 1.0;
      if (this.allArg.get(context)) {
         store.forEachEntityParallel(NPCEntity.getComponentType(), (index, archetypeChunk, commandBuffer) -> {
            BeaconSupport beaconSupport = archetypeChunk.getComponent(index, BeaconSupport.getComponentType());
            if (beaconSupport != null) {
               beaconSupport.postMessage(msg, ref, expiration);
            }
         });
      } else {
         Pair<Ref<EntityStore>, NPCEntity> targetNpcPair = NPCCommandUtils.getTargetNpc(context, this.entityArg, store);
         if (targetNpcPair != null) {
            Ref<EntityStore> targetNpcRef = (Ref<EntityStore>)targetNpcPair.first();
            BeaconSupport beaconSupportComponent = store.getComponent(targetNpcRef, BeaconSupport.getComponentType());
            if (beaconSupportComponent != null) {
               beaconSupportComponent.postMessage(msg, ref, expiration);
            }
         }
      }
   }
}
