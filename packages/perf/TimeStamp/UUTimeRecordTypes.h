// This file is copyrighted by McDonnell-Douglas Corporation, a wholly
// owned subsidiary of The Boeing Company, Copyright (c) 2002, all rights
// reserved. This file is open source, free software, you are free to
// use, modify, and distribute the source code and object code produced
// from the source, as long as you include this copyright statement,
// along with code built using this file.

// In particular, you can use this file in proprietary software and are
// under no obligation to redistribute any of your source code that is
// built using this file. Note, however, that you may not do anything to
// this file code, such as copyrighting it yourself or claiming
// authorship of this code, that will prevent this file from being
// distributed freely using an open source development model.

// Warranty
// This file is provided as is, with no warranties of any kind, including
// the warranties of design, merchantability and fitness for a particular
// purpose, non-infringement, or arising from a course of dealing, usage
// or trade practice. Moreover, this file is provided with no support and
// without any obligation on the part of McDonnell-Douglas, its
// employees, or others to assist in its use, correction, modification,
// or enhancement.

// Liability
// McDonnell-Douglas, its employees, and agents have no liability with 
// respect to the infringement of copyrights, trade secrets or any 
// patents by this file thereof. Moreover, in no event will 
// McDonnell-Douglas, its employees, or agents be liable for any lost
// revenue or profits or other special, indirect and consequential 
// damages.

// Acknowledgement
// This work was sponsored by the US Air Force Research Laboratory
// Information Directorate, Wright-Patterson Air Force Base.
//
//
// DESCRIPTION:
//
#ifndef UUTIMERECORDTYPES_H
#define UUTIMERECORDTYPES_H  

namespace UUTimeRecordTypes
{
      enum UUTimeRecordTypesTypes
      {
          UNDEFINED                                  = 0, 
          START_OF_01HZ_FRAME                        = 1,
          STOP_OF_01HZ_FRAME                         = 2,   
          START_OF_01HZ_COMPONENT                    = 3,
          START_OF_05HZ_FRAME                        = 4,
          STOP_OF_01HZ_COMPONENT                     = 5,
          START_OF_05HZ_COMPONENT                    = 6,
          START_OF_20HZ_FRAME                        = 7,
          START_OF_20HZ_COMPONENT                    = 8,
          START_OF_MAIN                              = 9,
          STOP_OF_05HZ_FRAME                         = 10,
          START_OF_FINISH_INIT_PLUGGABLES            = 11,
          STOP_OF_MAIN                               = 12,
          START_OF_CREATE_PLUGGABLES                 = 13,
          STOP_OF_20HZ_FRAME                         = 14,
          START_OF_START_PLUGGABLES                  = 15,
          STOP_OF_05HZ_COMPONENT                     = 16,
          STOP_OF_CREATE_PLUGGABLES                  = 17,
          STOP_OF_20HZ_COMPONENT                     = 18,
          STOP_OF_FINISH_INIT_PLUGGABLES             = 19,
          STOP_OF_START_PLUGGABLES                   = 20,
          FAULT_01HZ_FRAME_ALREADY_ON                = 21,
          FAULT_05HZ_FRAME_ALREADY_ON                = 22,
          FAULT_20HZ_FRAME_ALREADY_ON                = 23,
          FAULT_01HZ_FRAME_ALREADY_OFF               = 24,
          FAULT_05HZ_FRAME_ALREADY_OFF               = 25,
          FAULT_20HZ_FRAME_ALREADY_OFF               = 26,
          FAULT_01HZ_COMPONENT_ALREADY_ON            = 27,
          FAULT_05HZ_COMPONENT_ALREADY_ON            = 28,
          FAULT_20HZ_COMPONENT_ALREADY_ON            = 29,
          FAULT_01HZ_COMPONENT_ALREADY_OFF           = 30,
          FAULT_05HZ_COMPONENT_ALREADY_OFF           = 31,
          FAULT_20HZ_COMPONENT_ALREADY_OFF           = 32,
          FAULT_CREATE_PLUGGABLES_ALREADY_ON         = 33,
          FAULT_START_PLUGGABLES_ALREADY_ON          = 34,
          FAULT_FINISH_INIT_PLUGGABLES_ALREADY_ON    = 35,
          FAULT_CREATE_PLUGGABLES_ALREADY_OFF        = 36,
          FAULT_START_PLUGGABLES_ALREADY_OFF         = 37,
          FAULT_FINISH_INIT_PLUGGABLES_ALREADY_OFF   = 38
      };
    
    enum UUConfigurationTypesTypes
    {
       CREATE_PLUGGABLES_ = 20,
       START_PLUGGABLES_ = 21,
       FINISH_INIT_PLUGGABLES_ = 22
    };
};

#endif
