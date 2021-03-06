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
// $RCSfile: WheelShape.java,v $
// @author $Author: Ian.Mayo $
// @version $Revision: 1.7 $
// $Log: WheelShape.java,v $
// Revision 1.7  2006/04/21 07:48:38  Ian.Mayo
// Make things draggable
//
// Revision 1.6  2005/11/11 16:34:59  Ian.Mayo
// Add spoke sizes
//
// Revision 1.5  2005/11/10 16:17:08  Ian.Mayo
// Introduce Stepping bounded integer editor
//
// Revision 1.4  2005/11/10 11:37:13  Ian.Mayo
// Allow user to specify number of spokes
//
// Revision 1.3  2004/08/31 09:38:20  Ian.Mayo
// Rename inner static tests to match signature **Test to make automated testing more consistent
//
// Revision 1.2  2004/05/25 15:37:22  Ian.Mayo
// Commit updates from home
//
// Revision 1.1.1.1  2004/03/04 20:31:22  ian
// no message
//
// Revision 1.1.1.1  2003/07/17 10:07:34  Ian.Mayo
// Initial import
//
// Revision 1.8  2003-07-04 11:00:52+01  ian_mayo
// Reflect name change of parent editor test
//
// Revision 1.7  2003-07-03 14:59:49+01  ian_mayo
// Reflect new signature of PlainShape constructor, where we don't need to set the default colour
//
// Revision 1.6  2003-06-25 08:50:58+01  ian_mayo
// Only plot if we are visible
//
// Revision 1.5  2003-02-07 15:36:46+00  ian_mayo
// Implement unused "Get data points" method from parent class
//
// Revision 1.4  2003-01-21 16:32:11+00  ian_mayo
// move getColor property management to ShapeWrapper
//
// Revision 1.3  2002-10-30 16:26:57+00  ian_mayo
// tidy (shorten) up display names for editables
//
// Revision 1.2  2002-05-28 09:25:51+01  ian_mayo
// after switch to new system
//
// Revision 1.1  2002-05-28 09:14:24+01  ian_mayo
// Initial revision
//
// Revision 1.1  2002-04-11 14:01:09+01  ian_mayo
// Initial revision
//
// Revision 1.2  2002-03-19 11:04:04+00  administrator
// Add a "type" property to indicate type of shape (label, rectangle, etc)
//
// Revision 1.1  2002-01-17 20:40:26+00  administrator
// Reflect switch to Duration/WorldDistance
//
// Revision 1.0  2001-07-17 08:43:17+01  administrator
// Initial revision
//
// Revision 1.3  2001-01-22 19:40:35+00  novatech
// reflect optimised projection.toScreen plotting
//
// Revision 1.2  2001-01-22 12:29:27+00  novatech
// added JUnit testing code
//
// Revision 1.1  2001-01-03 13:42:26+00  novatech
// Initial revision
//
// Revision 1.1.1.1  2000/12/12 21:49:21  ianmayo
// initial version
//
// Revision 1.3  2000-11-17 09:07:32+00  ian_mayo
// create other, more useful constructor
//
// Revision 1.2  2000-10-03 14:14:39+01  ian_mayo
// complete implementation
//
// Revision 1.1  2000-09-28 16:56:32+01  ian_mayo
// Initial revision
//

package MWC.GUI.Shapes;

import java.awt.Color;
import java.awt.Point;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import MWC.GUI.CanvasType;
import MWC.GUI.Editable;
import MWC.GUI.PlainWrapper;
import MWC.GUI.Properties.SteppingBoundedInteger;
import MWC.GenericData.WorldArea;
import MWC.GenericData.WorldDistance;
import MWC.GenericData.WorldLocation;
import MWC.GenericData.WorldVector;

/**
 * Class representing a cart-wheel type shape - drawn with inner and outer
 * radiuses, with spokes at 60 degree intervals
 */
public class WheelShape extends PlainShape implements Editable
{

	// ////////////////////////////////////////////////
	// member variables
	// ////////////////////////////////////////////////

	// ////////////////////////////////////////////////////
	// bean info for this class
	// ///////////////////////////////////////////////////
	public class WheelInfo extends Editable.EditorType
	{

		public WheelInfo(final WheelShape data, final String theName)
		{
			super(data, theName, "");
		}

