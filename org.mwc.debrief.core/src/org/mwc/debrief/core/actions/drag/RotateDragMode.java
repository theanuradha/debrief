package org.mwc.debrief.core.actions.drag;

import java.awt.Point;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

import org.eclipse.swt.graphics.Cursor;
import org.mwc.cmap.core.CursorRegistry;
import org.mwc.debrief.core.actions.DragSegment.DragMode;
import org.mwc.debrief.core.actions.DragSegment.IconProvider;

import Debrief.Wrappers.FixWrapper;
import Debrief.Wrappers.TrackWrapper;
import Debrief.Wrappers.Track.TrackSegment;
import Debrief.Wrappers.Track.TrackWrapper_Support.SegmentList;
import MWC.GUI.Editable;
import MWC.GUI.Layer;
import MWC.GUI.Layers;
import MWC.GUI.Shapes.DraggableItem;
import MWC.GUI.Shapes.DraggableItem.LocationConstruct;
import MWC.GenericData.WorldArea;
import MWC.GenericData.WorldDistance;
import MWC.GenericData.WorldLocation;
import MWC.GenericData.WorldVector;

public class RotateDragMode extends DragMode
{

	public RotateDragMode()
	{
		this("Rotate", "Vary course, maintain speed of TMA solution");
	}

	public RotateDragMode(final String title, final String toolTip)
	{
		super(title, toolTip);
	}

	protected TrackSegment findNearest(final TrackWrapper track,
			final WorldLocation loc)
	{
		double res = -1;
		TrackSegment nearest = null;
		final Enumeration<Editable> items = track.elements();
		while (items.hasMoreElements())
		{
			final Editable editable = (Editable) items.nextElement();
			if (editable instanceof SegmentList)
			{
				final SegmentList segList = (SegmentList) editable;
				final Collection<Editable> segments = segList.getData();
				for (final Iterator<Editable> iterator = segments.iterator(); iterator
						.hasNext();)
				{
					final TrackSegment thisSeg = (TrackSegment) iterator.next();

					// is this type of segment suitable?
					if (isAcceptable(thisSeg))
					{
						// yes, is it visible?
						if (thisSeg.getVisible())
						{
							final double thisRes = thisSeg.rangeFrom(loc);
							if (nearest == null)
							{
								nearest = thisSeg;
								res = thisRes;
							}
							else
							{
								if (thisRes < res)
								{
									nearest = thisSeg;
									res = thisRes;
								}
							}
						}
					}
				}

			}
			else if (editable instanceof TrackSegment)
			{
				final TrackSegment thisSeg = (TrackSegment) editable;
				if (isAcceptable(thisSeg))
				{
					if (thisSeg.getVisible())
					{
						final double thisRes = thisSeg.rangeFrom(loc);
						if (nearest == null)
						{
							nearest = thisSeg;
							res = thisRes;
						}
						else
						{
							if (thisRes < res)
							{
								nearest = thisSeg;
								res = thisRes;
							}
						}
					}
				}

			}
		}
		return nearest;

	}

	@Override
	public void findNearest(final Layer thisLayer, final WorldLocation cursorLoc,
			final Point cursorPos, final LocationConstruct currentNearest,
			final Layer parentLayer, final Layers theLayers)
	{
		/**
		 * we need to get the following hit points, both ends (to support rotate),
		 * and the middle (to support drag)
		 */
		if (thisLayer instanceof TrackWrapper)
		{
			final TrackWrapper track = (TrackWrapper) thisLayer;

			// find the nearest segment
			final TrackSegment seg = findNearest(track, cursorLoc);

			if (seg != null)
			{
				final FixWrapper first = (FixWrapper) seg.first();
				final FixWrapper last = (FixWrapper) seg.last();
				final WorldLocation firstLoc = first.getFixLocation();
				final WorldLocation lastLoc = last.getFixLocation();
				final WorldArea lineBounds = new WorldArea(firstLoc, lastLoc);
				final WorldLocation centreLoc = lineBounds.getCentre();

				final WorldDistance firstDist = calcDist(firstLoc, cursorLoc);
				final WorldDistance lastDist = calcDist(lastLoc, cursorLoc);
				final WorldDistance centreDist = calcDist(centreLoc, cursorLoc);

				final DraggableItem dragCentre = getCentreOperation(seg, track,
						theLayers);
				final DraggableItem dragStart = getEndOperation(cursorLoc, seg, last,
						track, theLayers);
				final DraggableItem dragEnd = getEndOperation(cursorLoc, seg, first,
						track, theLayers);

				currentNearest.checkMe(dragStart, firstDist, null, thisLayer);
				currentNearest.checkMe(dragEnd, lastDist, null, thisLayer);
				currentNearest.checkMe(dragCentre, centreDist, null, thisLayer);
			
			}
		}
	}

