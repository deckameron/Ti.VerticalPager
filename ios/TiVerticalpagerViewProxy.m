//
//  TiVerticalpagerViewProxy.m
//  Ti.VerticalPager
//
//  Created by Douglas Alves on 01/02/26.
//


#import "TiVerticalpagerViewProxy.h"
#import "TiVerticalpagerView.h"
#import "TiUtils.h"

@implementation TiVerticalpagerViewProxy

- (instancetype)init
{
    self = [super init];
    if (self) {
        _viewProxies = [NSMutableArray array];
        _currentPage = 0;
        _cacheSize = 3;
    }
    return self;
}

- (void)setViews:(id)args
{
    ENSURE_TYPE_OR_NIL(args, NSArray);
    
    if (args == nil) {
        [self.viewProxies removeAllObjects];
        [(TiVerticalpagerView *)[self view] reloadData];
        return;
    }
    
    [self.viewProxies removeAllObjects];
    
    for (id item in args) {
        if ([item isKindOfClass:[TiViewProxy class]]) {
            [self.viewProxies addObject:item];
        }
    }
    
    [(TiVerticalpagerView *)[self view] reloadData];
}

- (void)scrollToPage:(id)args
{
    ENSURE_SINGLE_ARG(args, NSObject);
    
    NSInteger page = [TiUtils intValue:args def:0];
    
    if (page >= 0 && page < self.viewProxies.count) {
        self.currentPage = page;
        [(TiVerticalpagerView *)[self view] scrollToPage:page animated:YES];
    }
}

- (void)addView:(id)args
{
    ENSURE_SINGLE_ARG(args, TiViewProxy);
    
    [self.viewProxies addObject:args];
    [(TiVerticalpagerView *)[self view] reloadData];
}

- (void)removeView:(id)args
{
    ENSURE_SINGLE_ARG(args, NSObject);
    
    NSInteger index = [TiUtils intValue:args def:-1];
    
    if (index >= 0 && index < self.viewProxies.count) {
        [self.viewProxies removeObjectAtIndex:index];
        [(TiVerticalpagerView *)[self view] reloadData];
    }
}

- (void)release:(id)args
{
    TiThreadPerformOnMainThread(^{
        TiVerticalpagerView *view = (TiVerticalpagerView *)[self view];
        
        [view.cachedCells removeAllObjects];
        
        [view.collectionView reloadData];
        
        NSLog(@"[TiVerticalPager] Memory cleanup executed");
    }, NO);
}

#pragma mark - Public Getters

- (NSArray *)views
{
    return [NSArray arrayWithArray:self.viewProxies];
}

@end
