//
//  SessionViewController.h
//  FlowMaschine2
//
//  Created by Wilken Holle on 06.01.15.
//  Copyright (c) 2015 out there! communication UG. All rights reserved.
//

#define kMotionRecordMaxCount   720
#define kCountdownTime 8

#import "SessionViewController.h"
#import "AppDelegate.h"
#import "WalkSequence+OutStream.h"
#import "GaitEvent.h"
#import "LocationEvent.h"
#import "EnvironmentEvent.h"
#import "SteadyStepEvent.h"
#import "MotionRecord.h"
#import "HeartRateRecord.h"
#import <LocationSense/LocationSense.h>
#import <HeartRateMonitorFramework/HeartRateMonitorFramework.h>
#import "SelfReport+Description.h"
#import "TutorialViewController.h"
#import "PulsingHaloLayer.h"
#import "MultiplePulsingHaloLayer.h"
#import <QuartzCore/QuartzCore.h>
#import "DBManager.h"
#import <WalkingPrimitives/WalkingPrimitives.h>

@interface SessionViewController () <WalkingPrimitivesDelegate, EnvironmentManagerDelegate, HeartRateMonitorManagerDelegate, TutorialViewControllerDelegate> {
    BOOL tutorialHasShownOnce;
}

@property (strong, nonatomic) AppDelegate *appDelegate;
@property (strong, nonatomic) EnvironmentManager *environmentManager;

@property (nonatomic, weak) PulsingHaloLayer *halo;
@property (nonatomic, weak) MultiplePulsingHaloLayer *mutiHalo;

@property (weak, nonatomic) IBOutlet UIButton *stopButton;
@property (weak, nonatomic) IBOutlet UILabel *stopLabel;
@property (weak, nonatomic) IBOutlet UILabel *recordingInfoLabel;

@property (weak, nonatomic) IBOutlet UIView *viewCountdownCounter;
@property (weak, nonatomic) IBOutlet UILabel *labelCountdownCounter;
@property (strong, nonatomic) NSTimer *timerCountdownCounter;
@property (assign, nonatomic) int currentCountdownTime;

@property (weak, nonatomic) IBOutlet UIView *viewStepCounter;
@property (weak, nonatomic) IBOutlet UILabel *labelStepCounter;
@property (weak, nonatomic) IBOutlet UILabel *labelStepLabel;
@property (weak, nonatomic) IBOutlet UILabel *labelStopWatch;
@property (weak, nonatomic) IBOutlet UILabel *labelWalkDistance;
@property (weak, nonatomic) IBOutlet UILabel *labelWalkDistanceUnit;
@property (strong, nonatomic) NSTimer *stopWatchTimer;
@property (nonatomic) BOOL isRecording;

@property (strong, nonatomic) WalkSequence *walkSequenceCurrent;
@property (strong, nonatomic) GaitEvent *lastGaitEvent;
@property (strong, nonatomic) MotionActivity *motionActivity;

@property (assign, nonatomic) double walkDistance;
@property (assign, nonatomic) int numberOfStrides;
@property (assign, nonatomic) int numberOfHeartBeats;
@property (assign, nonatomic) int sumOfHeartRates;

@property (nonatomic, strong) NSNumber *firstMotionTimestamp;
@property (nonatomic, strong) NSNumber *firstHeartRateTimestamp;
@property (nonatomic, strong) NSNumber *firstGaitEventTimestamp;
@property (nonatomic) NSTimeInterval startSelfReportTimestamp;

@property (retain, nonatomic) LocationEvent *lastLocationEvent;

@property (nonatomic, strong) DBManager *dbManager;
@property (nonatomic) int sessionPK;

@property (assign, nonatomic) int steadyStepLevel;
@property (assign, nonatomic) int consecStepCount; // aufeinanderfolgende Schritte
@property (assign, nonatomic) BOOL newEnvironmentWasOnceNil;

@property (assign, nonatomic) int counterForMotionDataExport;
@property (assign, nonatomic) int motionDataUpdatesCounter;
@property (assign, nonatomic) bool pauseMode;
@property (retain, nonatomic) NSMutableArray *motionDataForExport;

@property (assign, nonatomic) int lastSteadyStepLevel;
@property( assign) int speed;


@end

@implementation SessionViewController

- (AppDelegate *)appDelegate
{
    return (AppDelegate *)[[UIApplication sharedApplication] delegate];
}

#pragma mark -
#pragma mark - Setter

- (void)setNumberOfStrides:(int)numberOfStrides
{
    _numberOfStrides = numberOfStrides;
    
    UIApplicationState state = [[UIApplication sharedApplication] applicationState];
    if (!(state == UIApplicationStateBackground || state == UIApplicationStateInactive)) {
        NSUInteger steps = numberOfStrides * 2;
        self.labelStepCounter.text = [WalkSequence getFormatedSteps:steps];
        self.labelStepLabel.text = [WalkSequence getFormatedStepsUnit:steps];
    }
}

- (void)setWalkDistance:(double)walkDistance
{
    _walkDistance = walkDistance;
    
    UIApplicationState state = [[UIApplication sharedApplication] applicationState];
    if (!(state == UIApplicationStateBackground || state == UIApplicationStateInactive)) {
        self.labelWalkDistance.text = [WalkSequence getFormatedDistance:_walkDistance];
        self.labelWalkDistanceUnit.text = [WalkSequence getFormatedDistanceUnit:_walkDistance];
    }
}

#pragma mark -
#pragma mark - UIViewControllerDelegate methods

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(applicationDidBecomeActive) name:@"applicationDidBecomeActive" object:nil];
    
    self.lastSteadyStepLevel = -1;
    self.motionDataUpdatesCounter = 0;
    self.counterForMotionDataExport = 0;
    self.pauseMode = NO;
    self.speed = 0;
    // Set default sonification settings
    //[self.appDelegate.audioController initSetWithName:@"dimens"];
    
    // Initialize WalkingPrimitivesManager
    [[WalkingPrimitivesManager sharedManager] initManagerWithDelegate:self detectionMode: 3];
    //level 1-7 & reset grenzwert
    NSMutableArray *steadyStepLevels = [[NSMutableArray alloc] initWithArray:@[ XsteadyStepLevelsX ]];
    [[WalkingPrimitivesManager sharedManager] initSteadyStepLevels:steadyStepLevels];
    
    
    // Initialize EnvironmentManager
    self.environmentManager = [[EnvironmentManager alloc] init];
    self.environmentManager.delegate = self;
    
    // Initialize DBManager
    self.dbManager = [[DBManager alloc] initWithDatabaseFilename:@"database.sqlite"];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:YES];
    
    /*PulsingHaloLayer *layer = [PulsingHaloLayer layer];
    self.halo = layer;
    self.halo.position = CGPointMake(self.view.frame.size.width / 2, self.view.frame.size.height / 2);
    UIColor *color = [UIColor whiteColor];
    [self.halo setBackgroundColor:color.CGColor];
    self.halo.radius = 200;
    [self.view.layer insertSublayer:self.halo atIndex:1];*/
    
    if (!tutorialHasShownOnce) {
        
        UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"MainDesign" bundle:nil];
        TutorialViewController *tutorialViewController = [storyboard instantiateViewControllerWithIdentifier:@"TutorialViewController"];
        tutorialViewController.delegate = self;
        [self presentViewController:tutorialViewController animated:YES completion:nil];
        
        tutorialHasShownOnce = YES;
    }
    
    if ([self.delegate respondsToSelector:@selector(sessionViewControllerDidAppear:)]) {
        [self.delegate sessionViewControllerDidAppear:self];
    }
}

