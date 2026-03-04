package com.hypixel.hytale.builtin.blockphysics;

import com.hypixel.hytale.component.data.unknown.UnknownComponents;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.hypixel.hytale.server.core.universe.world.ValidationOption;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.Set;
import javax.annotation.Nonnull;

public class WorldValidationUtil {
   @Nonnull
   public static IPrefabBuffer.RawBlockConsumer<Void> blockValidator(@Nonnull StringBuilder sb, @Nonnull Set<ValidationOption> options) {
      return blockValidator(0, 0, 0, sb, options);
   }

   @Nonnull
   public static IPrefabBuffer.RawBlockConsumer<Void> blockValidator(
      int offsetX, int offsetY, int offsetZ, @Nonnull StringBuilder sb, @Nonnull Set<ValidationOption> options
   ) {
      return (x, y, z, mask, blockId, chance, holder, support, rotation, filler, aVoid) -> {
         BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
         if (options.contains(ValidationOption.PHYSICS)) {
         }

         if (options.contains(ValidationOption.BLOCKS) && (blockType == null || blockType.isUnknown())) {
            sb.append("\tInvalid Block Type: ")
               .append(blockType == null ? "null" : blockType.getId())
               .append(" at ")
               .append('(')
               .append(x + offsetX)
               .append(',')
               .append(y + offsetY)
               .append(',')
               .append(z + offsetZ)
               .append(')')
               .append('\n');
         }

         if (options.contains(ValidationOption.BLOCK_STATES) && holder != null) {
            UnknownComponents<ChunkStore> unknownComponents = holder.getComponent(ChunkStore.REGISTRY.getUnknownComponentType());
            if (unknownComponents != null && !unknownComponents.getUnknownComponents().isEmpty()) {
               sb.append("\tUnknown Components at (")
                  .append(x + offsetX)
                  .append(',')
                  .append(y + offsetY)
                  .append(',')
                  .append(z + offsetZ)
                  .append("): ")
                  .append(unknownComponents.getUnknownComponents().keySet())
                  .append("\n");
            }
         }
      };
   }
}