		@Override
		public PropertyDescriptor[] getPropertyDescriptors()
		{
			try
			{
				final PropertyDescriptor[] res =
				{
						displayProp("RadiusInner", "Inner radius", "the Inner radius of the wheel (yds)"),
						displayProp("SpokeSize", "Spoke size", "the arc covered by each spoke (degs)"),
						displayProp("EmptyInner", "Empty inner",
								"whether to keep the inner circle clear of spokes"),
						displayProp("RadiusOuter", "Outer radius", "the Outer radius of the wheel (yds)"),
						prop("Orientation", "the orientation of the wheel"),
						prop("Centre", "the centre of the Wheel") 
				};

				return res;

			}
			catch (final IntrospectionException e)
			{
				return super.getPropertyDescriptors();
			}
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	// testing for this class
	// ////////////////////////////////////////////////////////////////////////////////////////////////
	static public class WheelTest extends junit.framework.TestCase
	{
		static public final String TEST_ALL_TEST_TYPE = "UNIT";

		public WheelTest(final String val)
		{
			super(val);
		}

		public void testMyParams()
		{
			MWC.GUI.Editable ed = new WheelShape(new WorldLocation(2d, 2d, 2d), 2d,
					2d);
			MWC.GUI.Editable.editableTesterSupport.testParams(ed, this);
			ed = null;
		}
	}

	// keep track of versions
	static final long serialVersionUID = 1;

	/**
	 * the area covered by this Wheel
	 */
	private WorldArea _theArea;

	/**
	 * the centre of this Wheel
	 */
	private WorldLocation _theCentre;

	/**
	 * whether to keep the inner circle clear of spokes
	 * 
	 */
	private boolean _emptyInner = false;

	/**
	 * the outer radius of the wheel (in degs)
	 */
	private WorldDistance _theOuterRadius;

	/**
	 * the inner radius of the wheel (in degs)
	 */
	private WorldDistance _theInnerRadius;

	/**
	 * the size of each spoke (in degs)
	 */
	private int _theSpokeSize = 60;
	/**
	 * The (clockwise) orientation of the wheel : that is, the 
	 * rotation of the spokes of the wheel.
	 */
	private int _orientation=0;

	/**
	 * our editor
	 */
	transient private Editable.EditorType _myEditor;

	// ////////////////////////////////////////////////
	// constructor
	// ////////////////////////////////////////////////

	/**
	 * Normal constructor for object
	 * 
	 * @param theCentre
	 *          the centre of the wheel
	 * @param theInnerRadius
	 *          the inner radius of the wheel, in yds
	 * @param theOuterRadius
	 *          the outer radius of the wheel, in yds
	 * @param theColor
	 *          the colour to plot the wheel
	 */
	public WheelShape(final WorldLocation theCentre, final double theInnerRadius,
			final double theOuterRadius)
	{
		this(theCentre, new WorldDistance(theInnerRadius, WorldDistance.YARDS),
				new WorldDistance(theOuterRadius, WorldDistance.YARDS));
	}

	/**
	 * Normal constructor for object
	 * 
	 * @param theCentre
	 *          the centre of the wheel
	 * @param theInnerRadius
	 *          the inner radius of the wheel, in yds
	 * @param theOuterRadius
	 *          the outer radius of the wheel, in yds
	 * @param theColor
	 *          the colour to plot the wheel
	 */
	public WheelShape(final WorldLocation theCentre, final WorldDistance theInnerRadius,
			final WorldDistance theOuterRadius)
	{
		super(0, "Wheel");

		// store the values
		_theCentre = theCentre;
		_theInnerRadius = theInnerRadius;
		_theOuterRadius = theOuterRadius;

		// store the corners of the area,
		calcPoints();
	}

	/**
	 * default constructor
	 */
	// public WheelShape(){
	// // scrap, in case we are serializing
	// _theArea = null;
	// }

	// ////////////////////////////////////////////////
	// member functions
	// ////////////////////////////////////////////////

	/**
	 * calculate some convenience values based on the radius and centre of the
	 * Wheel
	 */
	protected void calcPoints()
	{
		// create our area
		_theArea = new WorldArea(_theCentre, _theCentre);

		// create & extend to top left
		WorldLocation other = _theCentre.add(new WorldVector(0, _theOuterRadius
				.getValueIn(WorldDistance.DEGS), 0));
		other.addToMe(new WorldVector(MWC.Algorithms.Conversions.Degs2Rads(270),
				_theOuterRadius.getValueIn(WorldDistance.DEGS), 0));
		_theArea.extend(other);

		// create & extend to bottom right
		other = _theCentre.add(new WorldVector(MWC.Algorithms.Conversions
				.Degs2Rads(180), _theOuterRadius.getValueIn(WorldDistance.DEGS), 0));
		other.addToMe(new WorldVector(MWC.Algorithms.Conversions.Degs2Rads(90),
				_theOuterRadius.getValueIn(WorldDistance.DEGS), 0));
		_theArea.extend(other);
	}

	/**
	 * get the 'anchor point' for any labels attached to this shape
	 */
	public MWC.GenericData.WorldLocation getAnchor()
	{
		return _theCentre;
	}

	@Override
	public MWC.GenericData.WorldArea getBounds()
	{
		return _theArea;
	}

	/**
	 * return the centre of the Wheel
	 * 
	 * @return the centre of the Wheel
	 */
	public WorldLocation getCentre()
	{
		return _theCentre;
	}

	public Editable.EditorType getInfo()
	{
		if (_myEditor == null)
			_myEditor = new WheelInfo(this, this.getName());

		return _myEditor;
	}

	/**
	 * get the inner radius of the Wheel
	 * 
	 * @return radius in yards
	 */
	public WorldDistance getRadiusInner()
	{
		return _theInnerRadius;
	}

	/**
	 * get the outer radius of the Wheel
	 * 
	 * @return radius in yards
	 */
	public WorldDistance getRadiusOuter()
	{
		return _theOuterRadius;
	}

	/**
	 * @return Returns the _theSpokeSize.
	 */
	public SteppingBoundedInteger getSpokeSize()
	{
		return new SteppingBoundedInteger(_theSpokeSize, 5, 180, 5);
	}

	public Color getWheelColor()
	{
		return super.getColor();
	}

	public boolean hasEditor()
	{
		return true;
	}

	public boolean isEmptyInner()
	{
		return _emptyInner;
	}
	
	
	public Integer getOrientation() {
		return _orientation;
	}

	public void setOrientation(final Integer orientation) {
		this._orientation = orientation;
	}

	/**
	 * paint the object
	 * 
	 * @param dest
	 *          the destination
	 */
	@Override
	public void paint(final CanvasType dest)
	{
		// are we visible?
		if (!getVisible())
			return;
		// set the colour, if we know it
		if (this.getColor() != null)
			dest.setColor(this.getColor());

		final double spoke_separation = _theSpokeSize;
		final int arcs = 180;

		dest.setColor(getColor());

		final MWC.Algorithms.PlainProjection _proj = dest.getProjection();

		// sort out the centre in screen coords
		final Point centre = new Point(_proj.toScreen(_theCentre));

		// sort out the range in screen coords
		final WorldLocation outerEdge = _theCentre.add(new WorldVector(
				MWC.Algorithms.Conversions.Degs2Rads(0), _theOuterRadius
						.getValueIn(WorldDistance.DEGS), 0));
		final WorldLocation innerEdge = _theCentre.add(new WorldVector(
				MWC.Algorithms.Conversions.Degs2Rads(0), _theInnerRadius
						.getValueIn(WorldDistance.DEGS), 0));
		final Point screenOuterEdge = new Point(_proj.toScreen(outerEdge));
		final Point screenInnerEdge = new Point(_proj.toScreen(innerEdge));
		int dx = screenOuterEdge.x - centre.x;
		int dy = screenOuterEdge.y - centre.y;
		final int outerRadius = (int) Math.sqrt(dx * dx + dy * dy);

		dx = screenInnerEdge.x - centre.x;
		dy = screenInnerEdge.y - centre.y;
		final int innerRadius = (int) Math.sqrt(dx * dx + dy * dy);

		// check that the axis is in the correct direction (we may be in relative
		// projection)
		int axis = (int) MWC.Algorithms.Conversions.Rads2Degs(Math.atan2(dx, -dy));
		//Add the orientation to offest the spokes correctly
		axis += _orientation;
		
		// now draw the spokes, working out either side from the axis
		double thisSpoke = 0; // on the axis
		double spokeRads1 = 0;
		double spokeRads2 = 0;
		final Point edge1 = new Point();
		final Point edge2 = new Point();
		final Point inner1 = new Point();
		final Point inner2 = new Point();

		inner1.setLocation(centre);
		inner2.setLocation(centre);

		while (thisSpoke <= arcs)
		{
			// find the left/right angles in rads
			spokeRads1 = MWC.Algorithms.Conversions.Degs2Rads(axis - thisSpoke);
			spokeRads2 = MWC.Algorithms.Conversions.Degs2Rads(axis + thisSpoke);
			// calculate the offset produced by this angle
			edge1.setLocation((int) (outerRadius * Math.sin(spokeRads1)),
					-(int) (outerRadius * Math.cos(spokeRads1)));
			edge2.setLocation((int) (outerRadius * Math.sin(spokeRads2)),
					-(int) (outerRadius * Math.cos(spokeRads2)));

			if (isEmptyInner())
			{
				// calculate the offset produced by this angle
				inner1.setLocation((int) (innerRadius * Math.sin(spokeRads1)),
						-(int) (innerRadius * Math.cos(spokeRads1)));
				inner2.setLocation((int) (innerRadius * Math.sin(spokeRads2)),
						-(int) (innerRadius * Math.cos(spokeRads2)));
				inner1.translate(centre.x, centre.y);
				inner2.translate(centre.x, centre.y);
			}

			// add this to the centre
			edge1.translate(centre.x, centre.y);
			edge2.translate(centre.x, centre.y);


			// draw the line
			dest.drawLine(inner1.x, inner1.y, edge1.x, edge1.y);
			dest.drawLine(inner2.x, inner2.y, edge2.x, edge2.y);
			// move on to the next spoke
			thisSpoke += spoke_separation;
		}

		// now the inner and outer range rings
		final double ring_separation = outerRadius - innerRadius;
		int thisRadius = innerRadius;
		final Point origin = new Point();

		// draw the ovals
		for (int i = 0; i < 2; i++)
		{
			origin.setLocation(centre);

			// shift the centre point to the TL corner of the area
			origin.translate(-thisRadius, -thisRadius);

			// draw in the arc itself
			dest.drawOval(origin.x, origin.y, thisRadius * 2, thisRadius * 2);

			// move on to the next radius
			thisRadius += ring_separation;
		}

	}

	/**
	 * get the range from the indicated world location - making this abstract
	 * allows for individual shapes to have 'hit-spots' in various locations.
	 */
	@Override
	public double rangeFrom(final WorldLocation point)
	{
		final double res = _theCentre.rangeFrom(point);

		return res;
	}

	/**
	 * set the centre location of the Wheel
	 */
	public void setCentre(final WorldLocation centre)
	{
		// inform our listeners
		firePropertyChange(PlainWrapper.LOCATION_CHANGED, _theCentre, centre);
		// make the change
		_theCentre = centre;
		// and calc the new summary data
		calcPoints();

		// and inform the parent (so it can move the label)
		firePropertyChange(PlainWrapper.LOCATION_CHANGED, null, null);

	}

	public void setEmptyInner(final boolean emptyInner)
	{
		_emptyInner = emptyInner;
	}

	/**
	 * set the inner radius of the wheel
	 * 
	 * @param val
	 *          the new radius (in degrees)
	 */
	public void setRadiusInner(final WorldDistance val)
	{
		_theInnerRadius = val;
		// and calc the new summary data
		calcPoints();

		// and inform the parent (so it can move the label)
		firePropertyChange(PlainWrapper.LOCATION_CHANGED, null, null);

	}

	/**
	 * set the outer radius of the wheel
	 * 
	 * @param val
	 *          the new radius (in degrees)
	 */
	public void setRadiusOuter(final WorldDistance val)
	{
		_theOuterRadius = val;

		// and calc the new summary data
		calcPoints();

		// and inform the parent (so it can move the label)
		firePropertyChange(PlainWrapper.LOCATION_CHANGED, null, null);

	}

	/**
	 * @param spokeSize
	 *          The _theSpokeSize to set.
	 */
	public void setSpokeSize(final SteppingBoundedInteger spokeSize)
	{
		_theSpokeSize = spokeSize.getCurrent();

		// and calc the new summary data
		calcPoints();

		// and inform the parent (so it can move the label)
		firePropertyChange(PlainWrapper.LOCATION_CHANGED, null, null);

	}

	// ////////////////////////////////////////
	// convenience functions which pass calls back to parent
	// ////////////////////////////////////////
	public void setWheelColor(final Color val)
	{
		super.setColor(val);
	}

	public void shift(final WorldVector vector)
	{
		final WorldLocation oldCentre = getCentre();
		final WorldLocation newCentre = oldCentre.add(vector);
		setCentre(newCentre);

		// and calc the new summary data
		calcPoints();

		// and inform the parent (so it can move the label)
		firePropertyChange(PlainWrapper.LOCATION_CHANGED, null, null);

	}
}