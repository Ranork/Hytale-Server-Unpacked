package com.hypixel.hytale.builtin.hytalegenerator.framework.interfaces.functions;

import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import java.util.List;
import javax.annotation.Nonnull;

public abstract class BiCarta<R> {
   public abstract R apply(int var1, int var2, @Nonnull WorkerIndexer.Id var3);

   public abstract List<R> allPossibleValues();
}
