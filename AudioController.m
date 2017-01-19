//
//  AudioController.m
//  DataCollector
//
//  Created by Simon Bogutzky on 22.04.13.
//  Copyright (c) 2013 Simon Bogutzky. All rights reserved.
//

#import "AudioController.h"

@interface AudioController ()
{
    PdAudioController *audioController;
}
@end

@implementation AudioController


static NSDictionary* _soundDict; // dictionary with all soundfiles



- (id)init:(NSString*)patchFileName
{
    self = [super init];
    if (self) {
        audioController = [[PdAudioController alloc] init];
        if ([audioController configurePlaybackWithSampleRate:44100 numberChannels:2 inputEnabled:NO mixingEnabled:YES] != PdAudioOK) {
            NSLog(@"failed to initialize audio components");
        } else {
            self.dispatcher = [[PdDispatcher alloc]init];
            [PdBase setDelegate:self.dispatcher];
            //mp3play_tilde_setup();
            self.patch = [PdBase openFile:patchFileName
                                                       path:[[NSBundle mainBundle] resourcePath]];
            if (!self.patch) {
                NSLog(@"Failed to open patch!"); // Gracefully handle failure...
            } else {
                [self loadDictionary];
            }
        }
    }
    return self;
}


#pragma mark - Recording

-(void)setUpAudioRecord:(NSString*)recordFileName{ //TODO: dynamic recordfilename
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory,
                                                         NSUserDomainMask, YES);
    NSString *path = [paths objectAtIndex:0];
    NSString *samplePath = [path stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.wav",recordFileName]];
    
    self.readWriteArgs  = [NSArray arrayWithObjects:samplePath,nil];
    
    [PdBase sendMessage:@"open" withArguments:self.readWriteArgs toReceiver:@"filePath"];

}

- (void)startAudioRecord{
    [PdBase sendBangToReceiver:@"startRecord"];

}
- (void)stopAudioRecord{
    [PdBase sendBangToReceiver:@"stopRecord"];
    
}

#pragma mark - SetUp Sound processing


- (void)startGKsessionWithAudioStatus:(BOOL)audioStatus
{
    [self audioActive:audioStatus];
    [self startAudioRecord];
    //CI-start audiofeedback
    [self setPanningtoListFromR:0.8 FromL:0.8 toR:0.8 toL:0.8];
    [self playLogo];
}

- (void) endGKsession{
    [self stopAllLayers];
    //CI-end audiofeedback
    [self performSelector:@selector(playLogo) withObject:nil afterDelay:3];
    [self performSelector:@selector(audioInactive) withObject:nil afterDelay:8];

}

- (void) playLogo{
    [self setPanningtoListFromR:1 FromL:1 toR:1 toL:1];
    [self playSoundmark:@"Logo"];
}


- (void)audioActive:(bool)vol {

    audioController.active = YES;
    if(vol){
        [PdBase sendBangToReceiver: @"audioOn"];
    } else {
        [PdBase sendBangToReceiver: @"audioOff"];
    }
}

- (void)audioInactive{
    
    [PdBase sendBangToReceiver: @"audioOff"];
    [self stopAudioRecord];
    [self performSelector:@selector(audioDSPoff) withObject:nil afterDelay:3];
}





- (void)audioDSPoff{

    audioController.active = NO;

}


#pragma mark - Loading

- (void)loadDictionary
{
    NSError *error = NULL;
    NSPropertyListFormat format;
    NSString *rootPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES)objectAtIndex:0];
    NSString *plistPath = [rootPath stringByAppendingPathComponent:@"SoundList.plist"];
    
    if (![[NSFileManager defaultManager]fileExistsAtPath:plistPath]) {
        plistPath = [[NSBundle mainBundle] pathForResource:@"SoundList" ofType:@"plist"];
    }
    
    NSData *listData = [[NSFileManager defaultManager]contentsAtPath: plistPath];
    
    _soundDict = [NSPropertyListSerialization propertyListWithData:listData options:NSPropertyListMutableContainersAndLeaves format:&format error:&error];
    
    if (! _soundDict){
        
        NSLog(@"Datei konnte nicht gelesen werden");
    }else{
        self.gaitDict = [_soundDict objectForKey:@"Gaits" ];
    }