- (void)tutorialViewControllerClosed:(TutorialViewController *)viewController
{
    [self initRecording];
}

#pragma mark -
#pragma mark - IBActions

- (void)initRecording {
    
    // For starting motion manager updates
    self.firstMotionTimestamp = nil;
    
    self.firstHeartRateTimestamp = nil;
    
    self.firstGaitEventTimestamp = nil;
    
    self.lastLocationEvent = nil;
    
    // Create walking sequence object
    self.walkSequenceCurrent = [NSEntityDescription insertNewObjectForEntityForName:@"WalkSequence" inManagedObjectContext:self.appDelegate.managedObjectContext];
    self.walkSequenceCurrent.date = [NSDate date];
    [self.appDelegate saveContext];
    
    // Create wave file
    NSDateFormatter *dateTimeFormatter = [[NSDateFormatter alloc] init];
    [dateTimeFormatter setDateFormat:@"yyyy-MM-dd--HH-mm-ss"];
    NSString *filename = [NSString stringWithFormat:@"%@-audio",[dateTimeFormatter stringFromDate:self.walkSequenceCurrent.date]];
    [self.appDelegate.audioController setUpAudioRecord:filename];
    
    NSManagedObjectID *sessionID = self.walkSequenceCurrent.objectID;
    self.sessionPK = [[[[[sessionID URIRepresentation] absoluteString] lastPathComponent] substringFromIndex:1] intValue];
    
    // Set date properties
    NSCalendar *calender = [NSCalendar currentCalendar];
    NSDateComponents *dateComponent = [calender components:(NSCalendarUnitWeekOfYear) fromDate:[NSDate date]];
    self.walkSequenceCurrent.weekOfYear = [NSNumber numberWithLong:[dateComponent weekOfYear]];
    
    // Set start values
    self.numberOfStrides = 1;
    self.numberOfHeartBeats = 0;
    self.sumOfHeartRates = 0;
    self.walkDistance = 0;
    
    self.isRecording = NO;
    
    self.newEnvironmentWasOnceNil = NO;
    
    [self startSessionCountdown];
/**
 * ueberfluessig, wird auch in den infos gespeichert
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    bool sound = [defaults boolForKey:@"audioStatus"];
    
    if(sound) {
        NSManagedObjectContext *context = self.appDelegate.managedObjectContext;
        NSString *entityName = @"GaitEvent";
        GaitEvent *gaitEvent = [NSEntityDescription insertNewObjectForEntityForName:entityName inManagedObjectContext:context];
        
        gaitEvent.timestamp = 0;
        gaitEvent.name = @"SoundOn";
        gaitEvent.timeInterval = 0;
        gaitEvent.confidence = 100;
        [self.walkSequenceCurrent addGaitEventsObject:gaitEvent];
    }
    **/
}

- (IBAction)startRecording:(id)sender {
    
    self.motionDataForExport = [[NSMutableArray alloc] initWithCapacity:10];
    
    //Start detection
    [[WalkingPrimitivesManager sharedManager] startManager:YES :YES :YES :XsteadyStepSwitcherX :YES :YES];
    
    // Start updating environment
    [self.environmentManager startUpdatingEnvironment];
    
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"LocationEvent" inManagedObjectContext:self.appDelegate.managedObjectContext];
    [fetchRequest setEntity:entity];
    
    // Specify criteria for filtering which objects to fetch
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"name == %@ || name == %@ || name == %@", @"Start", @"Turn", @"Stop"];
    [fetchRequest setPredicate:predicate];
    
    // Specify how the fetched objects should be sorted
    NSSortDescriptor *sortDescriptorTimestamp = [[NSSortDescriptor alloc] initWithKey:@"timestamp"
                                                                            ascending:YES];
    NSSortDescriptor *sortDescriptorName = [[NSSortDescriptor alloc] initWithKey:@"name"
                                                                       ascending:YES];
    [fetchRequest setSortDescriptors:[NSArray arrayWithObjects:sortDescriptorName, sortDescriptorTimestamp, nil]];
    
    NSError *error = nil;
    NSArray *fetchedObjects = [self.appDelegate.managedObjectContext executeFetchRequest:fetchRequest error:&error];
    if (fetchedObjects == nil) {
        NSLog(@"Core data error");
    }
    
    for (LocationEvent *locationEvent in fetchedObjects) {
        CLLocation *location = [[CLLocation alloc] initWithLatitude:[locationEvent.latitude doubleValue] longitude:[locationEvent.longitude doubleValue]];
        [self.environmentManager createEnvironmentStructureWithNamePrefix:locationEvent.name location:location];
    }
    
    // Create start environment
    [self.environmentManager createEnvironmentStructureWithNamePrefix:@"Start"];
    
    // Start heart rate monitor updates
    if (self.appDelegate.heartRateMonitorManager.hasConnection) {
        self.appDelegate.heartRateMonitorManager.delegate = self;
        [self.appDelegate.heartRateMonitorManager startMonitoring];
    }
    
    // Start stop watch
    [self startStopWatch];
    
    self.isRecording = YES;
}

- (IBAction)stopRecording:(id)sender {
    // Stop stop watch
    [self.stopWatchTimer invalidate];
    
    // Disable sound and stop audio recording
    [self.appDelegate.audioController endGKsession];
    
    // Stop updating motion
    [[WalkingPrimitivesManager sharedManager] stopManager];
    
    // Stop updating environment
    [self.environmentManager stopUpdatingEnvironment];
    
    // Stop heart rate monitor updates
    if (self.appDelegate.heartRateMonitorManager.hasConnection) {
        [self.appDelegate.heartRateMonitorManager stopMonitoring];
    }
    
    self.isRecording = NO;
    
    // Set to nil
    self.firstMotionTimestamp = nil;
    self.firstHeartRateTimestamp = nil;
    self.firstGaitEventTimestamp = nil;
    [[NSNotificationCenter defaultCenter] removeObserver:self name:@"applicationDidBecomeActive" object:nil];
    
    // Set walking sequence properties
    NSDate *now = [NSDate date];
    double duration = [now timeIntervalSinceDate:self.walkSequenceCurrent.date];
    self.walkSequenceCurrent.duration = [NSNumber numberWithDouble:duration];
    self.walkSequenceCurrent.numberOfSteps = [NSNumber numberWithInteger:(self.numberOfStrides * 2)];
    if (self.numberOfHeartBeats != 0) {
        self.walkSequenceCurrent.averageHeartRate = [NSNumber numberWithDouble:self.sumOfHeartRates / self.numberOfHeartBeats];
    }
    
    self.walkSequenceCurrent.distanceInM = [NSNumber numberWithDouble:[self calculateDistance]];
    
    // Add last and first location event
    NSArray *locationEvents = [[self.walkSequenceCurrent.locationEvents allObjects] sortedArrayUsingDescriptors:@[[NSSortDescriptor sortDescriptorWithKey:@"timestamp" ascending:YES]]];
    LocationEvent *firstLocationEvent = [locationEvents firstObject];
    [self addCopyFromLocationEvent:firstLocationEvent withName:NSLocalizedString(@"Start", @"Datenbankeintrag: Startposition") TimeDifferenceInSeconds:-0.001];
    
    LocationEvent *lastLocationEvent = [locationEvents lastObject];
    [self addCopyFromLocationEvent:lastLocationEvent withName:NSLocalizedString(@"Stop", @"Datenbankeintrag: Endposition") TimeDifferenceInSeconds:0.001];

    // Show self report
    [self showSelfReport];
}


