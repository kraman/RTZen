clc;
clear;
close all;

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
for localNr = [1 10]
if (localNr == 1) 
  file = fopen('rawOutput.local.txt','w');
else
  file = fopen('rawOutput.remote.txt','w');
end

fprintf(file,'ORB, min latency [ms], avg latency [ms], max latency [ms], std deviation [ms], throughput [calls/sec]\n');
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
    %loglog(xIndexArray, yValA, '-@;Zen;');
  end
  if ( testNr == 2 ) 
    %loglog(xIndexArray, yValA, '-@;RTZen;');
  end
  if ( testNr == 3 ) 
    %loglog(xIndexArray, yValA, '-@;TAO;');
  end

  if ( testNr == 1 ) 
	orbName='Zen';
  end
  if ( testNr == 2 ) 
	orbName='RTZen';
  end
  if ( testNr == 3 ) 
	orbName='TAO';
  end

  i=4;
  fprintf(file,'%s,%f,%f,%f,%f, %d\n',orbName, 1000 * minZen(i), 1000* avgZen(i), 1000*maxZen(i), 1000* stdZen(i), 1/avgZen(i));
  
end


if (typeNr == 1)
  myTitle = 'Average Throughput';
  ylabel('Number of calls / second');
end

if (typeNr == 2)
  myTitle = 'Average Latency';
  ylabel('Roundtrip latency [sec]');
end

if (localNr == 1) 
  myTitle = strcat(myTitle, ' on Single Host Emulab');
else
  myTitle = strcat(myTitle, ' between Two Emulab Hosts');
end
 
title(myTitle);
xlabel('Message size [bytes]');
%set(gca, 'xtick', xIndexArray);

fclose(file);
end
