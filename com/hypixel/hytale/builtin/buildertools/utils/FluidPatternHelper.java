package com.hypixel.hytale.builtin.buildertools.utils;

import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.PlaceFluidInteraction;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class FluidPatternHelper {
   private FluidPatternHelper() {
   }

   @Nullable
   public static FluidPatternHelper.FluidInfo getFluidInfo(@Nonnull String itemKey) {
      Item item = Item.getAssetMap().getAsset(itemKey);
      if (item == null) {
         return null;
      } else {
         Map<InteractionType, String> interactions = item.getInteractions();
         String secondaryRootId = interactions.get(InteractionType.Secondary);
         if (secondaryRootId == null) {
            return null;
         } else {
            RootInteraction rootInteraction = RootInteraction.getAssetMap().getAsset(secondaryRootId);
            if (rootInteraction == null) {
               return null;
            } else {
               for (String interactionId : rootInteraction.getInteractionIds()) {
                  Interaction interaction = Interaction.getAssetMap().getAsset(interactionId);
                  if (interaction instanceof PlaceFluidInteraction placeFluidInteraction) {
                     String fluidKey = placeFluidInteraction.getFluidKey();
                     if (fluidKey != null) {
                        int fluidId = Fluid.getAssetMap().getIndex(fluidKey);
                        if (fluidId >= 0) {
                           Fluid fluid = Fluid.getAssetMap().getAsset(fluidId);
                           byte maxLevel = (byte)(fluid != null ? fluid.getMaxFluidLevel() : 8);
                           return new FluidPatternHelper.FluidInfo(fluidId, maxLevel);
                        }
                     }
                  }
               }

               return null;
            }
         }
      }
   }

   public static boolean isFluidItem(@Nonnull String itemKey) {
      return getFluidInfo(itemKey) != null;
   }

   @Nullable
   public static FluidPatternHelper.FluidInfo getFluidInfoFromBlockType(@Nonnull String blockTypeKey) {
      return getFluidInfo(blockTypeKey);
   }

   public record FluidInfo(int fluidId, byte fluidLevel) {
   }
}
