//
//  TiVerticalpagerView.m
//  Ti.VerticalPager
//
//  Created by Douglas Alves on 01/02/26.
//

#import "TiVerticalpagerView.h"
#import "TiVerticalpagerViewProxy.h"
#import "TiUtils.h"
#import "TiViewProxy.h"

static NSString * const CellIdentifier = @"VerticalPagerCell";

@interface TiVerticalpagerView ()
@property (nonatomic, assign) BOOL isScrolling;
@property (nonatomic, strong) NSDictionary *pageIndicatorConfig;
@property (nonatomic, assign) NSInteger indicatorType;
@end

@implementation TiVerticalpagerView

- (instancetype)init
{
    self = [super init];
    if (self) {
        _cachedCells = [NSMutableDictionary dictionary];
        _isScrolling = NO;
        _indicatorType = 0; // HORIZONTAL
        
        [self setupCollectionView];
    }
    return self;
}

- (void)setupCollectionView
{
    UICollectionViewFlowLayout *layout = [[UICollectionViewFlowLayout alloc] init];
    layout.scrollDirection = UICollectionViewScrollDirectionVertical;
    layout.minimumLineSpacing = 0;
    layout.minimumInteritemSpacing = 0;
    
    self.collectionView = [[UICollectionView alloc] initWithFrame:CGRectZero collectionViewLayout:layout];
    self.collectionView.delegate = self;
    self.collectionView.dataSource = self;
    self.collectionView.pagingEnabled = YES;
    self.collectionView.showsVerticalScrollIndicator = NO;
    self.collectionView.backgroundColor = [UIColor clearColor];
    self.collectionView.bounces = YES;
    
    if (@available(iOS 11.0, *)) {
        self.collectionView.contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentNever;
    }
    
    [self.collectionView registerClass:[UICollectionViewCell class] forCellWithReuseIdentifier:CellIdentifier];
    
    [self addSubview:self.collectionView];
}

- (void)setupPageIndicatorWithConfig:(NSDictionary *)config
{
    if (self.pageControl) {
        [self.pageControl removeFromSuperview];
        self.pageControl = nil;
    }
    
    if (!config) {
        return;
    }
    
    NSInteger type = [TiUtils intValue:[config objectForKey:@"type"] def:0];
    self.indicatorType = type;
    
    UIColor *pageColor = [[TiUtils colorValue:[config objectForKey:@"pageIndicatorColor"]] color] ?: [UIColor lightGrayColor];
    UIColor *currentColor = [[TiUtils colorValue:[config objectForKey:@"currentPageIndicatorColor"]] color] ?: [UIColor darkGrayColor];
    
    TiVerticalpagerViewProxy *proxy = (TiVerticalpagerViewProxy *)self.proxy;
    NSInteger numberOfPages = proxy.viewProxies.count;
    
    self.pageControl = [[UIPageControl alloc] init];
    self.pageControl.numberOfPages = numberOfPages;
    self.pageControl.currentPage = proxy.currentPage;
    self.pageControl.pageIndicatorTintColor = pageColor;
    self.pageControl.currentPageIndicatorTintColor = currentColor;
    self.pageControl.hidesForSinglePage = YES;
    self.pageControl.userInteractionEnabled = NO;
    
    if (type == 1) {
        self.pageControl.transform = CGAffineTransformMakeRotation(M_PI_2); // 90 degrees
    }
    
    [self addSubview:self.pageControl];
    [self positionPageIndicatorWithConfig:config];
}

- (UIImage *)createIndicatorImageWithSize:(CGFloat)size color:(UIColor *)color API_AVAILABLE(ios(14.0))
{
    UIGraphicsBeginImageContextWithOptions(CGSizeMake(size, size), NO, 0);
    CGContextRef context = UIGraphicsGetCurrentContext();
    
    [color setFill];
    CGContextFillEllipseInRect(context, CGRectMake(0, 0, size, size));
    
    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    return image;
}

