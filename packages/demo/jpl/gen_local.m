
clc;
clear;
close all;

fNameBase='timeRecords.1.';
title('Roundtrip Latency/Jitter, Single Host, Interpreted Mode');

noOfBins = 999;
difference = 2;
stepSmall = 0.2;
stepNotThere = 0.00000000001;

barWidth = 0.1;

xIndexArray = [4 32 128 512 2048 8192];

%xticks(2);
for index = 1:1:2

xIndex = index * difference;
	% clear fileName, hiss, pdfArray;

	fileName = strcat(fNameBase, num2str(round(index)),'.', num2str(round(index )),'.', num2str(round(128)), '.txt');
        s = load(fileName);
	%s = diff(s(:, 3));
    s = s(:,3);
	
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
	semilogy([xIndex - stepNotThere xIndex + stepNotThere], [minS maxS]);
	hold on;
    % splot the historam 
	%semilogy((hiss + xIndex) , pdfArray);
	semilogy((hiss + xIndex) , pdfArray);
	hold on;
    % place the dot for the average
	semilogy(xIndex , averageS, 'o');
	hold on;
    % minimum bar
    semilogy([xIndex - barWidth xIndex + barWidth], [minS minS]);
	hold on;
    % maximum bar
	semilogy([xIndex - barWidth xIndex + barWidth], [maxS maxS]);

end

ylabel('Roundtrip Latency [seconds] ');
%xlabel('ORB Implementation');

set(gca, 'xtick', [2 4]);
set(gca,'XTickLabel',{'Low Priority Task';'High Priority Task'})

%gset term tgif;
%gset output "timeRecords.gif";
%replot;
epsFName = strcat(fNameBase, 'eps');
%print -depsc2 latency_local.eps

saveas(gcf, 'latency_Local', 'epsc2')


