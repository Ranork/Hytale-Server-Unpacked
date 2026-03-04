package com.hypixel.fastutil.longs;

import com.hypixel.fastutil.FastCollection;
import com.hypixel.fastutil.util.SneakyThrow;
import com.hypixel.fastutil.util.TLRUtil;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSpliterator;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSpliterator;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.LongConsumer;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import sun.misc.Unsafe;

public class Long2ObjectConcurrentHashMap<V> {
   protected static final long serialVersionUID = 7249069246763182397L;
   protected static final int MAXIMUM_CAPACITY = 1073741824;
   protected static final int DEFAULT_CAPACITY = 16;
   protected static final int MAX_ARRAY_SIZE = 2147483639;
   protected static final int DEFAULT_CONCURRENCY_LEVEL = 16;
   protected static final float LOAD_FACTOR = 0.75F;
   protected static final int TREEIFY_THRESHOLD = 8;
   protected static final int UNTREEIFY_THRESHOLD = 6;
   protected static final int MIN_TREEIFY_CAPACITY = 64;
   protected static final int MIN_TRANSFER_STRIDE = 16;
   protected static int RESIZE_STAMP_BITS = 16;
   protected static final int MAX_RESIZERS = (1 << 32 - RESIZE_STAMP_BITS) - 1;
   protected static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;
   protected static final int MOVED = -1;
   protected static final int TREEBIN = -2;
   protected static final int RESERVED = -3;
   protected static final int HASH_BITS = Integer.MAX_VALUE;
   protected static final int NCPU = Runtime.getRuntime().availableProcessors();
   protected transient volatile Long2ObjectConcurrentHashMap.Node<V>[] table;
   protected transient volatile Long2ObjectConcurrentHashMap.Node<V>[] nextTable;
   protected transient volatile long baseCount;
   protected transient volatile int sizeCtl;
   protected transient volatile int transferIndex;
   protected transient volatile int cellsBusy;
   protected transient volatile Long2ObjectConcurrentHashMap.CounterCell[] counterCells;
   protected transient Long2ObjectConcurrentHashMap.KeySetView<V> keySet;
   protected transient Long2ObjectConcurrentHashMap.ValuesView<V> values;
   protected transient Long2ObjectConcurrentHashMap.EntrySetView<V> entrySet;
   protected final long EMPTY;
   protected static final Unsafe U;
   protected static final long SIZECTL;
   protected static final long TRANSFERINDEX;
   protected static final long BASECOUNT;
   protected static final long CELLSBUSY;
   protected static final long CELLVALUE;
   protected static final long ABASE;
   protected static final int ASHIFT;

   protected static final int spread(int h) {
      return (h ^ h >>> 16) & 2147483647;
   }

   protected static final int tableSizeFor(int c) {
      int n = c - 1;
      n |= n >>> 1;
      n |= n >>> 2;
      n |= n >>> 4;
      n |= n >>> 8;
      n |= n >>> 16;
      return n < 0 ? 1 : (n >= 1073741824 ? 1073741824 : n + 1);
   }

   protected static final <V> Long2ObjectConcurrentHashMap.Node<V> tabAt(Long2ObjectConcurrentHashMap.Node<V>[] tab, int i) {
      return (Long2ObjectConcurrentHashMap.Node<V>)U.getObjectVolatile(tab, ((long)i << ASHIFT) + ABASE);
   }

   protected static final <V> boolean casTabAt(
      Long2ObjectConcurrentHashMap.Node<V>[] tab, int i, Long2ObjectConcurrentHashMap.Node<V> c, Long2ObjectConcurrentHashMap.Node<V> v
   ) {
      return U.compareAndSwapObject(tab, ((long)i << ASHIFT) + ABASE, c, v);
   }

   protected static final <V> void setTabAt(Long2ObjectConcurrentHashMap.Node<V>[] tab, int i, Long2ObjectConcurrentHashMap.Node<V> v) {
      U.putObjectVolatile(tab, ((long)i << ASHIFT) + ABASE, v);
   }

   public Long2ObjectConcurrentHashMap() {
      this.EMPTY = -1L;
   }

   public Long2ObjectConcurrentHashMap(boolean nonce, long emptyValue) {
      this.EMPTY = emptyValue;
   }

   public Long2ObjectConcurrentHashMap(int initialCapacity) {
      this(initialCapacity, true, -1L);
   }

   public Long2ObjectConcurrentHashMap(int initialCapacity, boolean nonce, long emptyValue) {
      if (initialCapacity < 0) {
         throw new IllegalArgumentException();
      } else {
         int cap = initialCapacity >= 536870912 ? 1073741824 : tableSizeFor(initialCapacity + (initialCapacity >>> 1) + 1);
         this.sizeCtl = cap;
         this.EMPTY = emptyValue;
      }
   }

   public Long2ObjectConcurrentHashMap(Map<? extends Long, ? extends V> m, long emptyValue) {
      this.sizeCtl = 16;
      this.EMPTY = emptyValue;
      this.putAll(m);
   }

   public Long2ObjectConcurrentHashMap(Long2ObjectConcurrentHashMap<? extends V> m) {
      this.sizeCtl = 16;
      this.EMPTY = m.EMPTY;
      this.putAll(m);
   }

   public Long2ObjectConcurrentHashMap(Long2ObjectMap<V> m) {
      this.sizeCtl = 16;
      this.EMPTY = -1L;
      this.putAll(m);
   }

   public Long2ObjectConcurrentHashMap(Long2ObjectMap<V> m, long emptyValue) {
      this.sizeCtl = 16;
      this.EMPTY = emptyValue;
      this.putAll(m);
   }

   public Long2ObjectConcurrentHashMap(int initialCapacity, float loadFactor) {
      this(initialCapacity, loadFactor, 1, -1L);
   }

