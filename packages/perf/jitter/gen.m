
fNameBase = "timeRecords.1.3.2.";

%clc;
%clear;
%close all;

noOfBins = 999;
difference = 2;
stepSmall = 0.2;
stepNotThere = 0.00000000001;

barWidth = 0.1;

xIndexArray = [4 32 128 512];

%xticks(2);
for xIndex = 2:difference:8

	% clear fileName, hiss, pdfArray;

	fileName = strcat(fNameBase, num2str(round((xIndexArray([xIndex/difference])))), '.txt');
        s = load(fileName);
	s = diff(s(:, 3));

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

end

title('Roundtrip Latency/Jitter, RTZen/TimeSys, Single Host Emulab');
ylabel('Roundtrip Latency [seconds] ');
xlabel('Message size [bytes] (4 32 128 512)');
%set(gca, 'xtick', xIndexArray);

gset term postscript eps color;
gset output "timeRecords.eps";
replot;
