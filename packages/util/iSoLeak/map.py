import sys

classMap = {}
methodMap = {}
memAreas = {}
threads = {}

class MemoryArea:
    leakyMethods = {}
    totalMemAllocated = 0
    parentMem = 0
    portal = -2
    id = -1

    def setId( self , id ):
        self.id = id

    def setPortal( self , portal ):
        if( self.portal == -2 or self.portal == -1 ):
            self.portal = portal
    
    def setParent( self , parent ):
        if( self.parentMem == 0 and parent != self.id ):
            self.parentMem = parent

    def write( self , ostream ):
        ostream.write( str(self.id) )
        if( self.portal == -2 ):
            ostream.write('[ label="T" , shape=box ]\n')
        else:
            if( self.portal == -1 ):
                ostream.write('[ label="Unkn" , shape=box ]\n')
            else:
                ostream.write( '[ label="' )
                className = classMap[self.portal].split( "." )
                className = className[ len(className)-1 ]
                ostream.write( className )
                ostream.write('" , shape=box ]\n')

    def writeParent( self  , ostream ):
        if( self.parentMem != 0 ):
            ostream.write( str(self.parentMem) )
            ostream.write( "->" )
            ostream.write( str(self.id) )
            ostream.write( "\n" )

def processLeak( memAreas , methodId , data ):
    #print data
    return

mapFile = open( "iSoLeak.map" )
for line in mapFile:
    line = line.rstrip()
    splitLine = line.split(',')
    if( splitLine[0] == "class" ):
        classMap[ int(splitLine[2]) ] = splitLine[1]
    else:
        methodMap[ int(splitLine[2]) ] = splitLine[1]

logFile = open( sys.argv[1] )
for line in logFile:
    line = line.rstrip()
    if( line.startswith( "ISoL1," ) ):
        splitLine = line.split( ',' )
        
        memArea   = int( splitLine[1] )
        threadId  = int( splitLine[2] )
        if( not memAreas.has_key( memArea ) ):
            memAreas[ memArea ] = MemoryArea()
            memAreas[ memArea ].setId( memArea );
        if( threads.has_key( threadId ) ):
            parentMem = threads[ threadId ]
            memAreas[ memArea ].setParent( parentMem )
        threads[ threadId ] = memArea

    if( line.startswith( "ISoL2," ) ):
        splitLine = line.split( ',' )
        
        methodId  = int( splitLine[1] )
        memArea   = int( splitLine[2] )
        threadId  = int( splitLine[3] )
        portalObj = int( splitLine[4] )
        if( not memAreas.has_key( memArea ) ):
            memAreas[ memArea ] = MemoryArea()
            memAreas[ memArea ].setId( memArea );
        if( threads.has_key( threadId ) ):
            childMem = threads[ threadId ]
            memAreas[ childMem ].setParent( memArea )
        threads[ threadId ] = memArea

        memAreas[ memArea ].setPortal( portalObj )

        if( len(splitLine) > 5 ):
            processLeak( memArea , methodId , splitLine[5] )
        if( len(splitLine) > 6 ):
            processLeak( memArea , methodId , splitLine[6] )

dotFile = open( "out.dot" , "w" )
dotFile.write( "digraph zen_mem_hierarchy {\n" );
for mem in memAreas.keys():
    memAreas[mem].write( dotFile )
for mem in memAreas.keys():
    memAreas[mem].writeParent( dotFile )
dotFile.write( "}\n" );
dotFile.close()

exec( "dot out.dot -Ipng -oout.png" )
