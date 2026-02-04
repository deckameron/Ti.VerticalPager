const VerticalPager = require('ti.verticalpager');

const win = Ti.UI.createWindow({
    backgroundColor: '#000'
});

// Array of colors for pages
const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#FFA07A', '#98D8C8', '#F7DC6F', '#BB8FCE', '#85C1E2'];

// Create initial views
const initialViews = [];
for (let i = 0; i < 5; i++) {
    initialViews.push(createPageView(i, colors[i % colors.length]));
}

// Create the VerticalPager
const pager = VerticalPager.createView({
    top: 0,
    left: 0,
    right: 0,
    bottom: 80,
    views: initialViews,
    cacheSize: 3,
    pageIndicator: {
        currentPageIndicatorColor: colors[0],
        pageIndicatorColor: colors[0] + '30', // Glass effect
        type: VerticalPager.INDICATOR_TYPE_VERTICAL,
        right: 16,
        bottom: 100
    }
});

// EVENT: scrollstart
pager.addEventListener('scrollstart', function(e) {
    console.log('📱 SCROLLSTART - Current page:', e.currentPage);
});

// EVENT: scroll
pager.addEventListener('scroll', function(e) {
    console.log('📜 SCROLL - Offset:', e.offset.toFixed(2));
});

// EVENT: scrollend
pager.addEventListener('scrollend', function(e) {
    console.log('🛑 SCROLLEND - Stopped at page:', e.currentPage);
    updatePageLabel();
});

// EVENT: change
pager.addEventListener('change', function(e) {
    console.log('🔄 CHANGE - Changed from', e.previousPage, 'to', e.currentPage);
    updatePageLabel();
    
    // 🎨 Update pageIndicator colors (glass effect)
    const currentColor = colors[e.currentPage % colors.length];
    pager.pageIndicatorColor = currentColor + '30';         // Transparent
    pager.currentPageIndicatorColor = currentColor;         // Solid
});

win.add(pager);

// ====================================
// BOTTOM CONTROLS
// ====================================

const controlsContainer = Ti.UI.createView({
    bottom: 0,
    height: 80,
    width: Ti.UI.FILL,
    backgroundColor: '#1a1a1a'
});

// Label showing current page
const pageLabel = Ti.UI.createLabel({
    top: 5,
    text: 'Page: 0 / 4',
    color: '#fff',
    font: { fontSize: 14, fontWeight: 'bold' }
});
controlsContainer.add(pageLabel);

// Buttons container
const buttonsContainer = Ti.UI.createView({
    bottom: 5,
    height: 50,
    layout: 'horizontal'
});

// Button: Previous Page
const btnPrev = Ti.UI.createButton({
    left: 5,
    width: 70,
    height: 40,
    title: '⬆️ Prev',
    backgroundColor: '#4ECDC4',
    color: '#fff',
    font: { fontSize: 12, fontWeight: 'bold' },
    borderRadius: 8
});
btnPrev.addEventListener('click', function() {
    const currentPage = pager.currentPage || 0;
    if (currentPage > 0) {
        pager.scrollToPage(currentPage - 1);
    }
});
buttonsContainer.add(btnPrev);

// Button: Next Page
const btnNext = Ti.UI.createButton({
    left: 5,
    width: 70,
    height: 40,
    title: '⬇️ Next',
    backgroundColor: '#4ECDC4',
    color: '#fff',
    font: { fontSize: 12, fontWeight: 'bold' },
    borderRadius: 8
});
btnNext.addEventListener('click', function() {
    const currentPage = pager.currentPage || 0;
    const totalPages = pager.views ? pager.views.length : 0;
    if (currentPage < totalPages - 1) {
        pager.scrollToPage(currentPage + 1);
    }
});
buttonsContainer.add(btnNext);

// Button: Go to First Page
const btnGoToFirst = Ti.UI.createButton({
    left: 5,
    width: 70,
    height: 40,
    title: '🏠 First',
    backgroundColor: '#FF6B6B',
    color: '#fff',
    font: { fontSize: 12, fontWeight: 'bold' },
    borderRadius: 8
});
btnGoToFirst.addEventListener('click', function() {
    pager.scrollToPage(0);
});
buttonsContainer.add(btnGoToFirst);

// Button: Add View
const btnAdd = Ti.UI.createButton({
    left: 5,
    width: 70,
    height: 40,
    title: '➕ Add',
    backgroundColor: '#45B7D1',
    color: '#fff',
    font: { fontSize: 12, fontWeight: 'bold' },
    borderRadius: 8
});
btnAdd.addEventListener('click', function() {
    const totalPages = pager.views ? pager.views.length : 0;
    const newView = createPageView(totalPages, colors[totalPages % colors.length]);
    pager.addView(newView);
    updatePageLabel();
    Ti.API.info('✅ View added! Total:', totalPages + 1);
});
buttonsContainer.add(btnAdd);

// Button: Remove Last View
const btnRemove = Ti.UI.createButton({
    left: 5,
    width: 70,
    height: 40,
    title: '➖ Remove',
    backgroundColor: '#E74C3C',
    color: '#fff',
    font: { fontSize: 12, fontWeight: 'bold' },
    borderRadius: 8
});
btnRemove.addEventListener('click', function() {
    const totalPages = pager.views ? pager.views.length : 0;
    if (totalPages > 1) {
        pager.removeView(totalPages - 1);
        updatePageLabel();
        Ti.API.info('🗑️ View removed! Total:', totalPages - 1);
    } else {
        alert('Need at least 1 page!');
    }
});
buttonsContainer.add(btnRemove);

controlsContainer.add(buttonsContainer);
win.add(controlsContainer);

// ====================================
// HELPER FUNCTIONS
// ====================================

function createPageView(index, backgroundColor) {
    const view = Ti.UI.createView({
        backgroundColor: backgroundColor
    });
    
    const contentView = Ti.UI.createView({
        layout: 'vertical',
        height: Ti.UI.SIZE
    });
    
    const title = Ti.UI.createLabel({
        top: 100,
        text: `PAGE ${index}`,
        color: '#fff',
        font: { fontSize: 48, fontWeight: 'bold' },
        textAlign: 'center'
    });
    contentView.add(title);
    
    const subtitle = Ti.UI.createLabel({
        top: 20,
        text: `Swipe up or down\nto navigate`,
        color: '#ffffffcc',
        font: { fontSize: 16 },
        textAlign: 'center'
    });
    contentView.add(subtitle);
    
    // Large emoji
    const emoji = Ti.UI.createLabel({
        top: 40,
        text: getEmoji(index),
        font: { fontSize: 120 },
        textAlign: 'center'
    });
    contentView.add(emoji);
    
    // Additional info
    const info = Ti.UI.createLabel({
        top: 40,
        text: `This is view #${index}\nCache Size: 3 pages\nMemory optimized! 🚀`,
        color: '#ffffffaa',
        font: { fontSize: 14 },
        textAlign: 'center'
    });
    contentView.add(info);
    
    view.add(contentView);
    
    return view;
}

function getEmoji(index) {
    const emojis = ['🎬', '📱', '🎮', '🎵', '🎨', '⚡', '🔥', '💎', '🌟', '🚀'];
    return emojis[index % emojis.length];
}

function updatePageLabel() {
    const currentPage = pager.currentPage || 0;
    const totalPages = pager.views ? pager.views.length : 0;
    pageLabel.text = `Page: ${currentPage} / ${totalPages - 1}`;
}

win.open();