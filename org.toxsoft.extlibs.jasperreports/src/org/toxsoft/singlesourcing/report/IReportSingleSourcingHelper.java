package org.toxsoft.singlesourcing.report;

import org.eclipse.swt.widgets.Shell;

import com.jasperassistant.designer.viewer.IReportViewer;
import com.jasperassistant.designer.viewer.actions.ExportAsPdfAction;
import com.jasperassistant.designer.viewer.actions.ExportAsSingleXlsAction;

import net.sf.jasperreports.engine.JasperPrint;

/**
 * Вспомогательные методы, по-разному реализуемые в RCP и RAP.
 * <p>
 * Ссылка на этот интерфейс передается в контексте приложения "запускальщиком" RAP и RCP приложений.
 *
 * @author mvk
 */
public interface IReportSingleSourcingHelper {

  /**
   * В RCP выводит диалог и возвраащает имя PDF-файла.
   * <p>
   * В RAP выводит возвращает имя определяемое реализацией.
   *
   * @param aParent {@link Shell} -родительское окно диалога
   * @return String - имя файла. Пустая строка: отказ от операции
   */
  String getPdfFileName( Shell aParent );

  /**
   * В RCP выводит диалог и возвраащает имя XLS-файла.
   * <p>
   * В RAP выводит возвращает имя определяемое реализацией.
   *
   * @param aParent {@link Shell} -родительское окно диалога
   * @return String - имя файла. Пустая строка: отказ от операции
   */
  String getXlsFileName( Shell aParent );

  /**
   * Экспорт отчета в pdf-файл
   *
   * @param aReport {@link JasperPrint} отчет
   * @param aFileName String имя файла
   */
  void saveReportToPdf( JasperPrint aReport, String aFileName );

  /**
   * Экспорт отчета в xls-файл
   *
   * @param aReport {@link JasperPrint} отчет
   * @param aFileName String имя файла
   */
  void saveReportToXls( JasperPrint aReport, String aFileName );

  // ------------------------------------------------------------------------------------
  // Действия (Action)
  //
  /**
   * Формирует действие для UI: "экспорт в pdf"
   *
   * @param aViewer {@link IReportViewer} окно отчета
   * @return {@link ExportAsSingleXlsAction} действие
   */
  ExportAsPdfAction createExportAsPdfAction( IReportViewer aViewer );

  /**
   * Формирует действие для UI: "экспорт в xls"
   *
   * @param aViewer {@link IReportViewer} окно отчета
   * @return {@link ExportAsSingleXlsAction} действие
   */
  ExportAsSingleXlsAction createExportAsSingleXlsAction( IReportViewer aViewer );
}
