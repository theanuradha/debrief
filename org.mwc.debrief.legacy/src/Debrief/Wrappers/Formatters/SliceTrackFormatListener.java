package Debrief.Wrappers.Formatters;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.List;

import junit.framework.TestCase;
import Debrief.Wrappers.FixWrapper;
import Debrief.Wrappers.TrackWrapper;
import MWC.GUI.CanvasType;
import MWC.GUI.Editable;
import MWC.GUI.Layer;
import MWC.GUI.Layers.INewItemListener;
import MWC.GUI.PlainWrapper;
import MWC.GenericData.HiResDate;
import MWC.GenericData.WorldArea;
import MWC.GenericData.WorldLocation;
import MWC.TacticalData.Fix;

public class SliceTrackFormatListener extends PlainWrapper implements
    INewItemListener
{

  // ///////////////////////////////////////////////////////////
  // info class
  // //////////////////////////////////////////////////////////
  final public class TrackNameInfo extends Editable.EditorType implements
      Serializable
  {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public TrackNameInfo(final SliceTrackFormatListener data)
    {
      super(data, data.getName(), "");
    }

    final public PropertyDescriptor[] getPropertyDescriptors()
    {
      try
      {
        final PropertyDescriptor[] res =
            {
                displayProp("Name", "Name", "Name for this formatter"),
                displayProp("Interval", "Interval", "Interval to split on"),
                displayProp("Visible", "Active",
                    "Whether this formatter is active"),
                };

        return res;
      }
      catch (final IntrospectionException e)
      {
        return super.getPropertyDescriptors();
      }
    }
  }

  public static class TestMe extends TestCase
  {

    private FixWrapper createFix(int time)
    {
      Fix newF =
          new Fix(new HiResDate(time), new WorldLocation(2, 2, 0), 22, 33);
      FixWrapper fw = new FixWrapper(newF);
      return fw;

    }
    public void testNameMatch()
    {
      SliceTrackFormatListener cf =
          new SliceTrackFormatListener("Test", 3600000L);
      TrackWrapper tw = new TrackWrapper();
      tw.setName("Name");
      FixWrapper f1 = createFix(4000);
      FixWrapper f2 = createFix(5000);
      FixWrapper f3 = createFix(6000);
      FixWrapper f4 = createFix(10000);
      FixWrapper f5 = createFix(12000);
      cf.newItem(tw, f1, null);
      cf.newItem(tw, f2, null);
      cf.newItem(tw, f3, null);
      cf.newItem(tw, f4, null);
      cf.newItem(tw, f5, null);
      assertTrue("not at end", tw.getNameAtStart());
    }
  }

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private String _formatName;
  private EditorType _myEditor;
  private String[] _tracks;
  private long _interval;

  public SliceTrackFormatListener(final String name,final long interval, final List<String> trackNames)
  {
    _formatName = name;
    _interval = interval;
  }

  @Override
  public void paint(CanvasType dest)
  {
    // don't bother, it can't be plotted
  }

  @Override
  public String getName()
  {
    return _formatName;
  }

  
  
  public long getInterval()
  {
    return _interval;
  }

  public void setInterval(final long interval)
  {
    this._interval = interval;
  }

  public void setName(final String name)
  {
    _formatName = name;
  }

  @Override
  public String toString()
  {
    return getName();
  }

  @Override
  public WorldArea getBounds()
  {
    return null;
  }

  @Override
  public boolean hasEditor()
  {
    return true;
  }

  @Override
  public EditorType getInfo()
  {
    if (_myEditor == null)
    {
      _myEditor = new TrackNameInfo(this);
    }
    return _myEditor;
  }

  @Override
  public void newItem(final Layer parent, final Editable item,
      final String symbology)
  {
    // are we active
    if (!getVisible())
    {
      return;
    }

    // just check if this is actually a new layer call
    if (parent instanceof TrackWrapper)
    {
      TrackWrapper track = (TrackWrapper) parent;

      // ok, do we have a set of track names?
      if (_tracks == null || _tracks.length == 0)
      {
        // nope, just set it
        track.setNameAtStart(false);
      }
      else
      {
        // check if this is one of our tracks
        for (int i = 0; i < _tracks.length; i++)
        {
          String thisT = _tracks[i];
          if (thisT.equals(track.getName()))
          {
            track.setNameAtStart(false);
          }
        }
      }
    }
  }

  @Override
  public void reset()
  {
    // ignore
  }

  public String[] getTracks()
  {
    return _tracks;
  }
}