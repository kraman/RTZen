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

class DataProcTao{

    public static void main(String[] args){
        try{
            if(args[0] == null){
                System.err.println("Usage: java perf.jitter.DataProcTao <name of the txt file to be processed>");
                System.exit(-1);
            }
            System.out.println("Start to process "+args[0]);
            BufferedReader br = new BufferedReader(new FileReader(args[0]));
            String s;
            int id;

            Vector v1 = new Vector();
            Vector v1Proc = new Vector();


            while((s = br.readLine())!=null){
                StringTokenizer st = new StringTokenizer(s, ",");
                st.nextToken();
                id = Integer.parseInt(st.nextToken());
                switch(id){
                    case 22: 
                             Double db1 = new Double(Double.parseDouble(st.nextToken()));
                             v1.addElement(db1);
                             break;
                    case 21: break;
                    default:System.err.println("Unrecogzied position id "+id);
                            System.exit(-1);
                }
            }
            System.out.println("v1's size is "+v1.size());

            
            for(int i = 0; i < v1.size();i++){
                if(i%2==1){
                    double d1 = ((Double)v1.elementAt(i)).doubleValue();
                    double d2 = ((Double)v1.elementAt(i-1)).doubleValue();
                    v1Proc.addElement(new Double(d1-d2));
                }
            }


            PrintWriter out1 = new PrintWriter(new BufferedWriter(new FileWriter(args[0])));
            for(int i=0; i<v1Proc.size();i++){
                out1.println(((Double)v1Proc.elementAt(i)).doubleValue());
            }
            out1.flush();
            out1.close();
            System.out.println("Finish processing "+args[0]);

        }
        catch(Exception ex){
            ex.printStackTrace();
            System.exit(-1);
        }
    }
}

