clc;
clear;
close all;

% 1 - average throughput
% 2 - average latency
typeNr = 1;
% 1  - local calls 
% 10 - remote calls
localNr  = 1;

fNameBase = 'timeRecords.1.';

noOfBins = 999;
difference = 2;
stepSmall = 0.2;
stepNotThere = 0.00000000001;

barWidth = 0.1;

xIndexArray = [4 32 128 512 1024 4096];
for localNr = [1]
if (localNr == 1) 
  file = fopen('rawOutput.local.txt','w');
else
  file = fopen('rawOutput.remote.txt','w');
end

fprintf(file,'ORB, size, min latency [us], avg latency [us], max latency [us], std deviation [us], throughput [calls/sec]\n');
for testNr = 1:1:4
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
    
    minZen = [minZen min(s)];
    maxZen = [maxZen max(s)];
    avgZen = [avgZen mean(s)];
    stdZen = [stdZen std(s)];

    %text(xIndexArray([index], avgZen, num2str(avgZEn));

  %end


  hold on;

  if ( testNr == 1 ) 
	orbName='RTZen on jRate';
  end
  if ( testNr == 2 ) 
	orbName='TAO';
  end
  if ( testNr == 3 ) 
	orbName='JacORB';
  end

  if ( testNr == 4 )
    orbName='Simulated RTZen on JVM';
    end
        

  %i=4;
  fprintf(file,'%s,%d,%f,%f,%f,%f, %d\n',orbName, xIndexArray([index]), minZen(index), avgZen(index), maxZen(index), stdZen(index), 1000000/avgZen(index));
  end
  


end
%if (typeNr == 1)
 % myTitle = 'Average Throughput';
 % ylabel('Number of calls / second');
%end

%if (typeNr == 2)
 % myTitle = 'Average Latency';
 % ylabel('Roundtrip latency [sec]');
%end

%if (localNr == 1) 
 % myTitle = strcat(myTitle, ' on Single Host Emulab');
%else
%  myTitle = strcat(myTitle, ' between Two Emulab Hosts');
%end
 
%title(myTitle);
%xlabel('Message size [bytes]');
%set(gca, 'xtick', xIndexArray);

fclose(file);
end
