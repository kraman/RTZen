% Genarate raphs for throughput for jitter
% comparing all implementations and remote + local
% see statements gset output ... for the filenames

clc;
clear;

% 1 - average throughput
% 2 - average latency
typeNr = 1;
% 1  - local calls 
% 10 - remote calls
localNr  = 10;

fNameBase = 'timeRecords.1.';

noOfBins = 999;
difference = 2;
stepSmall = 0.2;
stepNotThere = 0.00000000001;

barWidth = 0.1;

xIndexArray = [4 32 128 512 2048 8192];
for typeNr = [1 2]
  for localNr = [1 10]
    close all;
    for testNr = 1:1:3
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
	    s = diff(s(:, 3));
    
        minZen = [minZen min(s)];
        maxZen = [maxZen max(s)];
        avgZen = [avgZen mean(s)];
        stdZen = [stdZen std(s)];

        %text(xIndexArray([index], avgZen, num2str(avgZEn));

      end

     % compute what to output
     if (typeNr == 1)
        yValA = 1 ./ avgZen;
     end
     if (typeNr == 2)
        yValA = avgZen;
   
     end

     hold on;
     if ( testNr == 1 ) 
       loglog(xIndexArray, yValA, '-@;Zen;');
     end
     if ( testNr == 2 ) 
       loglog(xIndexArray, yValA, '-@;RTZen;');
     end
     if ( testNr == 3 ) 
       loglog(xIndexArray, yValA, '-@;TAO;');
     end


    end


    if (typeNr == 1)
      myTitle = 'Throughput';
      ylabel('Number of calls / second');
    end

    if (typeNr == 2)
      myTitle = 'Latency';
      ylabel('Roundtrip latency [sec]');
    end

    if (localNr == 1) 
      localStr = 'local';
	  myTitle = strcat('Average', myTitle, ' on Single Host Emulab');
    else
      localStr = 'remote';
      myTitle = strcat('Average', myTitle, ' between Two Emulab Hosts');
    end
 
    title(myTitle);
    xlabel('Message size [bytes]');
    %set(gca, 'xtick', xIndexArray);

	% set the output filename according to test and location
    if (typeNr == 1) 
		if (localNr == 10)
		  gset output 'throughput_remote_size.png';
		else
		  gset output 'throughput_local_size.png';
		end	
	else
		if (localNr == 10)
		  gset output 'latency_remote_size.png';
		else
		  gset output 'latency_local_size.png';
		end	
	end
	%set the term type and redraw to file
    gset term png;
    replot;
	gset output 'empty';
	gset term x11;
  end
end

