/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */


package edu.uci.ece.zen.utils;

import java.util.Vector;
import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;

/** Class to intepreter the memory debug info 
 * @author Yue Zhang
 */


public class MemoryDebug{
    public static void main(String[] args ){
        try{
            /* Start to parse the input file into a Vector */
            if(args.length!=1){
                System.err.println(
                        "Usage:java MemoryDebug -filename");
            }
            BufferedReader br = new BufferedReader(new FileReader(args[0]));
            Vector allInfo = new Vector();
            String aLine;
            while((aLine=br.readLine())!=null){
                if(!aLine.equals("")){
                    allInfo.addElement(aLine);
                }
            }

            /*Start to trim off the unneeded information*/ 

            float trimValue=ZenBuildProperties.observeLastXPercent/100.0f;
            int unuseful= (int)(allInfo.size()*trimValue);
            int useful = allInfo.size()-unuseful;

            Vector usefulInfo = new Vector();

            for(int i = 0; i<useful; i++){
                usefulInfo.addElement((String)allInfo.elementAt(unuseful+i));
            }
            /*Start to group the useful info */
            int groupSize = 6;
            Vector[] memoryConsumed = new Vector[groupSize];
            Vector[] memoryRemaining = new Vector[groupSize];
            for(int j = 0; j<groupSize; j++){
                memoryConsumed[j] = new Vector();
                memoryRemaining[j] = new Vector();
            }

            String groupId;

            for(int i = 0; i<useful; i++){
                String oneLine = (String)usefulInfo.elementAt(i);
                StringTokenizer st = new StringTokenizer(oneLine, ",");
                StringTokenizer stBackup = new StringTokenizer(oneLine, ",");
                if(st.countTokens()!=4 && st.countTokens()!=3){
                    //System.err.println("The format of line "+oneLine+" is wrong.");
                    //System.err.println("It's should be 3 or 4 numbers separated by \",\"");
                    //System.exit(-1);
                    continue; //Now we just omit this format wrong thing, may need to change later
                }           
               
                boolean judge = true;
                while(stBackup.hasMoreTokens()){
                    String judgeLength = stBackup.nextToken();
                    if(judgeLength.length()!=11){
                        //System.err.println("The format of line "+oneLine+" is wrong.");
                        //System.err.println("Each number should be 11 digits long");
                        //System.exit(-1);
                        judge = false;
                        break; //Now we just omit this format wrong thing, may need to change later
                    }
                }
                if(!judge) continue;
               
                groupId = st.nextToken();
                if(groupId.equals("00000999999")){
                    memoryConsumed[0].addElement(st.nextToken());
                    memoryRemaining[0].addElement(st.nextToken());
                }
                else{
                    if(groupId.equals("00000000000")){
                        memoryConsumed[1].addElement(st.nextToken());
                        memoryRemaining[1].addElement(st.nextToken());
                    }
                    else{
                        if(groupId.equals("00000000001")){
                            memoryConsumed[2].addElement(st.nextToken());
                            memoryRemaining[2].addElement(st.nextToken());
                        }
                        else{
                            if(groupId.equals("00000000002")){
                                memoryConsumed[3].addElement(st.nextToken());
                                memoryRemaining[3].addElement(st.nextToken());
                            }
                            else{
                                if(groupId.equals("00000000003")){
                                    memoryConsumed[4].addElement(st.nextToken());
                                    memoryRemaining[4].addElement(st.nextToken());
                                }
                                else{
                                    if(groupId.equals("00000000004")){
                                        memoryConsumed[5].addElement(st.nextToken());
                                        memoryRemaining[5].addElement(st.nextToken());
                                    }
                                    else{
                                        if(groupId.equals("00000000006")){
                                            memoryConsumed[6].addElement(st.nextToken());
                                            memoryRemaining[6].addElement(st.nextToken());
                                        }
                                        else{
                                            //System.err.println("Wrong group id: "+groupId); 
                                            //System.exit(-1);
                                            continue; //Now we just omit this format wrong thing, may need to change later
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            /*Start to check if there is any memory leak*/
            boolean[] leak = new boolean[groupSize];

            for(int i = 0; i<groupSize; i++){
                leak[i] = false;
                for(int j = 0; j<memoryConsumed[i].size()-1; j++) {
                    String s1 = (String)memoryConsumed[i].elementAt(j);
                    String s2 = (String)memoryConsumed[i].elementAt(j+1);
                    if(!leak[i] && !(s1.equals(s2))){
                        leak[i] = true;
                        break;
                    }
                }
            }

            /*Start to print out the result to file*/

            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("MemoryDebugResults.log")));

            for(int i = 0 ; i < groupSize; i++) {
                switch(i){
                    case 0: 
                        if(memoryConsumed[i].size()!=0){
                            out.print("00000999999 area has ");
                            if(!leak[i]){out.print("no ");}
                            out.println("memory leak");
                            if(leak[i]){
                                for(int j=0; j<memoryConsumed[i].size();j++){
                                    out.println("00000999999"+","+(String)memoryConsumed[i].elementAt(j)+","+(String)memoryRemaining[i].elementAt(j));
                                }
                            }
                        }
                        break;

                    case 1:
                        if(memoryConsumed[i].size()!=0){

                            out.print("00000000000 area has ");
                            if(!leak[i]){out.print("no ");}
                            out.println("memory leak");
                            if(leak[i]){
                                for(int j=0; j<memoryConsumed[i].size();j++){
                                    out.println("00000000000"+","+(String)memoryConsumed[i].elementAt(j)+","+(String)memoryRemaining[i].elementAt(j));
                                }
                            }
                        }
                        break;
                    case 2: 
                        if(memoryConsumed[i].size()!=0){

                            out.print("00000000001 area has ");
                            if(!leak[i]){out.print("no ");}
                            out.println("memory leak");
                            if(leak[i]){
                                for(int j=0; j<memoryConsumed[i].size();j++){
                                    out.println("00000000001"+","+(String)memoryConsumed[i].elementAt(j)+","+(String)memoryRemaining[i].elementAt(j));
                                }
                            }
                        }
                        break;
                    case 3:
                        if(memoryConsumed[i].size()!=0){
                            out.print("00000000002 area has ");
                            if(!leak[i]){out.print("no ");}
                            out.println("memory leak");
                            if(leak[i]){
                                for(int j=0; j<memoryConsumed[i].size();j++){
                                    out.println("00000000002"+","+(String)memoryConsumed[i].elementAt(j)+","+(String)memoryRemaining[i].elementAt(j));
                                }
                            }
                        }
                        break;

                    case 4:
                        if(memoryConsumed[i].size()!=0){
                            out.print("00000000003 area has ");
                            if(!leak[i]){out.print("no ");}
                            out.println("memory leak");
                            if(leak[i]){
                                for(int j=0; j<memoryConsumed[i].size();j++){
                                    out.println("00000000003"+","+(String)memoryConsumed[i].elementAt(j)+","+(String)memoryRemaining[i].elementAt(j));
                                }
                            }
                        }
                        break;

                    case 5:
                        if(memoryConsumed[i].size()!=0){

                            out.print("00000000004 area has ");
                            if(!leak[i]){out.print("no ");}
                            out.println("memory leak");
                            if(leak[i]){
                                for(int j=0; j<memoryConsumed[i].size();j++){
                                    out.println("00000000004"+","+(String)memoryConsumed[i].elementAt(j)+","+(String)memoryRemaining[i].elementAt(j));
                                }
                            }
                        }
                        break;

                    case 6:
                        if(memoryConsumed[i].size()!=0){

                            out.print("00000000006 area has ");
                            if(!leak[i]){out.print("no ");}
                            out.println("memory leak");
                            if(leak[i]){
                                for(int j=0; j<memoryConsumed[i].size();j++){
                                    out.println("00000000006"+","+(String)memoryConsumed[i].elementAt(j)+","+(String)memoryRemaining[i].elementAt(j));
                                }
                            }
                        }
                        break;

                    default: break;
                }
            }
            out.flush();
            out.close();

        }

        catch(Exception ex){
            ex.printStackTrace();
            System.exit(-1);
        }
    }
}