/* load dictionary for ambients
    
        self.ambientsDict = [_soundDict objectForKey:@"Ambients" ];
 
*/
}


#pragma mark - Set Methods

-(void)loadSetWithName: (NSString *) setName{
    
    
    [self loadSoundmarks]; // until now one set only!
    [self loadLayers];
    
    //TODO: iteration ANMERKUNG J: kann auch hardcoded bleiben
    for(int i=0;i<_gaitDict.count;i++){
        [self loadGait:(i+1) withSet:setName];
    }
   // [self loadGait:1 withSet:setName];
   // [self loadGait:2 withSet:setName];
   // [self loadGait:3 withSet:setName];
   // [self loadGait:4 withSet:setName];
    
}


- (void)initSetWithName:(NSString *) setName{
    
    self.actSet = setName;
    [self loadSetWithName:setName];
    if([setName isEqualToString:@"dimens"]){
        
        //panning
        /*[self setPanningtoGait:1 withR:1 withL:0.2];//Kopfhörerlautstärke auf beiden Seiten gleich setzen?
        [self setPanningtoGait:2 withR:0.2 withL:1];
        [self setPanningtoGait:3 withR:0.8 withL:0.8];
        [self setPanningtoGait:4 withR:0.6 withL:0.6];
        
        
        //sonification delay
        [self sonifyDelay:1 toGait:1]; //einem fuß zugeordnet
        [self sonifyDelay:0 toGait:2]; //keinem Fuß zugeordnet, Gaits werden im Tool immer einem Fuß zugeteilt
        [self sonifyDelay:0 toGait:3];
        [self sonifyDelay:0 toGait:4];
        
        //sonify mode
        [self sonifyMode:0 toGait:1 withParameters:nil];//everytime same sound
        [self sonifyMode:0 toGait:2 withParameters:nil];//Parameter 1: continous
        [self sonifyMode:0 toGait:3 withParameters:nil];//MODE 2 EIBNAUEN IN INTERFACE
        
        [self sonifyMode:0 toGait:4 withParameters:nil];
        
        
        //additional settings
        [self sonifyAtBang:1 toGait:3];
        [self sonifyAtBang:13 toGait:4];*/
        
        XpanningX
        
        XsonifyDelayX
        
        XsonifyModeX
    }
    
}





#pragma mark - Gait Methods


-(NSUInteger)countGaitSounds:(int)gait{
    
    NSString *setname = [NSString stringWithFormat:@"%@_%d", self.actSet,gait];
    NSUInteger soundNum = [[self.gaitDict objectForKey:setname]count];
    NSLog(@"soundnum of gait_%d: is %lu!!!",gait,(unsigned long)soundNum);
    
    return soundNum;
}

-(void)loadGait:(int)gait withSet:(NSString*)setName {
    NSArray *soundFiles =  [self.gaitDict objectForKey: [NSString stringWithFormat:@"%@_%d",setName,gait]];
    [PdBase sendList:soundFiles toReceiver:[NSString stringWithFormat:@"gait_%d-load",gait]];
}

-(void)sonifyDelay:(int)delay toGait:(int)gait{
    
    [PdBase sendFloat:delay toReceiver:[NSString stringWithFormat:@"gait_%d-setEventDelay",gait]];
}

-(void)sonifyAtBang:(int)bang toGait:(int)gait{
    
    [PdBase sendFloat:bang toReceiver:[NSString stringWithFormat:@"gait_%d-triggerAtBang",gait]];
}


- (void)sonifyGait:(int)gait withPace:(double) paceMillisec
{
    
    [PdBase sendFloat:(float)(paceMillisec*1000) toReceiver:[NSString stringWithFormat:@"gait_%d-setPace",gait]];
    [PdBase sendBangToReceiver:[NSString stringWithFormat:@"gait_%d-sonify",gait]];

}

