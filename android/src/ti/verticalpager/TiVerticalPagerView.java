package ti.verticalpager;

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

        if (proxy.getActivity() == null) {
            Log.e(TAG, "Activity is null in constructor");
            return;
        }

        // Create container
        containerLayout = new FrameLayout(proxy.getActivity());

        // Setup ViewPager2
        setupViewPager();

        // Add to container
        containerLayout.addView(viewPager);

        setNativeView(containerLayout);
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
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);

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
        if (!(config instanceof KrollDict)) {
            return;
        }

        this.pageIndicatorConfig = (KrollDict) config;

        cleanupPageIndicator();

        // Remove old indicator
        if (pageIndicatorView != null) {
            containerLayout.removeView(pageIndicatorView);
            pageIndicatorView = null;
        }

        // Get type
        indicatorType = TiConvert.toInt(pageIndicatorConfig.get("type"), 0);

        // Get colors
        if (pageIndicatorConfig.containsKey("pageIndicatorColor")) {
            pageIndicatorColor = TiConvert.toColor(Objects.requireNonNull(pageIndicatorConfig.get("pageIndicatorColor")).toString());
        }

        if (pageIndicatorConfig.containsKey("currentPageIndicatorColor")) {
            currentPageIndicatorColor = TiConvert.toColor(Objects.requireNonNull(pageIndicatorConfig.get("currentPageIndicatorColor")).toString());
        }

        if (indicatorType == 1) {
            // Vertical indicator
            setupVerticalPageIndicator();
        } else {
            // Horizontal indicator (using TabLayout)
            setupHorizontalPageIndicator();
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

        if (pagerProxy.getActivity() == null) {
            Log.e(TAG, "Activity is null, cannot create page indicator");
            return;
        }

        TiVerticalPagerIndicator indicator = new TiVerticalPagerIndicator(pagerProxy.getActivity());
        indicator.setNumberOfPages(pagerProxy.getViewProxies().size());
        indicator.setCurrentPage(pagerProxy.getCurrentPage());
        indicator.setPageIndicatorColor(pageIndicatorColor);
        indicator.setCurrentPageIndicatorColor(currentPageIndicatorColor);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );

        // Position
        positionPageIndicator(params);

        indicator.setLayoutParams(params);
        pageIndicatorView = indicator;
        containerLayout.addView(pageIndicatorView);
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