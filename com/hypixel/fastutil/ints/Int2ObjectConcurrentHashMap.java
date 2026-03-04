package com.hypixel.fastutil.ints;

import com.hypixel.fastutil.FastCollection;
import com.hypixel.fastutil.util.SneakyThrow;
import com.hypixel.fastutil.util.TLRUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSpliterator;
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
import java.util.function.IntConsumer;
import java.util.function.LongBinaryOperator;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;
import sun.misc.Unsafe;

public class Int2ObjectConcurrentHashMap<V> {
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
   protected transient volatile Int2ObjectConcurrentHashMap.Node<V>[] table;
   protected transient volatile Int2ObjectConcurrentHashMap.Node<V>[] nextTable;
   protected transient volatile long baseCount;
   protected transient volatile int sizeCtl;
   protected transient volatile int transferIndex;
   protected transient volatile int cellsBusy;
   protected transient volatile Int2ObjectConcurrentHashMap.CounterCell[] counterCells;
   protected transient Int2ObjectConcurrentHashMap.KeySetView<V> keySet;
   protected transient Int2ObjectConcurrentHashMap.ValuesView<V> values;
   protected transient Int2ObjectConcurrentHashMap.EntrySetView<V> entrySet;
   protected final int EMPTY;
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

   protected static final <V> Int2ObjectConcurrentHashMap.Node<V> tabAt(Int2ObjectConcurrentHashMap.Node<V>[] tab, int i) {
      return (Int2ObjectConcurrentHashMap.Node<V>)U.getObjectVolatile(tab, ((long)i << ASHIFT) + ABASE);
   }

   protected static final <V> boolean casTabAt(
      Int2ObjectConcurrentHashMap.Node<V>[] tab, int i, Int2ObjectConcurrentHashMap.Node<V> c, Int2ObjectConcurrentHashMap.Node<V> v
   ) {
      return U.compareAndSwapObject(tab, ((long)i << ASHIFT) + ABASE, c, v);
   }

   protected static final <V> void setTabAt(Int2ObjectConcurrentHashMap.Node<V>[] tab, int i, Int2ObjectConcurrentHashMap.Node<V> v) {
      U.putObjectVolatile(tab, ((long)i << ASHIFT) + ABASE, v);
   }

   public Int2ObjectConcurrentHashMap() {
      this.EMPTY = -1;
   }

   public Int2ObjectConcurrentHashMap(boolean nonce, int emptyValue) {
      this.EMPTY = emptyValue;
   }

   public Int2ObjectConcurrentHashMap(int initialCapacity) {
      this(initialCapacity, true, -1);
   }

   public Int2ObjectConcurrentHashMap(int initialCapacity, boolean nonce, int emptyValue) {
      if (initialCapacity < 0) {
         throw new IllegalArgumentException();
      } else {
         int cap = initialCapacity >= 536870912 ? 1073741824 : tableSizeFor(initialCapacity + (initialCapacity >>> 1) + 1);
         this.sizeCtl = cap;
         this.EMPTY = emptyValue;
      }
   }

   public Int2ObjectConcurrentHashMap(Map<? extends Integer, ? extends V> m, int emptyValue) {
      this.sizeCtl = 16;
      this.EMPTY = emptyValue;
      this.putAll(m);
   }

   public Int2ObjectConcurrentHashMap(Int2ObjectConcurrentHashMap<? extends V> m) {
      this.sizeCtl = 16;
      this.EMPTY = m.EMPTY;
      this.putAll(m);
   }

   public Int2ObjectConcurrentHashMap(Int2ObjectMap<V> m) {
      this.sizeCtl = 16;
      this.EMPTY = -1;
      this.putAll(m);
   }

   public Int2ObjectConcurrentHashMap(Int2ObjectMap<V> m, int emptyValue) {
      this.sizeCtl = 16;
      this.EMPTY = emptyValue;
      this.putAll(m);
   }

   public Int2ObjectConcurrentHashMap(int initialCapacity, float loadFactor) {
      this(initialCapacity, loadFactor, 1, -1);
   }

