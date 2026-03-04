package com.hypixel.hytale.server.core.command.commands.utility.lighting;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.lighting.ChunkLightingManager;
import com.hypixel.hytale.server.core.universe.world.lighting.FloodLightCalculation;
import com.hypixel.hytale.server.core.universe.world.lighting.FullBrightLightCalculation;
import com.hypixel.hytale.server.core.universe.world.lighting.LightCalculation;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class LightingCalculationCommand extends AbstractWorldCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_INVALIDATE_LIGHTING = Message.translation("server.commands.invalidatedlighting");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_LIGHTING_CALCULATION_ALREADY_FULLBRIGHT = Message.translation(
      "server.commands.lightingcalculation.alreadyFullBright"
   );
   @Nonnull
   private final RequiredArg<LightingCalculationCommand.LightCalculationType> calculationTypeArg = this.withRequiredArg(
      "light-calculation",
      "server.commands.lightingcalculation.calculation.desc",
      ArgTypes.forEnum("server.commands.parsing.argtype.enum.name", LightingCalculationCommand.LightCalculationType.class)
   );
   @Nonnull
   private final FlagArg invalidateFlag = this.withFlagArg("invalidate", "server.commands.lightingcalculation.invalidate.desc");

   public LightingCalculationCommand() {
      super("calculation", "server.commands.lightingcalculation.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      LightingCalculationCommand.LightCalculationType type = this.calculationTypeArg.get(context);
      ChunkLightingManager chunkLighting = world.getChunkLighting();
      switch (type) {
         case FLOOD:
            chunkLighting.setLightCalculation(new FloodLightCalculation(chunkLighting));
            context.sendMessage(Message.translation("server.commands.lightingcalculation.setCalculation").param("calculation", "Flood"));
            break;
         case FULLBRIGHT:
            LightCalculation lightCalculation = chunkLighting.getLightCalculation();
            if (lightCalculation instanceof FullBrightLightCalculation) {
               context.sendMessage(MESSAGE_COMMANDS_LIGHTING_CALCULATION_ALREADY_FULLBRIGHT);
               return;
            }

            chunkLighting.setLightCalculation(new FullBrightLightCalculation(chunkLighting, lightCalculation));
            context.sendMessage(
               Message.translation("server.commands.lightcalculation.setCalculationWithDelegate")
                  .param("delegate", lightCalculation.getClass().getSimpleName())
            );
      }

      if (this.invalidateFlag.get(context)) {
         chunkLighting.invalidateLoadedChunks();
         context.sendMessage(MESSAGE_COMMANDS_INVALIDATE_LIGHTING);
      }
   }

   private static enum LightCalculationType {
      FLOOD,
      FULLBRIGHT;
   }
}
