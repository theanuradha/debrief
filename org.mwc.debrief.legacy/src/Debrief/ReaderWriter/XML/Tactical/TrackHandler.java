/*
 *    Debrief - the Open Source Maritime Analysis Application
 *    http://debrief.info
 *
 *    (C) 2000-2014, PlanetMayo Ltd
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the Eclipse Public License v1.0
 *    (http://www.eclipse.org/legal/epl-v10.html)
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 */
package Debrief.ReaderWriter.XML.Tactical;

/**
 * Title:        Debrief 2000
 * Description:  Debrief 2000 Track Analysis Software
 * Copyright:    Copyright (c) 2000
 * Company:      MWC
 * @author Ian Mayo
 * @version 1.0
 */

import java.awt.Color;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

import org.w3c.dom.Element;
import org.xml.sax.Attributes;

import Debrief.Wrappers.FixWrapper;
import Debrief.Wrappers.SensorWrapper;
import Debrief.Wrappers.TMAWrapper;
import Debrief.Wrappers.TrackWrapper;
import Debrief.Wrappers.DynamicTrackShapes.DynamicTrackShapeSetWrapper;
import Debrief.Wrappers.Track.AbsoluteTMASegment;
import Debrief.Wrappers.Track.DynamicInfillSegment;
import Debrief.Wrappers.Track.ICompositeTrackSegment;
import Debrief.Wrappers.Track.RelativeTMASegment;
import Debrief.Wrappers.Track.TrackColorModeHelper;
import Debrief.Wrappers.Track.TrackColorModeHelper.LegacyTrackColorModes;
import Debrief.Wrappers.Track.TrackColorModeHelper.TrackColorMode;
import Debrief.Wrappers.Track.TrackSegment;
import Debrief.Wrappers.Track.TrackWrapper_Support.SegmentList;
import MWC.GUI.Editable;
import MWC.GUI.Plottable;
import MWC.GenericData.Duration;
import MWC.GenericData.WorldDistance;
import MWC.Utilities.ReaderWriter.XML.Util.ColourHandler;
import MWC.Utilities.ReaderWriter.XML.Util.DurationHandler;
import MWC.Utilities.ReaderWriter.XML.Util.FontHandler;
import MWC.Utilities.ReaderWriter.XML.Util.WorldDistanceHandler;

public class TrackHandler extends MWC.Utilities.ReaderWriter.XML.MWCXMLReader
{

  private static final String TRACK = "track";
  private static final String SYMBOL_COLOR = "SymbolColor";
  private static final String SYMBOL_WIDTH = "SYMBOL_WIDTH";
  private static final String SYMBOL_LENGTH = "SYMBOL_LENGTH";
  private static final String SOLUTIONS_VISIBLE = "SolutionsVisible";
  private static final String SENSORS_VISIBLE = "SensorsVisible";
  private static final String PLOT_ARRAY_CENTRE = "PlotArrayCentre";
  private static final String LINK_POSITIONS = "LinkPositions";
  private static final String LINE_THICKNESS = "LineThickness";
  private static final String LINE_STYLE = "LineStyle";
  private static final String INTERPOLATE_POINTS = "InterpolatePoints";
  private static final String END_TIME_LABELS = "EndTimeLabels";
  private static final String CUSTOM_TRAIL_LENGTH = "CustomTrailLength";
  private static final String CUSTOM_VECTOR_STRETCH = "CustomVectorStretch";
  private static final String COLOR_MODE = "ColorMode";

  final MWC.GUI.Layers _theLayers;

  // private MWC.GUI.Layer _myLayer;

  // our "working" track
  Debrief.Wrappers.TrackWrapper _myTrack;
  protected WorldDistance _symWidth;
  protected WorldDistance _symLength;
  protected Color _symCol;

  /**
   * class which contains list of textual representations of label locations
   */
  static final MWC.GUI.Properties.LocationPropertyEditor lp =
      new MWC.GUI.Properties.LocationPropertyEditor();

