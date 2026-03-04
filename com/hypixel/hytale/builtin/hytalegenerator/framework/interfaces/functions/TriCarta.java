package com.hypixel.hytale.builtin.hytalegenerator.framework.interfaces.functions;

import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TriCarta<R> {
   @Nullable
   public abstract R apply(int var1, int var2, int var3, @Nonnull WorkerIndexer.Id var4);

   public abstract List<R> allPossibleValues();
}
