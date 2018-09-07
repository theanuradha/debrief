/*
 *    Debrief - the Open Source Maritime Analysis Application
 *    http://debrief.info
 *
 *    (C) 2000-2018, Deep Blue C Technology Ltd
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the Eclipse Public License v1.0
 *    (http://www.eclipse.org/legal/epl-v10.html)
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 */
package MWC.GUI.Shapes.Symbols.SVG;

import java.awt.Color;
import java.awt.Point;

import org.w3c.dom.Element;

import MWC.GUI.CanvasType;

public class SVGCircle extends SVGElement
{

  private double _x;

  private double _y;

  private double _r;

  private Color _fill;

  public SVGCircle(Element dom)
  {
    super(dom);
    try
    {
      _x = Double.parseDouble(getDom().getAttribute("cx"));
      _y = Double.parseDouble(getDom().getAttribute("cy"));
      _r = Double.parseDouble(getDom().getAttribute("r"));

      if (getDom().hasAttribute("fill"))
      {
        // We have a color.
        String colorString = getDom().getAttribute("fill");
        if (colorString.matches("#[0-9A-Fa-f]{6}"))
        {
          _fill = hex2Rgb(colorString);
        }
        else
        {
          MWC.Utilities.Errors.Trace.trace("SVG contains a non-valid fill "
              + colorString);
        }
      }
    }
    catch (Exception e)
    {
      MWC.Utilities.Errors.Trace.trace("Invalid SVG Format");
    }
  }

  @Override
  public void render(CanvasType dest, double sym_size, Point origin_coords,
      double rotation_degs)
  {
    final double x = _x * sym_size + origin_coords.getX();
    final double y = _y * sym_size + origin_coords.getY();
    final double r = _r * sym_size;

    if (_fill != null)
    {
      dest.setColor(_fill);
      dest.fillOval((int) x, (int) y, (int) r, (int) r);
    }
    else
    {
      dest.drawOval((int) x, (int) y, (int) r, (int) r);
    }
  }

}
