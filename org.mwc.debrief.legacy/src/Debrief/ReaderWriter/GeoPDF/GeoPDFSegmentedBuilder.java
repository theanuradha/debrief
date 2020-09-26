package Debrief.ReaderWriter.GeoPDF;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import Debrief.GUI.Frames.Application;
import Debrief.ReaderWriter.GeoPDF.GenerateSegmentedGeoJSON.GeometryType;
import Debrief.ReaderWriter.GeoPDF.GenerateSegmentedGeoJSON.SegmentedGeoJSONConfiguration;
import Debrief.ReaderWriter.GeoPDF.GeoPDF.GeoPDFLayerBackground;
import Debrief.ReaderWriter.GeoPDF.GeoPDF.GeoPDFLayerTrack;
import Debrief.ReaderWriter.GeoPDF.GeoPDF.GeoPDFLayerVector;
import Debrief.ReaderWriter.GeoPDF.GeoPDF.GeoPDFPage;
import Debrief.ReaderWriter.GeoPDF.GeoPDF.GeoPDFLayerVector.LogicalStructure;
import Debrief.Wrappers.FixWrapper;
import Debrief.Wrappers.TrackWrapper;
import MWC.GUI.Editable;
import MWC.GUI.Layers;
import MWC.GUI.ToolParent;
import MWC.GenericData.TimePeriod;

public class GeoPDFSegmentedBuilder extends AbstractGeoPDFBuilder {

	@Override
	public GeoPDF build(Layers layers, GeoPDFConfiguration configuration)
			throws IOException, InterruptedException, NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException, ClassNotFoundException {

		if (!configuration.isReady()) {
			Application.logError3(ToolParent.INFO, "GeoPDF-GDAL Temporary environment is about to be prepared.", null,
					false);
			configuration.prepareGdalEnvironment();
		}
		final GeoPDF geoPDF = new GeoPDF();

		geoPDF.setAuthor(configuration.getAuthor());

		/**
		 * For now let's work using only one page, but we will fix it eventually to have
		 * several pages.
		 */
		final GeoPDFPage mainPage = geoPDF.createNewPage();

		mainPage.setDpi(configuration.getPageDpi());
		mainPage.setWidth(configuration.getPageWidth());
		mainPage.setHeight(configuration.getPageHeight());
		mainPage.setMargin(configuration.getMarginPercent());
		if (configuration.getViewportArea() == null) {
			mainPage.setArea(layers.getBounds());
		} else {
			mainPage.setArea(configuration.getViewportArea());
		}

		/**
		 * Let's create the BackGroundLayer;
		 */
		for (String background : configuration.getBackground()) {
			final File backgroundFile = createBackgroundFile(configuration, background, geoPDF.getFilesToDelete());
			final GeoPDFLayerBackground backgroundLayer = new GeoPDFLayerBackground();
			backgroundLayer.setName("Background chart");
			backgroundLayer.setId("background");
			backgroundLayer.addRaster(backgroundFile.getAbsolutePath());
			mainPage.addLayer(backgroundLayer);
		}

		/**
		 * Let's add now the non-interactive layers
		 */
		final ObjectMapper mapper = new ObjectMapper();
		final StringBuilder javascriptNonInteractiveLayerIndex = new StringBuilder();
		javascriptNonInteractiveLayerIndex.append("var nonInteractiveLayers = [");

		/**
		 * Let's iterate over all the layers to find the Tracks to export
		 */
		final Enumeration<Editable> enumerationNonInteractive = layers.elements();
		while (enumerationNonInteractive.hasMoreElements()) {
			final Editable currentEditable = enumerationNonInteractive.nextElement();
			if (currentEditable instanceof TrackWrapper) {
				/**
				 * Ok, at this point we have a TrackWrapper. Now, let's create a Geometry of the
				 * type Simple Features Geotools Library.
				 */
				final TrackWrapper currentTrack = (TrackWrapper) currentEditable;

				/**
				 * Let's draw only visible tracks.
				 */
				if (currentTrack.getVisible()) {

					final GeoPDFLayerTrack newTrackLayer = new GeoPDFLayerTrack();
					mainPage.addLayer(newTrackLayer);

					newTrackLayer.setId(currentTrack.getName());
					newTrackLayer.setName(currentTrack.getName() + NON_INTERACTIVE_SUFFIX);
					javascriptNonInteractiveLayerIndex
							.append('\'' + currentTrack.getName() + NON_INTERACTIVE_SUFFIX + '\'');

					/**
					 * TrackLine
					 */
					createTrackLine(geoPDF.getFilesToDelete(), currentTrack, newTrackLayer, null,
							configuration.getDateFormat());

					// Let's create now the point-type vectors
					/**
					 * Minutes difference Layer
					 */
					createTicksLayer(configuration, geoPDF.getFilesToDelete(), currentTrack, newTrackLayer, null,
							configuration.getDateFormat());

					/**
					 * Label Layer
					 */

					createLabelsLayer(configuration, geoPDF.getFilesToDelete(), currentTrack, newTrackLayer, null,
							configuration.getDateFormat());

					/**
					 * One point layer
					 */
					createTrackNameLayer(configuration, geoPDF.getFilesToDelete(), currentTrack, newTrackLayer, null);

				}

			}

		}
		javascriptNonInteractiveLayerIndex.append("]");

		final StringBuilder javaScriptReplacementJsTimestamps = new StringBuilder();
		javaScriptReplacementJsTimestamps.append("var timestamps = ");
		final ArrayNode jsonTimestamps = mapper.createArrayNode();

		final String jsonTimestampsContent = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonTimestamps);
		javaScriptReplacementJsTimestamps.append(jsonTimestampsContent);
		javaScriptReplacementJsTimestamps.append(";");

