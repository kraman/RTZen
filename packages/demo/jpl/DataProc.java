package demo.jpl;

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
            BufferedReader br = new BufferedReader(new FileReader("timeRecords.txt"));
            String s;
            int id;

            Vector v1 = new Vector();
            Vector v2 = new Vector();
            Vector v1Proc = new Vector();
            Vector v2Proc = new Vector();


            while((s = br.readLine())!=null){
                StringTokenizer st = new StringTokenizer(s, ",");
                st.nextToken();
                id = Integer.parseInt(st.nextToken());
                switch(id){
                    case 20: break;
                    case 21: 
                             Double db1 = new Double(Double.parseDouble(st.nextToken()));
                             v1.addElement(db1);
                             break;
                    case 22: 
                             Double db2 = new Double(Double.parseDouble(st.nextToken()));
                             v2.addElement(db2);
                             break;
                    default:System.err.println("Unrecogzied position id "+id);
                            System.exit(-1);
                }
            }
            for(int i = 0; i < v2.size();i++){
                if(i%2==1){
                    double d1 = ((Double)v2.elementAt(i)).doubleValue();
                    double d2 = ((Double)v2.elementAt(i-1)).doubleValue();
                    v2Proc.addElement(new Double(d1-d2));
                }
            }

            for(int i = 0; i < v1.size();i++){
                if(i%2==1){
                    double d1 = ((Double)v2.elementAt(i)).doubleValue();
                    double d2 = ((Double)v2.elementAt(i-1)).doubleValue();
                    v1Proc.addElement(new Double(d1-d2));
                    if(v1Proc.size()==v2Proc.size()){
                        break;
                    }
                }
            }


            PrintWriter out1 = new PrintWriter(new BufferedWriter(new FileWriter("timeRecords.1.1.1.128.txt")));
            PrintWriter out2 = new PrintWriter(new BufferedWriter(new FileWriter("timeRecords.1.2.2.128.txt")));
            for(int i=0; i<v2Proc.size();i++){
                out1.println(((Double)v1Proc.elementAt(i)).doubleValue());
                out2.println(((Double)v2Proc.elementAt(i)).doubleValue());
            }
            out1.flush();
            out1.close();
            out2.flush();
            out2.close();
        }
        catch(Exception ex){
            ex.printStackTrace();
            System.exit(-1);
        }
    }
    */
}

