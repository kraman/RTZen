clc;
clear;
close all;

noOfBins = 999;
difference = 2;
stepSmall = 0.2;

barWidth = 0.1;

xIndexArray = [2];

for xIndex=xIndexArray

	% clear fileName, hiss, pdfArray;

	fileName = strcat('timeRecords', num2str(round((xIndex))), '.txt');
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
	plot([xIndex xIndex], [minS maxS], 'r');
	hold on;
	plot((hiss + xIndex) , pdfArray);
	hold on;

	plot(xIndex , averageS, 'bo');
	hold on;

	plot([xIndex - barWidth xIndex + barWidth], [minS minS]);
	hold on;
	plot([xIndex - barWidth xIndex + barWidth], [maxS maxS]);

end

title('tttt');
ylabel('koskfdsf');
xlabel('ze y');

set(gca, 'xtick', xIndexArray);