-(void) setPanningtoGait:(int)gait withR:(float)panR withL:(float)panL{
    
    NSArray *env = @[[NSNumber numberWithFloat:panR], [NSNumber numberWithInt:1000], [NSNumber numberWithInt:0]];
    
    [PdBase sendList:env toReceiver:[NSString stringWithFormat:@"gait_%d-panR",gait]];
    
    env = @[[NSNumber numberWithFloat:panL], [NSNumber numberWithInt:1000], [NSNumber numberWithInt:0]];
    
    [PdBase sendList:env toReceiver:[NSString stringWithFormat:@"gait_%d-panL",gait]];
}

//envelope: targetvalue, over time of, wait for
-(void)gaitVol:(int)gait withEnvelope:(NSArray*)env {
//     NSString *gaitName = [NSString stringWithFormat:@"gait_%i",gait];
    [PdBase sendList:env toReceiver:[NSString stringWithFormat:@"gait_%d-panR",gait]];
    [PdBase sendList:env toReceiver:[NSString stringWithFormat:@"gait_%d-panL",gait]];
}

- (void)clearGait:(int)gait
{
   [PdBase sendBangToReceiver:[NSString stringWithFormat:@"gait_%d-clear",gait]];

}

- (void)sonifyMode:(int)mode toGait:(int)gait withParameters: (NSArray*)parameters{
    NSString *gaitName = [NSString stringWithFormat:@"gait_%i",gait];
    switch (mode) {
        case 0:{
            [PdBase sendFloat:0 toReceiver:[NSString stringWithFormat:@"%@-soniMode",gaitName]];
            break;
            }
        case 1:{ //TODO: change hardcoded parameter value
            [PdBase sendFloat:[[parameters  objectAtIndex:0]floatValue] toReceiver:[NSString stringWithFormat:@"%@-soundRotate",gaitName]];
            [PdBase sendFloat:1 toReceiver:[NSString stringWithFormat:@"%@-soniMode",gaitName]];
            break;
        }
        case 2:{
            [PdBase sendList:parameters toReceiver:[NSString stringWithFormat:@"%@-atTriggerChangeTo",gaitName]];
            [PdBase sendFloat:2 toReceiver:[NSString stringWithFormat:@"%@-soniMode",gaitName]];
            break;
        }
        case 3:{
            [PdBase sendFloat:[[parameters  objectAtIndex:0]floatValue] toReceiver:[NSString stringWithFormat:@"%@-soundPool",gaitName]];
            [PdBase sendFloat:3 toReceiver:[NSString stringWithFormat:@"%@-soniMode",gaitName]];
            break;
        }
        default:
            break;
    }

}

-(void) changeSound: (int)sound toGait:(int) gait{
    
    [PdBase sendFloat:sound toReceiver:[NSString stringWithFormat:@"gait_%d-changeSound",gait]];
    
}



#pragma mark - Soundmark Methods


- (void)loadSoundmarks
{
    self.soundArray = [_soundDict objectForKey:@"Soundmarks"];
    [PdBase sendList:self.soundArray toReceiver:@"soundmarks-load"];
    [PdBase sendFloat:0.8 toReceiver:@"soundmarks-panR"];
    [PdBase sendFloat:0.8 toReceiver:@"soundmarks-panL"];

}

- (void)playSoundmark:(NSString*)gaitEvent{
    
    NSUInteger soundID =[self.soundArray indexOfObject:gaitEvent]+1;
    [PdBase sendFloat:soundID toReceiver:@"soundmarks-play"];

}

-(void) setPanningtoListFromR:(float)startR FromL:(float)startL toR:(float)endR toL:(float)endL{
    //NSArray *resetParameters = [NSArray arrayWithObjects:[NSNumber numberWithFloat:1],[NSNumber numberWithFloat:0],nil];
    [PdBase sendFloat:startR toReceiver:@"soundmarks-panR"];
    [PdBase sendFloat:startL toReceiver:@"soundmarks-panL"];
    
    //TODO: always a line?
    NSArray *panParametersR = [NSArray arrayWithObjects:[NSNumber numberWithFloat:endR],[NSNumber numberWithFloat:4000],nil];
    NSArray *panParametersL = [NSArray arrayWithObjects:[NSNumber numberWithFloat:endL],[NSNumber numberWithFloat:4000],nil];
    [PdBase sendList:panParametersR toReceiver:@"soundmarks-panR"];
    [PdBase sendList:panParametersL toReceiver:@"soundmarks-panL"];
}


