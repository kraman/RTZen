% Genarate raphs for throughput for jitter
% comparing all implementations and remote + local
% see statements gset output ... for the filenames

## tics(axis,[pos1,pos2,...],['lab1';'lab2';...])
##
## Explicitly set the tic positions and labels for the given axis.
##
## If no positions or labels are given, then restore the default.
## If positions are given but no labels, use those positions with the
## normal labels.  If positions and labels are given, each position
## labeled with the corresponding row from the label matrix.
##
## Axis is 'x', 'y' or 'z'.

## This program is in the public domain
## Author: Paul Kienzle <pkienzle@users.sf.net>

function tics(axis,pos,lab)

  if nargin == 1
    eval(["gset ", axis, "tics autofreq"]);
  elseif nargin == 2
    tics = sprintf(' %g,', pos);
    tics(length(tics)) = ' ';
    eval(["gset ", axis, "tics (", tics, ")"]);
  elseif nargin == 3
    tics = sprintf('"%s" %g', deblank(lab(1,:)), pos(1));
    for i=2:rows(lab)
      tics = [tics, sprintf(', "%s" %g', deblank(lab(i,:)), pos(i)) ];
    endfor
    eval([ "gset ", axis, "tics (", tics ,")" ]);
  else
    usage("tics(axis,[pos1,pos2,...],['lab1';'lab2';...])");
  endif

endfunction

## Copyright (C) 2000 Paul Kienzle
##
## This program is free software; you can redistribute it and/or modify
## it under the terms of the GNU General Public License as published by
## the Free Software Foundation; either version 2 of the License, or
## (at your option) any later version.
##
## This program is distributed in the hope that it will be useful,
## but WITHOUT ANY WARRANTY; without even the implied warranty of
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
## GNU General Public License for more details.
##
## You should have received a copy of the GNU General Public License
## along with this program; if not, write to the Free Software
## Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

## text(x,y[,z],'text','property',value,...)
##    Add 'text' to the plot at position (x,y,z). 
##
## text('property',value,...)
##    Property controls the features of the text:
##    'HorizontalAlignment': value is 'left' (default), 'right' or 'center'
##        where to locate the text relative to (x,y)
##    'Units': value is 'data' (default), 'normalized' or 'screen'
##        data uses the coordinate system of the axes.
##        normalized uses x,y,z values in the range [0,1] for the axes.
##        screen uses x,y values in the range [0,1] for the window/page.
##            note that this only works if called after the last subplot.
##    'Rotation' : value is degrees
##    'FontName' : value is the name of the font (terminal dependent)
##    'FontSize' : value is the size of the font (terminal dependent)
##    'Position' : value is [x, y] or [x, y, z]
##    'String': value is 'text'
##
## text();
##    Clear all text from the plot (must be used before the next plot
##    since the labels persist from plot to plot).
##
##
## Example
##    text(0.5,0.50,'Graph center',...
##         'HorizontalAlignment','center','Units','normalized');
##    plot(linspace(-pi,pi),linspace(0,e));

