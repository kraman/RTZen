package edu.uci.ece.zen.utils;

public class HashtableOverflowException extends Exception{
    public HashtableOverflowException(){
        super( "No more space in hashtable." );
    }
}