  public static void exportTrack(final Debrief.Wrappers.TrackWrapper track,
      final org.w3c.dom.Element parent, final org.w3c.dom.Document doc)
  {
    final Element trk = doc.createElement(TRACK);
    parent.appendChild(trk);
    exportTrackObject(track, trk, doc);
  }

  protected static void exportTrackObject(
      final Debrief.Wrappers.TrackWrapper track, final org.w3c.dom.Element trk,
      final org.w3c.dom.Document doc)
  {

    /*
     * <!ELEMENT track (colour,((fix|contact)*))> <!ATTLIST track name CDATA #REQUIRED visible
     * (TRUE|FALSE) "TRUE" PositionsLinked (TRUE|FALSE) "TRUE" NameVisible (TRUE|FALSE) "TRUE"
     * PositionsVisible (TRUE|FALSE) "TRUE" NameAtStart (TRUE|FALSE) "TRUE" NameLocation
     * (Top|Left|Bottom|Centre|Right) "Right" Symbol CDATA "SQUARE" >
     */

    trk.setAttribute("Name", toXML(track.getName()));
    trk.setAttribute("Visible", writeThis(track.getVisible()));
    trk.setAttribute("PositionsVisible", writeThis(track.getPositionsVisible()));
    trk.setAttribute("NameVisible", writeThis(track.getNameVisible()));
    trk.setAttribute("NameAtStart", writeThis(track.getNameAtStart()));
    trk.setAttribute(LINE_THICKNESS, writeThis(track.getLineThickness()));
    trk.setAttribute(LINE_STYLE, writeThis(track.getLineStyle()));
    trk.setAttribute(PLOT_ARRAY_CENTRE, writeThis(track.getPlotArrayCentre()));
    trk.setAttribute(INTERPOLATE_POINTS,
        writeThis(track.getInterpolatePoints()));
    trk.setAttribute(LINK_POSITIONS, writeThis(track.getLinkPositions()));
    lp.setValue(track.getNameLocation());
    trk.setAttribute("NameLocation", lp.getAsText());
    trk.setAttribute("Symbol", track.getSymbolType());
    trk.setAttribute(END_TIME_LABELS, writeThis(track.getEndTimeLabels()));

    // whether the sensor/solution layers should be visible
    trk.setAttribute(SENSORS_VISIBLE,
        writeThis(track.getSensors().getVisible()));
    trk.setAttribute(SOLUTIONS_VISIBLE, writeThis(track.getSolutions()
        .getVisible()));

    // do we have a vector stretch?
    if (track.getCustomVectorStretch() != 0)
    {
      trk.setAttribute(CUSTOM_VECTOR_STRETCH, writeThis(track
          .getCustomVectorStretch()));
    }

    ColourHandler.exportColour(track.getColor(), trk, doc);
    ColourHandler.exportColour(track.getSymbolColor(), trk, doc, SYMBOL_COLOR);

    // and the worm in the hole indicator?
    TrackColorMode mode = track.getTrackColorMode();
    trk.setAttribute(COLOR_MODE, mode.asString());

    // and the font
    final java.awt.Font theFont = track.getTrackFont();
    if (theFont != null)
    {
      FontHandler.exportFont(theFont, trk, doc);
    }

    // do we have a custom trail length?
    final Duration trailLen = track.getCustomTrailLength();
    if (trailLen != null)
    {
      DurationHandler.exportDuration(CUSTOM_TRAIL_LENGTH, trailLen, trk, doc);
    }

    // and the symbol
    if (track.getSymbolLength() != null)
      WorldDistanceHandler.exportDistance(SYMBOL_LENGTH, track
          .getSymbolLength(), trk, doc);
    if (track.getSymbolWidth() != null)
      WorldDistanceHandler.exportDistance(SYMBOL_WIDTH, track.getSymbolWidth(),
          trk, doc);

    // first output any sensor data
    final Enumeration<Editable> sensors = track.getSensors().elements();

    // check if there is any data!
    if (sensors != null)
    {
      while (sensors.hasMoreElements())
      {
        final Debrief.Wrappers.SensorWrapper thisS =
            (SensorWrapper) sensors.nextElement();
        SensorHandler.exportSensor(thisS, trk, doc);
      }
    }

    // first output any sensor data
    final Enumeration<Editable> solutions = track.getSolutions().elements();

    // check if there is any data!
    if (solutions != null)
    {
      while (solutions.hasMoreElements())
      {
        final Debrief.Wrappers.TMAWrapper thisS =
            (TMAWrapper) solutions.nextElement();
        TMAHandler.exportSolutionTrack(thisS, trk, doc);
      }
    }

    // output any sensor arc data
    final Enumeration<Editable> shapeSet = track.getDynamicShapes().elements();

    if (shapeSet != null)
    {
      while (shapeSet.hasMoreElements())
      {
        final DynamicTrackShapeSetWrapper thisS =
            (DynamicTrackShapeSetWrapper) shapeSet.nextElement();
        DynamicTrackShapeSetHandler.exportShapeSet(thisS, trk, doc);
      }
    }

    final Enumeration<Editable> allItems = track.elements();
    while (allItems.hasMoreElements())
    {
      final Editable next = allItems.nextElement();
      if (next instanceof SegmentList)
      {
        final SegmentList list = (SegmentList) next;
        final Element sList =
            doc.createElement(SegmentListHandler.SEGMENT_LIST);
        final Collection<Editable> items = list.getData();
        for (final Iterator<Editable> iterator = items.iterator(); iterator
            .hasNext();)
        {
          final TrackSegment editable = (TrackSegment) iterator.next();
          exportThisTrackSegment(doc, trk, editable);
        }
        trk.appendChild(sList);
      }
      else if (next instanceof TrackSegment
          && !(next instanceof ICompositeTrackSegment))
      {
        exportThisTrackSegment(doc, trk, (TrackSegment) next);
      }
    }
  }

