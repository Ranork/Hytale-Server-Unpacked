package com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.EntityMatcher;
import com.hypixel.hytale.protocol.EntityMatcherType;
import com.hypixel.hytale.server.core.modules.entity.component.Invulnerable;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.SelectInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class VulnerableMatcher extends SelectInteraction.EntityMatcher {
   @Nonnull
   public static final BuilderCodec<VulnerableMatcher> CODEC = BuilderCodec.builder(VulnerableMatcher.class, VulnerableMatcher::new, BASE_CODEC)
      .documentation("Used to match any entity that is attackable")
      .build();

   @Override
   public boolean test0(@Nonnull Ref<EntityStore> sourceRef, @Nonnull Ref<EntityStore> targetRef, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      return !commandBuffer.getArchetype(targetRef).contains(Invulnerable.getComponentType());
   }

   @Nonnull
   @Override
   public EntityMatcher toPacket() {
      EntityMatcher packet = super.toPacket();
      packet.type = EntityMatcherType.VulnerableMatcher;
      return packet;
   }
}
