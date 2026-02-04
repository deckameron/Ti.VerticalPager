//
//  TiVerticalpagerViewProxy.h
//  Ti.VerticalPager
//
//  Created by Douglas Alves on 01/02/26.
//


#import "TiViewProxy.h"

@interface TiVerticalpagerViewProxy : TiViewProxy

@property (nonatomic, strong) NSMutableArray *viewProxies;
@property (nonatomic, assign) NSInteger currentPage;
@property (nonatomic, assign) NSInteger cacheSize;

- (void)setViews:(id)args;
- (void)scrollToPage:(id)args;
- (void)addView:(id)args;
- (void)removeView:(id)args;

@end