## TODO: label property should be automatically cleared in plot
## TODO:   if hold is not on.  Same for title, xlabel, ylable, etc.,
## TODO:   so handle text in the same way, by requiring text with no
## TODO:   to clear all the labels for the next graph.
## TODO: several properties missing
## TODO: permit text(blah,'units','screen') before subplot as well as after
function text(varargin)
  usage_str = "text(x,y[,z],'text', 'property',value...)";
  if nargin == 0, gset nolabel; return; endif

  position=[0, 0, 0];
  str="";
  rotate="norotate";
  align="left";
  fontname="";
  fontsize=[];
  units="first";

  ## Process text(x,y[,z],'text') forms
  arg = varargin{1};
  if is_scalar(arg),
    position(1) = arg;
    if nargin < 2, usage(usage_str); endif
    arg = varargin{2};
    if !is_scalar(arg), usage(usage_str); endif
    position(2) = arg;

    if nargin < 3, usage(usage_str); endif
    arg = varargin{3};
    if isstr(arg)
      str=arg;
      n=4;
    else
      position(3) = arg;
      if nargin < 4, usage(usage_str); endif
      str=varargin{4};
      n=5;
    endif
    if !isstr(str), usage(usage_str); endif
  else
    n=1;
  endif

  ## Process text('property',value) forms
  if rem(nargin-(n-1), 2) != 0, error(usage_str); endif
  for i=n:2:nargin
    prop=varargin{i}; val=varargin{i+1};
    if !isstr(prop), error(usage_str); endif
    prop = tolower(prop);
    if strcmp(prop, "fontname"),
      if !isstr(val), 
	error("text 'FontName' expects a string"); endif
      fontname = val;
    elseif strcmp(prop, "fontsize"),
      if !is_scalar(val), 
	error("text 'FontSize' expects a scalar"); endif
      fontsize = val;
    elseif strcmp(prop, "horizontalalignment"),
      if isstr(val), val=tolower(val); endif
      if !isstr(val) || ...
	    !(strcmp(val,"left")||strcmp(val,"right")||strcmp(val,"center"))
	error("text 'HorizontalAlignment' expects 'right','left' or 'center'");
      endif
      align = val;
    elseif strcmp(prop, "units")
      if isstr(val), val=tolower(val); endif
      if !isstr(val)
	error("text 'Units' expects 'data' or 'normalized'");
      elseif strcmp(val,"data")
	units="first";
      elseif strcmp(val,"normalized")
	units="graph";
      elseif strcmp(val,"screen")
	units="screen";
      else
	warning(["text('Units','", val, "') ignored"]);
      endif
    elseif strcmp(prop, "position")
      if !isreal(val) || length(val)<2 || length(val)>3
	error("text 'Position' expects vector of x,y and maybe z"); 
      elseif length(val)==2, position=postpad(val,3);
      else position = val;
      endif
    elseif strcmp(prop, "rotation")
      if !is_scalar(val), error("text 'Rotation' expects scalar"); endif
      if mod(val+45,180)<=90
	rotate="norotate";
      else
	rotate="rotate";
      endif
    elseif strcmp(prop, "string")
      if !isstr(val), error("text 'String' expects a string"); endif
      str = val;
    else
      warning(["ignoring property ", prop]);
    endif
  endfor
  if !isempty(fontsize)
    font = sprintf(' font "%s,%d"', fontname, fontsize);
  elseif !isempty(fontname)
    font = sprintf(' font "%s"', fontname);
  else
    font = "";
  endif
  if position(3)!=0,
    atstr = sprintf("%g,%g,%g", position(1),position(2),position(3));
  else
    atstr = sprintf("%g,%g", position(1),position(2));
  endif
  command = sprintf('gset label "%s" at %s %s %s %s%s',
		    str, units, atstr, align, rotate, font);
  eval(command);

endfunction
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

xIndexArray = [4 32 128 512 1024 4096];
for typeNr = [1 2]
  %localNr = [1 10]
  for localNr = [1]
   %close all;
    %for testNr = 1:1:3
    for testNr = [1, 2, 3, 4]
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
	    % s = diff(s(:, 3));
    
        minZen = [minZen min(s)];
        maxZen = [maxZen max(s)];
        avgZen = [avgZen mean(s)];
        stdZen = [stdZen std(s)];

        %text(xIndexArray([index], avgZen, num2str(avgZEn));

      end

     % compute what to output
     if (typeNr == 1)
        yValA = 1000000*1 ./ avgZen;
     end
     if (typeNr == 2)
        yValA = avgZen;
   
     end

     hold on;
     if ( testNr == 1 ) 
       loglog(xIndexArray, yValA, '-@;RTZen on jRate;');
       %semilogx(xIndexArray, yValA, '-@;RTZen on jRate;');
     end
     if ( testNr == 2 ) 
       loglog(xIndexArray, yValA, '-@;TAO;');
       %semilogx(xIndexArray, yValA, '-@;TAO;');
     end
     if ( testNr == 3 ) 
       loglog(xIndexArray, yValA, '-@;JacORB;');
       %semilogx(xIndexArray, yValA, '-@;TAO;');
     end
     if ( testNr == 4 )
       loglog(xIndexArray, yValA, '-@;Simulated RTZen on JVM;');
       %semilogx(xIndexArray, yValA, '-@;TAO;');
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
	  myTitle = strcat('Average ', myTitle, ' on Single Host');
    else
      localStr = 'remote';
      myTitle = strcat('Average ', myTitle, ' between Two Hosts');
    end
 
    title(myTitle);
    xlabel('Message size [bytes]');
    %set(gca, 'xtick', xIndexArray);
    %text(xIndexArray([index]),0,xIndexArray([index]));
    %text(4,0,'4');

    tics('x',[4 32 128 512 1024 4096],['4';'32';'128';'512';'1024';'4096']);


    %set(gca, 'xtick', [2 4 6 8 10 12]);
    %set(gca,'XTickLabel',{'4'; '32'; '128'; '512'; '1024'; '4096'});
    
%    set(gca,'XTickLabel',{'Zen';'RTZen (TJVM)';'TAO'})
    
    
    automatic_replot = 0
    gset term postscript color

	% set the output filename according to test and location
    if (typeNr == 1) 
		if (localNr == 10)
		  gset output 'throughput_remote_size.eps';
		else
		  gset output 'throughput_local_size.eps';
		end	
	else
		if (localNr == 10)
		  gset output 'latency_remote_size.eps';
		else
		  gset output 'latency_local_size.eps';
		end	
	end
	%set the term type and redraw to file
    %gset term png;
    replot;
	gset output 'empty';
	gset term x11;
  end
end