- (void)positionPageIndicatorWithConfig:(NSDictionary *)config
{
    if (!self.pageControl || !config) {
        return;
    }
    
    CGRect bounds = self.bounds;
    
    CGSize indicatorSize = [self.pageControl sizeForNumberOfPages:self.pageControl.numberOfPages];
    
    if (self.indicatorType == 1) {
        indicatorSize = CGSizeMake(indicatorSize.height, indicatorSize.width);
    }
    
    CGFloat left = [TiUtils floatValue:[config objectForKey:@"left"] def:-1];
    CGFloat right = [TiUtils floatValue:[config objectForKey:@"right"] def:-1];
    CGFloat top = [TiUtils floatValue:[config objectForKey:@"top"] def:-1];
    CGFloat bottom = [TiUtils floatValue:[config objectForKey:@"bottom"] def:-1];
    
    CGFloat x, y;
    
    if (left != -1) {
        x = left;
    } else if (right != -1) {
        x = bounds.size.width - indicatorSize.width - right;
    } else {
        x = (bounds.size.width - indicatorSize.width) / 2;
    }
    
    if (top != -1) {
        y = top;
    } else if (bottom != -1) {
        y = bounds.size.height - indicatorSize.height - bottom;
    } else {
        y = bounds.size.height - indicatorSize.height - 20;
    }
    
    self.pageControl.bounds = CGRectMake(0, 0,
        self.indicatorType == 1 ? indicatorSize.height : indicatorSize.width,
        self.indicatorType == 1 ? indicatorSize.width : indicatorSize.height
    );
    self.pageControl.center = CGPointMake(x + indicatorSize.width / 2, y + indicatorSize.height / 2);
}

- (void)setPageIndicator_:(id)args
{
    ENSURE_TYPE_OR_NIL(args, NSDictionary);
    
    self.pageIndicatorConfig = args;
    [self setupPageIndicatorWithConfig:args];
}

- (void)frameSizeChanged:(CGRect)frame bounds:(CGRect)bounds
{
    [super frameSizeChanged:frame bounds:bounds];
    
    self.collectionView.frame = bounds;
    [self.collectionView.collectionViewLayout invalidateLayout];
    
    if (self.pageControl && self.pageIndicatorConfig) {
        [self positionPageIndicatorWithConfig:self.pageIndicatorConfig];
    }
}

- (void)reloadData
{
    TiVerticalpagerViewProxy *proxy = (TiVerticalpagerViewProxy *)self.proxy;
    
    [self.cachedCells removeAllObjects];
    
    if (self.pageControl) {
        self.pageControl.numberOfPages = proxy.viewProxies.count;
        self.pageControl.currentPage = proxy.currentPage;
    }
    
    [self.collectionView reloadData];
}

- (void)scrollToPage:(NSInteger)page animated:(BOOL)animated
{
    if (page < 0 || page >= [self.collectionView numberOfItemsInSection:0]) {
        return;
    }
    
    TiVerticalpagerViewProxy *proxy = (TiVerticalpagerViewProxy *)self.proxy;
    proxy.currentPage = page;
    
    if (self.pageControl) {
        self.pageControl.currentPage = page;
    }
    
    NSIndexPath *indexPath = [NSIndexPath indexPathForItem:page inSection:0];
    [self.collectionView scrollToItemAtIndexPath:indexPath
                                atScrollPosition:UICollectionViewScrollPositionTop
                                        animated:animated];
}

#pragma mark - Dynamic Properties

- (void)setPageIndicatorColor_:(id)value
{
    if (!self.pageControl) {
        return;
    }
    
    UIColor *color = [[TiUtils colorValue:value] color];
    self.pageControl.pageIndicatorTintColor = color;
}

- (void)setCurrentPageIndicatorColor_:(id)value
{
    if (!self.pageControl) {
        return;
    }
    
    UIColor *color = [[TiUtils colorValue:value] color];
    self.pageControl.currentPageIndicatorTintColor = color;
}

#pragma mark - UICollectionViewDataSource

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section
{
    TiVerticalpagerViewProxy *proxy = (TiVerticalpagerViewProxy *)self.proxy;
    return proxy.viewProxies.count;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath
{
    UICollectionViewCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:CellIdentifier forIndexPath:indexPath];
    
    // Clear previous content
    for (UIView *subview in cell.contentView.subviews) {
        [subview removeFromSuperview];
    }
    
    TiVerticalpagerViewProxy *proxy = (TiVerticalpagerViewProxy *)self.proxy;
    
    if (indexPath.item < proxy.viewProxies.count) {
        TiViewProxy *viewProxy = proxy.viewProxies[indexPath.item];
        
        if (viewProxy.parent == nil) {
            [viewProxy setParent:proxy];
        }
        
        CGRect cellBounds = cell.contentView.bounds;
        
        LayoutConstraint *layoutProperties = [viewProxy layoutProperties];
        layoutProperties->width = TiDimensionDip(cellBounds.size.width);
        layoutProperties->height = TiDimensionDip(cellBounds.size.height);
        layoutProperties->top = TiDimensionDip(0);
        layoutProperties->left = TiDimensionDip(0);
        
        [viewProxy setSandboxBounds:cellBounds];
        
        [viewProxy layoutChildren:NO];
        
        UIView *view = [viewProxy view];
        
        view.frame = cellBounds;
        view.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
        
        [cell.contentView addSubview:view];
        [viewProxy layoutChildren:NO];
        
        [view layoutIfNeeded];
    }
    
    return cell;
}

