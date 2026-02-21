package ti.verticalpager;

import android.util.Log;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiUIView;

import java.util.ArrayList;
import java.util.List;

@Kroll.proxy(creatableInModule = TiVerticalPagerModule.class)
public class TiVerticalPagerProxy extends TiViewProxy{
    private static final String TAG = "TiVerticalPagerProxy";

    private final List<TiViewProxy> viewProxies = new ArrayList<>();
    private int currentPage = 0;
    private int cacheSize = 3;

    public TiVerticalPagerProxy() {
        super();
    }

    @Override
    public void handleCreationDict(KrollDict options) {
        super.handleCreationDict(options);

        Log.d(TAG, "handleCreationDict called with options: " + options);

        // Process views
        if (options.containsKey("views")) {
            Log.d(TAG, "Found 'views' in creation dict");
            Object viewsObj = options.get("views");
            setViews(viewsObj);
        }

        // Process cacheSize
        if (options.containsKey("cacheSize")) {
            Log.d(TAG, "Found 'cacheSize' in creation dict");
            int size = TiConvert.toInt(options.get("cacheSize"), 3);
            setCacheSize(size);
        }

        // Process pageIndicator
        if (options.containsKey("pageIndicator")) {
            Log.d(TAG, "Found 'pageIndicator' in creation dict");
            Object indicatorObj = options.get("pageIndicator");
            setPageIndicator(indicatorObj);
        }

        // Process pageIndicatorColor
        if (options.containsKey("pageIndicatorColor")) {
            Log.d(TAG, "Found 'pageIndicatorColor' in creation dict");
            String color = TiConvert.toString(options.get("pageIndicatorColor"));
            setPageIndicatorColor(color);
        }

        // Process currentPageIndicatorColor
        if (options.containsKey("currentPageIndicatorColor")) {
            Log.d(TAG, "Found 'currentPageIndicatorColor' in creation dict");
            String color = TiConvert.toString(options.get("currentPageIndicatorColor"));
            setCurrentPageIndicatorColor(color);
        }
    }

    @Override
    public TiUIView createView(android.app.Activity activity) {
        return new TiVerticalPagerView(this);
    }

    @Kroll.setProperty
    public void setViews(Object views) {
        Log.d(TAG, "setViews called");

        if (views == null) {
            Log.d(TAG, "Views is null");
            setViewsArray(null);
            return;
        }

        Log.d(TAG, "Views type: " + views.getClass().getName());

        Object[] viewArray = null;

        // Tenta converter para array
        try {
            if (views instanceof Object[]) {
                viewArray = (Object[]) views;
                Log.d(TAG, "Views is Object[] with length: " + viewArray.length);
            } else if (views.getClass().isArray()) {
                // Array nativo do Java
                int length = java.lang.reflect.Array.getLength(views);
                viewArray = new Object[length];
                for (int i = 0; i < length; i++) {
                    viewArray[i] = java.lang.reflect.Array.get(views, i);
                }
                Log.d(TAG, "Converted native array, length: " + length);
            } else {
                Log.w(TAG, "Views is not an array: " + views.getClass().getName());
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing views: " + e.getMessage(), e);
            return;
        }

        setViewsArray(viewArray);
    }

    private void setViewsArray(Object[] views) {
        Log.d(TAG, "setViewsArray called with " + (views != null ? views.length : 0) + " views");

        synchronized (viewProxies) {
            // Remove parent das views antigas
            for (TiViewProxy proxy : viewProxies) {
                if (proxy.getParent() == this) {
                    proxy.setParent(null);
                }
            }

            viewProxies.clear();

            if (views != null) {
                for (int i = 0; i < views.length; i++) {
                    Object obj = views[i];
                    Log.d(TAG, "Processing view " + i + ": " + (obj != null ? obj.getClass().getName() : "null"));

                    if (obj instanceof TiViewProxy) {
                        viewProxies.add((TiViewProxy) obj);
                        Log.d(TAG, "Added TiViewProxy");
                    } else {
                        Log.w(TAG, "Object is not TiViewProxy: " + (obj != null ? obj.getClass().getName() : "null"));
                    }
                }
            }

            Log.d(TAG, "Total views in proxy: " + viewProxies.size());
        }

        TiVerticalPagerView view = (TiVerticalPagerView) peekView();
        if (view != null) {
            Log.d(TAG, "View exists, calling reloadData()");
            view.reloadData();
        } else {
            Log.d(TAG, "View doesn't exist yet");
        }
    }

    private String getScrollDirectionString() {
        TiVerticalPagerView view = (TiVerticalPagerView) peekView();
        if (view != null) {
            int direction = view.getScrollDirection();
            if (direction > 0) {
                return "down";
            } else if (direction < 0) {
                return "up";
            }
        }
        return "none";
    }

    @Kroll.getProperty
    public Object[] getViews() {
        return viewProxies.toArray();
    }

    @Kroll.setProperty
    public void setCacheSize(int size) {
        this.cacheSize = Math.max(1, size);
    }

    @Kroll.getProperty
    public int getCacheSize() {
        return cacheSize;
    }

    @Kroll.getProperty
    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int page) {
        this.currentPage = page;
    }

