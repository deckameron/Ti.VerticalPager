/**
 * Ti.VerticalPager
 *
 * Created by Your Name
 * Copyright (c) 2026 Your Company. All rights reserved.
 */

#import "TiVerticalpagerViewProxy.h"
#import "TiVerticalpagerModule.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"

@implementation TiVerticalpagerModule

#pragma mark Internal

// This is generated for your module, please do not change it
- (id)moduleGUID
{
  return @"5f8f0ddd-e9c7-4a1a-bba8-3e6c5d790628";
}

// This is generated for your module, please do not change it
- (NSString *)moduleId
{
  return @"ti.verticalpager";
}

#pragma mark Lifecycle

- (void)startup
{
  // This method is called when the module is first loaded
  // You *must* call the superclass
  [super startup];
  DebugLog(@"[DEBUG] %@ loaded", self);
}

#pragma Public APIs

- (id)createView:(id)args
{
    TiVerticalpagerViewProxy *viewProxy = [[TiVerticalpagerViewProxy alloc] _initWithPageContext:[self pageContext] args:args];
    return viewProxy;
}

#pragma mark - Constants

MAKE_SYSTEM_PROP(INDICATOR_TYPE_HORIZONTAL, 0);
MAKE_SYSTEM_PROP(INDICATOR_TYPE_VERTICAL, 1);

@end
