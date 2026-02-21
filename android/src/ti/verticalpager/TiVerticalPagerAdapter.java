package ti.verticalpager;

import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TiVerticalPagerAdapter extends RecyclerView.Adapter<TiVerticalPagerAdapter.ViewHolder> {

    private static final String TAG = "TiVerticalPagerAdapter";

    private List<TiViewProxy> viewProxies;
    private TiVerticalPagerProxy pagerProxy;

    private final Map<Integer, View> viewCache = new HashMap<>();

    public TiVerticalPagerAdapter(TiVerticalPagerProxy proxy, List<TiViewProxy> viewProxies) {
        this.pagerProxy = proxy;
        this.viewProxies = new ArrayList<>(viewProxies);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(new android.widget.FrameLayout(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position < 0 || position >= viewProxies.size()) {
            Log.w(TAG, "Invalid position: " + position);
            return;
        }

        holder.container.removeAllViews();

        try {
            TiViewProxy viewProxy = viewProxies.get(position);

            if (viewProxy == null) {
                Log.w(TAG, "ViewProxy is null at position: " + position);
                return;
            }

            // Set parent if needed
            if (viewProxy.getParent() == null) {
                viewProxy.setParent(pagerProxy);
            }

            // Get or create the view
            TiUIView tiView;
            try {
                tiView = viewProxy.getOrCreateView();
            } catch (Exception e) {
                Log.e(TAG, "Failed to create view at position " + position + ": " + e.getMessage(), e);

                TextView errorView = new TextView(holder.container.getContext());
                errorView.setText("Error loading view at position " + position);
                errorView.setTextColor(0xFFFF0000); // Vermelho
                errorView.setGravity(android.view.Gravity.CENTER);

                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                errorView.setLayoutParams(params);
                holder.container.addView(errorView);
                return;
            }

            if (tiView == null) {
                Log.w(TAG, "TiUIView is null at position: " + position);
                return;
            }

            View nativeView = tiView.getNativeView();

            if (nativeView == null) {
                Log.w(TAG, "Native view is null at position: " + position);
                return;
            }

            ViewGroup currentParent = (ViewGroup) nativeView.getParent();
            if (currentParent != null) {
                try {
                    currentParent.removeView(nativeView);
                } catch (Exception e) {
                    Log.e(TAG, "Error removing view from parent: " + e.getMessage());
                }
            }

            // Set layout params
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            nativeView.setLayoutParams(params);

            holder.container.addView(nativeView);

        } catch (Exception e) {
            Log.e(TAG, "Error in onBindViewHolder at position " + position + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.container.removeAllViews();
    }

    @Override
    public int getItemCount() {
        return viewProxies != null ? viewProxies.size() : 0;
    }

    public void updateData(List<TiViewProxy> newProxies) {
        this.viewProxies = newProxies != null ? new ArrayList<>(newProxies) : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void cleanup() {
        viewCache.clear();
        if (viewProxies != null) {
            viewProxies.clear();
        }
        pagerProxy = null;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        android.widget.FrameLayout container;

        ViewHolder(View itemView) {
            super(itemView);
            container = (android.widget.FrameLayout) itemView;
            container.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
        }
    }
}