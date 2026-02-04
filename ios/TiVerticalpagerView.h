//
//  TiVerticalpagerView.h
//  Ti.VerticalPager
//
//  Created by Douglas Alves on 01/02/26.
//


#import "TiUIView.h"

@class TiVerticalpagerViewProxy;

@interface TiVerticalpagerView : TiUIView <UICollectionViewDelegate, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout>

@property (nonatomic, strong) UICollectionView *collectionView;
@property (nonatomic, strong) UIPageControl *pageControl;
@property (nonatomic, strong) NSMutableDictionary *cachedCells;

- (void)reloadData;
- (void)scrollToPage:(NSInteger)page animated:(BOOL)animated;
- (void)setPageIndicator_:(id)args;

@end
