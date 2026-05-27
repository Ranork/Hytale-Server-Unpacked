package com.hypixel.hytale.builtin.buildertools.prefabeditor.saving;

import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.World;
import javax.annotation.Nonnull;
import org.joml.Vector3i;

public interface PrefabSaveContributor {
   void contribute(@Nonnull BlockSelection var1, @Nonnull World var2, @Nonnull Vector3i var3, @Nonnull Vector3i var4);
}
