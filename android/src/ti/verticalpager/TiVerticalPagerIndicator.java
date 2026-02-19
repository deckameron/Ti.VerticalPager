package ti.verticalpager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import androidx.annotation.NonNull;

public class TiVerticalPagerIndicator extends View {

    private static final int MAX_VISIBLE_INDICATORS = 9;

    private int numberOfPages = 0;
    private int currentPage = 0;
    private float currentPositionOffset = 0f; // ⬅️ NOVO
    private int pageIndicatorColor = 0x80FFFFFF;
    private int currentPageIndicatorColor = 0xFFFFFFFF;

    private final float normalDotSize = 6f;
    private final float activeDotSize = 8f;
    private final float minDotSize = 3f;
    private final float dotSpacing = 16f;

    private Paint paint;

    public TiVerticalPagerIndicator(Context context) {
        super(context);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
        requestLayout();
        invalidate();
    }

    public void setCurrentPage(int currentPage) {
        if (this.currentPage != currentPage) {
            this.currentPage = currentPage;
            this.currentPositionOffset = 0f;
            invalidate();
        }
    }

    public void setCurrentPageWithOffset(int position, float positionOffset) {
        this.currentPage = position;
        this.currentPositionOffset = positionOffset;
        invalidate();
    }

    public void setPageIndicatorColor(int color) {
        this.pageIndicatorColor = color;
        invalidate();
    }

    public void setCurrentPageIndicatorColor(int color) {
        this.currentPageIndicatorColor = color;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {

        super.onDraw(canvas);

        if (numberOfPages <= 0) {
            return;
        }

        float viewHeight = getHeight();
        float viewWidth = getWidth();

        if (numberOfPages <= MAX_VISIBLE_INDICATORS) {
            drawAllIndicators(canvas, viewWidth, viewHeight);
        } else {
            drawCondensedIndicatorsWithSliding(canvas, viewWidth, viewHeight);
        }
    }

    private void drawAllIndicators(Canvas canvas, float viewWidth, float viewHeight) {

        float totalHeight = (activeDotSize * 2 * numberOfPages) + (dotSpacing * (numberOfPages - 1));
        float startY = (viewHeight - totalHeight) / 2 + activeDotSize;
        float centerX = viewWidth / 2;

        for (int i = 0; i < numberOfPages; i++) {
            boolean isCurrent = (i == currentPage);
            float radius = isCurrent ? activeDotSize : normalDotSize;
            int alpha = isCurrent ? 255 : 153;

            paint.setColor(isCurrent ? currentPageIndicatorColor : pageIndicatorColor);
            paint.setAlpha(alpha);

            float y = startY + (i * (activeDotSize * 2 + dotSpacing));
            canvas.drawCircle(centerX, y, radius, paint);
        }
    }

    private void drawCondensedIndicatorsWithSliding(Canvas canvas, float viewWidth, float viewHeight) {

        int indicatorsToShow = MAX_VISIBLE_INDICATORS;
        int centerIndex = indicatorsToShow / 2;

        float baseCenterY = viewHeight / 2;
        float centerX = viewWidth / 2;

        float scrollOffset = -currentPositionOffset * (activeDotSize * 2 + dotSpacing);

        int startPage = Math.max(0, currentPage - centerIndex - 1);
        int endPage = Math.min(numberOfPages - 1, currentPage + centerIndex + 2); // +2 para mostrar a próxima entrando

        for (int pageIndex = startPage; pageIndex <= endPage; pageIndex++) {
            int relativePosition = pageIndex - currentPage;

            float y = baseCenterY + (relativePosition * (activeDotSize * 2 + dotSpacing)) + scrollOffset;

            if (y < -activeDotSize * 2 || y > viewHeight + activeDotSize * 2) {
                continue;
            }

            boolean isCurrent = (pageIndex == currentPage);

            float distanceFromCenter = Math.abs(y - baseCenterY);
            float maxDistance = ((float) indicatorsToShow / 2) * (activeDotSize * 2 + dotSpacing);

            float radius;
            int alpha;

            if (isCurrent) {
                radius = activeDotSize;
                alpha = 255;
            } else {
                float normalizedDistance = Math.min(1f, distanceFromCenter / maxDistance);

                if (normalizedDistance > 0.8f) {
                    radius = minDotSize;
                    alpha = 128;
                } else if (normalizedDistance > 0.5f) {
                    radius = normalDotSize * 0.8f;
                    alpha = 180;
                } else {
                    radius = normalDotSize;
                    alpha = 204;
                }
            }

            paint.setColor(isCurrent ? currentPageIndicatorColor : pageIndicatorColor);
            paint.setAlpha(alpha);
            canvas.drawCircle(centerX, y, radius, paint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = (int) (activeDotSize * 2 + 16);

        int visibleIndicators = Math.min(numberOfPages, MAX_VISIBLE_INDICATORS);
        int desiredHeight = (int) ((activeDotSize * 2 * visibleIndicators) +
                (dotSpacing * (visibleIndicators - 1)) + 40);

        int width = resolveSize(desiredWidth, widthMeasureSpec);
        int height = resolveSize(desiredHeight, heightMeasureSpec);

        setMeasuredDimension(width, height);
    }
}