import sys
import os

classMap = {}
methodMap = {}
memAreaTimelines = {}
memHtml = {}

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
    splitLine = line.split( ',' )

    beforeEventType      = int( splitLine[0] )
    if( beforeEventType == 0 ):
        beforeEventType = "After entering ";
    else:
        beforeEventType = "After exiting ";
    beforeEventMethod    = int( splitLine[1] )
    afterEventType       = int( splitLine[2] )
    if( afterEventType == 0 ):
        afterEventType = "Before entering ";
    else:
        afterEventType = "Before exiting ";
    afterEventMethod     = int( splitLine[3] )
    leakType             = int( splitLine[4] )
    leakSize             = int( splitLine[5] )
    leakTime             = int( splitLine[6] )
    memSize              = int( splitLine[7] )
    memId                = int( splitLine[8] )

    if( not memAreaTimelines.has_key(memId) ):
        if( memId == -1 ):
            memAreaTimelines[memId] = open( "Immortal_timeline.txt" , "w" )
            memHtml[memId] = open( "Immortal.html" , "w" )
        else:
            memAreaTimelines[memId] = open( "mem" + str(memId) + "_timeline.txt" , "w" )
            memHtml[memId] = open( "mem" + str(memId) + ".html" , "w" )
        memHtml[memId].write( 
            "<html>\n" + 
            "\t<head>\n"+
            "\t\t<title>" + str(memId) + "</title>\n" +
            "\t</head>\n" +
            "\t<body>\n" +
            "\t\t<img src=\""+str(memId)+"\".jpg><br/>\n" +
            "\t\t<table>\n" +
            "\t\t\t<tr><td>After</td><td>Before</td><td>Leak size</td><td>Final memory size</td></tr>\n" )

    memAreaTimelines[memId].write( str(leakTime) + "\t" + str(memSize) + "\n" )
    memHtml[memId].write( "\t\t\t<tr><td>" + beforeEventType + methodMap[beforeEventMethod] + 
                               "</td><td>" + afterEventType + methodMap[afterEventMethod] +
                               "</td><td>" + str(leakSize) + 
                               "</td><td>" + str(memSize) + "</td></tr>\n" );

for memId in memAreaTimelines.keys():
    memAreaTimelines[memId].close()
    genGraph = open( "genGraph.sh" , "w" )
    genGraph.write( "set term jpeg\n" )
    genGraph.write( "set output \"" + str(memId) + ".jpg\"\n" )
    #genGraph.write( "set title \""+portalObject+"\"" )
    genGraph.write( "set ylabel \"bytes\"\n" )
    genGraph.write( "set xlabel \"time\"\n" )
    genGraph.write( "set grid\n" )
    if( memId == -1 ):
        genGraph.write( "plot \"" + "Immortal_timeline.txt\" with lines\n" )
    else:
        genGraph.write( "plot \"" + "mem" + str(memId) + "_timeline.txt\" with lines\n" )
    genGraph.close()
    os.system( "gnuplot genGraph.sh" )

for memId in memHtml.keys():
    memHtml[memId].write( 
            "\t\t</table>\n" +
            "\t</body>\n" +
            "</html>" )
    memHtml[memId].close()

#dotFile = open( "out.dot" , "w" )
#dotFile.write( "digraph zen_mem_hierarchy {\n" );
#for mem in memAreas.keys():
#    memAreas[mem].write( dotFile )
#for mem in memAreas.keys():
#    memAreas[mem].writeParent( dotFile )
#dotFile.write( "}\n" );
#dotFile.close()
#
#exec( "dot out.dot -Ipng -oout.png" )
