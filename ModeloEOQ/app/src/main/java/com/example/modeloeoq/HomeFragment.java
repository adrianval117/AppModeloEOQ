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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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
import java.text.DecimalFormat;
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

    EditText DInput, SInput, CInput, iInput, diasInput, LInput;
    TextView ResultadoEOQText, ResultadoOrdenesText, ResultadoCostoMantenimientoText, ResultadoTRCText;
    TextView ResultadoNText, ResultadoTText, ResultadoRText, ResultadoPeriodoEOQText;
    TextView tasaMantenimientoLabel;
    CSVWriter csvWriter;
    Switch cancelIC;
    Boolean isCheckedOn;
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
        cancelIC = (Switch) view.findViewById(R.id.swNoiC);
        final View calculate = view.findViewById(R.id.calculateButton);
        final View erase = view.findViewById(R.id.eraseButton);
        DInput = (EditText) view.findViewById(R.id.DInputText);
        SInput = (EditText) view.findViewById(R.id.SInputText);
        CInput = (EditText) view.findViewById(R.id.CInputText);
        iInput = (EditText) view.findViewById(R.id.IInputText);
        diasInput = (EditText) view.findViewById(R.id.diasHabilesInputText);
        LInput = (EditText) view.findViewById(R.id.tiempoEntregaProveedorInputText);
        ResultadoEOQText = (TextView) view.findViewById(R.id.ResultadoEOQtext);
        ResultadoOrdenesText = (TextView) view.findViewById(R.id.ResultadoCostoOrdenesText);
        ResultadoCostoMantenimientoText = (TextView) view.findViewById(R.id.ResultadoCostoMantenimientoText);
        ResultadoTRCText = (TextView) view.findViewById(R.id.ResultadoTRCText);
        ResultadoNText = (TextView) view.findViewById(R.id.ResultadoNText);
        ResultadoTText = (TextView) view.findViewById(R.id.ResultadoTText);
        ResultadoRText = (TextView) view.findViewById(R.id.ResultadoRText);
        ResultadoPeriodoEOQText = (TextView) view.findViewById(R.id.ResultadoPeriodoEOQText);
        tasaMantenimientoLabel = (TextView) view.findViewById(R.id.TasaMantenimientoLabel);

        //Check if C or i are present
        isCheckedOn = false;
        CompoundButton.OnCheckedChangeListener sw = (View, isChecked) -> {
            if (isChecked) {
                isCheckedOn = true;
                Toast.makeText(getActivity(), "La tasa de mantenimiento no está habilitada", Toast.LENGTH_SHORT).show();
            } else {
                isCheckedOn = false;
                Toast.makeText(getActivity(), "La tasa de mantenimiento está habilitada", Toast.LENGTH_SHORT).show();
            }
            chekModeiC();
        };

        //Button to calculate
        View.OnClickListener calulator = v -> {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //When permission to write storage is granted
                checkEditsFields();
            } else {
                //When permission denied
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 44);
            }
        };

        //Button to erase the data
        View.OnClickListener eraser = v -> {
            cleanInputs();
        };

        cancelIC.setOnCheckedChangeListener(sw);
        calculate.setOnClickListener(calulator);
        erase.setOnClickListener(eraser);
    }

    private void cleanInputs() {
        DInput.setText("");
        SInput.setText("");
        CInput.setText("");
        iInput.setText("");
        diasInput.setText("");
        LInput.setText("");
        ResultadoEOQText.setText("");
        ResultadoOrdenesText.setText("");
        ResultadoCostoMantenimientoText.setText("");
        ResultadoTRCText.setText("");
        ResultadoNText.setText("");
        ResultadoTText.setText("");
        ResultadoRText.setText("");
        ResultadoPeriodoEOQText.setText("");
    }

    private void chekModeiC() {
        //If the no C mode is switched, set C = 0 and change the label to H ($) mode
        if (isCheckedOn) {
            CInput.setText("0");
            CInput.setFocusable(false);
            tasaMantenimientoLabel.setText("Costo de mantenimiento anual");
            iInput.setHint("H ($)");
        } else {
            CInput.setText("");
            CInput.setFocusableInTouchMode(true);
            tasaMantenimientoLabel.setText("Tasa de mantenimiento (%)");
            iInput.setHint("i");
        }
    }

    private void checkEditsFields() {
        //Getting values from the inputs
        String dInput_str = DInput.getText().toString();
        String sInput_str = SInput.getText().toString();
        String CInput_str = CInput.getText().toString();
        String iInput_str = iInput.getText().toString();
        String diasInput_str = diasInput.getText().toString();
        String LInput_str = LInput.getText().toString();

        if (dInput_str.matches("") || sInput_str.matches("") ||
                CInput_str.matches("") || iInput_str.matches("") ||
                diasInput_str.matches("") || LInput_str.matches("")) {
            Toast.makeText(getActivity(), "Se deben llenar todos los campos", Toast.LENGTH_SHORT).show();
        } else {
            Double D = Double.parseDouble(dInput_str);
            Double S = Double.parseDouble(sInput_str);
            Double C = Double.parseDouble(CInput_str);
            Double i = Double.parseDouble(iInput_str);
            Double diasLaborales = Double.parseDouble(diasInput_str);
            Double L = Double.parseDouble(LInput_str);
            calulateEOQ(D, S, C, i, diasLaborales, L);
        }
    }

    public void calulateEOQ(Double D, Double S, Double C, Double i, Double diasLaborales, Double L) {

        //If it makit till here, then it's ready to calculate
        Toast.makeText(getActivity(), "Calculando...", Toast.LENGTH_SHORT).show();

        //Answers
        /*If you are not given the unit cost (C), you must enter a 0 in the field.
        In addition, the field associated with the maintenance rate (i) must be changed
        to the cost of the maintenance (H) in monetary units*/
        Double H;
        if (isCheckedOn) {
            H = i;
        } else {
            H = C * i;
            Log.e("", "" + H);
        }

        //Left columns results
        Double EOQ = Math.ceil(Math.pow((2 * D * S) / H, 0.5));
        Double ordenes = D * S / EOQ;
        Double mant = EOQ * H / 2;
        Double TRC = D * C + mant + ordenes;

        //Right columns results
        Double N = Math.ceil(D / EOQ);
        Double T = Math.ceil(diasLaborales / N);
        Double d = Math.ceil(D / diasLaborales);
        Double R = Math.ceil(d * L);
        Double periodoEOQ = EOQ / d;

        //Convert answers to strings
        //inputs
        String dInput_str = Double.toString(D);
        String sInput_str = Double.toString(S);
        String CInput_str = Double.toString(C);
        String iInput_str = Double.toString(i);
        String diasInput_str = Double.toString(diasLaborales);
        String LInput_str = Double.toString(L);
        String hInput_str = Double.toString(H);

        //Results
        DecimalFormat round = new DecimalFormat("0.00");
        String EOQ_str = Integer.toString(EOQ.intValue());
        String ordenes_str = Double.toString(Double.parseDouble(round.format(ordenes)));
        String mant_str = Double.toString(Double.parseDouble(round.format(mant)));
        String TRC_str = Double.toString(Double.parseDouble(round.format(TRC)));
        String N_str = Integer.toString(N.intValue());
        String T_str = Integer.toString(T.intValue());
        String R_str = Integer.toString(R.intValue());
        String periodoEOQ_str = Integer.toString(periodoEOQ.intValue());

        //Show results in the TextViews
        ResultadoEOQText.setText(HtmlCompat.fromHtml(EOQ_str + " unds", HtmlCompat.FROM_HTML_MODE_LEGACY
        ));
        ResultadoOrdenesText.setText(HtmlCompat.fromHtml("$" + ordenes_str, HtmlCompat.FROM_HTML_MODE_LEGACY
        ));
        ResultadoCostoMantenimientoText.setText(HtmlCompat.fromHtml("$" + mant_str, HtmlCompat.FROM_HTML_MODE_LEGACY
        ));
        ResultadoTRCText.setText(HtmlCompat.fromHtml("$" + TRC_str, HtmlCompat.FROM_HTML_MODE_LEGACY
        ));
        ResultadoNText.setText(HtmlCompat.fromHtml(N_str + " unds", HtmlCompat.FROM_HTML_MODE_LEGACY
        ));
        ResultadoTText.setText(HtmlCompat.fromHtml(T_str + " días", HtmlCompat.FROM_HTML_MODE_LEGACY
        ));
        ResultadoRText.setText(HtmlCompat.fromHtml(R_str + " unds", HtmlCompat.FROM_HTML_MODE_LEGACY
        ));
        ResultadoPeriodoEOQText.setText(HtmlCompat.fromHtml(periodoEOQ_str + " días", HtmlCompat.FROM_HTML_MODE_LEGACY
        ));

        //Get current date to save record in the CSV
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        String currentDatetime = dateFormat.format(currentTime);

        //Save inputs and results into a list
        List<String> dataList = new ArrayList<String>();
        dataList.add(currentDatetime + ";" + dInput_str + ";" + sInput_str + ";" + CInput_str + ";" //Inputs
                + iInput_str + ";" + hInput_str + ";" + diasInput_str + ";" + LInput_str + ";"
                + EOQ_str + ";" + ordenes_str + ";" + mant_str + ";" + TRC_str + ";" //Outputs
                + N_str + ";" + T_str + ";" + R_str + ";" + periodoEOQ_str);

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.e("TAG", "External storage not available or it's just readeble");
        }

        //To save with csv
        createFiles();
        boolean isFileCreated = createFiles();
        writeCSV(dataList);
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
                        "Tasa de demanda (D)",                      //Inputs
                        "Costo de colocacion de una orden (S)",
                        "Costo total unitario (C)",
                        "Tasa de mantenimiento (i)",
                        "Costo anual de mantenimiento (H)",
                        "Dias habiles anuales",
                        "Tiempo de entrega proveedor (L)",
                        "EOQ",                                      //Outputs
                        "Costo anual de colocar ordenes (Ordenes)",
                        "Costo anual de mantenimiento de invetario (Mant.)",
                        "Costo total relevante (TRC)",
                        "Numero de ordenes colocadas anuales (N)",
                        "Tiempo entre cada orden (T)",
                        "Punto de reorden (R)",
                        "Periodo de consumo del EOQ"};
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
            String a, b;
            if (isCheckedOn) {
                a = "N/A";
                b = "N/A";
            } else {
                a = dataList.get(0).split(";")[3];
                b = dataList.get(0).split(";")[4];
            }
            bw.write(String.valueOf(dataList.get(0).split(";")[0] + ","
                    + dataList.get(0).split(";")[1] + ","       //Inputs
                    + dataList.get(0).split(";")[2] + ","
                    + a + ","
                    + b + ","
                    + dataList.get(0).split(";")[5] + ","
                    + dataList.get(0).split(";")[6] + ","
                    + dataList.get(0).split(";")[7] + ","       //Ouputs
                    + dataList.get(0).split(";")[8] + ","
                    + dataList.get(0).split(";")[9] + ","
                    + dataList.get(0).split(";")[10] + ","
                    + dataList.get(0).split(";")[11] + ","
                    + dataList.get(0).split(";")[12] + ","
                    + dataList.get(0).split(";")[13] + ","
                    + dataList.get(0).split(";")[14] + ","
                    + dataList.get(0).split(";")[15] + "\n"));
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