#pragma mark -
#pragma mark - WalkingPrimitivesManagerDelegate methods

- (void) walkingPrimitivesManager:(WalkingPrimitivesManager *)manager didUpdateGaitEvent:(PrimitiveGaitEvent *)gaitEvent {
        NSLog(@"%@", gaitEvent.name);
    // Direction Change Events
    if ([gaitEvent.name isEqualToString:TURN_L] || [gaitEvent.name isEqualToString:TURN_R]) {
        if([gaitEvent.name isEqualToString:TURN_L]){
            [self.appDelegate.audioController setPanningtoListFromR:0.1 FromL:maxVolTurn toR:maxVolTurn toL:maxVolTurn]; //"1" ist die Lautstärke die der Nutzer einstellen kann, "0.1" ist das min (Beginn-Lautstärke)
        }
        else {
            [self.appDelegate.audioController setPanningtoListFromR:maxVolTurn FromL:0.1 toR:maxVolTurn toL:maxVolTurn];
        }
        [self turn:self];
    }
    
    // Gait events
    if ([gaitEvent.name isEqualToString:MIDSWING] || [gaitEvent.name isEqualToString:INITIALSWING]) {
        
        self.consecStepCount +=1;
        
        // Compute confidence
        int16_t confidence = 0;
        if ([self.lastGaitEvent.name isEqualToString:INITIALSWING] && [gaitEvent.name isEqualToString:MIDSWING]) {
            confidence += 50;
            self.numberOfStrides++;
        } else if ([self.lastGaitEvent.name isEqualToString:gaitEvent.name]) {
            confidence += 45;
            self.numberOfStrides++;
        }
        if ([self.lastGaitEvent.name isEqualToString:MIDSWING] && [gaitEvent.name isEqualToString:INITIALSWING]) {
            confidence += 50;
        }
        
        if ([self.motionActivity.name isEqualToString:@"Walking"] || [self.motionActivity.name isEqualToString:@"Running"]) {
            confidence += 50;
        } else if ([self.motionActivity.name isEqualToString:@"Transport"]) {
            confidence += 40;
        } else if ([self.motionActivity.name isEqualToString:@"NoAccuracy"]) {
            confidence += 30;
        }
        
        // Store gait event in the database
        NSManagedObjectContext *context = self.appDelegate.managedObjectContext;
        NSString *entityName = @"GaitEvent";
        GaitEvent *ge = [NSEntityDescription insertNewObjectForEntityForName:entityName inManagedObjectContext:context];
        
        if (self.firstGaitEventTimestamp == nil) {
            ge.timestamp = fabs([self.walkSequenceCurrent.date timeIntervalSinceNow]);
            self.firstGaitEventTimestamp = [NSNumber numberWithDouble:gaitEvent.timestamp - ge.timestamp];
        } else {
            ge.timestamp = gaitEvent.timestamp - [self.firstGaitEventTimestamp doubleValue];
        }
        
        ge.name = gaitEvent.name;
        ge.timeInterval = gaitEvent.timeInterval;
        ge.confidence = confidence;
        [self.walkSequenceCurrent addGaitEventsObject:ge];
        
        // Hold last gait event
        self.lastGaitEvent = ge;
    }
    
    // Start audio feedback
    
    //Gait AudioFeedback Initial
    if([gaitEvent.name isEqualToString:INITIALSWING]) {
        
       /* if(self.steadyStepLevel < 5){
            
            //steadySteplevel 0-5: gait
            [self.appDelegate.audioController sonifyGait:2 withPace:gaitEvent.timeInterval];
        }
        //steadySteplevel 1-5: +bass
        if(self.steadyStepLevel>0 && self.steadyStepLevel<6){
            [self.appDelegate.audioController sonifyGait:3 withPace:(gaitEvent.timeInterval/2)];
        }
        
        //steadySteplevel 5-6: +white
        if(self.steadyStepLevel > 3 &&self.steadyStepLevel < 7 ){
            [self.appDelegate.audioController sonifyGait:4 withPace:(gaitEvent.timeInterval/2)];
        }*/
        switch(self.speed){
            case 0: //slow
                XinitialswingX
                break;
            case 1: //fast
                XinitialswingfastX
                break;
                
        }
        XsteadyStepInitialSwingX
    }
    
    //Gait AudioFeedback Midswing
    else if ([gaitEvent.name isEqualToString:MIDSWING]){//&& self.steadyStepLevel<5) {
        //[self.appDelegate.audioController sonifyGait:1 withPace:gaitEvent.timeInterval];
        switch(self.speed){
            case 0: //slow
                XmidswingX
                break;
            case 1: //fast
                XmidswingfastX
                break;
        }
        XsteadyStepMidSwingX
    }
    //Standing AudioFeedback
    else if([gaitEvent.name isEqualToString:STANDING]&& self.steadyStepLevel<6) {
        if (self.consecStepCount>=5) {//verhindert standing sound nach einem schritt
            [self.appDelegate.audioController setPanningtoListFromR:volStanding FromL:volStanding toR:volStanding toL: volStanding];
            [self.appDelegate.audioController playSoundmark:@"Standing"]; //Standing ist der eintrag in plist
        }
        self.consecStepCount=0;
    }
    
    else if([gaitEvent.name isEqualToString:Turn_Around]){
        //[self.appDelegate.audioController playSoundmark:Turn_Around];
        NSLog(Turn_Around);
    }
    
    //Slower/Faster AudioFeedback
    if([gaitEvent.name isEqualToString:SLOWER]) {
        self.speed = 0;
    }
    
    else if([gaitEvent.name isEqualToString:FASTER]) {
        self.speed = 1;
    }
    
    // End AudioFeedback
    if([gaitEvent.name isEqualToString:TURN_L] || [gaitEvent.name isEqualToString:TURN_R] || [gaitEvent.name isEqualToString:SLOWER] || [gaitEvent.name isEqualToString:SLOWER_S] || [gaitEvent.name isEqualToString:FASTER] || [gaitEvent.name isEqualToString:Turn_Around] || [gaitEvent.name isEqualToString:STANDING]) {
        NSManagedObjectContext *context = self.appDelegate.managedObjectContext;
        NSString *entityName = @"GaitEvent";
        GaitEvent *ge = [NSEntityDescription insertNewObjectForEntityForName:entityName inManagedObjectContext:context];
        if (self.firstGaitEventTimestamp == nil) {
            ge.timestamp = fabs([self.walkSequenceCurrent.date timeIntervalSinceNow]);
            self.firstGaitEventTimestamp = [NSNumber numberWithDouble:gaitEvent.timestamp - ge.timestamp];
        } else {
            ge.timestamp = gaitEvent.timestamp - [self.firstGaitEventTimestamp doubleValue];
        }
        ge.name = gaitEvent.name;
        ge.timeInterval = gaitEvent.timeInterval;
        ge.confidence = 100;
        [self.walkSequenceCurrent addGaitEventsObject:ge];
    }
}

