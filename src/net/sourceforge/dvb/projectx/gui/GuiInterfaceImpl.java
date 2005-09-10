/*
 * @(#)GuiInterfaceImpl
 *
 * Copyright (c) 2005 by dvb.matt, All Rights Reserved. 
 * 
 * This file is part of X, a free Java based demux utility.
 * X is intended for educational purposes only, as a non-commercial test project.
 * It may not be used otherwise. Most parts are only experimental.
 * 
 *
 * This program is free software; you can redistribute it free of charge
 * and/or modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package net.sourceforge.dvb.projectx.gui;

import net.sourceforge.dvb.projectx.gui.TeletextPageMatrix;
import net.sourceforge.dvb.projectx.gui.PreSettings;
import net.sourceforge.dvb.projectx.gui.ProcessWindow;
import net.sourceforge.dvb.projectx.gui.MainFrame;
import net.sourceforge.dvb.projectx.gui.CommonGui;
import net.sourceforge.dvb.projectx.gui.SubpictureFrame;
import net.sourceforge.dvb.projectx.gui.StartUp;

import net.sourceforge.dvb.projectx.common.GuiInterfaceIF;
import net.sourceforge.dvb.projectx.common.Common;


public class GuiInterfaceImpl implements GuiInterfaceIF {

	private TeletextPageMatrix teletextpagematrix;

	private PreSettings presettings;

	private ProcessWindow processwindow;

	private SubpictureFrame subpictureframe;

	private StartUp startup;

	/**
	 *
	 */
	public GuiInterfaceImpl()
	{
		init();
	}

	/**
	 *
	 */
	private void init()
	{
		new CommonGui();

		processwindow = new ProcessWindow();
	}

	/**
	 * load main stuff
	 */
	public void loadGui()
	{
		startup = new StartUp();
		startup.show();

		new MainFrame(startup);
	}

	/**
	 *
	 */
	private void loadTeletextPageMatrix()
	{
		if (teletextpagematrix == null)
			teletextpagematrix = new TeletextPageMatrix();
	}

	/**
	 *
	 */
	public void showTtxPageMatrix()
	{
		loadTeletextPageMatrix();
		teletextpagematrix.show();
	}

	/**
	 *
	 */
	public void initTtxPageMatrix(String str)
	{
		loadTeletextPageMatrix();
		teletextpagematrix.picture.init(str);
	}

	/**
	 *
	 */
	public void updateTtxPageMatrix(String str)
	{
		loadTeletextPageMatrix();
		teletextpagematrix.picture.update(str);
	}

	/**
	 *
	 */
	private void initPreSettings()
	{
		if (presettings == null)
			presettings = new PreSettings();
	}

	/**
	 *
	 */
	public void showPreSettings()
	{
		initPreSettings();

		if (presettings.isVisible())
			presettings.toFront();

		else
			presettings.show();
	}

	/**
	 *
	 */
	public void resetBitrateMonitor()
	{
		processwindow.resetBitrateMonitor();
	}

	/**
	 *
	 */
	public void updateBitrateMonitor(int value, byte[] array, String str)
	{
		processwindow.updateBitrateMonitor(value, array, str);
	}

	/**
	 *
	 */
	public void updateTtxHeader(String str)
	{
		processwindow.updateTtxHeader(str);
	}

	/**
	 *
	 */
	public void updateVpsLabel(String str)
	{
		processwindow.updateVpsLabel(str);
	}

	/**
	 *
	 */
	public void showAVOffset(String str)
	{
		processwindow.showAVOffset(str);
	}

	/**
	 *
	 */
	public void showExportStatus(String str)
	{
		processwindow.showExportStatus(str);
	}

	/**
	 *
	 */
	public void showExportStatus(String str, int value)
	{
		processwindow.showExportStatus(str, value);
	}

	/**
	 * progress
	 *
	 * @param1 - the msg
	 */
	public void updateProgressBar(int percent)
	{
		processwindow.updateProgressBar(percent);
	}

	/**
	 * progress
	 *
	 * @param1 - the msg
	 */
	public void updateProgressBar(String str)
	{
		processwindow.updateProgressBar(str);
	}

	/**
	 *
	 */
	public void setMessage(String msg, boolean tofront, int background)
	{
		processwindow.setMessage(msg, tofront, background);
	}

	/**
	 *
	 */
	public void addPidToExtract(Object obj)
	{
		processwindow.addPidToExtract(obj);
	}

	/**
	 *
	 */
	public void closeLogWindow()
	{
		processwindow.close();
	}

	/**
	 *
	 */
	public void showLogWindow()
	{
		if (processwindow.isVisible())
			processwindow.toFront();

		else
			processwindow.show();
	}

	/**
	 *
	 */
	public String getUserInputDialog(String arg1, String arg2)
	{
		return CommonGui.getUserInput(arg1, arg2);
	}

	/**
	 *
	 */
	public boolean getUserConfirmationDialog(String str)
	{
		return CommonGui.getUserConfirmation(str);
	}

	/**
	 *
	 */
	public void showErrorMessageDialog(Object message, String title)
	{
		CommonGui.showErrorMessageDialog(message, title);
	}

	/**
	 *
	 */
	public void showMessageDialog(Object message, String title)
	{
		CommonGui.showMessageDialog(message, title);
	}

	/**
	 * 
	 */
	public Object getMainFrameBounds()
	{
		return MainFrame.getFrameBounds();
	}

	/**
	 *
	 */
	public void minimizeMainFrame()
	{
		MainFrame.minimize();
	}

	/**
	 *
	 */
	public void setMainFrameTitle(String str)
	{
		MainFrame.setFrameTitle(str);
	}

	/**
	 *
	 */
	public void resetMainFrameTitle()
	{
		MainFrame.resetFrameTitle();
	}

	/**
	 *
	 */
	public void addCollectionAtEnd()
	{
		MainFrame.addCollectionAtEnd();
	}

	/**
	 *
	 */
	public void showActiveCollection(int index)
	{
		MainFrame.showActiveCollection(index);
	}

	/**
	 *
	 */
	public void updateCollectionPanel(int index)
	{
		MainFrame.updateCollectionPanel(index);
	}

	/**
	 *
	 */
	private void initSubpictureFrame()
	{
		if (subpictureframe == null)
			subpictureframe = new SubpictureFrame();
	}

	/**
	 *
	 */
	public void setSubpictureTitle(String str)
	{
		initSubpictureFrame();
		subpictureframe.setFrameTitle(str);
	}

	/**
	 *
	 */
	public void showSubpicture()
	{
		initSubpictureFrame();

		if (subpictureframe.isVisible())
			subpictureframe.toFront();

		else
			subpictureframe.show();
	}

	/**
	 *
	 */
	public void hideSubpicture()
	{
		initSubpictureFrame();

		subpictureframe.close();
	}

	/**
	 *
	 */
	public boolean isSubpictureVisible()
	{
		if (subpictureframe == null || !subpictureframe.isVisible())
			return false;

		return true;
	}

	/**
	 *
	 */
	public void repaintSubpicture()
	{
		initSubpictureFrame();

		subpictureframe.repaintSubpicture();
	}

	/**
	 *
	 */
	public void setOSDMessage(String str, boolean b)
	{
		CommonGui.getPicturePanel().setOSDMessage(str, b);
	}

	/**
	 *
	 */
	public void showCutIcon(boolean b, Object[] obj, Object list)
	{
		CommonGui.getPicturePanel().showCutIcon(b, obj, list);
	}

	/**
	 *
	 */
	public void updatePreviewPixel()
	{
		CommonGui.getPicturePanel().updatePreviewPixel();
	}

	/**
	 *
	 */
	public void repaintPicturePanel()
	{
		CommonGui.getPicturePanel().repaint();
	}
}

