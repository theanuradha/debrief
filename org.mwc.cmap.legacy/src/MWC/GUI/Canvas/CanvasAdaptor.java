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

package MWC.GUI.Canvas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.ImageObserver;
import java.util.Vector;

import MWC.GUI.CanvasType;
import MWC.GenericData.WorldLocation;

/**
 * 
 * @author IAN MAYO
 * @version
 */

public class CanvasAdaptor implements MWC.GUI.CanvasType {

	private final java.awt.Graphics _dest;

	private final MWC.Algorithms.PlainProjection _proj;

  private Color _backColor;

  private final Vector<PaintListener> _painters;

	/** Creates new CanvasAdaptor */
	public CanvasAdaptor(final MWC.Algorithms.PlainProjection proj,
			final Graphics dest) {
	  this(proj, dest, null);
	}

	public CanvasAdaptor(final MWC.Algorithms.PlainProjection proj, 
	    final Graphics dest, final Color bkColor)
  {
    _proj = proj;
    _dest = dest;
    _backColor = bkColor;
    _painters= new Vector<CanvasType.PaintListener>();
  }

  public void addPainter(final CanvasType.PaintListener listener) {
    _painters.add(listener);
	}

	public void drawLine(final int x1, final int y1, final int x2, final int y2) {
		_dest.drawLine(x1, y1, x2, y2);
	}

	public boolean drawImage(final Image img, final int x, final int y, final int width, final int height,
			final ImageObserver observer) {
		if (_dest == null)
			return false;

		return _dest.drawImage(img, x, y, width, height, observer);
	}

	/**
	 * draw a filled polygon
	 * 
	 * @param xPoints
	 *            list of x coordinates
	 * @param yPoints
	 *            list of y coordinates
	 * @param nPoints
	 *            length of list
	 */
	public void fillPolygon(final int[] xPoints, final int[] yPoints, final int nPoints) {
		_dest.fillPolygon(xPoints, yPoints, nPoints);
	}

	/**
	 * drawPolyline
	 * 
	 * @param xPoints
	 *            list of x coordinates
	 * @param yPoints
	 *            list of y coordinates
	 * @param nPoints
	 *            length of list
	 */
	public void drawPolyline(final int[] xPoints, final int[] yPoints, final int nPoints) {
		_dest.drawPolyline(xPoints, yPoints, nPoints);
	}

	/**
	 * drawPolygon
	 * 
	 * @param xPoints
	 *            list of x coordinates
	 * @param yPoints
	 *            list of y coordinates
	 * @param nPoints
	 *            length of list
	 */
	public void drawPolygon(final int[] xPoints, final int[] yPoints, final int nPoints) {
		_dest.drawPolygon(xPoints, yPoints, nPoints);
	}

	public void drawText(final Font theFont, final String theStr, final int x, final int y) {
		_dest.setFont(theFont);
		_dest.drawString(theStr, x, y);
	}

	/**
	 * set/get the background colour
	 */
	public Color getBackgroundColor() {
		return _backColor;
	}