		geoPDF.setJavascript(createJavascriptContent(javaScriptReplacementJsTimestamps.toString(),
				javascriptNonInteractiveLayerIndex.toString(), JAVASCRIPT_TEMPLATE_PATH));

		return geoPDF;
	}

	public void createTicksLayer(GeoPDFConfiguration configuration, ArrayList<File> filesToDelete,
			TrackWrapper currentTrack, GeoPDFLayerTrack newTrackLayer, TimePeriod period, String dateFormat)
			throws FileNotFoundException, JsonProcessingException {

		final FixWrapper[] fixes = currentTrack.getFixes();
		for (int i = 0; i < fixes.length; i++) {
			if ((period == null || period.contains(fixes[i].getDTG())) && fixes[i].getSymbolShowing()) {
				final String vectorName = currentTrack.getName() + "_TICKS_"
						+ HiResDateToFileName(fixes[i].getDTG(), dateFormat);
				final SegmentedGeoJSONConfiguration segmentConfiguration = new SegmentedGeoJSONConfiguration(vectorName,
						GeometryType.Point);
				segmentConfiguration.addProperty("elevation", fixes[i].getLocation().getDepth() + "");
				segmentConfiguration.addProperty("longitude", fixes[i].getLocation().getLong() + "");
				segmentConfiguration.addProperty("latitude", fixes[i].getLocation().getLat() + "");
				segmentConfiguration.addProperty("time", HiResDateToFileName(fixes[i].getDTG(), dateFormat));
				segmentConfiguration.addProperty("course", fixes[i].getCourse() + "");
				segmentConfiguration.addProperty("speed", fixes[i].getSpeed() + "");
				segmentConfiguration.addCoordinate(
						new double[] { fixes[i].getLocation().getLong(), fixes[i].getLocation().getLat() });

				final String vectorTickData = GenerateSegmentedGeoJSON.createGeoJsonFixSegment(segmentConfiguration);
				final File tickFile = createTempFile(vectorName + ".geojson", vectorTickData);
				filesToDelete.add(tickFile);

				final GeoPDFLayerVector segmentTickVector = new GeoPDFLayerVector();
				segmentTickVector.setData(tickFile.getAbsolutePath());
				segmentTickVector.setName(vectorName);

				final Color vectorColor = fixes[i].getColor();
				final String colorHex = String.format("#%02x%02x%02x", vectorColor.getRed(), vectorColor.getGreen(),
						vectorColor.getBlue());
				segmentTickVector.setStyle("SYMBOL(c:" + colorHex + ",s:2,id:\"ogr-sym-3\")");

				segmentTickVector.setLogicalStructure(new LogicalStructure(currentTrack.getName(), "time"));

				newTrackLayer.addVector(segmentTickVector);
			}
		}
	}

	@Override
	protected void createLabelsLayer(GeoPDFConfiguration configuration, ArrayList<File> filesToDelete,
			TrackWrapper currentTrack, GeoPDFLayerTrack newTrackLayer, TimePeriod period, String dateFormat)
			throws FileNotFoundException, JsonProcessingException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void createTrackLine(ArrayList<File> filesToDelete, TrackWrapper currentTrack,
			GeoPDFLayerTrack newTrackLayer, TimePeriod period, String dateFormat)
			throws FileNotFoundException, JsonProcessingException {
		final FixWrapper[] fixes = currentTrack.getFixes();
		for (int i = 0; i < fixes.length - 1; i++) {
			if (period == null || period.contains(fixes[i].getDTG())) {
				final String vectorName = currentTrack.getName() + "_LINE_"
						+ HiResDateToFileName(fixes[i].getDTG(), dateFormat);
				final SegmentedGeoJSONConfiguration configuration = new SegmentedGeoJSONConfiguration(vectorName,
						GeometryType.MultiLineString);
				configuration.addProperty("begin", HiResDateToFileName(fixes[i].getDTG(), dateFormat));
				configuration.addProperty("end", HiResDateToFileName(fixes[i + 1].getDTG(), dateFormat));
				configuration.addCoordinate(
						new double[] { fixes[i].getLocation().getLong(), fixes[i].getLocation().getLat() });
				configuration.addCoordinate(
						new double[] { fixes[i + 1].getLocation().getLong(), fixes[i + 1].getLocation().getLat() });

				final String vectorSegmentTrackLineData = GenerateSegmentedGeoJSON
						.createGeoJsonFixSegment(configuration);
				final File trackLineFile = createTempFile(vectorName + ".geojson", vectorSegmentTrackLineData);
				filesToDelete.add(trackLineFile);

				final GeoPDFLayerVector segmentTrackLineVector = new GeoPDFLayerVector();
				segmentTrackLineVector.setData(trackLineFile.getAbsolutePath());
				segmentTrackLineVector.setName(vectorName);

				final Color vectorColor = fixes[i].getColor();
				final String colorHex = String.format("#%02x%02x%02x", vectorColor.getRed(), vectorColor.getGreen(),
						vectorColor.getBlue());
				segmentTrackLineVector.setStyle("PEN(c:" + colorHex + ",w:5px)");

				newTrackLayer.addVector(segmentTrackLineVector);
			}
		}
	}

	@Override
	protected void createTrackNameLayer(GeoPDFConfiguration configuration, ArrayList<File> filesToDelete,
			TrackWrapper currentTrack, GeoPDFLayerTrack newTrackLayer, TimePeriod period)
			throws FileNotFoundException, JsonProcessingException {
		// TODO Auto-generated method stub

	}

}