- (void) walkingPrimitivesManager:(WalkingPrimitivesManager *)manager didUpdateSteadyStepEvent:(PrimitiveSteadyStepEvent *)steadyStepEvent {
    NSLog(@"steady step level: %@", steadyStepEvent.level);
    
    SteadyStepEvent *sseIn  = [NSEntityDescription insertNewObjectForEntityForName:@"SteadyStepEvent" inManagedObjectContext:self.appDelegate.managedObjectContext];
    SteadyStepEvent *sseOut  = [NSEntityDescription insertNewObjectForEntityForName:@"SteadyStepEvent" inManagedObjectContext:self.appDelegate.managedObjectContext];
    sseIn.action = @"In";
    sseOut.action = @"Out";
    sseIn.timestamp = [NSNumber numberWithDouble:fabs([self.walkSequenceCurrent.date timeIntervalSinceNow]-0.001)];
    sseOut.timestamp = [NSNumber numberWithDouble:fabs([self.walkSequenceCurrent.date timeIntervalSinceNow])];
    
    if([steadyStepEvent.level intValue] != 0) {
        sseIn.level = steadyStepEvent.level;
        sseOut.level = [NSNumber numberWithInt:([steadyStepEvent.level intValue]-1)];
    } else {
        if(self.lastSteadyStepLevel == -1) {
            sseIn.level = steadyStepEvent.level;
            sseOut.level = steadyStepEvent.level;
        } else {
            sseIn.level = steadyStepEvent.level;
            sseOut.level = [NSNumber numberWithInt:self.lastSteadyStepLevel];
        }
        
    }
    
    [self.walkSequenceCurrent addSteadyStepEventsObject:sseOut];
    [self.walkSequenceCurrent addSteadyStepEventsObject:sseIn];
    
    // steadysteplevel 1-3: gait + bass; 4: gait+ bass+bliss; 5: gait+ bass+bliss+white; 6: white; 7: silence;
    switch ([steadyStepEvent.level intValue]) {
        /*case 0:
            //mute gait4
            [self.appDelegate.audioController gaitVol:4 withEnvelope:[NSArray arrayWithObjects:[NSNumber numberWithFloat:0],[NSNumber numberWithFloat:1000], nil]];
            [self.appDelegate.audioController stopLayerWithName:@"WalkAlong"];
            [self.appDelegate.audioController setPanningtoGait:1 withR:1 withL:0.2];
            [self.appDelegate.audioController setPanningtoGait:2 withR:0.2 withL:1];
            self.steadyStepLevel=0;
            break;
        case 1:
            //demute gait4
            [self.appDelegate.audioController gaitVol:4 withEnvelope:[NSArray arrayWithObjects:[NSNumber numberWithFloat:0.6],[NSNumber numberWithFloat:1000], nil]];
            [self.appDelegate.audioController sonifyAtBang:1 toGait:3];
            [self.appDelegate.audioController sonifyMode:1 toGait:3 withParameters:[NSArray arrayWithObjects:[NSNumber numberWithFloat:2], nil]];
            [self.appDelegate.audioController setPanningtoGait:1 withR:0.7 withL:0.1];
            [self.appDelegate.audioController setPanningtoGait:2 withR:0.1 withL:0.7];
            self.steadyStepLevel=1;
            break;
        case 2:
            [self.appDelegate.audioController sonifyAtBang:1 toGait:3];
            [self.appDelegate.audioController sonifyMode:1 toGait:3 withParameters:[NSArray arrayWithObjects:[NSNumber numberWithFloat:4], nil]];
            [self.appDelegate.audioController setPanningtoGait:1 withR:0.6 withL:0.1];
            [self.appDelegate.audioController setPanningtoGait:2 withR:0.1 withL:0.6];
            self.steadyStepLevel=2;
            break;
        case 3:
            [self.appDelegate.audioController playLayerWithName:@"WalkAlong" withEnvelope:0.8];
            self.steadyStepLevel=3;
            break;
        case 4:
            [self.appDelegate.audioController setPanningtoGait:1 withR:0.4 withL:0.1];
            [self.appDelegate.audioController setPanningtoGait:2 withR:0.1 withL:0.4];
            self.steadyStepLevel=4;
            break;
        case 5:
            self.steadyStepLevel=5;
            break;
        case 6:
            self.steadyStepLevel=6;
            break;
        case 7:
            [self.appDelegate.audioController stopLayerWithName:@"WalkAlong"];
            self.steadyStepLevel=7;
            break;*/
        case 0:
            XstopAllLayersX
            self.steadyStepLevel=0;
            break;
            
            XsteadyStepCasesX
        default:
            break;
    }
    
    self.lastSteadyStepLevel = [steadyStepEvent.level intValue];
}

- (void) walkingPrimitivesManager:(WalkingPrimitivesManager *)manager didUpdateDeviceMotion:(CMDeviceMotion *) deviceMotion {
    self.motionDataUpdatesCounter++;
    if(self.motionDataUpdatesCounter >= 2) {
        self.counterForMotionDataExport++;
        //NSString *query = [NSString stringWithFormat:@"INSERT INTO ZMOTIONRECORD VALUES(NULL, 4, 1, %d, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f)", self.sessionPK, motion.attitude.pitch, motion.attitude.roll, motion.attitude.yaw, motion.gravity.x, motion.gravity.y, motion.gravity.z, motion.rotationRate.x, motion.rotationRate.y, motion.rotationRate.z, timestamp, motion.userAcceleration.x, motion.userAcceleration.y, motion.userAcceleration.z];
        
        //[self.dbManager executeQuery:query];
        self.motionDataUpdatesCounter = 0;
        [self.motionDataForExport addObject:deviceMotion];
        
        NSMutableString *q = [NSMutableString stringWithFormat:@"INSERT INTO ZMOTIONRECORD VALUES"];
        
        if(self.counterForMotionDataExport >= 10) {
            int count = 1;
            for (CMDeviceMotion *m in self.motionDataForExport) {
                double timestamp;
                if (self.firstMotionTimestamp == nil) {
                    timestamp = fabs([self.walkSequenceCurrent.date timeIntervalSinceNow]);
                    self.firstMotionTimestamp = [NSNumber numberWithDouble:m.timestamp - timestamp];
                } else {
                    timestamp = m.timestamp - [self.firstMotionTimestamp doubleValue];
                }
                NSString *s = [NSString stringWithFormat:@"(NULL, 4, 1, %d, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f)", self.sessionPK, m.attitude.pitch, m.attitude.roll, m.attitude.yaw, m.gravity.x, m.gravity.y, m.gravity.z, m.rotationRate.x, m.rotationRate.y, m.rotationRate.z, timestamp, m.userAcceleration.x, m.userAcceleration.y, m.userAcceleration.z];
                [q appendString:s];
                if(count < [self.motionDataForExport count]) {
                    [q appendString:@","];
                }
                count++;
            }
            [self.motionDataForExport removeAllObjects];
            self.counterForMotionDataExport = 0;
            [self.dbManager executeQuery:q];
        }
    }
}

- (void) walkingPrimitivesManager:(WalkingPrimitivesManager *)manager didFailWithError:(NSError *)error {
    NSLog(@"# walkingPrimitivesManager:didFailWithError: = %@", error.debugDescription);
}

#pragma mark -
#pragma mark - EnvironmentManagerDelegate methods