   public Int2ObjectConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel, int emptyValue) {
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

   public V get(int key) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else {
         int h = spread(Integer.hashCode(key));
         Int2ObjectConcurrentHashMap.Node<V>[] tab = this.table;
         Int2ObjectConcurrentHashMap.Node<V> e;
         int n;
         if (this.table != null && (n = tab.length) > 0 && (e = tabAt(tab, n - 1 & h)) != null) {
            int eh = e.hash;
            if (e.hash == h) {
               int ek = e.key;
               if (e.key == key || ek != this.EMPTY && key == ek) {
                  return e.val;
               }
            } else if (eh < 0) {
               Int2ObjectConcurrentHashMap.Node<V> p;
               return (p = e.find(h, key)) != null ? p.val : null;
            }

            while ((e = e.next) != null) {
               if (e.hash == h) {
                  int ek = e.key;
                  if (e.key == key || ek != this.EMPTY && key == ek) {
                     return e.val;
                  }
               }
            }
         }

         return null;
      }
   }

   public boolean containsKey(int key) {
      return this.get(key) != null;
   }

   public boolean containsValue(Object value) {
      if (value == null) {
         throw new NullPointerException();
      } else {
         Int2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Int2ObjectConcurrentHashMap.Node<V> next = null;
            Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Int2ObjectConcurrentHashMap.Node<V> p = null;
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
                     Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab != null && (n = tab.length) > index && index >= 0) {
                        if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                           if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                              tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                              p = null;
                              Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                              if (spare != null) {
                                 spare = spare.next;
                              } else {
                                 s = new Int2ObjectConcurrentHashMap.TableStack<>();
                              }

                              s.tab = t;
                              s.length = n;
                              s.index = index;
                              s.next = stack;
                              stack = s;
                              continue;
                           }

                           if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                              p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                           Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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

   public V put(int key, V value) {
      return this.putVal(key, value, false);
   }

   protected final V putVal(int key, V value, boolean onlyIfAbsent) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else if (value == null) {
         throw new NullPointerException();
      } else {
         int hash = spread(Integer.hashCode(key));
         int binCount = 0;
         Int2ObjectConcurrentHashMap.Node<V>[] tab = this.table;

         while (true) {
            int n;
            while (tab == null || (n = tab.length) == 0) {
               tab = this.initTable();
            }

            Int2ObjectConcurrentHashMap.Node<V> f;
            int i;
            if ((f = tabAt(tab, i = n - 1 & hash)) == null) {
               if (casTabAt(tab, i, null, new Int2ObjectConcurrentHashMap.Node<>(this.EMPTY, hash, key, value, null))) {
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
                           if (f instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                              binCount = 2;
                              Int2ObjectConcurrentHashMap.Node<V> p;
                              if ((p = ((Int2ObjectConcurrentHashMap.TreeBin)f).putTreeVal(hash, key, value)) != null) {
                                 oldVal = p.val;
                                 if (!onlyIfAbsent) {
                                    p.val = value;
                                 }
                              }
                           }
                        } else {
                           binCount = 1;
                           Int2ObjectConcurrentHashMap.Node<V> e = f;

                           while (true) {
                              if (e.hash == hash) {
                                 int ek = e.key;
                                 if (e.key == key || ek != this.EMPTY && key == ek) {
                                    oldVal = e.val;
                                    if (!onlyIfAbsent) {
                                       e.val = value;
                                    }
                                    break;
                                 }
                              }

                              Int2ObjectConcurrentHashMap.Node<V> pred = e;
                              if ((e = e.next) == null) {
                                 pred.next = new Int2ObjectConcurrentHashMap.Node<>(this.EMPTY, hash, key, value, null);
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

   public void putAll(Map<? extends Integer, ? extends V> m) {
      this.tryPresize(m.size());

      for (Map.Entry<? extends Integer, ? extends V> e : m.entrySet()) {
         this.putVal(e.getKey(), (V)e.getValue(), false);
      }
   }

   public void putAll(Int2ObjectConcurrentHashMap<? extends V> m) {
      this.tryPresize(m.size());
      ObjectIterator var2 = m.int2ObjectEntrySet().iterator();

      while (var2.hasNext()) {
         it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<? extends V> e = (it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<? extends V>)var2.next();
         this.putVal(e.getIntKey(), (V)e.getValue(), false);
      }
   }

   public void putAll(Int2ObjectMap<V> m) {
      this.tryPresize(m.size());
      ObjectIterator<? extends it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<? extends V>> iterator = m.int2ObjectEntrySet().iterator();

      while (iterator.hasNext()) {
         it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<? extends V> next = (it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<? extends V>)iterator.next();
         this.putVal(next.getIntKey(), (V)next.getValue(), false);
      }
   }

   public V remove(int key) {
      return this.replaceNode(key, null, null);
   }

   @Deprecated
   public V remove(Integer key) {
      return this.replaceNode(key, null, null);
   }

   @Deprecated
   public V remove(Object key) {
      return this.remove((Integer)key);
   }

   protected final V replaceNode(int key, V value, Object cv) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else {
         int hash = spread(Integer.hashCode(key));
         Int2ObjectConcurrentHashMap.Node<V>[] tab = this.table;

         Int2ObjectConcurrentHashMap.Node<V> f;
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
                        if (f instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                           validated = true;
                           Int2ObjectConcurrentHashMap.TreeBin<V> t = (Int2ObjectConcurrentHashMap.TreeBin<V>)f;
                           Int2ObjectConcurrentHashMap.TreeNode<V> r = t.root;
                           Int2ObjectConcurrentHashMap.TreeNode<V> p;
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
                        Int2ObjectConcurrentHashMap.Node<V> e = f;
                        Int2ObjectConcurrentHashMap.Node<V> pred = null;

                        do {
                           if (e.hash == hash) {
                              int ek = e.key;
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
      Int2ObjectConcurrentHashMap.Node<V>[] tab = this.table;

      while (tab != null && i < tab.length) {
         Int2ObjectConcurrentHashMap.Node<V> f = tabAt(tab, i);
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
                     for (Int2ObjectConcurrentHashMap.Node<V> p = (Int2ObjectConcurrentHashMap.Node<V>)(fh >= 0
                           ? f
                           : (f instanceof Int2ObjectConcurrentHashMap.TreeBin ? ((Int2ObjectConcurrentHashMap.TreeBin)f).first : null));
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

   public Int2ObjectConcurrentHashMap.KeySetView<V> keySet() {
      Int2ObjectConcurrentHashMap.KeySetView<V> ks = this.keySet;
      return this.keySet != null ? ks : (this.keySet = this.buildKeySetView());
   }

   protected Int2ObjectConcurrentHashMap.KeySetView<V> buildKeySetView() {
      return new Int2ObjectConcurrentHashMap.KeySetView<>(this, null);
   }

   public FastCollection<V> values() {
      Int2ObjectConcurrentHashMap.ValuesView<V> vs = this.values;
      return this.values != null ? vs : (this.values = this.buildValuesView());
   }

   protected Int2ObjectConcurrentHashMap.ValuesView<V> buildValuesView() {
      return new Int2ObjectConcurrentHashMap.ValuesView<>(this);
   }

   public ObjectSet<it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<V>> int2ObjectEntrySet() {
      Int2ObjectConcurrentHashMap.EntrySetView<V> es = this.entrySet;
      return this.entrySet != null ? es : (this.entrySet = this.buildEntrySetView());
   }

   @Deprecated
   public ObjectSet<Map.Entry<Integer, V>> entrySet() {
      return this.int2ObjectEntrySet();
   }

   protected Int2ObjectConcurrentHashMap.EntrySetView<V> buildEntrySetView() {
      return new Int2ObjectConcurrentHashMap.EntrySetView<>(this);
   }

   @Override
   public int hashCode() {
      int h = 0;
      Int2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
      if (this.table != null) {
         Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
         Int2ObjectConcurrentHashMap.Node<V> next = null;
         Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
         Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
         int index = 0;
         int baseIndex = 0;
         int baseLimit = tt.length;
         int baseSize = tt.length;

         while (true) {
            Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                  Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                  int n;
                  if (tab == null || (n = tab.length) <= index || index < 0) {
                     break;
                  }

                  if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                     if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                        tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                        p = null;
                        Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                        if (spare != null) {
                           spare = spare.next;
                        } else {
                           s = new Int2ObjectConcurrentHashMap.TableStack<>();
                        }

                        s.tab = t;
                        s.length = n;
                        s.index = index;
                        s.next = stack;
                        stack = s;
                        continue;
                     }

                     if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                        p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                        Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                        if (stack != null) {
                           int len = stack.length;
                           if ((index += stack.length) >= n) {
                              n = len;
                              index = stack.index;
                              tab = stack.tab;
                              stack.tab = null;
                              Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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

            h += Integer.hashCode(p.key) ^ p.val.hashCode();
         }
      }

      return h;
   }

   @Override
   public String toString() {
      Int2ObjectConcurrentHashMap.Node<V>[] t = this.table;
      int f = this.table == null ? 0 : t.length;
      Int2ObjectConcurrentHashMap.Traverser<V> it = new Int2ObjectConcurrentHashMap.Traverser<>(t, f, 0, f);
      StringBuilder sb = new StringBuilder();
      sb.append('{');
      Int2ObjectConcurrentHashMap.Node<V> p;
      if ((p = it.advance()) != null) {
         while (true) {
            int k = p.key;
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
         if (!(o instanceof Int2ObjectConcurrentHashMap<?> m)) {
            return false;
         }

         Int2ObjectConcurrentHashMap.Node<V>[] t = this.table;
         int f = this.table == null ? 0 : t.length;
         Int2ObjectConcurrentHashMap.Traverser<V> it = new Int2ObjectConcurrentHashMap.Traverser<>(t, f, 0, f);

         Int2ObjectConcurrentHashMap.Node<V> p;
         while ((p = it.advance()) != null) {
            V val = p.val;
            Object v = m.get(p.key);
            if (v == null || v != val && !v.equals(val)) {
               return false;
            }
         }

         ObjectIterator var11 = m.int2ObjectEntrySet().iterator();

         while (var11.hasNext()) {
            it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<?> e = (it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<?>)var11.next();
            Object mv;
            Object v;
            int mk;
            if ((mk = e.getIntKey()) == m.EMPTY || (mv = e.getValue()) == null || (v = this.get(mk)) == null || mv != v && !mv.equals(v)) {
               return false;
            }
         }
      }

      return true;
   }

   public V putIfAbsent(int key, V value) {
      return this.putVal(key, value, true);
   }

   public boolean remove(int key, Object value) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else {
         return value != null && this.replaceNode(key, null, value) != null;
      }
   }

   public boolean replace(int key, V oldValue, V newValue) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else if (oldValue != null && newValue != null) {
         return this.replaceNode(key, newValue, oldValue) != null;
      } else {
         throw new NullPointerException();
      }
   }

   public V replace(int key, V value) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else if (value == null) {
         throw new NullPointerException();
      } else {
         return this.replaceNode(key, value, null);
      }
   }

   public V getOrDefault(int key, V defaultValue) {
      V v;
      return (v = this.get(key)) == null ? defaultValue : v;
   }

   public int forEach(Int2ObjectConcurrentHashMap.IntObjConsumer<? super V> action) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Int2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Int2ObjectConcurrentHashMap.Node<V> next = null;
            Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                     Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Int2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                           Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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

   public <X> int forEach(Int2ObjectConcurrentHashMap.IntBiObjConsumer<? super V, X> action, X x) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Int2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Int2ObjectConcurrentHashMap.Node<V> next = null;
            Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                     Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Int2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                           Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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

   public <X, Y> int forEach(Int2ObjectConcurrentHashMap.IntTriObjConsumer<? super V, X, Y> action, X x, Y y) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Int2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Int2ObjectConcurrentHashMap.Node<V> next = null;
            Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                     Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Int2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                           Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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

   public int forEachWithByte(Int2ObjectConcurrentHashMap.IntObjByteConsumer<? super V> action, byte ii) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Int2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Int2ObjectConcurrentHashMap.Node<V> next = null;
            Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                     Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Int2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                           Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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

   public int forEachWithShort(Int2ObjectConcurrentHashMap.IntObjShortConsumer<? super V> action, short ii) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Int2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Int2ObjectConcurrentHashMap.Node<V> next = null;
            Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                     Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Int2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                           Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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

   public int forEachWithInt(Int2ObjectConcurrentHashMap.IntObjIntConsumer<? super V> action, int ii) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Int2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Int2ObjectConcurrentHashMap.Node<V> next = null;
            Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                     Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Int2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                           Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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

   public int forEachWithLong(Int2ObjectConcurrentHashMap.IntObjLongConsumer<? super V> action, long ii) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Int2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Int2ObjectConcurrentHashMap.Node<V> next = null;
            Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                     Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Int2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                           Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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

   public int forEachWithFloat(Int2ObjectConcurrentHashMap.IntObjFloatConsumer<? super V> action, float ii) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Int2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Int2ObjectConcurrentHashMap.Node<V> next = null;
            Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                     Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Int2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                           Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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

   public int forEachWithDouble(Int2ObjectConcurrentHashMap.IntObjDoubleConsumer<? super V> action, double ii) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Int2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Int2ObjectConcurrentHashMap.Node<V> next = null;
            Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                     Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Int2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                           Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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

   public <X> int forEachWithByte(Int2ObjectConcurrentHashMap.IntBiObjByteConsumer<? super V, X> action, byte ii, X x) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Int2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Int2ObjectConcurrentHashMap.Node<V> next = null;
            Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                     Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Int2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                           Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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

   public <X> int forEachWithShort(Int2ObjectConcurrentHashMap.IntBiObjShortConsumer<? super V, X> action, short ii, X x) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Int2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Int2ObjectConcurrentHashMap.Node<V> next = null;
            Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                     Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Int2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                           Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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

   public <X> int forEachWithInt(Int2ObjectConcurrentHashMap.IntBiObjIntConsumer<? super V, X> action, int ii, X x) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Int2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Int2ObjectConcurrentHashMap.Node<V> next = null;
            Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                     Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Int2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                           Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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

   public <X> int forEachWithLong(Int2ObjectConcurrentHashMap.IntBiObjLongConsumer<? super V, X> action, long ii, X x) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Int2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Int2ObjectConcurrentHashMap.Node<V> next = null;
            Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                     Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Int2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                           Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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

   public <X> int forEachWithFloat(Int2ObjectConcurrentHashMap.IntBiObjFloatConsumer<? super V, X> action, float ii, X x) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Int2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Int2ObjectConcurrentHashMap.Node<V> next = null;
            Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                     Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Int2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                           Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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

   public <X> int forEachWithDouble(Int2ObjectConcurrentHashMap.IntBiObjDoubleConsumer<? super V, X> action, double ii, X x) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Int2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Int2ObjectConcurrentHashMap.Node<V> next = null;
            Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                     Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Int2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                           Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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

   public void replaceAll(Int2ObjectOperator<V> function) {
      if (function == null) {
         throw new NullPointerException();
      } else {
         Int2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Int2ObjectConcurrentHashMap.Node<V> next = null;
            Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                     Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Int2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                           Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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
               int key = p.key;

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

   public V computeIfAbsent(int key, Int2ObjectConcurrentHashMap.IntFunction<? extends V> mappingFunction) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else if (mappingFunction == null) {
         throw new NullPointerException();
      } else {
         int h = spread(Integer.hashCode(key));
         V val = null;
         int binCount = 0;
         Int2ObjectConcurrentHashMap.Node<V>[] tab = this.table;

         while (true) {
            int n;
            while (tab == null || (n = tab.length) == 0) {
               tab = this.initTable();
            }

            Int2ObjectConcurrentHashMap.Node<V> f;
            int i;
            if ((f = tabAt(tab, i = n - 1 & h)) == null) {
               Int2ObjectConcurrentHashMap.Node<V> r = new Int2ObjectConcurrentHashMap.ReservationNode<>(this.EMPTY);
               synchronized (r) {
                  if (casTabAt(tab, i, null, r)) {
                     binCount = 1;
                     Int2ObjectConcurrentHashMap.Node<V> node = null;

                     try {
                        if ((val = (V)mappingFunction.apply(key)) != null) {
                           node = new Int2ObjectConcurrentHashMap.Node<>(this.EMPTY, h, key, val, null);
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
                           if (f instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                              binCount = 2;
                              Int2ObjectConcurrentHashMap.TreeBin<V> t = (Int2ObjectConcurrentHashMap.TreeBin<V>)f;
                              Int2ObjectConcurrentHashMap.TreeNode<V> r = t.root;
                              Int2ObjectConcurrentHashMap.TreeNode<V> p;
                              if (t.root != null && (p = r.findTreeNode(h, key, null)) != null) {
                                 val = p.val;
                              } else if ((val = (V)mappingFunction.apply(key)) != null) {
                                 added = true;
                                 t.putTreeVal(h, key, val);
                              }
                           }
                        } else {
                           binCount = 1;
                           Int2ObjectConcurrentHashMap.Node<V> e = f;

                           while (true) {
                              if (e.hash == h) {
                                 int ek = e.key;
                                 if (e.key == key || ek != this.EMPTY && key == ek) {
                                    val = e.val;
                                    break;
                                 }
                              }

                              Int2ObjectConcurrentHashMap.Node<V> pred = e;
                              if ((e = e.next) == null) {
                                 if ((val = (V)mappingFunction.apply(key)) != null) {
                                    added = true;
                                    pred.next = new Int2ObjectConcurrentHashMap.Node<>(this.EMPTY, h, key, val, null);
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

   public V computeIfPresent(int key, Int2ObjectConcurrentHashMap.IntObjFunction<? super V, ? extends V> remappingFunction) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else if (remappingFunction == null) {
         throw new NullPointerException();
      } else {
         int h = spread(Integer.hashCode(key));
         V val = null;
         int delta = 0;
         int binCount = 0;
         Int2ObjectConcurrentHashMap.Node<V>[] tab = this.table;

         while (true) {
            int n;
            while (tab == null || (n = tab.length) == 0) {
               tab = this.initTable();
            }

            Int2ObjectConcurrentHashMap.Node<V> f;
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
                        if (f instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                           binCount = 2;
                           Int2ObjectConcurrentHashMap.TreeBin<V> t = (Int2ObjectConcurrentHashMap.TreeBin<V>)f;
                           Int2ObjectConcurrentHashMap.TreeNode<V> r = t.root;
                           Int2ObjectConcurrentHashMap.TreeNode<V> p;
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
                        Int2ObjectConcurrentHashMap.Node<V> e = f;
                        Int2ObjectConcurrentHashMap.Node<V> pred = null;

                        while (true) {
                           if (e.hash == h) {
                              int ek = e.key;
                              if (e.key == key || ek != this.EMPTY && key == ek) {
                                 val = (V)remappingFunction.apply(key, e.val);
                                 if (val != null) {
                                    e.val = val;
                                 } else {
                                    delta = -1;
                                    Int2ObjectConcurrentHashMap.Node<V> en = e.next;
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

   public V compute(int key, Int2ObjectConcurrentHashMap.IntObjFunction<? super V, ? extends V> remappingFunction) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else if (remappingFunction == null) {
         throw new NullPointerException();
      } else {
         int h = spread(Integer.hashCode(key));
         V val = null;
         int delta = 0;
         int binCount = 0;
         Int2ObjectConcurrentHashMap.Node<V>[] tab = this.table;

         while (true) {
            int n;
            while (tab == null || (n = tab.length) == 0) {
               tab = this.initTable();
            }

            Int2ObjectConcurrentHashMap.Node<V> f;
            int i;
            if ((f = tabAt(tab, i = n - 1 & h)) == null) {
               Int2ObjectConcurrentHashMap.Node<V> r = new Int2ObjectConcurrentHashMap.ReservationNode<>(this.EMPTY);
               synchronized (r) {
                  if (casTabAt(tab, i, null, r)) {
                     binCount = 1;
                     Int2ObjectConcurrentHashMap.Node<V> node = null;

                     try {
                        if ((val = (V)remappingFunction.apply(key, null)) != null) {
                           delta = 1;
                           node = new Int2ObjectConcurrentHashMap.Node<>(this.EMPTY, h, key, val, null);
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
                           if (f instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                              binCount = 1;
                              Int2ObjectConcurrentHashMap.TreeBin<V> t = (Int2ObjectConcurrentHashMap.TreeBin<V>)f;
                              Int2ObjectConcurrentHashMap.TreeNode<V> r = t.root;
                              Int2ObjectConcurrentHashMap.TreeNode<V> p;
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
                           Int2ObjectConcurrentHashMap.Node<V> e = f;
                           Int2ObjectConcurrentHashMap.Node<V> pred = null;

                           while (true) {
                              if (e.hash == h) {
                                 int ek = e.key;
                                 if (e.key == key || ek != this.EMPTY && key == ek) {
                                    val = (V)remappingFunction.apply(key, e.val);
                                    if (val != null) {
                                       e.val = val;
                                    } else {
                                       delta = -1;
                                       Int2ObjectConcurrentHashMap.Node<V> en = e.next;
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
                                    pred.next = new Int2ObjectConcurrentHashMap.Node<>(this.EMPTY, h, key, val, null);
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

   public V merge(int key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else if (value != null && remappingFunction != null) {
         int h = spread(Integer.hashCode(key));
         V val = null;
         int delta = 0;
         int binCount = 0;
         Int2ObjectConcurrentHashMap.Node<V>[] tab = this.table;

         while (true) {
            int n;
            while (tab == null || (n = tab.length) == 0) {
               tab = this.initTable();
            }

            Int2ObjectConcurrentHashMap.Node<V> f;
            int i;
            if ((f = tabAt(tab, i = n - 1 & h)) == null) {
               if (casTabAt(tab, i, null, new Int2ObjectConcurrentHashMap.Node<>(this.EMPTY, h, key, value, null))) {
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
                           if (f instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                              binCount = 2;
                              Int2ObjectConcurrentHashMap.TreeBin<V> t = (Int2ObjectConcurrentHashMap.TreeBin<V>)f;
                              Int2ObjectConcurrentHashMap.TreeNode<V> r = t.root;
                              Int2ObjectConcurrentHashMap.TreeNode<V> p = r == null ? null : r.findTreeNode(h, key, null);
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
                           Int2ObjectConcurrentHashMap.Node<V> e = f;
                           Int2ObjectConcurrentHashMap.Node<V> pred = null;

                           while (true) {
                              if (e.hash == h) {
                                 int ek = e.key;
                                 if (e.key == key || ek != this.EMPTY && key == ek) {
                                    val = (V)remappingFunction.apply(e.val, value);
                                    if (val != null) {
                                       e.val = val;
                                    } else {
                                       delta = -1;
                                       Int2ObjectConcurrentHashMap.Node<V> en = e.next;
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
                                 pred.next = new Int2ObjectConcurrentHashMap.Node<>(this.EMPTY, h, key, value, null);
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

   public static IntSet newKeySet() {
      return new Int2ObjectConcurrentHashMap.KeySetView<>(new Int2ObjectConcurrentHashMap<>(), Boolean.TRUE);
   }

   public static Int2ObjectConcurrentHashMap.KeySetView<Boolean> newKeySet(int initialCapacity) {
      return new Int2ObjectConcurrentHashMap.KeySetView<>(new Int2ObjectConcurrentHashMap<>(initialCapacity), Boolean.TRUE);
   }

   public Int2ObjectConcurrentHashMap.KeySetView<V> keySet(V mappedValue) {
      if (mappedValue == null) {
         throw new NullPointerException();
      } else {
         return new Int2ObjectConcurrentHashMap.KeySetView<>(this, mappedValue);
      }
   }

   protected static final int resizeStamp(int n) {
      return Integer.numberOfLeadingZeros(n) | 1 << RESIZE_STAMP_BITS - 1;
   }

   protected final Int2ObjectConcurrentHashMap.Node<V>[] initTable() {
      Int2ObjectConcurrentHashMap.Node<V>[] tab;
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
                  Int2ObjectConcurrentHashMap.Node<V>[] nt = new Int2ObjectConcurrentHashMap.Node[n];
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
            Int2ObjectConcurrentHashMap.CounterCell[] as = this.counterCells;
            if (this.counterCells == null) {
               long b = this.baseCount;
               if (U.compareAndSwapLong(this, BASECOUNT, this.baseCount, s = b + x)) {
                  break label74;
               }
            }

            uncontended = (boolean)1;
            Int2ObjectConcurrentHashMap.CounterCell a;
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

               Int2ObjectConcurrentHashMap.Node<V>[] tab = this.table;
               int n;
               if (this.table == null || (n = tab.length) >= 1073741824) {
                  break;
               }

               uncontended = (boolean)resizeStamp(n);
               if (sc < 0) {
                  if (sc >>> RESIZE_STAMP_SHIFT != uncontended || sc == uncontended + 1 || sc == uncontended + MAX_RESIZERS) {
                     break;
                  }

                  Int2ObjectConcurrentHashMap.Node<V>[] nt = this.nextTable;
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

   protected final Int2ObjectConcurrentHashMap.Node<V>[] helpTransfer(Int2ObjectConcurrentHashMap.Node<V>[] tab, Int2ObjectConcurrentHashMap.Node<V> f) {
      if (tab != null && f instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
         Int2ObjectConcurrentHashMap.Node<V>[] nextTab = ((Int2ObjectConcurrentHashMap.ForwardingNode)f).nextTable;
         if (((Int2ObjectConcurrentHashMap.ForwardingNode)f).nextTable != null) {
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

         Int2ObjectConcurrentHashMap.Node<V>[] tab = this.table;
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

                  Int2ObjectConcurrentHashMap.Node<V>[] nt = this.nextTable;
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
                     Int2ObjectConcurrentHashMap.Node<V>[] ntx = new Int2ObjectConcurrentHashMap.Node[n];
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

   protected final void transfer(Int2ObjectConcurrentHashMap.Node<V>[] tab, Int2ObjectConcurrentHashMap.Node<V>[] nextTab) {
      int n = tab.length;
      int stride;
      if ((stride = NCPU > 1 ? (n >>> 3) / NCPU : n) < 16) {
         stride = 16;
      }

      if (nextTab == null) {
         try {
            Int2ObjectConcurrentHashMap.Node<V>[] nt = new Int2ObjectConcurrentHashMap.Node[n << 1];
            nextTab = nt;
         } catch (Throwable var27) {
            this.sizeCtl = Integer.MAX_VALUE;
            return;
         }

         this.nextTable = nextTab;
         this.transferIndex = n;
      }

      int nextn = nextTab.length;
      Int2ObjectConcurrentHashMap.ForwardingNode<V> fwd = new Int2ObjectConcurrentHashMap.ForwardingNode<>(this.EMPTY, nextTab);
      boolean advance = true;
      boolean finishing = false;
      int i = 0;
      int bound = 0;

      while (true) {
         while (!advance) {
            if (i >= 0 && i < n && i + n < nextn) {
               Int2ObjectConcurrentHashMap.Node<V> f;
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
                              Int2ObjectConcurrentHashMap.Node<V> lastRun = f;

                              for (Int2ObjectConcurrentHashMap.Node<V> p = f.next; p != null; p = p.next) {
                                 int b = p.hash & n;
                                 if (b != runBit) {
                                    runBit = b;
                                    lastRun = p;
                                 }
                              }

                              Int2ObjectConcurrentHashMap.Node<V> ln;
                              Int2ObjectConcurrentHashMap.Node<V> hn;
                              if (runBit == 0) {
                                 ln = lastRun;
                                 hn = null;
                              } else {
                                 hn = lastRun;
                                 ln = null;
                              }

                              for (Int2ObjectConcurrentHashMap.Node<V> px = f; px != lastRun; px = px.next) {
                                 int ph = px.hash;
                                 int pk = px.key;
                                 V pv = px.val;
                                 if ((ph & n) == 0) {
                                    ln = new Int2ObjectConcurrentHashMap.Node<>(this.EMPTY, ph, pk, pv, ln);
                                 } else {
                                    hn = new Int2ObjectConcurrentHashMap.Node<>(this.EMPTY, ph, pk, pv, hn);
                                 }
                              }

                              setTabAt(nextTab, i, ln);
                              setTabAt(nextTab, i + n, hn);
                              setTabAt(tab, i, fwd);
                              advance = true;
                           } else if (f instanceof Int2ObjectConcurrentHashMap.TreeBin<V> t) {
                              Int2ObjectConcurrentHashMap.TreeNode<V> lo = null;
                              Int2ObjectConcurrentHashMap.TreeNode<V> loTail = null;
                              Int2ObjectConcurrentHashMap.TreeNode<V> hi = null;
                              Int2ObjectConcurrentHashMap.TreeNode<V> hiTail = null;
                              int lc = 0;
                              int hc = 0;

                              for (Int2ObjectConcurrentHashMap.Node<V> e = t.first; e != null; e = e.next) {
                                 int h = e.hash;
                                 Int2ObjectConcurrentHashMap.TreeNode<V> pxx = new Int2ObjectConcurrentHashMap.TreeNode<>(
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

                              Int2ObjectConcurrentHashMap.Node<V> ln = (Int2ObjectConcurrentHashMap.Node<V>)(lc <= 6
                                 ? this.untreeify(lo)
                                 : (hc != 0 ? new Int2ObjectConcurrentHashMap.TreeBin<>(this.EMPTY, lo) : t));
                              Int2ObjectConcurrentHashMap.Node<V> hn = (Int2ObjectConcurrentHashMap.Node<V>)(hc <= 6
                                 ? this.untreeify(hi)
                                 : (lc != 0 ? new Int2ObjectConcurrentHashMap.TreeBin<>(this.EMPTY, hi) : t));
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
      Int2ObjectConcurrentHashMap.CounterCell[] as = this.counterCells;
      long sum = this.baseCount;
      if (as != null) {
         for (int i = 0; i < as.length; i++) {
            Int2ObjectConcurrentHashMap.CounterCell a;
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
         Int2ObjectConcurrentHashMap.CounterCell[] as = this.counterCells;
         int n;
         if (this.counterCells != null && (n = as.length) > 0) {
            Int2ObjectConcurrentHashMap.CounterCell a;
            if ((a = as[n - 1 & h]) == null) {
               if (this.cellsBusy == 0) {
                  Int2ObjectConcurrentHashMap.CounterCell r = new Int2ObjectConcurrentHashMap.CounterCell(x);
                  if (this.cellsBusy == 0 && U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
                     boolean created = false;

                     try {
                        Int2ObjectConcurrentHashMap.CounterCell[] rs = this.counterCells;
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
                        Int2ObjectConcurrentHashMap.CounterCell[] rs = new Int2ObjectConcurrentHashMap.CounterCell[n << 1];

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
                  Int2ObjectConcurrentHashMap.CounterCell[] rs = new Int2ObjectConcurrentHashMap.CounterCell[2];
                  rs[h & 1] = new Int2ObjectConcurrentHashMap.CounterCell(x);
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

   protected final void treeifyBin(Int2ObjectConcurrentHashMap.Node<V>[] tab, int index) {
      if (tab != null) {
         int n;
         if ((n = tab.length) < 64) {
            this.tryPresize(n << 1);
         } else {
            Int2ObjectConcurrentHashMap.Node<V> b;
            if ((b = tabAt(tab, index)) != null && b.hash >= 0) {
               synchronized (b) {
                  if (tabAt(tab, index) == b) {
                     Int2ObjectConcurrentHashMap.TreeNode<V> hd = null;
                     Int2ObjectConcurrentHashMap.TreeNode<V> tl = null;

                     for (Int2ObjectConcurrentHashMap.Node<V> e = b; e != null; e = e.next) {
                        Int2ObjectConcurrentHashMap.TreeNode<V> p = new Int2ObjectConcurrentHashMap.TreeNode<>(this.EMPTY, e.hash, e.key, e.val, null, null);
                        if ((p.prev = tl) == null) {
                           hd = p;
                        } else {
                           tl.next = p;
                        }

                        tl = p;
                     }

                     setTabAt(tab, index, new Int2ObjectConcurrentHashMap.TreeBin<>(this.EMPTY, hd));
                  }
               }
            }
         }
      }
   }

   protected <V> Int2ObjectConcurrentHashMap.Node<V> untreeify(Int2ObjectConcurrentHashMap.Node<V> b) {
      Int2ObjectConcurrentHashMap.Node<V> hd = null;
      Int2ObjectConcurrentHashMap.Node<V> tl = null;

      for (Int2ObjectConcurrentHashMap.Node<V> q = b; q != null; q = q.next) {
         Int2ObjectConcurrentHashMap.Node<V> p = new Int2ObjectConcurrentHashMap.Node<>(this.EMPTY, q.hash, q.key, q.val, null);
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

   public void forEach(long parallelismThreshold, Int2ObjectConcurrentHashMap.IntObjConsumer<? super V> action) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         new Int2ObjectConcurrentHashMap.ForEachMappingTask<>(null, this.batchFor(parallelismThreshold), 0, 0, this.table, action).invoke();
      }
   }

   public <U> void forEach(
      long parallelismThreshold, Int2ObjectConcurrentHashMap.IntObjFunction<? super V, ? extends U> transformer, Consumer<? super U> action
   ) {
      if (transformer != null && action != null) {
         new Int2ObjectConcurrentHashMap.ForEachTransformedMappingTask<>(null, this.batchFor(parallelismThreshold), 0, 0, this.table, transformer, action)
            .invoke();
      } else {
         throw new NullPointerException();
      }
   }

   public <U> U search(long parallelismThreshold, Int2ObjectConcurrentHashMap.IntObjFunction<? super V, ? extends U> searchFunction) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         return new Int2ObjectConcurrentHashMap.SearchMappingsTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, searchFunction, new AtomicReference()
            )
            .invoke();
      }
   }

   public <U> U search(Int2ObjectConcurrentHashMap.IntObjFunction<? super V, ? extends U> searchFunction) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         Int2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Int2ObjectConcurrentHashMap.Node<V> next = null;
            Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                     Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Int2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                           Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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

   public <U, X> U search(Int2ObjectConcurrentHashMap.IntBiObjFunction<? super V, X, ? extends U> searchFunction, X x) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         Int2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Int2ObjectConcurrentHashMap.Node<V> next = null;
            Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                     Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Int2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                           Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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

   public <U> U searchWithByte(Int2ObjectConcurrentHashMap.IntObjByteFunction<? super V, ? extends U> searchFunction, byte x) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         Int2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Int2ObjectConcurrentHashMap.Node<V> next = null;
            Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                     Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Int2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                           Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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

   public <U> U searchWithShort(Int2ObjectConcurrentHashMap.IntObjShortFunction<? super V, ? extends U> searchFunction, short x) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         Int2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Int2ObjectConcurrentHashMap.Node<V> next = null;
            Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                     Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Int2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                           Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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

   public <U> U searchWithInt(Int2ObjectConcurrentHashMap.IntObjIntFunction<? super V, ? extends U> searchFunction, int x) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         Int2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Int2ObjectConcurrentHashMap.Node<V> next = null;
            Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                     Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Int2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                           Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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

   public <U> U searchWithLong(Int2ObjectConcurrentHashMap.IntObjLongFunction<? super V, ? extends U> searchFunction, long x) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         Int2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Int2ObjectConcurrentHashMap.Node<V> next = null;
            Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                     Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Int2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                           Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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

   public <U> U searchWithFloat(Int2ObjectConcurrentHashMap.IntObjFloatFunction<? super V, ? extends U> searchFunction, float x) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         Int2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Int2ObjectConcurrentHashMap.Node<V> next = null;
            Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                     Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Int2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                           Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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

   public <U> U searchWithDouble(Int2ObjectConcurrentHashMap.IntObjDoubleFunction<? super V, ? extends U> searchFunction, double x) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         Int2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table != null) {
            Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Int2ObjectConcurrentHashMap.Node<V> next = null;
            Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while (true) {
               Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                     Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Int2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                           Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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
      Int2ObjectConcurrentHashMap.IntObjFunction<? super V, ? extends U> transformer,
      BiFunction<? super U, ? super U, ? extends U> reducer
   ) {
      if (transformer != null && reducer != null) {
         return new Int2ObjectConcurrentHashMap.MapReduceMappingsTask<>(null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, reducer)
            .invoke();
      } else {
         throw new NullPointerException();
      }
   }

   public <U> U reduce(Int2ObjectConcurrentHashMap.IntObjFunction<? super V, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
      if (transformer != null && reducer != null) {
         Int2ObjectConcurrentHashMap.Node<V>[] tt = this.table;
         if (this.table == null) {
            return null;
         } else {
            Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
            Int2ObjectConcurrentHashMap.Node<V> next = null;
            Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
            Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;
            U r = null;

            while (true) {
               Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                     Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                           tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                           p = null;
                           Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new Int2ObjectConcurrentHashMap.TableStack<>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                           p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                           Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                           if (stack != null) {
                              int len = stack.length;
                              if ((index += stack.length) >= n) {
                                 n = len;
                                 index = stack.index;
                                 tab = stack.tab;
                                 stack.tab = null;
                                 Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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
      long parallelismThreshold, Int2ObjectConcurrentHashMap.ToDoubleIntObjFunction<? super V> transformer, double basis, DoubleBinaryOperator reducer
   ) {
      if (transformer != null && reducer != null) {
         return new Int2ObjectConcurrentHashMap.MapReduceMappingsToDoubleTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer
            )
            .invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public long reduceToLong(
      long parallelismThreshold, Int2ObjectConcurrentHashMap.ToLongIntObjFunction<? super V> transformer, long basis, LongBinaryOperator reducer
   ) {
      if (transformer != null && reducer != null) {
         return new Int2ObjectConcurrentHashMap.MapReduceMappingsToLongTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer
            )
            .invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public int reduceToInt(
      long parallelismThreshold, Int2ObjectConcurrentHashMap.ToIntIntObjFunction<? super V> transformer, int basis, IntBinaryOperator reducer
   ) {
      if (transformer != null && reducer != null) {
         return new Int2ObjectConcurrentHashMap.MapReduceMappingsToIntTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer
            )
            .invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public void forEachKey(long parallelismThreshold, IntConsumer action) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         new Int2ObjectConcurrentHashMap.ForEachKeyTask<>(null, this.batchFor(parallelismThreshold), 0, 0, this.table, action).invoke();
      }
   }

   public <U> void forEachKey(long parallelismThreshold, Int2ObjectConcurrentHashMap.IntFunction<? extends U> transformer, Consumer<? super U> action) {
      if (transformer != null && action != null) {
         new Int2ObjectConcurrentHashMap.ForEachTransformedKeyTask<>(null, this.batchFor(parallelismThreshold), 0, 0, this.table, transformer, action).invoke();
      } else {
         throw new NullPointerException();
      }
   }

   public <U> U searchKeys(long parallelismThreshold, Int2ObjectConcurrentHashMap.IntFunction<? extends U> searchFunction) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         return new Int2ObjectConcurrentHashMap.SearchKeysTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, searchFunction, new AtomicReference()
            )
            .invoke();
      }
   }

   public int reduceKeys(long parallelismThreshold, Int2ObjectConcurrentHashMap.IntegerReduceTaskOperator reducer) {
      if (reducer == null) {
         throw new NullPointerException();
      } else {
         return new Int2ObjectConcurrentHashMap.ReduceKeysTask<>(this.EMPTY, null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, reducer)
            .invoke0();
      }
   }

   public <U> U reduceKeys(
      long parallelismThreshold, Int2ObjectConcurrentHashMap.IntFunction<? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer
   ) {
      if (transformer != null && reducer != null) {
         return new Int2ObjectConcurrentHashMap.MapReduceKeysTask<>(null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, reducer)
            .invoke();
      } else {
         throw new NullPointerException();
      }
   }

   public double reduceKeysToDouble(
      long parallelismThreshold, Int2ObjectConcurrentHashMap.IntToDoubleFunction transformer, double basis, DoubleBinaryOperator reducer
   ) {
      if (transformer != null && reducer != null) {
         return new Int2ObjectConcurrentHashMap.MapReduceKeysToDoubleTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer
            )
            .invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public long reduceKeysToLong(long parallelismThreshold, Int2ObjectConcurrentHashMap.IntToLongFunction transformer, long basis, LongBinaryOperator reducer) {
      if (transformer != null && reducer != null) {
         return new Int2ObjectConcurrentHashMap.MapReduceKeysToLongTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer
            )
            .invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public int reduceKeysToInt(long parallelismThreshold, Int2ObjectConcurrentHashMap.IntToIntFunction transformer, int basis, IntBinaryOperator reducer) {
      if (transformer != null && reducer != null) {
         return new Int2ObjectConcurrentHashMap.MapReduceKeysToIntTask<>(
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
         new Int2ObjectConcurrentHashMap.ForEachValueTask<>(null, this.batchFor(parallelismThreshold), 0, 0, this.table, action).invoke();
      }
   }

   public <U> void forEachValue(long parallelismThreshold, Function<? super V, ? extends U> transformer, Consumer<? super U> action) {
      if (transformer != null && action != null) {
         new Int2ObjectConcurrentHashMap.ForEachTransformedValueTask<>(null, this.batchFor(parallelismThreshold), 0, 0, this.table, transformer, action)
            .invoke();
      } else {
         throw new NullPointerException();
      }
   }

   public <U> U searchValues(long parallelismThreshold, Function<? super V, ? extends U> searchFunction) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         return new Int2ObjectConcurrentHashMap.SearchValuesTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, searchFunction, new AtomicReference()
            )
            .invoke();
      }
   }

   public V reduceValues(long parallelismThreshold, BiFunction<? super V, ? super V, ? extends V> reducer) {
      if (reducer == null) {
         throw new NullPointerException();
      } else {
         return new Int2ObjectConcurrentHashMap.ReduceValuesTask<>(null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, reducer).invoke();
      }
   }

   public <U> U reduceValues(long parallelismThreshold, Function<? super V, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
      if (transformer != null && reducer != null) {
         return new Int2ObjectConcurrentHashMap.MapReduceValuesTask<>(null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, reducer)
            .invoke();
      } else {
         throw new NullPointerException();
      }
   }

   public double reduceValuesToDouble(long parallelismThreshold, ToDoubleFunction<? super V> transformer, double basis, DoubleBinaryOperator reducer) {
      if (transformer != null && reducer != null) {
         return new Int2ObjectConcurrentHashMap.MapReduceValuesToDoubleTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer
            )
            .invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public long reduceValuesToLong(long parallelismThreshold, ToLongFunction<? super V> transformer, long basis, LongBinaryOperator reducer) {
      if (transformer != null && reducer != null) {
         return new Int2ObjectConcurrentHashMap.MapReduceValuesToLongTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer
            )
            .invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public int reduceValuesToInt(
      long parallelismThreshold, Int2ObjectConcurrentHashMap.ToIntFunction<? super V> transformer, int basis, IntBinaryOperator reducer
   ) {
      if (transformer != null && reducer != null) {
         return new Int2ObjectConcurrentHashMap.MapReduceValuesToIntTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer
            )
            .invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public void forEachEntry(long parallelismThreshold, Consumer<? super Int2ObjectConcurrentHashMap.Entry<V>> action) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         new Int2ObjectConcurrentHashMap.ForEachEntryTask<>(null, this.batchFor(parallelismThreshold), 0, 0, this.table, action).invoke();
      }
   }

   public <U> void forEachEntry(long parallelismThreshold, Function<Int2ObjectConcurrentHashMap.Entry<V>, ? extends U> transformer, Consumer<? super U> action) {
      if (transformer != null && action != null) {
         new Int2ObjectConcurrentHashMap.ForEachTransformedEntryTask<>(null, this.batchFor(parallelismThreshold), 0, 0, this.table, transformer, action)
            .invoke();
      } else {
         throw new NullPointerException();
      }
   }

   public <U> U searchEntries(long parallelismThreshold, Function<Int2ObjectConcurrentHashMap.Entry<V>, ? extends U> searchFunction) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         return new Int2ObjectConcurrentHashMap.SearchEntriesTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, searchFunction, new AtomicReference()
            )
            .invoke();
      }
   }

   public Int2ObjectConcurrentHashMap.Entry<V> reduceEntries(
      long parallelismThreshold,
      BiFunction<Int2ObjectConcurrentHashMap.Entry<V>, Int2ObjectConcurrentHashMap.Entry<V>, ? extends Int2ObjectConcurrentHashMap.Entry<V>> reducer
   ) {
      if (reducer == null) {
         throw new NullPointerException();
      } else {
         return new Int2ObjectConcurrentHashMap.ReduceEntriesTask<>(null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, reducer).invoke();
      }
   }

   public <U> U reduceEntries(
      long parallelismThreshold, Function<Int2ObjectConcurrentHashMap.Entry<V>, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer
   ) {
      if (transformer != null && reducer != null) {
         return new Int2ObjectConcurrentHashMap.MapReduceEntriesTask<>(null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, reducer)
            .invoke();
      } else {
         throw new NullPointerException();
      }
   }

   public double reduceEntriesToDouble(
      long parallelismThreshold, ToDoubleFunction<Int2ObjectConcurrentHashMap.Entry<V>> transformer, double basis, DoubleBinaryOperator reducer
   ) {
      if (transformer != null && reducer != null) {
         return new Int2ObjectConcurrentHashMap.MapReduceEntriesToDoubleTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer
            )
            .invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public long reduceEntriesToLong(
      long parallelismThreshold, ToLongFunction<Int2ObjectConcurrentHashMap.Entry<V>> transformer, long basis, LongBinaryOperator reducer
   ) {
      if (transformer != null && reducer != null) {
         return new Int2ObjectConcurrentHashMap.MapReduceEntriesToLongTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer
            )
            .invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public int reduceEntriesToInt(
      long parallelismThreshold,
      Int2ObjectConcurrentHashMap.ToIntFunction<Int2ObjectConcurrentHashMap.Entry<V>> transformer,
      int basis,
      IntBinaryOperator reducer
   ) {
      if (transformer != null && reducer != null) {
         return new Int2ObjectConcurrentHashMap.MapReduceEntriesToIntTask<>(
               null, this.batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer
            )
            .invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public V valueMatching(Predicate<V> predicate) {
      Int2ObjectConcurrentHashMap.Node<V> next = null;
      Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
      Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
      int index = 0;
      int baseIndex = 0;
      Int2ObjectConcurrentHashMap.Node<V>[] tab = this.table;
      int f = this.table == null ? 0 : tab.length;
      int baseLimit = f;
      int baseSize = f;
      boolean b = false;

      label80:
      while (next != null || !b) {
         b |= true;
         Int2ObjectConcurrentHashMap.Node<V> e = next;
         if (next != null) {
            e = next.next;
         }

         label76:
         while (e == null) {
            if (baseIndex < baseLimit) {
               Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
               int n;
               if (tab != null && (n = tab.length) > index && index >= 0) {
                  if ((e = tabAt(tab, index)) != null && e.hash < 0) {
                     if (e instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                        tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)e).nextTable;
                        e = null;
                        Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                        if (spare != null) {
                           spare = spare.next;
                        } else {
                           s = new Int2ObjectConcurrentHashMap.TableStack<>();
                        }

                        s.tab = t;
                        s.length = n;
                        s.index = index;
                        s.next = stack;
                        stack = s;
                        continue;
                     }

                     if (e instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                        e = ((Int2ObjectConcurrentHashMap.TreeBin)e).first;
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
                     Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                     if (stack != null) {
                        int len = stack.length;
                        if ((index += stack.length) >= n) {
                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           Int2ObjectConcurrentHashMap.TableStack<V> next1 = stack.next;
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
         Class<?> k = Int2ObjectConcurrentHashMap.class;
         SIZECTL = U.objectFieldOffset(k.getDeclaredField("sizeCtl"));
         TRANSFERINDEX = U.objectFieldOffset(k.getDeclaredField("transferIndex"));
         BASECOUNT = U.objectFieldOffset(k.getDeclaredField("baseCount"));
         CELLSBUSY = U.objectFieldOffset(k.getDeclaredField("cellsBusy"));
         Class<?> ck = Int2ObjectConcurrentHashMap.CounterCell.class;
         CELLVALUE = U.objectFieldOffset(ck.getDeclaredField("value"));
         Class<?> ak = Int2ObjectConcurrentHashMap.Node[].class;
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

   protected static class BaseIterator<V> extends Int2ObjectConcurrentHashMap.Traverser<V> {
      public final Int2ObjectConcurrentHashMap<V> map;
      public Int2ObjectConcurrentHashMap.Node<V> lastReturned;

      public BaseIterator(Int2ObjectConcurrentHashMap.Node<V>[] tab, int size, int index, int limit, Int2ObjectConcurrentHashMap<V> map) {
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
         Int2ObjectConcurrentHashMap.Node<V> p = this.lastReturned;
         if (this.lastReturned == null) {
            throw new IllegalStateException();
         } else {
            this.lastReturned = null;
            this.map.replaceNode(p.key, null, null);
         }
      }
   }

   protected abstract static class BulkTask<V, R> extends CountedCompleter<R> {
      public Int2ObjectConcurrentHashMap.Node<V>[] tab;
      public Int2ObjectConcurrentHashMap.Node<V> next;
      public Int2ObjectConcurrentHashMap.TableStack<V> stack;
      public Int2ObjectConcurrentHashMap.TableStack<V> spare;
      public int index;
      public int baseIndex;
      public int baseLimit;
      public final int baseSize;
      public int batch;

      protected BulkTask(Int2ObjectConcurrentHashMap.BulkTask<V, ?> par, int b, int i, int f, Int2ObjectConcurrentHashMap.Node<V>[] t) {
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

      protected final Int2ObjectConcurrentHashMap.Node<V> advance() {
         Int2ObjectConcurrentHashMap.Node<V> e = this.next;
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

            Int2ObjectConcurrentHashMap.Node<V>[] t = this.tab;
            if (this.tab == null) {
               break;
            }

            int n;
            int var10000 = n = t.length;
            int i = this.index;
            if (var10000 <= this.index || i < 0) {
               break;
            }

            if ((e = Int2ObjectConcurrentHashMap.tabAt(t, i)) != null && e.hash < 0) {
               if (e instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                  this.tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)e).nextTable;
                  e = null;
                  this.pushState(t, i, n);
                  continue;
               }

               if (e instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                  e = ((Int2ObjectConcurrentHashMap.TreeBin)e).first;
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

      protected void pushState(Int2ObjectConcurrentHashMap.Node<V>[] t, int i, int n) {
         Int2ObjectConcurrentHashMap.TableStack<V> s = this.spare;
         if (s != null) {
            this.spare = s.next;
         } else {
            s = new Int2ObjectConcurrentHashMap.TableStack<>();
         }

         s.tab = t;
         s.length = n;
         s.index = i;
         s.next = this.stack;
         this.stack = s;
      }

      protected void recoverState(int n) {
         while (true) {
            Int2ObjectConcurrentHashMap.TableStack<V> s = this.stack;
            if (this.stack != null) {
               int len = s.length;
               if ((this.index = this.index + s.length) >= n) {
                  n = len;
                  this.index = s.index;
                  this.tab = s.tab;
                  s.tab = null;
                  Int2ObjectConcurrentHashMap.TableStack<V> next = s.next;
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
      public final Int2ObjectConcurrentHashMap<K> map;
      protected static final String oomeMsg = "Required array size too large";

      public CollectionView(Int2ObjectConcurrentHashMap<K> map) {
         this.map = map;
      }

      public Int2ObjectConcurrentHashMap<K> getMap() {
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

   protected abstract static class DoubleReturningBulkTask<V> extends Int2ObjectConcurrentHashMap.BulkTask<V, Double> {
      public double result;

      public DoubleReturningBulkTask(Int2ObjectConcurrentHashMap.BulkTask<V, ?> par, int b, int i, int f, Int2ObjectConcurrentHashMap.Node<V>[] t) {
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

   public interface Entry<V> extends it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<V> {
      boolean isEmpty();

      @Deprecated
      Integer getKey();

      int getIntKey();

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
      extends Int2ObjectConcurrentHashMap.BaseIterator<V>
      implements ObjectIterator<it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<V>> {
      public EntryIterator(Int2ObjectConcurrentHashMap.Node<V>[] tab, int index, int size, int limit, Int2ObjectConcurrentHashMap<V> map) {
         super(tab, index, size, limit, map);
      }

      public final Int2ObjectConcurrentHashMap.Entry<V> next() {
         Int2ObjectConcurrentHashMap.Node<V> p = this.next;
         if (this.next == null) {
            throw new NoSuchElementException();
         } else {
            int k = p.key;
            V v = p.val;
            this.lastReturned = p;
            this.advance();
            return new Int2ObjectConcurrentHashMap.MapEntry<>(p.isEmpty(), k, v, this.map);
         }
      }
   }

   protected static final class EntrySetView<V>
      extends Int2ObjectConcurrentHashMap.CollectionView<V, it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<V>>
      implements ObjectSet<it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<V>>,
      Serializable {
      public static final long serialVersionUID = 2249069246763182397L;

      public EntrySetView(Int2ObjectConcurrentHashMap<V> map) {
         super(map);
      }

      @Override
      public boolean contains(Object o) {
         if (o instanceof it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry) {
            it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<?> e;
            int k = (e = (it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<?>)o).getIntKey();
            if (!((Int2ObjectConcurrentHashMap.Entry)o).isEmpty()) {
               Object v;
               Object r;
               return (r = this.map.get(k)) != null && (v = e.getValue()) != null && (v == r || v.equals(r));
            }
         }

         return false;
      }

      @Override
      public boolean remove(Object o) {
         if (o instanceof it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry) {
            it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<?> e;
            int k = (e = (it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<?>)o).getIntKey();
            if (!((Int2ObjectConcurrentHashMap.Entry)o).isEmpty()) {
               Object v;
               return (v = e.getValue()) != null && this.map.remove(k, v);
            }
         }

         return false;
      }

      @Override
      public ObjectIterator<it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<V>> iterator() {
         Int2ObjectConcurrentHashMap<V> m = this.map;
         Int2ObjectConcurrentHashMap.Node<V>[] t = m.table;
         int f = m.table == null ? 0 : t.length;
         return new Int2ObjectConcurrentHashMap.EntryIterator<>(t, f, 0, f, m);
      }

      public boolean add(it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<V> e) {
         return this.map.putVal(e.getIntKey(), (V)e.getValue(), false) == null;
      }

      public boolean addAll(Collection<? extends it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<V>> c) {
         boolean added = false;

         for (it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<V> e : c) {
            if (this.add(e)) {
               added = true;
            }
         }

         return added;
      }

      @Override
      public final int hashCode() {
         int h = 0;
         Int2ObjectConcurrentHashMap.Node<V>[] t = this.map.table;
         if (this.map.table != null) {
            Int2ObjectConcurrentHashMap.Traverser<V> it = new Int2ObjectConcurrentHashMap.Traverser<>(t, t.length, 0, t.length);

            Int2ObjectConcurrentHashMap.Node<V> p;
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

      public ObjectSpliterator<it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<V>> spliterator() {
         Int2ObjectConcurrentHashMap<V> m = this.map;
         long n = m.sumCount();
         Int2ObjectConcurrentHashMap.Node<V>[] t = m.table;
         int f = m.table == null ? 0 : t.length;
         return new Int2ObjectConcurrentHashMap.EntrySpliterator<>(t, f, 0, f, n < 0L ? 0L : n, m);
      }

      public void forEach(Consumer<? super it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<V>> action) {
         if (action == null) {
            throw new NullPointerException();
         } else {
            Int2ObjectConcurrentHashMap.Node<V>[] t = this.map.table;
            if (this.map.table != null) {
               Int2ObjectConcurrentHashMap.Traverser<V> it = new Int2ObjectConcurrentHashMap.Traverser<>(t, t.length, 0, t.length);

               Int2ObjectConcurrentHashMap.Node<V> p;
               while ((p = it.advance()) != null) {
                  action.accept(new Int2ObjectConcurrentHashMap.MapEntry<>(p.isEmpty(), p.key, p.val, this.map));
               }
            }
         }
      }
   }

   protected static final class EntrySpliterator<V>
      extends Int2ObjectConcurrentHashMap.Traverser<V>
      implements ObjectSpliterator<it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<V>> {
      public final Int2ObjectConcurrentHashMap<V> map;
      public long est;

      public EntrySpliterator(Int2ObjectConcurrentHashMap.Node<V>[] tab, int size, int index, int limit, long est, Int2ObjectConcurrentHashMap<V> map) {
         super(tab, size, index, limit);
         this.map = map;
         this.est = est;
      }

      public ObjectSpliterator<it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<V>> trySplit() {
         int i = this.baseIndex;
         int f = this.baseLimit;
         int h;
         return (h = this.baseIndex + this.baseLimit >>> 1) <= i
            ? null
            : new Int2ObjectConcurrentHashMap.EntrySpliterator<>(this.tab, this.baseSize, this.baseLimit = h, f, this.est >>>= 1, this.map);
      }

      public void forEachRemaining(Consumer<? super it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<V>> action) {
         if (action == null) {
            throw new NullPointerException();
         } else {
            Int2ObjectConcurrentHashMap.Node<V> p;
            while ((p = this.advance()) != null) {
               action.accept(new Int2ObjectConcurrentHashMap.MapEntry<>(p.isEmpty(), p.key, p.val, this.map));
            }
         }
      }

      public boolean tryAdvance(Consumer<? super it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<V>> action) {
         if (action == null) {
            throw new NullPointerException();
         } else {
            Int2ObjectConcurrentHashMap.Node<V> p;
            if ((p = this.advance()) == null) {
               return false;
            } else {
               action.accept(new Int2ObjectConcurrentHashMap.MapEntry<>(p.isEmpty(), p.key, p.val, this.map));
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

   protected static final class ForEachEntryTask<V> extends Int2ObjectConcurrentHashMap.BulkTask<V, Void> {
      public final Consumer<? super Int2ObjectConcurrentHashMap.Entry<V>> action;

      public ForEachEntryTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
         Consumer<? super Int2ObjectConcurrentHashMap.Entry<V>> action
      ) {
         super(p, b, i, f, t);
         this.action = action;
      }

      @Override
      public final void compute() {
         Consumer<? super Int2ObjectConcurrentHashMap.Entry<V>> action = this.action;
         if (this.action != null) {
            int i = this.baseIndex;

            while (this.batch > 0) {
               int f = this.baseLimit;
               int h;
               if ((h = this.baseLimit + i >>> 1) <= i) {
                  break;
               }

               this.addToPendingCount(1);
               new Int2ObjectConcurrentHashMap.ForEachEntryTask<>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, action).fork();
            }

            while ((p = this.advance()) != null) {
               action.accept(p);
            }

            this.propagateCompletion();
         }
      }
   }

   protected static final class ForEachKeyTask<V> extends Int2ObjectConcurrentHashMap.BulkTask<V, Void> {
      public final IntConsumer action;

      public ForEachKeyTask(Int2ObjectConcurrentHashMap.BulkTask<V, ?> p, int b, int i, int f, Int2ObjectConcurrentHashMap.Node<V>[] t, IntConsumer action) {
         super(p, b, i, f, t);
         this.action = action;
      }

      @Override
      public final void compute() {
         IntConsumer action = this.action;
         if (this.action != null) {
            int i = this.baseIndex;

            while (this.batch > 0) {
               int f = this.baseLimit;
               int h;
               if ((h = this.baseLimit + i >>> 1) <= i) {
                  break;
               }

               this.addToPendingCount(1);
               new Int2ObjectConcurrentHashMap.ForEachKeyTask<>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, action).fork();
            }

            while ((p = this.advance()) != null) {
               action.accept(p.key);
            }

            this.propagateCompletion();
         }
      }
   }

   protected static final class ForEachMappingTask<V> extends Int2ObjectConcurrentHashMap.BulkTask<V, Void> {
      public final Int2ObjectConcurrentHashMap.IntObjConsumer<? super V> action;

      public ForEachMappingTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
         Int2ObjectConcurrentHashMap.IntObjConsumer<? super V> action
      ) {
         super(p, b, i, f, t);
         this.action = action;
      }

      @Override
      public final void compute() {
         Int2ObjectConcurrentHashMap.IntObjConsumer<? super V> action = this.action;
         if (this.action != null) {
            int i = this.baseIndex;

            while (this.batch > 0) {
               int f = this.baseLimit;
               int h;
               if ((h = this.baseLimit + i >>> 1) <= i) {
                  break;
               }

               this.addToPendingCount(1);
               new Int2ObjectConcurrentHashMap.ForEachMappingTask<>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, action).fork();
            }

            while ((p = this.advance()) != null) {
               action.accept(p.key, p.val);
            }

            this.propagateCompletion();
         }
      }
   }

   protected static final class ForEachTransformedEntryTask<V, U> extends Int2ObjectConcurrentHashMap.BulkTask<V, Void> {
      public final Function<Int2ObjectConcurrentHashMap.Entry<V>, ? extends U> transformer;
      public final Consumer<? super U> action;

      public ForEachTransformedEntryTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
         Function<Int2ObjectConcurrentHashMap.Entry<V>, ? extends U> transformer,
         Consumer<? super U> action
      ) {
         super(p, b, i, f, t);
         this.transformer = transformer;
         this.action = action;
      }

      @Override
      public final void compute() {
         Function<Int2ObjectConcurrentHashMap.Entry<V>, ? extends U> transformer = this.transformer;
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
                  new Int2ObjectConcurrentHashMap.ForEachTransformedEntryTask<>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, transformer, action)
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

   protected static final class ForEachTransformedKeyTask<V, U> extends Int2ObjectConcurrentHashMap.BulkTask<V, Void> {
      public final Int2ObjectConcurrentHashMap.IntFunction<? extends U> transformer;
      public final Consumer<? super U> action;

      public ForEachTransformedKeyTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
         Int2ObjectConcurrentHashMap.IntFunction<? extends U> transformer,
         Consumer<? super U> action
      ) {
         super(p, b, i, f, t);
         this.transformer = transformer;
         this.action = action;
      }

      @Override
      public final void compute() {
         Int2ObjectConcurrentHashMap.IntFunction<? extends U> transformer = this.transformer;
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
                  new Int2ObjectConcurrentHashMap.ForEachTransformedKeyTask<>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, transformer, action)
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

   protected static final class ForEachTransformedMappingTask<V, U> extends Int2ObjectConcurrentHashMap.BulkTask<V, Void> {
      public final Int2ObjectConcurrentHashMap.IntObjFunction<? super V, ? extends U> transformer;
      public final Consumer<? super U> action;

      public ForEachTransformedMappingTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
         Int2ObjectConcurrentHashMap.IntObjFunction<? super V, ? extends U> transformer,
         Consumer<? super U> action
      ) {
         super(p, b, i, f, t);
         this.transformer = transformer;
         this.action = action;
      }

      @Override
      public final void compute() {
         Int2ObjectConcurrentHashMap.IntObjFunction<? super V, ? extends U> transformer = this.transformer;
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
                  new Int2ObjectConcurrentHashMap.ForEachTransformedMappingTask<>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, transformer, action)
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

   protected static final class ForEachTransformedValueTask<V, U> extends Int2ObjectConcurrentHashMap.BulkTask<V, Void> {
      public final Function<? super V, ? extends U> transformer;
      public final Consumer<? super U> action;

      public ForEachTransformedValueTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
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
                  new Int2ObjectConcurrentHashMap.ForEachTransformedValueTask<>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, transformer, action)
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

   protected static final class ForEachValueTask<V> extends Int2ObjectConcurrentHashMap.BulkTask<V, Void> {
      public final Consumer<? super V> action;

      public ForEachValueTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p, int b, int i, int f, Int2ObjectConcurrentHashMap.Node<V>[] t, Consumer<? super V> action
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
               new Int2ObjectConcurrentHashMap.ForEachValueTask<>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, action).fork();
            }

            while ((p = this.advance()) != null) {
               action.accept(p.val);
            }

            this.propagateCompletion();
         }
      }
   }

   protected static final class ForwardingNode<V> extends Int2ObjectConcurrentHashMap.Node<V> {
      public final Int2ObjectConcurrentHashMap.Node<V>[] nextTable;

      public ForwardingNode(int empty, Int2ObjectConcurrentHashMap.Node<V>[] tab) {
         super(empty, -1, empty, null, null);
         this.nextTable = tab;
      }

      @Override
      protected Int2ObjectConcurrentHashMap.Node<V> find(int h, int k) {
         Int2ObjectConcurrentHashMap.Node<V>[] tab = this.nextTable;

         Int2ObjectConcurrentHashMap.Node<V> e;
         int n;
         label41:
         while (k != this.EMPTY && tab != null && (n = tab.length) != 0 && (e = Int2ObjectConcurrentHashMap.tabAt(tab, n - 1 & h)) != null) {
            do {
               int eh = e.hash;
               if (e.hash == h) {
                  int ek = e.key;
                  if (e.key == k || ek != this.EMPTY && k == ek) {
                     return e;
                  }
               }

               if (eh < 0) {
                  if (!(e instanceof Int2ObjectConcurrentHashMap.ForwardingNode)) {
                     return e.find(h, k);
                  }

                  tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)e).nextTable;
                  continue label41;
               }
            } while ((e = e.next) != null);

            return null;
         }

         return null;
      }
   }

   @FunctionalInterface
   public interface IntBiObjByteConsumer<V, X> {
      void accept(int var1, V var2, byte var3, X var4);
   }

   @FunctionalInterface
   public interface IntBiObjConsumer<V, X> {
      void accept(int var1, V var2, X var3);
   }

   @FunctionalInterface
   public interface IntBiObjDoubleConsumer<V, X> {
      void accept(int var1, V var2, double var3, X var5);
   }

   @FunctionalInterface
   public interface IntBiObjFloatConsumer<V, X> {
      void accept(int var1, V var2, float var3, X var4);
   }

   @FunctionalInterface
   public interface IntBiObjFunction<V, X, J> {
      J apply(int var1, V var2, X var3);
   }

   @FunctionalInterface
   public interface IntBiObjIntConsumer<V, X> {
      void accept(int var1, V var2, int var3, X var4);
   }

   @FunctionalInterface
   public interface IntBiObjLongConsumer<V, X> {
      void accept(int var1, V var2, long var3, X var5);
   }

   @FunctionalInterface
   public interface IntBiObjShortConsumer<V, X> {
      void accept(int var1, V var2, short var3, X var4);
   }

   @FunctionalInterface
   public interface IntFunction<R> {
      R apply(int var1);
   }

   @FunctionalInterface
   public interface IntObjByteConsumer<V> {
      void accept(int var1, V var2, byte var3);
   }

   @FunctionalInterface
   public interface IntObjByteFunction<V, J> {
      J apply(int var1, V var2, byte var3);
   }

   @FunctionalInterface
   public interface IntObjConsumer<V> {
      void accept(int var1, V var2);
   }

   @FunctionalInterface
   public interface IntObjDoubleConsumer<V> {
      void accept(int var1, V var2, double var3);
   }

   @FunctionalInterface
   public interface IntObjDoubleFunction<V, J> {
      J apply(int var1, V var2, double var3);
   }

   @FunctionalInterface
   public interface IntObjFloatConsumer<V> {
      void accept(int var1, V var2, float var3);
   }

   @FunctionalInterface
   public interface IntObjFloatFunction<V, J> {
      J apply(int var1, V var2, float var3);
   }

   @FunctionalInterface
   public interface IntObjFunction<V, J> {
      J apply(int var1, V var2);
   }

   @FunctionalInterface
   public interface IntObjIntConsumer<V> {
      void accept(int var1, V var2, int var3);
   }

   @FunctionalInterface
   public interface IntObjIntFunction<V, J> {
      J apply(int var1, V var2, int var3);
   }

   @FunctionalInterface
   public interface IntObjLongConsumer<V> {
      void accept(int var1, V var2, long var3);
   }

   @FunctionalInterface
   public interface IntObjLongFunction<V, J> {
      J apply(int var1, V var2, long var3);
   }

   @FunctionalInterface
   public interface IntObjShortConsumer<V> {
      void accept(int var1, V var2, short var3);
   }

   @FunctionalInterface
   public interface IntObjShortFunction<V, J> {
      J apply(int var1, V var2, short var3);
   }

   protected abstract static class IntReturningBulkTask<V> extends Int2ObjectConcurrentHashMap.BulkTask<V, Integer> {
      public int result;

      public IntReturningBulkTask(Int2ObjectConcurrentHashMap.BulkTask<V, ?> par, int b, int i, int f, Int2ObjectConcurrentHashMap.Node<V>[] t) {
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

   @FunctionalInterface
   public interface IntToDoubleFunction {
      double applyAsDouble(int var1);
   }

   @FunctionalInterface
   public interface IntToIntFunction {
      int applyAsInt(int var1);
   }

   @FunctionalInterface
   public interface IntToLongFunction {
      long applyAsLong(int var1);
   }

   @FunctionalInterface
   public interface IntTriObjConsumer<V, X, Y> {
      void accept(int var1, V var2, X var3, Y var4);
   }

   @FunctionalInterface
   public interface IntegerReduceTaskOperator {
      int reduce(int var1, int var2, int var3);
   }

   protected abstract static class IntegerReturningBulkTask2<V> extends Int2ObjectConcurrentHashMap.BulkTask<V, Integer> {
      public int result;

      public IntegerReturningBulkTask2(Int2ObjectConcurrentHashMap.BulkTask<V, ?> par, int b, int i, int f, Int2ObjectConcurrentHashMap.Node<V>[] t) {
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

   protected static final class KeyIterator<V> implements IntIterator {
      public Int2ObjectConcurrentHashMap.Node<V>[] tab;
      public Int2ObjectConcurrentHashMap.Node<V> next;
      public Int2ObjectConcurrentHashMap.TableStack<V> stack;
      public Int2ObjectConcurrentHashMap.TableStack<V> spare;
      public int index;
      public int baseIndex;
      public int baseLimit;
      public final int baseSize;
      public final Int2ObjectConcurrentHashMap<V> map;
      public Int2ObjectConcurrentHashMap.Node<V> lastReturned;

      public KeyIterator(Int2ObjectConcurrentHashMap.Node<V>[] tab, int size, int index, int limit, Int2ObjectConcurrentHashMap<V> map) {
         this.tab = tab;
         this.baseSize = size;
         this.baseIndex = this.index = index;
         this.baseLimit = limit;
         this.next = null;
         this.map = map;
         this.advance();
      }

      protected final Int2ObjectConcurrentHashMap.Node<V> advance() {
         Int2ObjectConcurrentHashMap.Node<V> e = this.next;
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

            Int2ObjectConcurrentHashMap.Node<V>[] t = this.tab;
            if (this.tab == null) {
               break;
            }

            int n;
            int var10000 = n = t.length;
            int i = this.index;
            if (var10000 <= this.index || i < 0) {
               break;
            }

            if ((e = Int2ObjectConcurrentHashMap.tabAt(t, i)) != null && e.hash < 0) {
               if (e instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                  this.tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)e).nextTable;
                  e = null;
                  this.pushState(t, i, n);
                  continue;
               }

               if (e instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                  e = ((Int2ObjectConcurrentHashMap.TreeBin)e).first;
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

      protected void pushState(Int2ObjectConcurrentHashMap.Node<V>[] t, int i, int n) {
         Int2ObjectConcurrentHashMap.TableStack<V> s = this.spare;
         if (s != null) {
            this.spare = s.next;
         } else {
            s = new Int2ObjectConcurrentHashMap.TableStack<>();
         }

         s.tab = t;
         s.length = n;
         s.index = i;
         s.next = this.stack;
         this.stack = s;
      }

      protected void recoverState(int n) {
         while (true) {
            Int2ObjectConcurrentHashMap.TableStack<V> s = this.stack;
            if (this.stack != null) {
               int len = s.length;
               if ((this.index = this.index + s.length) >= n) {
                  n = len;
                  this.index = s.index;
                  this.tab = s.tab;
                  s.tab = null;
                  Int2ObjectConcurrentHashMap.TableStack<V> next = s.next;
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
         Int2ObjectConcurrentHashMap.Node<V> p = this.lastReturned;
         if (this.lastReturned == null) {
            throw new IllegalStateException();
         } else {
            this.lastReturned = null;
            this.map.replaceNode(p.key, null, null);
         }
      }

      public final int nextInt() {
         Int2ObjectConcurrentHashMap.Node<V> p = this.next;
         if (this.next == null) {
            throw new NoSuchElementException();
         } else {
            int k = p.key;
            this.lastReturned = p;
            this.advance();
            return k;
         }
      }
   }

   public static class KeySetView<V> implements IntSet {
      public static final long serialVersionUID = 7249069246763182397L;
      public final Int2ObjectConcurrentHashMap<V> map;
      public final V value;

      public KeySetView(Int2ObjectConcurrentHashMap<V> map, V value) {
         this.map = map;
         this.value = value;
      }

      public V getMappedValue() {
         return this.value;
      }

      public boolean contains(int o) {
         return this.map.containsKey(o);
      }

      public boolean remove(int o) {
         return this.map.remove(o) != null;
      }

      public IntIterator iterator() {
         Int2ObjectConcurrentHashMap<V> m = this.map;
         Int2ObjectConcurrentHashMap.Node<V>[] t = m.table;
         int f = m.table == null ? 0 : t.length;
         return new Int2ObjectConcurrentHashMap.KeyIterator<>(t, f, 0, f, m);
      }

      public boolean add(int e) {
         V v = this.value;
         if (this.value == null) {
            throw new UnsupportedOperationException();
         } else {
            return this.map.putVal(e, v, true) == null;
         }
      }

      public boolean addAll(IntCollection c) {
         boolean added = false;
         V v = this.value;
         if (this.value == null) {
            throw new UnsupportedOperationException();
         } else {
            IntIterator iter = c.iterator();

            while (iter.hasNext()) {
               int e = iter.nextInt();
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
         IntIterator iter = this.iterator();

         while (iter.hasNext()) {
            h += Integer.hashCode(iter.nextInt());
         }

         return h;
      }

      @Override
      public boolean equals(Object o) {
         IntSet c;
         return o instanceof IntSet && ((c = (IntSet)o) == this || this.containsAll(c) && c.containsAll(this));
      }

      public int getNoEntryValue() {
         return this.map.EMPTY;
      }

      public int size() {
         return this.map.size();
      }

      public boolean isEmpty() {
         return this.map.isEmpty();
      }

      public Object[] toArray() {
         Object[] out = new Integer[this.size()];
         IntIterator iter = this.iterator();

         int i;
         for (i = 0; i < out.length && iter.hasNext(); i++) {
            out[i] = iter.nextInt();
         }

         if (out.length > i + 1) {
            out[i] = this.map.EMPTY;
         }

         return out;
      }

      public Object[] toArray(Object[] dest) {
         IntIterator iter = this.iterator();

         int i;
         for (i = 0; i < dest.length && iter.hasNext() && i <= dest.length; i++) {
            dest[i] = iter.next();
         }

         if (dest.length > i + 1) {
            dest[i] = this.map.EMPTY;
         }

         return dest;
      }

      public int[] toIntArray() {
         int[] out = new int[this.size()];
         IntIterator iter = this.iterator();

         int i;
         for (i = 0; i < out.length && iter.hasNext(); i++) {
            out[i] = iter.next();
         }

         if (out.length > i + 1) {
            out[i] = this.map.EMPTY;
         }

         return out;
      }

      public int[] toArray(int[] dest) {
         IntIterator iter = this.iterator();

         int i;
         for (i = 0; i < dest.length && iter.hasNext() && i <= dest.length; i++) {
            dest[i] = iter.next();
         }

         if (dest.length > i + 1) {
            dest[i] = this.map.EMPTY;
         }

         return dest;
      }

      public int[] toIntArray(int[] dest) {
         return this.toArray(dest);
      }

      public boolean containsAll(Collection<?> collection) {
         for (Object element : collection) {
            if (!(element instanceof Long)) {
               return false;
            }

            int c = (Integer)element;
            if (!this.contains(c)) {
               return false;
            }
         }

         return true;
      }

      public boolean containsAll(IntCollection collection) {
         IntIterator iter = collection.iterator();

         while (iter.hasNext()) {
            int element = iter.next();
            if (!this.contains(element)) {
               return false;
            }
         }

         return true;
      }

      public boolean containsAll(int[] array) {
         int i = array.length;

         while (i-- > 0) {
            if (!this.contains(array[i])) {
               return false;
            }
         }

         return true;
      }

      public boolean addAll(Collection<? extends Integer> collection) {
         boolean changed = false;

         for (Integer element : collection) {
            int e = element;
            if (this.add(e)) {
               changed = true;
            }
         }

         return changed;
      }

      public boolean addAll(int[] array) {
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
         IntIterator iter = this.iterator();

         while (iter.hasNext()) {
            if (!collection.contains(iter.next())) {
               iter.remove();
               modified = true;
            }
         }

         return modified;
      }

      public boolean retainAll(IntCollection collection) {
         if (this == collection) {
            return false;
         } else {
            boolean modified = false;
            IntIterator iter = this.iterator();

            while (iter.hasNext()) {
               if (!collection.contains(iter.next())) {
                  iter.remove();
                  modified = true;
               }
            }

            return modified;
         }
      }

      public boolean retainAll(int[] array) {
         boolean modified = false;
         IntIterator iter = this.iterator();

         while (iter.hasNext()) {
            if (Arrays.binarySearch(array, iter.next().intValue()) < 0) {
               iter.remove();
               modified = true;
            }
         }

         return modified;
      }

      public boolean removeAll(Collection<?> collection) {
         boolean changed = false;

         for (Object element : collection) {
            if (element instanceof Integer) {
               int c = (Integer)element;
               if (this.remove(c)) {
                  changed = true;
               }
            }
         }

         return changed;
      }

      public boolean removeAll(IntCollection collection) {
         boolean changed = false;
         IntIterator iter = collection.iterator();

         while (iter.hasNext()) {
            int element = iter.next();
            if (this.remove(element)) {
               changed = true;
            }
         }

         return changed;
      }

      public boolean removeAll(int[] array) {
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

      public IntSpliterator spliterator() {
         Int2ObjectConcurrentHashMap<V> m = this.map;
         long n = m.sumCount();
         Int2ObjectConcurrentHashMap.Node<V>[] t = m.table;
         int f = m.table == null ? 0 : t.length;
         return new Int2ObjectConcurrentHashMap.KeySpliterator<>(t, f, 0, f, n < 0L ? 0L : n);
      }
   }

   protected static final class KeySpliterator<V> extends Int2ObjectConcurrentHashMap.Traverser<V> implements IntSpliterator {
      public long est;

      public KeySpliterator(Int2ObjectConcurrentHashMap.Node<V>[] tab, int size, int index, int limit, long est) {
         super(tab, size, index, limit);
         this.est = est;
      }

      public IntSpliterator trySplit() {
         int i = this.baseIndex;
         int f = this.baseLimit;
         int h;
         return (h = this.baseIndex + this.baseLimit >>> 1) <= i
            ? null
            : new Int2ObjectConcurrentHashMap.KeySpliterator<>(this.tab, this.baseSize, this.baseLimit = h, f, this.est >>>= 1);
      }

      public boolean tryAdvance(Consumer<? super Integer> action) {
         return action instanceof IntConsumer ? this.tryAdvance((IntConsumer)action) : this.tryAdvance(value -> action.accept(value));
      }

      public void forEachRemaining(IntConsumer action) {
         if (action == null) {
            throw new NullPointerException();
         } else {
            Int2ObjectConcurrentHashMap.Node<V> p;
            while ((p = this.advance()) != null) {
               action.accept(p.key);
            }
         }
      }

      public boolean tryAdvance(IntConsumer action) {
         if (action == null) {
            throw new NullPointerException();
         } else {
            Int2ObjectConcurrentHashMap.Node<V> p;
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

   protected abstract static class LongReturningBulkTask<V> extends Int2ObjectConcurrentHashMap.BulkTask<V, Long> {
      public long result;

      public LongReturningBulkTask(Int2ObjectConcurrentHashMap.BulkTask<V, ?> par, int b, int i, int f, Int2ObjectConcurrentHashMap.Node<V>[] t) {
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

   protected static final class MapEntry<V> implements Int2ObjectConcurrentHashMap.Entry<V> {
      public final boolean empty;
      public final int key;
      public V val;
      public final Int2ObjectConcurrentHashMap<V> map;

      public MapEntry(boolean empty, int key, V val, Int2ObjectConcurrentHashMap<V> map) {
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
      public Integer getKey() {
         return this.key;
      }

      @Override
      public int getIntKey() {
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
         } else if (o instanceof Int2ObjectConcurrentHashMap.Entry) {
            if (this.empty != ((Int2ObjectConcurrentHashMap.Entry)o).isEmpty()) {
               return false;
            } else {
               return !this.empty && this.key != ((Int2ObjectConcurrentHashMap.Entry)o).getIntKey()
                  ? false
                  : this.val.equals(((Int2ObjectConcurrentHashMap.Entry)o).getValue());
            }
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         int result = this.empty ? 1 : 0;
         result = 31 * result + Integer.hashCode(this.key);
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

   protected static final class MapReduceEntriesTask<V, U> extends Int2ObjectConcurrentHashMap.BulkTask<V, U> {
      public final Function<Int2ObjectConcurrentHashMap.Entry<V>, ? extends U> transformer;
      public final BiFunction<? super U, ? super U, ? extends U> reducer;
      public U result;
      public Int2ObjectConcurrentHashMap.MapReduceEntriesTask<V, U> rights;
      public Int2ObjectConcurrentHashMap.MapReduceEntriesTask<V, U> nextRight;

      public MapReduceEntriesTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
         Int2ObjectConcurrentHashMap.MapReduceEntriesTask<V, U> nextRight,
         Function<Int2ObjectConcurrentHashMap.Entry<V>, ? extends U> transformer,
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
         Function<Int2ObjectConcurrentHashMap.Entry<V>, ? extends U> transformer = this.transformer;
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
                  (this.rights = new Int2ObjectConcurrentHashMap.MapReduceEntriesTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, reducer
                     ))
                     .fork();
               }

               U r = null;

               Int2ObjectConcurrentHashMap.Node<V> p;
               while ((p = this.advance()) != null) {
                  U u;
                  if ((u = (U)transformer.apply(p)) != null) {
                     r = r == null ? u : reducer.apply(r, u);
                  }
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Int2ObjectConcurrentHashMap.MapReduceEntriesTask<V, U> t = (Int2ObjectConcurrentHashMap.MapReduceEntriesTask<V, U>)c;

                  for (Int2ObjectConcurrentHashMap.MapReduceEntriesTask<V, U> s = t.rights; s != null; s = t.rights = s.nextRight) {
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

   protected static final class MapReduceEntriesToDoubleTask<V> extends Int2ObjectConcurrentHashMap.DoubleReturningBulkTask<V> {
      public final ToDoubleFunction<Int2ObjectConcurrentHashMap.Entry<V>> transformer;
      public final DoubleBinaryOperator reducer;
      public final double basis;
      public Int2ObjectConcurrentHashMap.MapReduceEntriesToDoubleTask<V> rights;
      public Int2ObjectConcurrentHashMap.MapReduceEntriesToDoubleTask<V> nextRight;

      public MapReduceEntriesToDoubleTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
         Int2ObjectConcurrentHashMap.MapReduceEntriesToDoubleTask<V> nextRight,
         ToDoubleFunction<Int2ObjectConcurrentHashMap.Entry<V>> transformer,
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
         ToDoubleFunction<Int2ObjectConcurrentHashMap.Entry<V>> transformer = this.transformer;
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
                  (this.rights = new Int2ObjectConcurrentHashMap.MapReduceEntriesToDoubleTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer
                     ))
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  r = reducer.applyAsDouble(r, transformer.applyAsDouble(p));
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Int2ObjectConcurrentHashMap.MapReduceEntriesToDoubleTask<V> t = (Int2ObjectConcurrentHashMap.MapReduceEntriesToDoubleTask<V>)c;

                  for (Int2ObjectConcurrentHashMap.MapReduceEntriesToDoubleTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     t.result = reducer.applyAsDouble(t.result, s.result);
                  }
               }
            }
         }
      }
   }

   protected static final class MapReduceEntriesToIntTask<V> extends Int2ObjectConcurrentHashMap.IntReturningBulkTask<V> {
      public final Int2ObjectConcurrentHashMap.ToIntFunction<Int2ObjectConcurrentHashMap.Entry<V>> transformer;
      public final IntBinaryOperator reducer;
      public final int basis;
      public Int2ObjectConcurrentHashMap.MapReduceEntriesToIntTask<V> rights;
      public Int2ObjectConcurrentHashMap.MapReduceEntriesToIntTask<V> nextRight;

      public MapReduceEntriesToIntTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
         Int2ObjectConcurrentHashMap.MapReduceEntriesToIntTask<V> nextRight,
         Int2ObjectConcurrentHashMap.ToIntFunction<Int2ObjectConcurrentHashMap.Entry<V>> transformer,
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
         Int2ObjectConcurrentHashMap.ToIntFunction<Int2ObjectConcurrentHashMap.Entry<V>> transformer = this.transformer;
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
                  (this.rights = new Int2ObjectConcurrentHashMap.MapReduceEntriesToIntTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer
                     ))
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  r = reducer.applyAsInt(r, transformer.applyAsInt(p));
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Int2ObjectConcurrentHashMap.MapReduceEntriesToIntTask<V> t = (Int2ObjectConcurrentHashMap.MapReduceEntriesToIntTask<V>)c;

                  for (Int2ObjectConcurrentHashMap.MapReduceEntriesToIntTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     t.result = reducer.applyAsInt(t.result, s.result);
                  }
               }
            }
         }
      }
   }

   protected static final class MapReduceEntriesToLongTask<V> extends Int2ObjectConcurrentHashMap.LongReturningBulkTask<V> {
      public final ToLongFunction<Int2ObjectConcurrentHashMap.Entry<V>> transformer;
      public final LongBinaryOperator reducer;
      public final long basis;
      public Int2ObjectConcurrentHashMap.MapReduceEntriesToLongTask<V> rights;
      public Int2ObjectConcurrentHashMap.MapReduceEntriesToLongTask<V> nextRight;

      public MapReduceEntriesToLongTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
         Int2ObjectConcurrentHashMap.MapReduceEntriesToLongTask<V> nextRight,
         ToLongFunction<Int2ObjectConcurrentHashMap.Entry<V>> transformer,
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
         ToLongFunction<Int2ObjectConcurrentHashMap.Entry<V>> transformer = this.transformer;
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
                  (this.rights = new Int2ObjectConcurrentHashMap.MapReduceEntriesToLongTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer
                     ))
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  r = reducer.applyAsLong(r, transformer.applyAsLong(p));
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Int2ObjectConcurrentHashMap.MapReduceEntriesToLongTask<V> t = (Int2ObjectConcurrentHashMap.MapReduceEntriesToLongTask<V>)c;

                  for (Int2ObjectConcurrentHashMap.MapReduceEntriesToLongTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     t.result = reducer.applyAsLong(t.result, s.result);
                  }
               }
            }
         }
      }
   }

   protected static final class MapReduceKeysTask<V, U> extends Int2ObjectConcurrentHashMap.BulkTask<V, U> {
      public final Int2ObjectConcurrentHashMap.IntFunction<? extends U> transformer;
      public final BiFunction<? super U, ? super U, ? extends U> reducer;
      public U result;
      public Int2ObjectConcurrentHashMap.MapReduceKeysTask<V, U> rights;
      public Int2ObjectConcurrentHashMap.MapReduceKeysTask<V, U> nextRight;

      public MapReduceKeysTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
         Int2ObjectConcurrentHashMap.MapReduceKeysTask<V, U> nextRight,
         Int2ObjectConcurrentHashMap.IntFunction<? extends U> transformer,
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
         Int2ObjectConcurrentHashMap.IntFunction<? extends U> transformer = this.transformer;
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
                  (this.rights = new Int2ObjectConcurrentHashMap.MapReduceKeysTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, reducer
                     ))
                     .fork();
               }

               U r = null;

               Int2ObjectConcurrentHashMap.Node<V> p;
               while ((p = this.advance()) != null) {
                  U u;
                  if ((u = (U)transformer.apply(p.key)) != null) {
                     r = r == null ? u : reducer.apply(r, u);
                  }
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Int2ObjectConcurrentHashMap.MapReduceKeysTask<V, U> t = (Int2ObjectConcurrentHashMap.MapReduceKeysTask<V, U>)c;

                  for (Int2ObjectConcurrentHashMap.MapReduceKeysTask<V, U> s = t.rights; s != null; s = t.rights = s.nextRight) {
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

   protected static final class MapReduceKeysToDoubleTask<V> extends Int2ObjectConcurrentHashMap.DoubleReturningBulkTask<V> {
      public final Int2ObjectConcurrentHashMap.IntToDoubleFunction transformer;
      public final DoubleBinaryOperator reducer;
      public final double basis;
      public Int2ObjectConcurrentHashMap.MapReduceKeysToDoubleTask<V> rights;
      public Int2ObjectConcurrentHashMap.MapReduceKeysToDoubleTask<V> nextRight;

      public MapReduceKeysToDoubleTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
         Int2ObjectConcurrentHashMap.MapReduceKeysToDoubleTask<V> nextRight,
         Int2ObjectConcurrentHashMap.IntToDoubleFunction transformer,
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
         Int2ObjectConcurrentHashMap.IntToDoubleFunction transformer = this.transformer;
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
                  (this.rights = new Int2ObjectConcurrentHashMap.MapReduceKeysToDoubleTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer
                     ))
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  r = reducer.applyAsDouble(r, transformer.applyAsDouble(p.key));
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Int2ObjectConcurrentHashMap.MapReduceKeysToDoubleTask<V> t = (Int2ObjectConcurrentHashMap.MapReduceKeysToDoubleTask<V>)c;

                  for (Int2ObjectConcurrentHashMap.MapReduceKeysToDoubleTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     t.result = reducer.applyAsDouble(t.result, s.result);
                  }
               }
            }
         }
      }
   }

   protected static final class MapReduceKeysToIntTask<V> extends Int2ObjectConcurrentHashMap.IntReturningBulkTask<V> {
      public final Int2ObjectConcurrentHashMap.IntToIntFunction transformer;
      public final IntBinaryOperator reducer;
      public final int basis;
      public Int2ObjectConcurrentHashMap.MapReduceKeysToIntTask<V> rights;
      public Int2ObjectConcurrentHashMap.MapReduceKeysToIntTask<V> nextRight;

      public MapReduceKeysToIntTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
         Int2ObjectConcurrentHashMap.MapReduceKeysToIntTask<V> nextRight,
         Int2ObjectConcurrentHashMap.IntToIntFunction transformer,
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
         Int2ObjectConcurrentHashMap.IntToIntFunction transformer = this.transformer;
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
                  (this.rights = new Int2ObjectConcurrentHashMap.MapReduceKeysToIntTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer
                     ))
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  r = reducer.applyAsInt(r, transformer.applyAsInt(p.key));
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Int2ObjectConcurrentHashMap.MapReduceKeysToIntTask<V> t = (Int2ObjectConcurrentHashMap.MapReduceKeysToIntTask<V>)c;

                  for (Int2ObjectConcurrentHashMap.MapReduceKeysToIntTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     t.result = reducer.applyAsInt(t.result, s.result);
                  }
               }
            }
         }
      }
   }

   protected static final class MapReduceKeysToLongTask<V> extends Int2ObjectConcurrentHashMap.LongReturningBulkTask<V> {
      public final Int2ObjectConcurrentHashMap.IntToLongFunction transformer;
      public final LongBinaryOperator reducer;
      public final long basis;
      public Int2ObjectConcurrentHashMap.MapReduceKeysToLongTask<V> rights;
      public Int2ObjectConcurrentHashMap.MapReduceKeysToLongTask<V> nextRight;

      public MapReduceKeysToLongTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
         Int2ObjectConcurrentHashMap.MapReduceKeysToLongTask<V> nextRight,
         Int2ObjectConcurrentHashMap.IntToLongFunction transformer,
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
         Int2ObjectConcurrentHashMap.IntToLongFunction transformer = this.transformer;
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
                  (this.rights = new Int2ObjectConcurrentHashMap.MapReduceKeysToLongTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer
                     ))
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  r = reducer.applyAsLong(r, transformer.applyAsLong(p.key));
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Int2ObjectConcurrentHashMap.MapReduceKeysToLongTask<V> t = (Int2ObjectConcurrentHashMap.MapReduceKeysToLongTask<V>)c;

                  for (Int2ObjectConcurrentHashMap.MapReduceKeysToLongTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     t.result = reducer.applyAsLong(t.result, s.result);
                  }
               }
            }
         }
      }
   }

   protected static final class MapReduceMappingsTask<V, U> extends Int2ObjectConcurrentHashMap.BulkTask<V, U> {
      public final Int2ObjectConcurrentHashMap.IntObjFunction<? super V, ? extends U> transformer;
      public final BiFunction<? super U, ? super U, ? extends U> reducer;
      public U result;
      public Int2ObjectConcurrentHashMap.MapReduceMappingsTask<V, U> rights;
      public Int2ObjectConcurrentHashMap.MapReduceMappingsTask<V, U> nextRight;

      public MapReduceMappingsTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
         Int2ObjectConcurrentHashMap.MapReduceMappingsTask<V, U> nextRight,
         Int2ObjectConcurrentHashMap.IntObjFunction<? super V, ? extends U> transformer,
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
         Int2ObjectConcurrentHashMap.IntObjFunction<? super V, ? extends U> transformer = this.transformer;
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
                  (this.rights = new Int2ObjectConcurrentHashMap.MapReduceMappingsTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, reducer
                     ))
                     .fork();
               }

               U r = null;

               Int2ObjectConcurrentHashMap.Node<V> p;
               while ((p = this.advance()) != null) {
                  U u;
                  if ((u = (U)transformer.apply(p.key, p.val)) != null) {
                     r = r == null ? u : reducer.apply(r, u);
                  }
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Int2ObjectConcurrentHashMap.MapReduceMappingsTask<V, U> t = (Int2ObjectConcurrentHashMap.MapReduceMappingsTask<V, U>)c;

                  for (Int2ObjectConcurrentHashMap.MapReduceMappingsTask<V, U> s = t.rights; s != null; s = t.rights = s.nextRight) {
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

   protected static final class MapReduceMappingsToDoubleTask<V> extends Int2ObjectConcurrentHashMap.DoubleReturningBulkTask<V> {
      public final Int2ObjectConcurrentHashMap.ToDoubleIntObjFunction<? super V> transformer;
      public final DoubleBinaryOperator reducer;
      public final double basis;
      public Int2ObjectConcurrentHashMap.MapReduceMappingsToDoubleTask<V> rights;
      public Int2ObjectConcurrentHashMap.MapReduceMappingsToDoubleTask<V> nextRight;

      public MapReduceMappingsToDoubleTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
         Int2ObjectConcurrentHashMap.MapReduceMappingsToDoubleTask<V> nextRight,
         Int2ObjectConcurrentHashMap.ToDoubleIntObjFunction<? super V> transformer,
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
         Int2ObjectConcurrentHashMap.ToDoubleIntObjFunction<? super V> transformer = this.transformer;
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
                  (this.rights = new Int2ObjectConcurrentHashMap.MapReduceMappingsToDoubleTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer
                     ))
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  r = reducer.applyAsDouble(r, transformer.applyAsDouble(p.key, p.val));
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Int2ObjectConcurrentHashMap.MapReduceMappingsToDoubleTask<V> t = (Int2ObjectConcurrentHashMap.MapReduceMappingsToDoubleTask<V>)c;

                  for (Int2ObjectConcurrentHashMap.MapReduceMappingsToDoubleTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     t.result = reducer.applyAsDouble(t.result, s.result);
                  }
               }
            }
         }
      }
   }

   protected static final class MapReduceMappingsToIntTask<V> extends Int2ObjectConcurrentHashMap.IntReturningBulkTask<V> {
      public final Int2ObjectConcurrentHashMap.ToIntIntObjFunction<? super V> transformer;
      public final IntBinaryOperator reducer;
      public final int basis;
      public Int2ObjectConcurrentHashMap.MapReduceMappingsToIntTask<V> rights;
      public Int2ObjectConcurrentHashMap.MapReduceMappingsToIntTask<V> nextRight;

      public MapReduceMappingsToIntTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
         Int2ObjectConcurrentHashMap.MapReduceMappingsToIntTask<V> nextRight,
         Int2ObjectConcurrentHashMap.ToIntIntObjFunction<? super V> transformer,
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
         Int2ObjectConcurrentHashMap.ToIntIntObjFunction<? super V> transformer = this.transformer;
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
                  (this.rights = new Int2ObjectConcurrentHashMap.MapReduceMappingsToIntTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer
                     ))
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  r = reducer.applyAsInt(r, transformer.applyAsInt(p.key, p.val));
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Int2ObjectConcurrentHashMap.MapReduceMappingsToIntTask<V> t = (Int2ObjectConcurrentHashMap.MapReduceMappingsToIntTask<V>)c;

                  for (Int2ObjectConcurrentHashMap.MapReduceMappingsToIntTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     t.result = reducer.applyAsInt(t.result, s.result);
                  }
               }
            }
         }
      }
   }

   protected static final class MapReduceMappingsToLongTask<V> extends Int2ObjectConcurrentHashMap.LongReturningBulkTask<V> {
      public final Int2ObjectConcurrentHashMap.ToLongIntObjFunction<? super V> transformer;
      public final LongBinaryOperator reducer;
      public final long basis;
      public Int2ObjectConcurrentHashMap.MapReduceMappingsToLongTask<V> rights;
      public Int2ObjectConcurrentHashMap.MapReduceMappingsToLongTask<V> nextRight;

      public MapReduceMappingsToLongTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
         Int2ObjectConcurrentHashMap.MapReduceMappingsToLongTask<V> nextRight,
         Int2ObjectConcurrentHashMap.ToLongIntObjFunction<? super V> transformer,
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
         Int2ObjectConcurrentHashMap.ToLongIntObjFunction<? super V> transformer = this.transformer;
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
                  (this.rights = new Int2ObjectConcurrentHashMap.MapReduceMappingsToLongTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer
                     ))
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  r = reducer.applyAsLong(r, transformer.applyAsLong(p.key, p.val));
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Int2ObjectConcurrentHashMap.MapReduceMappingsToLongTask<V> t = (Int2ObjectConcurrentHashMap.MapReduceMappingsToLongTask<V>)c;

                  for (Int2ObjectConcurrentHashMap.MapReduceMappingsToLongTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     t.result = reducer.applyAsLong(t.result, s.result);
                  }
               }
            }
         }
      }
   }

   protected static final class MapReduceValuesTask<V, U> extends Int2ObjectConcurrentHashMap.BulkTask<V, U> {
      public final Function<? super V, ? extends U> transformer;
      public final BiFunction<? super U, ? super U, ? extends U> reducer;
      public U result;
      public Int2ObjectConcurrentHashMap.MapReduceValuesTask<V, U> rights;
      public Int2ObjectConcurrentHashMap.MapReduceValuesTask<V, U> nextRight;

      public MapReduceValuesTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
         Int2ObjectConcurrentHashMap.MapReduceValuesTask<V, U> nextRight,
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
                  (this.rights = new Int2ObjectConcurrentHashMap.MapReduceValuesTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, reducer
                     ))
                     .fork();
               }

               U r = null;

               Int2ObjectConcurrentHashMap.Node<V> p;
               while ((p = this.advance()) != null) {
                  U u;
                  if ((u = (U)transformer.apply(p.val)) != null) {
                     r = r == null ? u : reducer.apply(r, u);
                  }
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Int2ObjectConcurrentHashMap.MapReduceValuesTask<V, U> t = (Int2ObjectConcurrentHashMap.MapReduceValuesTask<V, U>)c;

                  for (Int2ObjectConcurrentHashMap.MapReduceValuesTask<V, U> s = t.rights; s != null; s = t.rights = s.nextRight) {
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

   protected static final class MapReduceValuesToDoubleTask<V> extends Int2ObjectConcurrentHashMap.DoubleReturningBulkTask<V> {
      public final ToDoubleFunction<? super V> transformer;
      public final DoubleBinaryOperator reducer;
      public final double basis;
      public Int2ObjectConcurrentHashMap.MapReduceValuesToDoubleTask<V> rights;
      public Int2ObjectConcurrentHashMap.MapReduceValuesToDoubleTask<V> nextRight;

      public MapReduceValuesToDoubleTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
         Int2ObjectConcurrentHashMap.MapReduceValuesToDoubleTask<V> nextRight,
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
                  (this.rights = new Int2ObjectConcurrentHashMap.MapReduceValuesToDoubleTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer
                     ))
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  r = reducer.applyAsDouble(r, transformer.applyAsDouble(p.val));
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Int2ObjectConcurrentHashMap.MapReduceValuesToDoubleTask<V> t = (Int2ObjectConcurrentHashMap.MapReduceValuesToDoubleTask<V>)c;

                  for (Int2ObjectConcurrentHashMap.MapReduceValuesToDoubleTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     t.result = reducer.applyAsDouble(t.result, s.result);
                  }
               }
            }
         }
      }
   }

   protected static final class MapReduceValuesToIntTask<V> extends Int2ObjectConcurrentHashMap.IntReturningBulkTask<V> {
      public final Int2ObjectConcurrentHashMap.ToIntFunction<? super V> transformer;
      public final IntBinaryOperator reducer;
      public final int basis;
      public Int2ObjectConcurrentHashMap.MapReduceValuesToIntTask<V> rights;
      public Int2ObjectConcurrentHashMap.MapReduceValuesToIntTask<V> nextRight;

      public MapReduceValuesToIntTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
         Int2ObjectConcurrentHashMap.MapReduceValuesToIntTask<V> nextRight,
         Int2ObjectConcurrentHashMap.ToIntFunction<? super V> transformer,
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
         Int2ObjectConcurrentHashMap.ToIntFunction<? super V> transformer = this.transformer;
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
                  (this.rights = new Int2ObjectConcurrentHashMap.MapReduceValuesToIntTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer
                     ))
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  r = reducer.applyAsInt(r, transformer.applyAsInt(p.val));
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Int2ObjectConcurrentHashMap.MapReduceValuesToIntTask<V> t = (Int2ObjectConcurrentHashMap.MapReduceValuesToIntTask<V>)c;

                  for (Int2ObjectConcurrentHashMap.MapReduceValuesToIntTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     t.result = reducer.applyAsInt(t.result, s.result);
                  }
               }
            }
         }
      }
   }

   protected static final class MapReduceValuesToLongTask<V> extends Int2ObjectConcurrentHashMap.LongReturningBulkTask<V> {
      public final ToLongFunction<? super V> transformer;
      public final LongBinaryOperator reducer;
      public final long basis;
      public Int2ObjectConcurrentHashMap.MapReduceValuesToLongTask<V> rights;
      public Int2ObjectConcurrentHashMap.MapReduceValuesToLongTask<V> nextRight;

      public MapReduceValuesToLongTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
         Int2ObjectConcurrentHashMap.MapReduceValuesToLongTask<V> nextRight,
         ToLongFunction<? super V> transformer,
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
         ToLongFunction<? super V> transformer = this.transformer;
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
                  (this.rights = new Int2ObjectConcurrentHashMap.MapReduceValuesToLongTask<>(
                        this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer
                     ))
                     .fork();
               }

               while ((p = this.advance()) != null) {
                  r = reducer.applyAsLong(r, transformer.applyAsLong(p.val));
               }

               this.result = r;

               for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
                  Int2ObjectConcurrentHashMap.MapReduceValuesToLongTask<V> t = (Int2ObjectConcurrentHashMap.MapReduceValuesToLongTask<V>)c;

                  for (Int2ObjectConcurrentHashMap.MapReduceValuesToLongTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                     t.result = reducer.applyAsLong(t.result, s.result);
                  }
               }
            }
         }
      }
   }

   protected static class Node<V> implements Int2ObjectConcurrentHashMap.Entry<V> {
      public final int EMPTY;
      public final int hash;
      public final int key;
      public volatile V val;
      public volatile Int2ObjectConcurrentHashMap.Node<V> next;

      public Node(int empty, int hash, int key, V val, Int2ObjectConcurrentHashMap.Node<V> next) {
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
      public final Integer getKey() {
         return this.key;
      }

      @Override
      public final int getIntKey() {
         return this.key;
      }

      @Override
      public final V getValue() {
         return this.val;
      }

      @Override
      public final int hashCode() {
         return Integer.hashCode(this.key) ^ this.val.hashCode();
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
         if (o instanceof Int2ObjectConcurrentHashMap.Entry) {
            if (empty != ((Int2ObjectConcurrentHashMap.Entry)o).isEmpty()) {
               return false;
            } else {
               return !empty && this.key != ((Int2ObjectConcurrentHashMap.Entry)o).getIntKey()
                  ? false
                  : this.val.equals(((Int2ObjectConcurrentHashMap.Entry)o).getValue());
            }
         } else {
            return false;
         }
      }

      protected Int2ObjectConcurrentHashMap.Node<V> find(int h, int k) {
         Int2ObjectConcurrentHashMap.Node<V> e = this;
         if (k != this.EMPTY) {
            do {
               if (e.hash == h) {
                  int ek = e.key;
                  if (e.key == k || ek != this.EMPTY && k == ek) {
                     return e;
                  }
               }
            } while ((e = e.next) != null);
         }

         return null;
      }
   }

   protected static final class ReduceEntriesTask<V> extends Int2ObjectConcurrentHashMap.BulkTask<V, Int2ObjectConcurrentHashMap.Entry<V>> {
      public final BiFunction<Int2ObjectConcurrentHashMap.Entry<V>, Int2ObjectConcurrentHashMap.Entry<V>, ? extends Int2ObjectConcurrentHashMap.Entry<V>> reducer;
      public Int2ObjectConcurrentHashMap.Entry<V> result;
      public Int2ObjectConcurrentHashMap.ReduceEntriesTask<V> rights;
      public Int2ObjectConcurrentHashMap.ReduceEntriesTask<V> nextRight;

      public ReduceEntriesTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
         Int2ObjectConcurrentHashMap.ReduceEntriesTask<V> nextRight,
         BiFunction<Int2ObjectConcurrentHashMap.Entry<V>, Int2ObjectConcurrentHashMap.Entry<V>, ? extends Int2ObjectConcurrentHashMap.Entry<V>> reducer
      ) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.reducer = reducer;
      }

      public final Int2ObjectConcurrentHashMap.Entry<V> getRawResult() {
         return this.result;
      }

      @Override
      public final void compute() {
         BiFunction<Int2ObjectConcurrentHashMap.Entry<V>, Int2ObjectConcurrentHashMap.Entry<V>, ? extends Int2ObjectConcurrentHashMap.Entry<V>> reducer = this.reducer;
         if (this.reducer != null) {
            int i = this.baseIndex;

            while (this.batch > 0) {
               int f = this.baseLimit;
               int h;
               if ((h = this.baseLimit + i >>> 1) <= i) {
                  break;
               }

               this.addToPendingCount(1);
               (this.rights = new Int2ObjectConcurrentHashMap.ReduceEntriesTask<>(
                     this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, reducer
                  ))
                  .fork();
            }

            Int2ObjectConcurrentHashMap.Entry<V> r = null;

            Int2ObjectConcurrentHashMap.Node<V> p;
            while ((p = this.advance()) != null) {
               r = (Int2ObjectConcurrentHashMap.Entry<V>)(r == null ? p : reducer.apply(r, p));
            }

            this.result = r;

            for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
               Int2ObjectConcurrentHashMap.ReduceEntriesTask<V> t = (Int2ObjectConcurrentHashMap.ReduceEntriesTask<V>)c;

               for (Int2ObjectConcurrentHashMap.ReduceEntriesTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                  Int2ObjectConcurrentHashMap.Entry<V> sr = s.result;
                  if (s.result != null) {
                     Int2ObjectConcurrentHashMap.Entry<V> tr = t.result;
                     t.result = t.result == null ? sr : reducer.apply(tr, sr);
                  }
               }
            }
         }
      }
   }

   protected static final class ReduceKeysTask<V> extends Int2ObjectConcurrentHashMap.IntegerReturningBulkTask2<V> {
      public final int EMPTY;
      public final Int2ObjectConcurrentHashMap.IntegerReduceTaskOperator reducer;
      public Int2ObjectConcurrentHashMap.ReduceKeysTask<V> rights;
      public Int2ObjectConcurrentHashMap.ReduceKeysTask<V> nextRight;

      public ReduceKeysTask(
         int EMPTY,
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
         Int2ObjectConcurrentHashMap.ReduceKeysTask<V> nextRight,
         Int2ObjectConcurrentHashMap.IntegerReduceTaskOperator reducer
      ) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.EMPTY = EMPTY;
         this.reducer = reducer;
      }

      public final Integer getRawResult() {
         throw new UnsupportedOperationException();
      }

      @Override
      public final void compute() {
         Int2ObjectConcurrentHashMap.IntegerReduceTaskOperator reducer = this.reducer;
         if (this.reducer != null) {
            int i = this.baseIndex;

            while (this.batch > 0) {
               int f = this.baseLimit;
               int h;
               if ((h = this.baseLimit + i >>> 1) <= i) {
                  break;
               }

               this.addToPendingCount(1);
               (this.rights = new Int2ObjectConcurrentHashMap.ReduceKeysTask<>(
                     this.EMPTY, this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, reducer
                  ))
                  .fork();
            }

            boolean found = false;
            int r = this.EMPTY;

            Int2ObjectConcurrentHashMap.Node<V> p;
            while ((p = this.advance()) != null) {
               int u = p.key;
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
               Int2ObjectConcurrentHashMap.ReduceKeysTask<V> t = (Int2ObjectConcurrentHashMap.ReduceKeysTask<V>)c;

               for (Int2ObjectConcurrentHashMap.ReduceKeysTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                  int sr = s.result;
                  if (s.result != this.EMPTY) {
                     int tr = t.result;
                     t.result = t.result == this.EMPTY ? sr : reducer.reduce(this.EMPTY, tr, sr);
                  }
               }
            }
         }
      }
   }

   protected static final class ReduceValuesTask<V> extends Int2ObjectConcurrentHashMap.BulkTask<V, V> {
      public final BiFunction<? super V, ? super V, ? extends V> reducer;
      public V result;
      public Int2ObjectConcurrentHashMap.ReduceValuesTask<V> rights;
      public Int2ObjectConcurrentHashMap.ReduceValuesTask<V> nextRight;

      public ReduceValuesTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
         Int2ObjectConcurrentHashMap.ReduceValuesTask<V> nextRight,
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
               (this.rights = new Int2ObjectConcurrentHashMap.ReduceValuesTask<>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, reducer))
                  .fork();
            }

            V r = null;

            Int2ObjectConcurrentHashMap.Node<V> p;
            while ((p = this.advance()) != null) {
               V v = p.val;
               r = r == null ? v : reducer.apply(r, v);
            }

            this.result = r;

            for (CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
               Int2ObjectConcurrentHashMap.ReduceValuesTask<V> t = (Int2ObjectConcurrentHashMap.ReduceValuesTask<V>)c;

               for (Int2ObjectConcurrentHashMap.ReduceValuesTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
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

   protected static final class ReservationNode<V> extends Int2ObjectConcurrentHashMap.Node<V> {
      public ReservationNode(int empty) {
         super(empty, -3, empty, null, null);
      }

      @Override
      protected Int2ObjectConcurrentHashMap.Node<V> find(int h, int k) {
         return null;
      }
   }

   protected static final class SearchEntriesTask<V, U> extends Int2ObjectConcurrentHashMap.BulkTask<V, U> {
      public final Function<Int2ObjectConcurrentHashMap.Entry<V>, ? extends U> searchFunction;
      public final AtomicReference<U> result;

      public SearchEntriesTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
         Function<Int2ObjectConcurrentHashMap.Entry<V>, ? extends U> searchFunction,
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
         Function<Int2ObjectConcurrentHashMap.Entry<V>, ? extends U> searchFunction = this.searchFunction;
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
                  new Int2ObjectConcurrentHashMap.SearchEntriesTask<>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, searchFunction, result).fork();
               }

               while (result.get() == null) {
                  Int2ObjectConcurrentHashMap.Node<V> p;
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

   protected static final class SearchKeysTask<V, U> extends Int2ObjectConcurrentHashMap.BulkTask<V, U> {
      public final Int2ObjectConcurrentHashMap.IntFunction<? extends U> searchFunction;
      public final AtomicReference<U> result;

      public SearchKeysTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
         Int2ObjectConcurrentHashMap.IntFunction<? extends U> searchFunction,
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
         Int2ObjectConcurrentHashMap.IntFunction<? extends U> searchFunction = this.searchFunction;
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
                  new Int2ObjectConcurrentHashMap.SearchKeysTask<>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, searchFunction, result).fork();
               }

               while (result.get() == null) {
                  Int2ObjectConcurrentHashMap.Node<V> p;
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

   protected static final class SearchMappingsTask<V, U> extends Int2ObjectConcurrentHashMap.BulkTask<V, U> {
      public final Int2ObjectConcurrentHashMap.IntObjFunction<? super V, ? extends U> searchFunction;
      public final AtomicReference<U> result;

      public SearchMappingsTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
         Int2ObjectConcurrentHashMap.IntObjFunction<? super V, ? extends U> searchFunction,
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
         Int2ObjectConcurrentHashMap.IntObjFunction<? super V, ? extends U> searchFunction = this.searchFunction;
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
                  new Int2ObjectConcurrentHashMap.SearchMappingsTask<>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, searchFunction, result).fork();
               }

               while (result.get() == null) {
                  Int2ObjectConcurrentHashMap.Node<V> p;
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

   protected static final class SearchValuesTask<V, U> extends Int2ObjectConcurrentHashMap.BulkTask<V, U> {
      public final Function<? super V, ? extends U> searchFunction;
      public final AtomicReference<U> result;

      public SearchValuesTask(
         Int2ObjectConcurrentHashMap.BulkTask<V, ?> p,
         int b,
         int i,
         int f,
         Int2ObjectConcurrentHashMap.Node<V>[] t,
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
                  new Int2ObjectConcurrentHashMap.SearchValuesTask<>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, searchFunction, result).fork();
               }

               while (result.get() == null) {
                  Int2ObjectConcurrentHashMap.Node<V> p;
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
      public Int2ObjectConcurrentHashMap.Node<V>[] tab;
      public Int2ObjectConcurrentHashMap.TableStack<V> next;

      public TableStack() {
      }
   }

   @FunctionalInterface
   public interface ToDoubleIntObjFunction<V> {
      double applyAsDouble(int var1, V var2);
   }

   @FunctionalInterface
   public interface ToIntFunction<T> {
      int applyAsInt(T var1);
   }

   @FunctionalInterface
   public interface ToIntIntObjFunction<V> {
      int applyAsInt(int var1, V var2);
   }

   @FunctionalInterface
   public interface ToLongIntObjFunction<V> {
      long applyAsLong(int var1, V var2);
   }

   protected static class Traverser<V> {
      public Int2ObjectConcurrentHashMap.Node<V>[] tab;
      public Int2ObjectConcurrentHashMap.Node<V> next;
      public Int2ObjectConcurrentHashMap.TableStack<V> stack;
      public Int2ObjectConcurrentHashMap.TableStack<V> spare;
      public int index;
      public int baseIndex;
      public int baseLimit;
      public final int baseSize;

      public Traverser(Int2ObjectConcurrentHashMap.Node<V>[] tab, int size, int index, int limit) {
         this.tab = tab;
         this.baseSize = size;
         this.baseIndex = this.index = index;
         this.baseLimit = limit;
         this.next = null;
      }

      protected final Int2ObjectConcurrentHashMap.Node<V> advance() {
         Int2ObjectConcurrentHashMap.Node<V> e = this.next;
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

            Int2ObjectConcurrentHashMap.Node<V>[] t = this.tab;
            if (this.tab == null) {
               break;
            }

            int n;
            int var10000 = n = t.length;
            int i = this.index;
            if (var10000 <= this.index || i < 0) {
               break;
            }

            if ((e = Int2ObjectConcurrentHashMap.tabAt(t, i)) != null && e.hash < 0) {
               if (e instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                  this.tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)e).nextTable;
                  e = null;
                  this.pushState(t, i, n);
                  continue;
               }

               if (e instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                  e = ((Int2ObjectConcurrentHashMap.TreeBin)e).first;
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

      protected void pushState(Int2ObjectConcurrentHashMap.Node<V>[] t, int i, int n) {
         Int2ObjectConcurrentHashMap.TableStack<V> s = this.spare;
         if (s != null) {
            this.spare = s.next;
         } else {
            s = new Int2ObjectConcurrentHashMap.TableStack<>();
         }

         s.tab = t;
         s.length = n;
         s.index = i;
         s.next = this.stack;
         this.stack = s;
      }

      protected void recoverState(int n) {
         while (true) {
            Int2ObjectConcurrentHashMap.TableStack<V> s = this.stack;
            if (this.stack != null) {
               int len = s.length;
               if ((this.index = this.index + s.length) >= n) {
                  n = len;
                  this.index = s.index;
                  this.tab = s.tab;
                  s.tab = null;
                  Int2ObjectConcurrentHashMap.TableStack<V> next = s.next;
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

   protected static final class TreeBin<V> extends Int2ObjectConcurrentHashMap.Node<V> {
      public Int2ObjectConcurrentHashMap.TreeNode<V> root;
      public volatile Int2ObjectConcurrentHashMap.TreeNode<V> first;
      public volatile Thread waiter;
      public volatile int lockState;
      public static final int WRITER = 1;
      public static final int WAITER = 2;
      public static final int READER = 4;
      protected static final Unsafe U;
      protected static final long LOCKSTATE;

      protected int tieBreakOrder(int a, int b) {
         int comp = Integer.compare(a, b);
         return comp > 0 ? 1 : -1;
      }

      public TreeBin(int empty, Int2ObjectConcurrentHashMap.TreeNode<V> b) {
         super(empty, -2, empty, null, null);
         this.first = b;
         Int2ObjectConcurrentHashMap.TreeNode<V> r = null;
         Int2ObjectConcurrentHashMap.TreeNode<V> x = b;

         while (x != null) {
            Int2ObjectConcurrentHashMap.TreeNode<V> next = (Int2ObjectConcurrentHashMap.TreeNode<V>)x.next;
            x.left = x.right = null;
            if (r == null) {
               x.parent = null;
               x.red = false;
               r = x;
            } else {
               int k = x.key;
               int h = x.hash;
               Class<?> kc = null;
               Int2ObjectConcurrentHashMap.TreeNode<V> p = r;

               int dir;
               Int2ObjectConcurrentHashMap.TreeNode<V> xp;
               do {
                  int pk = p.key;
                  int ph = p.hash;
                  if (p.hash > h) {
                     dir = -1;
                  } else if (ph < h) {
                     dir = 1;
                  } else if ((dir = Integer.compare(k, pk)) == 0) {
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
      protected final Int2ObjectConcurrentHashMap.Node<V> find(int h, int k) {
         if (k != this.EMPTY) {
            Int2ObjectConcurrentHashMap.Node<V> e = this.first;

            while (e != null) {
               int s = this.lockState;
               if ((this.lockState & 3) != 0) {
                  if (e.hash == h) {
                     int ek = e.key;
                     if (e.key == k || ek != this.EMPTY && k == ek) {
                        return e;
                     }
                  }

                  e = e.next;
               } else if (U.compareAndSwapInt(this, LOCKSTATE, s, s + 4)) {
                  Int2ObjectConcurrentHashMap.TreeNode<V> p;
                  try {
                     Int2ObjectConcurrentHashMap.TreeNode<V> r = this.root;
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

      protected final Int2ObjectConcurrentHashMap.TreeNode<V> putTreeVal(int h, int k, V v) {
         Class<?> kc = null;
         boolean searched = false;
         Int2ObjectConcurrentHashMap.TreeNode<V> p = this.root;

         while (true) {
            if (p == null) {
               this.first = this.root = new Int2ObjectConcurrentHashMap.TreeNode<>(this.EMPTY, h, k, v, null, null);
            } else {
               int ph = p.hash;
               int dir;
               if (p.hash > h) {
                  dir = -1;
               } else if (ph < h) {
                  dir = 1;
               } else {
                  int pk = p.key;
                  if (p.key == k || pk != this.EMPTY && k == pk) {
                     return p;
                  }

                  if ((dir = Integer.compare(k, pk)) == 0) {
                     if (!searched) {
                        searched = true;
                        Int2ObjectConcurrentHashMap.TreeNode<V> ch = p.left;
                        Int2ObjectConcurrentHashMap.TreeNode<V> q;
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

               Int2ObjectConcurrentHashMap.TreeNode<V> xp = p;
               if ((p = dir <= 0 ? p.left : p.right) != null) {
                  continue;
               }

               Int2ObjectConcurrentHashMap.TreeNode<V> f = this.first;
               Int2ObjectConcurrentHashMap.TreeNode<V> x;
               this.first = x = new Int2ObjectConcurrentHashMap.TreeNode<>(this.EMPTY, h, k, v, f, xp);
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

      protected final boolean removeTreeNode(Int2ObjectConcurrentHashMap.TreeNode<V> p) {
         Int2ObjectConcurrentHashMap.TreeNode<V> next = (Int2ObjectConcurrentHashMap.TreeNode<V>)p.next;
         Int2ObjectConcurrentHashMap.TreeNode<V> pred = p.prev;
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
            Int2ObjectConcurrentHashMap.TreeNode<V> r = this.root;
            if (this.root != null && r.right != null) {
               Int2ObjectConcurrentHashMap.TreeNode<V> rl = r.left;
               if (r.left != null && rl.left != null) {
                  this.lockRoot();

                  try {
                     Int2ObjectConcurrentHashMap.TreeNode<V> pl = p.left;
                     Int2ObjectConcurrentHashMap.TreeNode<V> pr = p.right;
                     Int2ObjectConcurrentHashMap.TreeNode<V> replacement;
                     if (pl != null && pr != null) {
                        Int2ObjectConcurrentHashMap.TreeNode<V> s = pr;

                        while (true) {
                           Int2ObjectConcurrentHashMap.TreeNode<V> sl = s.left;
                           if (s.left == null) {
                              boolean c = s.red;
                              s.red = p.red;
                              p.red = c;
                              Int2ObjectConcurrentHashMap.TreeNode<V> sr = s.right;
                              Int2ObjectConcurrentHashMap.TreeNode<V> pp = p.parent;
                              if (s == pr) {
                                 p.parent = s;
                                 s.right = p;
                              } else {
                                 Int2ObjectConcurrentHashMap.TreeNode<V> sp = s.parent;
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
                        Int2ObjectConcurrentHashMap.TreeNode<V> ppx = replacement.parent = p.parent;
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
                        Int2ObjectConcurrentHashMap.TreeNode<V> ppx = p.parent;
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

      protected <V> Int2ObjectConcurrentHashMap.TreeNode<V> rotateLeft(Int2ObjectConcurrentHashMap.TreeNode<V> root, Int2ObjectConcurrentHashMap.TreeNode<V> p) {
         if (p != null) {
            Int2ObjectConcurrentHashMap.TreeNode<V> r = p.right;
            if (p.right != null) {
               Int2ObjectConcurrentHashMap.TreeNode<V> rl;
               if ((rl = p.right = r.left) != null) {
                  rl.parent = p;
               }

               Int2ObjectConcurrentHashMap.TreeNode<V> pp;
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

      protected <V> Int2ObjectConcurrentHashMap.TreeNode<V> rotateRight(Int2ObjectConcurrentHashMap.TreeNode<V> root, Int2ObjectConcurrentHashMap.TreeNode<V> p) {
         if (p != null) {
            Int2ObjectConcurrentHashMap.TreeNode<V> l = p.left;
            if (p.left != null) {
               Int2ObjectConcurrentHashMap.TreeNode<V> lr;
               if ((lr = p.left = l.right) != null) {
                  lr.parent = p;
               }

               Int2ObjectConcurrentHashMap.TreeNode<V> pp;
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

      protected <V> Int2ObjectConcurrentHashMap.TreeNode<V> balanceInsertion(
         Int2ObjectConcurrentHashMap.TreeNode<V> root, Int2ObjectConcurrentHashMap.TreeNode<V> x
      ) {
         x.red = true;

         while (true) {
            Int2ObjectConcurrentHashMap.TreeNode<V> xp = x.parent;
            if (x.parent == null) {
               x.red = false;
               return x;
            }

            if (!xp.red) {
               break;
            }

            Int2ObjectConcurrentHashMap.TreeNode<V> xpp = xp.parent;
            if (xp.parent == null) {
               break;
            }

            Int2ObjectConcurrentHashMap.TreeNode<V> xppl = xpp.left;
            if (xp == xpp.left) {
               Int2ObjectConcurrentHashMap.TreeNode<V> xppr = xpp.right;
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

      protected <V> Int2ObjectConcurrentHashMap.TreeNode<V> balanceDeletion(
         Int2ObjectConcurrentHashMap.TreeNode<V> root, Int2ObjectConcurrentHashMap.TreeNode<V> x
      ) {
         while (x != null && x != root) {
            Int2ObjectConcurrentHashMap.TreeNode<V> xp = x.parent;
            if (x.parent == null) {
               x.red = false;
               return x;
            }

            if (x.red) {
               x.red = false;
               return root;
            }

            Int2ObjectConcurrentHashMap.TreeNode<V> xpl = xp.left;
            if (xp.left == x) {
               Int2ObjectConcurrentHashMap.TreeNode<V> xpr = xp.right;
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
                  Int2ObjectConcurrentHashMap.TreeNode<V> sl = xpr.left;
                  Int2ObjectConcurrentHashMap.TreeNode<V> sr = xpr.right;
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
                  Int2ObjectConcurrentHashMap.TreeNode<V> sl = xpl.left;
                  Int2ObjectConcurrentHashMap.TreeNode<V> sr = xpl.right;
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

      protected <V> boolean checkInvariants(Int2ObjectConcurrentHashMap.TreeNode<V> t) {
         Int2ObjectConcurrentHashMap.TreeNode<V> tp = t.parent;
         Int2ObjectConcurrentHashMap.TreeNode<V> tl = t.left;
         Int2ObjectConcurrentHashMap.TreeNode<V> tr = t.right;
         Int2ObjectConcurrentHashMap.TreeNode<V> tb = t.prev;
         Int2ObjectConcurrentHashMap.TreeNode<V> tn = (Int2ObjectConcurrentHashMap.TreeNode<V>)t.next;
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
            Class<?> k = Int2ObjectConcurrentHashMap.TreeBin.class;
            LOCKSTATE = U.objectFieldOffset(k.getDeclaredField("lockState"));
         } catch (Exception var2) {
            throw new Error(var2);
         }
      }
   }

   protected static final class TreeNode<V> extends Int2ObjectConcurrentHashMap.Node<V> {
      public Int2ObjectConcurrentHashMap.TreeNode<V> parent;
      public Int2ObjectConcurrentHashMap.TreeNode<V> left;
      public Int2ObjectConcurrentHashMap.TreeNode<V> right;
      public Int2ObjectConcurrentHashMap.TreeNode<V> prev;
      public boolean red;

      public TreeNode(int empty, int hash, int key, V val, Int2ObjectConcurrentHashMap.Node<V> next, Int2ObjectConcurrentHashMap.TreeNode<V> parent) {
         super(empty, hash, key, val, next);
         this.parent = parent;
      }

      @Override
      protected Int2ObjectConcurrentHashMap.Node<V> find(int h, int k) {
         return this.findTreeNode(h, k, null);
      }

      protected final Int2ObjectConcurrentHashMap.TreeNode<V> findTreeNode(int h, int k, Class<?> kc) {
         if (k != this.EMPTY) {
            Int2ObjectConcurrentHashMap.TreeNode<V> p = this;

            do {
               Int2ObjectConcurrentHashMap.TreeNode<V> pl = p.left;
               Int2ObjectConcurrentHashMap.TreeNode<V> pr = p.right;
               int ph = p.hash;
               if (p.hash > h) {
                  p = pl;
               } else if (ph < h) {
                  p = pr;
               } else {
                  int pk = p.key;
                  if (p.key == k || pk != this.EMPTY && k == pk) {
                     return p;
                  }

                  if (pl == null) {
                     p = pr;
                  } else if (pr == null) {
                     p = pl;
                  } else {
                     int dir;
                     if ((dir = Integer.compare(k, pk)) != 0) {
                        p = dir < 0 ? pl : pr;
                     } else {
                        Int2ObjectConcurrentHashMap.TreeNode<V> q;
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

   protected static final class ValueIterator<V> extends Int2ObjectConcurrentHashMap.BaseIterator<V> implements ObjectIterator<V>, Enumeration<V> {
      public ValueIterator(Int2ObjectConcurrentHashMap.Node<V>[] tab, int index, int size, int limit, Int2ObjectConcurrentHashMap<V> map) {
         super(tab, index, size, limit, map);
      }

      public final V next() {
         Int2ObjectConcurrentHashMap.Node<V> p = this.next;
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

   protected static final class ValueSpliterator<V> extends Int2ObjectConcurrentHashMap.Traverser<V> implements ObjectSpliterator<V> {
      public long est;

      public ValueSpliterator(Int2ObjectConcurrentHashMap.Node<V>[] tab, int size, int index, int limit, long est) {
         super(tab, size, index, limit);
         this.est = est;
      }

      public ObjectSpliterator<V> trySplit() {
         int i = this.baseIndex;
         int f = this.baseLimit;
         int h;
         return (h = this.baseIndex + this.baseLimit >>> 1) <= i
            ? null
            : new Int2ObjectConcurrentHashMap.ValueSpliterator<>(this.tab, this.baseSize, this.baseLimit = h, f, this.est >>>= 1);
      }

      public void forEachRemaining(Consumer<? super V> action) {
         if (action == null) {
            throw new NullPointerException();
         } else {
            Int2ObjectConcurrentHashMap.Node<V> p;
            while ((p = this.advance()) != null) {
               action.accept(p.val);
            }
         }
      }

      public boolean tryAdvance(Consumer<? super V> action) {
         if (action == null) {
            throw new NullPointerException();
         } else {
            Int2ObjectConcurrentHashMap.Node<V> p;
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

   protected static final class ValuesView<V> extends Int2ObjectConcurrentHashMap.CollectionView<V, V> implements FastCollection<V>, Serializable {
      public static final long serialVersionUID = 2249069246763182397L;

      public ValuesView(Int2ObjectConcurrentHashMap<V> map) {
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
         Int2ObjectConcurrentHashMap<V> m = this.map;
         Int2ObjectConcurrentHashMap.Node<V>[] t = m.table;
         int f = m.table == null ? 0 : t.length;
         return new Int2ObjectConcurrentHashMap.ValueIterator<>(t, f, 0, f, m);
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
         Int2ObjectConcurrentHashMap<V> m = this.map;
         long n = m.sumCount();
         Int2ObjectConcurrentHashMap.Node<V>[] t = m.table;
         int f = m.table == null ? 0 : t.length;
         return new Int2ObjectConcurrentHashMap.ValueSpliterator<>(t, f, 0, f, n < 0L ? 0L : n);
      }

      @Override
      public void forEach(Consumer<? super V> action) {
         if (action == null) {
            throw new NullPointerException();
         } else {
            Int2ObjectConcurrentHashMap.Node<V>[] tt = this.map.table;
            if (this.map.table != null) {
               Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
               Int2ObjectConcurrentHashMap.Node<V> next = null;
               Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
               Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
               int index = 0;
               int baseIndex = 0;
               int baseLimit = tt.length;
               int baseSize = tt.length;

               while (true) {
                  Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                        Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                        int n;
                        if (tab == null || (n = tab.length) <= index || index < 0) {
                           break;
                        }

                        if ((p = Int2ObjectConcurrentHashMap.tabAt(tab, index)) != null && p.hash < 0) {
                           if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                              tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                              p = null;
                              Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                              if (spare != null) {
                                 spare = spare.next;
                              } else {
                                 s = new Int2ObjectConcurrentHashMap.TableStack<>();
                              }

                              s.tab = t;
                              s.length = n;
                              s.index = index;
                              s.next = stack;
                              stack = s;
                              continue;
                           }

                           if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                              p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                              Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                              if (stack != null) {
                                 int len = stack.length;
                                 if ((index += stack.length) >= n) {
                                    n = len;
                                    index = stack.index;
                                    tab = stack.tab;
                                    stack.tab = null;
                                    Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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
            Int2ObjectConcurrentHashMap.Node<V>[] tt = this.map.table;
            if (this.map.table != null) {
               Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
               Int2ObjectConcurrentHashMap.Node<V> next = null;
               Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
               Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
               int index = 0;
               int baseIndex = 0;
               int baseLimit = tt.length;
               int baseSize = tt.length;

               while (true) {
                  Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                        Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                        int n;
                        if (tab == null || (n = tab.length) <= index || index < 0) {
                           break;
                        }

                        if ((p = Int2ObjectConcurrentHashMap.tabAt(tab, index)) != null && p.hash < 0) {
                           if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                              tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                              p = null;
                              Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                              if (spare != null) {
                                 spare = spare.next;
                              } else {
                                 s = new Int2ObjectConcurrentHashMap.TableStack<>();
                              }

                              s.tab = t;
                              s.length = n;
                              s.index = index;
                              s.next = stack;
                              stack = s;
                              continue;
                           }

                           if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                              p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                              Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                              if (stack != null) {
                                 int len = stack.length;
                                 if ((index += stack.length) >= n) {
                                    n = len;
                                    index = stack.index;
                                    tab = stack.tab;
                                    stack.tab = null;
                                    Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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
            Int2ObjectConcurrentHashMap.Node<V>[] tt = this.map.table;
            if (this.map.table != null) {
               Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
               Int2ObjectConcurrentHashMap.Node<V> next = null;
               Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
               Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
               int index = 0;
               int baseIndex = 0;
               int baseLimit = tt.length;
               int baseSize = tt.length;

               while (true) {
                  Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                        Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                        int n;
                        if (tab == null || (n = tab.length) <= index || index < 0) {
                           break;
                        }

                        if ((p = Int2ObjectConcurrentHashMap.tabAt(tab, index)) != null && p.hash < 0) {
                           if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                              tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                              p = null;
                              Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                              if (spare != null) {
                                 spare = spare.next;
                              } else {
                                 s = new Int2ObjectConcurrentHashMap.TableStack<>();
                              }

                              s.tab = t;
                              s.length = n;
                              s.index = index;
                              s.next = stack;
                              stack = s;
                              continue;
                           }

                           if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                              p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                              Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                              if (stack != null) {
                                 int len = stack.length;
                                 if ((index += stack.length) >= n) {
                                    n = len;
                                    index = stack.index;
                                    tab = stack.tab;
                                    stack.tab = null;
                                    Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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
            Int2ObjectConcurrentHashMap.Node<V>[] tt = this.map.table;
            if (this.map.table != null) {
               Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
               Int2ObjectConcurrentHashMap.Node<V> next = null;
               Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
               Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
               int index = 0;
               int baseIndex = 0;
               int baseLimit = tt.length;
               int baseSize = tt.length;

               while (true) {
                  Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                        Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                        int n;
                        if (tab == null || (n = tab.length) <= index || index < 0) {
                           break;
                        }

                        if ((p = Int2ObjectConcurrentHashMap.tabAt(tab, index)) != null && p.hash < 0) {
                           if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                              tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                              p = null;
                              Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                              if (spare != null) {
                                 spare = spare.next;
                              } else {
                                 s = new Int2ObjectConcurrentHashMap.TableStack<>();
                              }

                              s.tab = t;
                              s.length = n;
                              s.index = index;
                              s.next = stack;
                              stack = s;
                              continue;
                           }

                           if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                              p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                              Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                              if (stack != null) {
                                 int len = stack.length;
                                 if ((index += stack.length) >= n) {
                                    n = len;
                                    index = stack.index;
                                    tab = stack.tab;
                                    stack.tab = null;
                                    Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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
            Int2ObjectConcurrentHashMap.Node<V>[] tt = this.map.table;
            if (this.map.table != null) {
               Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
               Int2ObjectConcurrentHashMap.Node<V> next = null;
               Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
               Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
               int index = 0;
               int baseIndex = 0;
               int baseLimit = tt.length;
               int baseSize = tt.length;

               while (true) {
                  Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                        Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                        int n;
                        if (tab == null || (n = tab.length) <= index || index < 0) {
                           break;
                        }

                        if ((p = Int2ObjectConcurrentHashMap.tabAt(tab, index)) != null && p.hash < 0) {
                           if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                              tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                              p = null;
                              Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                              if (spare != null) {
                                 spare = spare.next;
                              } else {
                                 s = new Int2ObjectConcurrentHashMap.TableStack<>();
                              }

                              s.tab = t;
                              s.length = n;
                              s.index = index;
                              s.next = stack;
                              stack = s;
                              continue;
                           }

                           if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                              p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                              Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                              if (stack != null) {
                                 int len = stack.length;
                                 if ((index += stack.length) >= n) {
                                    n = len;
                                    index = stack.index;
                                    tab = stack.tab;
                                    stack.tab = null;
                                    Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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
            Int2ObjectConcurrentHashMap.Node<V>[] tt = this.map.table;
            if (this.map.table != null) {
               Int2ObjectConcurrentHashMap.Node<V>[] tab = tt;
               Int2ObjectConcurrentHashMap.Node<V> next = null;
               Int2ObjectConcurrentHashMap.TableStack<V> stack = null;
               Int2ObjectConcurrentHashMap.TableStack<V> spare = null;
               int index = 0;
               int baseIndex = 0;
               int baseLimit = tt.length;
               int baseSize = tt.length;

               while (true) {
                  Int2ObjectConcurrentHashMap.Node<V> p = null;
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

                        Int2ObjectConcurrentHashMap.Node<V>[] t = tab;
                        int n;
                        if (tab == null || (n = tab.length) <= index || index < 0) {
                           break;
                        }

                        if ((p = Int2ObjectConcurrentHashMap.tabAt(tab, index)) != null && p.hash < 0) {
                           if (p instanceof Int2ObjectConcurrentHashMap.ForwardingNode) {
                              tab = ((Int2ObjectConcurrentHashMap.ForwardingNode)p).nextTable;
                              p = null;
                              Int2ObjectConcurrentHashMap.TableStack<V> s = spare;
                              if (spare != null) {
                                 spare = spare.next;
                              } else {
                                 s = new Int2ObjectConcurrentHashMap.TableStack<>();
                              }

                              s.tab = t;
                              s.length = n;
                              s.index = index;
                              s.next = stack;
                              stack = s;
                              continue;
                           }

                           if (p instanceof Int2ObjectConcurrentHashMap.TreeBin) {
                              p = ((Int2ObjectConcurrentHashMap.TreeBin)p).first;
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
                              Int2ObjectConcurrentHashMap.TableStack<V> s = stack;
                              if (stack != null) {
                                 int len = stack.length;
                                 if ((index += stack.length) >= n) {
                                    n = len;
                                    index = stack.index;
                                    tab = stack.tab;
                                    stack.tab = null;
                                    Int2ObjectConcurrentHashMap.TableStack<V> anext = stack.next;
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
