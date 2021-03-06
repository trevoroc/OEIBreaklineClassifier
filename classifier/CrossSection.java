/*
 * This is a Java source file for the CrossSection class, used in the OEIClassifier.
 */

/** @author Trevor O'Connor, @date 9/7/2016 3:55 PM */

package classifier;

import java.util.ArrayList;
import java.util.Collections;

class CrossSection {

	/** An array containing the CrossSectionPoints making up this CrossSection. */
	private ArrayList<CrossSectionPoint> _points;

	/** The name of the site this CrossSection came from. */
	private String _name;

	/** A float that will function as a unique identifier for this CrossSection
	 *  within the scope of its site. Will be the downstream distance of this
	 *  CrossSection. */
	private float _id;

	/** The thalweg of this CrossSection. */
	private CrossSectionPoint _thalweg;

	/** True if this CrossSection's thalweg is an edge. */
	private boolean _thalIsEdge;

	/** Constructs a new CrossSection with _name NAME, _id ID, and _points POINTS,
	 *  and the index of the thalweg THALIND. */
	CrossSection(String name, float id, ArrayList<CrossSectionPoint> points, int thalInd) {
		_name = name;
		_id = id;
		_points = points;
		_thalweg = points.get(thalInd);
		_thalIsEdge = thalInd == 0 || thalInd == points.size()-1;
	}

	/** Returns the name of this CrossSection's site. */
	String getName() {
		return _name;
	}

	/** Returns this CrossSection's _id. */
	float getId() {
		return _id;
	}

	/** Returns the array of points of this CrossSection. May or may not be sorted. */
	ArrayList<CrossSectionPoint> getPoints() {
		return _points;
	}

	/** Returns this CrossSection's thalweg. */
	CrossSectionPoint getThalweg() {
		return _thalweg;
	}

	/** Returns _thalIsEdge. */
	boolean thalIsEdge() {
		return _thalIsEdge;
	}

	/** Calculates the slope between the two given points. */
	private static float calculateSlope(CrossSectionPoint p1, CrossSectionPoint p2) {
		return (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());
	}

	/** Calculates the inflections of every point in _points. Excludes the first
	 *  and last points. */
	void calculateInflections() {
		float s1;
		float s2;
		int size = _points.size();
		for (int i = 1; i < size-1; i++) {
			s1 = calculateSlope(_points.get(i-1), _points.get(i));
			s2 = calculateSlope(_points.get(i), _points.get(i+1));
			_points.get(i).setInflection(Math.abs(s1-s2));
		}
	}

	/** Performs all analytical calculations on this CrossSection:
	 *  inflections, subsequent ranking, and vertical and horizontal distances
	 *  from the thalweg. */
	void analyze(Options options) {
		calculateInflections();
		rank();
		calculateThalwegDistances();
		filter(options);
	}

	/** Removes all points designated by the limits in OPTIONS from this
	 *  CrossSection. */
	private void filter(Options options) {
		trimHorizVert(options);
		trimRank(options.getN());
	}

	/** Removes all points from this CrossSection that do not fit within the
	 *  range of designated horizontal and vertical distance from the thalweg. */
	private void trimHorizVert(Options options) {
		int i = 0;
		CrossSectionPoint p;
		while (i < _points.size()) {
			p = _points.get(i);
			if (!options.isWithinLimits(p)) {
				_points.remove(p);
			} else {
				i++;
			}
		}
	}

	/** Filters this CrossSection to only include the top N points. */
	private void trimRank(int n) {
		int size = _points.size();
		if (size > n) {
			Collections.sort(_points, new CSPRankComparator());
			_points.subList(n, size).clear();
			Collections.sort(_points, new CSPXComparator());
		}
	}

	/** Performs all thalweg distance calculations for this CrossSection. */
	void calculateThalwegDistances() {
		int size = _points.size();
		float thalX = _thalweg.getX();
		float thalY = _thalweg.getY();
		for (int i = 0; i < size; i++) {
			_points.get(i).setHorizDistThal(thalX);
			_points.get(i).setVertDistThal(thalY);
		}
	}

	/** Sorts _points in descending order of their inflections. */
	void rank() {
		ArrayList<CrossSectionPoint> dupPoints = (ArrayList) _points.clone();
		Collections.sort(dupPoints, new CSPInflectionComparator());
		int size = _points.size();
		for (int i = 0; i < size; i++) {
			dupPoints.get(i).setRank(i+1);
		}
		
	}

}
