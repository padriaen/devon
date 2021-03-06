/*******************************************************************************
 * Copyright 2015-2018 Capgemini SE.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package com.devonfw.module.reporting.common.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.ExporterOutput;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.devonfw.module.reporting.common.api.Reporting;
import com.devonfw.module.reporting.common.api.dataType.ReportFormat;
import com.devonfw.module.reporting.common.api.entity.Report;
import com.devonfw.module.reporting.common.exception.ReportingException;

/**
 * This is the simple implementation of {@link Reporting}
 *
 * @author pparrado
 * @param <T> type of the data that is provided to be included in the report.
 */
@Named
public class ReportingJasperImpl<T> implements Reporting<T> {

  @Inject
  private JasperUtils utils;

  private static final Log log = LogFactory.getLog(ReportingJasperImpl.class);

  private JRDataSource dataSource = null;

  /**
   * the start index for each report in concatenated reports
   */
  public static String PAGES_INIT_NUM = "PAGES_INIT_NUM";

  @Override
  public void generateReport(Report report, File file, ReportFormat format) {

    FileOutputStream stream = null;

    try {

      this.dataSource = this.utils.getDataSource(report.getData());
      JRAbstractExporter<?, ?, ExporterOutput, ?> exporter = this.utils.getExporter(format);
      JasperDesign design = JRXmlLoader.load(report.getTemplatePath());
      JasperReport jreport = JasperCompileManager.compileReport(design);
      JasperPrint jasperPrint = JasperFillManager.fillReport(jreport, report.getParams(), this.dataSource);

      stream = new FileOutputStream(file);
      this.utils.configureExporter(exporter, jasperPrint, stream, format);
      exporter.exportReport();

    } catch (ReportingException | JRException | IOException e) {
      log.error("An error occurred while trying to create the report: " + e.getMessage(), e);
      throw new ReportingException(e, e.getMessage());

    } finally {
      try {
        if (stream != null)
          stream.close();
      } catch (IOException ioex) {
        throw new ReportingException(ioex,
            "The stream associated to the temp file could not be closed. " + ioex.getMessage());
      }
    }

  }

  @Override
  public void generateReport(Report report, OutputStream stream, ReportFormat format) {

    try {
      this.dataSource = this.utils.getDataSource(report.getData());
      JRAbstractExporter<?, ?, ExporterOutput, ?> exporter = this.utils.getExporter(format);
      JasperDesign design = JRXmlLoader.load(report.getTemplatePath());
      JasperReport jreport = JasperCompileManager.compileReport(design);
      JasperPrint jasperPrint = JasperFillManager.fillReport(jreport, report.getParams(), this.dataSource);
      this.utils.configureExporter(exporter, jasperPrint, stream, format);
      exporter.exportReport();
    } catch (ReportingException | JRException e) {
      log.error("An error occurred while trying to create the report. " + e.getMessage(), e);
      throw new ReportingException(e, e.getMessage());
    }

  }

