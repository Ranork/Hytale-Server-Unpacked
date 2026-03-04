package com.hypixel.hytale.server.core.entity.entities.player.pages;

import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomPage;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class InteractiveCustomUIPage<T> extends CustomUIPage {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   protected final BuilderCodec<T> eventDataCodec;

   public InteractiveCustomUIPage(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime, @Nonnull BuilderCodec<T> eventDataCodec) {
      super(playerRef, lifetime);
      this.eventDataCodec = eventDataCodec;
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull T data) {
   }

   protected void sendUpdate(@Nullable UICommandBuilder commandBuilder, @Nullable UIEventBuilder eventBuilder, boolean clear) {
      Ref<EntityStore> ref = this.playerRef.getReference();
      if (ref != null) {
         Store<EntityStore> store = ref.getStore();
         World world = store.getExternalData().getWorld();
         world.execute(
            () -> {
               if (ref.isValid()) {
                  Player playerComponent = store.getComponent(ref, Player.getComponentType());

                  assert playerComponent != null;

                  playerComponent.getPageManager()
                     .updateCustomPage(
                        new CustomPage(
                           this.getClass().getName(),
                           false,
                           clear,
                           this.lifetime,
                           commandBuilder != null ? commandBuilder.getCommands() : UICommandBuilder.EMPTY_COMMAND_ARRAY,
                           eventBuilder != null ? eventBuilder.getEvents() : UIEventBuilder.EMPTY_EVENT_BINDING_ARRAY
                        )
                     );
               }
            }
         );
      }
   }

   @Override
   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, String rawData) {
      ExtraInfo extraInfo = ExtraInfo.THREAD_LOCAL.get();

      T data;
      try {
         data = this.eventDataCodec.decodeJson(new RawJsonReader(rawData.toCharArray()), extraInfo);
      } catch (IOException var7) {
         throw new RuntimeException(var7);
      }

      extraInfo.getValidationResults().logOrThrowValidatorExceptions(LOGGER);
      this.handleDataEvent(ref, store, data);
   }

   @Override
   protected void sendUpdate(@Nullable UICommandBuilder commandBuilder, boolean clear) {
      this.sendUpdate(commandBuilder, null, clear);
   }
}