	/**
	 * expose the graphics object, used only for plotting non-persistent
	 * graphics (temporary lines, etc).
	 */
	public Graphics getGraphicsTemp() {
		return _dest;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public java.util.Enumeration getPainters() {
	  return _painters.elements();
	}

	public MWC.Algorithms.PlainProjection getProjection() {
		return _proj;
	}

	public Dimension getSize() {
		return _proj.getScreenArea().getSize();
	}

	public int getStringHeight(final Font theFont) {
		return _dest.getFontMetrics(theFont).getHeight();
	}

	public int getStringWidth(final Font theFont, final String theString) {
		return _dest.getFontMetrics(theFont).stringWidth(theString);
	}

	public void removePainter(final CanvasType.PaintListener listener) {
	  _painters.remove(listener);
	}

	/**
	 * retrieve the full data area, and do a fit to window
	 */
	public void rescale() {
		//
	}

	public void setBackgroundColor(final Color theColor) {
		//
	}

	public void setProjection(final MWC.Algorithms.PlainProjection val) {
		//
	}

	public void setTooltipHandler(final CanvasType.TooltipHandler handler) {
		//
	}

	public Point toScreen(final WorldLocation val) {
		return _proj.toScreen(val);
	}

	public WorldLocation toWorld(final Point val) {
		return _proj.toWorld(val);
	}

	public void updateMe() {
		//
	}

	public void drawOval(final int x, final int y, final int width, final int height) {
		//
		_dest.drawOval(x, y, width, height);
	}

	public void fillOval(final int x, final int y, final int width, final int height) {
		//
		_dest.fillOval(x, y, width, height);
	}

	public void drawText(final String str, final int x, final int y) {
		//
		_dest.drawString(str, x, y);
	}

	public void drawRect(final int x1, final int y1, final int wid, final int height) {
		//
		_dest.drawRect(x1, y1, wid, height);
	}

	public void fillRect(final int x, final int y, final int wid, final int height) {
		//
		_dest.fillRect(x, y, wid, height);
	}

	/** client has finished drawing operation */
	public void endDraw(final Object theVal) {
		//
	}

	/** client is about to start drawing operation */
	public void startDraw(final Object theVal) {
		//
	  if(theVal instanceof Graphics2D)
	  {
      Graphics2D g2 = (Graphics2D) theVal;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	  }
	}

	/**
   * set the style for the line, using our constants
   * 
   */
  public void setLineStyle(final int style)
  {
    final BasicStroke stk = MWC.GUI.Canvas.Swing.SwingCanvas.getStrokeFor(
        style);
    final BasicStroke stk2 = new BasicStroke(getLineWidth(), stk.getEndCap(),
        stk.getLineJoin());
    final Graphics2D g2 = (Graphics2D) _dest;
    g2.setStroke(stk2);
	}

  /**
   * set the width of the line, in pixels
   * 
   */
  public void setLineWidth(final float width)
  {
    final BasicStroke stk = new BasicStroke(width);
    final Graphics2D g2 = (Graphics2D) _dest;
    g2.setStroke(stk);
  }

	public float getLineWidth() {
		final Graphics2D g2 = (Graphics2D) _dest;
		final BasicStroke bs = (BasicStroke) g2.getStroke();
		return bs.getLineWidth();
	}

	public void setColor(final Color theCol) {
		//
		_dest.setColor(theCol);
	}

	public void fillArc(final int x, final int y, final int width, final int height, final int startAngle,
			final int arcAngle) {
		//
		_dest.fillArc(x, y, width, height, startAngle, arcAngle);
	}

	public void drawArc(final int x, final int y, final int width, final int height, final int startAngle,
			final int arcAngle) {
		//
		_dest.drawArc(x, y, width, height, startAngle, arcAngle);
	}

	final public void drawPolyline(final int[] points) {
		// get the convenience function to plot this for us
		CanvasAdaptor.drawPolylineForMe(points, this);
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	// testing for this class
	// ////////////////////////////////////////////////////////////////////////////////////////////////
	static public final class testMe extends junit.framework.TestCase {
		static public final String TEST_ALL_TEST_TYPE = "UNIT";

		public testMe(final String val) {
			super(val);
		}

		public final void testPolygonMgt() {
			final int[] points = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
			// convert to normal format
			final int len = points.length / 2;
			final int[] xP = new int[len];
			final int[] yP = new int[len];

			for (int i = 0; i < points.length; i += 2) {
				xP[i / 2] = points[i];
				yP[i / 2] = points[i + 1];
			}
			assertEquals("array wrong length", len, 5);
			assertEquals("wrong first x", xP[0], 1);
			assertEquals("wrong first y", yP[0], 2);
			assertEquals("wrong last x", xP[len - 1], 9);
			assertEquals("wrong last y", yP[len - 1], 10);
		}
	}

	/** convenience method that allows a canvas implementation to support 
	 * the new polyline method by just converting the data and calling the old 
	 * method
	 * @param points the series of points in the new format
	 * @param canvas the canvas implementation that wants to plot it.
	 */
	public static void drawPolylineForMe(final int[] points,
			final CanvasType canvas) {
		final int len = points.length / 2;
		final int[] xP = new int[len];
		final int[] yP = new int[len];

		// copy bits in to new arrays
		for (int i = 0; i < points.length; i += 2) {
			xP[i / 2] = points[i];
			yP[i / 2] = points[i + 1];
		}

		// do the old-fashioned copy operation
		canvas.drawPolyline(xP, yP, len);

	}

  @Override
  public void drawText(final String str, final int x, final int y,
      final float rotate)
  {
    drawText(str, x, y, rotate, true);
  }

	@Override
	public void setFont(final Font theFont)
	{
		_dest.setFont(theFont);
	}

  @Override
  public void drawText(String str, int x, int y, float rotate, boolean above)
  {
    if (_dest instanceof Graphics2D)
    {
      final Graphics2D g2d = (Graphics2D) _dest;

      g2d.translate((float) x, (float) y);
      g2d.rotate(Math.toRadians(rotate));
      g2d.drawString(str, 0, 0);
      g2d.rotate(-Math.toRadians(rotate));
      g2d.translate(-(float) x, -(float) y);
    }
  }
}