- (void)environmentManager:(EnvironmentManager *)manager didUpdateToEnvironment:(Environment *)newEnvironment fromEnvironment:(Environment *)oldEnvironment
{

    // Wenn man in die Ringstruktur kommt - ist oldEnvironment = nil und newEnvironment.name = {Start|Stop|Turn}OuterRing
    // Dann - oldEnvironment = {Start|Stop|Turn}OuterRing und newEnvironment.name = {Start|Stop|Turn}MiddleRing
    // In der Mitte oldEnvironment = {Start|Stop|Turn}MiddleRing und newEnvironment.name = {Start|Stop|Turn}InnerRing
    
    // Man verlässt die Ringstruktur - {Start|Stop|Turn}InnerRing und newEnvironment.name = {Start|Stop|Turn}MiddleRing
    // Dann - {Start|Stop|Turn}MiddleRing und newEnvironment.name = {Start|Stop|Turn}OuterRing
    // aus der Ringstruktur - {Start|Stop|Turn}OuterRing und newEnvironment = nil
    NSLog(@"# environmentManager:didUpdateToEnvironment: = %@ fromEnvironment: = %@", newEnvironment.name, oldEnvironment.name);
    
    if(oldEnvironment == nil) {
        //in neues env
        EnvironmentEvent *envEvent  = [NSEntityDescription insertNewObjectForEntityForName:@"EnvironmentEvent" inManagedObjectContext:self.appDelegate.managedObjectContext];
        envEvent.action = [NSString stringWithFormat:@"In"];
        envEvent.name = newEnvironment.name;
        envEvent.latitude = [NSNumber numberWithDouble:newEnvironment.region.center.latitude];
        envEvent.longitude = [NSNumber numberWithDouble:newEnvironment.region.center.longitude];
        envEvent.timestamp = [NSNumber numberWithDouble:fabs([self.walkSequenceCurrent.date timeIntervalSinceNow])];
        [self.walkSequenceCurrent addEnvironmentEventsObject:envEvent];
    } else {
        if(newEnvironment == nil) {
            self.newEnvironmentWasOnceNil = YES;
            //aus env raus
            EnvironmentEvent *envEvent  = [NSEntityDescription insertNewObjectForEntityForName:@"EnvironmentEvent" inManagedObjectContext:self.appDelegate.managedObjectContext];
            envEvent.action = [NSString stringWithFormat:@"Out"];
            envEvent.name = oldEnvironment.name;
            envEvent.latitude = [NSNumber numberWithDouble:oldEnvironment.region.center.latitude];
            envEvent.longitude = [NSNumber numberWithDouble:oldEnvironment.region.center.longitude];
            envEvent.timestamp = [NSNumber numberWithDouble:fabs([self.walkSequenceCurrent.date timeIntervalSinceNow])];
            [self.walkSequenceCurrent addEnvironmentEventsObject:envEvent];
        } else {
            //aus einem raus
            EnvironmentEvent *envEventOut  = [NSEntityDescription insertNewObjectForEntityForName:@"EnvironmentEvent" inManagedObjectContext:self.appDelegate.managedObjectContext];
            envEventOut.action = [NSString stringWithFormat:@"Out"];
            envEventOut.name = oldEnvironment.name;
            envEventOut.latitude = [NSNumber numberWithDouble:oldEnvironment.region.center.latitude];
            envEventOut.longitude = [NSNumber numberWithDouble:oldEnvironment.region.center.longitude];
            envEventOut.timestamp = [NSNumber numberWithDouble:fabs([self.walkSequenceCurrent.date timeIntervalSinceNow])];
            [self.walkSequenceCurrent addEnvironmentEventsObject:envEventOut];
            
            //in ein anderes
            EnvironmentEvent *envEventIn  = [NSEntityDescription insertNewObjectForEntityForName:@"EnvironmentEvent" inManagedObjectContext:self.appDelegate.managedObjectContext];
            envEventIn.action = [NSString stringWithFormat:@"In"];
            envEventIn.name = newEnvironment.name;
            envEventIn.latitude = [NSNumber numberWithDouble:newEnvironment.region.center.latitude];
            envEventIn.longitude = [NSNumber numberWithDouble:newEnvironment.region.center.longitude];
            envEventIn.timestamp = [NSNumber numberWithDouble:fabs([self.walkSequenceCurrent.date timeIntervalSinceNow])];
            [self.walkSequenceCurrent addEnvironmentEventsObject:envEventIn];
            
        }
    }

    //start audio feedback environments
    if(newEnvironment != nil && oldEnvironment == nil && self.newEnvironmentWasOnceNil) {
        
        [self.appDelegate.audioController playLayerWithName:@"Env" withEnvelope:0.3];
        
    }
    
    //end audio feedback environments
    
    if(newEnvironment == nil && oldEnvironment != nil && self.newEnvironmentWasOnceNil) {
        
        [self.appDelegate.audioController stopLayerWithName:@"Env"];
        
    }
}

- (void)environmentManager:(EnvironmentManager *)manager didFailWithError:(NSError *)error
{
    /* Debug logs */
//    NSLog(@"# environmentManager:didFailWithError: = %@", error.debugDescription);
}

- (void)environmentManager:(EnvironmentManager *)manager didUpdateLocations:(NSArray *)locations
{
    CLLocation *currentLocation = nil;
    LocationEvent *locationEvent;
    for(CLLocation *location in locations) {
        
        locationEvent = [NSEntityDescription insertNewObjectForEntityForName:@"LocationEvent" inManagedObjectContext:self.appDelegate.managedObjectContext];
        double accuracy = sqrt(pow(location.horizontalAccuracy, 2) + pow(location.verticalAccuracy, 2));
        locationEvent.accuracy = [NSNumber numberWithDouble:accuracy];
        locationEvent.speed = [NSNumber numberWithDouble:location.speed];
        locationEvent.timestamp = [NSNumber numberWithDouble:[location.timestamp timeIntervalSince1970]];
        locationEvent.latitude = [NSNumber numberWithDouble:location.coordinate.latitude];
        locationEvent.longitude = [NSNumber numberWithDouble:location.coordinate.longitude];
        locationEvent.name = NSLocalizedString(@"Position", @"Datenbankeintrag: Position");
        [self.walkSequenceCurrent addLocationEventsObject:locationEvent];
        if(accuracy <= 25.0) {
            currentLocation = location;
        }
    }
    
    if (self.walkSequenceCurrent.locationEvents.count > 1) {
        CLLocation *lastLocation = [[CLLocation alloc] initWithLatitude:[self.lastLocationEvent.latitude doubleValue] longitude:[self.lastLocationEvent.longitude doubleValue]];
        CLLocationDistance distance = [lastLocation distanceFromLocation:currentLocation];
        if (distance >= 0.0) {
           self.walkDistance += distance;
        }
    }
    
    self.lastLocationEvent = locationEvent;
}

- (void)environmentManager:(EnvironmentManager *)manager didUpdateMotionActivity:(MotionActivity *)motionActivity
{
    self.motionActivity = motionActivity;
}

#pragma mark -
#pragma mark - HeartRateMonitorManagerDelegate methods

