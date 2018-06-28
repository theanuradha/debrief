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
package org.mwc.debrief.core.ContextOperations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.mwc.cmap.core.CorePlugin;
import org.mwc.cmap.core.DataTypes.TrackData.TrackManager;
import org.mwc.cmap.core.operations.CMAPOperation;
import org.mwc.cmap.core.property_support.RightClickSupport.RightClickContextItemGenerator;
import org.mwc.debrief.core.ContextOperations.ExportCSVPrefs.CSVExportDropdownRegistry;
import org.mwc.debrief.core.ContextOperations.ExportCSVPrefs.DropdownProvider;
import org.mwc.debrief.core.ContextOperations.ExportCSVPrefs.ExportCSVPreferencesPage;
import org.mwc.debrief.core.wizards.CSVExportWizard;

import Debrief.Wrappers.FixWrapper;
import Debrief.Wrappers.TrackWrapper;
import MWC.GUI.Editable;
import MWC.GUI.Layer;
import MWC.GUI.Layers;
import MWC.GenericData.HiResDate;
import MWC.GenericData.WatchableList;
import MWC.GenericData.WorldLocation;

/**
 * @author ian.mayo
 */
public class ExportTrackAsCSV implements RightClickContextItemGenerator
{
  public static interface CSVAttributeProvider
  {
    String getCaseNumber();

    String getClassification();

    String getConfidence();

    String getDistributionStatement();

    String getFilePath();

    String getFlag();

    String getInfoCutoffDate();

    String getLikelihood();

    String getMajorAxis();

    String getProvenance();

    String getPurpose();

    String getSemiMajorAxis();

    String getSemiMinorAxis();

    String getSensor();

    String getSuppliedBy();

    String getType();

    String getUnitName();

  }

  private static class ExportTrackToCSV extends CMAPOperation
  {

    private static void performExport(final TrackWrapper subject,
        final CSVAttributeProvider provider)
    {
      FileWriter fos = null;
      try
      {
        final DateFormat fileDateFormat = new SimpleDateFormat(
            "yyyyMMdd_HHmmss", Locale.ENGLISH);

        // sort out the destination filename
        final StringBuffer fileName = new StringBuffer();
        fileName.append(tidyMe(provider.getSuppliedBy()));
        fileName.append("_");
        fileName.append(tidyMe(fileDateFormat.format(new Date())));
        fileName.append("_");
        fileName.append(tidyMe(provider.getUnitName()));
        fileName.append("_");
        fileName.append(tidyMe("" + subject.numFixes()));
        fileName.append("_");
        fileName.append(tidyMe(provider.getClassification()));
        fileName.append(".csv");

        final File outFile = new File(provider.getFilePath(), fileName
            .toString());
        System.out.println("Writing data to:" + outFile.getAbsolutePath());
        fos = new FileWriter(outFile);

        final DateFormat dateFormatter = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm'Z'", Locale.ENGLISH);

        final NumberFormat numF = new DecimalFormat("0.0000");

        final String lineBreak = System.getProperty("line.separator");

        // capture the constants
        final String provenance = provider.getProvenance();
        final String unitName = provider.getUnitName();
        final String caseNumber = provider.getCaseNumber();
        final String infoCutoffDate = provider.getInfoCutoffDate();
        final String suppliedBy = provider.getSuppliedBy();
        final String purpose = provider.getPurpose();
        final String classification = provider.getClassification();
        final String distributionStatement = provider
            .getDistributionStatement();
        final String type = provider.getType();
        final String flag = provider.getFlag();
        final String sensor = provider.getSensor();
        final String majorAxis = provider.getMajorAxis();
        final String semiMajorAxis = provider.getSemiMajorAxis();
        final String semiMinorAxis = provider.getSemiMinorAxis();
        final String likelihood = provider.getLikelihood();
        final String confidence = provider.getConfidence();

        // ok, collate the data
        final StringBuffer lineOut = new StringBuffer();

        // start with the comment markers

        // first the version num
        lineOut.append("# UK TRACK EXCHANGE FORMAT, V1.0");
        lineOut.append(lineBreak);

        // now the fields
        lineOut.append(
            "# provenance,Long,lat,DTG,unitName,caseNumber,infoCutoffDate,suppliedBy,purpose,"
                + "classification,distributionStatement,type,flag,sensor,majorAxis,semiMajorAxis,"
                + "semiMinorAxis,Course,Speed,Depth,likelihood,confidence");
        lineOut.append(lineBreak);

        final Enumeration<Editable> iter = subject.getPositionIterator();
        while (iter.hasMoreElements())
        {
          final FixWrapper next = (FixWrapper) iter.nextElement();
          lineOut.append(provenance);
          lineOut.append(",");
          lineOut.append(write(next.getLocation()));
          lineOut.append(",");
          lineOut.append(write(next.getDTG(), dateFormatter));
          lineOut.append(",");
          lineOut.append(unitName);
          lineOut.append(",");
          lineOut.append(caseNumber);
          lineOut.append(",");
          lineOut.append(infoCutoffDate);
          lineOut.append(",");
          lineOut.append(suppliedBy);
          lineOut.append(",");
          lineOut.append(purpose);
          lineOut.append(",");
          lineOut.append(classification);
          lineOut.append(",");
          lineOut.append(distributionStatement);
          lineOut.append(",");
          lineOut.append(type);
          lineOut.append(",");
          lineOut.append(flag);
          lineOut.append(",");
          lineOut.append(sensor);
          lineOut.append(",");
          lineOut.append(majorAxis);
          lineOut.append(",");
          lineOut.append(semiMajorAxis);
          lineOut.append(",");
          lineOut.append(semiMinorAxis);
          lineOut.append(",");
          lineOut.append(numF.format(MWC.Algorithms.Conversions.Rads2Degs(next
              .getCourse())));
          lineOut.append(",");
          lineOut.append(numF.format(next.getSpeed()));
          lineOut.append(",");
          lineOut.append(next.getLocation().getDepth());
          lineOut.append(",");
          lineOut.append(likelihood);
          lineOut.append(",");
          lineOut.append(confidence);

          // and the newline
          lineOut.append(lineBreak);
        }

        // done.
        fos.write(lineOut.toString());
      }
      catch (final IOException e)
      {
        CorePlugin.logError(IStatus.ERROR,
            "Error while writing to CSV exchange file", e);
      }
      finally
      {
        if (fos != null)
        {
          try
          {
            fos.close();
          }
          catch (final IOException e)
          {
            CorePlugin.logError(IStatus.ERROR,
                "Error while closing CSV exchange file", e);
          }
        }
      }
      System.out.println("File write complete");
    }