#pragma mark - Layer Methods

- (void)loadLayers{
    
    //TODO: load all layers in plist-dictioinary Layers
    //[self loadLayerwithName:@"Turn"];
    //[self loadLayerwithName:@"Stop"];
    /*[self loadLayerwithName:@"WalkAlong"];
    [self loadLayerwithName:@"Env"];*/
    XloadLayersX
    
    
}

- (void)loadLayerwithName:(NSString*)layerName {
    NSDictionary *layersDict= [_soundDict objectForKey:@"Layers" ];
    NSArray *keyArray =  [layersDict allKeys];
    NSUInteger m =[keyArray indexOfObject:layerName];
    NSArray  *actLayer= [layersDict objectForKey:layerName];
    NSLog(@"### loadLayer: %lu with name %@", (unsigned long)m,layerName );
    
    //get number of sounds
    NSUInteger soundsNum = [[actLayer objectAtIndex:2]count];
    NSLog(@"layer %@ has %lu sounds", layerName, (unsigned long)soundsNum);
    
    
    float delayFrom= [[actLayer objectAtIndex:0]floatValue];
    float delayTo= [[actLayer objectAtIndex:1]floatValue];
    NSArray *soundFiles = [actLayer objectAtIndex:2];
    
    //create Pd list
    NSMutableArray  * pdList = [NSMutableArray arrayWithObjects:[NSNumber numberWithUnsignedLong:m],[NSNumber numberWithUnsignedLong:soundsNum],[NSNumber numberWithInt:delayFrom],[NSNumber numberWithInt:delayTo], nil];
    for (int j =0 ; j< soundsNum; j++) {
        NSString *soundFile = [soundFiles objectAtIndex:j];
        [pdList addObject:soundFile];
    }
    [PdBase sendList:pdList toReceiver:@"createLayer"];
    
}

- (void)playLayerWithName:(NSString *)name withEnvelope:(float)value
{
    NSDictionary *layersDict= [_soundDict objectForKey:@"Layers" ];
    NSArray *keys = [layersDict allKeys];
    NSUInteger layerID =[keys indexOfObject:name];
    NSLog(@"### Play layer with ID: %lu", (unsigned long)layerID);
    
    //parameters: ambientID, envelope_rampto,envelope_overtime, envelope_wait
    NSArray *parameters = @[[NSNumber numberWithUnsignedLong:layerID], [NSNumber numberWithFloat:value], [NSNumber numberWithInt:2000], [NSNumber numberWithInt:0]];
    
    [PdBase sendList:parameters toReceiver:@"playLayer"];
}
- (void)stopLayerWithName:(NSString *)name
{
    NSDictionary *layersDict= [_soundDict objectForKey:@"Layers" ];
    NSArray *keys = [layersDict allKeys];
    NSUInteger layerID =[keys indexOfObject:name];
    NSLog(@"### Stop layer with ID: %lu", (unsigned long)layerID);
    
    //parameters: ambientID, envelope_rampto,envelope_overtime, envelope_wait
    NSArray *parameters = @[[NSNumber numberWithUnsignedLong:layerID],[NSNumber numberWithInt:0],[NSNumber numberWithInt:5000],[NSNumber numberWithInt:0]];
    [PdBase sendList:parameters toReceiver:@"stopLayer"];
}

- (void)stopAllLayers{
    //TODO: iterative for all layers in dictionary
    /*[self stopLayerWithName:@"Turn"];
    [self stopLayerWithName:@"WalkAlong"];
    [self stopLayerWithName:@"Stop"];*/
    XstopLayersX
}

/*- (void)layerPanningWithName:(NSString *)name withEnvelope:(float)value{
    NSDictionary *layersDict= [_soundDict objectForKey:@"Layers" ];
    NSArray *keys = [layersDict allKeys];
    int layerID =[keys indexOfObject:name];
    
    NSArray *parameters = @[[NSNumber numberWithInt:layerID], [NSNumber numberWithFloat:value], [NSNumber numberWithInt:1], [NSNumber numberWithInt:0]];

    [PdBase sendList:parameters toReceiver:@"panRLayer"];
    [PdBase sendList:parameters toReceiver:@"panLLayer"];

}*/

