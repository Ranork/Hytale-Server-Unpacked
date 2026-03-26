package com.hypixel.hytale.server.npc.corecomponents.entity.filters;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterEntityEffect;
import com.hypixel.hytale.server.npc.role.Role;
import javax.annotation.Nonnull;

public class EntityFilterEntityEffect extends EntityFilterBase {
   public static final int COST = 100;
   private static final ComponentType<EntityStore, EffectControllerComponent> EFFECT_CONTROLLER_COMPONENT_TYPE = EffectControllerComponent.getComponentType();
   private final int entityEffectIndex;

   public EntityFilterEntityEffect(@Nonnull BuilderEntityFilterEntityEffect builder, @Nonnull BuilderSupport support) {
      this.entityEffectIndex = builder.getEntityEffectIndex(support);
   }

   @Override
   public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Role role, @Nonnull Store<EntityStore> store) {
      EffectControllerComponent effectController = store.getComponent(targetRef, EFFECT_CONTROLLER_COMPONENT_TYPE);
      return effectController == null ? false : effectController.hasEffect(this.entityEffectIndex);
   }

   @Override
   public int cost() {
      return 100;
   }
}
