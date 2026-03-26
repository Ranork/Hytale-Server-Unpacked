package com.hypixel.hytale.builtin.hytalegenerator.biome;

import com.hypixel.hytale.builtin.hytalegenerator.PropRuntime;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

public interface PropsSource {
   void getRuntimesWithIndex(int var1, @Nonnull Consumer<PropRuntime> var2);

   List<PropRuntime> getPropRuntimes();
}
