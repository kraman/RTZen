/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package perf.jitter;

import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.BufferedWriter;

class DataProc{

    public static void main(String[] args){
        try{
            final int[] messageSizes = new int[]{4, 32, 128, 256, 512, 1024};
            int testType = 1; //default value
            int orbNum = 3;
	    String localOrRemote = "local";
	    if(args.length == 0){
		    System.err.println("Usage: java DataProc <local|remote|both> <orbNum> ");
		    System.exit(-1);
	    }
	    if(args[0]!=null){
		    localOrRemote = args[0];
		    if(localOrRemote.equals("local") | localOrRemote.equals("remote") | localOrRemote.equals("both")){
			    System.out.println("Start to process "+localOrRemote+" data...");
		    }
		    else{
			    System.err.println("Usage: java DataProc <local|remote|both> <orbNum> ");
			    System.exit(-1);
		    }
	    }
	    else{
		    System.err.println("Usage: java DataProc <local|remote|both> <orbNum> ");
		    System.exit(-1);
	    }


	    if(args[1]!= null){
		    orbNum = Integer.parseInt(args[1]);
                    System.out.println("Number of ORB being tests is "+orbNum);
	    }
	    else{
		    System.err.println("Usage: java DataProc <local|remote|both> <orbNum> ");
		    System.exit(-1);
	    }


	    for(int num=1; num<=orbNum; num++){
		    for (int j=0; j<messageSizes.length; j++){
			    if(localOrRemote.equals("local")|localOrRemote.equals("both")){
				    String filename = "timeRecords.raw."+testType+"."+num+"."+num+"."+messageSizes[j]+".txt";
                                    String newFilename = "timeRecords."+testType+"."+num+"."+num+"."+messageSizes[j]+".txt";
				    BufferedReader br = new BufferedReader(new FileReader(filename));
				    System.out.println("The file being processed is "+filename);
				    String s;
				    int id;

				    Vector vec = new Vector();
				    Vector vecProc = new Vector();

				    while((s = br.readLine())!=null){
					    StringTokenizer st = new StringTokenizer(s, ",");
					    st.nextToken();
					    id = Integer.parseInt(st.nextToken());
					    switch(id){
						    case 22: 
							    Double db1 = new Double(Double.parseDouble(st.nextToken()));
							    vec.addElement(db1);
							    break;
						    case 21: break;
						    default:System.err.println("Unrecogzied position id "+id);
							    System.exit(-1);
					    }
				    }
				    //System.out.println("v1's size is "+vec.size());

				    for(int i = 0; i < vec.size();i++){
					    if(i%2==1){
						    double d1 = ((Double)vec.elementAt(i)).doubleValue();
						    double d2 = ((Double)vec.elementAt(i-1)).doubleValue();
						    vecProc.addElement(new Double((d1-d2)*1000000)); //Change the unit to microsecond
					    }
				    }


				    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(newFilename)));
				    for(int i=0; i<vecProc.size();i++){
					    out.println(((Double)vecProc.elementAt(i)).doubleValue());
				    }
				    out.flush();
				    out.close();
				    System.out.println("Finish processing "+newFilename);
			    }
			    if(localOrRemote.equals("remote")|localOrRemote.equals("both")){
				    String filename = "timeRecords.raw."+testType+"."+num+"."+num+"."+messageSizes[j]+".txt";
				    String newFilename = "timeRecords."+testType+"."+num+"."+num+"."+messageSizes[j]+".txt";

				    BufferedReader br = new BufferedReader(new FileReader(filename));
				    System.out.println("The file being processed is "+filename);
				    String s;
				    int id;

				    Vector vec = new Vector();
				    Vector vecProc = new Vector();

				    while((s = br.readLine())!=null){
					    StringTokenizer st = new StringTokenizer(s, ",");
					    st.nextToken();
					    id = Integer.parseInt(st.nextToken());
					    switch(id){
						    case 22:
							    Double db1 = new Double(Double.parseDouble(st.nextToken()));
							    vec.addElement(db1);
							    break;
						    case 21: break;
						    default:System.err.println("Unrecogzied position id "+id);
							    System.exit(-1);
					    }
				    }
				    //System.out.println("v1's size is "+vec.size());

				    for(int i = 0; i < vec.size();i++){
					    if(i%2==1){
						    double d1 = ((Double)vec.elementAt(i)).doubleValue();
						    double d2 = ((Double)vec.elementAt(i-1)).doubleValue();
						    vecProc.addElement(new Double((d1-d2)*1000000)); //Change the unit to microsecond
					    }
				    }


				    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(newFilename)));
				    for(int i=0; i<vecProc.size();i++){
					    out.println(((Double)vecProc.elementAt(i)).doubleValue());
				    }
				    out.flush();
				    out.close();
				    System.out.println("Finish processing "+newFilename);
			    }
		    }
	    }
	}
	catch(Exception ex){
		ex.printStackTrace();
            System.exit(-1);
        }
    }
}