/*
- (void)playSoundmark:(NSString*)curEnvironment{

    
    NSArray *soundFiles = [_soundDict objectForKey:@"Soundmarks"];
    int newAmbientID =[soundFiles indexOfObject:curEnvironment]+1;

    
    NSArray *ambientParameters1 = [NSArray arrayWithObjects:[NSNumber numberWithInt:self.actAmbientID],[NSNumber numberWithFloat:0],[NSNumber numberWithFloat:3000],[NSNumber numberWithFloat:1],nil];
    [PdBase sendList: ambientParameters1 toReceiver:@"ambientVol"];
    
    //parameters:, vol, vol, vol
    NSArray *markParameters = [NSArray arrayWithObjects:[NSNumber numberWithFloat:0.5],[NSNumber numberWithFloat:3000],[NSNumber numberWithFloat:3000],nil];
    [PdBase sendList:markParameters toReceiver:@"soundmarks-vol"];
    [PdBase sendFloat:newAmbientID toReceiver:@"soundmarks-play"];
    

    
    NSArray *ambientParameters2 = [NSArray arrayWithObjects:[NSNumber numberWithInt:self.actAmbientID],[NSNumber numberWithFloat:0.8],[NSNumber numberWithFloat:3000],[NSNumber numberWithFloat:9000],nil];
    [PdBase sendList: ambientParameters2 toReceiver:@"ambientVol"];

}
 */


#pragma mark - Ambient Methods

/*
- (void)changeAmbientTo:(NSString*)newEnvironment fromEnvironment:(NSString*)oldEnvironment
{
    NSDictionary *ambientsDict = [_soundDict objectForKey:@"Ambients" ];
    NSArray *keyArray =  [ambientsDict allKeys];
    
    if (newEnvironment!=nil){
        
        if(oldEnvironment != nil){
            int oldAmbientID =[keyArray indexOfObject:oldEnvironment];
            [self endAmbient:oldAmbientID];
        }

        int newAmbientID =[keyArray indexOfObject:newEnvironment];
        self.actAmbientID = newAmbientID;
        
        [self.pauseTimer invalidate];
        NSLog(@"### Change Ambient is called");
        self.pauseTimer = [NSTimer scheduledTimerWithTimeInterval:4.0 target:self selector:@selector(playAmbient) userInfo:nil repeats:NO];
        
        
    }
    else{
        int oldAmbientID =[keyArray indexOfObject:oldEnvironment];
        NSLog(@"### Change Ambient is NIL");
        [self endAmbient:oldAmbientID];
        self.actAmbientID= -1;
    }
}


*/
- (void)changeAmbientTo:(NSString*)newEnvironment fromEnvironment:(NSString*)oldEnvironment
{
    //NSDictionary *ambientsDict = [_soundDict objectForKey:@"Ambients" ];
    NSArray *keyArray =  [self.ambientsDict allKeys];
    
    NSUInteger oldAmbientID =[keyArray indexOfObject:oldEnvironment];
    NSUInteger newAmbientID =[keyArray indexOfObject:newEnvironment];
    self.actEnvironment = newEnvironment;
    
    
    if (newEnvironment!=nil){
        
        // Wechsel innerhalb gleicher Szene
        if ([[newEnvironment substringToIndex:2] intValue]== [[oldEnvironment substringToIndex:2] intValue]) {
            NSLog(@"### Same Ambient");
            //Weg vom Kern
            
            NSArray *newArray = [newEnvironment componentsSeparatedByString:@"_"];
            NSArray *oldArray = [oldEnvironment componentsSeparatedByString:@"_"];
            
            //if ([[newEnvironment substringWithRange:NSMakeRange(2, 1)] intValue]< ([[oldEnvironment substringWithRange:NSMakeRange(2, 1)] intValue]))
            if ([[newArray objectAtIndex:1]intValue] < [[oldArray objectAtIndex:1]intValue])
            {    NSLog(@"### Ambient same + num small");
                [self endAmbient:oldAmbientID];
            }
            //Hin zum Kern
            else{
                NSLog(@"### Ambient same + num high");
                self.actAmbientID = newAmbientID;
                [self.pauseTimer invalidate];
                self.pauseTimer = [NSTimer scheduledTimerWithTimeInterval:5.0 target:self selector:@selector(playAmbient) userInfo:nil repeats:NO];
            }
        }
        //Eintritt in eine neue Szene: detectiveMain aus, Sounds laden, ersten Ring starten
        else{
            //MainDetective leise
            [self stopAmbientWithName:@"detektiveMain"];
            [self.loadTimer invalidate];
            self.loadTimer = [NSTimer scheduledTimerWithTimeInterval:5.0 target:self selector:@selector(loadAmbientsOfEnvironment) userInfo:nil repeats:NO];
            self.actAmbientID = newAmbientID;
            [self.pauseTimer invalidate];
            self.pauseTimer = [NSTimer scheduledTimerWithTimeInterval:10.0 target:self selector:@selector(playAmbient) userInfo:nil repeats:NO];
        }
        
    }
    // Außerhalb von Szenen: Sounds löschen, detectiveMain an
    else{
        [self endAmbient:oldAmbientID];
        self.actAmbientID= -1;
        [self clearAmbients];
        [self playAmbientWithName:@"detektiveMain"];
    }
    
}


