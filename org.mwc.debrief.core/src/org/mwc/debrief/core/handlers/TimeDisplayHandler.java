package org.mwc.debrief.core.handlers;

import java.awt.Color;
import java.awt.Font;

import org.mwc.debrief.core.editors.painters.TimeDisplayPainter;

import MWC.GUI.Layer;
import MWC.GUI.Layers;
import MWC.Utilities.ReaderWriter.XML.LayerHandlerExtension;
import MWC.Utilities.ReaderWriter.XML.MWCXMLReader;
import MWC.Utilities.ReaderWriter.XML.Util.ColourHandler;
import MWC.Utilities.ReaderWriter.XML.Util.FontHandler;

public class TimeDisplayHandler extends MWCXMLReader implements LayerHandlerExtension
{
	private static final String COLOR = "Color";
	private static final String BACKGROUND_COLOR = "BackgroundColor";
	private static final String FORMAT_TIME = "FormatTime";
	private static final String PREFIX = "Prefix";
	private static final String SUFFIX = "Suffix";
	private static final String VISIBLE = "Visible";
	private static final String NAME = "Name";
	private static final String LOCATION = "Location";
	public static final String TYPE = "timeDisplay";
	protected boolean _visible;
	protected String _formatTime;
	protected String _name;
	protected String _suffix;
	protected String _prefix;
	protected Font _font;
	protected int _location;
	protected Color _color;
	protected Color _bgColor;
	private Layers _theLayers;

	public TimeDisplayHandler()
	{
		super(TYPE);
		addAttributeHandler(new HandleBooleanAttribute(VISIBLE)
		{
			public void setValue(final String name, final boolean value)
			{
				_visible = value;
			}
		});
		addAttributeHandler(new HandleAttribute(NAME)
		{
			public void setValue(final String name, final String value)
			{
				_name = value;
			}
		});
		addAttributeHandler(new HandleAttribute(FORMAT_TIME)
		{
			public void setValue(final String name, final String val)
			{
				_formatTime = val;
			}
		});
		addAttributeHandler(new HandleAttribute(PREFIX)
		{
			public void setValue(final String name, final String val)
			{
				_prefix = val;
			}
		});
		addAttributeHandler(new HandleAttribute(SUFFIX)
		{
			public void setValue(final String name, final String val)
			{
				_suffix = val;
			}
		});
		addAttributeHandler(new HandleIntegerAttribute(LOCATION)
		{
			public void setValue(final String name, final int val)
			{
				_location = val;
			}
		});
		addHandler(new FontHandler()
    {
      public void setFont(final java.awt.Font font)
      {
        _font = font;
      }
    });
		addHandler(new ColourHandler(COLOR)
    {
			public void setColour(Color res)
			{
				_color = res;
			}
    });
		addHandler(new ColourHandler(BACKGROUND_COLOR)
    {
			public void setColour(Color res)
			{
				_bgColor = res;
			}
    });
    
	}
	
	@Override
	public final void elementClosed()
	{
			// set our specific attributes
			final TimeDisplayPainter wrapper = new TimeDisplayPainter();
			wrapper.setVisible(_visible);
			wrapper.setName(_name);
			wrapper.setFormat(_formatTime);
			wrapper.setPrefix(_prefix);
			wrapper.setSuffix(_suffix);
			if (_font != null)
			{
				wrapper.setFont(_font);
			}
			if (_color != null)
			{
				wrapper.setColor(_color);
			}
			if (_bgColor != null)
			{
				wrapper.setBackground(_bgColor);
			}
			wrapper.setLocation(_location);
			_theLayers.addThisLayer(wrapper);
	}
		
	
	@Override
	public boolean canExportThis(Layer subject)
	{
		return subject instanceof TimeDisplayPainter;
	}

	@Override
	public void exportThis(Layer theLayer, org.w3c.dom.Element parent,
			org.w3c.dom.Document doc)
	{
		TimeDisplayPainter tdp = (TimeDisplayPainter) theLayer;
		if (tdp == null)
		{
			return;
		}
		final org.w3c.dom.Element timeDisplay = doc.createElement(TYPE);

		timeDisplay.setAttribute(NAME, tdp.getName());
		timeDisplay.setAttribute(VISIBLE, writeThis(tdp.getVisible()));
		timeDisplay.setAttribute(LOCATION, writeThis(tdp.getLocation()));
		timeDisplay.setAttribute(FORMAT_TIME, tdp.getFormat());
		timeDisplay.setAttribute(PREFIX, tdp.getPrefix());
		timeDisplay.setAttribute(SUFFIX, tdp.getSuffix());
		FontHandler.exportFont(tdp.getFont(), timeDisplay, doc);
    ColourHandler.exportColour(tdp.getColor(), timeDisplay, doc, COLOR);
    ColourHandler.exportColour(tdp.getBackground(), timeDisplay, doc, BACKGROUND_COLOR);
    
		parent.appendChild(timeDisplay);

	}

	@Override
	public void setLayers(Layers theLayers)
	{
		this._theLayers = theLayers;
	}

}
