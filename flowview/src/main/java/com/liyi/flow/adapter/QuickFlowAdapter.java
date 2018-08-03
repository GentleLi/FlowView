package com.liyi.flow.adapter;


import android.support.annotation.LayoutRes;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


public abstract class QuickFlowAdapter<T, K extends BaseFlowHolder> extends BaseFlowAdapter<T> {
    private final int LAYOUT_NOT_FOUND = -404;

    private LayoutInflater mLayoutInflater;
    private SparseIntArray mLayoutTypes;


    public QuickFlowAdapter() {

    }

    /**
     * 添加不同类型的 item 布局
     *
     * @param type     此处的 type 必须与 getItemViewType() 中返回的 type 对应
     * @param layoutId
     */
    protected void addItemType(int type, @LayoutRes int layoutId) {
        if (mLayoutTypes == null) {
            mLayoutTypes = new SparseIntArray();
        }
        mLayoutTypes.put(type, layoutId);
    }

    @Override
    public int getItemViewType(int position) {
        return onHandleViewType(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mLayoutInflater == null) mLayoutInflater = LayoutInflater.from(parent.getContext());
        int viewType = getItemViewType(position);
        T item = mData.get(position);
        K holder = null;
        if (convertView == null) {
            convertView = getItemView(getLayoutId(viewType), parent);
            holder = createBaseItemHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (K) convertView.getTag();
        }
        holder.setViewType(viewType);
        onHandleView(position, holder, item);
        return convertView;
    }

    protected abstract int onHandleViewType(int position);

    protected abstract void onHandleView(int position, K holder, T item);

    protected View getItemView(int layoutResId, ViewGroup parent) {
        if (layoutResId != LAYOUT_NOT_FOUND) {
            return mLayoutInflater.inflate(layoutResId, parent, false);
        }
        return null;
    }

    private int getLayoutId(int vieType) {
        return mLayoutTypes.get(vieType, LAYOUT_NOT_FOUND);
    }


    protected K createBaseItemHolder(View view) {
        Class temp = getClass();
        Class z = null;
        while (z == null && null != temp) {
            z = getInstancedGenericKClass(temp);
            temp = temp.getSuperclass();
        }
        K k;
        // 泛型擦除会导致 z 为 null
        if (z == null) {
            k = (K) new BaseFlowHolder(view);
        } else {
            k = createGenericKInstance(z, view);
        }
        return k != null ? k : (K) new BaseFlowHolder(view);
    }

    @SuppressWarnings("unchecked")
    private K createGenericKInstance(Class z, View view) {
        try {
            Constructor constructor;
            // inner and unstatic class
            if (z.isMemberClass() && !Modifier.isStatic(z.getModifiers())) {
                constructor = z.getDeclaredConstructor(getClass(), View.class);
                constructor.setAccessible(true);
                return (K) constructor.newInstance(this, view);
            } else {
                constructor = z.getDeclaredConstructor(View.class);
                constructor.setAccessible(true);
                return (K) constructor.newInstance(view);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Class getInstancedGenericKClass(Class z) {
        Type type = z.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            for (Type temp : types) {
                if (temp instanceof Class) {
                    Class tempClass = (Class) temp;
                    if (BaseFlowHolder.class.isAssignableFrom(tempClass)) {
                        return tempClass;
                    }
                } else if (temp instanceof ParameterizedType) {
                    Type rawType = ((ParameterizedType) temp).getRawType();
                    if (rawType instanceof Class && BaseFlowHolder.class.isAssignableFrom((Class<?>) rawType)) {
                        return (Class<?>) rawType;
                    }
                }
            }
        }
        return null;
    }
}