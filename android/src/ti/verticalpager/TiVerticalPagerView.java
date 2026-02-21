package ti.verticalpager;

import android.os.Handler;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiUIView;

import java.util.Objects;

public class TiVerticalPagerView extends TiUIView {

    private static final String TAG = "TiVerticalPagerView";

    private int lastScrollDirection = 0; // -1 = up, 0 = none, 1 = down
    private int lastScrollPosition = 0;
    private Handler preloadHandler = new Handler(android.os.Looper.getMainLooper());
    private Runnable preloadRunnable;
    private boolean isPreloading = false;

    private ViewPager2 viewPager;
    private TiVerticalPagerAdapter adapter;
    private final TiVerticalPagerProxy pagerProxy;
    private View pageIndicatorView;
    private FrameLayout containerLayout;

    private TabLayoutMediator tabLayoutMediator;
    private ViewPager2.OnPageChangeCallback pageChangeCallback;

    private boolean isScrolling = false;
    private int indicatorType = 0; // 0 = horizontal, 1 = vertical
    private KrollDict pageIndicatorConfig;

    // Page indicator colors
    private int pageIndicatorColor = Color.parseColor("#80FFFFFF");
    private int currentPageIndicatorColor = Color.parseColor("#FFFFFFFF");

    public TiVerticalPagerView(TiVerticalPagerProxy proxy) {
        super(proxy);
        this.pagerProxy = proxy;

        Log.d(TAG, "VerticalpagerView constructor called");

        if (proxy.getActivity() == null) {
            Log.e(TAG, "Activity is null in constructor");
            return;
        }

        containerLayout = new FrameLayout(proxy.getActivity());
        setupViewPager();
        containerLayout.addView(viewPager);
        setNativeView(containerLayout);

        Log.d(TAG, "View setup complete, checking for initial properties");

        // Process views if they were set before view creation
        if (proxy.hasProperty("views")) {
            Log.d(TAG, "Has 'views' property, reloading data");
            reloadData();
        }

        // Process page indicator
        if (proxy.hasProperty("pageIndicator")) {
            Log.d(TAG, "Has 'pageIndicator' property, processing...");
            Object config = proxy.getProperty("pageIndicator");
            Log.d(TAG, "PageIndicator config: " + config);
            Log.d(TAG, "PageIndicator config class: " + (config != null ? config.getClass().getName() : "null"));
            setPageIndicator(config);
        } else {
            Log.w(TAG, "NO 'pageIndicator' property found!");
        }

        if (proxy.hasProperty("pageIndicatorColor")) {
            Log.d(TAG, "Has 'pageIndicatorColor' property");
            setPageIndicatorColor(proxy.getProperty("pageIndicatorColor"));
        }

        if (proxy.hasProperty("currentPageIndicatorColor")) {
            Log.d(TAG, "Has 'currentPageIndicatorColor' property");
            setCurrentPageIndicatorColor(proxy.getProperty("currentPageIndicatorColor"));
        }

        Log.d(TAG, "VerticalpagerView constructor finished");
    }

    private void setupViewPager() {
        viewPager = new ViewPager2(pagerProxy.getActivity());
        viewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        viewPager.setLayoutParams(params);

        adapter = new TiVerticalPagerAdapter(pagerProxy, pagerProxy.getViewProxies());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(pagerProxy.getCacheSize());

        pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);

                if (state == ViewPager2.SCROLL_STATE_DRAGGING && !isScrolling) {
                    isScrolling = true;
                    pagerProxy.fireScrollStartEvent();
                } else if (state == ViewPager2.SCROLL_STATE_IDLE && isScrolling) {
                    isScrolling = false;
                    handleScrollEnd();
                    schedulePreloading();
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);

                if (position != lastScrollPosition) {
                    lastScrollDirection = position > lastScrollPosition ? 1 : -1;
                    lastScrollPosition = position;
                    Log.d(TAG, "Scroll direction: " + (lastScrollDirection > 0 ? "DOWN" : "UP"));
                }