#pragma mark - UICollectionViewDelegateFlowLayout

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath
{
    return collectionView.bounds.size;
}

#pragma mark - UIScrollViewDelegate

- (void)scrollViewWillBeginDragging:(UIScrollView *)scrollView
{
    if (!self.isScrolling) {
        self.isScrolling = YES;
        
        if ([self.proxy _hasListeners:@"scrollstart"]) {
            TiVerticalpagerViewProxy *proxy = (TiVerticalpagerViewProxy *)self.proxy;
            [self.proxy fireEvent:@"scrollstart" withObject:@{
                @"currentPage": @(proxy.currentPage)
            }];
        }
    }
}

- (void)scrollViewDidScroll:(UIScrollView *)scrollView
{
    if ([self.proxy _hasListeners:@"scroll"]) {
        CGFloat pageHeight = scrollView.bounds.size.height;
        CGFloat currentPosition = scrollView.contentOffset.y / pageHeight;
        
        [self.proxy fireEvent:@"scroll" withObject:@{
            @"currentPage": @((NSInteger)round(currentPosition)),
            @"offset": @(scrollView.contentOffset.y)
        }];
    }
}

- (void)scrollViewDidEndDecelerating:(UIScrollView *)scrollView
{
    [self handleScrollEnd:scrollView];
}

- (void)scrollViewDidEndDragging:(UIScrollView *)scrollView willDecelerate:(BOOL)decelerate
{
    if (!decelerate) {
        [self handleScrollEnd:scrollView];
    }
}

- (void)scrollViewDidEndScrollingAnimation:(UIScrollView *)scrollView
{
    [self handleScrollEnd:scrollView];
}

- (void)handleScrollEnd:(UIScrollView *)scrollView
{
    self.isScrolling = NO;
    
    CGFloat pageHeight = scrollView.bounds.size.height;
    NSInteger newPage = (NSInteger)round(scrollView.contentOffset.y / pageHeight);
    
    TiVerticalpagerViewProxy *proxy = (TiVerticalpagerViewProxy *)self.proxy;
    NSInteger oldPage = proxy.currentPage;
    
    if (newPage != oldPage && newPage >= 0 && newPage < proxy.viewProxies.count) {
        proxy.currentPage = newPage;
        
        if (self.pageControl) {
            self.pageControl.currentPage = newPage;
        }
        
        if ([self.proxy _hasListeners:@"change"]) {
            [self.proxy fireEvent:@"change" withObject:@{
                @"currentPage": @(newPage),
                @"previousPage": @(oldPage)
            }];
        }
    }
    
    if ([self.proxy _hasListeners:@"scrollend"]) {
        [self.proxy fireEvent:@"scrollend" withObject:@{
            @"currentPage": @(proxy.currentPage)
        }];
    }
    
    [self manageCacheForPage:proxy.currentPage];
}

- (void)manageCacheForPage:(NSInteger)currentPage
{
    TiVerticalpagerViewProxy *proxy = (TiVerticalpagerViewProxy *)self.proxy;
    NSInteger cacheSize = proxy.cacheSize;
    NSInteger totalPages = proxy.viewProxies.count;
    
    // Determine which pages should be cached
    NSInteger startPage = MAX(0, currentPage - cacheSize / 2);
    NSInteger endPage = MIN(totalPages - 1, currentPage + cacheSize / 2);
    
    NSMutableSet *pagesToKeep = [NSMutableSet set];
    for (NSInteger i = startPage; i <= endPage; i++) {
        [pagesToKeep addObject:@(i)];
    }
    
    // Get visible cells
    NSArray *visibleIndexPaths = [self.collectionView indexPathsForVisibleItems];
    for (NSIndexPath *indexPath in visibleIndexPaths) {
        [pagesToKeep addObject:@(indexPath.item)];
    }
    
    // Remove cells that are not in the cache range
    NSArray *allKeys = [self.cachedCells.allKeys copy];
    for (NSNumber *pageNumber in allKeys) {
        if (![pagesToKeep containsObject:pageNumber]) {
            [self.cachedCells removeObjectForKey:pageNumber];
        }
    }
}

- (void)dealloc
{
    self.collectionView.delegate = nil;
    self.collectionView.dataSource = nil;
}

@end
