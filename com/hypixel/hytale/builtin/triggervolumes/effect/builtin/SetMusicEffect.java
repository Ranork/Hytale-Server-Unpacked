package com.hypixel.hytale.builtin.triggervolumes.effect.builtin;

import com.hypixel.hytale.builtin.audio.components.ForcedMusicTracker;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.musiccontainer.config.MusicContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SetMusicEffect extends TriggerEffect {
   @Nonnull
   public static final BuilderCodec<SetMusicEffect> CODEC = BuilderCodec.builder(SetMusicEffect.class, SetMusicEffect::new, BASE_CODEC)
      .append(new KeyedCodec<>("MusicContainer", Codec.STRING), (e, v) -> e.musicContainerId = v, e -> e.musicContainerId)
      .add()
      .append(new KeyedCodec<>("ClearMusic", Codec.BOOLEAN, false), (e, v) -> e.clearMusic = v, e -> e.clearMusic)
      .add()
      .build();
   @Nullable
   private String musicContainerId;
   private boolean clearMusic;

   @Override
   public void execute(@Nonnull TriggerContext context) {
      Store<EntityStore> store = context.getStore();
      Ref<EntityStore> entityRef = context.getEntityRef();
      if (store.getComponent(entityRef, PlayerRef.getComponentType()) != null) {
         ForcedMusicTracker tracker = store.getComponent(entityRef, ForcedMusicTracker.getComponentType());
         if (tracker != null) {
            if (this.clearMusic) {
               tracker.setCurrentContainerIndex(0);
            } else if (this.musicContainerId != null && !this.musicContainerId.isBlank()) {
               int musicContainerIndex = MusicContainer.getAssetMap().getIndex(this.musicContainerId);
               if (musicContainerIndex > 0) {
                  tracker.setCurrentContainerIndex(musicContainerIndex);
               }
            }
         }
      }
   }
}