   public Long2ObjectConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel, long emptyValue) {
      if (loadFactor > 0.0F && initialCapacity >= 0 && concurrencyLevel > 0) {
         if (initialCapacity < concurrencyLevel) {
            initialCapacity = concurrencyLevel;
         }

         long size = (long)(1.0 + (float)initialCapacity / loadFactor);
         int cap = size >= 1073741824L ? 1073741824 : tableSizeFor((int)size);
         this.sizeCtl = cap;
         this.EMPTY = emptyValue;
      } else {
         throw new IllegalArgumentException();
      }
   }

   public int size() {
      long n = this.sumCount();
      return n < 0L ? 0 : (n > 2147483647L ? Integer.MAX_VALUE : (int)n);
   }

   public boolean isEmpty() {
      return this.sumCount() <= 0L;
   }

   public V get(long key) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else {
         int h = spread(Long.hashCode(key));
         Long2ObjectConcurrentHashMap.Node<V>[] tab = this.table;
         Long2ObjectConcurrentHashMap.Node<V> e;
         int n;
         if (this.table != null && (n = tab.length) > 0 && (e = tabAt(tab, n - 1 & h)) != null) {
            int eh = e.hash;
            if (e.hash == h) {
               long ek = e.key;
               if (e.key == key || ek != this.EMPTY && key == ek) {
                  return e.val;
               }
            } else if (eh < 0) {
               Long2ObjectConcurrentHashMap.Node<V> p;
               return (p = e.find(h, key)) != null ? p.val : null;
            }

            while ((e = e.next) != null) {
               if (e.hash == h) {
                  long ek = e.key;
                  if (e.key == key || ek != this.EMPTY && key == ek) {
                     return e.val;
                  }
               }
            }
         }

         return null;
      }
   }

   public boolean containsKey(long key) {
      return this.get(key) != null;
   }

   public boolean containsValue(Object value) {
      if (value == null) {
         throw new NullPointerException();
      } else {
         Long2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Long2ObjectConcurrentHashMap.Node<V> next = null;
            Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Long2ObjectConcurrentHashMap.Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label85:
               while (true) {
                  if (p != null) {
                     next = p;
                     break;
                  }

                  if (baseIndex < baseLimit) {
                     Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab != null && (n = tab.length) > index && index >= 0) {
                        if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                           if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                              tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                              p = null;
                              Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                              if (spare != null) {
                                 spare = spare.next;
                              } else {
                                 s = new Long2ObjectConcurrentHashMap.TableStack<>();
                              }

                              s.tab = t;
                              s.length = n;
                              s.index = index;
                              s.next = stack;
                              stack = s;
                              continue;
                           }

                           if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                              p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                           } else {
                              p = null;
                           }
                        }

                        if (stack == null) {
                           if ((index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                           continue;
                        }

                        while (true) {
                           Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                 stack.next = spare;
                                 stack = anext;
                                 spare = s;
                                 continue;
                              }
                           }

                           if (stack == null && (index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                           continue label85;
                        }
                     }
                  }

                  next = null;
                  break;
               }

               if (p == null) {
                  break;
               }

               V v = p.val;
               if (p.val == value || v != null && value.equals(v)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public V put(long key, V value) {
      return this.putVal(key, value, false);
   }

   protected final V putVal(long key, V value, boolean onlyIfAbsent) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else if (value == null) {
         throw new NullPointerException();
      } else {
         int hash = spread(Long.hashCode(key));
         int binCount = 0;
         Long2ObjectConcurrentHashMap.Node<V>[] tab = this.table;

         while (true) {
            int n;
            while (tab == null || (n = tab.length) == 0) {
               tab = this.initTable();
            }

            Long2ObjectConcurrentHashMap.Node<V> f;
            int i;
            if ((f = tabAt(tab, i = n - 1 & hash)) == null) {
               if (casTabAt(tab, i, null, new Long2ObjectConcurrentHashMap.Node<>(this.EMPTY, hash, key, value, null))) {
                  break;
               }
            } else {
               int fh = f.hash;
               if (f.hash == -1) {
                  tab = this.helpTransfer(tab, f);
               } else {
                  V oldVal = null;
                  synchronized (f) {
                     if (tabAt(tab, i) == f) {
                        if (fh < 0) {
                           if (f instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                              binCount = 2;
                              Long2ObjectConcurrentHashMap.Node<V> p;
                              if ((p = ((Long2ObjectConcurrentHashMap.TreeBin)f).putTreeVal(hash, key, value)) != null) {
                                 oldVal = p.val;
                                 if (!onlyIfAbsent) {
                                    p.val = value;
                                 }
                              }
                           }
                        } else {
                           binCount = 1;
                           Long2ObjectConcurrentHashMap.Node<V> e = f;

                           while (true) {
                              if (e.hash == hash) {
                                 long ek = e.key;
                                 if (e.key == key || ek != this.EMPTY && key == ek) {
                                    oldVal = e.val;
                                    if (!onlyIfAbsent) {
                                       e.val = value;
                                    }
                                    break;
                                 }
                              }

                              Long2ObjectConcurrentHashMap.Node<V> pred = e;
                              if ((e = e.next) == null) {
                                 pred.next = new Long2ObjectConcurrentHashMap.Node<>(this.EMPTY, hash, key, value, null);
                                 break;
                              }

                              binCount++;
                           }
                        }
                     }
                  }

                  if (binCount != 0) {
                     if (binCount >= 8) {
                        this.treeifyBin(tab, i);
                     }

                     if (oldVal != null) {
                        return oldVal;
                     }
                     break;
                  }
               }
            }
         }

         this.addCount(1L, binCount);
         return null;
      }
   }

   public void putAll(Map<? extends Long, ? extends V> m) {
      this.tryPresize(m.size());

      for (Map.Entry<? extends Long, ? extends V> e : m.entrySet()) {
         this.putVal(e.getKey(), (V)e.getValue(), false);
      }
   }

   public void putAll(Long2ObjectConcurrentHashMap<? extends V> m) {
      this.tryPresize(m.size());
      ObjectIterator var2 = m.long2ObjectEntrySet().iterator();

      while (var2.hasNext()) {
         it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<? extends V> e = (it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<? extends V>)var2.next();
         this.putVal(e.getLongKey(), (V)e.getValue(), false);
      }
   }

   public void putAll(Long2ObjectMap<V> m) {
      this.tryPresize(m.size());
      ObjectIterator<? extends it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<? extends V>> iterator = m.long2ObjectEntrySet().iterator();

      while (iterator.hasNext()) {
         it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<? extends V> next = (it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<? extends V>)iterator.next();
         this.putVal(next.getLongKey(), (V)next.getValue(), false);
      }
   }

   public V remove(long key) {
      return this.replaceNode(key, null, null);
   }

   @Deprecated
   public V remove(Long key) {
      return this.replaceNode(key, null, null);
   }

   @Deprecated
   public V remove(Object key) {
      return this.remove((Long)key);
   }

   protected final V replaceNode(long key, V value, Object cv) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else {
         int hash = spread(Long.hashCode(key));
         Long2ObjectConcurrentHashMap.Node<V>[] tab = this.table;

         Long2ObjectConcurrentHashMap.Node<V> f;
         int n;
         int i;
         while (tab != null && (n = tab.length) != 0 && (f = tabAt(tab, i = n - 1 & hash)) != null) {
            int fh = f.hash;
            if (f.hash == -1) {
               tab = this.helpTransfer(tab, f);
            } else {
               V oldVal = null;
               boolean validated = false;
               synchronized (f) {
                  if (tabAt(tab, i) == f) {
                     if (fh < 0) {
                        if (f instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                           validated = true;
                           Long2ObjectConcurrentHashMap.TreeBin<V> t = (Long2ObjectConcurrentHashMap.TreeBin<V>)f;
                           Long2ObjectConcurrentHashMap.TreeNode<V> r = t.root;
                           Long2ObjectConcurrentHashMap.TreeNode<V> p;
                           if (t.root != null && (p = r.findTreeNode(hash, key, null)) != null) {
                              V pv = p.val;
                              if (cv == null || cv == pv || pv != null && cv.equals(pv)) {
                                 oldVal = pv;
                                 if (value != null) {
                                    p.val = value;
                                 } else if (t.removeTreeNode(p)) {
                                    setTabAt(tab, i, this.untreeify(t.first));
                                 }
                              }
                           }
                        }
                     } else {
                        validated = true;
                        Long2ObjectConcurrentHashMap.Node<V> e = f;
                        Long2ObjectConcurrentHashMap.Node<V> pred = null;

                        do {
                           if (e.hash == hash) {
                              long ek = e.key;
                              if (e.key == key || ek != this.EMPTY && key == ek) {
                                 V ev = e.val;
                                 if (cv == null || cv == ev || ev != null && cv.equals(ev)) {
                                    oldVal = ev;
                                    if (value != null) {
                                       e.val = value;
                                    } else if (pred != null) {
                                       pred.next = e.next;
                                    } else {
                                       setTabAt(tab, i, e.next);
                                    }
                                 }
                                 break;
                              }
                           }

                           pred = e;
                        } while ((e = e.next) != null);
                     }
                  }
               }

               if (validated) {
                  if (oldVal != null) {
                     if (value == null) {
                        this.addCount(-1L, -1);
                     }

                     return oldVal;
                  }
                  break;
               }
            }
         }

         return null;
      }
   }

   public void clear() {
      long delta = 0L;
      int i = 0;
      Long2ObjectConcurrentHashMap.Node<V>[] tab = this.table;

      while (tab != null && i < tab.length) {
         Long2ObjectConcurrentHashMap.Node<V> f = tabAt(tab, i);
         if (f == null) {
            i++;
         } else {
            int fh = f.hash;
            if (f.hash == -1) {
               tab = this.helpTransfer(tab, f);
               i = 0;
            } else {
               synchronized (f) {
                  if (tabAt(tab, i) == f) {
                     for (Long2ObjectConcurrentHashMap.Node<V> p = (Long2ObjectConcurrentHashMap.Node<V>)(fh >= 0
                           ? f
                           : (f instanceof Long2ObjectConcurrentHashMap.TreeBin ? ((Long2ObjectConcurrentHashMap.TreeBin)f).first : null));
                        p != null;
                        p = p.next
                     ) {
                        delta--;
                     }

                     setTabAt(tab, i++, null);
                  }
               }
            }
         }
      }

      if (delta != 0L) {
         this.addCount(delta, -1);
      }
   }

   public Long2ObjectConcurrentHashMap.KeySetView<V> keySet() {
      Long2ObjectConcurrentHashMap.KeySetView<V> ks = this.keySet;
      return this.keySet != null ? ks : (this.keySet = this.buildKeySetView());
   }

   protected Long2ObjectConcurrentHashMap.KeySetView<V> buildKeySetView() {
      return new Long2ObjectConcurrentHashMap.KeySetView<>(this, null);
   }

   public FastCollection<V> values() {
      Long2ObjectConcurrentHashMap.ValuesView<V> vs = this.values;
      return this.values != null ? vs : (this.values = this.buildValuesView());
   }

   protected Long2ObjectConcurrentHashMap.ValuesView<V> buildValuesView() {
      return new Long2ObjectConcurrentHashMap.ValuesView<>(this);
   }

   public ObjectSet<it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<V>> long2ObjectEntrySet() {
      Long2ObjectConcurrentHashMap.EntrySetView<V> es = this.entrySet;
      return this.entrySet != null ? es : (this.entrySet = this.buildEntrySetView());
   }

   @Deprecated
   public ObjectSet<Map.Entry<Long, V>> entrySet() {
      return this.long2ObjectEntrySet();
   }

   protected Long2ObjectConcurrentHashMap.EntrySetView<V> buildEntrySetView() {
      return new Long2ObjectConcurrentHashMap.EntrySetView<>(this);
   }

   @Override
   public int hashCode() {
      int h = 0;
      Long2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
      if (this.table != null) {
         Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
         Long2ObjectConcurrentHashMap.Node<V> next = null;
         Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
         Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
         int index = 0;
         int baseIndex = 0;
         int baseLimit = tt.length;
         int baseSize = tt.length;

         while (true) {
            Long2ObjectConcurrentHashMap.Node<V> p = null;
            p = next;
            if (next != null) {
               p = next.next;
            }

            label75: {
               while (true) {
                  if (p != null) {
                     next = p;
                     break label75;
                  }

                  if (baseIndex >= baseLimit) {
                     break;
                  }

                  Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                  int n;
                  if (tab == null || (n = tab.length) <= index || index < 0) {
                     break;
                  }

                  if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                     if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                        tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                        p = null;
                        Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                        if (spare != null) {
                           spare = spare.next;
                        } else {
                           s = new Long2ObjectConcurrentHashMap.TableStack<>();
                        }

                        s.tab = t;
                        s.length = n;
                        s.index = index;
                        s.next = stack;
                        stack = s;
                        continue;
                     }

                     if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                        p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                     } else {
                        p = null;
                     }
                  }

                  if (stack == null) {
                     if ((index += baseSize) >= n) {
                        index = ++baseIndex;
                     }
                  } else {
                     while (true) {
                        Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                        if (stack != null) {
                           int len = stack.length;
                           if ((index += stack.length) >= n) {
                              n = len;
                              index = stack.index;
                              tab = stack.tab;
                              stack.tab = null;
                              Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                              stack.next = spare;
                              stack = anext;
                              spare = s;
                              continue;
                           }
                        }

                        if (stack == null && (index += baseSize) >= n) {
                           index = ++baseIndex;
                        }
                        break;
                     }
                  }
               }

               next = null;
            }

            if (p == null) {
               break;
            }

            h += Long.hashCode(p.key) ^ p.val.hashCode();
         }
      }

      return h;
   }

   @Override
   public String toString() {
      Long2ObjectConcurrentHashMap.Node<V>[] t = this.table;
      int f = this.table == null ? 0 : t.length;
      Long2ObjectConcurrentHashMap.Traverser<V> it = new Long2ObjectConcurrentHashMap.Traverser<>(t, f, 0, f);
      StringBuilder sb = new StringBuilder();
      sb.append('{');
      Long2ObjectConcurrentHashMap.Node<V> p;
      if ((p = it.advance()) != null) {
         while (true) {
            long k = p.key;
            V v = p.val;
            sb.append(k);
            sb.append('=');
            sb.append(v == this ? "(this Map)" : v);
            if ((p = it.advance()) == null) {
               break;
            }

            sb.append(',').append(' ');
         }
      }

      return sb.append('}').toString();
   }

   @Override
   public boolean equals(Object o) {
      if (o != this) {
         if (!(o instanceof Long2ObjectConcurrentHashMap<?> m)) {
            return false;
         }

         Long2ObjectConcurrentHashMap.Node<V>[] t = this.table;
         int f = this.table == null ? 0 : t.length;
         Long2ObjectConcurrentHashMap.Traverser<V> it = new Long2ObjectConcurrentHashMap.Traverser<>(t, f, 0, f);

         Long2ObjectConcurrentHashMap.Node<V> p;
         while ((p = it.advance()) != null) {
            V val = p.val;
            Object v = m.get(p.key);
            if (v == null || v != val && !v.equals(val)) {
               return false;
            }
         }

         ObjectIterator var12 = m.long2ObjectEntrySet().iterator();

         while (var12.hasNext()) {
            it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<?> e = (it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<?>)var12.next();
            Object mv;
            Object v;
            long mk;
            if ((mk = e.getLongKey()) == m.EMPTY || (mv = e.getValue()) == null || (v = this.get(mk)) == null || mv != v && !mv.equals(v)) {
               return false;
            }
         }
      }

      return true;
   }

   public V putIfAbsent(long key, V value) {
      return this.putVal(key, value, true);
   }

   public boolean remove(long key, Object value) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else {
         return value != null && this.replaceNode(key, null, value) != null;
      }
   }

   public boolean replace(long key, V oldValue, V newValue) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else if (oldValue != null && newValue != null) {
         return this.replaceNode(key, newValue, oldValue) != null;
      } else {
         throw new NullPointerException();
      }
   }

   public V replace(long key, V value) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else if (value == null) {
         throw new NullPointerException();
      } else {
         return this.replaceNode(key, value, null);
      }
   }

   public V getOrDefault(long key, V defaultValue) {
      V v;
      return (v = this.get(key)) == null ? defaultValue : v;
   }

   public int forEach(Long2ObjectConcurrentHashMap.LongObjConsumer<? super V> action) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Long2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Long2ObjectConcurrentHashMap.Node<V> next = null;
            Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Long2ObjectConcurrentHashMap.Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label78: {
                  while (true) {
                     if (p != null) {
                        next = p;
                        break label78;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Long2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           index = ++baseIndex;
                        }
                     } else {
                        while (true) {
                           Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                 stack.next = spare;
                                 stack = anext;
                                 spare = s;
                                 continue;
                              }
                           }

                           if (stack == null && (index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                           break;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val);
               count++;
            }
         }

         return count;
      }
   }

   public <X> int forEach(Long2ObjectConcurrentHashMap.LongBiObjConsumer<? super V, X> action, X x) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Long2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Long2ObjectConcurrentHashMap.Node<V> next = null;
            Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Long2ObjectConcurrentHashMap.Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label78: {
                  while (true) {
                     if (p != null) {
                        next = p;
                        break label78;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Long2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           index = ++baseIndex;
                        }
                     } else {
                        while (true) {
                           Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                 stack.next = spare;
                                 stack = anext;
                                 spare = s;
                                 continue;
                              }
                           }

                           if (stack == null && (index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                           break;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, x);
               count++;
            }
         }

         return count;
      }
   }

   public <X, Y> int forEach(Long2ObjectConcurrentHashMap.LongTriObjConsumer<? super V, X, Y> action, X x, Y y) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Long2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Long2ObjectConcurrentHashMap.Node<V> next = null;
            Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Long2ObjectConcurrentHashMap.Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label78: {
                  while (true) {
                     if (p != null) {
                        next = p;
                        break label78;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Long2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           index = ++baseIndex;
                        }
                     } else {
                        while (true) {
                           Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                 stack.next = spare;
                                 stack = anext;
                                 spare = s;
                                 continue;
                              }
                           }

                           if (stack == null && (index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                           break;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, x, y);
               count++;
            }
         }

         return count;
      }
   }

   public int forEachWithByte(Long2ObjectConcurrentHashMap.LongObjByteConsumer<? super V> action, byte ii) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Long2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Long2ObjectConcurrentHashMap.Node<V> next = null;
            Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Long2ObjectConcurrentHashMap.Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label78: {
                  while (true) {
                     if (p != null) {
                        next = p;
                        break label78;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Long2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           index = ++baseIndex;
                        }
                     } else {
                        while (true) {
                           Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                 stack.next = spare;
                                 stack = anext;
                                 spare = s;
                                 continue;
                              }
                           }

                           if (stack == null && (index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                           break;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, ii);
               count++;
            }
         }

         return count;
      }
   }

   public int forEachWithShort(Long2ObjectConcurrentHashMap.LongObjShortConsumer<? super V> action, short ii) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Long2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Long2ObjectConcurrentHashMap.Node<V> next = null;
            Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Long2ObjectConcurrentHashMap.Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label78: {
                  while (true) {
                     if (p != null) {
                        next = p;
                        break label78;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Long2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           index = ++baseIndex;
                        }
                     } else {
                        while (true) {
                           Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                 stack.next = spare;
                                 stack = anext;
                                 spare = s;
                                 continue;
                              }
                           }

                           if (stack == null && (index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                           break;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, ii);
               count++;
            }
         }

         return count;
      }
   }

   public int forEachWithInt(Long2ObjectConcurrentHashMap.LongObjIntConsumer<? super V> action, int ii) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Long2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Long2ObjectConcurrentHashMap.Node<V> next = null;
            Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Long2ObjectConcurrentHashMap.Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label78: {
                  while (true) {
                     if (p != null) {
                        next = p;
                        break label78;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Long2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           index = ++baseIndex;
                        }
                     } else {
                        while (true) {
                           Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                 stack.next = spare;
                                 stack = anext;
                                 spare = s;
                                 continue;
                              }
                           }

                           if (stack == null && (index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                           break;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, ii);
               count++;
            }
         }

         return count;
      }
   }

   public int forEachWithLong(Long2ObjectConcurrentHashMap.LongObjLongConsumer<? super V> action, long ii) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Long2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Long2ObjectConcurrentHashMap.Node<V> next = null;
            Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Long2ObjectConcurrentHashMap.Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label78: {
                  while (true) {
                     if (p != null) {
                        next = p;
                        break label78;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Long2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           index = ++baseIndex;
                        }
                     } else {
                        while (true) {
                           Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                 stack.next = spare;
                                 stack = anext;
                                 spare = s;
                                 continue;
                              }
                           }

                           if (stack == null && (index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                           break;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, ii);
               count++;
            }
         }

         return count;
      }
   }

   public int forEachWithFloat(Long2ObjectConcurrentHashMap.LongObjFloatConsumer<? super V> action, float ii) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Long2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Long2ObjectConcurrentHashMap.Node<V> next = null;
            Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Long2ObjectConcurrentHashMap.Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label78: {
                  while (true) {
                     if (p != null) {
                        next = p;
                        break label78;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Long2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           index = ++baseIndex;
                        }
                     } else {
                        while (true) {
                           Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                 stack.next = spare;
                                 stack = anext;
                                 spare = s;
                                 continue;
                              }
                           }

                           if (stack == null && (index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                           break;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, ii);
               count++;
            }
         }

         return count;
      }
   }

   public int forEachWithDouble(Long2ObjectConcurrentHashMap.LongObjDoubleConsumer<? super V> action, double ii) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Long2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Long2ObjectConcurrentHashMap.Node<V> next = null;
            Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Long2ObjectConcurrentHashMap.Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label78: {
                  while (true) {
                     if (p != null) {
                        next = p;
                        break label78;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Long2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           index = ++baseIndex;
                        }
                     } else {
                        while (true) {
                           Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                 stack.next = spare;
                                 stack = anext;
                                 spare = s;
                                 continue;
                              }
                           }

                           if (stack == null && (index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                           break;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, ii);
               count++;
            }
         }

         return count;
      }
   }

   public <X> int forEachWithByte(Long2ObjectConcurrentHashMap.LongBiObjByteConsumer<? super V, X> action, byte ii, X x) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Long2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Long2ObjectConcurrentHashMap.Node<V> next = null;
            Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Long2ObjectConcurrentHashMap.Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label78: {
                  while (true) {
                     if (p != null) {
                        next = p;
                        break label78;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Long2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           index = ++baseIndex;
                        }
                     } else {
                        while (true) {
                           Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                 stack.next = spare;
                                 stack = anext;
                                 spare = s;
                                 continue;
                              }
                           }

                           if (stack == null && (index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                           break;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, ii, x);
               count++;
            }
         }

         return count;
      }
   }

   public <X> int forEachWithShort(Long2ObjectConcurrentHashMap.LongBiObjShortConsumer<? super V, X> action, short ii, X x) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Long2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Long2ObjectConcurrentHashMap.Node<V> next = null;
            Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Long2ObjectConcurrentHashMap.Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label78: {
                  while (true) {
                     if (p != null) {
                        next = p;
                        break label78;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Long2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           index = ++baseIndex;
                        }
                     } else {
                        while (true) {
                           Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                 stack.next = spare;
                                 stack = anext;
                                 spare = s;
                                 continue;
                              }
                           }

                           if (stack == null && (index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                           break;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, ii, x);
               count++;
            }
         }

         return count;
      }
   }

   public <X> int forEachWithInt(Long2ObjectConcurrentHashMap.LongBiObjIntConsumer<? super V, X> action, int ii, X x) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Long2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Long2ObjectConcurrentHashMap.Node<V> next = null;
            Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Long2ObjectConcurrentHashMap.Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label78: {
                  while (true) {
                     if (p != null) {
                        next = p;
                        break label78;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Long2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           index = ++baseIndex;
                        }
                     } else {
                        while (true) {
                           Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                 stack.next = spare;
                                 stack = anext;
                                 spare = s;
                                 continue;
                              }
                           }

                           if (stack == null && (index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                           break;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, ii, x);
               count++;
            }
         }

         return count;
      }
   }

   public <X> int forEachWithLong(Long2ObjectConcurrentHashMap.LongBiObjLongConsumer<? super V, X> action, long ii, X x) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Long2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Long2ObjectConcurrentHashMap.Node<V> next = null;
            Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Long2ObjectConcurrentHashMap.Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label78: {
                  while (true) {
                     if (p != null) {
                        next = p;
                        break label78;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Long2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           index = ++baseIndex;
                        }
                     } else {
                        while (true) {
                           Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                 stack.next = spare;
                                 stack = anext;
                                 spare = s;
                                 continue;
                              }
                           }

                           if (stack == null && (index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                           break;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, ii, x);
               count++;
            }
         }

         return count;
      }
   }

   public <X> int forEachWithFloat(Long2ObjectConcurrentHashMap.LongBiObjFloatConsumer<? super V, X> action, float ii, X x) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Long2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Long2ObjectConcurrentHashMap.Node<V> next = null;
            Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Long2ObjectConcurrentHashMap.Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label78: {
                  while (true) {
                     if (p != null) {
                        next = p;
                        break label78;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Long2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           index = ++baseIndex;
                        }
                     } else {
                        while (true) {
                           Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                 stack.next = spare;
                                 stack = anext;
                                 spare = s;
                                 continue;
                              }
                           }

                           if (stack == null && (index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                           break;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, ii, x);
               count++;
            }
         }

         return count;
      }
   }

   public <X> int forEachWithDouble(Long2ObjectConcurrentHashMap.LongBiObjDoubleConsumer<? super V, X> action, double ii, X x) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Long2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Long2ObjectConcurrentHashMap.Node<V> next = null;
            Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Long2ObjectConcurrentHashMap.Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label78: {
                  while (true) {
                     if (p != null) {
                        next = p;
                        break label78;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Long2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           index = ++baseIndex;
                        }
                     } else {
                        while (true) {
                           Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                 stack.next = spare;
                                 stack = anext;
                                 spare = s;
                                 continue;
                              }
                           }

                           if (stack == null && (index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                           break;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, ii, x);
               count++;
            }
         }

         return count;
      }
   }

   public void replaceAll(Long2ObjectOperator<V> function) {
      if (function == null) {
         throw new NullPointerException();
      } else {
         Long2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Long2ObjectConcurrentHashMap.Node<V> next = null;
            Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Long2ObjectConcurrentHashMap.Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label86: {
                  while (true) {
                     if (p != null) {
                        next = p;
                        break label86;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Long2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           index = ++baseIndex;
                        }
                     } else {
                        while (true) {
                           Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                 stack.next = spare;
                                 stack = anext;
                                 spare = s;
                                 continue;
                              }
                           }

                           if (stack == null && (index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                           break;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               V oldValue = p.val;
               long key = p.key;

               V newValue;
               do {
                  newValue = function.apply(key, oldValue);
                  if (newValue == null) {
                     throw new NullPointerException();
                  }
               } while (this.replaceNode(key, newValue, oldValue) == null && (oldValue = this.get(key)) != null);
            }
         }
      }
   }

   public V computeIfAbsent(long key, Long2ObjectConcurrentHashMap.LongFunction<? extends V> mappingFunction) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else if (mappingFunction == null) {
         throw new NullPointerException();
      } else {
         int h = spread(Long.hashCode(key));
         V val = null;
         int binCount = 0;
         Long2ObjectConcurrentHashMap.Node<V>[] tab = this.table;

         while (true) {
            int n;
            while (tab == null || (n = tab.length) == 0) {
               tab = this.initTable();
            }

            Long2ObjectConcurrentHashMap.Node<V> f;
            int i;
            if ((f = tabAt(tab, i = n - 1 & h)) == null) {
               Long2ObjectConcurrentHashMap.Node<V> r = new Long2ObjectConcurrentHashMap.ReservationNode<>(this.EMPTY);
               synchronized (r) {
                  if (casTabAt(tab, i, null, r)) {
                     binCount = 1;
                     Long2ObjectConcurrentHashMap.Node<V> node = null;

                     try {
                        if ((val = (V)mappingFunction.apply(key)) != null) {
                           node = new Long2ObjectConcurrentHashMap.Node<>(this.EMPTY, h, key, val, null);
                        }
                     } finally {
                        setTabAt(tab, i, node);
                     }
                  }
               }

               if (binCount != 0) {
                  break;
               }
            } else {
               int fh = f.hash;
               if (f.hash == -1) {
                  tab = this.helpTransfer(tab, f);
               } else {
                  boolean added = false;
                  synchronized (f) {
                     if (tabAt(tab, i) == f) {
                        if (fh < 0) {
                           if (f instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                              binCount = 2;
                              Long2ObjectConcurrentHashMap.TreeBin<V> t = (Long2ObjectConcurrentHashMap.TreeBin<V>)f;
                              Long2ObjectConcurrentHashMap.TreeNode<V> r = t.root;
                              Long2ObjectConcurrentHashMap.TreeNode<V> p;
                              if (t.root != null && (p = r.findTreeNode(h, key, null)) != null) {
                                 val = p.val;
                              } else if ((val = (V)mappingFunction.apply(key)) != null) {
                                 added = true;
                                 t.putTreeVal(h, key, val);
                              }
                           }
                        } else {
                           binCount = 1;
                           Long2ObjectConcurrentHashMap.Node<V> e = f;

                           while (true) {
                              if (e.hash == h) {
                                 long ek = e.key;
                                 if (e.key == key || ek != this.EMPTY && key == ek) {
                                    val = e.val;
                                    break;
                                 }
                              }

                              Long2ObjectConcurrentHashMap.Node<V> pred = e;
                              if ((e = e.next) == null) {
                                 if ((val = (V)mappingFunction.apply(key)) != null) {
                                    added = true;
                                    pred.next = new Long2ObjectConcurrentHashMap.Node<>(this.EMPTY, h, key, val, null);
                                 }
                                 break;
                              }

                              binCount++;
                           }
                        }
                     }
                  }

                  if (binCount != 0) {
                     if (binCount >= 8) {
                        this.treeifyBin(tab, i);
                     }

                     if (!added) {
                        return val;
                     }
                     break;
                  }
               }
            }
         }

         if (val != null) {
            this.addCount(1L, binCount);
         }

         return val;
      }
   }

   public V computeIfPresent(long key, Long2ObjectConcurrentHashMap.LongObjFunction<? super V, ? extends V> remappingFunction) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else if (remappingFunction == null) {
         throw new NullPointerException();
      } else {
         int h = spread(Long.hashCode(key));
         V val = null;
         int delta = 0;
         int binCount = 0;
         Long2ObjectConcurrentHashMap.Node<V>[] tab = this.table;

         while (true) {
            int n;
            while (tab == null || (n = tab.length) == 0) {
               tab = this.initTable();
            }

            Long2ObjectConcurrentHashMap.Node<V> f;
            int i;
            if ((f = tabAt(tab, i = n - 1 & h)) == null) {
               break;
            }

            int fh = f.hash;
            if (f.hash == -1) {
               tab = this.helpTransfer(tab, f);
            } else {
               synchronized (f) {
                  if (tabAt(tab, i) == f) {
                     if (fh < 0) {
                        if (f instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                           binCount = 2;
                           Long2ObjectConcurrentHashMap.TreeBin<V> t = (Long2ObjectConcurrentHashMap.TreeBin<V>)f;
                           Long2ObjectConcurrentHashMap.TreeNode<V> r = t.root;
                           Long2ObjectConcurrentHashMap.TreeNode<V> p;
                           if (t.root != null && (p = r.findTreeNode(h, key, null)) != null) {
                              val = (V)remappingFunction.apply(key, p.val);
                              if (val != null) {
                                 p.val = val;
                              } else {
                                 delta = -1;
                                 if (t.removeTreeNode(p)) {
                                    setTabAt(tab, i, this.untreeify(t.first));
                                 }
                              }
                           }
                        }
                     } else {
                        binCount = 1;
                        Long2ObjectConcurrentHashMap.Node<V> e = f;
                        Long2ObjectConcurrentHashMap.Node<V> pred = null;

                        while (true) {
                           if (e.hash == h) {
                              long ek = e.key;
                              if (e.key == key || ek != this.EMPTY && key == ek) {
                                 val = (V)remappingFunction.apply(key, e.val);
                                 if (val != null) {
                                    e.val = val;
                                 } else {
                                    delta = -1;
                                    Long2ObjectConcurrentHashMap.Node<V> en = e.next;
                                    if (pred != null) {
                                       pred.next = en;
                                    } else {
                                       setTabAt(tab, i, en);
                                    }
                                 }
                                 break;
                              }
                           }

                           pred = e;
                           if ((e = e.next) == null) {
                              break;
                           }

                           binCount++;
                        }
                     }
                  }
               }

               if (binCount != 0) {
                  break;
               }
            }
         }

         if (delta != 0) {
            this.addCount(delta, binCount);
         }

         return val;
      }
   }

   public V compute(long key, Long2ObjectConcurrentHashMap.LongObjFunction<? super V, ? extends V> remappingFunction) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else if (remappingFunction == null) {
         throw new NullPointerException();
      } else {
         int h = spread(Long.hashCode(key));
         V val = null;
         int delta = 0;
         int binCount = 0;
         Long2ObjectConcurrentHashMap.Node<V>[] tab = this.table;

         while (true) {
            int n;
            while (tab == null || (n = tab.length) == 0) {
               tab = this.initTable();
            }

            Long2ObjectConcurrentHashMap.Node<V> f;
            int i;
            if ((f = tabAt(tab, i = n - 1 & h)) == null) {
               Long2ObjectConcurrentHashMap.Node<V> r = new Long2ObjectConcurrentHashMap.ReservationNode<>(this.EMPTY);
               synchronized (r) {
                  if (casTabAt(tab, i, null, r)) {
                     binCount = 1;
                     Long2ObjectConcurrentHashMap.Node<V> node = null;

                     try {
                        if ((val = (V)remappingFunction.apply(key, null)) != null) {
                           delta = 1;
                           node = new Long2ObjectConcurrentHashMap.Node<>(this.EMPTY, h, key, val, null);
                        }
                     } finally {
                        setTabAt(tab, i, node);
                     }
                  }
               }

               if (binCount != 0) {
                  break;
               }
            } else {
               int fh = f.hash;
               if (f.hash == -1) {
                  tab = this.helpTransfer(tab, f);
               } else {
                  synchronized (f) {
                     if (tabAt(tab, i) == f) {
                        if (fh < 0) {
                           if (f instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                              binCount = 1;
                              Long2ObjectConcurrentHashMap.TreeBin<V> t = (Long2ObjectConcurrentHashMap.TreeBin<V>)f;
                              Long2ObjectConcurrentHashMap.TreeNode<V> r = t.root;
                              Long2ObjectConcurrentHashMap.TreeNode<V> p;
                              if (t.root != null) {
                                 p = r.findTreeNode(h, key, null);
                              } else {
                                 p = null;
                              }

                              V pv = p == null ? null : p.val;
                              val = (V)remappingFunction.apply(key, pv);
                              if (val != null) {
                                 if (p != null) {
                                    p.val = val;
                                 } else {
                                    delta = 1;
                                    t.putTreeVal(h, key, val);
                                 }
                              } else if (p != null) {
                                 delta = -1;
                                 if (t.removeTreeNode(p)) {
                                    setTabAt(tab, i, this.untreeify(t.first));
                                 }
                              }
                           }
                        } else {
                           binCount = 1;
                           Long2ObjectConcurrentHashMap.Node<V> e = f;
                           Long2ObjectConcurrentHashMap.Node<V> pred = null;

                           while (true) {
                              if (e.hash == h) {
                                 long ek = e.key;
                                 if (e.key == key || ek != this.EMPTY && key == ek) {
                                    val = (V)remappingFunction.apply(key, e.val);
                                    if (val != null) {
                                       e.val = val;
                                    } else {
                                       delta = -1;
                                       Long2ObjectConcurrentHashMap.Node<V> en = e.next;
                                       if (pred != null) {
                                          pred.next = en;
                                       } else {
                                          setTabAt(tab, i, en);
                                       }
                                    }
                                    break;
                                 }
                              }

                              pred = e;
                              if ((e = e.next) == null) {
                                 val = (V)remappingFunction.apply(key, null);
                                 if (val != null) {
                                    delta = 1;
                                    pred.next = new Long2ObjectConcurrentHashMap.Node<>(this.EMPTY, h, key, val, null);
                                 }
                                 break;
                              }

                              binCount++;
                           }
                        }
                     }
                  }

                  if (binCount != 0) {
                     if (binCount >= 8) {
                        this.treeifyBin(tab, i);
                     }
                     break;
                  }
               }
            }
         }

         if (delta != 0) {
            this.addCount(delta, binCount);
         }

         return val;
      }
   }

   public V merge(long key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else if (value != null && remappingFunction != null) {
         int h = spread(Long.hashCode(key));
         V val = null;
         int delta = 0;
         int binCount = 0;
         Long2ObjectConcurrentHashMap.Node<V>[] tab = this.table;

         while (true) {
            int n;
            while (tab == null || (n = tab.length) == 0) {
               tab = this.initTable();
            }

            Long2ObjectConcurrentHashMap.Node<V> f;
            int i;
            if ((f = tabAt(tab, i = n - 1 & h)) == null) {
               if (casTabAt(tab, i, null, new Long2ObjectConcurrentHashMap.Node<>(this.EMPTY, h, key, value, null))) {
                  delta = 1;
                  val = value;
                  break;
               }
            } else {
               int fh = f.hash;
               if (f.hash == -1) {
                  tab = this.helpTransfer(tab, f);
               } else {
                  synchronized (f) {
                     if (tabAt(tab, i) == f) {
                        if (fh < 0) {
                           if (f instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                              binCount = 2;
                              Long2ObjectConcurrentHashMap.TreeBin<V> t = (Long2ObjectConcurrentHashMap.TreeBin<V>)f;
                              Long2ObjectConcurrentHashMap.TreeNode<V> r = t.root;
                              Long2ObjectConcurrentHashMap.TreeNode<V> p = r == null ? null : r.findTreeNode(h, key, null);
                              val = p == null ? value : remappingFunction.apply(p.val, value);
                              if (val != null) {
                                 if (p != null) {
                                    p.val = val;
                                 } else {
                                    delta = 1;
                                    t.putTreeVal(h, key, val);
                                 }
                              } else if (p != null) {
                                 delta = -1;
                                 if (t.removeTreeNode(p)) {
                                    setTabAt(tab, i, this.untreeify(t.first));
                                 }
                              }
                           }
                        } else {
                           binCount = 1;
                           Long2ObjectConcurrentHashMap.Node<V> e = f;
                           Long2ObjectConcurrentHashMap.Node<V> pred = null;

                           while (true) {
                              if (e.hash == h) {
                                 long ek = e.key;
                                 if (e.key == key || ek != this.EMPTY && key == ek) {
                                    val = (V)remappingFunction.apply(e.val, value);
                                    if (val != null) {
                                       e.val = val;
                                    } else {
                                       delta = -1;
                                       Long2ObjectConcurrentHashMap.Node<V> en = e.next;
                                       if (pred != null) {
                                          pred.next = en;
                                       } else {
                                          setTabAt(tab, i, en);
                                       }
                                    }
                                    break;
                                 }
                              }

                              pred = e;
                              if ((e = e.next) == null) {
                                 delta = 1;
                                 val = value;
                                 pred.next = new Long2ObjectConcurrentHashMap.Node<>(this.EMPTY, h, key, value, null);
                                 break;
                              }

                              binCount++;
                           }
                        }
                     }
                  }

                  if (binCount != 0) {
                     if (binCount >= 8) {
                        this.treeifyBin(tab, i);
                     }
                     break;
                  }
               }
            }
         }

         if (delta != 0) {
            this.addCount(delta, binCount);
         }

         return val;
      } else {
         throw new NullPointerException();
      }
   }

   public long mappingCount() {
      long n = this.sumCount();
      return n < 0L ? 0L : n;
   }

   public static LongSet newKeySet() {
      return new Long2ObjectConcurrentHashMap.KeySetView<>(new Long2ObjectConcurrentHashMap<>(), Boolean.TRUE);
   }

   public static Long2ObjectConcurrentHashMap.KeySetView<Boolean> newKeySet(int initialCapacity) {
      return new Long2ObjectConcurrentHashMap.KeySetView<>(new Long2ObjectConcurrentHashMap<>(initialCapacity), Boolean.TRUE);
   }

   public Long2ObjectConcurrentHashMap.KeySetView<V> keySet(V mappedValue) {
      if (mappedValue == null) {
         throw new NullPointerException();
      } else {
         return new Long2ObjectConcurrentHashMap.KeySetView<>(this, mappedValue);
      }
   }

   protected static final int resizeStamp(int n) {
      return Integer.numberOfLeadingZeros(n) | 1 << RESIZE_STAMP_BITS - 1;
   }

   protected final Long2ObjectConcurrentHashMap.Node<V>[] initTable() {
      Long2ObjectConcurrentHashMap.Node<V>[] tab;
      while (true) {
         tab = this.table;
         if (this.table != null && tab.length != 0) {
            break;
         }

         int sc = this.sizeCtl;
         if (this.sizeCtl < 0) {
            Thread.yield();
         } else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
            try {
               tab = this.table;
               if (this.table == null || tab.length == 0) {
                  int n = sc > 0 ? sc : 16;
                  Long2ObjectConcurrentHashMap.Node<V>[] nt = new Long2ObjectConcurrentHashMap.Node[n];
                  tab = nt;
                  this.table = nt;
                  sc = n - (n >>> 2);
               }
               break;
            } finally {
               this.sizeCtl = sc;
            }
         }
      }

      return tab;
   }

   protected final void addCount(long x, int check) {
      boolean uncontended;
      label77: {
         long s;
         label74: {
            Long2ObjectConcurrentHashMap.CounterCell[] as = this.counterCells;
            if (this.counterCells == null) {
               long b = this.baseCount;
               if (U.compareAndSwapLong(this, BASECOUNT, this.baseCount, s = b + x)) {
                  break label74;
               }
            }

            uncontended = (boolean)1;
            Long2ObjectConcurrentHashMap.CounterCell a;
            int m;
            if (as == null || (m = as.length - 1) < 0 || (a = as[TLRUtil.getProbe() & m]) == null) {
               break label77;
            }

            long v = a.value;
            if (!(uncontended = U.compareAndSwapLong(a, CELLVALUE, a.value, v + x))) {
               break label77;
            }

            if (check <= 1) {
               return;
            }

            s = this.sumCount();
         }

         if (check >= 0) {
            while (true) {
               int sc = this.sizeCtl;
               if (s < this.sizeCtl) {
                  break;
               }

               Long2ObjectConcurrentHashMap.Node<V>[] tab = this.table;
               int n;
               if (this.table == null || (n = tab.length) >= 1073741824) {
                  break;
               }

               uncontended = (boolean)resizeStamp(n);
               if (sc < 0) {
                  if (sc >>> RESIZE_STAMP_SHIFT != uncontended || sc == uncontended + 1 || sc == uncontended + MAX_RESIZERS) {
                     break;
                  }

                  Long2ObjectConcurrentHashMap.Node<V>[] nt = this.nextTable;
                  if (this.nextTable == null || this.transferIndex <= 0) {
                     break;
                  }

                  if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1)) {
                     this.transfer(tab, nt);
                  }
               } else if (U.compareAndSwapInt(this, SIZECTL, sc, (uncontended << RESIZE_STAMP_SHIFT) + 2)) {
                  this.transfer(tab, null);
               }

               s = this.sumCount();
            }
         }

         return;
      }

      this.fullAddCount(x, uncontended);
   }

   protected final Long2ObjectConcurrentHashMap.Node<V>[] helpTransfer(Long2ObjectConcurrentHashMap.Node<V>[] tab, Long2ObjectConcurrentHashMap.Node<V> f) {
      if (tab != null && f instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
         Long2ObjectConcurrentHashMap.Node<V>[] nextTab = ((Long2ObjectConcurrentHashMap.ForwardingNode)f).nextTable;
         if (((Long2ObjectConcurrentHashMap.ForwardingNode)f).nextTable != null) {
            int rs = resizeStamp(tab.length);

            while (nextTab == this.nextTable && this.table == tab) {
               int sc = this.sizeCtl;
               if (this.sizeCtl >= 0 || sc >>> RESIZE_STAMP_SHIFT != rs || sc == rs + 1 || sc == rs + MAX_RESIZERS || this.transferIndex <= 0) {
                  break;
               }

               if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1)) {
                  this.transfer(tab, nextTab);
                  break;
               }
            }

            return nextTab;
         }
      }

      return this.table;
   }

   protected final void tryPresize(int size) {
      int c = size >= 536870912 ? 1073741824 : tableSizeFor(size + (size >>> 1) + 1);

      while (true) {
         int sc = this.sizeCtl;
         if (this.sizeCtl < 0) {
            break;
         }

         Long2ObjectConcurrentHashMap.Node<V>[] tab = this.table;
         int n;
         if (tab != null && (n = tab.length) != 0) {
            if (c <= sc || n >= 1073741824) {
               break;
            }

            if (tab == this.table) {
               int rs = resizeStamp(n);
               if (sc < 0) {
                  if (sc >>> RESIZE_STAMP_SHIFT != rs || sc == rs + 1 || sc == rs + MAX_RESIZERS) {
                     break;
                  }

                  Long2ObjectConcurrentHashMap.Node<V>[] nt = this.nextTable;
                  if (this.nextTable == null || this.transferIndex <= 0) {
                     break;
                  }

                  if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1)) {
                     this.transfer(tab, nt);
                  }
               } else if (U.compareAndSwapInt(this, SIZECTL, sc, (rs << RESIZE_STAMP_SHIFT) + 2)) {
                  this.transfer(tab, null);
               }
            }
         } else {
            n = sc > c ? sc : c;
            if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
               try {
                  if (this.table == tab) {
                     Long2ObjectConcurrentHashMap.Node<V>[] ntx = new Long2ObjectConcurrentHashMap.Node[n];
                     this.table = ntx;
                     sc = n - (n >>> 2);
                  }
               } finally {
                  this.sizeCtl = sc;
               }
            }
         }
      }
   }

   protected final void transfer(Long2ObjectConcurrentHashMap.Node<V>[] tab, Long2ObjectConcurrentHashMap.Node<V>[] nextTab) {
      int n = tab.length;
      int stride;
      if ((stride = NCPU > 1 ? (n >>> 3) / NCPU : n) < 16) {
         stride = 16;
      }

      if (nextTab == null) {
         try {
            Long2ObjectConcurrentHashMap.Node<V>[] nt = new Long2ObjectConcurrentHashMap.Node[n << 1];
            nextTab = nt;
         } catch (Throwable var27) {
            this.sizeCtl = Integer.MAX_VALUE;
            return;
         }

         this.nextTable = nextTab;
         this.transferIndex = n;
      }

      int nextn = nextTab.length;
      Long2ObjectConcurrentHashMap.ForwardingNode<V> fwd = new Long2ObjectConcurrentHashMap.ForwardingNode<>(this.EMPTY, nextTab);
      boolean advance = true;
      boolean finishing = false;
      int i = 0;
      int bound = 0;

      while (true) {
         while (!advance) {
            if (i >= 0 && i < n && i + n < nextn) {
               Long2ObjectConcurrentHashMap.Node<V> f;
               if ((f = tabAt(tab, i)) == null) {
                  advance = casTabAt(tab, i, null, fwd);
               } else {
                  int fh = f.hash;
                  if (f.hash == -1) {
                     advance = true;
                  } else {
                     synchronized (f) {
                        if (tabAt(tab, i) == f) {
                           if (fh >= 0) {
                              int runBit = fh & n;
                              Long2ObjectConcurrentHashMap.Node<V> lastRun = f;

                              for (Long2ObjectConcurrentHashMap.Node<V> p = f.next; p != null; p = p.next) {
                                 int b = p.hash & n;
                                 if (b != runBit) {
                                    runBit = b;
                                    lastRun = p;
                                 }
                              }

                              Long2ObjectConcurrentHashMap.Node<V> ln;
                              Long2ObjectConcurrentHashMap.Node<V> hn;
                              if (runBit == 0) {
                                 ln = lastRun;
                                 hn = null;
                              } else {
                                 hn = lastRun;
                                 ln = null;
                              }

                              for (Long2ObjectConcurrentHashMap.Node<V> px = f; px != lastRun; px = px.next) {
                                 int ph = px.hash;
                                 long pk = px.key;
                                 V pv = px.val;
                                 if ((ph & n) == 0) {
                                    ln = new Long2ObjectConcurrentHashMap.Node<>(this.EMPTY, ph, pk, pv, ln);
                                 } else {
                                    hn = new Long2ObjectConcurrentHashMap.Node<>(this.EMPTY, ph, pk, pv, hn);
                                 }
                              }

                              setTabAt(nextTab, i, ln);
                              setTabAt(nextTab, i + n, hn);
                              setTabAt(tab, i, fwd);
                              advance = true;
                           } else if (f instanceof Long2ObjectConcurrentHashMap.TreeBin<V> t) {
                              Long2ObjectConcurrentHashMap.TreeNode<V> lo = null;
                              Long2ObjectConcurrentHashMap.TreeNode<V> loTail = null;
                              Long2ObjectConcurrentHashMap.TreeNode<V> hi = null;
                              Long2ObjectConcurrentHashMap.TreeNode<V> hiTail = null;
                              int lc = 0;
                              int hc = 0;

                              for (Long2ObjectConcurrentHashMap.Node<V> e = t.first; e != null; e = e.next) {
                                 int h = e.hash;
                                 Long2ObjectConcurrentHashMap.TreeNode<V> pxx = new Long2ObjectConcurrentHashMap.TreeNode<>(
                                    this.EMPTY, h, e.key, e.val, null, null
                                 );
                                 if ((h & n) == 0) {
                                    if ((pxx.prev = loTail) == null) {
                                       lo = pxx;
                                    } else {
                                       loTail.next = pxx;
                                    }

                                    loTail = pxx;
                                    lc++;
                                 } else {
                                    if ((pxx.prev = hiTail) == null) {
                                       hi = pxx;
                                    } else {
                                       hiTail.next = pxx;
                                    }

                                    hiTail = pxx;
                                    hc++;
                                 }
                              }

                              Long2ObjectConcurrentHashMap.Node<V> ln = (Long2ObjectConcurrentHashMap.Node<V>)(lc <= 6
                                 ? this.untreeify(lo)
                                 : (hc != 0 ? new Long2ObjectConcurrentHashMap.TreeBin<>(this.EMPTY, lo) : t));
                              Long2ObjectConcurrentHashMap.Node<V> hn = (Long2ObjectConcurrentHashMap.Node<V>)(hc <= 6
                                 ? this.untreeify(hi)
                                 : (lc != 0 ? new Long2ObjectConcurrentHashMap.TreeBin<>(this.EMPTY, hi) : t));
                              setTabAt(nextTab, i, ln);
                              setTabAt(nextTab, i + n, hn);
                              setTabAt(tab, i, fwd);
                              advance = true;
                           }
                        }
                     }
                  }
               }
            } else {
               if (finishing) {
                  this.nextTable = null;
                  this.table = nextTab;
                  this.sizeCtl = (n << 1) - (n >>> 1);
                  return;
               }

               int sc = this.sizeCtl;
               if (U.compareAndSwapInt(this, SIZECTL, this.sizeCtl, sc - 1)) {
                  if (sc - 2 != resizeStamp(n) << RESIZE_STAMP_SHIFT) {
                     return;
                  }

                  advance = true;
                  finishing = true;
                  i = n;
               }
            }
         }

         if (--i < bound && !finishing) {
            int nextIndex = this.transferIndex;
            if (this.transferIndex <= 0) {
               i = -1;
               advance = false;
            } else {
               int nextBound;
               if (U.compareAndSwapInt(this, TRANSFERINDEX, nextIndex, nextBound = nextIndex > stride ? nextIndex - stride : 0)) {
                  bound = nextBound;
                  i = nextIndex - 1;
                  advance = false;
               }
            }
         } else {
            advance = false;
         }
      }
   }

   protected final long sumCount() {
      Long2ObjectConcurrentHashMap.CounterCell[] as = this.counterCells;
      long sum = this.baseCount;
      if (as != null) {
         for (int i = 0; i < as.length; i++) {
            Long2ObjectConcurrentHashMap.CounterCell a;
            if ((a = as[i]) != null) {
               sum += a.value;
            }
         }
      }

      return sum;
   }

   protected final void fullAddCount(long x, boolean wasUncontended) {
      int h;
      if ((h = TLRUtil.getProbe()) == 0) {
         TLRUtil.localInit();
         h = TLRUtil.getProbe();
         wasUncontended = true;
      }

      boolean collide = false;

      while (true) {
         Long2ObjectConcurrentHashMap.CounterCell[] as = this.counterCells;
         int n;
         if (this.counterCells != null && (n = as.length) > 0) {
            Long2ObjectConcurrentHashMap.CounterCell a;
            if ((a = as[n - 1 & h]) == null) {
               if (this.cellsBusy == 0) {
                  Long2ObjectConcurrentHashMap.CounterCell r = new Long2ObjectConcurrentHashMap.CounterCell(x);
                  if (this.cellsBusy == 0 && U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
                     boolean created = false;

                     try {
                        Long2ObjectConcurrentHashMap.CounterCell[] rs = this.counterCells;
                        int m;
                        int j;
                        if (this.counterCells != null && (m = rs.length) > 0 && rs[j = m - 1 & h] == null) {
                           rs[j] = r;
                           created = true;
                        }
                     } finally {
                        this.cellsBusy = 0;
                     }

                     if (created) {
                        break;
                     }
                     continue;
                  }
               }

               collide = false;
            } else if (!wasUncontended) {
               wasUncontended = true;
            } else {
               long v = a.value;
               if (U.compareAndSwapLong(a, CELLVALUE, a.value, v + x)) {
                  break;
               }

               if (this.counterCells != as || n >= NCPU) {
                  collide = false;
               } else if (!collide) {
                  collide = true;
               } else if (this.cellsBusy == 0 && U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
                  try {
                     if (this.counterCells == as) {
                        Long2ObjectConcurrentHashMap.CounterCell[] rs = new Long2ObjectConcurrentHashMap.CounterCell[n << 1];

                        for (int i = 0; i < n; i++) {
                           rs[i] = as[i];
                        }

                        this.counterCells = rs;
                     }
                  } finally {
                     this.cellsBusy = 0;
                  }

                  collide = false;
                  continue;
               }
            }

            h = TLRUtil.advanceProbe(h);
         } else if (this.cellsBusy == 0 && this.counterCells == as && U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
            boolean init = false;

            try {
               if (this.counterCells == as) {
                  Long2ObjectConcurrentHashMap.CounterCell[] rs = new Long2ObjectConcurrentHashMap.CounterCell[2];
                  rs[h & 1] = new Long2ObjectConcurrentHashMap.CounterCell(x);
                  this.counterCells = rs;
                  init = true;
               }
            } finally {
               this.cellsBusy = 0;
            }

            if (init) {
               break;
            }
         } else {
            long vx = this.baseCount;
            if (U.compareAndSwapLong(this, BASECOUNT, this.baseCount, vx + x)) {
               break;
            }
         }
      }
   }

   protected final void treeifyBin(Long2ObjectConcurrentHashMap.Node<V>[] tab, int index) {
      if (tab != null) {
         int n;
         if ((n = tab.length) < 64) {
            this.tryPresize(n << 1);
         } else {
            Long2ObjectConcurrentHashMap.Node<V> b;
            if ((b = tabAt(tab, index)) != null && b.hash >= 0) {
               synchronized (b) {
                  if (tabAt(tab, index) == b) {
                     Long2ObjectConcurrentHashMap.TreeNode<V> hd = null;
                     Long2ObjectConcurrentHashMap.TreeNode<V> tl = null;

                     for (Long2ObjectConcurrentHashMap.Node<V> e = b; e != null; e = e.next) {
                        Long2ObjectConcurrentHashMap.TreeNode<V> p = new Long2ObjectConcurrentHashMap.TreeNode<>(this.EMPTY, e.hash, e.key, e.val, null, null);
                        if ((p.prev = tl) == null) {
                           hd = p;
                        } else {
                           tl.next = p;
                        }

                        tl = p;
                     }

                     setTabAt(tab, index, new Long2ObjectConcurrentHashMap.TreeBin<>(this.EMPTY, hd));
                  }
               }
            }
         }
      }
   }

   protected <V> Long2ObjectConcurrentHashMap.Node<V> untreeify(Long2ObjectConcurrentHashMap.Node<V> b) {
      Long2ObjectConcurrentHashMap.Node<V> hd = null;
      Long2ObjectConcurrentHashMap.Node<V> tl = null;

      for (Long2ObjectConcurrentHashMap.Node<V> q = b; q != null; q = q.next) {
         Long2ObjectConcurrentHashMap.Node<V> p = new Long2ObjectConcurrentHashMap.Node<>(this.EMPTY, q.hash, q.key, q.val, null);
         if (tl == null) {
            hd = p;
         } else {
            tl.next = p;
         }

         tl = p;
      }

      return hd;
   }

   protected final int batchFor(long b) {
      long n;
      if (b != Long.MAX_VALUE && (n = this.sumCount()) > 1L && n >= b) {
         int sp = ForkJoinPool.getCommonPoolParallelism() << 2;
         long var6;
         return b > 0L && (var6 = n / b) < sp ? (int)var6 : sp;
      } else {
         return 0;
      }
   }

   public void forEach(long parallelismThreshold, Long2ObjectConcurrentHashMap.LongObjConsumer<? super V> action) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         new Long2ObjectConcurrentHashMap.ForEachMappingTask<>(null, this.batchFor(parallelismThreshold), 0, 0, this.table, action).invoke();
      }
   }

   public <U> void forEach(
      long parallelismThreshold, Long2ObjectConcurrentHashMap.LongObjFunction<? super V, ? extends U> transformer, Consumer<? super U> action
   ) {
      if (transformer != null && action != null) {
         new Long2ObjectConcurrentHashMap.ForEachTransformedMappingTask<>(null, this.batchFor(parallelismThreshold), 0, 0, this.table, transformer, action)
            .invoke();
      } else {
         throw new NullPointerException();
      }
   }

   public <U> U search(long parallelismThreshold, Long2ObjectConcurrentHashMap.LongObjFunction<? super V, ? extends U> searchFunction) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         return new Long2ObjectConcurrentHashMap.SearchMappingsTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, searchFunction, new AtomicReference()
            )
            .invoke();
      }
   }

   public <U> U search(Long2ObjectConcurrentHashMap.LongObjFunction<? super V, ? extends U> searchFunction) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         Long2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Long2ObjectConcurrentHashMap.Node<V> next = null;
            Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Long2ObjectConcurrentHashMap.Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label81: {
                  while (true) {
                     if (p != null) {
                        next = p;
                        break label81;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Long2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           index = ++baseIndex;
                        }
                     } else {
                        while (true) {
                           Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                 stack.next = spare;
                                 stack = anext;
                                 spare = s;
                                 continue;
                              }
                           }

                           if (stack == null && (index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                           break;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               U u = (U)searchFunction.apply(p.key, p.val);
               if (u != null) {
                  return u;
               }
            }
         }

         return null;
      }
   }

   public <U, X> U search(Long2ObjectConcurrentHashMap.LongBiObjFunction<? super V, X, ? extends U> searchFunction, X x) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         Long2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Long2ObjectConcurrentHashMap.Node<V> next = null;
            Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Long2ObjectConcurrentHashMap.Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label81: {
                  while (true) {
                     if (p != null) {
                        next = p;
                        break label81;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Long2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           index = ++baseIndex;
                        }
                     } else {
                        while (true) {
                           Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                 stack.next = spare;
                                 stack = anext;
                                 spare = s;
                                 continue;
                              }
                           }

                           if (stack == null && (index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                           break;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               U u = (U)searchFunction.apply(p.key, p.val, x);
               if (u != null) {
                  return u;
               }
            }
         }

         return null;
      }
   }

   public <U> U searchWithByte(Long2ObjectConcurrentHashMap.LongObjByteFunction<? super V, ? extends U> searchFunction, byte x) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         Long2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Long2ObjectConcurrentHashMap.Node<V> next = null;
            Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Long2ObjectConcurrentHashMap.Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label81: {
                  while (true) {
                     if (p != null) {
                        next = p;
                        break label81;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Long2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           index = ++baseIndex;
                        }
                     } else {
                        while (true) {
                           Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                 stack.next = spare;
                                 stack = anext;
                                 spare = s;
                                 continue;
                              }
                           }

                           if (stack == null && (index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                           break;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               U u = (U)searchFunction.apply(p.key, p.val, x);
               if (u != null) {
                  return u;
               }
            }
         }

         return null;
      }
   }

   public <U> U searchWithShort(Long2ObjectConcurrentHashMap.LongObjShortFunction<? super V, ? extends U> searchFunction, short x) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         Long2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Long2ObjectConcurrentHashMap.Node<V> next = null;
            Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Long2ObjectConcurrentHashMap.Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label81: {
                  while (true) {
                     if (p != null) {
                        next = p;
                        break label81;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Long2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           index = ++baseIndex;
                        }
                     } else {
                        while (true) {
                           Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                 stack.next = spare;
                                 stack = anext;
                                 spare = s;
                                 continue;
                              }
                           }

                           if (stack == null && (index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                           break;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               U u = (U)searchFunction.apply(p.key, p.val, x);
               if (u != null) {
                  return u;
               }
            }
         }

         return null;
      }
   }

   public <U> U searchWithInt(Long2ObjectConcurrentHashMap.LongObjIntFunction<? super V, ? extends U> searchFunction, int x) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         Long2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Long2ObjectConcurrentHashMap.Node<V> next = null;
            Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Long2ObjectConcurrentHashMap.Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label81: {
                  while (true) {
                     if (p != null) {
                        next = p;
                        break label81;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Long2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           index = ++baseIndex;
                        }
                     } else {
                        while (true) {
                           Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                 stack.next = spare;
                                 stack = anext;
                                 spare = s;
                                 continue;
                              }
                           }

                           if (stack == null && (index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                           break;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               U u = (U)searchFunction.apply(p.key, p.val, x);
               if (u != null) {
                  return u;
               }
            }
         }

         return null;
      }
   }

   public <U> U searchWithLong(Long2ObjectConcurrentHashMap.LongObjLongFunction<? super V, ? extends U> searchFunction, long x) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         Long2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Long2ObjectConcurrentHashMap.Node<V> next = null;
            Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Long2ObjectConcurrentHashMap.Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label81: {
                  while (true) {
                     if (p != null) {
                        next = p;
                        break label81;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Long2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           index = ++baseIndex;
                        }
                     } else {
                        while (true) {
                           Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                 stack.next = spare;
                                 stack = anext;
                                 spare = s;
                                 continue;
                              }
                           }

                           if (stack == null && (index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                           break;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               U u = (U)searchFunction.apply(p.key, p.val, x);
               if (u != null) {
                  return u;
               }
            }
         }

         return null;
      }
   }

   public <U> U searchWithFloat(Long2ObjectConcurrentHashMap.LongObjFloatFunction<? super V, ? extends U> searchFunction, float x) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         Long2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Long2ObjectConcurrentHashMap.Node<V> next = null;
            Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Long2ObjectConcurrentHashMap.Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label81: {
                  while (true) {
                     if (p != null) {
                        next = p;
                        break label81;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Long2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           index = ++baseIndex;
                        }
                     } else {
                        while (true) {
                           Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                 stack.next = spare;
                                 stack = anext;
                                 spare = s;
                                 continue;
                              }
                           }

                           if (stack == null && (index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                           break;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               U u = (U)searchFunction.apply(p.key, p.val, x);
               if (u != null) {
                  return u;
               }
            }
         }

         return null;
      }
   }

   public <U> U searchWithDouble(Long2ObjectConcurrentHashMap.LongObjDoubleFunction<? super V, ? extends U> searchFunction, double x) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         Long2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Long2ObjectConcurrentHashMap.Node<V> next = null;
            Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Long2ObjectConcurrentHashMap.Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label81: {
                  while (true) {
                     if (p != null) {
                        next = p;
                        break label81;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Long2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           index = ++baseIndex;
                        }
                     } else {
                        while (true) {
                           Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                 stack.next = spare;
                                 stack = anext;
                                 spare = s;
                                 continue;
                              }
                           }

                           if (stack == null && (index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                           break;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               U u = (U)searchFunction.apply(p.key, p.val, x);
               if (u != null) {
                  return u;
               }
            }
         }

         return null;
      }
   }

   public <U> U reduce(
      long parallelismThreshold,
      Long2ObjectConcurrentHashMap.LongObjFunction<? super V, ? extends U> transformer,
      BiFunction<? super U, ? super U, ? extends U> reducer
   ) {
      if (transformer != null && reducer != null) {
         return new Long2ObjectConcurrentHashMap.MapReduceMappingsTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, reducer
            )
            .invoke();
      } else {
         throw new NullPointerException();
      }
   }

   public <U> U reduce(Long2ObjectConcurrentHashMap.LongObjFunction<? super V, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
      if (transformer != null && reducer != null) {
         Long2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table == null) {
            return null;
         } else {
            Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Long2ObjectConcurrentHashMap.Node<V> next = null;
            Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;
            U r = null;

            while (true) {
               Long2ObjectConcurrentHashMap.Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label88: {
                  while (true) {
                     if (p != null) {
                        next = p;
                        break label88;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Long2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           index = ++baseIndex;
                        }
                     } else {
                        while (true) {
                           Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                 stack.next = spare;
                                 stack = anext;
                                 spare = s;
                                 continue;
                              }
                           }

                           if (stack == null && (index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                           break;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  return r;
               }

               U u;
               if ((u = (U)transformer.apply(p.key, p.val)) != null) {
                  r = r == null ? u : reducer.apply(r, u);
               }
            }
         }
      } else {
         throw new NullPointerException();
      }
   }

   public double reduceToDouble(
      long parallelismThreshold, Long2ObjectConcurrentHashMap.ToDoubleLongObjFunction<? super V> transformer, double basis, DoubleBinaryOperator reducer
   ) {
      if (transformer != null && reducer != null) {
         return new Long2ObjectConcurrentHashMap.MapReduceMappingsToDoubleTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer
            )
            .invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public long reduceToLong(
      long parallelismThreshold, Long2ObjectConcurrentHashMap.ToLongLongObjFunction<? super V> transformer, long basis, LongBinaryOperator reducer
   ) {
      if (transformer != null && reducer != null) {
         return new Long2ObjectConcurrentHashMap.MapReduceMappingsToLongTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer
            )
            .invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public int reduceToInt(
      long parallelismThreshold, Long2ObjectConcurrentHashMap.ToIntLongObjFunction<? super V> transformer, int basis, IntBinaryOperator reducer
   ) {
      if (transformer != null && reducer != null) {
         return new Long2ObjectConcurrentHashMap.MapReduceMappingsToIntTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer
            )
            .invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public void forEachKey(long parallelismThreshold, LongConsumer action) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         new Long2ObjectConcurrentHashMap.ForEachKeyTask<>(null, this.batchFor(parallelismThreshold), 0, 0, this.table, action).invoke();
      }
   }

   public <U> void forEachKey(long parallelismThreshold, Long2ObjectConcurrentHashMap.LongFunction<? extends U> transformer, Consumer<? super U> action) {
      if (transformer != null && action != null) {
         new Long2ObjectConcurrentHashMap.ForEachTransformedKeyTask<>(null, this.batchFor(parallelismThreshold), 0, 0, this.table, transformer, action)
            .invoke();
      } else {
         throw new NullPointerException();
      }
   }

   public <U> U searchKeys(long parallelismThreshold, Long2ObjectConcurrentHashMap.LongFunction<? extends U> searchFunction) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         return new Long2ObjectConcurrentHashMap.SearchKeysTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, searchFunction, new AtomicReference()
            )
            .invoke();
      }
   }

   public long reduceKeys(long parallelismThreshold, Long2ObjectConcurrentHashMap.LongReduceTaskOperator reducer) {
      if (reducer == null) {
         throw new NullPointerException();
      } else {
         return new Long2ObjectConcurrentHashMap.ReduceKeysTask<>(this.EMPTY, null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, reducer)
            .invoke0();
      }
   }

   public <U> U reduceKeys(
      long parallelismThreshold, Long2ObjectConcurrentHashMap.LongFunction<? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer
   ) {
      if (transformer != null && reducer != null) {
         return new Long2ObjectConcurrentHashMap.MapReduceKeysTask<>(null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, reducer)
            .invoke();
      } else {
         throw new NullPointerException();
      }
   }

   public double reduceKeysToDouble(
      long parallelismThreshold, Long2ObjectConcurrentHashMap.LongToDoubleFunction transformer, double basis, DoubleBinaryOperator reducer
   ) {
      if (transformer != null && reducer != null) {
         return new Long2ObjectConcurrentHashMap.MapReduceKeysToDoubleTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer
            )
            .invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public long reduceKeysToLong(long parallelismThreshold, Long2ObjectConcurrentHashMap.LongToLongFunction transformer, long basis, LongBinaryOperator reducer) {
      if (transformer != null && reducer != null) {
         return new Long2ObjectConcurrentHashMap.MapReduceKeysToLongTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer
            )
            .invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public int reduceKeysToInt(long parallelismThreshold, Long2ObjectConcurrentHashMap.LongToIntFunction transformer, int basis, IntBinaryOperator reducer) {
      if (transformer != null && reducer != null) {
         return new Long2ObjectConcurrentHashMap.MapReduceKeysToIntTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer
            )
            .invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public void forEachValue(long parallelismThreshold, Consumer<? super V> action) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         new Long2ObjectConcurrentHashMap.ForEachValueTask<>(null, this.batchFor(parallelismThreshold), 0, 0, this.table, action).invoke();
      }
   }

   public <U> void forEachValue(long parallelismThreshold, Function<? super V, ? extends U> transformer, Consumer<? super U> action) {
      if (transformer != null && action != null) {
         new Long2ObjectConcurrentHashMap.ForEachTransformedValueTask<>(null, this.batchFor(parallelismThreshold), 0, 0, this.table, transformer, action)
            .invoke();
      } else {
         throw new NullPointerException();
      }
   }

   public <U> U searchValues(long parallelismThreshold, Function<? super V, ? extends U> searchFunction) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         return new Long2ObjectConcurrentHashMap.SearchValuesTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, searchFunction, new AtomicReference()
            )
            .invoke();
      }
   }

   public V reduceValues(long parallelismThreshold, BiFunction<? super V, ? super V, ? extends V> reducer) {
      if (reducer == null) {
         throw new NullPointerException();
      } else {
         return new Long2ObjectConcurrentHashMap.ReduceValuesTask<>(null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, reducer).invoke();
      }
   }

   public <U> U reduceValues(long parallelismThreshold, Function<? super V, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
      if (transformer != null && reducer != null) {
         return new Long2ObjectConcurrentHashMap.MapReduceValuesTask<>(null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, reducer)
            .invoke();
      } else {
         throw new NullPointerException();
      }
   }

   public double reduceValuesToDouble(long parallelismThreshold, ToDoubleFunction<? super V> transformer, double basis, DoubleBinaryOperator reducer) {
      if (transformer != null && reducer != null) {
         return new Long2ObjectConcurrentHashMap.MapReduceValuesToDoubleTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer
            )
            .invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public long reduceValuesToLong(
      long parallelismThreshold, Long2ObjectConcurrentHashMap.ToLongFunction<? super V> transformer, long basis, LongBinaryOperator reducer
   ) {
      if (transformer != null && reducer != null) {
         return new Long2ObjectConcurrentHashMap.MapReduceValuesToLongTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer
            )
            .invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public int reduceValuesToInt(long parallelismThreshold, ToIntFunction<? super V> transformer, int basis, IntBinaryOperator reducer) {
      if (transformer != null && reducer != null) {
         return new Long2ObjectConcurrentHashMap.MapReduceValuesToIntTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer
            )
            .invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public void forEachEntry(long parallelismThreshold, Consumer<? super Long2ObjectConcurrentHashMap.Entry<V>> action) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         new Long2ObjectConcurrentHashMap.ForEachEntryTask<>(null, this.batchFor(parallelismThreshold), 0, 0, this.table, action).invoke();
      }
   }

   public <U> void forEachEntry(long parallelismThreshold, Function<Long2ObjectConcurrentHashMap.Entry<V>, ? extends U> transformer, Consumer<? super U> action) {
      if (transformer != null && action != null) {
         new Long2ObjectConcurrentHashMap.ForEachTransformedEntryTask<>(null, this.batchFor(parallelismThreshold), 0, 0, this.table, transformer, action)
            .invoke();
      } else {
         throw new NullPointerException();
      }
   }

   public <U> U searchEntries(long parallelismThreshold, Function<Long2ObjectConcurrentHashMap.Entry<V>, ? extends U> searchFunction) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         return new Long2ObjectConcurrentHashMap.SearchEntriesTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, searchFunction, new AtomicReference()
            )
            .invoke();
      }
   }

   public Long2ObjectConcurrentHashMap.Entry<V> reduceEntries(
      long parallelismThreshold,
      BiFunction<Long2ObjectConcurrentHashMap.Entry<V>, Long2ObjectConcurrentHashMap.Entry<V>, ? extends Long2ObjectConcurrentHashMap.Entry<V>> reducer
   ) {
      if (reducer == null) {
         throw new NullPointerException();
      } else {
         return new Long2ObjectConcurrentHashMap.ReduceEntriesTask<>(null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, reducer).invoke();
      }
   }

   public <U> U reduceEntries(
      long parallelismThreshold,
      Function<Long2ObjectConcurrentHashMap.Entry<V>, ? extends U> transformer,
      BiFunction<? super U, ? super U, ? extends U> reducer
   ) {
      if (transformer != null && reducer != null) {
         return new Long2ObjectConcurrentHashMap.MapReduceEntriesTask<>(null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, reducer)
            .invoke();
      } else {
         throw new NullPointerException();
      }
   }

   public double reduceEntriesToDouble(
      long parallelismThreshold, ToDoubleFunction<Long2ObjectConcurrentHashMap.Entry<V>> transformer, double basis, DoubleBinaryOperator reducer
   ) {
      if (transformer != null && reducer != null) {
         return new Long2ObjectConcurrentHashMap.MapReduceEntriesToDoubleTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer
            )
            .invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public long reduceEntriesToLong(
      long parallelismThreshold,
      Long2ObjectConcurrentHashMap.ToLongFunction<Long2ObjectConcurrentHashMap.Entry<V>> transformer,
      long basis,
      LongBinaryOperator reducer
   ) {
      if (transformer != null && reducer != null) {
         return new Long2ObjectConcurrentHashMap.MapReduceEntriesToLongTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer
            )
            .invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public int reduceEntriesToInt(
      long parallelismThreshold, ToIntFunction<Long2ObjectConcurrentHashMap.Entry<V>> transformer, int basis, IntBinaryOperator reducer
   ) {
      if (transformer != null && reducer != null) {
         return new Long2ObjectConcurrentHashMap.MapReduceEntriesToIntTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer
            )
            .invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public V valueMatching(Predicate<V> predicate) {
      Long2ObjectConcurrentHashMap.Node<V> next = null;
      Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
      Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
      int index = 0;
      int baseIndex = 0;
      Long2ObjectConcurrentHashMap.Node<V>[] tab = this.table;
      int f = this.table == null ? 0 : tab.length;
      int baseLimit = f;
      int baseSize = f;
      boolean b = false;

      label80:
      while (next != null || !b) {
         b |= true;
         Long2ObjectConcurrentHashMap.Node<V> e = next;
         if (next != null) {
            e = next.next;
         }

         label76:
         while (e == null) {
            if (baseIndex < baseLimit) {
               Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
               int n;
               if (tab != null && (n = tab.length) > index && index >= 0) {
                  if ((e = tabAt(tab, index)) != null && e.hash < 0) {
                     if (e instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                        tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)e).nextTable;
                        e = null;
                        Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                        if (spare != null) {
                           spare = spare.next;
                        } else {
                           s = new Long2ObjectConcurrentHashMap.TableStack<>();
                        }

                        s.tab = t;
                        s.length = n;
                        s.index = index;
                        s.next = stack;
                        stack = s;
                        continue;
                     }

                     if (e instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                        e = ((Long2ObjectConcurrentHashMap.TreeBin)e).first;
                     } else {
                        e = null;
                     }
                  }

                  if (stack == null) {
                     if ((index += baseSize) >= n) {
                        index = ++baseIndex;
                     }
                     continue;
                  }

                  while (true) {
                     Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                     if (stack != null) {
                        int len = stack.length;
                        if ((index += stack.length) >= n) {
                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           Long2ObjectConcurrentHashMap.TableStack<V> next1 = stack.next;
                           stack.next = spare;
                           stack = next1;
                           spare = s;
                           continue;
                        }
                     }

                     if (stack == null && (index += baseSize) >= n) {
                        index = ++baseIndex;
                     }
                     continue label76;
                  }
               }
            }

            next = null;
            continue label80;
         }

         next = e;
         if (predicate.test(e.val)) {
            return e.val;
         }
      }

      return null;
   }

   static {
      try {
         Field f = Unsafe.class.getDeclaredField("theUnsafe");
         f.setAccessible(true);
         U = (Unsafe)f.get(null);
         Class<?> k = Long2ObjectConcurrentHashMap.class;
         SIZECTL = U.objectFieldOffset(k.getDeclaredField("sizeCtl"));
         TRANSFERINDEX = U.objectFieldOffset(k.getDeclaredField("transferIndex"));
         BASECOUNT = U.objectFieldOffset(k.getDeclaredField("baseCount"));
         CELLSBUSY = U.objectFieldOffset(k.getDeclaredField("cellsBusy"));
         Class<?> ck = Long2ObjectConcurrentHashMap.CounterCell.class;
         CELLVALUE = U.objectFieldOffset(ck.getDeclaredField("value"));
         Class<?> ak = Long2ObjectConcurrentHashMap.Node[].class;
         ABASE = U.arrayBaseOffset(ak);
         int scale = U.arrayIndexScale(ak);
         if ((scale & scale - 1) != 0) {
            throw new Error("data type scale not a power of two");
         } else {
            ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
         }
      } catch (Exception var5) {
         throw new Error(var5);
      }
   }

   protected static class BaseIterator<V> extends Long2ObjectConcurrentHashMap.Traverser<V> {
      public final Long2ObjectConcurrentHashMap<V> map;
      public Long2ObjectConcurrentHashMap.Node<V> lastReturned;

      public BaseIterator(Long2ObjectConcurrentHashMap.Node<V>[] tab, int size, int index, int limit, Long2ObjectConcurrentHashMap<V> map) {
         super(tab, size, index, limit);
         this.map = map;
         this.advance();
      }

      public final boolean hasNext() {
         return this.next != null;
      }

      public final boolean hasMoreElements() {
         return this.next != null;
      }

      public final void remove() {
         Long2ObjectConcurrentHashMap.Node<V> p = this.lastReturned;
         if (this.lastReturned == null) {
            throw new IllegalStateException();
         } else {
            this.lastReturned = null;
            this.map.replaceNode(p.key, null, null);
         }
      }
   }

   protected abstract static class BulkTask<V, R> extends CountedCompleter<R> {
      public Long2ObjectConcurrentHashMap.Node<V>[] tab;
      public Long2ObjectConcurrentHashMap.Node<V> next;
      public Long2ObjectConcurrentHashMap.TableStack<V> stack;
      public Long2ObjectConcurrentHashMap.TableStack<V> spare;
      public int index;
      public int baseIndex;
      public int baseLimit;
      public final int baseSize;
      public int batch;

      protected BulkTask(Long2ObjectConcurrentHashMap.BulkTask<V, ?> par, int b, int i, int f, Long2ObjectConcurrentHashMap.Node<V>[] t) {
         super(par);
         this.batch = b;
         this.index = this.baseIndex = i;
         if ((this.tab = t) == null) {
            this.baseSize = this.baseLimit = 0;
         } else if (par == null) {
            this.baseSize = this.baseLimit = t.length;
         } else {
            this.baseLimit = f;
            this.baseSize = par.baseSize;
         }
      }

      protected final Long2ObjectConcurrentHashMap.Node<V> advance() {
         Long2ObjectConcurrentHashMap.Node<V> e = this.next;
         if (this.next != null) {
            e = e.next;
         }

         while (true) {
            if (e != null) {
               return this.next = e;
            }

            if (this.baseIndex >= this.baseLimit) {
               break;
            }

            Long2ObjectConcurrentHashMap.Node<V>[] t = this.tab;
            if (this.tab == null) {
               break;
            }

            int n;
            int var10000 = n = t.length;
            int i = this.index;
            if (var10000 <= this.index || i < 0) {
               break;
            }

            if ((e = Long2ObjectConcurrentHashMap.tabAt(t, i)) != null && e.hash < 0) {
               if (e instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                  this.tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)e).nextTable;
                  e = null;
                  this.pushState(t, i, n);
                  continue;
               }

               if (e instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                  e = ((Long2ObjectConcurrentHashMap.TreeBin)e).first;
               } else {
                  e = null;
               }
            }

            if (this.stack != null) {
               this.recoverState(n);
            } else if ((this.index = i + this.baseSize) >= n) {
               this.index = ++this.baseIndex;
            }
         }

         return this.next = null;
      }

      protected void pushState(Long2ObjectConcurrentHashMap.Node<V>[] t, int i, int n) {
         Long2ObjectConcurrentHashMap.TableStack<V> s = this.spare;
         if (s != null) {
            this.spare = s.next;
         } else {
            s = new Long2ObjectConcurrentHashMap.TableStack<>();
         }

         s.tab = t;
         s.length = n;
         s.index = i;
         s.next = this.stack;
         this.stack = s;
      }

      protected void recoverState(int n) {
         while (true) {
            Long2ObjectConcurrentHashMap.TableStack<V> s = this.stack;
            if (this.stack != null) {
               int len = s.length;
               if ((this.index = this.index + s.length) >= n) {
                  n = len;
                  this.index = s.index;
                  this.tab = s.tab;
                  s.tab = null;
                  Long2ObjectConcurrentHashMap.TableStack<V> next = s.next;
                  s.next = this.spare;
                  this.stack = next;
                  this.spare = s;
                  continue;
               }
            }

            if (s == null && (this.index = this.index + this.baseSize) >= n) {
               this.index = ++this.baseIndex;
            }

            return;
         }
      }
   }

   protected abstract static class CollectionView<K, E> implements ObjectCollection<E>, Serializable {
      public static final long serialVersionUID = 7249069246763182397L;
      public final Long2ObjectConcurrentHashMap<K> map;
      protected static final String oomeMsg = "Required array size too large";

      public CollectionView(Long2ObjectConcurrentHashMap<K> map) {
         this.map = map;
      }

      public Long2ObjectConcurrentHashMap<K> getMap() {
         return this.map;
      }

      public final void clear() {
         this.map.clear();
      }

      public final int size() {
         return this.map.size();
      }

      public final boolean isEmpty() {
         return this.map.isEmpty();
      }

      public abstract ObjectIterator<E> iterator();

      public abstract boolean contains(Object var1);

      public abstract boolean remove(Object var1);

      public final Object[] toArray() {
         long sz = this.map.mappingCount();
         if (sz > 2147483639L) {
            throw new OutOfMemoryError("Required array size too large");
         } else {
            int n = (int)sz;
            Object[] r = new Object[n];
            int i = 0;
            ObjectIterator var6 = this.iterator();

            while (var6.hasNext()) {
               E e = (E)var6.next();
               if (i == n) {
                  if (n >= 2147483639) {
                     throw new OutOfMemoryError("Required array size too large");
                  }

                  if (n >= 1073741819) {
                     n = 2147483639;
                  } else {
                     n += (n >>> 1) + 1;
                  }

                  r = Arrays.copyOf(r, n);
               }

               r[i++] = e;
            }

            return i == n ? r : Arrays.copyOf(r, i);
         }
      }

      public final <T> T[] toArray(T[] a) {
         long sz = this.map.mappingCount();
         if (sz > 2147483639L) {
            throw new OutOfMemoryError("Required array size too large");
         } else {
            int m = (int)sz;
            T[] r = (T[])(a.length >= m ? a : (Object[])Array.newInstance(a.getClass().getComponentType(), m));
            int n = r.length;
            int i = 0;
            ObjectIterator var8 = this.iterator();

            while (var8.hasNext()) {
               E e = (E)var8.next();
               if (i == n) {
                  if (n >= 2147483639) {
                     throw new OutOfMemoryError("Required array size too large");
                  }

                  if (n >= 1073741819) {
                     n = 2147483639;
                  } else {
                     n += (n >>> 1) + 1;
                  }

                  r = (T[])Arrays.copyOf(r, n);
               }

               r[i++] = (T)e;
            }

            if (a == r && i < n) {
               r[i] = null;
               return r;
            } else {
               return (T[])(i == n ? r : Arrays.copyOf(r, i));
            }
         }
      }

      @Override
      public final String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append('[');
         Iterator<E> it = this.iterator();
         if (it.hasNext()) {
            while (true) {
               Object e = it.next();
               sb.append(e == this ? "(this Collection)" : e);
               if (!it.hasNext()) {
                  break;
               }

               sb.append(',').append(' ');
            }
         }

         return sb.append(']').toString();
      }

      public final boolean containsAll(Collection<?> c) {
         if (c != this) {
            for (Object e : c) {
               if (e == null || !this.contains(e)) {
                  return false;
               }
            }
         }

         return true;
      }

      public final boolean removeAll(Collection<?> c) {
         if (c == null) {
            throw new NullPointerException();
         } else {
            boolean modified = false;
            Iterator<E> it = this.iterator();

            while (it.hasNext()) {
               if (c.contains(it.next())) {
                  it.remove();
                  modified = true;
               }
            }

            return modified;
         }
      }

      public final boolean retainAll(Collection<?> c) {
         if (c == null) {
            throw new NullPointerException();
         } else {
            boolean modified = false;
            Iterator<E> it = this.iterator();

            while (it.hasNext()) {
               if (!c.contains(it.next())) {
                  it.remove();
                  modified = true;
               }
            }

            return modified;
         }
      }
   }

   protected static final class CounterCell {
      public volatile long value;

      public CounterCell(long x) {
         this.value = x;
      }
   }

   protected abstract static class DoubleReturningBulkTask<V> extends Long2ObjectConcurrentHashMap.BulkTask<V, Double> {
      public double result;

      public DoubleReturningBulkTask(Long2ObjectConcurrentHashMap.BulkTask<V, ?> par, int b, int i, int f, Long2ObjectConcurrentHashMap.Node<V>[] t) {
         super(par, b, i, f, t);
      }

      protected double invoke0() {
         this.quietlyInvoke();
         Throwable exc = this.getException();
         if (exc != null) {
            throw SneakyThrow.sneakyThrow(exc);
         } else {
            return this.result;
         }
      }
   }

   public interface Entry<V> extends it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<V> {
      boolean isEmpty();

      @Deprecated
      Long getKey();

      long getLongKey();

      V getValue();

      @Override
      int hashCode();

      @Override
      String toString();

      @Override
      boolean equals(Object var1);

      V setValue(V var1);
   }

   protected static final class EntryIterator<V>
      extends Long2ObjectConcurrentHashMap.BaseIterator<V>
      implements ObjectIterator<it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<V>> {
      public EntryIterator(Long2ObjectConcurrentHashMap.Node<V>[] tab, int index, int size, int limit, Long2ObjectConcurrentHashMap<V> map) {
         super(tab, index, size, limit, map);
      }

      public final Long2ObjectConcurrentHashMap.Entry<V> next() {
         Long2ObjectConcurrentHashMap.Node<V> p = this.next;
         if (this.next == null) {
            throw new NoSuchElementException();
         } else {
            long k = p.key;
            V v = p.val;
            this.lastReturned = p;
            this.advance();
            return new Long2ObjectConcurrentHashMap.MapEntry<>(p.isEmpty(), k, v, this.map);
         }
      }
   }

   protected static final class EntrySetView<V>
      extends Long2ObjectConcurrentHashMap.CollectionView<V, it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<V>>
      implements ObjectSet<it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<V>>,
      Serializable {
      public static final long serialVersionUID = 2249069246763182397L;

      public EntrySetView(Long2ObjectConcurrentHashMap<V> map) {
         super(map);
      }

      @Override
      public boolean contains(Object o) {
         if (o instanceof it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry) {
            it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<?> e;
            long k = (e = (it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<?>)o).getLongKey();
            if (!((Long2ObjectConcurrentHashMap.Entry)o).isEmpty()) {
               Object v;
               Object r;
               return (r = this.map.get(k)) != null && (v = e.getValue()) != null && (v == r || v.equals(r));
            }
         }

         return false;
      }

      @Override
      public boolean remove(Object o) {
         if (o instanceof it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry) {
            it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<?> e;
            long k = (e = (it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<?>)o).getLongKey();
            if (!((Long2ObjectConcurrentHashMap.Entry)o).isEmpty()) {
               Object v;
               return (v = e.getValue()) != null && this.map.remove(k, v);
            }
         }

         return false;
      }

      @Override
      public ObjectIterator<it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<V>> iterator() {
         Long2ObjectConcurrentHashMap<V> m = this.map;
         Long2ObjectConcurrentHashMap.Node<V>[] t = m.table;
         int f = m.table == null ? 0 : t.length;
         return new Long2ObjectConcurrentHashMap.EntryIterator<>(t, f, 0, f, m);
      }

      public boolean add(it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<V> e) {
         return this.map.putVal(e.getLongKey(), (V)e.getValue(), false) == null;
      }

      public boolean addAll(Collection<? extends it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<V>> c) {
         boolean added = false;

         for (it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<V> e : c) {
            if (this.add(e)) {
               added = true;
            }
         }

         return added;
      }

      @Override
      public final int hashCode() {
         int h = 0;
         Long2ObjectConcurrentHashMap.Node<V>[] t = this.map.table;
         if (this.map.table != null) {
            Long2ObjectConcurrentHashMap.Traverser<V> it = new Long2ObjectConcurrentHashMap.Traverser<>(t, t.length, 0, t.length);

            Long2ObjectConcurrentHashMap.Node<V> p;
            while ((p = it.advance()) != null) {
               h += p.hashCode();
            }
         }

         return h;
      }

      @Override
      public final boolean equals(Object o) {
         Set<?> c;
         return o instanceof Set && ((c = (Set<?>)o) == this || this.containsAll(c) && c.containsAll(this));
      }

      public ObjectSpliterator<it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<V>> spliterator() {
         Long2ObjectConcurrentHashMap<V> m = this.map;
         long n = m.sumCount();
         Long2ObjectConcurrentHashMap.Node<V>[] t = m.table;
         int f = m.table == null ? 0 : t.length;
         return new Long2ObjectConcurrentHashMap.EntrySpliterator<>(t, f, 0, f, n < 0L ? 0L : n, m);
      }

      public void forEach(Consumer<? super it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<V>> action) {
         if (action == null) {
            throw new NullPointerException();
         } else {
            Long2ObjectConcurrentHashMap.Node<V>[] t = this.map.table;
            if (this.map.table != null) {
               Long2ObjectConcurrentHashMap.Traverser<V> it = new Long2ObjectConcurrentHashMap.Traverser<>(t, t.length, 0, t.length);

               Long2ObjectConcurrentHashMap.Node<V> p;
               while ((p = it.advance()) != null) {
                  action.accept(new Long2ObjectConcurrentHashMap.MapEntry<>(p.isEmpty(), p.key, p.val, this.map));
               }
            }
         }
      }
   }

   protected static final class EntrySpliterator<V>
      extends Long2ObjectConcurrentHashMap.Traverser<V>
      implements ObjectSpliterator<it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<V>> {
      public final Long2ObjectConcurrentHashMap<V> map;
      public long est;

      public EntrySpliterator(Long2ObjectConcurrentHashMap.Node<V>[] tab, int size, int index, int limit, long est, Long2ObjectConcurrentHashMap<V> map) {
         super(tab, size, index, limit);
         this.map = map;
         this.est = est;
      }

      public ObjectSpliterator<it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<V>> trySplit() {
         int i = this.baseIndex;
         int f = this.baseLimit;
         int h;
         return (h = this.baseIndex + this.baseLimit >>> 1) <= i
            ? null
            : new Long2ObjectConcurrentHashMap.EntrySpliterator<>(this.tab, this.baseSize, this.baseLimit = h, f, this.est >>>= 1, this.map);
      }

      public void forEachRemaining(Consumer<? super it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<V>> action) {
         if (action == null) {
            throw new NullPointerException();
         } else {
            Long2ObjectConcurrentHashMap.Node<V> p;
            while ((p = this.advance()) != null) {
               action.accept(new Long2ObjectConcurrentHashMap.MapEntry<>(p.isEmpty(), p.key, p.val, this.map));
            }
         }
      }

      public boolean tryAdvance(Consumer<? super it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<V>> action) {
         if (action == null) {
            throw new NullPointerException();
         } else {
            Long2ObjectConcurrentHashMap.Node<V> p;
            if ((p = this.advance()) == null) {
               return false;
            } else {
               action.accept(new Long2ObjectConcurrentHashMap.MapEntry<>(p.isEmpty(), p.key, p.val, this.map));
               return true;
            }
         }
      }

      public long estimateSize() {
         return this.est;
      }

      public int characteristics() {
         return 4353;
      }
   }

   protected static final class ForEachEntryTask<V> extends Long2ObjectConcurrentHashMap.BulkTask<V, Void> {
      public final Consumer<? super Long2ObjectConcurrentHashMap.Entry<V>> action;

      public ForEachEntryTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Consumer<? super Long2ObjectConcurrentHashMap.Entry<V>> action
      ) {
         super(p, b, i, f, t);
         this.action = action;
      }

      @Override
      public final void compute() {
         Consumer<? super Long2ObjectConcurrentHashMap.Entry<V>> action = this.action;
         if (this.action != null) {
            int i = this.baseIndex;

            while (this.batch > 0) {
               int f = this.baseLimit;
               int h;
               if ((h = this.baseLimit + i >>> 1) <= i) {
                  break;
               }

               this.addToPendingCount(1);
               new Long2ObjectConcurrentHashMap.ForEachEntryTask<>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, action).fork();
            }

            while ((p = this.advance()) != null) {
               action.accept(p);
            }

            this.propagateCompletion();
         }
      }
   }

   protected static final class ForEachKeyTask<V> extends Long2ObjectConcurrentHashMap.BulkTask<V, Void> {
      public final LongConsumer action;

      public ForEachKeyTask(Long2ObjectConcurrentHashMap.BulkTask<V, ?> p, int b, int i, int f, Long2ObjectConcurrentHashMap.Node<V>[] t, LongConsumer action) {
         super(p, b, i, f, t);
         this.action = action;
      }

      @Override
      public final void compute() {
         LongConsumer action = this.action;
         if (this.action != null) {
            int i = this.baseIndex;

            while (this.batch > 0) {
               int f = this.baseLimit;
               int h;
               if ((h = this.baseLimit + i >>> 1) <= i) {
                  break;
               }

               this.addToPendingCount(1);
               new Long2ObjectConcurrentHashMap.ForEachKeyTask<>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, action).fork();
            }

            while ((p = this.advance()) != null) {
               action.accept(p.key);
            }

            this.propagateCompletion();
         }
      }
   }

   protected static final class ForEachMappingTask<V> extends Long2ObjectConcurrentHashMap.BulkTask<V, Void> {
      public final Long2ObjectConcurrentHashMap.LongObjConsumer<? super V> action;

      public ForEachMappingTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Long2ObjectConcurrentHashMap.LongObjConsumer<? super V> action
      ) {
         super(p, b, i, f, t);
         this.action = action;
      }

      @Override
      public final void compute() {
         Long2ObjectConcurrentHashMap.LongObjConsumer<? super V> action = this.action;
         if (this.action != null) {
            int i = this.baseIndex;

            while (this.batch > 0) {
               int f = this.baseLimit;
               int h;
               if ((h = this.baseLimit + i >>> 1) <= i) {
                  break;
               }

               this.addToPendingCount(1);
               new Long2ObjectConcurrentHashMap.ForEachMappingTask<>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, action).fork();
            }

            while ((p = this.advance()) != null) {
               action.accept(p.key, p.val);
            }

            this.propagateCompletion();
         }
      }
   }

   protected static final class ForEachTransformedEntryTask<V, U> extends Long2ObjectConcurrentHashMap.BulkTask<V, Void> {
      public final Function<Long2ObjectConcurrentHashMap.Entry<V>, ? extends U> transformer;
      public final Consumer<? super U> action;

      public ForEachTransformedEntryTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Function<Long2ObjectConcurrentHashMap.Entry<V>, ? extends U> transformer,
         Consumer<? super U> action
      ) {
         super(p, b, i, f, t);
         this.transformer = transformer;
         this.action = action;
      }

      @Override
      public final void compute() {
         Function<Long2ObjectConcurrentHashMap.Entry<V>, ? extends U> transformer = this.transformer;
         if (this.transformer != null) {
            Consumer<? super U> action = this.action;
            if (this.action != null) {
               int i = this.baseIndex;

               while (this.batch > 0) {
                  int f = this.baseLimit;
                  int h;
                  if ((h = this.baseLimit + i >>> 1) <= i) {
                     break;
                  }

                  this.addToPendingCount(1);
                  new Long2ObjectConcurrentHashMap.ForEachTransformedEntryTask<>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, transformer, action)
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  U u;
                  if ((u = (U)transformer.apply(p)) != null) {
                     action.accept(u);
                  }
               }

               this.propagateCompletion();
            }
         }
      }
   }

   protected static final class ForEachTransformedKeyTask<V, U> extends Long2ObjectConcurrentHashMap.BulkTask<V, Void> {
      public final Long2ObjectConcurrentHashMap.LongFunction<? extends U> transformer;
      public final Consumer<? super U> action;

      public ForEachTransformedKeyTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Long2ObjectConcurrentHashMap.LongFunction<? extends U> transformer,
         Consumer<? super U> action
      ) {
         super(p, b, i, f, t);
         this.transformer = transformer;
         this.action = action;
      }

      @Override
      public final void compute() {
         Long2ObjectConcurrentHashMap.LongFunction<? extends U> transformer = this.transformer;
         if (this.transformer != null) {
            Consumer<? super U> action = this.action;
            if (this.action != null) {
               int i = this.baseIndex;

               while (this.batch > 0) {
                  int f = this.baseLimit;
                  int h;
                  if ((h = this.baseLimit + i >>> 1) <= i) {
                     break;
                  }

                  this.addToPendingCount(1);
                  new Long2ObjectConcurrentHashMap.ForEachTransformedKeyTask<>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, transformer, action)
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  U u;
                  if ((u = (U)transformer.apply(p.key)) != null) {
                     action.accept(u);
                  }
               }

               this.propagateCompletion();
            }
         }
      }
   }

   protected static final class ForEachTransformedMappingTask<V, U> extends Long2ObjectConcurrentHashMap.BulkTask<V, Void> {
      public final Long2ObjectConcurrentHashMap.LongObjFunction<? super V, ? extends U> transformer;
      public final Consumer<? super U> action;

      public ForEachTransformedMappingTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Long2ObjectConcurrentHashMap.LongObjFunction<? super V, ? extends U> transformer,
         Consumer<? super U> action
      ) {
         super(p, b, i, f, t);
         this.transformer = transformer;
         this.action = action;
      }

      @Override
      public final void compute() {
         Long2ObjectConcurrentHashMap.LongObjFunction<? super V, ? extends U> transformer = this.transformer;
         if (this.transformer != null) {
            Consumer<? super U> action = this.action;
            if (this.action != null) {
               int i = this.baseIndex;

               while (this.batch > 0) {
                  int f = this.baseLimit;
                  int h;
                  if ((h = this.baseLimit + i >>> 1) <= i) {
                     break;
                  }

                  this.addToPendingCount(1);
                  new Long2ObjectConcurrentHashMap.ForEachTransformedMappingTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, transformer, action
                     )
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  U u;
                  if ((u = (U)transformer.apply(p.key, p.val)) != null) {
                     action.accept(u);
                  }
               }

               this.propagateCompletion();
            }
         }
      }
   }

   protected static final class ForEachTransformedValueTask<V, U> extends Long2ObjectConcurrentHashMap.BulkTask<V, Void> {
      public final Function<? super V, ? extends U> transformer;
      public final Consumer<? super U> action;

      public ForEachTransformedValueTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Function<? super V, ? extends U> transformer,
         Consumer<? super U> action
      ) {
         super(p, b, i, f, t);
         this.transformer = transformer;
         this.action = action;
      }

      @Override
      public final void compute() {
         Function<? super V, ? extends U> transformer = this.transformer;
         if (this.transformer != null) {
            Consumer<? super U> action = this.action;
            if (this.action != null) {
               int i = this.baseIndex;

               while (this.batch > 0) {
                  int f = this.baseLimit;
                  int h;
                  if ((h = this.baseLimit + i >>> 1) <= i) {
                     break;
                  }

                  this.addToPendingCount(1);
                  new Long2ObjectConcurrentHashMap.ForEachTransformedValueTask<>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, transformer, action)
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  U u;
                  if ((u = (U)transformer.apply(p.val)) != null) {
                     action.accept(u);
                  }
               }

               this.propagateCompletion();
            }
         }
      }
   }

   protected static final class ForEachValueTask<V> extends Long2ObjectConcurrentHashMap.BulkTask<V, Void> {
      public final Consumer<? super V> action;

      public ForEachValueTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p, int b, int i, int f, Long2ObjectConcurrentHashMap.Node<V>[] t, Consumer<? super V> action
      ) {
         super(p, b, i, f, t);
         this.action = action;
      }

      @Override
      public final void compute() {
         Consumer<? super V> action = this.action;
         if (this.action != null) {
            int i = this.baseIndex;

            while (this.batch > 0) {
               int f = this.baseLimit;
               int h;
               if ((h = this.baseLimit + i >>> 1) <= i) {
                  break;
               }

               this.addToPendingCount(1);
               new Long2ObjectConcurrentHashMap.ForEachValueTask<>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, action).fork();
            }

            while ((p = this.advance()) != null) {
               action.accept(p.val);
            }

            this.propagateCompletion();
         }
      }
   }

   protected static final class ForwardingNode<V> extends Long2ObjectConcurrentHashMap.Node<V> {
      public final Long2ObjectConcurrentHashMap.Node<V>[] nextTable;

      public ForwardingNode(long empty, Long2ObjectConcurrentHashMap.Node<V>[] tab) {
         super(empty, -1, empty, null, null);
         this.nextTable = tab;
      }

      @Override
      protected Long2ObjectConcurrentHashMap.Node<V> find(int h, long k) {
         Long2ObjectConcurrentHashMap.Node<V>[] tab = this.nextTable;

         Long2ObjectConcurrentHashMap.Node<V> e;
         int n;
         label41:
         while (k != this.EMPTY && tab != null && (n = tab.length) != 0 && (e = Long2ObjectConcurrentHashMap.tabAt(tab, n - 1 & h)) != null) {
            do {
               int eh = e.hash;
               if (e.hash == h) {
                  long ek = e.key;
                  if (e.key == k || ek != this.EMPTY && k == ek) {
                     return e;
                  }
               }

               if (eh < 0) {
                  if (!(e instanceof Long2ObjectConcurrentHashMap.ForwardingNode)) {
                     return e.find(h, k);
                  }

                  tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)e).nextTable;
                  continue label41;
               }
            } while ((e = e.next) != null);

            return null;
         }

         return null;
      }
   }

   protected abstract static class IntReturningBulkTask<V> extends Long2ObjectConcurrentHashMap.BulkTask<V, Integer> {
      public int result;

      public IntReturningBulkTask(Long2ObjectConcurrentHashMap.BulkTask<V, ?> par, int b, int i, int f, Long2ObjectConcurrentHashMap.Node<V>[] t) {
         super(par, b, i, f, t);
      }

      protected int invoke0() {
         this.quietlyInvoke();
         Throwable exc = this.getException();
         if (exc != null) {
            throw SneakyThrow.sneakyThrow(exc);
         } else {
            return this.result;
         }
      }
   }

   protected static final class KeyIterator<V> implements LongIterator {
      public Long2ObjectConcurrentHashMap.Node<V>[] tab;
      public Long2ObjectConcurrentHashMap.Node<V> next;
      public Long2ObjectConcurrentHashMap.TableStack<V> stack;
      public Long2ObjectConcurrentHashMap.TableStack<V> spare;
      public int index;
      public int baseIndex;
      public int baseLimit;
      public final int baseSize;
      public final Long2ObjectConcurrentHashMap<V> map;
      public Long2ObjectConcurrentHashMap.Node<V> lastReturned;

      public KeyIterator(Long2ObjectConcurrentHashMap.Node<V>[] tab, int size, int index, int limit, Long2ObjectConcurrentHashMap<V> map) {
         this.tab = tab;
         this.baseSize = size;
         this.baseIndex = this.index = index;
         this.baseLimit = limit;
         this.next = null;
         this.map = map;
         this.advance();
      }

      protected final Long2ObjectConcurrentHashMap.Node<V> advance() {
         Long2ObjectConcurrentHashMap.Node<V> e = this.next;
         if (this.next != null) {
            e = e.next;
         }

         while (true) {
            if (e != null) {
               return this.next = e;
            }

            if (this.baseIndex >= this.baseLimit) {
               break;
            }

            Long2ObjectConcurrentHashMap.Node<V>[] t = this.tab;
            if (this.tab == null) {
               break;
            }

            int n;
            int var10000 = n = t.length;
            int i = this.index;
            if (var10000 <= this.index || i < 0) {
               break;
            }

            if ((e = Long2ObjectConcurrentHashMap.tabAt(t, i)) != null && e.hash < 0) {
               if (e instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                  this.tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)e).nextTable;
                  e = null;
                  this.pushState(t, i, n);
                  continue;
               }

               if (e instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                  e = ((Long2ObjectConcurrentHashMap.TreeBin)e).first;
               } else {
                  e = null;
               }
            }

            if (this.stack != null) {
               this.recoverState(n);
            } else if ((this.index = i + this.baseSize) >= n) {
               this.index = ++this.baseIndex;
            }
         }

         return this.next = null;
      }

      protected void pushState(Long2ObjectConcurrentHashMap.Node<V>[] t, int i, int n) {
         Long2ObjectConcurrentHashMap.TableStack<V> s = this.spare;
         if (s != null) {
            this.spare = s.next;
         } else {
            s = new Long2ObjectConcurrentHashMap.TableStack<>();
         }

         s.tab = t;
         s.length = n;
         s.index = i;
         s.next = this.stack;
         this.stack = s;
      }

      protected void recoverState(int n) {
         while (true) {
            Long2ObjectConcurrentHashMap.TableStack<V> s = this.stack;
            if (this.stack != null) {
               int len = s.length;
               if ((this.index = this.index + s.length) >= n) {
                  n = len;
                  this.index = s.index;
                  this.tab = s.tab;
                  s.tab = null;
                  Long2ObjectConcurrentHashMap.TableStack<V> next = s.next;
                  s.next = this.spare;
                  this.stack = next;
                  this.spare = s;
                  continue;
               }
            }

            if (s == null && (this.index = this.index + this.baseSize) >= n) {
               this.index = ++this.baseIndex;
            }

            return;
         }
      }

      public final boolean hasNext() {
         return this.next != null;
      }

      public final boolean hasMoreElements() {
         return this.next != null;
      }

      public final void remove() {
         Long2ObjectConcurrentHashMap.Node<V> p = this.lastReturned;
         if (this.lastReturned == null) {
            throw new IllegalStateException();
         } else {
            this.lastReturned = null;
            this.map.replaceNode(p.key, null, null);
         }
      }

      public final long nextLong() {
         Long2ObjectConcurrentHashMap.Node<V> p = this.next;
         if (this.next == null) {
            throw new NoSuchElementException();
         } else {
            long k = p.key;
            this.lastReturned = p;
            this.advance();
            return k;
         }
      }
   }

   public static class KeySetView<V> implements LongSet {
      public static final long serialVersionUID = 7249069246763182397L;
      public final Long2ObjectConcurrentHashMap<V> map;
      public final V value;

      public KeySetView(Long2ObjectConcurrentHashMap<V> map, V value) {
         this.map = map;
         this.value = value;
      }

      public V getMappedValue() {
         return this.value;
      }

      public boolean contains(long o) {
         return this.map.containsKey(o);
      }

      public boolean remove(long o) {
         return this.map.remove(o) != null;
      }

      public LongIterator iterator() {
         Long2ObjectConcurrentHashMap<V> m = this.map;
         Long2ObjectConcurrentHashMap.Node<V>[] t = m.table;
         int f = m.table == null ? 0 : t.length;
         return new Long2ObjectConcurrentHashMap.KeyIterator<>(t, f, 0, f, m);
      }

      public boolean add(long e) {
         V v = this.value;
         if (this.value == null) {
            throw new UnsupportedOperationException();
         } else {
            return this.map.putVal(e, v, true) == null;
         }
      }

      public boolean addAll(LongCollection c) {
         boolean added = false;
         V v = this.value;
         if (this.value == null) {
            throw new UnsupportedOperationException();
         } else {
            LongIterator iter = c.iterator();

            while (iter.hasNext()) {
               long e = iter.nextLong();
               if (this.map.putVal(e, v, true) == null) {
                  added = true;
               }
            }

            return added;
         }
      }

      @Override
      public int hashCode() {
         int h = 0;
         LongIterator iter = this.iterator();

         while (iter.hasNext()) {
            h += Long.hashCode(iter.nextLong());
         }

         return h;
      }

      @Override
      public boolean equals(Object o) {
         LongSet c;
         return o instanceof LongSet && ((c = (LongSet)o) == this || this.containsAll(c) && c.containsAll(this));
      }

      public long getNoEntryValue() {
         return this.map.EMPTY;
      }

      public int size() {
         return this.map.size();
      }

      public boolean isEmpty() {
         return this.map.isEmpty();
      }

      public Object[] toArray() {
         Object[] out = new Long[this.size()];
         LongIterator iter = this.iterator();

         int i;
         for (i = 0; i < out.length && iter.hasNext(); i++) {
            out[i] = iter.nextLong();
         }

         if (out.length > i + 1) {
            out[i] = this.map.EMPTY;
         }

         return out;
      }

      public Object[] toArray(Object[] dest) {
         LongIterator iter = this.iterator();

         int i;
         for (i = 0; i < dest.length && iter.hasNext() && i <= dest.length; i++) {
            dest[i] = iter.next();
         }

         if (dest.length > i + 1) {
            dest[i] = this.map.EMPTY;
         }

         return dest;
      }

      public long[] toLongArray() {
         long[] out = new long[this.size()];
         LongIterator iter = this.iterator();

         int i;
         for (i = 0; i < out.length && iter.hasNext(); i++) {
            out[i] = iter.next();
         }

         if (out.length > i + 1) {
            out[i] = this.map.EMPTY;
         }

         return out;
      }

      public long[] toArray(long[] dest) {
         LongIterator iter = this.iterator();

         int i;
         for (i = 0; i < dest.length && iter.hasNext() && i <= dest.length; i++) {
            dest[i] = iter.next();
         }

         if (dest.length > i + 1) {
            dest[i] = this.map.EMPTY;
         }

         return dest;
      }

      public long[] toLongArray(long[] dest) {
         return this.toArray(dest);
      }

      public boolean containsAll(Collection<?> collection) {
         for (Object element : collection) {
            if (!(element instanceof Long)) {
               return false;
            }

            long c = (Long)element;
            if (!this.contains(c)) {
               return false;
            }
         }

         return true;
      }

      public boolean containsAll(LongCollection collection) {
         LongIterator iter = collection.iterator();

         while (iter.hasNext()) {
            long element = iter.next();
            if (!this.contains(element)) {
               return false;
            }
         }

         return true;
      }

      public boolean containsAll(long[] array) {
         int i = array.length;

         while (i-- > 0) {
            if (!this.contains(array[i])) {
               return false;
            }
         }

         return true;
      }

      public boolean addAll(Collection<? extends Long> collection) {
         boolean changed = false;

         for (Long element : collection) {
            long e = element;
            if (this.add(e)) {
               changed = true;
            }
         }

         return changed;
      }

      public boolean addAll(long[] array) {
         boolean changed = false;
         int i = array.length;

         while (i-- > 0) {
            if (this.add(array[i])) {
               changed = true;
            }
         }

         return changed;
      }

      public boolean retainAll(Collection<?> collection) {
         boolean modified = false;
         LongIterator iter = this.iterator();

         while (iter.hasNext()) {
            if (!collection.contains(iter.next())) {
               iter.remove();
               modified = true;
            }
         }

         return modified;
      }

      public boolean retainAll(LongCollection collection) {
         if (this == collection) {
            return false;
         } else {
            boolean modified = false;
            LongIterator iter = this.iterator();

            while (iter.hasNext()) {
               if (!collection.contains(iter.next())) {
                  iter.remove();
                  modified = true;
               }
            }

            return modified;
         }
      }

      public boolean retainAll(long[] array) {
         boolean modified = false;
         LongIterator iter = this.iterator();

         while (iter.hasNext()) {
            if (Arrays.binarySearch(array, iter.next().longValue()) < 0) {
               iter.remove();
               modified = true;
            }
         }

         return modified;
      }

      public boolean removeAll(Collection<?> collection) {
         boolean changed = false;

         for (Object element : collection) {
            if (element instanceof Long) {
               long c = (Long)element;
               if (this.remove(c)) {
                  changed = true;
               }
            }
         }

         return changed;
      }

      public boolean removeAll(LongCollection collection) {
         boolean changed = false;
         LongIterator iter = collection.iterator();

         while (iter.hasNext()) {
            long element = iter.next();
            if (this.remove(element)) {
               changed = true;
            }
         }

         return changed;
      }

      public boolean removeAll(long[] array) {
         boolean changed = false;
         int i = array.length;

         while (i-- > 0) {
            if (this.remove(array[i])) {
               changed = true;
            }
         }

         return changed;
      }

      public void clear() {
         this.map.clear();
      }

      public LongSpliterator spliterator() {
         Long2ObjectConcurrentHashMap<V> m = this.map;
         long n = m.sumCount();
         Long2ObjectConcurrentHashMap.Node<V>[] t = m.table;
         int f = m.table == null ? 0 : t.length;
         return new Long2ObjectConcurrentHashMap.KeySpliterator<>(t, f, 0, f, n < 0L ? 0L : n);
      }
   }

   protected static final class KeySpliterator<V> extends Long2ObjectConcurrentHashMap.Traverser<V> implements LongSpliterator {
      public long est;

      public KeySpliterator(Long2ObjectConcurrentHashMap.Node<V>[] tab, int size, int index, int limit, long est) {
         super(tab, size, index, limit);
         this.est = est;
      }

      public LongSpliterator trySplit() {
         int i = this.baseIndex;
         int f = this.baseLimit;
         int h;
         return (h = this.baseIndex + this.baseLimit >>> 1) <= i
            ? null
            : new Long2ObjectConcurrentHashMap.KeySpliterator<>(this.tab, this.baseSize, this.baseLimit = h, f, this.est >>>= 1);
      }

      public boolean tryAdvance(Consumer<? super Long> action) {
         return action instanceof LongConsumer ? this.tryAdvance((LongConsumer)action) : this.tryAdvance(value -> action.accept(value));
      }

      public void forEachRemaining(LongConsumer action) {
         if (action == null) {
            throw new NullPointerException();
         } else {
            Long2ObjectConcurrentHashMap.Node<V> p;
            while ((p = this.advance()) != null) {
               action.accept(p.key);
            }
         }
      }

      public boolean tryAdvance(LongConsumer action) {
         if (action == null) {
            throw new NullPointerException();
         } else {
            Long2ObjectConcurrentHashMap.Node<V> p;
            if ((p = this.advance()) == null) {
               return false;
            } else {
               action.accept(p.key);
               return true;
            }
         }
      }

      public long estimateSize() {
         return this.est;
      }

      public int characteristics() {
         return 4353;
      }
   }

   @FunctionalInterface
   public interface LongBiObjByteConsumer<V, X> {
      void accept(long var1, V var3, byte var4, X var5);
   }

   @FunctionalInterface
   public interface LongBiObjConsumer<V, X> {
      void accept(long var1, V var3, X var4);
   }

   @FunctionalInterface
   public interface LongBiObjDoubleConsumer<V, X> {
      void accept(long var1, V var3, double var4, X var6);
   }

   @FunctionalInterface
   public interface LongBiObjFloatConsumer<V, X> {
      void accept(long var1, V var3, float var4, X var5);
   }

   @FunctionalInterface
   public interface LongBiObjFunction<V, X, J> {
      J apply(long var1, V var3, X var4);
   }

   @FunctionalInterface
   public interface LongBiObjIntConsumer<V, X> {
      void accept(long var1, V var3, int var4, X var5);
   }

   @FunctionalInterface
   public interface LongBiObjLongConsumer<V, X> {
      void accept(long var1, V var3, long var4, X var6);
   }

   @FunctionalInterface
   public interface LongBiObjShortConsumer<V, X> {
      void accept(long var1, V var3, short var4, X var5);
   }

   @FunctionalInterface
   public interface LongFunction<R> {
      R apply(long var1);
   }

   @FunctionalInterface
   public interface LongObjByteConsumer<V> {
      void accept(long var1, V var3, byte var4);
   }

   @FunctionalInterface
   public interface LongObjByteFunction<V, J> {
      J apply(long var1, V var3, byte var4);
   }

   @FunctionalInterface
   public interface LongObjConsumer<V> {
      void accept(long var1, V var3);
   }

   @FunctionalInterface
   public interface LongObjDoubleConsumer<V> {
      void accept(long var1, V var3, double var4);
   }

   @FunctionalInterface
   public interface LongObjDoubleFunction<V, J> {
      J apply(long var1, V var3, double var4);
   }

   @FunctionalInterface
   public interface LongObjFloatConsumer<V> {
      void accept(long var1, V var3, float var4);
   }

   @FunctionalInterface
   public interface LongObjFloatFunction<V, J> {
      J apply(long var1, V var3, float var4);
   }

   @FunctionalInterface
   public interface LongObjFunction<V, J> {
      J apply(long var1, V var3);
   }

   @FunctionalInterface
   public interface LongObjIntConsumer<V> {
      void accept(long var1, V var3, int var4);
   }

   @FunctionalInterface
   public interface LongObjIntFunction<V, J> {
      J apply(long var1, V var3, int var4);
   }

   @FunctionalInterface
   public interface LongObjLongConsumer<V> {
      void accept(long var1, V var3, long var4);
   }

   @FunctionalInterface
   public interface LongObjLongFunction<V, J> {
      J apply(long var1, V var3, long var4);
   }

   @FunctionalInterface
   public interface LongObjShortConsumer<V> {
      void accept(long var1, V var3, short var4);
   }

   @FunctionalInterface
   public interface LongObjShortFunction<V, J> {
      J apply(long var1, V var3, short var4);
   }

   @FunctionalInterface
   public interface LongReduceTaskOperator {
      long reduce(long var1, long var3, long var5);
   }

   protected abstract static class LongReturningBulkTask<V> extends Long2ObjectConcurrentHashMap.BulkTask<V, Long> {
      public long result;

      public LongReturningBulkTask(Long2ObjectConcurrentHashMap.BulkTask<V, ?> par, int b, int i, int f, Long2ObjectConcurrentHashMap.Node<V>[] t) {
         super(par, b, i, f, t);
      }

      protected long invoke0() {
         this.quietlyInvoke();
         Throwable exc = this.getException();
         if (exc != null) {
            throw SneakyThrow.sneakyThrow(exc);
         } else {
            return this.result;
         }
      }
   }

   protected abstract static class LongReturningBulkTask2<V> extends Long2ObjectConcurrentHashMap.BulkTask<V, Long> {
      public long result;

      public LongReturningBulkTask2(Long2ObjectConcurrentHashMap.BulkTask<V, ?> par, int b, int i, int f, Long2ObjectConcurrentHashMap.Node<V>[] t) {
         super(par, b, i, f, t);
      }

      protected long invoke0() {
         this.quietlyInvoke();
         Throwable exc = this.getException();
         if (exc != null) {
            throw SneakyThrow.sneakyThrow(exc);
         } else {
            return this.result;
         }
      }
   }

   @FunctionalInterface
   public interface LongToDoubleFunction {
      double applyAsDouble(long var1);
   }

   @FunctionalInterface
   public interface LongToIntFunction {
      int applyAsInt(long var1);
   }

   @FunctionalInterface
   public interface LongToLongFunction {
      long applyAsLong(long var1);
   }

   @FunctionalInterface
   public interface LongTriObjConsumer<V, X, Y> {
      void accept(long var1, V var3, X var4, Y var5);
   }

   protected static final class MapEntry<V> implements Long2ObjectConcurrentHashMap.Entry<V> {
      public final boolean empty;
      public final long key;
      public V val;
      public final Long2ObjectConcurrentHashMap<V> map;

      public MapEntry(boolean empty, long key, V val, Long2ObjectConcurrentHashMap<V> map) {
         this.empty = empty;
         this.key = key;
         this.val = val;
         this.map = map;
      }

      @Override
      public boolean isEmpty() {
         return this.empty;
      }

      @Override
      public Long getKey() {
         return this.key;
      }

      @Override
      public long getLongKey() {
         return this.key;
      }

      @Override
      public V getValue() {
         return this.val;
      }

      @Override
      public String toString() {
         return this.empty ? "EMPTY=" + this.val : this.key + "=" + this.val;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o instanceof Long2ObjectConcurrentHashMap.Entry) {
            if (this.empty != ((Long2ObjectConcurrentHashMap.Entry)o).isEmpty()) {
               return false;
            } else {
               return !this.empty && this.key != ((Long2ObjectConcurrentHashMap.Entry)o).getLongKey()
                  ? false
                  : this.val.equals(((Long2ObjectConcurrentHashMap.Entry)o).getValue());
            }
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         int result = this.empty ? 1 : 0;
         result = 31 * result + Long.hashCode(this.key);
         return 31 * result + this.val.hashCode();
      }

      @Override
      public V setValue(V value) {
         if (value == null) {
            throw new NullPointerException();
         } else {
            V v = this.val;
            this.val = value;
            this.map.put(this.key, value);
            return v;
         }
      }
   }

   protected static final class MapReduceEntriesTask<V, U> extends Long2ObjectConcurrentHashMap.BulkTask<V, U> {
      public final Function<Long2ObjectConcurrentHashMap.Entry<V>, ? extends U> transformer;
      public final BiFunction<? super U, ? super U, ? extends U> reducer;
      public U result;
      public Long2ObjectConcurrentHashMap.MapReduceEntriesTask<V, U> rights;
      public Long2ObjectConcurrentHashMap.MapReduceEntriesTask<V, U> nextRight;

      public MapReduceEntriesTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Long2ObjectConcurrentHashMap.MapReduceEntriesTask<V, U> nextRight,
         Function<Long2ObjectConcurrentHashMap.Entry<V>, ? extends U> transformer,
         BiFunction<? super U, ? super U, ? extends U> reducer
      ) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.reducer = reducer;
      }

      @Override
      public final U getRawResult() {
         return this.result;
      }

      @Override
      public final void compute() {
         Function<Long2ObjectConcurrentHashMap.Entry<V>, ? extends U> transformer = this.transformer;
         if (this.transformer != null) {
            BiFunction<? super U, ? super U, ? extends U> reducer = this.reducer;
            if (this.reducer != null) {
               int i = this.baseIndex;

               while (this.batch > 0) {
                  int f = this.baseLimit;
                  int h;
                  if ((h = this.baseLimit + i >>> 1) <= i) {
                     break;
                  }

                  this.addToPendingCount(1);
                  (this.rights = new Long2ObjectConcurrentHashMap.MapReduceEntriesTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, reducer
                     ))
                     .fork();
               }

               U r = null;

               Long2ObjectConcurrentHashMap.Node<V> p;
               while ((p = this.advance()) != null) {
                  U u;
                  if ((u = (U)transformer.apply(p)) != null) {
                     r = r == null ? u : reducer.apply(r, u);
                  }
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Long2ObjectConcurrentHashMap.MapReduceEntriesTask<V, U> t = (Long2ObjectConcurrentHashMap.MapReduceEntriesTask<V, U>)c;

                  for (Long2ObjectConcurrentHashMap.MapReduceEntriesTask<V, U> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     U sr = s.result;
                     if (s.result != null) {
                        U tr = t.result;
                        t.result = t.result == null ? sr : reducer.apply(tr, sr);
                     }
                  }
               }
            }
         }
      }
   }

   protected static final class MapReduceEntriesToDoubleTask<V> extends Long2ObjectConcurrentHashMap.DoubleReturningBulkTask<V> {
      public final ToDoubleFunction<Long2ObjectConcurrentHashMap.Entry<V>> transformer;
      public final DoubleBinaryOperator reducer;
      public final double basis;
      public Long2ObjectConcurrentHashMap.MapReduceEntriesToDoubleTask<V> rights;
      public Long2ObjectConcurrentHashMap.MapReduceEntriesToDoubleTask<V> nextRight;

      public MapReduceEntriesToDoubleTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Long2ObjectConcurrentHashMap.MapReduceEntriesToDoubleTask<V> nextRight,
         ToDoubleFunction<Long2ObjectConcurrentHashMap.Entry<V>> transformer,
         double basis,
         DoubleBinaryOperator reducer
      ) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.basis = basis;
         this.reducer = reducer;
      }

      public final Double getRawResult() {
         throw new UnsupportedOperationException();
      }

      @Override
      public final void compute() {
         ToDoubleFunction<Long2ObjectConcurrentHashMap.Entry<V>> transformer = this.transformer;
         if (this.transformer != null) {
            DoubleBinaryOperator reducer = this.reducer;
            if (this.reducer != null) {
               double r = this.basis;
               int i = this.baseIndex;

               while (this.batch > 0) {
                  int f = this.baseLimit;
                  int h;
                  if ((h = this.baseLimit + i >>> 1) <= i) {
                     break;
                  }

                  this.addToPendingCount(1);
                  (this.rights = new Long2ObjectConcurrentHashMap.MapReduceEntriesToDoubleTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer
                     ))
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  r = reducer.applyAsDouble(r, transformer.applyAsDouble(p));
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Long2ObjectConcurrentHashMap.MapReduceEntriesToDoubleTask<V> t = (Long2ObjectConcurrentHashMap.MapReduceEntriesToDoubleTask<V>)c;

                  for (Long2ObjectConcurrentHashMap.MapReduceEntriesToDoubleTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     t.result = reducer.applyAsDouble(t.result, s.result);
                  }
               }
            }
         }
      }
   }

   protected static final class MapReduceEntriesToIntTask<V> extends Long2ObjectConcurrentHashMap.IntReturningBulkTask<V> {
      public final ToIntFunction<Long2ObjectConcurrentHashMap.Entry<V>> transformer;
      public final IntBinaryOperator reducer;
      public final int basis;
      public Long2ObjectConcurrentHashMap.MapReduceEntriesToIntTask<V> rights;
      public Long2ObjectConcurrentHashMap.MapReduceEntriesToIntTask<V> nextRight;

      public MapReduceEntriesToIntTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Long2ObjectConcurrentHashMap.MapReduceEntriesToIntTask<V> nextRight,
         ToIntFunction<Long2ObjectConcurrentHashMap.Entry<V>> transformer,
         int basis,
         IntBinaryOperator reducer
      ) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.basis = basis;
         this.reducer = reducer;
      }

      public final Integer getRawResult() {
         throw new UnsupportedOperationException();
      }

      @Override
      public final void compute() {
         ToIntFunction<Long2ObjectConcurrentHashMap.Entry<V>> transformer = this.transformer;
         if (this.transformer != null) {
            IntBinaryOperator reducer = this.reducer;
            if (this.reducer != null) {
               int r = this.basis;
               int i = this.baseIndex;

               while (this.batch > 0) {
                  int f = this.baseLimit;
                  int h;
                  if ((h = this.baseLimit + i >>> 1) <= i) {
                     break;
                  }

                  this.addToPendingCount(1);
                  (this.rights = new Long2ObjectConcurrentHashMap.MapReduceEntriesToIntTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer
                     ))
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  r = reducer.applyAsInt(r, transformer.applyAsInt(p));
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Long2ObjectConcurrentHashMap.MapReduceEntriesToIntTask<V> t = (Long2ObjectConcurrentHashMap.MapReduceEntriesToIntTask<V>)c;

                  for (Long2ObjectConcurrentHashMap.MapReduceEntriesToIntTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     t.result = reducer.applyAsInt(t.result, s.result);
                  }
               }
            }
         }
      }
   }

   protected static final class MapReduceEntriesToLongTask<V> extends Long2ObjectConcurrentHashMap.LongReturningBulkTask<V> {
      public final Long2ObjectConcurrentHashMap.ToLongFunction<Long2ObjectConcurrentHashMap.Entry<V>> transformer;
      public final LongBinaryOperator reducer;
      public final long basis;
      public Long2ObjectConcurrentHashMap.MapReduceEntriesToLongTask<V> rights;
      public Long2ObjectConcurrentHashMap.MapReduceEntriesToLongTask<V> nextRight;

      public MapReduceEntriesToLongTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Long2ObjectConcurrentHashMap.MapReduceEntriesToLongTask<V> nextRight,
         Long2ObjectConcurrentHashMap.ToLongFunction<Long2ObjectConcurrentHashMap.Entry<V>> transformer,
         long basis,
         LongBinaryOperator reducer
      ) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.basis = basis;
         this.reducer = reducer;
      }

      public final Long getRawResult() {
         throw new UnsupportedOperationException();
      }

      @Override
      public final void compute() {
         Long2ObjectConcurrentHashMap.ToLongFunction<Long2ObjectConcurrentHashMap.Entry<V>> transformer = this.transformer;
         if (this.transformer != null) {
            LongBinaryOperator reducer = this.reducer;
            if (this.reducer != null) {
               long r = this.basis;
               int i = this.baseIndex;

               while (this.batch > 0) {
                  int f = this.baseLimit;
                  int h;
                  if ((h = this.baseLimit + i >>> 1) <= i) {
                     break;
                  }

                  this.addToPendingCount(1);
                  (this.rights = new Long2ObjectConcurrentHashMap.MapReduceEntriesToLongTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer
                     ))
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  r = reducer.applyAsLong(r, transformer.applyAsLong(p));
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Long2ObjectConcurrentHashMap.MapReduceEntriesToLongTask<V> t = (Long2ObjectConcurrentHashMap.MapReduceEntriesToLongTask<V>)c;

                  for (Long2ObjectConcurrentHashMap.MapReduceEntriesToLongTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     t.result = reducer.applyAsLong(t.result, s.result);
                  }
               }
            }
         }
      }
   }

   protected static final class MapReduceKeysTask<V, U> extends Long2ObjectConcurrentHashMap.BulkTask<V, U> {
      public final Long2ObjectConcurrentHashMap.LongFunction<? extends U> transformer;
      public final BiFunction<? super U, ? super U, ? extends U> reducer;
      public U result;
      public Long2ObjectConcurrentHashMap.MapReduceKeysTask<V, U> rights;
      public Long2ObjectConcurrentHashMap.MapReduceKeysTask<V, U> nextRight;

      public MapReduceKeysTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Long2ObjectConcurrentHashMap.MapReduceKeysTask<V, U> nextRight,
         Long2ObjectConcurrentHashMap.LongFunction<? extends U> transformer,
         BiFunction<? super U, ? super U, ? extends U> reducer
      ) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.reducer = reducer;
      }

      @Override
      public final U getRawResult() {
         return this.result;
      }

      @Override
      public final void compute() {
         Long2ObjectConcurrentHashMap.LongFunction<? extends U> transformer = this.transformer;
         if (this.transformer != null) {
            BiFunction<? super U, ? super U, ? extends U> reducer = this.reducer;
            if (this.reducer != null) {
               int i = this.baseIndex;

               while (this.batch > 0) {
                  int f = this.baseLimit;
                  int h;
                  if ((h = this.baseLimit + i >>> 1) <= i) {
                     break;
                  }

                  this.addToPendingCount(1);
                  (this.rights = new Long2ObjectConcurrentHashMap.MapReduceKeysTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, reducer
                     ))
                     .fork();
               }

               U r = null;

               Long2ObjectConcurrentHashMap.Node<V> p;
               while ((p = this.advance()) != null) {
                  U u;
                  if ((u = (U)transformer.apply(p.key)) != null) {
                     r = r == null ? u : reducer.apply(r, u);
                  }
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Long2ObjectConcurrentHashMap.MapReduceKeysTask<V, U> t = (Long2ObjectConcurrentHashMap.MapReduceKeysTask<V, U>)c;

                  for (Long2ObjectConcurrentHashMap.MapReduceKeysTask<V, U> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     U sr = s.result;
                     if (s.result != null) {
                        U tr = t.result;
                        t.result = t.result == null ? sr : reducer.apply(tr, sr);
                     }
                  }
               }
            }
         }
      }
   }

   protected static final class MapReduceKeysToDoubleTask<V> extends Long2ObjectConcurrentHashMap.DoubleReturningBulkTask<V> {
      public final Long2ObjectConcurrentHashMap.LongToDoubleFunction transformer;
      public final DoubleBinaryOperator reducer;
      public final double basis;
      public Long2ObjectConcurrentHashMap.MapReduceKeysToDoubleTask<V> rights;
      public Long2ObjectConcurrentHashMap.MapReduceKeysToDoubleTask<V> nextRight;

      public MapReduceKeysToDoubleTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Long2ObjectConcurrentHashMap.MapReduceKeysToDoubleTask<V> nextRight,
         Long2ObjectConcurrentHashMap.LongToDoubleFunction transformer,
         double basis,
         DoubleBinaryOperator reducer
      ) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.basis = basis;
         this.reducer = reducer;
      }

      public final Double getRawResult() {
         throw new UnsupportedOperationException();
      }

      @Override
      public final void compute() {
         Long2ObjectConcurrentHashMap.LongToDoubleFunction transformer = this.transformer;
         if (this.transformer != null) {
            DoubleBinaryOperator reducer = this.reducer;
            if (this.reducer != null) {
               double r = this.basis;
               int i = this.baseIndex;

               while (this.batch > 0) {
                  int f = this.baseLimit;
                  int h;
                  if ((h = this.baseLimit + i >>> 1) <= i) {
                     break;
                  }

                  this.addToPendingCount(1);
                  (this.rights = new Long2ObjectConcurrentHashMap.MapReduceKeysToDoubleTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer
                     ))
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  r = reducer.applyAsDouble(r, transformer.applyAsDouble(p.key));
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Long2ObjectConcurrentHashMap.MapReduceKeysToDoubleTask<V> t = (Long2ObjectConcurrentHashMap.MapReduceKeysToDoubleTask<V>)c;

                  for (Long2ObjectConcurrentHashMap.MapReduceKeysToDoubleTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     t.result = reducer.applyAsDouble(t.result, s.result);
                  }
               }
            }
         }
      }
   }

   protected static final class MapReduceKeysToIntTask<V> extends Long2ObjectConcurrentHashMap.IntReturningBulkTask<V> {
      public final Long2ObjectConcurrentHashMap.LongToIntFunction transformer;
      public final IntBinaryOperator reducer;
      public final int basis;
      public Long2ObjectConcurrentHashMap.MapReduceKeysToIntTask<V> rights;
      public Long2ObjectConcurrentHashMap.MapReduceKeysToIntTask<V> nextRight;

      public MapReduceKeysToIntTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Long2ObjectConcurrentHashMap.MapReduceKeysToIntTask<V> nextRight,
         Long2ObjectConcurrentHashMap.LongToIntFunction transformer,
         int basis,
         IntBinaryOperator reducer
      ) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.basis = basis;
         this.reducer = reducer;
      }

      public final Integer getRawResult() {
         throw new UnsupportedOperationException();
      }

      @Override
      public final void compute() {
         Long2ObjectConcurrentHashMap.LongToIntFunction transformer = this.transformer;
         if (this.transformer != null) {
            IntBinaryOperator reducer = this.reducer;
            if (this.reducer != null) {
               int r = this.basis;
               int i = this.baseIndex;

               while (this.batch > 0) {
                  int f = this.baseLimit;
                  int h;
                  if ((h = this.baseLimit + i >>> 1) <= i) {
                     break;
                  }

                  this.addToPendingCount(1);
                  (this.rights = new Long2ObjectConcurrentHashMap.MapReduceKeysToIntTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer
                     ))
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  r = reducer.applyAsInt(r, transformer.applyAsInt(p.key));
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Long2ObjectConcurrentHashMap.MapReduceKeysToIntTask<V> t = (Long2ObjectConcurrentHashMap.MapReduceKeysToIntTask<V>)c;

                  for (Long2ObjectConcurrentHashMap.MapReduceKeysToIntTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     t.result = reducer.applyAsInt(t.result, s.result);
                  }
               }
            }
         }
      }
   }

   protected static final class MapReduceKeysToLongTask<V> extends Long2ObjectConcurrentHashMap.LongReturningBulkTask<V> {
      public final Long2ObjectConcurrentHashMap.LongToLongFunction transformer;
      public final LongBinaryOperator reducer;
      public final long basis;
      public Long2ObjectConcurrentHashMap.MapReduceKeysToLongTask<V> rights;
      public Long2ObjectConcurrentHashMap.MapReduceKeysToLongTask<V> nextRight;

      public MapReduceKeysToLongTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Long2ObjectConcurrentHashMap.MapReduceKeysToLongTask<V> nextRight,
         Long2ObjectConcurrentHashMap.LongToLongFunction transformer,
         long basis,
         LongBinaryOperator reducer
      ) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.basis = basis;
         this.reducer = reducer;
      }

      public final Long getRawResult() {
         throw new UnsupportedOperationException();
      }

      @Override
      public final void compute() {
         Long2ObjectConcurrentHashMap.LongToLongFunction transformer = this.transformer;
         if (this.transformer != null) {
            LongBinaryOperator reducer = this.reducer;
            if (this.reducer != null) {
               long r = this.basis;
               int i = this.baseIndex;

               while (this.batch > 0) {
                  int f = this.baseLimit;
                  int h;
                  if ((h = this.baseLimit + i >>> 1) <= i) {
                     break;
                  }

                  this.addToPendingCount(1);
                  (this.rights = new Long2ObjectConcurrentHashMap.MapReduceKeysToLongTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer
                     ))
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  r = reducer.applyAsLong(r, transformer.applyAsLong(p.key));
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Long2ObjectConcurrentHashMap.MapReduceKeysToLongTask<V> t = (Long2ObjectConcurrentHashMap.MapReduceKeysToLongTask<V>)c;

                  for (Long2ObjectConcurrentHashMap.MapReduceKeysToLongTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     t.result = reducer.applyAsLong(t.result, s.result);
                  }
               }
            }
         }
      }
   }

   protected static final class MapReduceMappingsTask<V, U> extends Long2ObjectConcurrentHashMap.BulkTask<V, U> {
      public final Long2ObjectConcurrentHashMap.LongObjFunction<? super V, ? extends U> transformer;
      public final BiFunction<? super U, ? super U, ? extends U> reducer;
      public U result;
      public Long2ObjectConcurrentHashMap.MapReduceMappingsTask<V, U> rights;
      public Long2ObjectConcurrentHashMap.MapReduceMappingsTask<V, U> nextRight;

      public MapReduceMappingsTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Long2ObjectConcurrentHashMap.MapReduceMappingsTask<V, U> nextRight,
         Long2ObjectConcurrentHashMap.LongObjFunction<? super V, ? extends U> transformer,
         BiFunction<? super U, ? super U, ? extends U> reducer
      ) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.reducer = reducer;
      }

      @Override
      public final U getRawResult() {
         return this.result;
      }

      @Override
      public final void compute() {
         Long2ObjectConcurrentHashMap.LongObjFunction<? super V, ? extends U> transformer = this.transformer;
         if (this.transformer != null) {
            BiFunction<? super U, ? super U, ? extends U> reducer = this.reducer;
            if (this.reducer != null) {
               int i = this.baseIndex;

               while (this.batch > 0) {
                  int f = this.baseLimit;
                  int h;
                  if ((h = this.baseLimit + i >>> 1) <= i) {
                     break;
                  }

                  this.addToPendingCount(1);
                  (this.rights = new Long2ObjectConcurrentHashMap.MapReduceMappingsTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, reducer
                     ))
                     .fork();
               }

               U r = null;

               Long2ObjectConcurrentHashMap.Node<V> p;
               while ((p = this.advance()) != null) {
                  U u;
                  if ((u = (U)transformer.apply(p.key, p.val)) != null) {
                     r = r == null ? u : reducer.apply(r, u);
                  }
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Long2ObjectConcurrentHashMap.MapReduceMappingsTask<V, U> t = (Long2ObjectConcurrentHashMap.MapReduceMappingsTask<V, U>)c;

                  for (Long2ObjectConcurrentHashMap.MapReduceMappingsTask<V, U> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     U sr = s.result;
                     if (s.result != null) {
                        U tr = t.result;
                        t.result = t.result == null ? sr : reducer.apply(tr, sr);
                     }
                  }
               }
            }
         }
      }
   }

   protected static final class MapReduceMappingsToDoubleTask<V> extends Long2ObjectConcurrentHashMap.DoubleReturningBulkTask<V> {
      public final Long2ObjectConcurrentHashMap.ToDoubleLongObjFunction<? super V> transformer;
      public final DoubleBinaryOperator reducer;
      public final double basis;
      public Long2ObjectConcurrentHashMap.MapReduceMappingsToDoubleTask<V> rights;
      public Long2ObjectConcurrentHashMap.MapReduceMappingsToDoubleTask<V> nextRight;

      public MapReduceMappingsToDoubleTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Long2ObjectConcurrentHashMap.MapReduceMappingsToDoubleTask<V> nextRight,
         Long2ObjectConcurrentHashMap.ToDoubleLongObjFunction<? super V> transformer,
         double basis,
         DoubleBinaryOperator reducer
      ) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.basis = basis;
         this.reducer = reducer;
      }

      public final Double getRawResult() {
         throw new UnsupportedOperationException();
      }

      @Override
      public final void compute() {
         Long2ObjectConcurrentHashMap.ToDoubleLongObjFunction<? super V> transformer = this.transformer;
         if (this.transformer != null) {
            DoubleBinaryOperator reducer = this.reducer;
            if (this.reducer != null) {
               double r = this.basis;
               int i = this.baseIndex;

               while (this.batch > 0) {
                  int f = this.baseLimit;
                  int h;
                  if ((h = this.baseLimit + i >>> 1) <= i) {
                     break;
                  }

                  this.addToPendingCount(1);
                  (this.rights = new Long2ObjectConcurrentHashMap.MapReduceMappingsToDoubleTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer
                     ))
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  r = reducer.applyAsDouble(r, transformer.applyAsDouble(p.key, p.val));
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Long2ObjectConcurrentHashMap.MapReduceMappingsToDoubleTask<V> t = (Long2ObjectConcurrentHashMap.MapReduceMappingsToDoubleTask<V>)c;

                  for (Long2ObjectConcurrentHashMap.MapReduceMappingsToDoubleTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     t.result = reducer.applyAsDouble(t.result, s.result);
                  }
               }
            }
         }
      }
   }

   protected static final class MapReduceMappingsToIntTask<V> extends Long2ObjectConcurrentHashMap.IntReturningBulkTask<V> {
      public final Long2ObjectConcurrentHashMap.ToIntLongObjFunction<? super V> transformer;
      public final IntBinaryOperator reducer;
      public final int basis;
      public Long2ObjectConcurrentHashMap.MapReduceMappingsToIntTask<V> rights;
      public Long2ObjectConcurrentHashMap.MapReduceMappingsToIntTask<V> nextRight;

      public MapReduceMappingsToIntTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Long2ObjectConcurrentHashMap.MapReduceMappingsToIntTask<V> nextRight,
         Long2ObjectConcurrentHashMap.ToIntLongObjFunction<? super V> transformer,
         int basis,
         IntBinaryOperator reducer
      ) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.basis = basis;
         this.reducer = reducer;
      }

      public final Integer getRawResult() {
         throw new UnsupportedOperationException();
      }

      @Override
      public final void compute() {
         Long2ObjectConcurrentHashMap.ToIntLongObjFunction<? super V> transformer = this.transformer;
         if (this.transformer != null) {
            IntBinaryOperator reducer = this.reducer;
            if (this.reducer != null) {
               int r = this.basis;
               int i = this.baseIndex;

               while (this.batch > 0) {
                  int f = this.baseLimit;
                  int h;
                  if ((h = this.baseLimit + i >>> 1) <= i) {
                     break;
                  }

                  this.addToPendingCount(1);
                  (this.rights = new Long2ObjectConcurrentHashMap.MapReduceMappingsToIntTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer
                     ))
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  r = reducer.applyAsInt(r, transformer.applyAsInt(p.key, p.val));
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Long2ObjectConcurrentHashMap.MapReduceMappingsToIntTask<V> t = (Long2ObjectConcurrentHashMap.MapReduceMappingsToIntTask<V>)c;

                  for (Long2ObjectConcurrentHashMap.MapReduceMappingsToIntTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     t.result = reducer.applyAsInt(t.result, s.result);
                  }
               }
            }
         }
      }
   }

   protected static final class MapReduceMappingsToLongTask<V> extends Long2ObjectConcurrentHashMap.LongReturningBulkTask<V> {
      public final Long2ObjectConcurrentHashMap.ToLongLongObjFunction<? super V> transformer;
      public final LongBinaryOperator reducer;
      public final long basis;
      public Long2ObjectConcurrentHashMap.MapReduceMappingsToLongTask<V> rights;
      public Long2ObjectConcurrentHashMap.MapReduceMappingsToLongTask<V> nextRight;

      public MapReduceMappingsToLongTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Long2ObjectConcurrentHashMap.MapReduceMappingsToLongTask<V> nextRight,
         Long2ObjectConcurrentHashMap.ToLongLongObjFunction<? super V> transformer,
         long basis,
         LongBinaryOperator reducer
      ) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.basis = basis;
         this.reducer = reducer;
      }

      public final Long getRawResult() {
         throw new UnsupportedOperationException();
      }

      @Override
      public final void compute() {
         Long2ObjectConcurrentHashMap.ToLongLongObjFunction<? super V> transformer = this.transformer;
         if (this.transformer != null) {
            LongBinaryOperator reducer = this.reducer;
            if (this.reducer != null) {
               long r = this.basis;
               int i = this.baseIndex;

               while (this.batch > 0) {
                  int f = this.baseLimit;
                  int h;
                  if ((h = this.baseLimit + i >>> 1) <= i) {
                     break;
                  }

                  this.addToPendingCount(1);
                  (this.rights = new Long2ObjectConcurrentHashMap.MapReduceMappingsToLongTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer
                     ))
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  r = reducer.applyAsLong(r, transformer.applyAsLong(p.key, p.val));
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Long2ObjectConcurrentHashMap.MapReduceMappingsToLongTask<V> t = (Long2ObjectConcurrentHashMap.MapReduceMappingsToLongTask<V>)c;

                  for (Long2ObjectConcurrentHashMap.MapReduceMappingsToLongTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     t.result = reducer.applyAsLong(t.result, s.result);
                  }
               }
            }
         }
      }
   }

   protected static final class MapReduceValuesTask<V, U> extends Long2ObjectConcurrentHashMap.BulkTask<V, U> {
      public final Function<? super V, ? extends U> transformer;
      public final BiFunction<? super U, ? super U, ? extends U> reducer;
      public U result;
      public Long2ObjectConcurrentHashMap.MapReduceValuesTask<V, U> rights;
      public Long2ObjectConcurrentHashMap.MapReduceValuesTask<V, U> nextRight;

      public MapReduceValuesTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Long2ObjectConcurrentHashMap.MapReduceValuesTask<V, U> nextRight,
         Function<? super V, ? extends U> transformer,
         BiFunction<? super U, ? super U, ? extends U> reducer
      ) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.reducer = reducer;
      }

      @Override
      public final U getRawResult() {
         return this.result;
      }

      @Override
      public final void compute() {
         Function<? super V, ? extends U> transformer = this.transformer;
         if (this.transformer != null) {
            BiFunction<? super U, ? super U, ? extends U> reducer = this.reducer;
            if (this.reducer != null) {
               int i = this.baseIndex;

               while (this.batch > 0) {
                  int f = this.baseLimit;
                  int h;
                  if ((h = this.baseLimit + i >>> 1) <= i) {
                     break;
                  }

                  this.addToPendingCount(1);
                  (this.rights = new Long2ObjectConcurrentHashMap.MapReduceValuesTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, reducer
                     ))
                     .fork();
               }

               U r = null;

               Long2ObjectConcurrentHashMap.Node<V> p;
               while ((p = this.advance()) != null) {
                  U u;
                  if ((u = (U)transformer.apply(p.val)) != null) {
                     r = r == null ? u : reducer.apply(r, u);
                  }
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Long2ObjectConcurrentHashMap.MapReduceValuesTask<V, U> t = (Long2ObjectConcurrentHashMap.MapReduceValuesTask<V, U>)c;

                  for (Long2ObjectConcurrentHashMap.MapReduceValuesTask<V, U> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     U sr = s.result;
                     if (s.result != null) {
                        U tr = t.result;
                        t.result = t.result == null ? sr : reducer.apply(tr, sr);
                     }
                  }
               }
            }
         }
      }
   }

   protected static final class MapReduceValuesToDoubleTask<V> extends Long2ObjectConcurrentHashMap.DoubleReturningBulkTask<V> {
      public final ToDoubleFunction<? super V> transformer;
      public final DoubleBinaryOperator reducer;
      public final double basis;
      public Long2ObjectConcurrentHashMap.MapReduceValuesToDoubleTask<V> rights;
      public Long2ObjectConcurrentHashMap.MapReduceValuesToDoubleTask<V> nextRight;

      public MapReduceValuesToDoubleTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Long2ObjectConcurrentHashMap.MapReduceValuesToDoubleTask<V> nextRight,
         ToDoubleFunction<? super V> transformer,
         double basis,
         DoubleBinaryOperator reducer
      ) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.basis = basis;
         this.reducer = reducer;
      }

      public final Double getRawResult() {
         throw new UnsupportedOperationException();
      }

      @Override
      public final void compute() {
         ToDoubleFunction<? super V> transformer = this.transformer;
         if (this.transformer != null) {
            DoubleBinaryOperator reducer = this.reducer;
            if (this.reducer != null) {
               double r = this.basis;
               int i = this.baseIndex;

               while (this.batch > 0) {
                  int f = this.baseLimit;
                  int h;
                  if ((h = this.baseLimit + i >>> 1) <= i) {
                     break;
                  }

                  this.addToPendingCount(1);
                  (this.rights = new Long2ObjectConcurrentHashMap.MapReduceValuesToDoubleTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer
                     ))
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  r = reducer.applyAsDouble(r, transformer.applyAsDouble(p.val));
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Long2ObjectConcurrentHashMap.MapReduceValuesToDoubleTask<V> t = (Long2ObjectConcurrentHashMap.MapReduceValuesToDoubleTask<V>)c;

                  for (Long2ObjectConcurrentHashMap.MapReduceValuesToDoubleTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     t.result = reducer.applyAsDouble(t.result, s.result);
                  }
               }
            }
         }
      }
   }

   protected static final class MapReduceValuesToIntTask<V> extends Long2ObjectConcurrentHashMap.IntReturningBulkTask<V> {
      public final ToIntFunction<? super V> transformer;
      public final IntBinaryOperator reducer;
      public final int basis;
      public Long2ObjectConcurrentHashMap.MapReduceValuesToIntTask<V> rights;
      public Long2ObjectConcurrentHashMap.MapReduceValuesToIntTask<V> nextRight;

      public MapReduceValuesToIntTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Long2ObjectConcurrentHashMap.MapReduceValuesToIntTask<V> nextRight,
         ToIntFunction<? super V> transformer,
         int basis,
         IntBinaryOperator reducer
      ) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.basis = basis;
         this.reducer = reducer;
      }

      public final Integer getRawResult() {
         throw new UnsupportedOperationException();
      }

      @Override
      public final void compute() {
         ToIntFunction<? super V> transformer = this.transformer;
         if (this.transformer != null) {
            IntBinaryOperator reducer = this.reducer;
            if (this.reducer != null) {
               int r = this.basis;
               int i = this.baseIndex;

               while (this.batch > 0) {
                  int f = this.baseLimit;
                  int h;
                  if ((h = this.baseLimit + i >>> 1) <= i) {
                     break;
                  }

                  this.addToPendingCount(1);
                  (this.rights = new Long2ObjectConcurrentHashMap.MapReduceValuesToIntTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer
                     ))
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  r = reducer.applyAsInt(r, transformer.applyAsInt(p.val));
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Long2ObjectConcurrentHashMap.MapReduceValuesToIntTask<V> t = (Long2ObjectConcurrentHashMap.MapReduceValuesToIntTask<V>)c;

                  for (Long2ObjectConcurrentHashMap.MapReduceValuesToIntTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     t.result = reducer.applyAsInt(t.result, s.result);
                  }
               }
            }
         }
      }
   }

   protected static final class MapReduceValuesToLongTask<V> extends Long2ObjectConcurrentHashMap.LongReturningBulkTask<V> {
      public final Long2ObjectConcurrentHashMap.ToLongFunction<? super V> transformer;
      public final LongBinaryOperator reducer;
      public final long basis;
      public Long2ObjectConcurrentHashMap.MapReduceValuesToLongTask<V> rights;
      public Long2ObjectConcurrentHashMap.MapReduceValuesToLongTask<V> nextRight;

      public MapReduceValuesToLongTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Long2ObjectConcurrentHashMap.MapReduceValuesToLongTask<V> nextRight,
         Long2ObjectConcurrentHashMap.ToLongFunction<? super V> transformer,
         long basis,
         LongBinaryOperator reducer
      ) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.basis = basis;
         this.reducer = reducer;
      }

      public final Long getRawResult() {
         throw new UnsupportedOperationException();
      }

      @Override
      public final void compute() {
         Long2ObjectConcurrentHashMap.ToLongFunction<? super V> transformer = this.transformer;
         if (this.transformer != null) {
            LongBinaryOperator reducer = this.reducer;
            if (this.reducer != null) {
               long r = this.basis;
               int i = this.baseIndex;

               while (this.batch > 0) {
                  int f = this.baseLimit;
                  int h;
                  if ((h = this.baseLimit + i >>> 1) <= i) {
                     break;
                  }

                  this.addToPendingCount(1);
                  (this.rights = new Long2ObjectConcurrentHashMap.MapReduceValuesToLongTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer
                     ))
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  r = reducer.applyAsLong(r, transformer.applyAsLong(p.val));
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Long2ObjectConcurrentHashMap.MapReduceValuesToLongTask<V> t = (Long2ObjectConcurrentHashMap.MapReduceValuesToLongTask<V>)c;

                  for (Long2ObjectConcurrentHashMap.MapReduceValuesToLongTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     t.result = reducer.applyAsLong(t.result, s.result);
                  }
               }
            }
         }
      }
   }

   protected static class Node<V> implements Long2ObjectConcurrentHashMap.Entry<V> {
      public final long EMPTY;
      public final int hash;
      public final long key;
      public volatile V val;
      public volatile Long2ObjectConcurrentHashMap.Node<V> next;

      public Node(long empty, int hash, long key, V val, Long2ObjectConcurrentHashMap.Node<V> next) {
         this.EMPTY = empty;
         this.hash = hash;
         this.key = key;
         this.val = val;
         this.next = next;
      }

      @Override
      public final boolean isEmpty() {
         return this.key == this.EMPTY;
      }

      @Override
      public final Long getKey() {
         return this.key;
      }

      @Override
      public final long getLongKey() {
         return this.key;
      }

      @Override
      public final V getValue() {
         return this.val;
      }

      @Override
      public final int hashCode() {
         return Long.hashCode(this.key) ^ this.val.hashCode();
      }

      @Override
      public final String toString() {
         return this.isEmpty() ? "EMPTY=" + this.val : this.key + "=" + this.val;
      }

      @Override
      public final V setValue(V value) {
         throw new UnsupportedOperationException();
      }

      @Override
      public final boolean equals(Object o) {
         boolean empty = this.isEmpty();
         if (o instanceof Long2ObjectConcurrentHashMap.Entry) {
            if (empty != ((Long2ObjectConcurrentHashMap.Entry)o).isEmpty()) {
               return false;
            } else {
               return !empty && this.key != ((Long2ObjectConcurrentHashMap.Entry)o).getLongKey()
                  ? false
                  : this.val.equals(((Long2ObjectConcurrentHashMap.Entry)o).getValue());
            }
         } else {
            return false;
         }
      }

      protected Long2ObjectConcurrentHashMap.Node<V> find(int h, long k) {
         Long2ObjectConcurrentHashMap.Node<V> e = this;
         if (k != this.EMPTY) {
            do {
               if (e.hash == h) {
                  long ek = e.key;
                  if (e.key == k || ek != this.EMPTY && k == ek) {
                     return e;
                  }
               }
            } while ((e = e.next) != null);
         }

         return null;
      }
   }

   protected static final class ReduceEntriesTask<V> extends Long2ObjectConcurrentHashMap.BulkTask<V, Long2ObjectConcurrentHashMap.Entry<V>> {
      public final BiFunction<Long2ObjectConcurrentHashMap.Entry<V>, Long2ObjectConcurrentHashMap.Entry<V>, ? extends Long2ObjectConcurrentHashMap.Entry<V>> reducer;
      public Long2ObjectConcurrentHashMap.Entry<V> result;
      public Long2ObjectConcurrentHashMap.ReduceEntriesTask<V> rights;
      public Long2ObjectConcurrentHashMap.ReduceEntriesTask<V> nextRight;

      public ReduceEntriesTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Long2ObjectConcurrentHashMap.ReduceEntriesTask<V> nextRight,
         BiFunction<Long2ObjectConcurrentHashMap.Entry<V>, Long2ObjectConcurrentHashMap.Entry<V>, ? extends Long2ObjectConcurrentHashMap.Entry<V>> reducer
      ) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.reducer = reducer;
      }

      public final Long2ObjectConcurrentHashMap.Entry<V> getRawResult() {
         return this.result;
      }

      @Override
      public final void compute() {
         BiFunction<Long2ObjectConcurrentHashMap.Entry<V>, Long2ObjectConcurrentHashMap.Entry<V>, ? extends Long2ObjectConcurrentHashMap.Entry<V>> reducer = this.reducer;
         if (this.reducer != null) {
            int i = this.baseIndex;

            while (this.batch > 0) {
               int f = this.baseLimit;
               int h;
               if ((h = this.baseLimit + i >>> 1) <= i) {
                  break;
               }

               this.addToPendingCount(1);
               (this.rights = new Long2ObjectConcurrentHashMap.ReduceEntriesTask<>(
                     this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, reducer
                  ))
                  .fork();
            }

            Long2ObjectConcurrentHashMap.Entry<V> r = null;

            Long2ObjectConcurrentHashMap.Node<V> p;
            while ((p = this.advance()) != null) {
               r = (Long2ObjectConcurrentHashMap.Entry<V>)(r == null ? p : reducer.apply(r, p));
            }

            this.result = r;

            for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
               Long2ObjectConcurrentHashMap.ReduceEntriesTask<V> t = (Long2ObjectConcurrentHashMap.ReduceEntriesTask<V>)c;

               for (Long2ObjectConcurrentHashMap.ReduceEntriesTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                  Long2ObjectConcurrentHashMap.Entry<V> sr = s.result;
                  if (s.result != null) {
                     Long2ObjectConcurrentHashMap.Entry<V> tr = t.result;
                     t.result = t.result == null ? sr : reducer.apply(tr, sr);
                  }
               }
            }
         }
      }
   }

   protected static final class ReduceKeysTask<V> extends Long2ObjectConcurrentHashMap.LongReturningBulkTask2<V> {
      public final long EMPTY;
      public final Long2ObjectConcurrentHashMap.LongReduceTaskOperator reducer;
      public Long2ObjectConcurrentHashMap.ReduceKeysTask<V> rights;
      public Long2ObjectConcurrentHashMap.ReduceKeysTask<V> nextRight;

      public ReduceKeysTask(
         long EMPTY,
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Long2ObjectConcurrentHashMap.ReduceKeysTask<V> nextRight,
         Long2ObjectConcurrentHashMap.LongReduceTaskOperator reducer
      ) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.EMPTY = EMPTY;
         this.reducer = reducer;
      }

      public final Long getRawResult() {
         throw new UnsupportedOperationException();
      }

      @Override
      public final void compute() {
         Long2ObjectConcurrentHashMap.LongReduceTaskOperator reducer = this.reducer;
         if (this.reducer != null) {
            int i = this.baseIndex;

            while (this.batch > 0) {
               int f = this.baseLimit;
               int h;
               if ((h = this.baseLimit + i >>> 1) <= i) {
                  break;
               }

               this.addToPendingCount(1);
               (this.rights = new Long2ObjectConcurrentHashMap.ReduceKeysTask<>(
                     this.EMPTY, this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, reducer
                  ))
                  .fork();
            }

            boolean found = false;
            long r = this.EMPTY;

            Long2ObjectConcurrentHashMap.Node<V> p;
            while ((p = this.advance()) != null) {
               long u = p.key;
               if (!found) {
                  found = true;
                  r = u;
               } else if (!p.isEmpty()) {
                  found = true;
                  r = reducer.reduce(this.EMPTY, r, u);
               }
            }

            this.result = r;

            for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
               Long2ObjectConcurrentHashMap.ReduceKeysTask<V> t = (Long2ObjectConcurrentHashMap.ReduceKeysTask<V>)c;

               for (Long2ObjectConcurrentHashMap.ReduceKeysTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                  long sr = s.result;
                  if (s.result != this.EMPTY) {
                     long tr = t.result;
                     t.result = t.result == this.EMPTY ? sr : reducer.reduce(this.EMPTY, tr, sr);
                  }
               }
            }
         }
      }
   }

   protected static final class ReduceValuesTask<V> extends Long2ObjectConcurrentHashMap.BulkTask<V, V> {
      public final BiFunction<? super V, ? super V, ? extends V> reducer;
      public V result;
      public Long2ObjectConcurrentHashMap.ReduceValuesTask<V> rights;
      public Long2ObjectConcurrentHashMap.ReduceValuesTask<V> nextRight;

      public ReduceValuesTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Long2ObjectConcurrentHashMap.ReduceValuesTask<V> nextRight,
         BiFunction<? super V, ? super V, ? extends V> reducer
      ) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.reducer = reducer;
      }

      @Override
      public final V getRawResult() {
         return this.result;
      }

      @Override
      public final void compute() {
         BiFunction<? super V, ? super V, ? extends V> reducer = this.reducer;
         if (this.reducer != null) {
            int i = this.baseIndex;

            while (this.batch > 0) {
               int f = this.baseLimit;
               int h;
               if ((h = this.baseLimit + i >>> 1) <= i) {
                  break;
               }

               this.addToPendingCount(1);
               (this.rights = new Long2ObjectConcurrentHashMap.ReduceValuesTask<>(
                     this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, reducer
                  ))
                  .fork();
            }

            V r = null;

            Long2ObjectConcurrentHashMap.Node<V> p;
            while ((p = this.advance()) != null) {
               V v = p.val;
               r = r == null ? v : reducer.apply(r, v);
            }

            this.result = r;

            for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
               Long2ObjectConcurrentHashMap.ReduceValuesTask<V> t = (Long2ObjectConcurrentHashMap.ReduceValuesTask<V>)c;

               for (Long2ObjectConcurrentHashMap.ReduceValuesTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                  V sr = s.result;
                  if (s.result != null) {
                     V tr = t.result;
                     t.result = t.result == null ? sr : reducer.apply(tr, sr);
                  }
               }
            }
         }
      }
   }

   protected static final class ReservationNode<V> extends Long2ObjectConcurrentHashMap.Node<V> {
      public ReservationNode(long empty) {
         super(empty, -3, empty, null, null);
      }

      @Override
      protected Long2ObjectConcurrentHashMap.Node<V> find(int h, long k) {
         return null;
      }
   }

   protected static final class SearchEntriesTask<V, U> extends Long2ObjectConcurrentHashMap.BulkTask<V, U> {
      public final Function<Long2ObjectConcurrentHashMap.Entry<V>, ? extends U> searchFunction;
      public final AtomicReference<U> result;

      public SearchEntriesTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Function<Long2ObjectConcurrentHashMap.Entry<V>, ? extends U> searchFunction,
         AtomicReference<U> result
      ) {
         super(p, b, i, f, t);
         this.searchFunction = searchFunction;
         this.result = result;
      }

      @Override
      public final U getRawResult() {
         return this.result.get();
      }

      @Override
      public final void compute() {
         Function<Long2ObjectConcurrentHashMap.Entry<V>, ? extends U> searchFunction = this.searchFunction;
         if (this.searchFunction != null) {
            AtomicReference<U> result = this.result;
            if (this.result != null) {
               int i = this.baseIndex;

               while (this.batch > 0) {
                  int f = this.baseLimit;
                  int h;
                  if ((h = this.baseLimit + i >>> 1) <= i) {
                     break;
                  }

                  if (result.get() != null) {
                     return;
                  }

                  this.addToPendingCount(1);
                  new Long2ObjectConcurrentHashMap.SearchEntriesTask<>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, searchFunction, result).fork();
               }

               while (result.get() == null) {
                  Long2ObjectConcurrentHashMap.Node<V> p;
                  if ((p = this.advance()) == null) {
                     this.propagateCompletion();
                     break;
                  }

                  U u;
                  if ((u = (U)searchFunction.apply(p)) != null) {
                     if (result.compareAndSet(null, u)) {
                        this.quietlyCompleteRoot();
                     }

                     return;
                  }
               }
            }
         }
      }
   }

   protected static final class SearchKeysTask<V, U> extends Long2ObjectConcurrentHashMap.BulkTask<V, U> {
      public final Long2ObjectConcurrentHashMap.LongFunction<? extends U> searchFunction;
      public final AtomicReference<U> result;

      public SearchKeysTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Long2ObjectConcurrentHashMap.LongFunction<? extends U> searchFunction,
         AtomicReference<U> result
      ) {
         super(p, b, i, f, t);
         this.searchFunction = searchFunction;
         this.result = result;
      }

      @Override
      public final U getRawResult() {
         return this.result.get();
      }

      @Override
      public final void compute() {
         Long2ObjectConcurrentHashMap.LongFunction<? extends U> searchFunction = this.searchFunction;
         if (this.searchFunction != null) {
            AtomicReference<U> result = this.result;
            if (this.result != null) {
               int i = this.baseIndex;

               while (this.batch > 0) {
                  int f = this.baseLimit;
                  int h;
                  if ((h = this.baseLimit + i >>> 1) <= i) {
                     break;
                  }

                  if (result.get() != null) {
                     return;
                  }

                  this.addToPendingCount(1);
                  new Long2ObjectConcurrentHashMap.SearchKeysTask<>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, searchFunction, result).fork();
               }

               while (result.get() == null) {
                  Long2ObjectConcurrentHashMap.Node<V> p;
                  if ((p = this.advance()) == null) {
                     this.propagateCompletion();
                     break;
                  }

                  U u;
                  if ((u = (U)searchFunction.apply(p.key)) != null) {
                     if (result.compareAndSet(null, u)) {
                        this.quietlyCompleteRoot();
                     }
                     break;
                  }
               }
            }
         }
      }
   }

   protected static final class SearchMappingsTask<V, U> extends Long2ObjectConcurrentHashMap.BulkTask<V, U> {
      public final Long2ObjectConcurrentHashMap.LongObjFunction<? super V, ? extends U> searchFunction;
      public final AtomicReference<U> result;

      public SearchMappingsTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Long2ObjectConcurrentHashMap.LongObjFunction<? super V, ? extends U> searchFunction,
         AtomicReference<U> result
      ) {
         super(p, b, i, f, t);
         this.searchFunction = searchFunction;
         this.result = result;
      }

      @Override
      public final U getRawResult() {
         return this.result.get();
      }

      @Override
      public final void compute() {
         Long2ObjectConcurrentHashMap.LongObjFunction<? super V, ? extends U> searchFunction = this.searchFunction;
         if (this.searchFunction != null) {
            AtomicReference<U> result = this.result;
            if (this.result != null) {
               int i = this.baseIndex;

               while (this.batch > 0) {
                  int f = this.baseLimit;
                  int h;
                  if ((h = this.baseLimit + i >>> 1) <= i) {
                     break;
                  }

                  if (result.get() != null) {
                     return;
                  }

                  this.addToPendingCount(1);
                  new Long2ObjectConcurrentHashMap.SearchMappingsTask<>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, searchFunction, result)
                     .fork();
               }

               while (result.get() == null) {
                  Long2ObjectConcurrentHashMap.Node<V> p;
                  if ((p = this.advance()) == null) {
                     this.propagateCompletion();
                     break;
                  }

                  U u;
                  if ((u = (U)searchFunction.apply(p.key, p.val)) != null) {
                     if (result.compareAndSet(null, u)) {
                        this.quietlyCompleteRoot();
                     }
                     break;
                  }
               }
            }
         }
      }
   }

   protected static final class SearchValuesTask<V, U> extends Long2ObjectConcurrentHashMap.BulkTask<V, U> {
      public final Function<? super V, ? extends U> searchFunction;
      public final AtomicReference<U> result;

      public SearchValuesTask(
         Long2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Long2ObjectConcurrentHashMap.Node<V>[] t,
         Function<? super V, ? extends U> searchFunction,
         AtomicReference<U> result
      ) {
         super(p, b, i, f, t);
         this.searchFunction = searchFunction;
         this.result = result;
      }

      @Override
      public final U getRawResult() {
         return this.result.get();
      }

      @Override
      public final void compute() {
         Function<? super V, ? extends U> searchFunction = this.searchFunction;
         if (this.searchFunction != null) {
            AtomicReference<U> result = this.result;
            if (this.result != null) {
               int i = this.baseIndex;

               while (this.batch > 0) {
                  int f = this.baseLimit;
                  int h;
                  if ((h = this.baseLimit + i >>> 1) <= i) {
                     break;
                  }

                  if (result.get() != null) {
                     return;
                  }

                  this.addToPendingCount(1);
                  new Long2ObjectConcurrentHashMap.SearchValuesTask<>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, searchFunction, result).fork();
               }

               while (result.get() == null) {
                  Long2ObjectConcurrentHashMap.Node<V> p;
                  if ((p = this.advance()) == null) {
                     this.propagateCompletion();
                     break;
                  }

                  U u;
                  if ((u = (U)searchFunction.apply(p.val)) != null) {
                     if (result.compareAndSet(null, u)) {
                        this.quietlyCompleteRoot();
                     }
                     break;
                  }
               }
            }
         }
      }
   }

   protected static class Segment<V> extends ReentrantLock implements Serializable {
      public static final long serialVersionUID = 2249069246763182397L;
      public final float loadFactor;

      public Segment(float lf) {
         this.loadFactor = lf;
      }
   }

   protected static final class TableStack<V> {
      public int length;
      public int index;
      public Long2ObjectConcurrentHashMap.Node<V>[] tab;
      public Long2ObjectConcurrentHashMap.TableStack<V> next;

      public TableStack() {
      }
   }

   @FunctionalInterface
   public interface ToDoubleLongObjFunction<V> {
      double applyAsDouble(long var1, V var3);
   }

   @FunctionalInterface
   public interface ToIntLongObjFunction<V> {
      int applyAsInt(long var1, V var3);
   }

   @FunctionalInterface
   public interface ToLongFunction<T> {
      long applyAsLong(T var1);
   }

   @FunctionalInterface
   public interface ToLongLongObjFunction<V> {
      long applyAsLong(long var1, V var3);
   }

   protected static class Traverser<V> {
      public Long2ObjectConcurrentHashMap.Node<V>[] tab;
      public Long2ObjectConcurrentHashMap.Node<V> next;
      public Long2ObjectConcurrentHashMap.TableStack<V> stack;
      public Long2ObjectConcurrentHashMap.TableStack<V> spare;
      public int index;
      public int baseIndex;
      public int baseLimit;
      public final int baseSize;

      public Traverser(Long2ObjectConcurrentHashMap.Node<V>[] tab, int size, int index, int limit) {
         this.tab = tab;
         this.baseSize = size;
         this.baseIndex = this.index = index;
         this.baseLimit = limit;
         this.next = null;
      }

      protected final Long2ObjectConcurrentHashMap.Node<V> advance() {
         Long2ObjectConcurrentHashMap.Node<V> e = this.next;
         if (this.next != null) {
            e = e.next;
         }

         while (true) {
            if (e != null) {
               return this.next = e;
            }

            if (this.baseIndex >= this.baseLimit) {
               break;
            }

            Long2ObjectConcurrentHashMap.Node<V>[] t = this.tab;
            if (this.tab == null) {
               break;
            }

            int n;
            int var10000 = n = t.length;
            int i = this.index;
            if (var10000 <= this.index || i < 0) {
               break;
            }

            if ((e = Long2ObjectConcurrentHashMap.tabAt(t, i)) != null && e.hash < 0) {
               if (e instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                  this.tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)e).nextTable;
                  e = null;
                  this.pushState(t, i, n);
                  continue;
               }

               if (e instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                  e = ((Long2ObjectConcurrentHashMap.TreeBin)e).first;
               } else {
                  e = null;
               }
            }

            if (this.stack != null) {
               this.recoverState(n);
            } else if ((this.index = i + this.baseSize) >= n) {
               this.index = ++this.baseIndex;
            }
         }

         return this.next = null;
      }

      protected void pushState(Long2ObjectConcurrentHashMap.Node<V>[] t, int i, int n) {
         Long2ObjectConcurrentHashMap.TableStack<V> s = this.spare;
         if (s != null) {
            this.spare = s.next;
         } else {
            s = new Long2ObjectConcurrentHashMap.TableStack<>();
         }

         s.tab = t;
         s.length = n;
         s.index = i;
         s.next = this.stack;
         this.stack = s;
      }

      protected void recoverState(int n) {
         while (true) {
            Long2ObjectConcurrentHashMap.TableStack<V> s = this.stack;
            if (this.stack != null) {
               int len = s.length;
               if ((this.index = this.index + s.length) >= n) {
                  n = len;
                  this.index = s.index;
                  this.tab = s.tab;
                  s.tab = null;
                  Long2ObjectConcurrentHashMap.TableStack<V> next = s.next;
                  s.next = this.spare;
                  this.stack = next;
                  this.spare = s;
                  continue;
               }
            }

            if (s == null && (this.index = this.index + this.baseSize) >= n) {
               this.index = ++this.baseIndex;
            }

            return;
         }
      }
   }

   protected static final class TreeBin<V> extends Long2ObjectConcurrentHashMap.Node<V> {
      public Long2ObjectConcurrentHashMap.TreeNode<V> root;
      public volatile Long2ObjectConcurrentHashMap.TreeNode<V> first;
      public volatile Thread waiter;
      public volatile int lockState;
      public static final int WRITER = 1;
      public static final int WAITER = 2;
      public static final int READER = 4;
      protected static final Unsafe U;
      protected static final long LOCKSTATE;

      protected int tieBreakOrder(long a, long b) {
         int comp = Long.compare(a, b);
         return comp > 0 ? 1 : -1;
      }

      public TreeBin(long empty, Long2ObjectConcurrentHashMap.TreeNode<V> b) {
         super(empty, -2, empty, null, null);
         this.first = b;
         Long2ObjectConcurrentHashMap.TreeNode<V> r = null;
         Long2ObjectConcurrentHashMap.TreeNode<V> x = b;

         while (x != null) {
            Long2ObjectConcurrentHashMap.TreeNode<V> next = (Long2ObjectConcurrentHashMap.TreeNode<V>)x.next;
            x.left = x.right = null;
            if (r == null) {
               x.parent = null;
               x.red = false;
               r = x;
            } else {
               long k = x.key;
               int h = x.hash;
               Class<?> kc = null;
               Long2ObjectConcurrentHashMap.TreeNode<V> p = r;

               int dir;
               Long2ObjectConcurrentHashMap.TreeNode<V> xp;
               do {
                  long pk = p.key;
                  int ph = p.hash;
                  if (p.hash > h) {
                     dir = -1;
                  } else if (ph < h) {
                     dir = 1;
                  } else if ((dir = Long.compare(k, pk)) == 0) {
                     dir = this.tieBreakOrder(k, pk);
                  }

                  xp = p;
               } while ((p = dir <= 0 ? p.left : p.right) != null);

               x.parent = xp;
               if (dir <= 0) {
                  xp.left = x;
               } else {
                  xp.right = x;
               }

               r = this.balanceInsertion(r, x);
            }

            x = next;
         }

         this.root = r;

         assert this.checkInvariants(this.root);
      }

      protected final void lockRoot() {
         if (!U.compareAndSwapInt(this, LOCKSTATE, 0, 1)) {
            this.contendedLock();
         }
      }

      protected final void unlockRoot() {
         this.lockState = 0;
      }

      protected final void contendedLock() {
         boolean waiting = false;

         while (true) {
            int s = this.lockState;
            if ((this.lockState & -3) == 0) {
               if (U.compareAndSwapInt(this, LOCKSTATE, s, 1)) {
                  if (waiting) {
                     this.waiter = null;
                  }

                  return;
               }
            } else if ((s & 2) == 0) {
               if (U.compareAndSwapInt(this, LOCKSTATE, s, s | 2)) {
                  waiting = true;
                  this.waiter = Thread.currentThread();
               }
            } else if (waiting) {
               LockSupport.park(this);
            }
         }
      }

      @Override
      protected final Long2ObjectConcurrentHashMap.Node<V> find(int h, long k) {
         if (k != this.EMPTY) {
            Long2ObjectConcurrentHashMap.Node<V> e = this.first;

            while (e != null) {
               int s = this.lockState;
               if ((this.lockState & 3) != 0) {
                  if (e.hash == h) {
                     long ek = e.key;
                     if (e.key == k || ek != this.EMPTY && k == ek) {
                        return e;
                     }
                  }

                  e = e.next;
               } else if (U.compareAndSwapInt(this, LOCKSTATE, s, s + 4)) {
                  Long2ObjectConcurrentHashMap.TreeNode<V> p;
                  try {
                     Long2ObjectConcurrentHashMap.TreeNode<V> r = this.root;
                     p = this.root == null ? null : r.findTreeNode(h, k, null);
                  } finally {
                     if (U.getAndAddInt(this, LOCKSTATE, -4) == 6) {
                        Thread w = this.waiter;
                        if (this.waiter != null) {
                           LockSupport.unpark(w);
                        }
                     }
                  }

                  return p;
               }
            }
         }

         return null;
      }

      protected final Long2ObjectConcurrentHashMap.TreeNode<V> putTreeVal(int h, long k, V v) {
         Class<?> kc = null;
         boolean searched = false;
         Long2ObjectConcurrentHashMap.TreeNode<V> p = this.root;

         while (true) {
            if (p == null) {
               this.first = this.root = new Long2ObjectConcurrentHashMap.TreeNode<>(this.EMPTY, h, k, v, null, null);
            } else {
               int ph = p.hash;
               int dir;
               if (p.hash > h) {
                  dir = -1;
               } else if (ph < h) {
                  dir = 1;
               } else {
                  long pk = p.key;
                  if (p.key == k || pk != this.EMPTY && k == pk) {
                     return p;
                  }

                  if ((dir = Long.compare(k, pk)) == 0) {
                     if (!searched) {
                        searched = true;
                        Long2ObjectConcurrentHashMap.TreeNode<V> ch = p.left;
                        Long2ObjectConcurrentHashMap.TreeNode<V> q;
                        if (p.left != null && (q = ch.findTreeNode(h, k, kc)) != null) {
                           return q;
                        }

                        ch = p.right;
                        if (p.right != null && (q = ch.findTreeNode(h, k, kc)) != null) {
                           return q;
                        }
                     }

                     dir = this.tieBreakOrder(k, pk);
                  }
               }

               Long2ObjectConcurrentHashMap.TreeNode<V> xp = p;
               if ((p = dir <= 0 ? p.left : p.right) != null) {
                  continue;
               }

               Long2ObjectConcurrentHashMap.TreeNode<V> f = this.first;
               Long2ObjectConcurrentHashMap.TreeNode<V> x;
               this.first = x = new Long2ObjectConcurrentHashMap.TreeNode<>(this.EMPTY, h, k, v, f, xp);
               if (f != null) {
                  f.prev = x;
               }

               if (dir <= 0) {
                  xp.left = x;
               } else {
                  xp.right = x;
               }

               if (!xp.red) {
                  x.red = true;
               } else {
                  this.lockRoot();

                  try {
                     this.root = this.balanceInsertion(this.root, x);
                  } finally {
                     this.unlockRoot();
                  }
               }
            }

            assert this.checkInvariants(this.root);

            return null;
         }
      }

      protected final boolean removeTreeNode(Long2ObjectConcurrentHashMap.TreeNode<V> p) {
         Long2ObjectConcurrentHashMap.TreeNode<V> next = (Long2ObjectConcurrentHashMap.TreeNode<V>)p.next;
         Long2ObjectConcurrentHashMap.TreeNode<V> pred = p.prev;
         if (pred == null) {
            this.first = next;
         } else {
            pred.next = next;
         }

         if (next != null) {
            next.prev = pred;
         }

         if (this.first == null) {
            this.root = null;
            return true;
         } else {
            Long2ObjectConcurrentHashMap.TreeNode<V> r = this.root;
            if (this.root != null && r.right != null) {
               Long2ObjectConcurrentHashMap.TreeNode<V> rl = r.left;
               if (r.left != null && rl.left != null) {
                  this.lockRoot();

                  try {
                     Long2ObjectConcurrentHashMap.TreeNode<V> pl = p.left;
                     Long2ObjectConcurrentHashMap.TreeNode<V> pr = p.right;
                     Long2ObjectConcurrentHashMap.TreeNode<V> replacement;
                     if (pl != null && pr != null) {
                        Long2ObjectConcurrentHashMap.TreeNode<V> s = pr;

                        while (true) {
                           Long2ObjectConcurrentHashMap.TreeNode<V> sl = s.left;
                           if (s.left == null) {
                              boolean c = s.red;
                              s.red = p.red;
                              p.red = c;
                              Long2ObjectConcurrentHashMap.TreeNode<V> sr = s.right;
                              Long2ObjectConcurrentHashMap.TreeNode<V> pp = p.parent;
                              if (s == pr) {
                                 p.parent = s;
                                 s.right = p;
                              } else {
                                 Long2ObjectConcurrentHashMap.TreeNode<V> sp = s.parent;
                                 if ((p.parent = sp) != null) {
                                    if (s == sp.left) {
                                       sp.left = p;
                                    } else {
                                       sp.right = p;
                                    }
                                 }

                                 if ((s.right = pr) != null) {
                                    pr.parent = s;
                                 }
                              }

                              p.left = null;
                              if ((p.right = sr) != null) {
                                 sr.parent = p;
                              }

                              if ((s.left = pl) != null) {
                                 pl.parent = s;
                              }

                              if ((s.parent = pp) == null) {
                                 r = s;
                              } else if (p == pp.left) {
                                 pp.left = s;
                              } else {
                                 pp.right = s;
                              }

                              if (sr != null) {
                                 replacement = sr;
                              } else {
                                 replacement = p;
                              }
                              break;
                           }

                           s = sl;
                        }
                     } else if (pl != null) {
                        replacement = pl;
                     } else if (pr != null) {
                        replacement = pr;
                     } else {
                        replacement = p;
                     }

                     if (replacement != p) {
                        Long2ObjectConcurrentHashMap.TreeNode<V> ppx = replacement.parent = p.parent;
                        if (ppx == null) {
                           r = replacement;
                        } else if (p == ppx.left) {
                           ppx.left = replacement;
                        } else {
                           ppx.right = replacement;
                        }

                        p.left = p.right = p.parent = null;
                     }

                     this.root = p.red ? r : this.balanceDeletion(r, replacement);
                     if (p == replacement) {
                        Long2ObjectConcurrentHashMap.TreeNode<V> ppx = p.parent;
                        if (p.parent != null) {
                           if (p == ppx.left) {
                              ppx.left = null;
                           } else if (p == ppx.right) {
                              ppx.right = null;
                           }

                           p.parent = null;
                        }
                     }
                  } finally {
                     this.unlockRoot();
                  }

                  assert this.checkInvariants(this.root);

                  return false;
               }
            }

            return true;
         }
      }

      protected <V> Long2ObjectConcurrentHashMap.TreeNode<V> rotateLeft(
         Long2ObjectConcurrentHashMap.TreeNode<V> root, Long2ObjectConcurrentHashMap.TreeNode<V> p
      ) {
         if (p != null) {
            Long2ObjectConcurrentHashMap.TreeNode<V> r = p.right;
            if (p.right != null) {
               Long2ObjectConcurrentHashMap.TreeNode<V> rl;
               if ((rl = p.right = r.left) != null) {
                  rl.parent = p;
               }

               Long2ObjectConcurrentHashMap.TreeNode<V> pp;
               if ((pp = r.parent = p.parent) == null) {
                  root = r;
                  r.red = false;
               } else if (pp.left == p) {
                  pp.left = r;
               } else {
                  pp.right = r;
               }

               r.left = p;
               p.parent = r;
            }
         }

         return root;
      }

      protected <V> Long2ObjectConcurrentHashMap.TreeNode<V> rotateRight(
         Long2ObjectConcurrentHashMap.TreeNode<V> root, Long2ObjectConcurrentHashMap.TreeNode<V> p
      ) {
         if (p != null) {
            Long2ObjectConcurrentHashMap.TreeNode<V> l = p.left;
            if (p.left != null) {
               Long2ObjectConcurrentHashMap.TreeNode<V> lr;
               if ((lr = p.left = l.right) != null) {
                  lr.parent = p;
               }

               Long2ObjectConcurrentHashMap.TreeNode<V> pp;
               if ((pp = l.parent = p.parent) == null) {
                  root = l;
                  l.red = false;
               } else if (pp.right == p) {
                  pp.right = l;
               } else {
                  pp.left = l;
               }

               l.right = p;
               p.parent = l;
            }
         }

         return root;
      }

      protected <V> Long2ObjectConcurrentHashMap.TreeNode<V> balanceInsertion(
         Long2ObjectConcurrentHashMap.TreeNode<V> root, Long2ObjectConcurrentHashMap.TreeNode<V> x
      ) {
         x.red = true;

         while (true) {
            Long2ObjectConcurrentHashMap.TreeNode<V> xp = x.parent;
            if (x.parent == null) {
               x.red = false;
               return x;
            }

            if (!xp.red) {
               break;
            }

            Long2ObjectConcurrentHashMap.TreeNode<V> xpp = xp.parent;
            if (xp.parent == null) {
               break;
            }

            Long2ObjectConcurrentHashMap.TreeNode<V> xppl = xpp.left;
            if (xp == xpp.left) {
               Long2ObjectConcurrentHashMap.TreeNode<V> xppr = xpp.right;
               if (xpp.right != null && xppr.red) {
                  xppr.red = false;
                  xp.red = false;
                  xpp.red = true;
                  x = xpp;
               } else {
                  if (x == xp.right) {
                     x = xp;
                     root = this.rotateLeft(root, xp);
                     xpp = (xp = xp.parent) == null ? null : xp.parent;
                  }

                  if (xp != null) {
                     xp.red = false;
                     if (xpp != null) {
                        xpp.red = true;
                        root = this.rotateRight(root, xpp);
                     }
                  }
               }
            } else if (xppl != null && xppl.red) {
               xppl.red = false;
               xp.red = false;
               xpp.red = true;
               x = xpp;
            } else {
               if (x == xp.left) {
                  x = xp;
                  root = this.rotateRight(root, xp);
                  xpp = (xp = xp.parent) == null ? null : xp.parent;
               }

               if (xp != null) {
                  xp.red = false;
                  if (xpp != null) {
                     xpp.red = true;
                     root = this.rotateLeft(root, xpp);
                  }
               }
            }
         }

         return root;
      }

      protected <V> Long2ObjectConcurrentHashMap.TreeNode<V> balanceDeletion(
         Long2ObjectConcurrentHashMap.TreeNode<V> root, Long2ObjectConcurrentHashMap.TreeNode<V> x
      ) {
         while (x != null && x != root) {
            Long2ObjectConcurrentHashMap.TreeNode<V> xp = x.parent;
            if (x.parent == null) {
               x.red = false;
               return x;
            }

            if (x.red) {
               x.red = false;
               return root;
            }

            Long2ObjectConcurrentHashMap.TreeNode<V> xpl = xp.left;
            if (xp.left == x) {
               Long2ObjectConcurrentHashMap.TreeNode<V> xpr = xp.right;
               if (xp.right != null && xpr.red) {
                  xpr.red = false;
                  xp.red = true;
                  root = this.rotateLeft(root, xp);
                  xp = x.parent;
                  xpr = x.parent == null ? null : xp.right;
               }

               if (xpr == null) {
                  x = xp;
               } else {
                  Long2ObjectConcurrentHashMap.TreeNode<V> sl = xpr.left;
                  Long2ObjectConcurrentHashMap.TreeNode<V> sr = xpr.right;
                  if (sr != null && sr.red || sl != null && sl.red) {
                     if (sr == null || !sr.red) {
                        if (sl != null) {
                           sl.red = false;
                        }

                        xpr.red = true;
                        root = this.rotateRight(root, xpr);
                        xp = x.parent;
                        xpr = x.parent == null ? null : xp.right;
                     }

                     if (xpr != null) {
                        xpr.red = xp == null ? false : xp.red;
                        sr = xpr.right;
                        if (xpr.right != null) {
                           sr.red = false;
                        }
                     }

                     if (xp != null) {
                        xp.red = false;
                        root = this.rotateLeft(root, xp);
                     }

                     x = root;
                  } else {
                     xpr.red = true;
                     x = xp;
                  }
               }
            } else {
               if (xpl != null && xpl.red) {
                  xpl.red = false;
                  xp.red = true;
                  root = this.rotateRight(root, xp);
                  xp = x.parent;
                  xpl = x.parent == null ? null : xp.left;
               }

               if (xpl == null) {
                  x = xp;
               } else {
                  Long2ObjectConcurrentHashMap.TreeNode<V> sl = xpl.left;
                  Long2ObjectConcurrentHashMap.TreeNode<V> sr = xpl.right;
                  if (sl != null && sl.red || sr != null && sr.red) {
                     if (sl == null || !sl.red) {
                        if (sr != null) {
                           sr.red = false;
                        }

                        xpl.red = true;
                        root = this.rotateLeft(root, xpl);
                        xp = x.parent;
                        xpl = x.parent == null ? null : xp.left;
                     }

                     if (xpl != null) {
                        xpl.red = xp == null ? false : xp.red;
                        sl = xpl.left;
                        if (xpl.left != null) {
                           sl.red = false;
                        }
                     }

                     if (xp != null) {
                        xp.red = false;
                        root = this.rotateRight(root, xp);
                     }

                     x = root;
                  } else {
                     xpl.red = true;
                     x = xp;
                  }
               }
            }
         }

         return root;
      }

      protected <V> boolean checkInvariants(Long2ObjectConcurrentHashMap.TreeNode<V> t) {
         Long2ObjectConcurrentHashMap.TreeNode<V> tp = t.parent;
         Long2ObjectConcurrentHashMap.TreeNode<V> tl = t.left;
         Long2ObjectConcurrentHashMap.TreeNode<V> tr = t.right;
         Long2ObjectConcurrentHashMap.TreeNode<V> tb = t.prev;
         Long2ObjectConcurrentHashMap.TreeNode<V> tn = (Long2ObjectConcurrentHashMap.TreeNode<V>)t.next;
         if (tb != null && tb.next != t) {
            return false;
         } else if (tn != null && tn.prev != t) {
            return false;
         } else if (tp != null && t != tp.left && t != tp.right) {
            return false;
         } else if (tl == null || tl.parent == t && tl.hash <= t.hash) {
            if (tr == null || tr.parent == t && tr.hash >= t.hash) {
               if (t.red && tl != null && tl.red && tr != null && tr.red) {
                  return false;
               } else {
                  return tl != null && !this.checkInvariants(tl) ? false : tr == null || this.checkInvariants(tr);
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      static {
         try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            U = (Unsafe)f.get(null);
            Class<?> k = Long2ObjectConcurrentHashMap.TreeBin.class;
            LOCKSTATE = U.objectFieldOffset(k.getDeclaredField("lockState"));
         } catch (Exception var2) {
            throw new Error(var2);
         }
      }
   }

   protected static final class TreeNode<V> extends Long2ObjectConcurrentHashMap.Node<V> {
      public Long2ObjectConcurrentHashMap.TreeNode<V> parent;
      public Long2ObjectConcurrentHashMap.TreeNode<V> left;
      public Long2ObjectConcurrentHashMap.TreeNode<V> right;
      public Long2ObjectConcurrentHashMap.TreeNode<V> prev;
      public boolean red;

      public TreeNode(long empty, int hash, long key, V val, Long2ObjectConcurrentHashMap.Node<V> next, Long2ObjectConcurrentHashMap.TreeNode<V> parent) {
         super(empty, hash, key, val, next);
         this.parent = parent;
      }

      @Override
      protected Long2ObjectConcurrentHashMap.Node<V> find(int h, long k) {
         return this.findTreeNode(h, k, null);
      }

      protected final Long2ObjectConcurrentHashMap.TreeNode<V> findTreeNode(int h, long k, Class<?> kc) {
         if (k != this.EMPTY) {
            Long2ObjectConcurrentHashMap.TreeNode<V> p = this;

            do {
               Long2ObjectConcurrentHashMap.TreeNode<V> pl = p.left;
               Long2ObjectConcurrentHashMap.TreeNode<V> pr = p.right;
               int ph = p.hash;
               if (p.hash > h) {
                  p = pl;
               } else if (ph < h) {
                  p = pr;
               } else {
                  long pk = p.key;
                  if (p.key == k || pk != this.EMPTY && k == pk) {
                     return p;
                  }

                  if (pl == null) {
                     p = pr;
                  } else if (pr == null) {
                     p = pl;
                  } else {
                     int dir;
                     if ((dir = Long.compare(k, pk)) != 0) {
                        p = dir < 0 ? pl : pr;
                     } else {
                        Long2ObjectConcurrentHashMap.TreeNode<V> q;
                        if ((q = pr.findTreeNode(h, k, kc)) != null) {
                           return q;
                        }

                        p = pl;
                     }
                  }
               }
            } while (p != null);
         }

         return null;
      }
   }

   protected static final class ValueIterator<V> extends Long2ObjectConcurrentHashMap.BaseIterator<V> implements ObjectIterator<V>, Enumeration<V> {
      public ValueIterator(Long2ObjectConcurrentHashMap.Node<V>[] tab, int index, int size, int limit, Long2ObjectConcurrentHashMap<V> map) {
         super(tab, index, size, limit, map);
      }

      public final V next() {
         Long2ObjectConcurrentHashMap.Node<V> p = this.next;
         if (this.next == null) {
            throw new NoSuchElementException();
         } else {
            V v = p.val;
            this.lastReturned = p;
            this.advance();
            return v;
         }
      }

      @Override
      public final V nextElement() {
         return this.next();
      }
   }

   protected static final class ValueSpliterator<V> extends Long2ObjectConcurrentHashMap.Traverser<V> implements ObjectSpliterator<V> {
      public long est;

      public ValueSpliterator(Long2ObjectConcurrentHashMap.Node<V>[] tab, int size, int index, int limit, long est) {
         super(tab, size, index, limit);
         this.est = est;
      }

      public ObjectSpliterator<V> trySplit() {
         int i = this.baseIndex;
         int f = this.baseLimit;
         int h;
         return (h = this.baseIndex + this.baseLimit >>> 1) <= i
            ? null
            : new Long2ObjectConcurrentHashMap.ValueSpliterator<>(this.tab, this.baseSize, this.baseLimit = h, f, this.est >>>= 1);
      }

      public void forEachRemaining(Consumer<? super V> action) {
         if (action == null) {
            throw new NullPointerException();
         } else {
            Long2ObjectConcurrentHashMap.Node<V> p;
            while ((p = this.advance()) != null) {
               action.accept(p.val);
            }
         }
      }

      public boolean tryAdvance(Consumer<? super V> action) {
         if (action == null) {
            throw new NullPointerException();
         } else {
            Long2ObjectConcurrentHashMap.Node<V> p;
            if ((p = this.advance()) == null) {
               return false;
            } else {
               action.accept(p.val);
               return true;
            }
         }
      }

      public long estimateSize() {
         return this.est;
      }

      public int characteristics() {
         return 4352;
      }
   }

   protected static final class ValuesView<V> extends Long2ObjectConcurrentHashMap.CollectionView<V, V> implements FastCollection<V>, Serializable {
      public static final long serialVersionUID = 2249069246763182397L;

      public ValuesView(Long2ObjectConcurrentHashMap<V> map) {
         super(map);
      }

      @Override
      public final boolean contains(Object o) {
         return this.map.containsValue(o);
      }

      @Override
      public final boolean remove(Object o) {
         if (o != null) {
            Iterator<V> it = this.iterator();

            while (it.hasNext()) {
               if (o.equals(it.next())) {
                  it.remove();
                  return true;
               }
            }
         }

         return false;
      }

      @Override
      public final ObjectIterator<V> iterator() {
         Long2ObjectConcurrentHashMap<V> m = this.map;
         Long2ObjectConcurrentHashMap.Node<V>[] t = m.table;
         int f = m.table == null ? 0 : t.length;
         return new Long2ObjectConcurrentHashMap.ValueIterator<>(t, f, 0, f, m);
      }

      @Override
      public final boolean add(V e) {
         throw new UnsupportedOperationException();
      }

      @Override
      public final boolean addAll(Collection<? extends V> c) {
         throw new UnsupportedOperationException();
      }

      public ObjectSpliterator<V> spliterator() {
         Long2ObjectConcurrentHashMap<V> m = this.map;
         long n = m.sumCount();
         Long2ObjectConcurrentHashMap.Node<V>[] t = m.table;
         int f = m.table == null ? 0 : t.length;
         return new Long2ObjectConcurrentHashMap.ValueSpliterator<>(t, f, 0, f, n < 0L ? 0L : n);
      }

      @Override
      public void forEach(Consumer<? super V> action) {
         if (action == null) {
            throw new NullPointerException();
         } else {
            Long2ObjectConcurrentHashMap.Node<V>[] tt = this.map.table;
            if (this.map.table != null) {
               Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
               Long2ObjectConcurrentHashMap.Node<V> next = null;
               Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
               Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
               int index = 0;
               int baseIndex = 0;
               int baseLimit = tt.length;
               int baseSize = tt.length;

               while (true) {
                  Long2ObjectConcurrentHashMap.Node<V> p = null;
                  p = next;
                  if (next != null) {
                     p = next.next;
                  }

                  label78: {
                     while (true) {
                        if (p != null) {
                           next = p;
                           break label78;
                        }

                        if (baseIndex >= baseLimit) {
                           break;
                        }

                        Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                        int n;
                        if (tab == null || (n = tab.length) <= index || index < 0) {
                           break;
                        }

                        if ((p = Long2ObjectConcurrentHashMap.tabAt(tab, index)) != null && p.hash < 0) {
                           if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                              tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                              p = null;
                              Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                              if (spare != null) {
                                 spare = spare.next;
                              } else {
                                 s = new Long2ObjectConcurrentHashMap.TableStack<>();
                              }

                              s.tab = t;
                              s.length = n;
                              s.index = index;
                              s.next = stack;
                              stack = s;
                              continue;
                           }

                           if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                              p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                           } else {
                              p = null;
                           }
                        }

                        if (stack == null) {
                           if ((index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                        } else {
                           while (true) {
                              Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                              if (stack != null) {
                                 int len = stack.length;
                                 if ((index += stack.length) >= n) {
                                    n = len;
                                    index = stack.index;
                                    tab = stack.tab;
                                    stack.tab = null;
                                    Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                    stack.next = spare;
                                    stack = anext;
                                    spare = s;
                                    continue;
                                 }
                              }

                              if (stack == null && (index += baseSize) >= n) {
                                 index = ++baseIndex;
                              }
                              break;
                           }
                        }
                     }

                     next = null;
                  }

                  if (p == null) {
                     break;
                  }

                  action.accept(p.val);
               }
            }
         }
      }

      @Override
      public <A, B, C, D> void forEach(
         FastCollection.FastConsumerD9<? super V, A, B, C, D> consumer,
         A a,
         double d1,
         double d2,
         double d3,
         double d4,
         double d5,
         double d6,
         double d7,
         double d8,
         double d9,
         B b,
         C c,
         D d
      ) {
         if (consumer == null) {
            throw new NullPointerException();
         } else {
            Long2ObjectConcurrentHashMap.Node<V>[] tt = this.map.table;
            if (this.map.table != null) {
               Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
               Long2ObjectConcurrentHashMap.Node<V> next = null;
               Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
               Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
               int index = 0;
               int baseIndex = 0;
               int baseLimit = tt.length;
               int baseSize = tt.length;

               while (true) {
                  Long2ObjectConcurrentHashMap.Node<V> p = null;
                  p = next;
                  if (next != null) {
                     p = next.next;
                  }

                  label78: {
                     while (true) {
                        if (p != null) {
                           next = p;
                           break label78;
                        }

                        if (baseIndex >= baseLimit) {
                           break;
                        }

                        Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                        int n;
                        if (tab == null || (n = tab.length) <= index || index < 0) {
                           break;
                        }

                        if ((p = Long2ObjectConcurrentHashMap.tabAt(tab, index)) != null && p.hash < 0) {
                           if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                              tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                              p = null;
                              Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                              if (spare != null) {
                                 spare = spare.next;
                              } else {
                                 s = new Long2ObjectConcurrentHashMap.TableStack<>();
                              }

                              s.tab = t;
                              s.length = n;
                              s.index = index;
                              s.next = stack;
                              stack = s;
                              continue;
                           }

                           if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                              p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                           } else {
                              p = null;
                           }
                        }

                        if (stack == null) {
                           if ((index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                        } else {
                           while (true) {
                              Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                              if (stack != null) {
                                 int len = stack.length;
                                 if ((index += stack.length) >= n) {
                                    n = len;
                                    index = stack.index;
                                    tab = stack.tab;
                                    stack.tab = null;
                                    Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                    stack.next = spare;
                                    stack = anext;
                                    spare = s;
                                    continue;
                                 }
                              }

                              if (stack == null && (index += baseSize) >= n) {
                                 index = ++baseIndex;
                              }
                              break;
                           }
                        }
                     }

                     next = null;
                  }

                  if (p == null) {
                     break;
                  }

                  consumer.accept(p.val, a, d1, d2, d3, d4, d5, d6, d7, d8, d9, b, c, d);
               }
            }
         }
      }

      @Override
      public <A, B, C, D> void forEach(
         FastCollection.FastConsumerD6<? super V, A, B, C, D> consumer, A a, double d1, double d2, double d3, double d4, double d5, double d6, B b, C c, D d
      ) {
         if (consumer == null) {
            throw new NullPointerException();
         } else {
            Long2ObjectConcurrentHashMap.Node<V>[] tt = this.map.table;
            if (this.map.table != null) {
               Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
               Long2ObjectConcurrentHashMap.Node<V> next = null;
               Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
               Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
               int index = 0;
               int baseIndex = 0;
               int baseLimit = tt.length;
               int baseSize = tt.length;

               while (true) {
                  Long2ObjectConcurrentHashMap.Node<V> p = null;
                  p = next;
                  if (next != null) {
                     p = next.next;
                  }

                  label78: {
                     while (true) {
                        if (p != null) {
                           next = p;
                           break label78;
                        }

                        if (baseIndex >= baseLimit) {
                           break;
                        }

                        Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                        int n;
                        if (tab == null || (n = tab.length) <= index || index < 0) {
                           break;
                        }

                        if ((p = Long2ObjectConcurrentHashMap.tabAt(tab, index)) != null && p.hash < 0) {
                           if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                              tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                              p = null;
                              Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                              if (spare != null) {
                                 spare = spare.next;
                              } else {
                                 s = new Long2ObjectConcurrentHashMap.TableStack<>();
                              }

                              s.tab = t;
                              s.length = n;
                              s.index = index;
                              s.next = stack;
                              stack = s;
                              continue;
                           }

                           if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                              p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                           } else {
                              p = null;
                           }
                        }

                        if (stack == null) {
                           if ((index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                        } else {
                           while (true) {
                              Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                              if (stack != null) {
                                 int len = stack.length;
                                 if ((index += stack.length) >= n) {
                                    n = len;
                                    index = stack.index;
                                    tab = stack.tab;
                                    stack.tab = null;
                                    Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                    stack.next = spare;
                                    stack = anext;
                                    spare = s;
                                    continue;
                                 }
                              }

                              if (stack == null && (index += baseSize) >= n) {
                                 index = ++baseIndex;
                              }
                              break;
                           }
                        }
                     }

                     next = null;
                  }

                  if (p == null) {
                     break;
                  }

                  consumer.accept(p.val, a, d1, d2, d3, d4, d5, d6, b, c, d);
               }
            }
         }
      }

      @Override
      public void forEachWithFloat(FastCollection.FastConsumerF<? super V> consumer, float ii) {
         if (consumer == null) {
            throw new NullPointerException();
         } else {
            Long2ObjectConcurrentHashMap.Node<V>[] tt = this.map.table;
            if (this.map.table != null) {
               Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
               Long2ObjectConcurrentHashMap.Node<V> next = null;
               Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
               Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
               int index = 0;
               int baseIndex = 0;
               int baseLimit = tt.length;
               int baseSize = tt.length;

               while (true) {
                  Long2ObjectConcurrentHashMap.Node<V> p = null;
                  p = next;
                  if (next != null) {
                     p = next.next;
                  }

                  label78: {
                     while (true) {
                        if (p != null) {
                           next = p;
                           break label78;
                        }

                        if (baseIndex >= baseLimit) {
                           break;
                        }

                        Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                        int n;
                        if (tab == null || (n = tab.length) <= index || index < 0) {
                           break;
                        }

                        if ((p = Long2ObjectConcurrentHashMap.tabAt(tab, index)) != null && p.hash < 0) {
                           if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                              tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                              p = null;
                              Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                              if (spare != null) {
                                 spare = spare.next;
                              } else {
                                 s = new Long2ObjectConcurrentHashMap.TableStack<>();
                              }

                              s.tab = t;
                              s.length = n;
                              s.index = index;
                              s.next = stack;
                              stack = s;
                              continue;
                           }

                           if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                              p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                           } else {
                              p = null;
                           }
                        }

                        if (stack == null) {
                           if ((index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                        } else {
                           while (true) {
                              Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                              if (stack != null) {
                                 int len = stack.length;
                                 if ((index += stack.length) >= n) {
                                    n = len;
                                    index = stack.index;
                                    tab = stack.tab;
                                    stack.tab = null;
                                    Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                    stack.next = spare;
                                    stack = anext;
                                    spare = s;
                                    continue;
                                 }
                              }

                              if (stack == null && (index += baseSize) >= n) {
                                 index = ++baseIndex;
                              }
                              break;
                           }
                        }
                     }

                     next = null;
                  }

                  if (p == null) {
                     break;
                  }

                  consumer.accept(p.val, ii);
               }
            }
         }
      }

      @Override
      public void forEachWithInt(FastCollection.FastConsumerI<? super V> consumer, int ii) {
         if (consumer == null) {
            throw new NullPointerException();
         } else {
            Long2ObjectConcurrentHashMap.Node<V>[] tt = this.map.table;
            if (this.map.table != null) {
               Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
               Long2ObjectConcurrentHashMap.Node<V> next = null;
               Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
               Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
               int index = 0;
               int baseIndex = 0;
               int baseLimit = tt.length;
               int baseSize = tt.length;

               while (true) {
                  Long2ObjectConcurrentHashMap.Node<V> p = null;
                  p = next;
                  if (next != null) {
                     p = next.next;
                  }

                  label78: {
                     while (true) {
                        if (p != null) {
                           next = p;
                           break label78;
                        }

                        if (baseIndex >= baseLimit) {
                           break;
                        }

                        Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                        int n;
                        if (tab == null || (n = tab.length) <= index || index < 0) {
                           break;
                        }

                        if ((p = Long2ObjectConcurrentHashMap.tabAt(tab, index)) != null && p.hash < 0) {
                           if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                              tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                              p = null;
                              Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                              if (spare != null) {
                                 spare = spare.next;
                              } else {
                                 s = new Long2ObjectConcurrentHashMap.TableStack<>();
                              }

                              s.tab = t;
                              s.length = n;
                              s.index = index;
                              s.next = stack;
                              stack = s;
                              continue;
                           }

                           if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                              p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                           } else {
                              p = null;
                           }
                        }

                        if (stack == null) {
                           if ((index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                        } else {
                           while (true) {
                              Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                              if (stack != null) {
                                 int len = stack.length;
                                 if ((index += stack.length) >= n) {
                                    n = len;
                                    index = stack.index;
                                    tab = stack.tab;
                                    stack.tab = null;
                                    Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                    stack.next = spare;
                                    stack = anext;
                                    spare = s;
                                    continue;
                                 }
                              }

                              if (stack == null && (index += baseSize) >= n) {
                                 index = ++baseIndex;
                              }
                              break;
                           }
                        }
                     }

                     next = null;
                  }

                  if (p == null) {
                     break;
                  }

                  consumer.accept(p.val, ii);
               }
            }
         }
      }

      @Override
      public void forEachWithLong(FastCollection.FastConsumerL<? super V> consumer, long ii) {
         if (consumer == null) {
            throw new NullPointerException();
         } else {
            Long2ObjectConcurrentHashMap.Node<V>[] tt = this.map.table;
            if (this.map.table != null) {
               Long2ObjectConcurrentHashMap.Node<V>[] tab = tt;
               Long2ObjectConcurrentHashMap.Node<V> next = null;
               Long2ObjectConcurrentHashMap.TableStack<V> stack = null;
               Long2ObjectConcurrentHashMap.TableStack<V> spare = null;
               int index = 0;
               int baseIndex = 0;
               int baseLimit = tt.length;
               int baseSize = tt.length;

               while (true) {
                  Long2ObjectConcurrentHashMap.Node<V> p = null;
                  p = next;
                  if (next != null) {
                     p = next.next;
                  }

                  label78: {
                     while (true) {
                        if (p != null) {
                           next = p;
                           break label78;
                        }

                        if (baseIndex >= baseLimit) {
                           break;
                        }

                        Long2ObjectConcurrentHashMap.Node<V>[] t = tab;
                        int n;
                        if (tab == null || (n = tab.length) <= index || index < 0) {
                           break;
                        }

                        if ((p = Long2ObjectConcurrentHashMap.tabAt(tab, index)) != null && p.hash < 0) {
                           if (p instanceof Long2ObjectConcurrentHashMap.ForwardingNode) {
                              tab = ((Long2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                              p = null;
                              Long2ObjectConcurrentHashMap.TableStack<V> s = spare;
                              if (spare != null) {
                                 spare = spare.next;
                              } else {
                                 s = new Long2ObjectConcurrentHashMap.TableStack<>();
                              }

                              s.tab = t;
                              s.length = n;
                              s.index = index;
                              s.next = stack;
                              stack = s;
                              continue;
                           }

                           if (p instanceof Long2ObjectConcurrentHashMap.TreeBin) {
                              p = ((Long2ObjectConcurrentHashMap.TreeBin)p).first;
                           } else {
                              p = null;
                           }
                        }

                        if (stack == null) {
                           if ((index += baseSize) >= n) {
                              index = ++baseIndex;
                           }
                        } else {
                           while (true) {
                              Long2ObjectConcurrentHashMap.TableStack<V> s = stack;
                              if (stack != null) {
                                 int len = stack.length;
                                 if ((index += stack.length) >= n) {
                                    n = len;
                                    index = stack.index;
                                    tab = stack.tab;
                                    stack.tab = null;
                                    Long2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
                                    stack.next = spare;
                                    stack = anext;
                                    spare = s;
                                    continue;
                                 }
                              }

                              if (stack == null && (index += baseSize) >= n) {
                                 index = ++baseIndex;
                              }
                              break;
                           }
                        }
                     }

                     next = null;
                  }

                  if (p == null) {
                     break;
                  }

                  consumer.accept(p.val, ii);
               }
            }
         }
      }
   }
}
