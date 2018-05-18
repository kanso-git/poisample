package ch.infofauna.excel;

import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class Utils {


    public void listExcelContents(){
        // getting rge cell contents
        // Use a file
        Workbook wb;

        {
            try {
                URL url = this.getClass().getResource("/F_Q_Klimaprojekt_BAFU_Fauna_Q01-12_20160219.xls");
                File file = new File(url.toURI());
                wb = WorkbookFactory.create(file);

                DataFormatter formatter = new DataFormatter();
                Sheet sheet1 = wb.getSheetAt(0);

                for (Row row : sheet1) {
                    for (Cell cell : row) {
                        System.out.println("Cell RowNum,ColumnIndex is:"+row.getRowNum()+","+cell.getColumnIndex());
                        CellReference cellRef = new CellReference(row.getRowNum(), cell.getColumnIndex());
                        System.out.println("CellReference is:"+cellRef.formatAsString());


                        // get the text that appears in the cell by getting the cell value and applying any data formats (Date, 0.00, 1.23e9, $1.23, etc)
                        // String text = formatter.formatCellValue(cell);
                        // System.out.println(text);

                        // Alternatively, get the value and format it yourself
                        CellType cellType = cell.getCellTypeEnum();
                        if (cellType == CellType.STRING) {
                            System.out.println("STRING");
                            System.out.println(cell.getRichStringCellValue().getString());

                        }
                        if (cellType == CellType.NUMERIC) {
                            System.out.println("NUMERIC");
                            if (DateUtil.isCellDateFormatted(cell)) {
                                System.out.println(cell.getDateCellValue());
                            } else {
                                System.out.println(cell.getNumericCellValue());
                            }
                        }
                        if (cellType == CellType.BOOLEAN) {
                            System.out.println("BOOLEAN");
                            System.out.println(cell.getBooleanCellValue());
                        }
                        if (cellType == CellType.FORMULA) {
                            System.out.println("FORMULA");
                            System.out.println(cell.getCellFormula());
                        }
                        if (cellType == CellType.BLANK) {
                            System.out.println(" cell is blank");
                        }

                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }catch(URISyntaxException e){
                e.printStackTrace();
            }
            catch (InvalidFormatException e) {
                e.printStackTrace();
            }
        }

    }

    // Use an InputStream, needs more memory
   //WorkbookFactory.create(new FileInputStream("MyExcel.xlsx"));


}