  @SuppressWarnings("rawtypes")
  @Override
  public void generateSubreport(Report master, List<Report> subs, File file, ReportFormat format) {

    FileOutputStream stream = null;

    try {

      Map<String, Object> subReportsParams = new HashMap<>();

      JasperDesign design = null;
      JasperReport subReport = null;
      JasperPrint jasperPrint = null;
      JRDataSource subDataSource = null;

      Map<String, Object> masterParams = master.getParams();
      this.dataSource = this.utils.getDataSource(master.getData());
      JRAbstractExporter exporter = this.utils.getExporter(format);
      JasperDesign masterDesign = JRXmlLoader.load(master.getTemplatePath());
      JasperReport masterReport = JasperCompileManager.compileReport(masterDesign);

      for (Report sub : subs) {
        subDataSource = this.utils.getDataSource(sub.getData());
        design = JRXmlLoader.load(sub.getTemplatePath());
        subReport = JasperCompileManager.compileReport(design);
        subReportsParams.put(sub.getName(), subReport);
        subReportsParams.put(sub.getDataSourceName(), subDataSource);
      }

      masterParams.putAll(subReportsParams);
      jasperPrint = JasperFillManager.fillReport(masterReport, masterParams, this.dataSource);

      stream = new FileOutputStream(file);
      this.utils.configureExporter(exporter, jasperPrint, stream, format);
      exporter.exportReport();

    } catch (ReportingException | JRException | IOException e) {
      log.error("An error occurred while trying to create the subreport. " + e.getMessage());
      throw new ReportingException(e, e.getMessage());
    } finally {
      try {
        if (stream != null)
          stream.close();
      } catch (IOException ioex) {
        throw new ReportingException(ioex,
            "The stream associated to the temp file could not be closed. " + ioex.getMessage());
      }
    }
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void generateSubreport(Report master, List<Report> subs, OutputStream stream, ReportFormat format) {

    try {

      Map<String, Object> subReportsParams = new HashMap<>();

      JasperDesign design = null;
      JasperReport subReport = null;
      JasperPrint jasperPrint = null;
      JRDataSource subDataSource = null;

      Map<String, Object> masterParams = master.getParams();
      this.dataSource = this.utils.getDataSource(master.getData());
      JRAbstractExporter exporter = this.utils.getExporter(format);
      JasperDesign masterDesign = JRXmlLoader.load(master.getTemplatePath());
      JasperReport masterReport = JasperCompileManager.compileReport(masterDesign);

      for (Report sub : subs) {
        subDataSource = this.utils.getDataSource(sub.getData());
        design = JRXmlLoader.load(sub.getTemplatePath());
        subReport = JasperCompileManager.compileReport(design);
        subReportsParams.put(sub.getName(), subReport);
        subReportsParams.put(sub.getDataSourceName(), subDataSource);
      }

      masterParams.putAll(subReportsParams);
      jasperPrint = JasperFillManager.fillReport(masterReport, masterParams, this.dataSource);

      this.utils.configureExporter(exporter, jasperPrint, stream, format);
      exporter.exportReport();

    } catch (ReportingException | JRException e) {
      log.error("An error occurred while trying to create the subreport. " + e.getMessage());
      throw new ReportingException(e, e.getMessage());
    }

  }

  @SuppressWarnings("rawtypes")
  @Override
  public void concatenateReports(List<Report> reports, File file, ReportFormat format) {

    FileOutputStream stream = null;
    List<JasperPrint> printList = new ArrayList<>();
    try {
      JRAbstractExporter exporter = this.utils.getExporter(format);

      int numPages = 0;
      for (Report<?> report : reports) {
        HashMap<String, Object> params = new HashMap<>();
        params.put(PAGES_INIT_NUM, numPages);
        params.putAll(report.getParams());
        report.setParams(params);
        JasperDesign design = JRXmlLoader.load(report.getTemplatePath());
        JasperReport jasperReport = JasperCompileManager.compileReport(design);
        JasperPrint jasperPrint =
            JasperFillManager.fillReport(jasperReport, report.getParams(), this.utils.getDataSource(report.getData()));
        printList.add(jasperPrint);

        numPages += jasperPrint.getPages().size();
      }

      stream = new FileOutputStream(file);
      this.utils.configureExporter(exporter, printList, stream, format);
      exporter.exportReport();

    } catch (Exception e) {
      log.error("An error occurred while trying to create the concatenated report: " + e.getMessage(), e);
      throw new ReportingException(e, e.getMessage());
    } finally {
      try {
        if (stream != null)
          stream.close();
      } catch (IOException ioex) {
        throw new ReportingException(ioex,
            "The stream associated to the temp file for the concatenated report could not be closed. "
                + ioex.getMessage());
      }
    }

  }

  @SuppressWarnings("rawtypes")
  @Override
  public void concatenateReports(List<Report> reports, OutputStream stream, ReportFormat format) {

    List<JasperPrint> printList = new ArrayList<>();
    try {
      JRAbstractExporter exporter = this.utils.getExporter(format);

      int numPages = 0;
      for (Report<?> report : reports) {
        HashMap<String, Object> params = new HashMap<>();
        params.put(PAGES_INIT_NUM, numPages);
        params.putAll(report.getParams());
        report.setParams(params);
        JasperDesign design = JRXmlLoader.load(report.getTemplatePath());
        JasperReport jasperReport = JasperCompileManager.compileReport(design);
        JasperPrint jasperPrint =
            JasperFillManager.fillReport(jasperReport, report.getParams(), this.utils.getDataSource(report.getData()));
        printList.add(jasperPrint);

        numPages += jasperPrint.getPages().size();
      }

      this.utils.configureExporter(exporter, printList, stream, format);
      exporter.exportReport();

    } catch (Exception e) {
      log.error("An error occurred while trying to create the concatenated report: " + e.getMessage(), e);
      throw new ReportingException(e, e.getMessage());
    }
  }

}