	/**
	 * whether this type of track is suitable for our operation
	 * 
	 * @param seg
	 * @return
	 */
	protected boolean isAcceptable(final TrackSegment seg)
	{
		return true;
	}

	/**
	 * generate an operation for when the end of the line segment is dragged
	 * 
	 * @param cursorLoc
	 *          where the cursor is
	 * @param seg
	 *          the segment that's being dragged
	 * @param subject
	 *          which end we're manipulating
	 * @param parentTrack
	 * @return
	 */
	protected DraggableItem getEndOperation(final WorldLocation cursorLoc,
			final TrackSegment seg, final FixWrapper subject,
			final TrackWrapper parentTrack, final Layers theLayers)
	{
		return new RotateOperation(cursorLoc, subject.getFixLocation(), seg,
				parentTrack, theLayers);
	}

	/**
	 * generate an operation for when the centre of the line segment is dragged
	 * 
	 * @param seg
	 *          the segment being dragged
	 * @param parent
	 *          the parent track for this segment
	 * @param theLayers
	 *          the set of layers data
	 * @return an operation we can use to do this
	 */
	protected DraggableItem getCentreOperation(final TrackSegment seg,
			final TrackWrapper parent, final Layers theLayers)
	{
		return new TranslateOperation(seg);
	}

	private WorldDistance calcDist(final WorldLocation myLoc,
			final WorldLocation cursorLoc)
	{
		return new WorldDistance(myLoc.subtract(cursorLoc).getRange(),
				WorldDistance.DEGS);

	}

	/*
	 * Public Function RotatePoint(ByRef pPoint As POINT, ByRef pOrigin As POINT,
	 * _ ByVal Degrees As Single) As POINT RotatePoint.X = pOrigin.X + (
	 * Cos(D2R(Degrees)) * (pPoint.X - pOrigin.X) - _ Sin(D2R(Degrees)) *
	 * (pPoint.Y - pOrigin.Y) ) RotatePoint.Y = pOrigin.Y + ( Sin(D2R(Degrees)) *
	 * (pPoint.X - pOrigin.X) + _ Cos(D2R(Degrees)) * (pPoint.Y - pOrigin.Y) ) End
	 * Function
	 */

	public static class RotateOperation extends CoreDragOperation implements
			DraggableItem, IconProvider
	{
		final WorldLocation workingLoc;
		final double originalBearing;
		final WorldLocation _origin;
		Double lastRotate = null;
		final protected TrackWrapper _parent;
		final protected Layers _layers;

		public RotateOperation(final WorldLocation cursorLoc,
				final WorldLocation origin, final TrackSegment segment,
				final TrackWrapper parentTrack, final Layers theLayers)
		{
			super(segment, "end point");
			workingLoc = cursorLoc;
			_origin = origin;
			originalBearing = cursorLoc.subtract(_origin).getBearing();
			_parent = parentTrack;
			_layers = theLayers;
		}

		public void shift(final WorldVector vector)
		{
			// find out where the cursor currently is
			workingLoc.addToMe(vector);

			// what's the bearing from the origin
			final WorldVector thisVector = workingLoc.subtract(_origin);

			// work out the vector (bearing) from the start
			final double brg = originalBearing - thisVector.getBearing();

			// undo the previous turn
			if (lastRotate != null)
			{
				_segment.rotate(-lastRotate, _origin);
			}

			_segment.rotate(brg, _origin);

			// get the segment to recalc it's bounds
			// _segment.clearBounds();

			// and remember it
			lastRotate = new Double(brg);

			// and tell the props view to update itself
			updatePropsView(_segment, _parent, _layers);
		}

		public Cursor getHotspotCursor()
		{
			return CursorRegistry.getCursor(CursorRegistry.SELECT_FEATURE_HIT_ROTATE);
		}
	}

}