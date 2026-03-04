package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.codec.validation.LegacyValidator;
import com.hypixel.hytale.codec.validation.ValidationResults;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class RequiredBlockFaceSupportValidator implements LegacyValidator<Map<BlockFace, RequiredBlockFaceSupport[]>> {
   static final RequiredBlockFaceSupportValidator INSTANCE = new RequiredBlockFaceSupportValidator();

   public void accept(@Nullable Map<BlockFace, RequiredBlockFaceSupport[]> support, @Nonnull ValidationResults results) {
      if (support != null) {
         for (Entry<BlockFace, RequiredBlockFaceSupport[]> entry : support.entrySet()) {
            BlockFace blockFace = entry.getKey();
            RequiredBlockFaceSupport[] requiredBlockFaceSupports = entry.getValue();

            for (int i = 0; i < requiredBlockFaceSupports.length; i++) {
               RequiredBlockFaceSupport blockFaceSupport = requiredBlockFaceSupports[i];
               if (blockFaceSupport == null) {
                  results.fail("Value for 'Support." + blockFace + "[" + i + "]' can't be null!");
               } else {
                  boolean noRequirements = blockFaceSupport.getFaceType() == null
                     && blockFaceSupport.getBlockSetId() == null
                     && blockFaceSupport.getBlockTypeId() == null
                     && blockFaceSupport.getFluidId() == null
                     && blockFaceSupport.getMatchSelf() == RequiredBlockFaceSupport.Match.IGNORED
                     && blockFaceSupport.getTagId() == null;
                  if (blockFaceSupport.getSupport() != RequiredBlockFaceSupport.Match.IGNORED && noRequirements) {
                     results.warn("Value for 'Support." + blockFace + "[" + i + "]' doesn't define any requirements and should be removed!");
                  }

                  if (blockFaceSupport.getSupport() == RequiredBlockFaceSupport.Match.IGNORED && !blockFaceSupport.allowsSupportPropagation()) {
                     results.warn("Value for 'Support." + blockFace + "[" + i + "]' doesn't allow support or support propagation so should be removed!");
                  }
               }
            }
         }
      }
   }
}
