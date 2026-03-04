package com.hypixel.hytale.server.core.modules.entityui.asset;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.EntityUIType;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import javax.annotation.Nonnull;

public class EntityStatUIComponent extends EntityUIComponent {
   public static final BuilderCodec<EntityStatUIComponent> CODEC = BuilderCodec.builder(
         EntityStatUIComponent.class, EntityStatUIComponent::new, EntityUIComponent.ABSTRACT_CODEC
      )
      .appendInherited(
         new KeyedCodec<>("EntityStat", Codec.STRING),
         (config, s) -> config.entityStat = s,
         config -> config.entityStat,
         (config, parent) -> config.entityStat = parent.entityStat
      )
      .addValidator(Validators.nonNull())
      .addValidator(Validators.nonEmptyString())
      .addValidator(EntityStatType.VALIDATOR_CACHE.getValidator())
      .documentation("The entity stat to represent.")
      .add()
      .afterDecode(config -> config.entityStatIndex = EntityStatType.getAssetMap().getIndex(config.entityStat))
      .build();
   protected String entityStat;
   protected int entityStatIndex;

   @Nonnull
   @Override
   protected com.hypixel.hytale.protocol.EntityUIComponent generatePacket() {
      com.hypixel.hytale.protocol.EntityUIComponent packet = super.generatePacket();
      packet.type = EntityUIType.EntityStat;
      packet.entityStatIndex = this.entityStatIndex;
      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "EntityStatUIComponentConfig{entityStat='" + this.entityStat + "'entityStatIndex='" + this.entityStatIndex + "'} " + super.toString();
   }
}
