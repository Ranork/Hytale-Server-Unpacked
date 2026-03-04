package com.hypixel.hytale.server.npc.asset.builder.validators.asset;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.BreakBlockInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.ChangeBlockInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.MovementConditionInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.PlaceBlockInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.ListCollector;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.role.support.CombatSupport;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

public class CombatInteractionValidator extends AssetValidator {
   private static final Set<Class<? extends Interaction>> DISALLOWED_INTERACTION_TYPES = Set.of(
      BreakBlockInteraction.class, PlaceBlockInteraction.class, ChangeBlockInteraction.class, MovementConditionInteraction.class
   );
   private final List<String> disallowedInteractions = new ObjectArrayList();
   private boolean assetExists;
   private boolean attackTag;
   private boolean onlyOneAttackType;
   private boolean onlyOneAimingReference;

   private CombatInteractionValidator() {
   }

   private CombatInteractionValidator(EnumSet<AssetValidator.Config> config) {
      super(config);
   }

   @Nonnull
   @Override
   public String getDomain() {
      return "Interaction";
   }

   @Override
   public boolean test(String value) {
      RootInteraction interaction = RootInteraction.getAssetMap().getAsset(value);
      this.assetExists = interaction != null;
      if (!this.assetExists) {
         return false;
      } else {
         this.attackTag = testAttackTag(interaction);
         this.onlyOneAttackType = testOnlyOneAttackType(interaction);
         Set<String> aimingReferenceInteractions = new HashSet<>();
         this.disallowedInteractions.clear();
         Set<String> aimingReferenceTags = Interaction.getAssetMap().getKeysForTag(CombatSupport.AIMING_REFERENCE_TAG_INDEX);
         ListCollector<Object> collector = new ListCollector<>((collectorTag, interactionContext, iteratedInteraction) -> {
            if (aimingReferenceTags.contains(iteratedInteraction.getId())) {
               aimingReferenceInteractions.add(iteratedInteraction.getId());
            }

            if (DISALLOWED_INTERACTION_TYPES.contains(iteratedInteraction.getClass())) {
               this.disallowedInteractions.add(iteratedInteraction.getClass().getSimpleName());
            }

            return null;
         });
         InteractionManager.walkChain(collector, InteractionType.Primary, InteractionContext.withoutEntity(), interaction);
         this.onlyOneAimingReference = aimingReferenceInteractions.size() <= 1;
         return this.attackTag && this.onlyOneAttackType && this.onlyOneAimingReference && this.disallowedInteractions.isEmpty();
      }
   }

   @Nonnull
   @Override
   public String errorMessage(String value, String attribute) {
      if (!this.assetExists) {
         return "Interaction \"" + value + "\" does not exist for attribute \"" + attribute + "\"";
      } else {
         StringBuilder sb = new StringBuilder("Attribute \"").append(attribute).append("\" uses interaction with name \"").append(value).append("\" which:");
         if (!this.attackTag) {
            sb.append("\n  - Is not marked with the \"").append("Attack").append("\" tag");
         }

         if (!this.onlyOneAttackType) {
            sb.append("\n  - Has too many attack types (only one may be defined)");
         }

         if (!this.onlyOneAimingReference) {
            sb.append("\n  - Has too many ").append("AimingReference").append(" tags");
         }

         if (!this.disallowedInteractions.isEmpty()) {
            sb.append("\n  - Contains the following disallowed interaction types: ").append(String.join(", ", this.disallowedInteractions));
         }

         return sb.toString();
      }
   }

   @Nonnull
   @Override
   public String getAssetName() {
      return RootInteraction.class.getSimpleName();
   }

   public static boolean testAttackTag(@Nonnull RootInteraction interaction) {
      return RootInteraction.getAssetMap().getKeysForTag(CombatSupport.ATTACK_TAG_INDEX).contains(interaction.getId());
   }

   public static boolean testOnlyOneAttackType(@Nonnull RootInteraction interaction) {
      IndexedLookupTableAssetMap<String, RootInteraction> assetMap = RootInteraction.getAssetMap();
      boolean meleeTag = assetMap.getKeysForTag(CombatSupport.MELEE_TAG_INDEX).contains(interaction.getId());
      boolean rangedTag = assetMap.getKeysForTag(CombatSupport.RANGED_TAG_INDEX).contains(interaction.getId());
      boolean blockTag = assetMap.getKeysForTag(CombatSupport.BLOCK_TAG_INDEX).contains(interaction.getId());
      if (!meleeTag) {
         return rangedTag ? !blockTag : true;
      } else {
         return !rangedTag && !blockTag;
      }
   }

   @Nonnull
   public static CombatInteractionValidator required() {
      return new CombatInteractionValidator();
   }

   @Nonnull
   public static CombatInteractionValidator withConfig(EnumSet<AssetValidator.Config> config) {
      return new CombatInteractionValidator(config);
   }
}