- (void)loadAmbientsOfEnvironment{
    NSArray *keyArray =  [self.ambientsDict allKeys];
    NSString *match = [self.actEnvironment substringFromIndex:4];
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"SELF contains %@", match];
    NSArray *results = [keyArray filteredArrayUsingPredicate:predicate];
    
    for (NSString *item in results) {
        [self loadAmbientwithName:item isConstant: false];
    }
}




- (void)loadAmbientwithName:(NSString*)ambientName isConstant: (BOOL)constant
{
    NSArray *keyArray =  [self.ambientsDict allKeys];
    NSUInteger m =[keyArray indexOfObject:ambientName];
    
    
    NSLog(@"### loadAmbient: %lu with name %@", (unsigned long)m,ambientName );
    //get number of layer
    NSUInteger layerNum = [[self.ambientsDict objectForKey:[keyArray objectAtIndex:m]] count];
    // create ambient
    NSArray *parameters = [NSArray arrayWithObjects:[NSNumber numberWithUnsignedLong:m],[NSNumber numberWithUnsignedLong:layerNum],nil];
    if (constant) {
        [PdBase sendList:parameters toReceiver:@"createConstAmbient"];
    } else {
        [PdBase sendList:parameters toReceiver:@"createAmbient"];
    }
    
    
    // create ambient layers
    
    for (int i = 0; i < layerNum; i++) {
        float delayFrom= [[[[self.ambientsDict objectForKey:[keyArray objectAtIndex:m]] objectAtIndex:i] objectAtIndex:0]floatValue];
        float delayTo= [[[[self.ambientsDict objectForKey:[keyArray objectAtIndex:m]] objectAtIndex:i] objectAtIndex:1] floatValue];
        NSArray *soundFiles = [[[self.ambientsDict objectForKey: [keyArray objectAtIndex:m]] objectAtIndex:i]objectAtIndex:2];
        NSUInteger soundNum = [soundFiles count];
        
        //create Pd list
        NSMutableArray  * pdList = [NSMutableArray arrayWithObjects:[NSNumber numberWithUnsignedLong:m],[NSNumber numberWithUnsignedLong:soundNum],[NSNumber numberWithUnsignedLong:delayFrom],[NSNumber numberWithInt:delayTo], nil];
        for (int j =0 ; j< soundNum; j++) {
            NSString *soundFile = [soundFiles objectAtIndex:j];
            [pdList addObject:soundFile];
        }
        [PdBase sendList:pdList toReceiver:@"ambientCreateLayer"];
    }
}



