# Ti.VerticalPager

A native iOS module for Titanium that provides a vertical scrolling pager view with TikTok/Reels-style snap behavior.

![Titanium](https://img.shields.io/badge/Titanium-13.0+-red.svg) ![Platform](https://img.shields.io/badge/platform-iOS-lightgrey.svg) ![License](https://img.shields.io/badge/license-MIT-blue.svg) ![Maintained](https://img.shields.io/badge/Maintained-Yes-green.svg)

<p align="center">
  <img src="https://github.com/deckameron/Ti.VerticalPager/blob/main/assets/ti.verticalpager.animation.gif?raw=true"
       width="300"
       alt="Example" />
</p>

### Roadmap

- [x] iOS support
- [ ] Android support

## Features

- ✅ **Vertical scrolling** with smooth snap behavior (like TikTok/Reels)
- ✅ **Smart memory management** with configurable cache size
- ✅ **Customizable page indicator**:
  - Horizontal or Vertical orientation
  - Custom positioning (left, right, top, bottom)
  - Dynamic color changes
  - Native Apple condensation for many pages
- ✅ **Complete event system**: scrollstart, scroll, scrollend, change
- ✅ **Programmatic navigation**: scrollToPage(), addView(), removeView()
- ✅ **Dynamic properties**: Update colors and configuration at runtime
- ✅ **Native performance**: Built on top of UICollectionView

## Installation

1.  Copy the module to your project's  `modules/iphone/`  folder
2.  Add to  `tiapp.xml`:

```xml
<modules>
    <module platform="iphone">ti.verticalpager</module>
</modules>

```

## Quick Start
```javascript
const VerticalPager = require('ti.verticalpager');

const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#FFA07A', '#98D8C8'];
const views = colors.map((color, i) => {
    const view = Ti.UI.createView({ backgroundColor: color });
    const label = Ti.UI.createLabel({
        text: `Page ${i}`,
        color: '#fff',
        font: { fontSize: 48 }
    });
    view.add(label);
    return view;
});

const pager = VerticalPager.createView({
    top: 0,
    views: views,
    cacheSize: 3,
    pageIndicator: {
        type: VerticalPager.INDICATOR_TYPE_VERTICAL,
        currentPageIndicatorColor: '#fff',
        pageIndicatorColor: '#ffffff50',
        right: 16,
        bottom: 100
    }
});

pager.addEventListener('change', (e) => {
    console.log(`Changed from page ${e.previousPage}`);
    console.log(`Changed from view ${e.previousView}`);
    console.log(`Changed to page ${e.currentPage}`);
    console.log(`Changed to view ${e.currentView}`);
});

win.add(pager);
```

## API Reference

### Properties

| Property | Type | Description |
|----------|------|-------------|
| `views` | Array<TiViewProxy> | Array of views to display as pages |
| `cacheSize` | Number | Number of views to keep in memory (default: 3) |
| `currentPage` | Number | Index of the current page (read-only) |
| `pageIndicator` | Object | Configuration object for the page indicator |
| `pageIndicatorColor` | String | Color of inactive page indicators (dynamic) |
| `currentPageIndicatorColor` | String | Color of active page indicator (dynamic) |

### Page Indicator Configuration
```javascript
{
    type: VerticalPager.INDICATOR_TYPE_VERTICAL, // or INDICATOR_TYPE_HORIZONTAL
    currentPageIndicatorColor: '#ffffff',
    pageIndicatorColor: '#ffffff50',
    left: 16,        // Optional: position from left
    right: 16,       // Optional: position from right
    top: 16,         // Optional: position from top
    bottom: 100      // Optional: position from bottom
}
```

### Methods

#### `scrollToPage(index)`
Scrolls to the specified page index.
```javascript
pager.scrollToPage(3);
```

#### `addView(view)`
Adds a new view to the end of the pager.
```javascript
const newView = Ti.UI.createView({ backgroundColor: '#F7DC6F' });
pager.addView(newView);
```

#### `removeView(index)`
Removes the view at the specified index.
```javascript
pager.removeView(2);
```

### Events

#### `scrollstart`
Fired when the user starts scrolling.
```javascript
pager.addEventListener('scrollstart', (e) => {
    console.log('Started scrolling from page:', e.currentPage);
});
```

**Event properties:**
- `currentPage` (Number): Current page index

#### `scroll`
Fired continuously during scrolling.
```javascript
pager.addEventListener('scroll', (e) => {
    console.log('Scrolling - offset:', e.offset);
});
```

**Event properties:**
- `currentPage` (Number): Current page index
- `offset` (Number): Current scroll offset

#### `scrollend`
Fired when scrolling stops.
```javascript
pager.addEventListener('scrollend', (e) => {
    console.log('Stopped at page:', e.currentPage);
});
```

**Event properties:**
- `currentPage` (Number): Current page index

#### `change`
Fired when the page changes.
```javascript
pager.addEventListener('change', (e) => {
    console.log(`Changed from index ${e.previousPage} to ${e.currentPage}`);
    console.log(`Changed from view ${e.previousView} to ${e.currentView}`);
});
```

**Event properties:**
- `currentPage` (Number): New page index
- `previousPage` (Number): Previous page index
- `currentView` (Ti.UI.View): New view page object
- `previousView` (Ti.UI.View): Previous view page object

### Constants

| Constant | Value | Description |
|----------|-------|-------------|
| `INDICATOR_TYPE_HORIZONTAL` | 0 | Horizontal page indicator |
| `INDICATOR_TYPE_VERTICAL` | 1 | Vertical page indicator (rotated 90°) |

## Advanced Examples

### Dynamic Color Changes (Glass Effect)
```javascript
const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#FFA07A'];

const pager = VerticalPager.createView({
    views: createViews(colors),
    pageIndicator: {
        type: VerticalPager.INDICATOR_TYPE_VERTICAL,
        currentPageIndicatorColor: colors[0],
        pageIndicatorColor: colors[0] + '30',
        right: 16,
        bottom: 100
    }
});

pager.addEventListener('change', (e) => {
    const color = colors[e.currentPage % colors.length];
    pager.pageIndicatorColor = color + '30';           // 30% opacity
    pager.currentPageIndicatorColor = color;            // 100% opacity
});
```

### News Feed Example
```javascript
function createNewsCard(article) {
    const view = Ti.UI.createView({
        backgroundColor: '#fff'
    });
    
    const image = Ti.UI.createImageView({
        image: article.image,
        top: 0,
        height: '50%'
    });
    
    const title = Ti.UI.createLabel({
        text: article.title,
        top: '50%',
        left: 16,
        right: 16,
        font: { fontSize: 24, fontWeight: 'bold' }
    });
    
    view.add(image);
    view.add(title);
    
    return view;
}

const newsViews = newsArticles.map(article => createNewsCard(article));

const newsFeed = VerticalPager.createView({
    views: newsViews,
    cacheSize: 3
});
```

### Infinite Loading
```javascript
let currentPage = 0;

pager.addEventListener('change', (e) => {
    currentPage = e.currentPage;
    
    // Load more when reaching the last 2 pages
    if (currentPage >= pager.views.length - 2) {
        loadMoreContent();
    }
});

function loadMoreContent() {
    // Fetch more data...
    const newViews = fetchNextBatch();
    newViews.forEach(view => pager.addView(view));
}
```

## Requirements

- Titanium SDK 13.0.0 or later
- iOS 17.0 or later


## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request