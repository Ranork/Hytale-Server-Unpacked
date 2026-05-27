package com.hypixel.hytale.builtin.audio.commands;

import com.hypixel.hytale.builtin.audio.components.AudioStateComponent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.AudioStateAuthority;
import com.hypixel.hytale.protocol.FadeCurve;
import com.hypixel.hytale.protocol.StateTransition;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.audiostate.config.AudioState;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AudioStateSetCommand extends AbstractWorldCommand {
   @Nonnull
   private final RequiredArg<AudioState> audioStateArg = this.withRequiredArg(
      "audioState", "server.commands.audio.state.set.arg.audiostate.desc", ArgTypes.AUDIO_STATE_ASSET
   );
   @Nonnull
   private final RequiredArg<String> valueArg = this.withRequiredArg("value", "server.commands.audio.state.set.arg.value.desc", ArgTypes.STRING);
   @Nonnull
   private final OptionalArg<Float> fadeMsArg = this.withOptionalArg("fadeMs", "server.commands.audio.state.set.arg.fadems.desc", ArgTypes.FLOAT);
   @Nonnull
   private final OptionalArg<FadeCurve> curveArg = this.withOptionalArg(
      "curve", "server.commands.audio.state.set.arg.curve.desc", ArgTypes.forEnum("FadeCurve", FadeCurve.class)
   );
   @Nonnull
   private final OptionalArg<AudioStateSetCommand.Scope> scopeArg = this.withOptionalArg(
      "scope", "server.commands.audio.state.set.arg.scope.desc", ArgTypes.forEnum("Scope", AudioStateSetCommand.Scope.class)
   );
   @Nonnull
   private final OptionalArg<List<PlayerRef>> playersArg = this.withListOptionalArg(
      "players", "server.commands.audio.state.set.arg.players.desc", ArgTypes.PLAYER_REF
   );

   public AudioStateSetCommand() {
      super("set", "server.commands.audio.state.set.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      AudioState audioState = this.audioStateArg.get(context);
      if (audioState.getAuthority() != AudioStateAuthority.Server) {
         context.sendMessage(Message.translation("server.commands.audio.state.set.error.clientAuthority").param("audioState", audioState.getId()));
      } else {
         String valueName = this.valueArg.get(context);
         String[] values = audioState.getValues();
         int valueIndex = -1;
         if (values != null) {
            for (int i = 0; i < values.length; i++) {
               if (values[i].equals(valueName)) {
                  valueIndex = i;
                  break;
               }
            }
         }

         if (valueIndex < 0) {
            context.sendMessage(
               Message.translation("server.commands.audio.state.set.error.unknownValue").param("audioState", audioState.getId()).param("value", valueName)
            );
         } else {
            int axisIndex = AudioState.getAssetMap().getIndex(audioState.getId());
            Float fadeMs = this.fadeMsArg.provided(context) ? this.fadeMsArg.get(context) : null;
            FadeCurve curve = this.curveArg.provided(context) ? this.curveArg.get(context) : null;
            StateTransition transitionOverride = buildTransitionOverride(fadeMs, curve);
            AudioStateSetCommand.Scope chosenScope = this.scopeArg.provided(context) ? this.scopeArg.get(context) : AudioStateSetCommand.Scope.World;
            int affected = 0;
            switch (chosenScope) {
               case World:
                  for (PlayerRef targetRef : world.getPlayerRefs()) {
                     Ref<EntityStore> ref = targetRef.getReference();
                     if (ref != null && applyToPlayer(store, ref, axisIndex, valueIndex, transitionOverride)) {
                        affected++;
                     }
                  }
                  break;
               case Self:
                  Ref<EntityStore> senderRef = context.senderAsPlayerRef();
                  if (senderRef == null) {
                     context.sendMessage(Message.translation("server.commands.audio.state.set.error.noSender"));
                     return;
                  }

                  if (applyToPlayer(store, senderRef, axisIndex, valueIndex, transitionOverride)) {
                     affected++;
                  }
                  break;
               case Players:
                  if (!this.playersArg.provided(context)) {
                     context.sendMessage(Message.translation("server.commands.audio.state.set.error.playersRequired"));
                     return;
                  }

                  List<PlayerRef> targetPlayers = this.playersArg.get(context);
                  if (targetPlayers == null || targetPlayers.isEmpty()) {
                     context.sendMessage(Message.translation("server.commands.audio.state.set.error.playersRequired"));
                     return;
                  }

                  for (PlayerRef targetPlayer : targetPlayers) {
                     if (targetPlayer != null) {
                        Ref<EntityStore> ref = targetPlayer.getReference();
                        if (ref != null && applyToPlayer(store, ref, axisIndex, valueIndex, transitionOverride)) {
                           affected++;
                        }
                     }
                  }
            }

            context.sendMessage(
               Message.translation("server.commands.audio.state.set.success")
                  .param("audioState", audioState.getId())
                  .param("value", valueName)
                  .param("count", Integer.toString(affected))
            );
         }
      }
   }

   private static boolean applyToPlayer(
      @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, int axisIndex, int valueIndex, @Nullable StateTransition transitionOverride
   ) {
      AudioStateComponent component = store.getComponent(ref, AudioStateComponent.getComponentType());
      if (component == null) {
         return false;
      } else {
         component.setValue(axisIndex, valueIndex, transitionOverride);
         return true;
      }
   }

   @Nullable
   private static StateTransition buildTransitionOverride(@Nullable Float fadeMs, @Nullable FadeCurve curve) {
      if (fadeMs == null && curve == null) {
         return null;
      } else {
         StateTransition t = new StateTransition();
         t.fromValueIndex = -1;
         t.toValueIndex = -1;
         t.durationMs = fadeMs != null ? fadeMs : 0.0F;
         t.curve = curve != null ? curve : FadeCurve.Linear;
         t.syncTo = null;
         return t;
      }
   }

   public static enum Scope {
      World,
      Self,
      Players;
   }
}
