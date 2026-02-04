/**
 * Ti.VerticalPager
 *
 * Created by Your Name
 * Copyright (c) 2026 Your Company. All rights reserved.
 */

#import "TiModule.h"

@interface TiVerticalpagerModule : TiModule

- (id)createView:(id)args;

@property (nonatomic, readonly) NSNumber *INDICATOR_TYPE_HORIZONTAL;
@property (nonatomic, readonly) NSNumber *INDICATOR_TYPE_VERTICAL;

@end
