/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package demo.doom;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

public final class GamePane extends JPanel
	implements Runnable
{
	public GamePane(DoomClient doomclient)
	{
		super(false);
		nextPaletteEntry = 1;
		paletteReds = new byte[256];
		paletteGreens = new byte[256];
		paletteBlues = new byte[256];
		oldTileOffset = 666;
		doomClient = doomclient;
	}

	public void convertPixels(int i, int j, int ai[], byte abyte0[])
	{
		boolean flag = false;
		for(int k = 0; k < i * j; k++)
		{
			byte byte0 = (byte)(ai[k] >> 16 & 0xf8);
			byte byte1 = (byte)(ai[k] >> 8 & 0xf8);
			byte byte2 = (byte)(ai[k] & 0xf8);
			if(byte0 == 0 && byte1 == 64 && byte2 == 0)
			{
				abyte0[k] = 0;
			} else
			{
				boolean flag1 = false;
				for(int l = 0; !flag1 && l < nextPaletteEntry; l++)
					if(paletteReds[l] == byte0 && paletteGreens[l] == byte1 && paletteBlues[l] == byte2)
					{
						flag1 = true;
						abyte0[k] = (byte)l;
					}

				if(!flag1)
				{
					paletteReds[nextPaletteEntry] = byte0;
					paletteGreens[nextPaletteEntry] = byte1;
					paletteBlues[nextPaletteEntry] = byte2;
					abyte0[k] = (byte)nextPaletteEntry;
					nextPaletteEntry++;
					if(nextPaletteEntry == 256)
						System.err.println("ERROR: Too many colours in textures");
				}
			}
		}

	}

	void createShadeTable(byte abyte0[], IndexColorModel indexcolormodel, int i, int j, int k)
	{
		int i3 = 0;
		for(int i2 = 0; i2 < 256; i2++)
		{
			for(int j2 = 0; j2 < 256; j2++)
			{
				int j3 = j2 - 128;
				if(j3 < 0)
					j3 = 256 + j3;
				int l = i + ((indexcolormodel.getRed(j3) - i) * i2) / 256;
				int i1 = j + ((indexcolormodel.getGreen(j3) - j) * i2) / 256;
				int j1 = k + ((indexcolormodel.getBlue(j3) - k) * i2) / 256;
				int l1 = 768;
				int l2 = 0;
				for(int k2 = 0; k2 < 256; k2++)
				{
					int k1 = Math.abs(indexcolormodel.getRed(k2) - l) + Math.abs(indexcolormodel.getGreen(k2) - i1) + Math.abs(indexcolormodel.getBlue(k2) - j1);
					if(k1 < l1)
					{
						l1 = k1;
						l2 = k2;
					}
				}

				abyte0[i3] = (byte)l2;
				i3++;
			}

		}

	}

	public void downLeft()
	{
		intersectionX = currentRayXpos & 0xffff0000;
		intersectionY = (currentRayYpos & 0xffff0000) + 0x10000;
		while(blockHit == -1) 
		{
			XHops = (double)(intersectionX - currentRayXpos) / XHopSize;
			YHops = (double)(intersectionY - currentRayYpos) / YHopSize;
			if(XHops < YHops)
			{
				currentRayXpos = intersectionX;
				currentRayYpos += XHops * YHopSize;
				if(map[((intersectionY >> 16) - 1 << 8) + ((intersectionX >> 16) - 1)] > -1)
				{
					blockHit = map[((intersectionY >> 16) - 1 << 8) + ((intersectionX >> 16) - 1)] * 2 + 1;
					blockLightRecord[screenX] = lightMap[((intersectionY & 0xffff0000) >> 8) + (intersectionX >> 16)];
				} else
				{
					intersectionX -= 0x10000;
				}
			} else
			{
				currentRayYpos = intersectionY;
				currentRayXpos += YHops * XHopSize;
				if(map[((intersectionY >> 16) << 8) + (intersectionX >> 16)] > -1)
				{
					blockHit = map[((intersectionY >> 16) << 8) + (intersectionX >> 16)] * 2;
					blockLightRecord[screenX] = lightMap[((intersectionY & 0xffff0000) >> 8) + (intersectionX >> 16)];
				} else
				{
					intersectionY += 0x10000;
				}
			}
		}
	}

	public void downRight()
	{
		intersectionX = (currentRayXpos & 0xffff0000) + 0x10000;
		intersectionY = (currentRayYpos & 0xffff0000) + 0x10000;
		while(blockHit == -1) 
		{
			XHops = (double)(intersectionX - currentRayXpos) / XHopSize;
			YHops = (double)(intersectionY - currentRayYpos) / YHopSize;
			if(XHops < YHops)
			{
				currentRayXpos = intersectionX;
				currentRayYpos += XHops * YHopSize;
				if(map[((intersectionY >> 16) - 1 << 8) + (intersectionX >> 16)] > -1)
				{
					blockHit = map[((intersectionY >> 16) - 1 << 8) + (intersectionX >> 16)] * 2 + 1;
					blockLightRecord[screenX] = lightMap[((intersectionY & 0xffff0000) >> 8) + (intersectionX >> 16)];
				} else
				{
					intersectionX += 0x10000;
				}
			} else
			{
				currentRayYpos = intersectionY;
				currentRayXpos += YHops * XHopSize;
				if(map[((intersectionY >> 16) << 8) + ((intersectionX >> 16) - 1)] > -1)
				{
					blockHit = map[((intersectionY >> 16) << 8) + ((intersectionX >> 16) - 1)] * 2;
					blockLightRecord[screenX] = lightMap[((intersectionY & 0xffff0000) >> 8) + (intersectionX >> 16)];
				} else
				{
					intersectionY += 0x10000;
				}
			}
		}
	}

	public void drawFloor()
	{
		int i = 0;
		floorY = (int)((double)(33280 * appletHeight) / furthestWall) / 2;
		if(floorY < appletHeight)
		{
			floorDestOffset = appletWidth * (appletHeight / 2) + floorY * appletWidth;
			ceilingDestOffset = appletWidth * (appletHeight / 2 - 1) - floorY * appletWidth;
			for(; floorY < appletHeight / 2; floorY++)
			{
				runStartX = startRayXpos + (Math.sin(6.2831853071795862D + rayDirection) * multiplier) / (double)floorY;
				runEndX = startRayXpos + (Math.sin(rayDirection - rotationStep * (double)appletWidth) * multiplier) / (double)floorY;
				textureXStep = (int)((runEndX - runStartX) / (double)appletWidth);
				textureXpos = (int)runStartX;
				runStartY = startRayYpos + (Math.cos(6.2831853071795862D + rayDirection) * multiplier) / (double)floorY;
				runEndY = startRayYpos + (Math.cos(rayDirection - rotationStep * (double)appletWidth) * multiplier) / (double)floorY;
				textureYStep = (int)((runEndY - runStartY) / (double)appletWidth);
				textureYpos = (int)runStartY;
				if(runStartX < 0.0D || runStartX > 16777216D || runStartY < 0.0D || runStartY > 16777216D || runEndX < 0.0D || runEndX > 16777216D || runEndY < 0.0D || runEndY > 16777216D)
				{
					yAtMinX = runStartY + ((runEndY - runStartY) / (runEndX - runStartX)) * (0.0D - runStartX);
					yAtMaxX = runStartY + ((runEndY - runStartY) / (runEndX - runStartX)) * (16777215D - runStartX);
					xAtMinY = runStartX + ((runEndX - runStartX) / (runEndY - runStartY)) * (0.0D - runStartY);
					xAtMaxY = runStartX + ((runEndX - runStartX) / (runEndY - runStartY)) * (16777215D - runStartY);
					floorDestOffset += appletWidth;
					ceilingDestOffset -= appletWidth;
				} else
				{
					for(floorX = 0; floorX < appletWidth; floorX++)
					{
						tileOffset = ((textureYpos & 0xffff0000) >> 8) + (textureXpos >> 16);
						if(tileOffset != oldTileOffset)
						{
							i = (lightMap[tileOffset] << 8) + 128;
							floorTexture = wallTextureData[floorMap[tileOffset]];
							ceilingTexture = wallTextureData[ceilingMap[tileOffset]];
						}
						pixelOffset = ((textureYpos & 0xfc0f) >> 4) + ((textureXpos & 0xffff) >> 10);

						int shadeTableIndex = floorTexture[pixelOffset] + i;
						
						// KLUDGE WARNING: This is not my code, and I don't plan to fix it, so I'm adding
						// the following kludge to prevent array indexes < 0 from causing exceptions.
						if (shadeTableIndex < 0)
							shadeTableIndex = 0;
						
						// KLUDGE WARNING: This is not my code, and I don't plan to fix it, so I'm adding
						// the following kludge to prevent array indexes < 0 from causing exceptions.
						if (floorDestOffset < 0)
							floorDestOffset = 0;
						
						renderBuffer[floorDestOffset] = shadeTable[shadeTableIndex];

						renderBuffer[ceilingDestOffset] = shadeTable[ceilingTexture[pixelOffset] + i];
						textureXpos += textureXStep;
						textureYpos += textureYStep;
						floorDestOffset++;
						ceilingDestOffset++;
						oldTileOffset = tileOffset;
					}

					ceilingDestOffset -= appletWidth * 2;
				}
			}

		}
	}

	public void drawObject(int i, int j, int k, int l)
	{
		int l1 = 0;
		if(j > 0)
		{
			int j1 = 0x400000 / j;
			int i1 = i - j / 2;
			if(i + j / 2 >= 0 && i - j / 2 < appletWidth)
			{
				int k1 = i + j / 2;
				if(k1 > appletWidth)
					k1 = appletWidth;
				if(i1 < 0)
				{
					l1 -= j1 * i1;
					i1 = 0;
				}
				while(i1 < k1) 
				{
					if(zBuffer[i1] > k)
						renderBufferVLine(objectTextureData[0], i1, j, l1 >> 16, true, l);
					i1++;
					l1 += j1;
				}
			}
		}
	}

	public void initialiser()
	{
		myGraphics = getGraphics();
		myGraphics.setPaintMode();
		appletWidth = getSize().width;
		appletHeight = getSize().height;
		multiplier = (double)((33280 * appletWidth) / 2) * 1.3999999999999999D;
		renderBuffer = new byte[appletWidth * appletHeight];
		map = doomClient.curMap.map;
		lightMap = doomClient.curMap.lightMap;
		floorMap = doomClient.curMap.floorMap;
		ceilingMap = doomClient.curMap.ceilingMap;
		blockLightRecord = new int[appletWidth];
		blockHitRecord = new int[appletWidth];
		blockDistanceRecord = new int[appletWidth];
		textureXRecord = new int[appletWidth];
		loadTextures();
		rotationStep = 6.2831853071795862D / (double)(appletWidth * 4);
		distanceAdjuster = new double[appletWidth];
		double d = Math.sin(2.3561944901923448D);
		double d1 = Math.sin(3.9269908169872414D);
		for(counter = 0; counter < appletWidth; counter++)
		{
			XHopSize = d + ((d1 - d) * (double)counter) / (double)appletWidth;
			distanceAdjuster[counter] = Math.sqrt(XHopSize * XHopSize + 0.5D) * 1.3999999999999999D;
		}

		myColorModel = new IndexColorModel(8, 256, paletteReds, paletteGreens, paletteBlues);
		shadeTable = new byte[0x10000];
		createShadeTable(shadeTable, myColorModel, 0, 0, 0);
		zBuffer = new int[appletWidth];
		playerXpos = 8421376D;
		playerYpos = 8421376D;
		playerDirection = 1.5707963267948966D;
		rightStepping = leftStepping = turningLeft = turningRight = runningForwards = runningBackwards = false;
		myGraphics.setColor(Color.black);
		myGraphics.fillRect(0, 0, appletWidth, appletHeight);
		pictureArray = new MemoryImageSource(appletWidth, appletHeight, myColorModel, renderBuffer, 0, appletWidth);
		pictureArray.setAnimated(true);
		displayImage = createImage(pictureArray);
		numObjects = 0;
		objectXpos = new double[10];
		objectZpos = new double[10];
		threadRunning = true;
		renderer = new Thread(this, "renderer");
		renderer.start();
	}

	public void loadTextureGun(String s, byte abyte0[])
	{
		int ai[] = new int[22500];
		String imageFile = "images/" + s + ".gif";
		Image image = Toolkit.getDefaultToolkit().getImage(imageFile);
		MediaTracker mediatracker = new MediaTracker(this);
		mediatracker.addImage(image, 1);
		try
		{
			mediatracker.waitForAll();
		}
		catch(InterruptedException _ex)
		{
			System.err.println("Image loading interrupted");
		}
		if(mediatracker.isErrorAny())
			System.err.println("Problems loading texture " + imageFile);

		PixelGrabber pixelgrabber = new PixelGrabber(image, 0, 0, 150, 150, ai, 0, 150);
		try
		{
			pixelgrabber.grabPixels();
		}
		catch(InterruptedException _ex)
		{
			System.err.println("Pixel grabber interrupted");
		}
		convertPixels(150, 150, ai, abyte0);
	}

	public void loadTextureSet(int i, String s, byte abyte0[][])
	{
		int ai[] = new int[4096];
		for(counter = 0; counter < i; counter++)
		{
			String imageFile = "images/" + s + counter + ".gif";
			Image image = Toolkit.getDefaultToolkit().getImage(imageFile);
			MediaTracker mediatracker = new MediaTracker(this);
			mediatracker.addImage(image, 1);
			try
			{
				mediatracker.waitForAll();
			}
			catch(InterruptedException _ex)
			{
				System.err.println("Image loading interrupted");
			}
			if(mediatracker.isErrorAny())
				System.err.println("Problems loading texture " + imageFile);
			PixelGrabber pixelgrabber = new PixelGrabber(image, 0, 0, 64, 64, ai, 0, 64);
			try
			{
				pixelgrabber.grabPixels();
			}
			catch(InterruptedException _ex)
			{
				System.err.println("Pixel grabber interrupted");
			}
			convertPixels(64, 64, ai, abyte0[counter]);
		}

	}

	public void loadTextures()
	{
		wallTextureData = new byte[22][4096];
		objectTextureData = new byte[1][4096];
		loadTextureSet(1, "object", objectTextureData);
		loadTextureSet(22, "texture", wallTextureData);
		gunTextureData = new byte[22500];
		loadTextureGun("gun", gunTextureData);
	}

	public void paintComponent(Graphics g)
	{
		if(threadRunning)
			g.drawImage(displayImage, 0, 0, this);
	}

	public void render()
	{
		furthestWall = 0.0D;
		copyOfPlayerDirection = playerDirection;
		rayDirection = copyOfPlayerDirection + 0.78539816339744828D;
		startRayXpos = playerXpos;
		startRayYpos = playerYpos;
		screenX = 0;
		firstRayXMove = Math.sin(6.2831853071795862D + rayDirection);
		firstRayYMove = Math.cos(6.2831853071795862D + rayDirection);
		lastRayXMove = Math.sin(rayDirection - 1.5707963267948966D);
		lastRayYMove = Math.cos(rayDirection - 1.5707963267948966D);
		for(; screenX < appletWidth; screenX++)
		{
			currentRayXpos = (int)startRayXpos;
			currentRayYpos = (int)startRayYpos;
			XHopSize = firstRayXMove + ((lastRayXMove - firstRayXMove) * (double)screenX) / (double)appletWidth;
			YHopSize = firstRayYMove + ((lastRayYMove - firstRayYMove) * (double)screenX) / (double)appletWidth;
			blockHit = -1;
			if(XHopSize > 0.0D)
			{
				if(YHopSize > 0.0D)
					downRight();
				else
					upRight();
			} else
			if(YHopSize > 0.0D)
				downLeft();
			else
				upLeft();
			double d = (double)currentRayXpos - startRayXpos;
			double d1 = (double)currentRayYpos - startRayYpos;
			zBuffer[screenX] = blockDistance = (int)(Math.sqrt(d * d + d1 * d1) / distanceAdjuster[screenX]);
			if((double)blockDistance > furthestWall)
				furthestWall = blockDistance;
			blockHitRecord[screenX] = blockHit;
			blockDistanceRecord[screenX] = blockDistance;
			if((blockHit & 1) == 0)
				textureXRecord[screenX] = (currentRayXpos & 0xffff) / 1024;
			else
				textureXRecord[screenX] = (currentRayYpos & 0xffff) / 1024;
		}

		drawFloor();
		for(screenX = 0; screenX < appletWidth; screenX++)
			renderBufferVLine(wallTextureData[blockHitRecord[screenX] / 2], screenX, (33280 * appletHeight) / blockDistanceRecord[screenX], textureXRecord[screenX], false, blockLightRecord[screenX]);

		for(counter = 0; counter < numObjects; counter++)
		{
			tz = (ax = objectXpos[counter] - startRayXpos) * Math.sin(copyOfPlayerDirection) + (az = objectZpos[counter] - startRayYpos) * Math.cos(copyOfPlayerDirection);
			if(tz > 0.0D && tz < furthestWall)
			{
				tx = ax * Math.cos(copyOfPlayerDirection) - az * Math.sin(copyOfPlayerDirection);
				sx = (int)((double)(appletWidth / 2) - (tx * (double)(appletWidth / 2)) / tz);
				drawObject(sx, (int)((double)(33280 * appletHeight) / tz), (int)tz, lightMap[(((int)objectZpos[counter] & 0xffff0000) >> 8) + ((int)objectXpos[counter] >> 16)]);
			}
		}

		int i = lightMap[(((int)playerYpos & 0xffff0000) >> 8) + ((int)playerXpos >> 16)] << 136;
		i = 65280;
		int j = appletWidth * (appletHeight - 150) + (appletWidth - 150) / 2;
		boolean flag = false;
		for(int k = 0; k < 150; k++)
		{
			for(int l = 0; l < 150; l++)
			{
				int i1 = gunTextureData[k * 150 + l];
				if(i1 != 0)
					renderBuffer[j + l] = (byte)i1;
			}

			j += appletWidth;
		}

	}

	public void renderBufferVLine(byte abyte0[], int i, int j, int k, boolean flag, int l)
	{
		int i2 = (l << 8) + 128;
		k *= 64;
		if(j > 0)
		{
			int k1 = 0x400000 / j;
			int i1;
			int j1;
			int l1;
			if(j > appletHeight)
			{
				j = appletHeight;
				l1 = 0x200000 - k1 * (j / 2);
				i1 = i;
				j1 = i + j * appletWidth;
			} else
			{
				l1 = 0;
				i1 = (appletHeight / 2 - j / 2) * appletWidth + i;
				j1 = i1 + j * appletWidth;
			}
			if(flag)
				while(i1 < j1) 
				{
					pixel = abyte0[k + (l1 >> 16)];
					if(pixel != 0)
						renderBuffer[i1] = shadeTable[pixel + i2];
					i1 += appletWidth;
					l1 += k1;
				}
			else
				while(i1 < j1) 
				{
					renderBuffer[i1] = shadeTable[abyte0[k + (l1 >> 16)] + i2];
					i1 += appletWidth;
					l1 += k1;
				}
		}
	}

	public void renderer()
	{
		long l = System.currentTimeMillis() + 1000L;
		while(threadRunning) 
		{
			long l1 = System.currentTimeMillis();
			if(l1 > l)
			{
				doomClient.inform((int)playerXpos, (int)playerYpos);
				doomClient.showPlayer();
				long l2 = System.currentTimeMillis() - l1;
				doomClient.playerTextArea.append("Frames per second: " + frames + "\n");
				doomClient.playerTextArea.append("Server delay: " + l2 + "ms");
				numObjects = 0;
				for(int i = 0; i < doomClient.playerTable.getPlayerCount(); i++)
				{
					Player player = doomClient.playerTable.getPlayer(i);
					
					if (!doomClient.name.equals(player.name))
					{
						objectZpos[numObjects] = player.ypos;
						objectXpos[numObjects] = player.xpos;
						numObjects++;
					}
				}

				frames = 0;
				l += 1000L;
			}
			render();
			pictureArray.newPixels();
			repaint();
			try
			{
				Thread.sleep(20L);
			}
			catch(InterruptedException _ex) { }
			if(turningLeft)
			{
				playerDirection += 0.1308996938995747D;
				if(playerDirection > 6.2831853071795862D)
					playerDirection -= 6.2831853071795862D;
			}
			if(turningRight)
			{
				playerDirection -= 0.1308996938995747D;
				if(playerDirection < 0.0D)
					playerDirection += 6.2831853071795862D;
			}
			if(rightStepping)
			{
				playerXpos += Math.sin(playerDirection - 1.5707963267948966D) * 10000D;
				playerYpos += Math.cos(playerDirection - 1.5707963267948966D) * 10000D;
			}
			if(leftStepping)
			{
				playerXpos += Math.sin(playerDirection + 1.5707963267948966D) * 10000D;
				playerYpos += Math.cos(playerDirection + 1.5707963267948966D) * 10000D;
			}
			if(runningForwards)
			{
				playerXpos += Math.sin(playerDirection) * 10000D;
				playerYpos += Math.cos(playerDirection) * 10000D;
			}
			if(runningBackwards)
			{
				playerXpos -= Math.sin(playerDirection) * 10000D;
				playerYpos -= Math.cos(playerDirection) * 10000D;
			}
			frames++;
		}
	}

	public void run()
	{
		renderer();
	}

	public void stopRunning()
	{
		threadRunning = false;
	}

	public void upLeft()
	{
		intersectionX = currentRayXpos & 0xffff0000;
		intersectionY = currentRayYpos & 0xffff0000;
		while(blockHit == -1) 
		{
			XHops = (double)(intersectionX - currentRayXpos) / XHopSize;
			YHops = (double)(intersectionY - currentRayYpos) / YHopSize;
			if(XHops < YHops)
			{
				currentRayXpos = intersectionX;
				currentRayYpos += XHops * YHopSize;
				if(map[((intersectionY >> 16) << 8) + ((intersectionX >> 16) - 1)] > -1)
				{
					blockHit = map[((intersectionY >> 16) << 8) + ((intersectionX >> 16) - 1)] * 2 + 1;
					blockLightRecord[screenX] = lightMap[((intersectionY & 0xffff0000) >> 8) + (intersectionX >> 16)];
				} else
				{
					intersectionX -= 0x10000;
				}
			} else
			{
				currentRayYpos = intersectionY;
				currentRayXpos += YHops * XHopSize;
				if(map[((intersectionY >> 16) - 1 << 8) + (intersectionX >> 16)] > -1)
				{
					blockHit = map[((intersectionY >> 16) - 1 << 8) + (intersectionX >> 16)] * 2;
					blockLightRecord[screenX] = lightMap[((intersectionY & 0xffff0000) >> 8) + (intersectionX >> 16)];
				} else
				{
					intersectionY -= 0x10000;
				}
			}
		}
	}

	public void upRight()
	{
		intersectionX = (currentRayXpos & 0xffff0000) + 0x10000;
		intersectionY = currentRayYpos & 0xffff0000;
		while(blockHit == -1) 
		{
			XHops = (double)(intersectionX - currentRayXpos) / XHopSize;
			YHops = (double)(intersectionY - currentRayYpos) / YHopSize;
			if(XHops < YHops)
			{
				currentRayXpos = intersectionX;
				currentRayYpos += XHops * YHopSize;
				if(map[((intersectionY >> 16) << 8) + (intersectionX >> 16)] > -1)
				{
					blockHit = map[((intersectionY >> 16) << 8) + (intersectionX >> 16)] * 2 + 1;
					blockLightRecord[screenX] = lightMap[((intersectionY & 0xffff0000) >> 8) + (intersectionX >> 16)];
				} else
				{
					intersectionX += 0x10000;
				}
			} else
			{
				currentRayYpos = intersectionY;
				currentRayXpos += YHops * XHopSize;
				if(map[((intersectionY >> 16) - 1 << 8) + ((intersectionX >> 16) - 1)] > -1)
				{
					blockHit = map[((intersectionY >> 16) - 1 << 8) + ((intersectionX >> 16) - 1)] * 2;
					blockLightRecord[screenX] = lightMap[((intersectionY & 0xffff0000) >> 8) + (intersectionX >> 16)];
				} else
				{
					intersectionY -= 0x10000;
				}
			}
		}
	}

	static final double fullCircle = 6.2831853071795862D;
	double multiplier;
	Graphics myGraphics;
	final int numWallTextures = 22;
	final int numObjectTextures = 1;
	int numObjects;
	double objectXpos[];
	double objectZpos[];
	Thread renderer;
	Thread initialiser;
	int counter;
	int nextPaletteEntry;
	byte paletteReds[];
	byte paletteGreens[];
	byte paletteBlues[];
	IndexColorModel myColorModel;
	byte wallTextureData[][];
	byte objectTextureData[][];
	byte gunTextureData[];
	int appletWidth;
	int appletHeight;
	int map[];
	int lightMap[];
	byte floorMap[];
	byte ceilingMap[];
	int intersectionX;
	int intersectionY;
	int screenX;
	double playerXpos;
	double playerYpos;
	double XHops;
	double YHops;
	double XHopSize;
	double YHopSize;
	int currentRayXpos;
	int currentRayYpos;
	double startRayXpos;
	double startRayYpos;
	int blockHit;
	int blockDistance;
	int blockLightRecord[];
	int blockHitRecord[];
	int blockDistanceRecord[];
	int textureXRecord[];
	double playerDirection;
	double copyOfPlayerDirection;
	double rayDirection;
	double rotationStep;
	double distanceAdjuster[];
	boolean runningForwards;
	boolean runningBackwards;
	boolean rightStepping;
	boolean leftStepping;
	boolean turningLeft;
	boolean turningRight;
	int frames;
	byte renderBuffer[];
	double firstRayXMove;
	double firstRayYMove;
	double lastRayXMove;
	double lastRayYMove;
	int zBuffer[];
	double ax;
	double az;
	double tx;
	double tz;
	int sx;
	int sy;
	int zoneLength;
	double furthestWall;
	int floorDestOffset;
	int ceilingDestOffset;
	int floorY;
	int floorX;
	double runStartX;
	double runEndX;
	double runStartY;
	double runEndY;
	double yAtMinX;
	double yAtMaxX;
	double xAtMinY;
	double xAtMaxY;
	int textureXStep;
	int textureYStep;
	int textureXpos;
	int textureYpos;
	byte shadeTable[];
	byte shadeTable2[];
	byte floorTexture[];
	byte ceilingTexture[];
	int pixel;
	int tileOffset;
	int oldTileOffset;
	int pixelOffset;
	boolean drawingFloor;
	int pixelsSkipped;
	Image displayImage;
	MemoryImageSource pictureArray;
	DoomClient doomClient;
	boolean threadRunning;
}