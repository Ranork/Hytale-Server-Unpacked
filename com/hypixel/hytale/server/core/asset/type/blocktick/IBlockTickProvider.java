package com.hypixel.hytale.server.core.asset.type.blocktick;

import com.hypixel.hytale.server.core.asset.type.blocktick.config.TickProcedure;
import javax.annotation.Nullable;

@FunctionalInterface
public interface IBlockTickProvider {
   IBlockTickProvider NONE = blockId -> null;

   @Nullable
   TickProcedure getTickProcedure(int var1);
}
