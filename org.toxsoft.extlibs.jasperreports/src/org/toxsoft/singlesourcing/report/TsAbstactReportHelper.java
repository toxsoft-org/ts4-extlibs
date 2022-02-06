package org.toxsoft.singlesourcing.report;

import static java.util.Objects.*;
import static org.toxsoft.singlesourcing.report.ITsResources.*;

import javax.management.RuntimeErrorException;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JRDesignStyle;

/**
 * Абстрактная реализация {@link IReportSingleSourcingHelper}
 *
 * @author mvk
 */
public abstract class TsAbstactReportHelper
    implements IReportSingleSourcingHelper {

  /**
   * Имя файла для экспортируемых отчетов
   */
  private static final String REPORT_FILENAME = "report"; //$NON-NLS-1$

  /**
   * Расширение pdf-файлов
   */
  public static final String PDF_FILE_EXTENSION = ".pdf"; //$NON-NLS-1$

  /**
   * Расширение xlsx-файлов
   */
  public static final String XLS_FILE_EXTENSION = ".xlsx"; //$NON-NLS-1$

  /**
   * Счетчик сформированных отчетов
   */
  public int reportCount;

  protected TsAbstactReportHelper() {
    // JasperReportsContext jrc = DefaultJasperReportsContext.getInstance();
    // jrc.setProperty( JRFont.DEFAULT_FONT_NAME, "arial" );
    // jrc.setProperty( JRFont.DEFAULT_PDF_ENCODING, "UTF-8" );
    // jrc.setProperty( JRFont.DEFAULT_PDF_EMBEDDED, "false" );
    // jrc.setProperty( JRStyledText.PROPERTY_AWT_IGNORE_MISSING_FONT, "true" );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IReportSingleSourcingHelper
  //
  @Override
  public final void saveReportToPdf( JasperPrint aReport, String aFileName ) {
    requireNonNull( aReport );
    requireNonNull( aFileName );
    String filename = aFileName;
    if( filename.toLowerCase().endsWith( PDF_FILE_EXTENSION ) == false ) {
      filename += PDF_FILE_EXTENSION;
    }
    try {
      doSaveReportToPdf( aReport, aFileName );
      reportCount++;
    }
    catch( Throwable e ) {
      String cause = e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "???"; //$NON-NLS-1$
      throw new RuntimeErrorException( new Error( e ), String.format( MSG_ERR_EXPORT, aFileName, cause ) );
    }
  }

  @Override
  public final void saveReportToXls( JasperPrint aReport, String aFileName ) {
    requireNonNull( aReport );
    requireNonNull( aFileName );
    String filename = aFileName;
    if( filename.toLowerCase().endsWith( XLS_FILE_EXTENSION ) == false ) {
      filename += XLS_FILE_EXTENSION;
    }
    try {
      doSaveReportToXls( aReport, aFileName );
      reportCount++;
    }
    catch( Throwable e ) {
      String cause = e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "???"; //$NON-NLS-1$
      throw new RuntimeErrorException( new Error( e ), String.format( MSG_ERR_EXPORT, aFileName, cause ) );
    }
  }

  // ------------------------------------------------------------------------------------
  // Методы для наследников
  //
  /**
   * Возвращает количество уже сформированных отчетов
   *
   * @return int количество уже сформированных отчетов
   */
  protected final int reportCount() {
    return reportCount;
  }

  /**
   * Имя файла для нового отчета в формате pdf
   *
   * @return String имя файла
   */
  protected final String createPdfFileName() {
    return REPORT_FILENAME + String.valueOf( reportCount() ) + PDF_FILE_EXTENSION;
  }

  /**
   * Имя файла для нового отчета в формате xls
   *
   * @return String имя файла
   */
  protected final String createXlsFileName() {
    return REPORT_FILENAME + String.valueOf( reportCount() ) + XLS_FILE_EXTENSION;
  }

  /**
   * Возвращает стиль по умолчанию для pdf-отчетов
   *
   * @return {@link JRDesignStyle} стиль
   */
  @SuppressWarnings( "nls" )
  protected final static JRDesignStyle getDefaultPdfStyle() {
    JRDesignStyle normalStyle = new JRDesignStyle();
    normalStyle.setName( "Arial_Normal" );
    normalStyle.setDefault( true );
    normalStyle.setFontName( "Serif" );
    normalStyle.setFontSize( Float.valueOf( 8 ) );
    normalStyle.setPdfFontName( "arial.ttf" );
    normalStyle.setPdfEncoding( "Cp1251" );
    normalStyle.setPdfEmbedded( false );
    JRLineBox lineBox = normalStyle.getLineBox();
    lineBox.getTopPen().setLineWidth( 0.5f );
    lineBox.getRightPen().setLineWidth( 0.5f );
    lineBox.getBottomPen().setLineWidth( 0.5f );
    lineBox.getLeftPen().setLineWidth( 0.5f );
    return normalStyle;
  }

  // ------------------------------------------------------------------------------------
  // Методы для реализации наследниками
  //
  /**
   * Экспорт отчета в pdf-формат
   *
   * @param aReport {@link JasperPrint} отчет
   * @param aFileName String имя файла
   * @throws JRException ошибка формирования отчета
   */
  protected abstract void doSaveReportToPdf( JasperPrint aReport, String aFileName )
      throws JRException;

  /**
   * Экспорт отчета в xls-формат
   *
   * @param aReport {@link JasperPrint} отчет
   * @param aFileName String имя файла
   * @throws JRException ошибка формирования отчета
   */
  protected abstract void doSaveReportToXls( JasperPrint aReport, String aFileName )
      throws JRException;
}
