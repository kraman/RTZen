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
    private long[] genCountTable;

    public Hashtable(){}

    public void init( int limit ){
        //System.err.println( "ConnReg:   " + limit );
        totObjects = limit;
        table = new Object[4*limit];
        keytable = new long[4*limit];
        genCountTable = new long[4*limit];
        for( int i=0;i<keytable.length;i++ ){
            keytable[i]=-1;
            genCountTable[i]=0;
        }
    }

    public void put( Object key , Object obj ) throws HashtableOverflowException{
        this.put( key.hashCode() , obj );
    }

    public Object get( Object key ){
        return this.get( key.hashCode() );
    }

    protected int getTableEntry( Object key ){
        return this.getTableEntry( key.hashCode() );
    }

    protected void setTableEntry( int index , Object key , Object obj , boolean incrGenCount ){
        this.setTableEntry( index , key.hashCode() , obj , incrGenCount );
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
                    genCountTable[pos]++;
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

    protected int getTableEntry( long key ){
        int hash = (int)(key % keytable.length);

        synchronized( table ){
            for( int i=0;i<keytable.length;i++ ){
                int pos = (hash+i)%keytable.length;
                if( keytable[pos] != -1 && keytable[pos] == key )
                    return pos;
            }
        }
        return -1;
    }

    protected Object getEntryAtIndex( int idx ){
        synchronized( table ){
            return table[idx];
        }
    }

    protected void setTableEntry( int index , long key , Object obj , boolean incrGenCount ){
        synchronized( table ){
            keytable[index] = key;
            table[index] = obj;
            if( incrGenCount )
                genCountTable[index]++;
        }
    }

    public long getGenCount( int index ){
        synchronized( table ){
            return genCountTable[index];
        }
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

