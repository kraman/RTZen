clc;
clear;
close all;

noOfBins = 999;
difference = 2;
stepSmall = 0.2;

barWidth = 0.1;

xIndexArray = [4 32 128 512];


for xIndex = 2:difference:8

	% clear fileName, hiss, pdfArray;

	fileName = strcat('timeRecords.1.3.2.', num2str(round((xIndexArray([xIndex/difference])))), '.txt');
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
	semilogy([xIndex xIndex], [minS maxS], 'r');
	hold on;
        % semilogy the historam 
	%semilogy((hiss + xIndex) , pdfArray);
	semilogy((hiss + xIndex) , pdfArray);
	hold on;

	semilogy(xIndex , averageS, 'bo');
	hold on;

        semilogy([xIndex - barWidth xIndex + barWidth], [minS minS]);
	hold on;
	semilogy([xIndex - barWidth xIndex + barWidth], [maxS maxS]);

end

title('Roundtrip Latency/Jitter, RTZen/TimeSys, Single Host Emulab');
ylabel('Roundtrip Latency [seconds] ');
xlabel('Message size [bytes] (4 32 128 512)');
set(gca, 'xtick', xIndexArray);