                if (pageIndicatorView != null && indicatorType == 1 && pageIndicatorView instanceof TiVerticalPagerIndicator) {
                    ((TiVerticalPagerIndicator) pageIndicatorView).setCurrentPageWithOffset(position, positionOffset);
                }

                if (isScrolling) {
                    pagerProxy.fireScrollEvent(position, positionOffsetPixels);
                }
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
            }
        };

        viewPager.registerOnPageChangeCallback(pageChangeCallback);
    }

    private void handleScrollEnd() {
        int newPage = viewPager.getCurrentItem();
        int oldPage = pagerProxy.getCurrentPage();

        if (newPage < 0 || newPage >= pagerProxy.getViewProxies().size()) {
            Log.w(TAG, "Invalid page index: " + newPage);
            return;
        }

        if (newPage != oldPage) {
            pagerProxy.setCurrentPage(newPage);
            updatePageIndicator(newPage);

            TiViewProxy currentView = pagerProxy.getViewProxies().get(newPage);
            TiViewProxy previousView = (oldPage >= 0 && oldPage < pagerProxy.getViewProxies().size())
                    ? pagerProxy.getViewProxies().get(oldPage)
                    : null;

            pagerProxy.fireChangeEvent(newPage, oldPage, currentView, previousView);
        }

        pagerProxy.fireScrollEndEvent();
    }

    public void reloadData() {
        if (adapter != null) {
            adapter.updateData(pagerProxy.getViewProxies());

            int cacheSize = Math.max(1, pagerProxy.getCacheSize());
            viewPager.setOffscreenPageLimit(cacheSize);

            if (pageIndicatorView != null) {
                updatePageIndicatorPages();
            }
        }
    }

    public void scrollToPage(int page, boolean animated) {
        if (page >= 0 && page < pagerProxy.getViewProxies().size()) {
            viewPager.setCurrentItem(page, animated);
            updatePageIndicator(page);
        }
    }

    private void schedulePreloading() {
        if (preloadRunnable != null) {
            preloadHandler.removeCallbacks(preloadRunnable);
        }

        preloadRunnable = new Runnable() {
            @Override
            public void run() {
                performIntelligentPreloading();
            }
        };

        preloadHandler.postDelayed(preloadRunnable, 100);
    }

    private void performIntelligentPreloading() {
        if (isPreloading || pagerProxy == null) {
            return;
        }

        isPreloading = true;
        int currentPage = pagerProxy.getCurrentPage();
        int totalPages = pagerProxy.getViewProxies().size();
        int cacheSize = pagerProxy.getCacheSize();

        Log.d(TAG, "Starting intelligent pre-loading from page " + currentPage);

        int preloadStart, preloadEnd;

        if (lastScrollDirection > 0) {
            preloadStart = currentPage + cacheSize;
            preloadEnd = Math.min(totalPages - 1, currentPage + cacheSize + 2);
            Log.d(TAG, "Pre-loading DOWN: pages " + preloadStart + " to " + preloadEnd);
        } else if (lastScrollDirection < 0) {
            preloadStart = Math.max(0, currentPage - cacheSize - 2);
            preloadEnd = currentPage - cacheSize;
            Log.d(TAG, "Pre-loading UP: pages " + preloadStart + " to " + preloadEnd);
        } else {
            preloadStart = Math.max(0, currentPage - cacheSize - 1);
            preloadEnd = Math.min(totalPages - 1, currentPage + cacheSize + 1);
            Log.d(TAG, "Pre-loading BOTH: pages " + preloadStart + " to " + preloadEnd);
        }

        preloadViewsInRange(preloadStart, preloadEnd, 0);
    }

    private void preloadViewsInRange(final int start, final int end, final int delay) {
        if (start > end || start < 0) {
            isPreloading = false;
            return;
        }

        preloadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    TiViewProxy viewProxy = pagerProxy.getViewProxies().get(start);

                    if (viewProxy != null) {
                        Log.d(TAG, "Pre-loading view at position " + start);

                        if (viewProxy.getParent() == null) {
                            viewProxy.setParent(pagerProxy);
                        }

                        viewProxy.getOrCreateView();

                        Log.d(TAG, "View " + start + " pre-loaded successfully");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error pre-loading view " + start + ": " + e.getMessage());
                }

                if (start < end) {
                    preloadViewsInRange(start + 1, end, 50); // 50ms entre cada
                } else {
                    isPreloading = false;
                    Log.d(TAG, "Pre-loading complete");
                }
            }
        }, delay);
    }

    private void cancelPreloading() {
        if (preloadRunnable != null) {
            preloadHandler.removeCallbacks(preloadRunnable);
        }
        preloadHandler.removeCallbacksAndMessages(null);
        isPreloading = false;
    }

    @Override
    public void processProperties(KrollDict properties) {
        super.processProperties(properties);

        if (properties.containsKey("pageIndicator")) {
            setPageIndicator(properties.get("pageIndicator"));
        }

        if (properties.containsKey("pageIndicatorColor")) {
            setPageIndicatorColor(Objects.requireNonNull(properties.get("pageIndicatorColor")));
        }

        if (properties.containsKey("currentPageIndicatorColor")) {
            setCurrentPageIndicatorColor(Objects.requireNonNull(properties.get("currentPageIndicatorColor")));
        }
    }

    public void setPageIndicator(Object config) {
        Log.d(TAG, "setPageIndicator called on VIEW");
        Log.d(TAG, "Config: " + config);

        if (config == null) {
            Log.w(TAG, "Config is null, returning");
            return;
        }

        KrollDict pageIndicatorDict;

        if (config instanceof KrollDict) {
            Log.d(TAG, "Config is KrollDict");
            pageIndicatorDict = (KrollDict) config;
        } else if (config instanceof java.util.HashMap) {
            Log.d(TAG, "Config is HashMap, converting to KrollDict");
            @SuppressWarnings("unchecked")
            java.util.HashMap<String, Object> hashMap = (java.util.HashMap<String, Object>) config;
            pageIndicatorDict = new KrollDict(hashMap);
        } else {
            Log.e(TAG, "Config is neither KrollDict nor HashMap! Type: " + config.getClass().getName());
            return;
        }

        this.pageIndicatorConfig = pageIndicatorDict;
        Log.d(TAG, "PageIndicator config keys: " + pageIndicatorConfig.keySet());

        cleanupPageIndicator();

        indicatorType = TiConvert.toInt(pageIndicatorConfig.get("type"), 0);
        Log.d(TAG, "Indicator type: " + indicatorType);

        if (pageIndicatorConfig.containsKey("pageIndicatorColor")) {
            String colorStr = pageIndicatorConfig.getString("pageIndicatorColor");
            Log.d(TAG, "pageIndicatorColor: " + colorStr);
            pageIndicatorColor = TiConvert.toColor(colorStr);
        }

        if (pageIndicatorConfig.containsKey("currentPageIndicatorColor")) {
            String colorStr = pageIndicatorConfig.getString("currentPageIndicatorColor");
            Log.d(TAG, "currentPageIndicatorColor: " + colorStr);
            currentPageIndicatorColor = TiConvert.toColor(colorStr);
        }

        Log.d(TAG, "Creating page indicator, type: " + (indicatorType == 1 ? "VERTICAL" : "HORIZONTAL"));

        if (indicatorType == 1) {
            Log.d(TAG, "Calling setupVerticalPageIndicator()");
            setupVerticalPageIndicator();

            if (pageIndicatorView instanceof TiVerticalPagerIndicator indicator) {

                if (pageIndicatorConfig.containsKey("normalDotSize")) {
                    float size = TiConvert.toFloat(pageIndicatorConfig.get("normalDotSize"), 6f);
                    indicator.setNormalDotSize(size);
                }

                if (pageIndicatorConfig.containsKey("activeDotSize")) {
                    float size = TiConvert.toFloat(pageIndicatorConfig.get("activeDotSize"), 8f);
                    indicator.setActiveDotSize(size);
                }

                if (pageIndicatorConfig.containsKey("minDotSize")) {
                    float size = TiConvert.toFloat(pageIndicatorConfig.get("minDotSize"), 3f);
                    indicator.setMinDotSize(size);
                }

                if (pageIndicatorConfig.containsKey("dotSpacing")) {
                    float spacing = TiConvert.toFloat(pageIndicatorConfig.get("dotSpacing"), 16f);
                    indicator.setDotSpacing(spacing);
                }
            }

        } else {
            Log.d(TAG, "Calling setupHorizontalPageIndicator()");
            setupHorizontalPageIndicator();
        }

        Log.d(TAG, "PageIndicator created: " + (pageIndicatorView != null));
        if (pageIndicatorView != null) {
            Log.d(TAG, "PageIndicator visibility: " + pageIndicatorView.getVisibility());
            Log.d(TAG, "PageIndicator parent: " + pageIndicatorView.getParent());
        }
    }

    private void cleanupPageIndicator() {
        if (tabLayoutMediator != null) {
            tabLayoutMediator.detach();
            tabLayoutMediator = null;
        }

        if (pageIndicatorView != null) {
            containerLayout.removeView(pageIndicatorView);
            pageIndicatorView = null;
        }
    }

    private void setupVerticalPageIndicator() {
        Log.d(TAG, "setupVerticalPageIndicator START");

        if (pagerProxy.getActivity() == null) {
            Log.e(TAG, "Activity is null, cannot create page indicator");
            return;
        }

        Log.d(TAG, "Creating VerticalPageIndicator...");
        TiVerticalPagerIndicator indicator = new TiVerticalPagerIndicator(pagerProxy.getActivity());

        int numPages = pagerProxy.getViewProxies().size();
        int currentPage = pagerProxy.getCurrentPage();

        Log.d(TAG, "Setting numberOfPages: " + numPages);
        indicator.setNumberOfPages(numPages);

        Log.d(TAG, "Setting currentPage: " + currentPage);
        indicator.setCurrentPage(currentPage);

        Log.d(TAG, "Setting pageIndicatorColor: " + pageIndicatorColor);
        indicator.setPageIndicatorColor(pageIndicatorColor);

        Log.d(TAG, "Setting currentPageIndicatorColor: " + currentPageIndicatorColor);
        indicator.setCurrentPageIndicatorColor(currentPageIndicatorColor);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );

        Log.d(TAG, "Positioning indicator...");
        positionPageIndicator(params);

        Log.d(TAG, "Params gravity: " + params.gravity);
        Log.d(TAG, "Params margins: L=" + params.leftMargin + " T=" + params.topMargin + " R=" + params.rightMargin + " B=" + params.bottomMargin);

        indicator.setLayoutParams(params);
        indicator.setVisibility(View.VISIBLE);

        pageIndicatorView = indicator;

        Log.d(TAG, "Adding indicator to container...");
        containerLayout.addView(pageIndicatorView);

        Log.d(TAG, "Indicator added! Child count: " + containerLayout.getChildCount());
        Log.d(TAG, "setupVerticalPageIndicator DONE");
    }

    private void setupHorizontalPageIndicator() {

        if (pagerProxy.getActivity() == null) {
            Log.e(TAG, "Activity is null, cannot create page indicator");
            return;
        }

        TabLayout indicator = new TabLayout(pagerProxy.getActivity());
        indicator.setTabMode(TabLayout.MODE_SCROLLABLE);
        indicator.setSelectedTabIndicatorColor(Color.TRANSPARENT);
        indicator.setTabRippleColor(null);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );

        // Position
        positionPageIndicator(params);

        indicator.setLayoutParams(params);
        pageIndicatorView = indicator;
        containerLayout.addView(pageIndicatorView);

        // Attach to ViewPager2
        tabLayoutMediator = new TabLayoutMediator(indicator, viewPager, (tab, position) -> {
            // Don't set any text, just create empty tabs as dots
        });

        // Style the tabs to look like dots
        styleTabLayoutAsDots(indicator);
    }

    private void styleTabLayoutAsDots(TabLayout tabLayout) {
        // This styles the TabLayout to look like page dots
        tabLayout.post(() -> {
            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                if (tab != null) {
                    View tabView = tab.view;
                    tabView.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        });
    }

    private void positionPageIndicator(FrameLayout.LayoutParams params) {
        if (pageIndicatorConfig == null) {
            // Default: bottom center
            params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            params.bottomMargin = 40;
            return;
        }

        int gravity = 0;

        // Horizontal positioning
        if (pageIndicatorConfig.containsKey("left")) {
            gravity |= Gravity.LEFT;
            params.leftMargin = TiConvert.toInt(pageIndicatorConfig.get("left"), 0);
        } else if (pageIndicatorConfig.containsKey("right")) {
            gravity |= Gravity.RIGHT;
            params.rightMargin = TiConvert.toInt(pageIndicatorConfig.get("right"), 0);
        } else {
            gravity |= Gravity.CENTER_HORIZONTAL;
        }

        // Vertical positioning
        if (pageIndicatorConfig.containsKey("top")) {
            gravity |= Gravity.TOP;
            params.topMargin = TiConvert.toInt(pageIndicatorConfig.get("top"), 0);
        } else if (pageIndicatorConfig.containsKey("bottom")) {
            gravity |= Gravity.BOTTOM;
            params.bottomMargin = TiConvert.toInt(pageIndicatorConfig.get("bottom"), 0);
        } else {
            gravity |= Gravity.BOTTOM;
            params.bottomMargin = 40;
        }

        params.gravity = gravity;
    }

    private void updatePageIndicator(int page) {
        if (pageIndicatorView == null) {
            return;
        }

        if (indicatorType == 1 && pageIndicatorView instanceof TiVerticalPagerIndicator) {
            ((TiVerticalPagerIndicator) pageIndicatorView).setCurrentPage(page);
        }
        // TabLayout updates automatically via TabLayoutMediator
    }

    private void updatePageIndicatorPages() {
        if (pageIndicatorView == null) {
            return;
        }

        if (indicatorType == 1 && pageIndicatorView instanceof TiVerticalPagerIndicator) {
            ((TiVerticalPagerIndicator) pageIndicatorView).setNumberOfPages(pagerProxy.getViewProxies().size());
            ((TiVerticalPagerIndicator) pageIndicatorView).setCurrentPage(pagerProxy.getCurrentPage());
        }
        // TabLayout updates automatically via adapter notifyDataSetChanged
    }

    public void setPageIndicatorColor(Object color) {
        pageIndicatorColor = TiConvert.toColor(color.toString());

        if (pageIndicatorView != null && indicatorType == 1 && pageIndicatorView instanceof TiVerticalPagerIndicator) {
            ((TiVerticalPagerIndicator) pageIndicatorView).setPageIndicatorColor(pageIndicatorColor);
        }
    }

    public void setCurrentPageIndicatorColor(Object color) {
        currentPageIndicatorColor = TiConvert.toColor(color.toString());

        if (pageIndicatorView != null && indicatorType == 1 && pageIndicatorView instanceof TiVerticalPagerIndicator) {
            ((TiVerticalPagerIndicator) pageIndicatorView).setCurrentPageIndicatorColor(currentPageIndicatorColor);
        }
    }

    public void manualRelease() {
        Log.d(TAG, "Manual cleanup called");

        // Force ViewPager2 to release non-visible pages
        if (viewPager != null && adapter != null) {
            // Temporarily set offscreen limit to 0 to force cleanup
            int currentLimit = viewPager.getOffscreenPageLimit();
            viewPager.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);

            // Notify adapter to refresh
            adapter.notifyDataSetChanged();

            // Restore original limit after a brief delay
            viewPager.postDelayed(() -> {
                viewPager.setOffscreenPageLimit(currentLimit);
            }, 100);
        }
    }

    @Override
    public void release() {
        Log.d(TAG, "Release called");

        cancelPreloading();

        if (viewPager != null && pageChangeCallback != null) {
            viewPager.unregisterOnPageChangeCallback(pageChangeCallback);
            pageChangeCallback = null;
        }

        cleanupPageIndicator();

        if (viewPager != null) {
            viewPager.setAdapter(null);
            viewPager = null;
        }

        if (adapter != null) {
            adapter = null;
        }

        if (pageIndicatorView != null) {
            pageIndicatorView = null;
        }

        super.release();
    }
}