- (void)heartRateMonitorManager:(HeartRateMonitorManager *)manager didReceiveHeartrateMonitorData:(HeartRateMonitorData *)data fromHeartRateMonitorDevice:(HeartRateMonitorDevice *)device
{
    if(self.isRecording) {
        
        self.numberOfHeartBeats++;
        self.sumOfHeartRates = self.sumOfHeartRates + data.heartRate;
        
        long rrDataCount = [data.rrTimes count];
        for (int i = 0; i < rrDataCount; i++) {
            double timestamp;
            
            if (self.firstHeartRateTimestamp == nil) {
                timestamp = fabs([self.walkSequenceCurrent.date timeIntervalSinceNow]);
                
                self.firstHeartRateTimestamp = [NSNumber numberWithDouble:[[data.rrTimes objectAtIndex:i] doubleValue] - timestamp];
            } else {
                timestamp = [[data.rrTimes objectAtIndex:i] doubleValue] - [self.firstHeartRateTimestamp doubleValue];
            }
            
           NSString *query = [NSString stringWithFormat:@"INSERT INTO ZHEARTRATERECORD VALUES(NULL, 2, 1, %d, %d, %f, %f)", data.heartRate, self.sessionPK, [[data.rrIntervals objectAtIndex:i] doubleValue], timestamp];
            
            [self.dbManager executeQuery:query];
        }
    }
}

- (void)heartRateMonitorManager:(HeartRateMonitorManager *)manager
didDisconnectHeartrateMonitorDevice:(CBPeripheral *)heartRateMonitorDevice
                          error:(NSError *)error
{
}

- (void)heartRateMonitorManager:(HeartRateMonitorManager *)manager
didConnectHeartrateMonitorDevice:(CBPeripheral *)heartRateMonitorDevice
{
    [manager startMonitoring];
}

#pragma mark -
#pragma mark - LikertScaleViewControllerDelegate methods

- (void)likertScaleViewController:(LikertScaleViewController *)viewController didFinishWithResponses:(NSArray *)responses atDate:(NSDate *)date
{
    SelfReport *selfReport = [NSEntityDescription insertNewObjectForEntityForName:@"SelfReport" inManagedObjectContext:self.appDelegate.managedObjectContext];
    selfReport.timestamp = self.startSelfReportTimestamp;
    selfReport.duration = fabs([self.walkSequenceCurrent.date timeIntervalSinceNow]) - selfReport.timestamp;
    NSDictionary *flowShortScaleFactors = [self calculateFlowShortScaleFactorsFromResponses:responses];
    selfReport.flow = [[flowShortScaleFactors objectForKey:@"flow"] floatValue];
    selfReport.flowSD = [[flowShortScaleFactors objectForKey:@"flowSD"] floatValue];
    selfReport.fluency = [[flowShortScaleFactors objectForKey:@"fluency"] floatValue];
    selfReport.fluencySD = [[flowShortScaleFactors objectForKey:@"fluencySD"] floatValue];
    selfReport.absorption = [[flowShortScaleFactors objectForKey:@"absorption"] floatValue];
    selfReport.absorptionSD = [[flowShortScaleFactors objectForKey:@"absorptionSD"] floatValue];
    selfReport.anxiety = [[flowShortScaleFactors objectForKey:@"anxiety"] floatValue];
    selfReport.anxietySD = [[flowShortScaleFactors objectForKey:@"anxietySD"] floatValue];
    selfReport.fit = [[flowShortScaleFactors objectForKey:@"fit"] floatValue];
    selfReport.fitSD = [[flowShortScaleFactors objectForKey:@"fitSD"] floatValue];
    
    [self.walkSequenceCurrent setSelfReport:selfReport];
    [self.appDelegate saveContext];
    [self.appDelegate.managedObjectContext reset];
    self.appDelegate.managedObjectContext = nil;
    
    [viewController dismissViewControllerAnimated:YES
                                       completion:^{
                                           UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"Klasse"
                                                                                             message:@"Dein Gang wurde erfolgreich gespeichert. Weitere Details kannst du in der Statistik einsehen."
                                                                                            delegate:self
                                                                                   cancelButtonTitle:@"OK"
                                                                                   otherButtonTitles:nil];
                                           
                                           [message show];
                                       }];
}

- (void)likertScaleViewControllerCancelled:(LikertScaleViewController *)viewController
{
    [viewController dismissViewControllerAnimated:YES
                                       completion:^{
                                           UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"Klasse"
                                                                                             message:@"Dein Gang wurde erfolgreich gespeichert. Weitere Details kannst du in der Statistik einsehen."
                                                                                            delegate:self
                                                                                   cancelButtonTitle:@"OK"
                                                                                   otherButtonTitles:nil];
                                           
                                           [message show];
                                       }];
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
    [self dismissViewControllerAnimated:NO completion:^{
        if ([self.delegate respondsToSelector:@selector(sessionViewControllerDidDisappear:)]) {
            [self.delegate sessionViewControllerDidDisappear:self];
        }
    }];
}

#pragma mark -
#pragma mark - Self-report methods

- (void)showSelfReport
{
    self.startSelfReportTimestamp = fabs([self.walkSequenceCurrent.date timeIntervalSinceNow]);
    //AudioServicesPlaySystemSound(1008);
    [self presentViewController:[self flowShortScaleViewControllerFromStoryboard] animated:YES completion:nil];
}

