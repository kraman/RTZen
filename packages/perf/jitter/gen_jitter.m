% Generate graphs for the jitter measurments over each message size
% for all implementations and remote + local
% see statements gset output ... for the filenames

clc;
clear;

%The next 3 vars define the graph type, however they are
% changed later in the loop, so modify the loop header instead

% 1 - average throughput
% 2 - average latency
typeNr = 1;

% 1  - local calls 
% 10 - remote calls
localNr  = 10;

% 1 - Zen
% 2 - RTZen
% 3 - TAO
testNr = 2;

fNameBase = 'timeRecords.1.';

noOfBins = 999;
difference = 2;
stepSmall = 0.2;
stepNotThere = 0.00000000001;

barWidth = 0.1;

xIndexArray = [4 32 128 512 1024 4096];

for typeNr = [1]
  %for localNr = [1 10]
  for localNr = [1]
    for testNr = 1:1:4
      close all;
      minZen = [];
      maxZen = [];
      avgZen = [];
      stdZen = [];

      for index= 1:1:6

        xIndex = index * difference;

    	% clear fileName, hiss, pdfArray;
    
        fileName = strcat(fNameBase,num2str(round(testNr*localNr)),'.', num2str(round(testNr)),'.',num2str(round((xIndexArray([index])))), '.txt');
        % read the file and compute the difference
        s = load(fileName);
	    %s = diff(s(:, 3));
    
		minS = min(s);
		maxS = max(s);
		averageS = mean(s);
	
		hiss = hist(s, noOfBins);
		hiss = hiss/max(hiss);

		hiss = (difference - stepSmall) * hiss;

		stepSize = (maxS - minS)/noOfBins;
		pdfArray = (minS:stepSize:(maxS - stepSize));

		hold on;
	    % draw the line from min to max
		semilogy([xIndex - stepNotThere xIndex + stepNotThere], [minS maxS], '-3;;');
		hold on;
	    % splot the historam 
		%semilogy((hiss + xIndex) , pdfArray, '-1;;');
		semilogy((hiss + xIndex) , pdfArray, '-3;;');
		hold on;
	    % place the dot for the average
		semilogy(xIndex , averageS, '1o;;');
		hold on;
 	   % minimum bar
	    semilogy([xIndex - barWidth xIndex + barWidth], [minS minS],'-3;;');
		hold on;
	    % maximum bar
		semilogy([xIndex - barWidth xIndex + barWidth], [maxS maxS],'-3;;');
     %END for each size
     end
	
	 ylabel('Roundtrip Latency [sec]');


     if (testNr == 1)
       myTitle = 'RTZen on jRate';
     end
     if (testNr == 2)
       myTitle = 'TAO';
     end
     if (testNr == 3)
       myTitle = 'JacORB';
     end
    if (testNr == 4)
       myTitle = 'Simulated RTZen on JVM';
    end


     if (localNr == 1) 
	   myTitle = strcat('Roundtrip Latency, ', myTitle, ', Single Host Emulab');
     else
       myTitle = strcat('Roundtrip Latency, ', myTitle, ', Two Emulab Hosts');
     end
 
     title(myTitle);
     xlabel('Message size [bytes]');
     %set(gca, 'xtick', xIndexArray);

	 % set the output filename according to test and location
	 % I did not make it so that gset output would accept a 
	 % variable name as input :-((
     automatic_replot = 0
     gset term postscript color
         
	 if (localNr == 10)
        if (testNr == 1)
    	  gset output 'jitter_RTZen_remote.eps';
        end
        if (testNr == 2)
    	  gset output 'jitter_TAO_remote.eps';
        end
        if (testNr == 3)
    	  gset output 'jitter_JacORB_remote.eps';
        end
	 else
        if (testNr == 1)
    	  gset output 'jitter_RTZen_local.eps';
        end
        if (testNr == 2)
    	  gset output 'jitter_TAO_local.eps';
        end
        if (testNr == 3)
    	  gset output 'jitter_JacORB_local.eps';
          end
          if (testNr == 4)
    gset output 'jitter_Simu_RTZen_local.eps';
    end
                          
	 end	
	 %set the term type and redraw to file

     %gset term png;
     replot;
	 gset output 'empty';
	 gset term x11;

   %END for each implementation
   end

  %END for each location
  end

%END for each type
end