  private static void exportThisTrackSegment(final org.w3c.dom.Document doc,
      final Element trk, final TrackSegment segment)
  {
    // right, sort out what type it is
    if (segment instanceof RelativeTMASegment)
    {
      RelativeTMASegmentHandler.exportThisTMASegment(doc, trk,
          (RelativeTMASegment) segment);
    }
    else if (segment instanceof AbsoluteTMASegment)
    {
      AbsoluteTMASegmentHandler.exportThisTMASegment(doc, trk,
          (AbsoluteTMASegment) segment);
    }
    else if (segment instanceof DynamicInfillSegment)
    {
      DynamicInfillSegmentHandler.exportThisSegment(doc, trk,
          (DynamicInfillSegment) segment);
    }
    else

      TrackSegmentHandler.exportThisSegment(doc, trk, (TrackSegment) segment);
  }

  public TrackHandler(final MWC.GUI.Layers theLayers)
  {
    this(theLayers, TRACK);
  }

  protected TrackHandler(final MWC.GUI.Layers theLayers, final String name)
  {
    // inform our parent what type of class we are
    super(name);

    // store the layers object, so that we can add ourselves to it
    _theLayers = theLayers;

    addHandler(new SensorHandler()
    {
      @Override
      public void addSensor(final Debrief.Wrappers.SensorWrapper sensor)
      {
        addThis(sensor);
      }
    });

    addHandler(new DynamicTrackShapeSetHandler()
    {
      @Override
      public void addDynamicTrackShapes(DynamicTrackShapeSetWrapper data)
      {
        addThis(data);
      }
    });

    addHandler(new SegmentListHandler(_theLayers)
    {
      @Override
      public void addThisSegment(final TrackSegment list)
      {
        addThis(list);
      }
    });

    addHandler(new RelativeTMASegmentHandler(_theLayers)
    {
      public void addSegment(final TrackSegment segment)
      {
        addThis(segment);
      }
    });

    addHandler(new PlanningSegmentHandler()
    {
      public void addSegment(final TrackSegment segment)
      {
        addThis(segment);
      }
    });
    addHandler(new PlanningSegmentHandler(
        PlanningSegmentHandler.CLOSING_SEGMENT)
    {
      public void addSegment(final TrackSegment segment)
      {
        // store the parent
        addThis(segment);
      }
    });

    addHandler(new AbsoluteTMASegmentHandler()
    {
      public void addSegment(final TrackSegment segment)
      {
        addThis(segment);
      }
    });

    addHandler(new DynamicInfillSegmentHandler()
    {
      public void addSegment(final TrackSegment segment)
      {
        addThis(segment);
      }
    });

    addHandler(new TrackSegmentHandler()
    {
      @Override
      public void addSegment(final TrackSegment list)
      {
        addThis(list);
      }
    });

    addHandler(new TMAHandler()
    {
      @Override
      public void addContact(final TMAWrapper data)
      {
        addThis(data);
      }
    });

    addHandler(new ColourHandler()
    {
      @Override
      public void setColour(final java.awt.Color res)
      {
        // nope, give it the track color
        _myTrack.setColor(res);
        // has the symbol been set?
        if (_symCol == null)
        {
          _myTrack.setSymbolColor(res);
        }
      }
    });

    addHandler(new DurationHandler(CUSTOM_TRAIL_LENGTH)
    {
      @Override
      public void setDuration(Duration res)
      {
        _myTrack.setCustomTrailLength(res);
      }
    });

    addHandler(new ColourHandler(SYMBOL_COLOR)
    {
      @Override
      public void setColour(final java.awt.Color res)
      {
        _symCol = res;
        _myTrack.setSymbolColor(res);
      }
    });

    addHandler(new FontHandler()
    {
      @Override
      public void setFont(final java.awt.Font font)
      {
        _myTrack.setTrackFont(font);
      }
    });

    addHandler(new FixHandler()
    {
      @Override
      public void addPlottable(final MWC.GUI.Plottable fix)
      {
        addFix((FixWrapper) fix);
      }
    });
    addAttributeHandler(new HandleAttribute("Name")
    {
      @Override
      public void setValue(final String name, final String val)
      {
        _myTrack.setName(fromXML(val));
      }
    });
    addAttributeHandler(new HandleBooleanAttribute("Visible")
    {
      @Override
      public void setValue(final String name, final boolean val)
      {
        _myTrack.setVisible(val);
      }
    });
    addAttributeHandler(new HandleBooleanAttribute(SENSORS_VISIBLE)
    {
      @Override
      public void setValue(final String name, final boolean val)
      {
        _myTrack.getSensors().setVisible(val);
      }
    });
    addAttributeHandler(new HandleBooleanAttribute(END_TIME_LABELS)
    {
      @Override
      public void setValue(final String name, final boolean val)
      {
        _myTrack.setEndTimeLabels(val);
      }
    });
    addAttributeHandler(new HandleBooleanAttribute(SOLUTIONS_VISIBLE)
    {
      @Override
      public void setValue(final String name, final boolean val)
      {
        _myTrack.getSolutions().setVisible(val);
      }
    });
    addAttributeHandler(new HandleBooleanAttribute("PositionsVisible")
    {
      @Override
      public void setValue(final String name, final boolean val)
      {
        _myTrack.setPositionsVisible(val);
      }
    });
    addAttributeHandler(new HandleBooleanAttribute(LINK_POSITIONS)
    {
      @Override
      public void setValue(final String name, final boolean val)
      {
        _myTrack.setLinkPositions(val);
      }
    });
    addAttributeHandler(new HandleBooleanAttribute(PLOT_ARRAY_CENTRE)
    {
      @Override
      public void setValue(final String name, final boolean val)
      {
        _myTrack.setPlotArrayCentre(val);
      }
    });
    addAttributeHandler(new HandleBooleanAttribute("NameVisible")
    {
      @Override
      public void setValue(final String name, final boolean val)
      {
        _myTrack.setNameVisible(val);
      }
    });
    addAttributeHandler(new HandleBooleanAttribute("NameAtStart")
    {
      @Override
      public void setValue(final String name, final boolean val)
      {
        _myTrack.setNameAtStart(val);
      }
    });
    addAttributeHandler(new HandleBooleanAttribute(INTERPOLATE_POINTS)
    {
      @Override
      public void setValue(final String name, final boolean val)
      {
        _myTrack.setInterpolatePoints(val);
      }
    });
    addAttributeHandler(new HandleAttribute("NameLocation")
    {
      @Override
      public void setValue(final String name, final String val)
      {
        lp.setAsText(val);
        _myTrack.setNameLocation((Integer) lp.getValue());
      }
    });
    addAttributeHandler(new HandleAttribute("Symbol")
    {
      @Override
      public void setValue(final String name, final String value)
      {
        _myTrack.setSymbolType(value);
      }
    });
    addAttributeHandler(new HandleIntegerAttribute(LINE_STYLE)
    {
      @Override
      public void setValue(final String name, final int value)
      {
        _myTrack.setLineStyle(value);
      }
    });
    addAttributeHandler(new HandleDoubleAttribute(CUSTOM_VECTOR_STRETCH)
    {
      @Override
      public void setValue(final String name, final double value)
      {
        _myTrack.setCustomVectorStretch(value);
      }
    });

    addAttributeHandler(new HandleIntegerAttribute(LINE_THICKNESS)
    {
      @Override
      public void setValue(final String name, final int value)
      {
        _myTrack.setLineThickness(value);
      }
    });
    addAttributeHandler(new HandleAttribute(COLOR_MODE)
    {
      @Override
      public void setValue(final String name, final String value)
      {
        final TrackColorMode mode;
        // ok, handle the string
        if (LegacyTrackColorModes.PER_FIX.asString().equals(value))
        {
          mode = LegacyTrackColorModes.PER_FIX;
        }
        else if (LegacyTrackColorModes.OVERRIDE.asString().equals(value))
        {
          mode = LegacyTrackColorModes.OVERRIDE;
        }
        else
        {
          // ok, it must be a datset name
          mode = new TrackColorModeHelper.DeferredDatasetColorMode(value);
        }
        _myTrack.setTrackColorMode(mode);
      }
    });

    addHandler(new WorldDistanceHandler(SYMBOL_WIDTH)
    {
      @Override
      public void setWorldDistance(final WorldDistance res)
      {
        _symWidth = res;
      }
    });

    addHandler(new WorldDistanceHandler(SYMBOL_LENGTH)
    {
      @Override
      public void setWorldDistance(final WorldDistance res)
      {
        _symLength = res;
      }
    });

  }

  void addThis(final Plottable val)
  {
    _myTrack.add(val);
  }

  void addFix(final FixWrapper val)
  {
    _myTrack.add(val);
  }

  @Override
  public void elementClosed()
  {
    // ok, the symbol should be sorted, do the lengths
    if (_symLength != null)
      _myTrack.setSymbolLength(_symLength);
    if (_symWidth != null)
      _myTrack.setSymbolWidth(_symWidth);

    // our layer is complete, add it to the parent!
    _theLayers.addThisLayer(_myTrack);

    _myTrack = null;
    _symWidth = null;
    _symLength = null;
    _symCol = null;
  }

  // this is one of ours, so get on with it!
  @Override
  protected final void handleOurselves(final String name,
      final Attributes attributes)
  {
    // create the wrapper
    _myTrack = getWrapper();

    // marry them together
    super.handleOurselves(name, attributes);
  }

  protected TrackWrapper getWrapper()
  {
    return new TrackWrapper();
  }

}