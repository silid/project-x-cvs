/*
 * @(#)HEXVIEWER.java - simple hexviewer
 *
 * Copyright (c) 2002-2005 by dvb.matt, All Rights Reserved. 
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.UIManager;
import javax.swing.KeyStroke;
import javax.swing.BoxLayout;

import net.sourceforge.dvb.projectx.common.Resource;
import net.sourceforge.dvb.projectx.common.Common;

import net.sourceforge.dvb.projectx.xinput.XInputFile;

import net.sourceforge.dvb.projectx.gui.CommonGui;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;

public class HexViewer extends JFrame {

	private XInputFile xinputFile;
	private JTextArea HexArea;
	private JTextField Field, Field1, from, fsize;
	private JScrollPane scroll;
	private JViewport viewport;
	private JLabel flen, hexn;
	private JSlider slider;
	private JFileChooser chooser;

	private boolean textonly = false;

	/**
	 *
	 */
	public HexViewer()
	{
		init();
	}

	/**
	 *
	 */
	protected void init()
	{
		addWindowListener (new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				close(); 
			}
		});

		buildMenu();

		setTitle(Resource.getString("hexviewer.title"));

		chooser = new JFileChooser();
		scroll = new JScrollPane();
		HexArea = new JTextArea();
		HexArea.setFont(new Font("Courier New", Font.PLAIN, 12));
		HexArea.setEditable(true);
		HexArea.setRows(24);
		HexArea.setTabSize(12);

		scroll.setViewportView(HexArea);
		viewport = scroll.getViewport();

		slider = new JSlider(JSlider.VERTICAL, 0, 15, 0);
		slider.setInverted(true);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e)
			{
				readfile((16L * slider.getValue()));
			}
		});

		Field = new JTextField("0");
		Field.setToolTipText(Resource.getString("hexviewer.jumpto_tip"));
		Field.setPreferredSize(new Dimension(100,25));
		Field.setMaximumSize(new Dimension(100,25));
		Field.setEditable(true);

		hexn = new JLabel("= hex: ");
		hexn.setPreferredSize(new Dimension(120,25));
		hexn.setMaximumSize(new Dimension(120,25));

		Field1 = new JTextField("0");
		Field1.setToolTipText(Resource.getString("hexviewer.jumpto_tip"));
		Field1.setPreferredSize(new Dimension(100,25));
		Field1.setMaximumSize(new Dimension(100,25));
		Field1.setEditable(true);

		JPanel container = new JPanel();
		container.setLayout( new BorderLayout() );

		JPanel menu = new JPanel();
		menu.setLayout( new BoxLayout(menu,BoxLayout.X_AXIS ));
		menu.setToolTipText(Resource.getString("hexviewer.jumpto_tip"));
		menu.add(new JLabel(Resource.getString("hexviewer.jumptodec")));
		menu.add(Field);
		menu.add(hexn);
		menu.add(new JLabel(Resource.getString("hexviewer.jumptohex")));
		menu.add(Field1);

		flen = new JLabel(Resource.getString("hexviewer.filesize"));
		menu.add(flen);

		Field.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try 
				{
					if (!Field.getText().equals(""))
					{
						hexn.setText("= hex: "+Long.toHexString(Long.parseLong(Field.getText())).toUpperCase());
						slider.setValue((int)(Long.parseLong(Field.getText()) / 16));
					}
				} 
				catch (Exception e1)
				{}
			}
		});

		Field1.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try 
				{
					if (!Field1.getText().equals(""))
					{
						slider.setValue((int)(Long.parseLong(Field1.getText(), 16) / 16));
					}
				} 
				catch (Exception e1)
				{}
			}
		});


		JPanel grid = new JPanel();
		grid.setLayout( new GridLayout(1,1) );
		grid.add(scroll);

		JPanel menu2 = new JPanel();
		menu2.setLayout( new BoxLayout(menu2,BoxLayout.X_AXIS ));
		menu2.setToolTipText(Resource.getString("hexviewer.extract_tip"));

		from = new JTextField("0");
		from.setPreferredSize(new Dimension(100,25));
		from.setMaximumSize(new Dimension(100,25));
		from.setEditable(true);

		fsize = new JTextField("1000");
		fsize.setPreferredSize(new Dimension(100,25));
		fsize.setMaximumSize(new Dimension(100,25));
		fsize.setEditable(true);

		JButton extract = new JButton(Resource.getString("hexviewer.extractfrom"));
		extract.setToolTipText(Resource.getString("hexviewer.extractfrom_tip"));

		extract.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try 
				{
					if (!from.getText().equals("") && !fsize.getText().equals(""))
					{
						long pos = Long.parseLong(from.getText(),16);
						long size = Long.parseLong(fsize.getText(),16) - pos;
						savefile(pos,size);
					} 
				} 
				catch (Exception e1)
				{}
			}
		});

		menu2.add(extract);
		menu2.add(from);
		menu2.add(new JLabel(Resource.getString("hexviewer.to") + ": (hex.)"));
		menu2.add(fsize);

		JPanel menu3 = new JPanel();
		menu3.setLayout( new GridLayout(2,1));
		menu3.add(menu);
		menu3.add(menu2);

		container.add(slider, BorderLayout.EAST);
		container.add(grid);
		container.add(menu3, BorderLayout.SOUTH);

		getContentPane().add(container);
		centerDialog();

		UIManager.addPropertyChangeListener(new UISwitchListener(getRootPane()));
	}

	/**
	 *
	 */
	protected void buildMenu()
	{
		JMenuBar menuBar = new JMenuBar();

		menuBar.add(buildFileMenu());
		menuBar.add(buildOptionMenu());

		setJMenuBar(menuBar);
	}

	/**
	 *
	 */
	protected JMenu buildFileMenu()
	{
		JMenu fileMenu = new JMenu();
		CommonGui.localize(fileMenu, "Common.File");

		JMenuItem save = new JMenuItem();
		CommonGui.localize(save, "Common.SaveAs");

		fileMenu.add(save);
		fileMenu.addSeparator();

		JMenuItem close = new JMenuItem();
		CommonGui.localize(close, "Common.Close");
		close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.ALT_MASK));
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				close();
			}
		});

		fileMenu.add(close);

		return fileMenu;
	}

	/**
	 *
	 */
	protected JMenu buildOptionMenu()
	{
		JMenu optionMenu = new JMenu();
		CommonGui.localize(optionMenu, "Common.Options");

		JCheckBoxMenuItem text_mode = new JCheckBoxMenuItem(Resource.getString("hexviewer.textmode"));
		text_mode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				textonly = ((JCheckBoxMenuItem) e.getSource()).getState();
			}
		});

		optionMenu.add(text_mode);

		return optionMenu;
	}

	/**
	 *
	 */
	private void savefile(long startPos, long size)
	{
		long len = xinputFile.length();
		size = (startPos + size > len) ? (len - startPos) : size;

		if (startPos >= len || startPos<0 || size < 1) 
			return;

		String newfile = xinputFile + "(0x" + Long.toHexString(startPos) + " to 0x" + Long.toHexString(startPos + size) + ").bin";
		chooser.setSelectedFile(new File(newfile));
		chooser.rescanCurrentDirectory();

		int retval = chooser.showSaveDialog(this);
		if(retval == JFileChooser.APPROVE_OPTION)
		{
			File theFile = chooser.getSelectedFile();

			if(theFile != null && !theFile.isDirectory())
			{
				newfile = theFile.getAbsolutePath();
			}
		}
		else 
			return;

		setTitle(Resource.getString("hexviewer.save") + ": " + newfile); 

		try 
		{
			int buf = 3072000;
			BufferedInputStream hex = new BufferedInputStream(xinputFile.getInputStream(), buf);
			BufferedOutputStream hex1 = new BufferedOutputStream(new FileOutputStream(newfile), buf);
			long filePos = 0;
			long endPos = startPos + size;

			while (filePos < startPos)
				filePos += hex.skip(startPos - filePos);

			byte data[];
			int datalen;

			while (filePos < endPos)
			{
				datalen = (endPos-filePos) < (long)buf ? (int)(endPos-filePos) : buf;
				data = new byte[datalen];
				datalen = hex.read(data);
				hex1.write(data,0,datalen);
				filePos += datalen;
			}

			hex.close();
			hex1.flush();
			hex1.close();

		}
		catch (IOException e)
		{ 
			HexArea.setText(Resource.getString("hexviewer.error") + ": " + xinputFile); 
		}

		setTitle(Resource.getString("hexviewer.file") + ": " + xinputFile);
	}

	/**
	 *
	 */
	private void readfile(long position)
	{
		try 
		{
			xinputFile.randomAccessOpen("r");

			long len = xinputFile.length();

			if (position < len)
			{
				if (textonly)
				{
					xinputFile.randomAccessSeek(position);

					String text = "";
					String nextLine = null;

					if (position != 0) 
						xinputFile.randomAccessReadLine();

					for (int a = 0; a < 24 && (nextLine = xinputFile.randomAccessReadLine()) != null; a++) 
						text += nextLine + "\n";

					HexArea.setText(text);
				}
				else
				{
					int viewsize = (int)(((len - position) >= 384L) ? 384 : (len - position) );
					byte[] data = new byte[viewsize];
					xinputFile.randomAccessSeek(position);
					xinputFile.randomAccessRead(data);

					print(data, position);
				}
			}
			xinputFile.randomAccessClose();

		} 
		catch (IOException e)
		{ 
			HexArea.setText(Resource.getString("hexviewer.error") + ": " + xinputFile); 
		}
	}

	/**
	 *
	 */
	private void print(byte[] data, long position)
	{
		String fill = "0000000000";
		String text = "";

		for (int a = 0; a < data.length; a += 16)
		{
			String ascii = " : ";
			String stuff = "   ";
			String pos = Long.toHexString(position + a).toUpperCase();
			text += fill.substring(0, 10 - pos.length()) + pos + " : ";
			int b=0;

			for (; b < 16 && a + b < data.length; b++)
			{ 
				String val = Integer.toHexString((0xFF & data[a + b])).toUpperCase();
				text += fill.substring(0, 2 - val.length()) + val + ((b == 7) ? "-" : " ");
				ascii += ((0xFF & data[a + b]) > 0x1F && (0xFF & data[a + b]) < 0x7F) ? "" + ((char)data[a + b]) : ".";
			}

			for (; b < 16; b++) 
				text += stuff;

			text += ascii + "\n";
		}

		HexArea.setText(text);
	}

	/**
	 *
	 */
	public void view(XInputFile aXInputFile)
	{
		long filelen = aXInputFile.length();

		if ((xinputFile == null) || !(xinputFile.equals(aXInputFile)))
		{
			xinputFile = aXInputFile.getNewInstance();

			HexArea.setText("");
			slider.setMaximum((int)(filelen / 16));

			if (slider.getValue() == 0) 
				readfile(0);

			else 
				slider.setValue(0);
		}

		setTitle(Resource.getString("hexviewer.file") + ": " + xinputFile);
		flen.setText(Resource.getString("hexviewer.filesize") + ": " + filelen + " b.");

		show();
	}

	/**
	 *
	 */
	protected void centerDialog()
	{
		setLocation(200, 200);
		setSize(620, 460);
	}

	/**
	 *
	 */
	private void close()
	{
		dispose();
	}


}
