package com.hypixel.hytale.server.core.modules.interaction;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.EnumMapCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Interactions implements Component<EntityStore> {
   @Nonnull
   public static final BuilderCodec<Interactions> CODEC = BuilderCodec.builder(Interactions.class, Interactions::new)
      .appendInherited(
         new KeyedCodec<>("Interactions", new EnumMapCodec<>(InteractionType.class, Codec.STRING, false)),
         (o, v) -> o.interactions = v,
         o -> o.interactions,
         (o, p) -> o.interactions = p.interactions
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("InteractionHint", Codec.STRING),
         (o, v) -> o.interactionHint = v,
         o -> o.interactionHint,
         (o, p) -> o.interactionHint = p.interactionHint
      )
      .add()
      .build();
   @Nonnull
   private Map<InteractionType, String> interactions = new EnumMap<>(InteractionType.class);
   @Nullable
   private String interactionHint;
   private boolean isNetworkOutdated = true;

   @Nonnull
   public static ComponentType<EntityStore, Interactions> getComponentType() {
      return InteractionModule.get().getInteractionsComponentType();
   }

   public Interactions() {
   }

   public Interactions(@Nonnull Map<InteractionType, String> interactions) {
      this.interactions = new EnumMap<>(interactions);
   }

   @Nullable
   public String getInteractionId(@Nonnull InteractionType type) {
      return this.interactions.get(type);
   }

   public void setInteractionId(@Nonnull InteractionType type, @Nonnull String interactionId) {
      this.interactions.put(type, interactionId);
      this.isNetworkOutdated = true;
   }

   @Nonnull
   public Map<InteractionType, String> getInteractions() {
      return Collections.unmodifiableMap(this.interactions);
   }

   @Nullable
   public String getInteractionHint() {
      return this.interactionHint;
   }

   public void setInteractionHint(@Nullable String interactionHint) {
      this.interactionHint = interactionHint;
      this.isNetworkOutdated = true;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      Interactions clone = new Interactions(this.interactions);
      clone.interactionHint = this.interactionHint;
      return clone;
   }

   public boolean consumeNetworkOutdated() {
      boolean tmp = this.isNetworkOutdated;
      this.isNetworkOutdated = false;
      return tmp;
   }
}