    private static String tidyMe(final String input)
    {
      return input.replaceAll("[^\\p{IsAlphabetic}^\\p{IsDigit}]", "_");
    }

    private static String write(final HiResDate dtg,
        final DateFormat dateFormat)
    {
      return dateFormat.format(dtg.getDate());
    }

    private static Object write(final WorldLocation location)
    {
      // TODO: NEED TO CONSIDER NUMBER OF D.P.
      return location.getLong() + ", " + location.getLat();
    }

    /**
     * the parent to update on completion
     */
    private final TrackWrapper _subject;

    public ExportTrackToCSV(final String title, final TrackWrapper subject)
    {
      super(title);
      _subject = subject;
    }

    @Override
    public boolean canRedo()
    {
      return false;
    }

    @Override
    public boolean canUndo()
    {
      return false;
    }

    @Override
    public IStatus execute(final IProgressMonitor monitor,
        final IAdaptable info) throws ExecutionException
    {
      // get the set of fields we need
      final DropdownProvider reg = CSVExportDropdownRegistry.getRegistry();

      // the wizard needs some other data
      final String unit = _subject.getName();

      // see if we can get the primary track
      final IEditorPart editor = CorePlugin.getActiveWindow().getActivePage()
          .getActiveEditor();
      if (editor == null)
      {
        CorePlugin.logError(IStatus.ERROR,
            "Export to CSV couldn't find current editor", null);
        return Status.CANCEL_STATUS;
      }
      final TrackManager trackManager = editor.getAdapter(TrackManager.class);
      final String provenance;
      if (trackManager != null)
      {
        final WatchableList primary = trackManager.getPrimaryTrack();
        if (primary != null)
        {
          provenance = primary.getName();
        }
        else
        {
          provenance = null;
        }
      }
      else
      {
        provenance = null;
      }

      // WIZARD OPENS HERE
      final CSVExportWizard wizard = new CSVExportWizard(reg, unit, provenance);

      final WizardDialog dialog = new WizardDialog(Display.getCurrent()
          .getActiveShell(), wizard);
      dialog.create();
      dialog.open();

      // did it work?
      if (dialog.getReturnCode() == Window.OK)
      {
        final CSVAttributeProvider provider = wizard;

        performExport(_subject, provider);
      }

      // return CANCEL so this event doesn't get put onto the undo buffer,
      // and unnecessarily block the undo queue
      return Status.CANCEL_STATUS;
    }

    @Override
    public IStatus undo(final IProgressMonitor monitor, final IAdaptable info)
        throws ExecutionException
    {
      CorePlugin.logError(IStatus.INFO,
          "Undo not relevant to export Track to CSV", null);
      return null;
    }
  }

  /**
   * @param parent
   * @param theLayers
   * @param parentLayers
   * @param subjects
   */
  @Override
  public void generate(final IMenuManager parent, final Layers theLayers,
      final Layer[] parentLayers, final Editable[] subjects)
  {
    // see if use wants to see this command.
    final IPreferenceStore store = CorePlugin.getDefault().getPreferenceStore();
    final boolean isEnabled = store.getBoolean(
        ExportCSVPreferencesPage.PreferenceConstants.INCLUDE_COMMAND);

    if (!isEnabled)
      return;

    TrackWrapper subject = null;

    // we're only going to work with two or more items
    if (subjects.length == 1)
    {
      final Editable item = subjects[0];
      if (item instanceof TrackWrapper)
      {
        subject = (TrackWrapper) item;
      }
    }

    // ok, is it worth going for?
    if (subject != null)
    {

      // right,stick in a separator
      parent.add(new Separator());

      final String theTitle = "Export Track to CSV Text format";
      final TrackWrapper finalItem = subject;

      // create this operation
      final Action doExport = new Action(theTitle)
      {
        @Override
        public void run()
        {
          final IUndoableOperation theAction = new ExportTrackToCSV(theTitle,
              finalItem);

          CorePlugin.run(theAction);
        }
      };
      parent.add(doExport);
    }
  }

}