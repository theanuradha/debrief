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
package org.mwc.debrief.core.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.mwc.cmap.core.CorePlugin;
import org.mwc.cmap.core.ui_support.CoreViewLabelProvider.ViewLabelImageHelper;
import org.mwc.debrief.core.DebriefPlugin;

import Debrief.Wrappers.FixWrapper;
import Debrief.Wrappers.LabelWrapper;
import Debrief.Wrappers.NarrativeWrapper;
import Debrief.Wrappers.SensorContactWrapper;
import Debrief.Wrappers.SensorWrapper;
import Debrief.Wrappers.ShapeWrapper;
import Debrief.Wrappers.TrackWrapper;
import Debrief.Wrappers.Track.CoreTMASegment;
import Debrief.Wrappers.Track.TrackSegment;
import MWC.GUI.Editable;
import MWC.GUI.ExternallyManagedDataLayer;
import MWC.GUI.Shapes.ChartBoundsWrapper;
import MWC.GUI.Shapes.ChartFolio;
import MWC.GUI.Shapes.PolygonShape.PolygonNode;
import MWC.TacticalData.NarrativeEntry;

public class DebriefImageHelper implements ViewLabelImageHelper
{

	public ImageDescriptor getImageFor(final Editable editable)
	{
		ImageDescriptor res = null;
		
		if (editable instanceof TrackWrapper)
			res = CorePlugin.getImageDescriptor("icons/track.png");
		else if (editable instanceof SensorWrapper)
			res = CorePlugin.getImageDescriptor("icons/SensorFit.png");
		else if (editable instanceof ChartFolio)
			res = DebriefPlugin.getImageDescriptor("icons/16/library.png");
		else if (editable instanceof ExternallyManagedDataLayer)
			res = DebriefPlugin.getImageDescriptor("icons/16/map.png");
		else if (editable instanceof ChartBoundsWrapper)
			res = DebriefPlugin.getImageDescriptor("icons/16/map.png");
		else if (editable instanceof SensorContactWrapper)
			res = CorePlugin.getImageDescriptor("icons/active16.png");
		else if (editable instanceof NarrativeWrapper)
			res = CorePlugin.getImageDescriptor("icons/narrative.jpg");
		else if (editable instanceof NarrativeEntry)
			res = CorePlugin.getImageDescriptor("icons/NarrativeItem.jpg");
		else if (editable instanceof CoreTMASegment)
			res = CorePlugin.getImageDescriptor("icons/tmasegment.png");
		else if (editable instanceof TrackSegment)
			res = CorePlugin.getImageDescriptor("icons/tracksegment.gif");
		else if (editable instanceof FixWrapper)
			res = CorePlugin.getImageDescriptor("icons/Location.png");
		else if (editable instanceof ShapeWrapper)
			res = CorePlugin.getImageDescriptor("icons/shape.gif");
		else if (editable instanceof PolygonNode)
			res = CorePlugin.getImageDescriptor("icons/fix.gif");
		else if (editable instanceof LabelWrapper)
			res = CorePlugin.getImageDescriptor("icons/shape.gif");
		 
		return res;
	}

}
