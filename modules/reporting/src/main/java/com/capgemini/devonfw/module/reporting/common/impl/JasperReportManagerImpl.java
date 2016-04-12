package com.capgemini.devonfw.module.reporting.common.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

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

import com.capgemini.devonfw.module.reporting.common.api.ReportManager;
import com.capgemini.devonfw.module.reporting.common.api.dataType.ReportFormat;

/**
 * TODO pparrado This type ...
 *
 * @author pparrado
 * @param <T>
 * @since 1.1
 */
public class JasperReportManagerImpl<T> implements ReportManager<T> {
  private static final Log log = LogFactory.getLog(JasperReportManagerImpl.class);

  private JRDataSource dataSource = null;

  @Override
  public void generateReport(List<T> data, String templatePath, HashMap<String, Object> params, File file,
      ReportFormat format) throws JRException {

    try {
      this.dataSource = JasperUtils.getDataSource(data);
      JRAbstractExporter<?, ?, ExporterOutput, ?> exporter = JasperUtils.getExporter(format);
      JasperDesign design = JRXmlLoader.load(templatePath);
      JasperReport report = JasperCompileManager.compileReport(design);

      JasperPrint jasperPrint = JasperFillManager.fillReport(report, params, this.dataSource);

      FileOutputStream stream = new FileOutputStream(file);

      JasperUtils.configureExporter(exporter, jasperPrint, stream);
      exporter.exportReport();

    } catch (Exception e) {
      log.error(e.getMessage());
    }

  }

  @Override
  public void generateReport(List<T> data, String templatePath, OutputStream stream) {

    // TODO Auto-generated method stub

  }

}
