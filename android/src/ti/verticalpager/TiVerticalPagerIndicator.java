package ti.verticalpager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import androidx.annotation.NonNull;

public class TiVerticalPagerIndicator extends View{

    private int numberOfPages = 0;
    private int currentPage = 0;
    private int pageIndicatorColor = 0x80FFFFFF; // 50% white
    private int currentPageIndicatorColor = 0xFFFFFFFF; // 100% white
    private float indicatorRadius = 8f;
    private final float currentIndicatorRadius = 10f;
    private final float spacing = 24f;
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
        this.currentPage = currentPage;
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

        // Calculate total height needed
        float totalHeight = (currentIndicatorRadius * 2 * numberOfPages) + (spacing * (numberOfPages - 1));
        float startY = (viewHeight - totalHeight) / 2 + currentIndicatorRadius;
        float centerX = viewWidth / 2;

        for (int i = 0; i < numberOfPages; i++) {
            boolean isCurrent = (i == currentPage);
            float radius = isCurrent ? currentIndicatorRadius : indicatorRadius;
            paint.setColor(isCurrent ? currentPageIndicatorColor : pageIndicatorColor);

            float y = startY + (i * (currentIndicatorRadius * 2 + spacing));
            canvas.drawCircle(centerX, y, radius, paint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = (int) (currentIndicatorRadius * 2 + 16);
        int desiredHeight = (int) ((currentIndicatorRadius * 2 * numberOfPages) + (spacing * (numberOfPages - 1)) + 40);

        int width = resolveSize(desiredWidth, widthMeasureSpec);
        int height = resolveSize(desiredHeight, heightMeasureSpec);

        setMeasuredDimension(width, height);
    }
}
