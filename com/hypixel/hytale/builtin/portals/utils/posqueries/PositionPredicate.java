package com.hypixel.hytale.builtin.portals.utils.posqueries;

import com.hypixel.hytale.server.core.universe.world.World;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public interface PositionPredicate {
   boolean test(@Nonnull World var1, @Nonnull Vector3d var2);
}
