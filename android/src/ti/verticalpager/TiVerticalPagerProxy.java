package ti.verticalpager;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
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
    public TiUIView createView(android.app.Activity activity) {
        return new TiVerticalPagerView(this);
    }

    @Kroll.setProperty
    public void setViews(Object[] views) {
        synchronized (viewProxies) {
            for (TiViewProxy proxy : viewProxies) {
                if (proxy.getParent() == this) {
                    proxy.setParent(null);
                }
            }

            viewProxies.clear();

            if (views != null) {
                for (Object obj : views) {
                    if (obj instanceof TiViewProxy) {
                        viewProxies.add((TiViewProxy) obj);
                    }
                }
            }
        }

        TiVerticalPagerView view = (TiVerticalPagerView) peekView();
        if (view != null) {
            view.reloadData();
        }
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
            fireEvent("scrollstart", data);
        }
    }

    public void fireScrollEvent(int currentPageEstimate, float offset) {
        if (hasListeners("scroll")) {
            KrollDict data = new KrollDict();
            data.put("currentPage", currentPageEstimate);
            data.put("offset", offset);
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
