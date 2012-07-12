package org.droidkit.cachekit;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.WeakHashMap;

import org.droidkit.DroidKit;
import org.droidkit.cachekit.CacheManager.CacheInterface;
import org.droidkit.util.tricks.CLog;

/** 
 * The purpose of this class is to provide provide a cache where each cached object is bound to a weakly referenced 
 * binder object. Everytime the cache is cleaned (as opposed to cleared), if any of the binder objects dissapear (due to 
 * lack of strong references) their associated cached objects will be cleared as well (assuming they're not bound to another
 * binder object thats still around). Most importantly, there is a callback for when an object is about to get unbound.
 * 
 * 
 * This will be the base class for a new bitmap cache that binds itself to weakly reference image views. 
 * 
 * Rule of thumb: a cached object can be bound to many binders, but a binder can only be bound to a single cached object.
 * Then you try to re-bind a binder to a new object, the original binding will be destroyed.
 * @author ghackett
 *
 * @param <K> they type of key
 * @param <B> the type of the binder object
 * @param <C> the type of the cached object
 */
public class BoundCache<K, B, C> implements CacheInterface {
    

    protected HashMap<C, LinkedList<WeakReference<B>>> mObjectBindings;
    protected WeakHashMap<B, C> mBinders;
    protected HashMap<K, C> mKeyMap;
    protected HashMap<C, K> mCacheMap;
    
    public BoundCache() {
        mObjectBindings = new HashMap<C, LinkedList<WeakReference<B>>>();
        mBinders = new WeakHashMap<B, C>();
        mCacheMap = new HashMap<C, K>();
        mKeyMap = new HashMap<K, C>();
        CacheManager.registerCache(this);
    }
    
    public synchronized void put(K key, B binder, C object, boolean cleanOldObject) {
        if (key == null || binder == null || object == null)
            throw new NullPointerException("cant use any nulls in " + this.getClass().getSimpleName() + ".put()");
        mKeyMap.put(key, object);
        mCacheMap.put(object, key);
        C oldObject = mBinders.put(binder, object);
        if (oldObject != null && oldObject == object) {
            return;
        }
            
            
        
        if (mObjectBindings.containsKey(object)) {
            mObjectBindings.get(object).add(new WeakReference<B>(binder));
        } else {
            LinkedList<WeakReference<B>> binderList = new LinkedList<WeakReference<B>>();
            binderList.add(new WeakReference<B>(binder));
            mObjectBindings.put(object, binderList);
        }
        
//        if (DroidKit.DEBUG) CLog.v("calling remove from put " + key.toString());
        removeBinderFromObject(oldObject, binder, cleanOldObject);
    }
    
    private void removeBinderFromObject(C oldObject, B binder, boolean cleanOldObject) {
        if (oldObject != null) {
            LinkedList<WeakReference<B>> bindings = mObjectBindings.get(oldObject);
            
            if (bindings != null) {
                WeakReference<B> bindingToRemove = null;
                for (WeakReference<B> ref : bindings) {
                    B b = ref.get();
                    if (b == binder) {
                        bindingToRemove = ref;
                    }
                }
                if (bindingToRemove != null)
                    bindings.remove(bindingToRemove);
//                if (bindings.isEmpty()) {
//                    mObjectBindings.remove(oldObject);
//                    if (cleanOldObject)
//                        onObjectUnbound(oldObject);
//                    return;
//                }
            }
            

            
            if (cleanOldObject) {
                if (bindings != null && !bindings.isEmpty()) {
                    LinkedList<WeakReference<B>> bindingsToRemove = new LinkedList<WeakReference<B>>();
                    for (WeakReference<B> b : bindings) {
                        if (b.get() == null)
                            bindingsToRemove.add(b);
                    }
                    bindings.removeAll(bindingsToRemove);
                }
                if (bindings == null || bindings.isEmpty()) {
                    mObjectBindings.remove(oldObject);
                    onObjectUnbound(oldObject);
                }
            }
//            if (DroidKit.DEBUG) CLog.v("rbfo Holding onto " + mCacheMap.keySet().size() + " cached objects with " + mBinders.keySet().size() + " binders");
        }
        
        
//        new Exception().printStackTrace();
    }
    

    public synchronized C bind(K key, B binder, boolean cleanOldObject) {
        C obj = mKeyMap.get(key);

        if (obj != null) {
//            if (DroidKit.DEBUG) CLog.v("calling put from bind " + key.toString());
            put(key, binder, obj, cleanOldObject);
        } else {
            C oldObject = mBinders.remove(binder);
            if (oldObject != null) {
//                if (DroidKit.DEBUG) CLog.v("didnt find object for key, but found one for the binder, so lets remove the binding");
                if (DroidKit.DEBUG) CLog.v("calling remove from bind " + key.toString());
                removeBinderFromObject(oldObject, binder, cleanOldObject);
            }
        }
        return obj;
    }
    
    public void destroyBinder(B binder, boolean cleanOldObject) {
        if (binder == null)
            return;
        C oldObject = mBinders.remove(binder);
        if (oldObject != null) {
            LinkedList<WeakReference<B>> bindings = mObjectBindings.get(oldObject);
            WeakReference<B> bindingToRemove = null;
            for (WeakReference<B> ref : bindings) {
                B b = ref.get();
                if (b != null && b == binder) {
                    bindingToRemove = ref;
                    break;
                }
            }
            if (bindingToRemove != null) {
                bindings.remove(bindingToRemove);
                bindingToRemove.clear();
            }
            
            if (cleanOldObject) {
                if (bindings == null || bindings.isEmpty()) {
                    mObjectBindings.remove(oldObject);
                    onObjectUnbound(oldObject);
                }
            }
        }
        
        

    }
    
    public synchronized void cleanCache() {
        if (mObjectBindings.isEmpty()) {
            if (DroidKit.DEBUG) CLog.v("cc1 Holding onto " + mCacheMap.keySet().size() + " cached objects with " + mBinders.keySet().size() + " binders");
            return;
        }
        
        System.gc();
        
        LinkedList<C> unboundObjects = new LinkedList<C>();
        LinkedList<WeakReference<B>> bindingsToRemove = new LinkedList<WeakReference<B>>();
        for (C obj : mObjectBindings.keySet()) {
            LinkedList<WeakReference<B>> bindings = mObjectBindings.get(obj);
            bindingsToRemove.clear();
            
            if (bindings != null && !bindings.isEmpty()) {
                for (WeakReference<B> ref : bindings) {
                    if (ref.get() == null) {
                        bindingsToRemove.add(ref);
                    }
                }
                bindings.removeAll(bindingsToRemove);
            }
            if (bindings == null || bindings.isEmpty())
                unboundObjects.add(obj);

        }
        if (!unboundObjects.isEmpty()) {
            for (C obj : unboundObjects) {
                mObjectBindings.remove(obj);
                onObjectUnbound(obj);
            }
        }
        if (DroidKit.DEBUG) CLog.v("cc2 Holding onto " + mCacheMap.keySet().size() + " cached objects with " + mBinders.keySet().size() + " binders");
    }
    
    protected void onObjectUnbound(C object) {
        K key = mCacheMap.remove(object);
        if (key != null)
            mKeyMap.remove(key);
    }
    
    public synchronized void clearCache() {
        Set<C> objects = mObjectBindings.keySet();
        mObjectBindings.clear();
        mBinders.clear();
        mKeyMap.clear();
        mCacheMap.clear();
        if (objects != null) {
            for (C obj : objects)
                onObjectUnbound(obj);
        }
    }
}