-(void)playAmbient // um aktuelles Ambient mit Timer zu starten
{
    
    NSLog(@"### Play Ambient with ID: %lu", (unsigned long)self.actAmbientID);
    //parameters: ambientID, vol, vol, vol
    NSArray *parameters = [NSArray arrayWithObjects:[NSNumber numberWithUnsignedLong:self.actAmbientID],[NSNumber numberWithFloat:0.8],[NSNumber numberWithInt:5000],[NSNumber numberWithInt:1],nil];
    [PdBase sendList:parameters toReceiver:@"ambientPlay"];
}

- (void)playAmbientWithName:(NSString *)name
{
    NSArray *keys = [self.ambientsDict allKeys];
    NSUInteger ambientID =[keys indexOfObject:name];
    NSLog(@"### Play Ambient with ID: %lu", (unsigned long)ambientID);
    
    //parameters: ambientID, vol, vol, vol
    NSArray *parameters = @[[NSNumber numberWithUnsignedLong:ambientID], [NSNumber numberWithFloat:0.8], [NSNumber numberWithInt:5000], [NSNumber numberWithInt:1]];
    [PdBase sendList:parameters toReceiver:@"ambientPlay"];
}

- (void)stopAmbientWithName:(NSString *)name
{
    NSArray *keys = [self.ambientsDict allKeys];
    NSUInteger ambientID =[keys indexOfObject:name];
    NSLog(@"### Play Ambient with ID: %lu", (unsigned long)ambientID);
    
    //parameters: ambientID, vol, vol, vol
    NSArray *parameters = [NSArray arrayWithObjects:[NSNumber numberWithUnsignedLong:ambientID],[NSNumber numberWithInt:0],[NSNumber numberWithInt:3000],[NSNumber numberWithInt:1],nil];
    [PdBase sendList:parameters toReceiver:@"ambientStop"];
}

- (void)endAmbient:(NSUInteger)envID
{
    //parameters: ambientID, vol, vol, vol
    NSArray *parameters = [NSArray arrayWithObjects:[NSNumber numberWithUnsignedLong:envID],[NSNumber numberWithInt:0],[NSNumber numberWithInt:3000],[NSNumber numberWithInt:1],nil];

    [PdBase sendList:parameters toReceiver:@"ambientStop"];
    //[self performSelector:@selector(clearAmbient:) withObject:[NSNumber numberWithInt: envID] afterDelay: 4.0 ];
}



// Alle temp Ambients werden gelöscht
- (void)clearAmbients
{
    [PdBase sendBangToReceiver:@"clearAmbients"];
}













/*
 - (void)startAmbient :(NSString*)environment withId:(int) envID
 {
 
 //get number of layers
 int layerNum = [[_soundDict objectForKey: environment] count];
 
 // create ambient
 NSArray *parameters = [NSArray arrayWithObjects:[NSNumber numberWithInt:envID],[NSNumber numberWithInt:layerNum],nil];
 [PdBase sendList:parameters toReceiver:@"createAmbient"];
 
 // create ambient layers
 
 for (int i = 0; i < layerNum; i++) {
 float delayFrom= [[[[_soundDict o/Users/nas/Documents/DiakoPrototyp/FlowMaschine/FlowMaschine/FlowMaschine/SoundEngine/Patches/layer-abs.pdbjectForKey: environment] objectAtIndex:i] objectAtIndex:0]floatValue];
 float delayTo= [[[[_soundDict objectForKey: environment] objectAtIndex:i] objectAtIndex:1] floatValue];
 NSArray *soundFiles = [[[_soundDict objectForKey: environment] objectAtIndex:i]objectAtIndex:2];
 int soundNum = [soundFiles count];
 
 //create Pd list
 NSMutableArray  * pdList = [NSMutableArray arrayWithObjects:[NSNumber numberWithInt:envID],[NSNumber numberWithInt:soundNum],[NSNumber numberWithInt:delayFrom],[NSNumber numberWithInt:delayTo], nil];
 for (int j =0 ; j< soundNum; j++) {
 NSString *soundFile = [soundFiles objectAtIndex:j];
 [pdList addObject:soundFile];
 }
 [PdBase sendList:pdList toReceiver:@"ambientCreateLayer"];
 }
 
 }*/


@end
