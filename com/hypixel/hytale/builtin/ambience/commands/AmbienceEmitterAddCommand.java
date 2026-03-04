package com.hypixel.hytale.builtin.ambience.commands;

import com.hypixel.hytale.builtin.ambience.AmbiencePlugin;
import com.hypixel.hytale.builtin.ambience.components.AmbientEmitterComponent;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEventLayer;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.AssetArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.HiddenFromAdventurePlayers;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class AmbienceEmitterAddCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final AssetArgumentType<SoundEvent, ?> SOUND_EVENT_ASSET_TYPE = new AssetArgumentType(
      "server.commands.ambience.emitter.add.arg.soundEvent.name", SoundEvent.class, "server.commands.ambience.emitter.add.arg.soundEvent.usage"
   );
   @Nonnull
   private static final Message MESSAGE_SERVER_COMMANDS_ERRORS_PLAYER_ONLY = Message.translation("server.commands.errors.playerOnly");
   @Nonnull
   private final RequiredArg<SoundEvent> soundEventArg = this.withRequiredArg(
      "soundEvent", "server.commands.ambience.emitter.add.arg.soundEvent.desc", SOUND_EVENT_ASSET_TYPE
   );

   public AmbienceEmitterAddCommand() {
      super("add", "server.commands.ambience.emitter.add.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      if (!context.isPlayer()) {
         throw new GeneralCommandException(MESSAGE_SERVER_COMMANDS_ERRORS_PLAYER_ONLY);
      } else {
         TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
         if (transformComponent != null) {
            Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
            SoundEvent soundEvent = this.soundEventArg.get(context);
            boolean looping = false;

            for (SoundEventLayer layer : soundEvent.getLayers()) {
               if (layer.isLooping()) {
                  looping = true;
                  break;
               }
            }

            if (!looping) {
               context.sendMessage(Message.translation("server.commands.ambience.emitter.add.notLooping").param("soundEventId", soundEvent.getId()));
            } else {
               AmbientEmitterComponent emitterComponent = new AmbientEmitterComponent();
               emitterComponent.setSoundEventId(soundEvent.getId());
               holder.addComponent(AmbientEmitterComponent.getComponentType(), emitterComponent);
               TransformComponent emitterTransform = transformComponent.clone();
               holder.addComponent(TransformComponent.getComponentType(), emitterTransform);
               holder.addComponent(Nameplate.getComponentType(), new Nameplate(soundEvent.getId()));
               holder.ensureComponent(UUIDComponent.getComponentType());
               holder.ensureComponent(HiddenFromAdventurePlayers.getComponentType());
               Model model = AmbiencePlugin.get().getAmbientEmitterModel();
               holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
               holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
               Ref<EntityStore> emitterRef = store.addEntity(holder, AddReason.SPAWN);
               if (emitterRef != null && emitterRef.isValid()) {
                  context.sendMessage(Message.translation("server.commands.ambience.emitter.add.added").param("soundEventId", soundEvent.getId()));
               } else {
                  context.sendMessage(Message.translation("server.commands.ambience.emitter.add.failed").param("soundEventId", soundEvent.getId()));
               }
            }
         }
      }
   }
}
