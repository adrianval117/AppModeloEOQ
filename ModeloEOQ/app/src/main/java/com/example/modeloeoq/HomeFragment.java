package com.example.modeloeoq;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.poi.hpsf.Constants;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.opencsv.CSVWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class HomeFragment extends Fragment {

    EditText DInput, SInput;
    TextView ResultadoEOQtext;
    CSVWriter csvWriter;
    private HomeFragment ExcelUtils;
    private String csv = "/storage/emulated/0/Android/data/com.example.modeloeoq/data/data.csv";

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final View calculate = view.findViewById(R.id.calculateButton);
        final View erase = view.findViewById(R.id.eraseButton);
        DInput = (EditText) view.findViewById(R.id.DInputText);
        SInput = (EditText) view.findViewById(R.id.SInputText);
        ResultadoEOQtext = (TextView) view.findViewById(R.id.ResultadoEOQtext);

        //Button to calculate
        View.OnClickListener calulator = v -> {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //When permission to write storage is granted
                Toast.makeText(getActivity(), "Calculando...", Toast.LENGTH_SHORT).show();
                calulateEOQ();
            } else {
                //When permission denied
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 44);
            }
        };

        //Button to calculate
        View.OnClickListener eraser = v -> {
            cleanInputs();
        };

        erase.setOnClickListener(eraser);
        calculate.setOnClickListener(calulator);
    }

    private void cleanInputs() {
        DInput.setText("");
        SInput.setText("");
        ResultadoEOQtext.setText("");
    }

    public void calulateEOQ() {
        //Getting values from the inputs
        String dInput_str = DInput.getText().toString();
        String sInput_str = SInput.getText().toString();
        Double D = Double.parseDouble(dInput_str);
        Double S = Double.parseDouble(sInput_str);

        //Answers
        Double EOQ;
        EOQ = D * S;

        //Convert answers to strings
        String EOQ_str = Double.toString(EOQ);
        ResultadoEOQtext.setText(HtmlCompat.fromHtml(EOQ_str, HtmlCompat.FROM_HTML_MODE_LEGACY
        ));

        //Currente date
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        String currentDatetime = dateFormat.format(currentTime);


        //Save inputs and results into a list
        List<String> dataList = new ArrayList<String>();
        dataList.add(currentDatetime + ";" + dInput_str + ";" + sInput_str + ";" + EOQ_str);

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.e("TAG", "External storage not available or it's just readeble");
        }

        //To save with csv
        createFiles();
        boolean isFileCreated = createFiles();
        Log.e("TAG", "CSV file was created succesfully: "+currentDatetime);
        writeCSV(dataList);

        //To save with excel
        /*boolean isExcelGenerated = ExcelUtils.exportDataIntoWorkbook(getActivity(),
                "EXCEL_FILE_NAME.xlsx", dataList);
        Log.e("TAG", "Excel saved in storage succesfully. "+isExcelGenerated);*/
    }


    /**
     * METHOD TO SAVE USING CSV
     */

    private boolean createFiles() {
        boolean isSucces = false;
        File outFile = new File(getActivity().getExternalFilesDir(null).getParent(), "data");
        try {
            if (!outFile.exists()) {
                outFile.mkdirs();
                //It is saved in /data due to csv
                csvWriter = new CSVWriter(new FileWriter(csv));
                String[] headerRow = new String[]{"Fecha del calculo",
                        "Tasa de demanda (D)",
                        "Costo de colocacion de una orden (S)",
                        "EOQ"};
                csvWriter.writeNext(headerRow);
                csvWriter.close();
                isSucces = true;
                Log.e("TAG", "CSV file was generated succesfully.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isSucces;
    }

    private void writeCSV(List<String> dataList) {
        try {
            File file = new File(csv);
            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(String.valueOf(dataList.get(0).split(";")[0] + ","
                    + dataList.get(0).split(";")[1] + ","
                    + dataList.get(0).split(";")[2] + ","
                    + dataList.get(0).split(";")[3] + "\n"));
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * METHOD TO SAVE USING EXCEL
     */


    private static Cell cell;
    private static Sheet sheet;
    private static HSSFWorkbook workbook;
    private static HSSFCellStyle headerCellStyle;

    public static boolean exportDataIntoWorkbook(Context context, String fileName, List<String> dataList) {
        boolean isWorkbookWrittenIntoStorage;

        // Check if available and not read only
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.e("TAG", "External storage not available or it's just readeble");
            return false;
        }

        // Creating a New HSSF Workbook (.xls format)
        workbook = new HSSFWorkbook();

        setHeaderCellStyle();
        // Creating a New Sheet and Setting width for each column
        sheet = workbook.createSheet(fileName);
        sheet.setColumnWidth(0, (15 * 400));
        sheet.setColumnWidth(1, (15 * 400));
        sheet.setColumnWidth(2, (15 * 400));

        setHeaderRow();
        fillDataIntoExcel(dataList);
        isWorkbookWrittenIntoStorage = storeExcelInStorage(context, fileName);
        //isWorkbookWrittenIntoStorage = true;

        return isWorkbookWrittenIntoStorage;
    }

    /*
     * Checks if Storage is READ-ONLY
     *
     * @return boolean
     */

    private static boolean isExternalStorageReadOnly() {
        String externalStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(externalStorageState);
    }

    /*
     * Checks if Storage is Available
     *
     * @return boolean
     */

    private static boolean isExternalStorageAvailable() {
        String externalStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(externalStorageState);
    }

    /*
     * Setup header cell style
     */

    public static void setHeaderCellStyle() {
        headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFillForegroundColor(HSSFColor.AQUA.index);
        headerCellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        headerCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
    }

    /*
     * Setup Header Row
     */

    private static void setHeaderRow() {
        Row headerRow = sheet.createRow(0);

        cell = headerRow.createCell(0);
        cell.setCellValue("Tasa de demanda (D)");
        cell.setCellStyle(headerCellStyle);

        cell = headerRow.createCell(1);
        cell.setCellValue("Costo de colocación de una orden (S)");
        cell.setCellStyle(headerCellStyle);

        cell = headerRow.createCell(2);
        cell.setCellValue("EOQ");
        cell.setCellStyle(headerCellStyle);
    }

    /*
     * Fills Data into Excel Sheet
     * <p>
     * NOTE: Set row index as i+1 since 0th index belongs to header row
     *
     * @param dataList - List containing data to be filled into excel
     */

    private static void fillDataIntoExcel(List<String> dataList) {
        for (int i = 0; i < dataList.size(); i++) {
            // Create a New Row for every new entry in list
            Row rowData = sheet.createRow(i + 1);

            // Create Cells for each row
            cell = rowData.createCell(0);
            cell.setCellValue(dataList.get(i).split(";")[0]);

            cell = rowData.createCell(1);
            cell.setCellValue(dataList.get(i).split(";")[1]);

            cell = rowData.createCell(2);
            cell.setCellValue(dataList.get(i).split(";")[2]);
            Log.e("TAG", "Filling the excel with the data succesfully!");
        }
    }

    /*
     * Store Excel Workbook in external storage
     *
     * @param context  - application context
     * @param fileName - name of workbook which will be stored in device
     * @return boolean - returns state whether workbook is written into storage or not
     */

    private static boolean storeExcelInStorage(Context context, String fileName) {
        boolean isSuccess;

        File file = new File(context.getExternalFilesDir(null), fileName);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            workbook.write(fileOutputStream);
            Log.e("TAG", "Writing file..." + file);
            isSuccess = true;
        } catch (IOException e) {
            Log.e("TAG", "Error writing Exception: ", e);
            isSuccess = false;
        } catch (Exception e) {
            Log.e("TAG", "Failed to save file due to Exception: ", e);
            isSuccess = false;
        } finally {
            try {
                if (null != fileOutputStream) {
                    fileOutputStream.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return isSuccess;
    }

}