- (LikertScaleViewController *)flowShortScaleViewControllerFromStoryboard
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"MainDesign" bundle:nil];
    LikertScaleViewController *flowShortScaleViewController = (LikertScaleViewController *)[storyboard instantiateViewControllerWithIdentifier:@"LikertScale"];
    
    flowShortScaleViewController.delegate = self;
    flowShortScaleViewController.itemLabelTexts = @[
                                                    NSLocalizedString(@"Ich fühle mich optimal beansprucht.", @"Ich fühle mich optimal beansprucht."),
                                                    NSLocalizedString(@"Meine Gedanken bzw. Aktivitäten laufen flüssig und glatt.", @"Meine Gedanken bzw. Aktivitäten laufen flüssig und glatt."),
                                                    NSLocalizedString(@"Ich merke gar nicht, wie die Zeit vergeht.", @"Ich merke gar nicht, wie die Zeit vergeht."),
                                                    NSLocalizedString(@"Ich habe keine Mühe, mich zu konzentrieren.", @"Ich habe keine Mühe, mich zu konzentrieren."),
                                                    NSLocalizedString(@"Mein Kopf ist völlig klar.", @"Mein Kopf ist völlig klar."),
                                                    NSLocalizedString(@"Ich bin ganz vertieft in das, was ich gerade mache.", @"Ich bin ganz vertieft in das, was ich gerade mache."),
                                                    NSLocalizedString(@"Die richtigen Gedanken/Bewegungen kommen wie von selbst.", @"Die richtigen Gedanken/Bewegungen kommen wie von selbst."),
                                                    NSLocalizedString(@"Ich weiß bei jedem Schritt, was ich zu tun habe.", @"Ich weiß bei jedem Schritt, was ich zu tun habe."),
                                                    NSLocalizedString(@"Ich habe das Gefühl, den Ablauf unter Kontrolle zu haben.", @"Ich habe das Gefühl, den Ablauf unter Kontrolle zu haben."),
                                                    NSLocalizedString(@"Ich bin völlig selbstvergessen.", @"Ich bin völlig selbstvergessen."),
                                                    NSLocalizedString(@"Es steht etwas für mich Wichtiges auf dem Spiel.", @"Es steht etwas für mich Wichtiges auf dem Spiel."),
                                                    NSLocalizedString(@"Ich darf jetzt keine Fehler machen.", @"Ich darf jetzt keine Fehler machen."),
                                                    NSLocalizedString(@"Ich mache mir Sorgen über einen Misserfolg.", @"Ich mache mir Sorgen über einen Misserfolg."),
                                                    NSLocalizedString(@"Verglichen mit allen anderen Tätigkeiten, die ich sonst mache, ist die jetzige Tätigkeit...", @"Verglichen mit allen anderen Tätigkeiten, die ich sonst mache, ist die jetzige Tätigkeit..."),
                                                    NSLocalizedString(@"Ich denke, meine Fähigkeiten auf diesem Gebiet sind...", @"Ich denke, meine Fähigkeiten auf diesem Gebiet sind..."),
                                                    NSLocalizedString(@"Für mich persönlich sind die jetzigen Anforderungen...", @"Für mich persönlich sind die jetzigen Anforderungen...")
                                                    ];
    flowShortScaleViewController.itemSegments = @[
                                                  @7,
                                                  @7,
                                                  @7,
                                                  @7,
                                                  @7,
                                                  @7,
                                                  @7,
                                                  @7,
                                                  @7,
                                                  @7,
                                                  @7,
                                                  @7,
                                                  @7,
                                                  @9,
                                                  @9,
                                                  @9
                                                  ];
    flowShortScaleViewController.scaleLabels = @[
                                                 @[NSLocalizedString(@"Trifft nicht zu", @"Trifft nicht zu"), NSLocalizedString(@"teils-teils", @"teils-teils"), NSLocalizedString(@"Trifft zu", @"Trifft zu")],
                                                 @[NSLocalizedString(@"Trifft nicht zu", @"Trifft nicht zu"), NSLocalizedString(@"teils-teils", @"teils-teils"), NSLocalizedString(@"Trifft zu", @"Trifft zu")],
                                                 @[NSLocalizedString(@"Trifft nicht zu", @"Trifft nicht zu"), NSLocalizedString(@"teils-teils", @"teils-teils"), NSLocalizedString(@"Trifft zu", @"Trifft zu")],
                                                 @[NSLocalizedString(@"Trifft nicht zu", @"Trifft nicht zu"), NSLocalizedString(@"teils-teils", @"teils-teils"), NSLocalizedString(@"Trifft zu", @"Trifft zu")],
                                                 @[NSLocalizedString(@"Trifft nicht zu", @"Trifft nicht zu"), NSLocalizedString(@"teils-teils", @"teils-teils"), NSLocalizedString(@"Trifft zu", @"Trifft zu")],
                                                 @[NSLocalizedString(@"Trifft nicht zu", @"Trifft nicht zu"), NSLocalizedString(@"teils-teils", @"teils-teils"), NSLocalizedString(@"Trifft zu", @"Trifft zu")],
                                                 @[NSLocalizedString(@"Trifft nicht zu", @"Trifft nicht zu"), NSLocalizedString(@"teils-teils", @"teils-teils"), NSLocalizedString(@"Trifft zu", @"Trifft zu")],
                                                 @[NSLocalizedString(@"Trifft nicht zu", @"Trifft nicht zu"), NSLocalizedString(@"teils-teils", @"teils-teils"), NSLocalizedString(@"Trifft zu", @"Trifft zu")],
                                                 @[NSLocalizedString(@"Trifft nicht zu", @"Trifft nicht zu"), NSLocalizedString(@"teils-teils", @"teils-teils"), NSLocalizedString(@"Trifft zu", @"Trifft zu")],
                                                 @[NSLocalizedString(@"Trifft nicht zu", @"Trifft nicht zu"), NSLocalizedString(@"teils-teils", @"teils-teils"), NSLocalizedString(@"Trifft zu", @"Trifft zu")],
                                                 @[NSLocalizedString(@"Trifft nicht zu", @"Trifft nicht zu"), NSLocalizedString(@"teils-teils", @"teils-teils"), NSLocalizedString(@"Trifft zu", @"Trifft zu")],
                                                 @[NSLocalizedString(@"Trifft nicht zu", @"Trifft nicht zu"), NSLocalizedString(@"teils-teils", @"teils-teils"), NSLocalizedString(@"Trifft zu", @"Trifft zu")],
                                                 @[NSLocalizedString(@"Trifft nicht zu", @"Trifft nicht zu"), NSLocalizedString(@"teils-teils", @"teils-teils"), NSLocalizedString(@"Trifft zu", @"Trifft zu")],
                                                 @[NSLocalizedString(@"leicht", @"Schwierigkeit: leicht"), @"", NSLocalizedString(@"schwer", @"Schwierigkeit: schwer")],
                                                 @[NSLocalizedString(@"niedrig", @"Fähigkeiten: niedrig"), @"", NSLocalizedString(@"hoch", @"Fähigkeiten: hoch")],
                                                 @[NSLocalizedString(@"zu gering", @"Beanspruchung: zu gering"), NSLocalizedString(@"gerade richtig", @"Beanspruchung: gerade richtig"), NSLocalizedString(@"zu hoch", @"Beanspruchung: zu hoch")]
                                                 ];
    
    return flowShortScaleViewController;
}

- (NSDictionary *)calculateFlowShortScaleFactorsFromResponses:(NSArray *)responses
{
    NSArray *flowItems = @[[responses objectAtIndex:0], [responses objectAtIndex:1], [responses objectAtIndex:2], [responses objectAtIndex:3], [responses objectAtIndex:4], [responses objectAtIndex:5], [responses objectAtIndex:6], [responses objectAtIndex:7], [responses objectAtIndex:8], [responses objectAtIndex:9]];
    NSArray *fluencyItems = @[[responses objectAtIndex:7], [responses objectAtIndex:6], [responses objectAtIndex:8], [responses objectAtIndex:3], [responses objectAtIndex:4], [responses objectAtIndex:1]];
    NSArray *absorptionItems = @[[responses objectAtIndex:5], [responses objectAtIndex:0], [responses objectAtIndex:9], [responses objectAtIndex:2]];
    NSArray *anxietyItems = @[[responses objectAtIndex:10], [responses objectAtIndex:11], [responses objectAtIndex:12]];
    NSArray *fitItems = @[[responses objectAtIndex:13], [responses objectAtIndex:14], [responses objectAtIndex:15]];
    
    
    return @{@"flow" : [self meanFromNumbers:flowItems], @"flowSD" : [self sdFromNumbers:flowItems], @"fluency" : [self meanFromNumbers:fluencyItems], @"fluencySD" : [self sdFromNumbers:fluencyItems], @"absorption" : [self meanFromNumbers:absorptionItems], @"absorptionSD" : [self sdFromNumbers:flowItems], @"anxiety" : [self meanFromNumbers:anxietyItems], @"anxietySD" : [self sdFromNumbers:anxietyItems], @"fit" : [self meanFromNumbers:fitItems], @"fitSD" : [self sdFromNumbers:fitItems]};
}

#pragma mark -
#pragma mark - Calculation methods

- (void)startStopWatch
{
    self.stopWatchTimer = [NSTimer scheduledTimerWithTimeInterval:1.0/10.0
                                                           target:self
                                                         selector:@selector(updateStopWatch)
                                                         userInfo:nil
                                                          repeats:YES];
}

- (void)updateStopWatch
{
    NSTimeInterval elapsedTime = [self stopWatchTimeInterval];
    NSDate *timerDate = [NSDate dateWithTimeIntervalSince1970:elapsedTime];
    // Create a date formatter
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setTimeZone:[NSTimeZone timeZoneForSecondsFromGMT:0.0]];
    [dateFormatter setDateFormat:@"HH:mm"];
    
    // Format the elapsed time and set it to the label
    
    UIApplicationState state = [[UIApplication sharedApplication] applicationState];
    if (!(state == UIApplicationStateBackground || state == UIApplicationStateInactive)) {
        NSString *timeString = [dateFormatter stringFromDate:timerDate];
        self.labelStopWatch.text = timeString;
    }
}

