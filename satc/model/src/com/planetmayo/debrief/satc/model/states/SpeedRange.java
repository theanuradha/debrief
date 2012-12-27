package com.planetmayo.debrief.satc.model.states;

/**
 * class representing a set of speed bounds
 * 
 * @author ian
 * 
 */
public class SpeedRange extends BaseRange<SpeedRange>
{
	private double _minSpeed;
	private double _maxSpeed;

	public SpeedRange(double minSpd, double maxSpd)
	{
		_minSpeed = minSpd;
		_maxSpeed = maxSpd;
	}

	/**
	 * copy constructor
	 * 
	 * @param range
	 */
	public SpeedRange(SpeedRange range)
	{
		this(range.getMin(), range.getMax());
	}

	@Override
	public void constrainTo(SpeedRange sTwo) throws IncompatibleStateException
	{
		_minSpeed = Math.max(getMin(), sTwo.getMin());
		_maxSpeed = Math.min(getMax(), sTwo.getMax());
	}

	public double getMax()
	{
		return _maxSpeed;
	}

	public double getMin()
	{
		return _minSpeed;
	}	

	@Override
	public int hashCode()
	{
    int result = (int) _minSpeed;
    result = 31 * result + (int) _maxSpeed;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || getClass() != obj.getClass())
		{
			return false;
		}
		SpeedRange other = (SpeedRange) obj;		
		
		if (_maxSpeed != other._maxSpeed)	return false;
		if (_minSpeed != other._minSpeed)	return false;

		return true;
	}		
}