    @Kroll.method
    public void scrollToPage(int page) {
        if (page >= 0 && page < viewProxies.size()) {
            currentPage = page;
            if (peekView() != null) {
                ((TiVerticalPagerView) peekView()).scrollToPage(page, true);
            }
        }
    }

    @Kroll.method
    public void addView(TiViewProxy viewProxy) {
        viewProxies.add(viewProxy);
        if (peekView() != null) {
            ((TiVerticalPagerView) peekView()).reloadData();
        }
    }

    @Kroll.method
    public void removeView(int index) {
        synchronized (viewProxies) {
            if (index >= 0 && index < viewProxies.size()) {
                TiViewProxy removedProxy = viewProxies.get(index);
                if (removedProxy.getParent() == this) {
                    removedProxy.setParent(null);
                }

                viewProxies.remove(index);
            }
        }

        TiVerticalPagerView view = (TiVerticalPagerView) peekView();
        if (view != null) {
            view.reloadData();
        }
    }

    @Kroll.method
    public void releaseFromMemory() {
        TiVerticalPagerView view = (TiVerticalPagerView) peekView();
        if (view != null) {
            view.manualRelease();
        }
    }

    @Kroll.setProperty
    public void setPageIndicator(Object config) {
        Log.d(TAG, "setPageIndicator called on PROXY");
        Log.d(TAG, "Config: " + config);
        Log.d(TAG, "Config class: " + (config != null ? config.getClass().getName() : "null"));

        TiVerticalPagerView view = (TiVerticalPagerView) peekView();
        if (view != null) {
            Log.d(TAG, "View exists, calling view.setPageIndicator()");
            view.setPageIndicator(config);
        } else {
            Log.d(TAG, "View doesn't exist, storing property");
            setProperty("pageIndicator", config);
        }
    }

    @Kroll.setProperty
    public void setPageIndicatorColor(String color) {
        TiVerticalPagerView view = (TiVerticalPagerView) peekView();
        if (view != null) {
            view.setPageIndicatorColor(color);
        } else {
            setProperty("pageIndicatorColor", color);
        }
    }

    @Kroll.setProperty
    public void setCurrentPageIndicatorColor(String color) {
        TiVerticalPagerView view = (TiVerticalPagerView) peekView();
        if (view != null) {
            view.setCurrentPageIndicatorColor(color);
        } else {
            setProperty("currentPageIndicatorColor", color);
        }
    }

    public List<TiViewProxy> getViewProxies() {
        synchronized (viewProxies) {
            return new ArrayList<>(viewProxies);
        }
    }

    public void fireChangeEvent(int newPage, int oldPage, TiViewProxy currentView, TiViewProxy previousView) {
        if (hasListeners(TiC.EVENT_CHANGE)) {
            KrollDict data = new KrollDict();
            data.put("currentPage", newPage);
            data.put("previousPage", oldPage);
            data.put("currentView", currentView);
            if (previousView != null) {
                data.put("previousView", previousView);
            }
            fireEvent(TiC.EVENT_CHANGE, data);
        }
    }

    public void fireScrollStartEvent() {
        if (hasListeners("scrollstart")) {
            KrollDict data = new KrollDict();
            data.put("currentPage", currentPage);
            data.put("direction", getScrollDirectionString());
            fireEvent("scrollstart", data);
        }
    }

    public void fireScrollEvent(int currentPageEstimate, float offset) {
        if (hasListeners("scroll")) {
            KrollDict data = new KrollDict();
            data.put("currentPage", currentPageEstimate);
            data.put("offset", offset);
            data.put("direction", getScrollDirectionString());
            fireEvent("scroll", data);
        }
    }

    public void fireScrollEndEvent() {
        if (hasListeners("scrollend")) {
            KrollDict data = new KrollDict();
            data.put("currentPage", currentPage);
            fireEvent("scrollend", data);
        }
    }

    @Override
    public void release() {
        synchronized (viewProxies) {
            for (TiViewProxy proxy : viewProxies) {
                if (proxy.getParent() == this) {
                    proxy.setParent(null);
                }
            }
            viewProxies.clear();
        }
        super.release();
    }
}
