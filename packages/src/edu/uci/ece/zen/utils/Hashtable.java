//$Id: Hashtable.java,v 1.3 2004/02/25 08:15:19 kraman Exp $
package edu.uci.ece.zen.utils;

/**
 * This class implements a hashtable which doesnot allocate any extra memory 
 * after initialization. It uses the next slot instead of chaining. This table
 * has O(n) complexity. Use only when needed.
 *
 * @author Krishna Raman
 * @version $Revision: 1.3 $ $Date: 2004/02/25 08:15:19 $
 */
public class Hashtable
{
    private int numObjects;
    private int totObjects;

    private Object[] table;
    private long[] keytable;

    public Hashtable(){}

    public void init( int limit ){
        //System.err.println( "ConnReg:   " + limit );
        totObjects = limit;
        table = new Object[4*limit];
        keytable = new long[4*limit];
        for( int i=0;i<keytable.length;i++ )
            keytable[i]=-1;
    }

    public void put( Object key , Object obj ) throws HashtableOverflowException{
        this.put( key.hashCode() , obj );
    }

    public Object get( Object key ){
        return this.get( key.hashCode() );
    }

    public void remove( Object key ){
        this.remove( key.hashCode() );
    }

    public void put( long key , Object obj ) throws edu.uci.ece.zen.utils.HashtableOverflowException{
        int hash = (int)(key % keytable.length);
        synchronized( table ){
            if( numObjects+1 > totObjects ){
                throw new edu.uci.ece.zen.utils.HashtableOverflowException();
            }

            for( int i=0;i<keytable.length;i++ ){
                int pos = (hash+i)%keytable.length;
                if( keytable[pos] == -1 || keytable[pos] == key ){
                    table[pos] = obj;
                    keytable[pos] = key;
                    numObjects++;
                    break;
                }
            }
        }
    }

    public Object get( long key ){
        int hash = (int)(key % keytable.length);

        synchronized( table ){
            for( int i=0;i<keytable.length;i++ ){
                int pos = (hash+i)%keytable.length;
                if( keytable[pos] != -1 && keytable[pos] == key )
                    return table[pos];
            }
        }
        return null;
    }

    public void remove( long key ){
        int hash = (int)(key % keytable.length);
        synchronized( table ){
            for( int i=0;i<keytable.length;i++ ){
                int pos = (hash+i)%keytable.length;
                if( keytable[pos] != -1 && keytable[pos]==key ){
                    table[pos] = null;
                    keytable[pos] = -1;
                    numObjects--;
                }
            }
        }
    }   
}