- (NSTimeInterval)stopWatchTimeInterval
{
    // Create date from the elapsed time
    NSDate *currentDate = [NSDate date];
    return [currentDate timeIntervalSinceDate:self.walkSequenceCurrent.date];
}

- (double)calculateDistance
{
    CLLocationDistance totalDistance = 0;
    NSMutableArray *accurateLocationEvents = [[NSMutableArray alloc] init];
    for (LocationEvent *locationEvent in [[[self.walkSequenceCurrent locationEvents] allObjects] sortedArrayUsingDescriptors:@[[NSSortDescriptor sortDescriptorWithKey:@"timestamp" ascending:YES]]]) {
        if([locationEvent.accuracy doubleValue] < 25.0) {
            [accurateLocationEvents addObject:locationEvent];
        }
    }
    if ([accurateLocationEvents count] > 1) {
        for (int i = 0; i < ([accurateLocationEvents count] - 1); i++) {
            LocationEvent *startLocationEvent = [accurateLocationEvents objectAtIndex:i];
            LocationEvent *endLocationEvent = [accurateLocationEvents objectAtIndex:i + 1];
            
            CLLocation *startLocation = [[CLLocation alloc] initWithLatitude:[startLocationEvent.latitude doubleValue] longitude:[startLocationEvent.longitude doubleValue]];
            CLLocation *endLocation = [[CLLocation alloc] initWithLatitude:[endLocationEvent.latitude doubleValue] longitude:[endLocationEvent.longitude doubleValue]];
            
            CLLocationDistance distance = [startLocation distanceFromLocation:endLocation];
            
            totalDistance += distance;
        }
    }

    return totalDistance;
}

- (NSNumber *)meanFromNumbers:(NSArray *)numbers
{
    NSExpression *expression = [NSExpression expressionForFunction:@"average:" arguments:@[[NSExpression expressionForConstantValue:numbers]]];
    return [expression expressionValueWithObject:nil context:nil];
}

- (NSNumber *)sdFromNumbers:(NSArray *)numbers
{
    NSExpression *expression = [NSExpression expressionForFunction:@"stddev:" arguments:@[[NSExpression expressionForConstantValue:numbers]]];
    return [expression expressionValueWithObject:nil context:nil];
}

#pragma mark -
#pragma mark - UI Animations methods
- (void)startSessionCountdown
{
    [self.viewStepCounter setHidden:YES];
    [self.recordingInfoLabel setHidden:YES];
    
    [self.viewCountdownCounter setHidden:NO];
    
    self.currentCountdownTime = kCountdownTime;
    [self.labelCountdownCounter setText:@""];
    
    // NOTE: (js) Workaround so dass der Time auch weiter läuft, sollte die App im Hintergrund laufen..
    [[UIApplication sharedApplication] beginBackgroundTaskWithExpirationHandler:nil];
    self.timerCountdownCounter = [NSTimer scheduledTimerWithTimeInterval:1 target:self selector:@selector(sessionCountdownTimer) userInfo:nil repeats:YES];
    [[NSRunLoop currentRunLoop] addTimer:self.timerCountdownCounter forMode:NSRunLoopCommonModes];
    
    // Start updating motion
    [[WalkingPrimitivesManager sharedManager] startUpdatingMotion];
    [self.environmentManager startLocationManager];
    
    [self sessionCountdownTimer];
}

- (void)sessionCountdownTimer
{
    if ((self.currentCountdownTime - 1) > 0) {
        --self.currentCountdownTime;
        [self.labelCountdownCounter setText:[NSString stringWithFormat:@"%d", self.currentCountdownTime]];
        
        self.labelCountdownCounter.font = [UIFont boldSystemFontOfSize:120];
        self.labelCountdownCounter.transform = CGAffineTransformScale(self.labelCountdownCounter.transform, 0.25, 0.25);
        [UIView animateWithDuration:0.9 animations:^{
            self.labelCountdownCounter.transform = CGAffineTransformScale(self.labelCountdownCounter.transform, 4, 4);
        }];
    } else { //nach countdown langsam einblenden
        [self.timerCountdownCounter invalidate];
        self.timerCountdownCounter = nil;
        self.recordingInfoLabel.text = @"Deine Session wird jetzt aufgenommen.";
        [self.recordingInfoLabel setAlpha:0.0f];
        [self.recordingInfoLabel setHidden:NO];
        self.currentCountdownTime = 0;
        
        [self.viewCountdownCounter setHidden:YES];
        
        [self.stopLabel setAlpha:0.f];
        [self.stopButton setAlpha:0.f];
        [self.stopLabel setHidden:NO];
        [self.stopButton setHidden:NO];
        
        //[self.recordingInfoLabel setAlpha:0.0f];
        //[self.recordingInfoLabel setHidden:NO];
        
        [self.viewStepCounter setAlpha:0.0f];
        [self.viewStepCounter setHidden:NO];
        
        [UIView animateWithDuration:0.8 animations:^{
            [self.viewStepCounter setAlpha:1.0f];
            [self.recordingInfoLabel setAlpha:1.0f];
            [self.stopButton setAlpha:1.0f];
            [self.stopLabel setAlpha:1.0f];
        }];
        
        [self startRecording:nil];
        // Enable sound and start audio recording
        NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
        [self.appDelegate.audioController startGKsessionWithAudioStatus:[defaults boolForKey:@"audioStatus"]];

    }
}


#pragma mark -
#pragma mark - UI Animations methods

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    ((MapViewController *) ((UINavigationController *)segue.destinationViewController).topViewController).environmentManager = self.environmentManager;
}

- (IBAction)turn:(id)sender {
    [self.environmentManager createEnvironmentStructureWithNamePrefix:@"Turn"];
    
    // Get last location event
    NSArray *locationEvents = [[self.walkSequenceCurrent.locationEvents allObjects] sortedArrayUsingDescriptors:@[[NSSortDescriptor sortDescriptorWithKey:@"timestamp" ascending:YES]]];
    [self addCopyFromLocationEvent:[locationEvents lastObject] withName:NSLocalizedString(@"Turn", @"Datenbankeintrag: Richtungsänderung") TimeDifferenceInSeconds:0.001];
    
    [self.appDelegate.audioController playSoundmark:@"Turn_main"];
}

- (IBAction)prepareForUnwind:(UIStoryboardSegue *)segue {
}

- (void)addCopyFromLocationEvent:(LocationEvent *)locationEvent withName:(NSString *)name TimeDifferenceInSeconds:(NSTimeInterval)timeDifferenceInSeconds {
    LocationEvent *locationEventCopy = [NSEntityDescription insertNewObjectForEntityForName:@"LocationEvent" inManagedObjectContext:self.appDelegate.managedObjectContext];
    locationEventCopy.accuracy = locationEvent.accuracy;
    locationEventCopy.speed = locationEvent.speed;
    locationEventCopy.timestamp = [NSNumber numberWithDouble:[locationEvent.timestamp doubleValue] + timeDifferenceInSeconds];
    locationEventCopy.latitude = locationEvent.latitude;
    locationEventCopy.longitude = locationEvent.longitude;
    locationEventCopy.name = name;
    [self.walkSequenceCurrent addLocationEventsObject:locationEventCopy];
}

- (void)applicationDidBecomeActive {
    self.numberOfStrides = _numberOfStrides;
    self.walkDistance = _walkDistance;
}

@end
