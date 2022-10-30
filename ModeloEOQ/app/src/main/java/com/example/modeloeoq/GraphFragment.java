package com.example.modeloeoq;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.Toast;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Phaser;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import android.graphics.Color;


public class GraphFragment extends Fragment {

    private String csv = "/storage/emulated/0/Android/data/com.example.modeloeoq/data/data.csv";
    LineChart lineChart;

    public GraphFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_graph, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final View graph = view.findViewById(R.id.graphButton);
        LineChart lineChart = (LineChart) getView().findViewById(R.id.eoqLineChart);

        //Button to start execution.
        View.OnClickListener grapher = v -> {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //When permission to write storage is granted
                Toast.makeText(getActivity(), "Graficando...", Toast.LENGTH_SHORT).show();
                File file = new File(csv);
                Map<String, ArrayList> dataMap;
                Map<String, ArrayList> setsMap = new HashMap();

                //Raw data
                dataMap = getData(file);
                ArrayList<Integer> diasAcumulados = dataMap.get("dias"); //x-data
                ArrayList<Float> nivelInventario = dataMap.get("eoq"); //y-data (EOQ)
                ArrayList<Float> inventarioMedio = dataMap.get("meanInv");  //y-data (inventario medio)
                ArrayList<Float> puntoReorden = dataMap.get("puntoRO");  //y-data (punto de reorden)

                //Sets
                setsMap = prepareDataset(diasAcumulados, nivelInventario, inventarioMedio, puntoReorden);
                ArrayList<ILineDataSet> yLineDataSets = (ArrayList<ILineDataSet>) setsMap.get("y-data");
                ArrayList<String> xLineDataSets = (ArrayList<String>) setsMap.get("x-data");
                Log.e("", "" + xLineDataSets);

                //Plotting
                LineData ylineData = new LineData(yLineDataSets);
                lineChart.setData(ylineData);
                lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
                lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xLineDataSets));
                lineChart.animateY(1000);
                lineChart.setVisibleXRangeMaximum(300F);
                lineChart.getDescription().setEnabled(false);
                lineChart.getAxisRight().setDrawGridLines(false);
                lineChart.getXAxis().setDrawGridLines(false);

            } else {
                //When permission denied
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 45);
            }
        };
        graph.setOnClickListener(grapher);
    }

    private String readCSV(File file) {
        RandomAccessFile fileHandler = null;
        try {
            fileHandler = new RandomAccessFile(file, "r");
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();

            for (long filePointer = fileLength; filePointer != -1; filePointer--) {
                fileHandler.seek(filePointer);
                int readByte = fileHandler.readByte();

                if (readByte == 0xA) {
                    if (filePointer == fileLength) {
                        continue;
                    }
                    break;
                } else if (readByte == 0xD) {
                    if (filePointer == fileLength - 1) {
                        continue;
                    }
                    break;
                }
                sb.append((char) readByte);
            }
            //Reverse the lastline
            String lastLine = sb.reverse().toString();
            return lastLine;
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fileHandler != null)
                try {
                    fileHandler.close();
                } catch (IOException e) {
                    /* ignore */
                }
        }
    }

    private Map<String, ArrayList> getData(File file) {
        //Read the last line from csv file
        String lastLine = readCSV(file);
        Float EOQ = Float.parseFloat(lastLine.split(",")[8]);
        Integer periodoEOQ = Integer.parseInt(lastLine.split(",")[15]);
        Float R = Float.parseFloat(lastLine.split(",")[14]);

        //Building the base period from the periodoEOQ
        ArrayList<Integer> periodoList = new ArrayList<Integer>();
        periodoList.add(0); //First is alwas day 0
        Integer len = (int) Math.floor(periodoEOQ / 10) + 1;
        for (int i = len; i >= 2; i--) {
            periodoList.add(
                    (int) Math.ceil(periodoEOQ / i)
            );
        }
        periodoList.add(periodoEOQ); //Add the last day, which is the periodoEOQ

        //Building the data (days and EOQ level) for the plot
        ArrayList<Integer> diasAcumulados = new ArrayList<Integer>(periodoList); //x-data
        ArrayList<Float> nivelInventario = new ArrayList<Float>(); //y-data (EOQ)
        ArrayList<Float> inventarioMedio = new ArrayList<Float>(); //y-data (inventario medio)
        ArrayList<Float> puntoReorden = new ArrayList<Float>(); //y-data (punto de reorden)
        Integer cantidadCiclos = 4;
        for (int i = 1; i <= cantidadCiclos; i++) {
            for (int j = 0; j < periodoList.toArray().length; j++) {

                //filling the x-data
                if (i < cantidadCiclos) {
                    diasAcumulados.add(
                            periodoList.get(j) + periodoEOQ * i
                    );
                }
                //filling the y-data (EOQ)
                if (j == 0) {
                    nivelInventario.add(
                            EOQ - (EOQ * periodoList.get(j) / periodoEOQ)
                    );
                } else {
                    Float previous = nivelInventario.get(j - 1);
                    nivelInventario.add(
                            previous - (previous * periodoList.get(j) / periodoEOQ)
                    );
                }
                //filling the y-data (inventario medio)
                inventarioMedio.add(
                        EOQ / 2
                );
                //filling the y-data (punto de reorden R)
                puntoReorden.add(
                        R
                );
            }
        }
        //Send data back
        Map<String, ArrayList> map = new HashMap();
        map.put("dias", diasAcumulados);
        map.put("eoq", nivelInventario);
        map.put("meanInv", inventarioMedio);
        map.put("puntoRO", puntoReorden);
        return map;
    }

    private Float maxValue(ArrayList<Float> nivelInventario) {
        float max = nivelInventario.get(0);
        for (int i = 0; i < nivelInventario.toArray().length; i++) {
            if (nivelInventario.get(i) > max) {
                max = nivelInventario.get(i);
            }
        }
        return max;
    }

    private Map<String, ArrayList> prepareDataset(ArrayList<Integer> diasAcumulados, ArrayList<Float> nivelInventario, ArrayList<Float> inventarioMedio, ArrayList<Float> puntoReorden) {
        //General datasets
        ArrayList<String> xAXES = new ArrayList<String>();
        ArrayList<Entry> nivelInventarioEntry = new ArrayList<Entry>(); //y-data (nivel inventario)
        ArrayList<Entry> inventarioMedioEntry = new ArrayList<Entry>(); //y-data (inventario medio)
        ArrayList<Entry> puntoReordenEntry = new ArrayList<Entry>(); //y-data (R)
        for (int i = 0; i < diasAcumulados.toArray().length; i++) {
            xAXES.add(i, String.valueOf(diasAcumulados.get(i)));
            nivelInventarioEntry.add(new Entry(i, nivelInventario.get(i)));
            inventarioMedioEntry.add(new Entry(i, inventarioMedio.get(i)));
            puntoReordenEntry.add(new Entry(i, puntoReorden.get(i)));
        }

        LineDataSet EOQYset = new LineDataSet(nivelInventarioEntry, "EOQ (" + String.valueOf((maxValue(nivelInventario)) + ")"));
        EOQYset.setDrawCircles(false);
        EOQYset.setColor(Color.BLUE);
        EOQYset.setLineWidth(2);

        LineDataSet inventarioMedioYset = new LineDataSet(inventarioMedioEntry,
                "Inventario medio (" + String.valueOf(inventarioMedio.get(0) + ")"));
        inventarioMedioYset.setDrawCircles(false);
        inventarioMedioYset.setDrawValues(false);
        inventarioMedioYset.setColor(Color.RED);

        LineDataSet puntoReordenYset = new LineDataSet(puntoReordenEntry,
                "Punto de reorden (" + String.valueOf(puntoReorden.get(0)) + ")");
        puntoReordenYset.setColor(Color.GREEN);
        puntoReordenYset.setDrawCircles(false);
        puntoReordenYset.setDrawValues(false);

        //Datasets
        ArrayList<ILineDataSet> yLineDataSets = new ArrayList<ILineDataSet>();
        yLineDataSets.add(EOQYset);
        yLineDataSets.add(inventarioMedioYset);
        yLineDataSets.add(puntoReordenYset);

        //Send data back
        Map<String, ArrayList> map = new HashMap();
        map.put("y-data", yLineDataSets);
        map.put("x-data", xAXES);
        return map;
    }


}
