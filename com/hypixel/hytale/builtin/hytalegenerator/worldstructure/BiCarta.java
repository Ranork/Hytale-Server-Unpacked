package com.hypixel.hytale.builtin.hytalegenerator.worldstructure;

import com.hypixel.hytale.builtin.hytalegenerator.workerindexer.WorkerIndexer;
import java.util.List;
import javax.annotation.Nonnull;

@Deprecated
public abstract class BiCarta<R> {
   public abstract R apply(int var1, int var2, @Nonnull WorkerIndexer.Id var3);

   public abstract List<R> allPossibleValues();
}
