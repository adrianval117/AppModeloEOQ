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

import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;


public class GraphFragment extends Fragment {

    private String csv = "/storage/emulated/0/Android/data/com.example.modeloeoq/data/data.csv";

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

        View.OnClickListener grapher = v -> {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //When permission to write storage is granted
                Toast.makeText(getActivity(), "Graficando...", Toast.LENGTH_SHORT).show();
                File file = new File(csv);
                getData(file);
            } else {
                //When permission denied
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 45);
            }
        };

        graph.setOnClickListener(grapher);
    }

    private void getData(File file){
        //Read the last line from csv file
        String lastLine = readCSV(file);

        Double EOQ = Double.parseDouble(lastLine.split(",")[8]);
        Integer periodoEOQ = Integer.parseInt(lastLine.split(",")[15]);
        Integer R = Integer.parseInt(lastLine.split(",")[14]);

        //Building the base period from the periodoEOQ
        List<Integer> periodoList = new ArrayList<Integer>();
        periodoList.add(0); //First is alwas day 0
        Integer len = (int) Math.floor(periodoEOQ/10) + 1;
        for(int i = len; i >= 2; i--){
            periodoList.add(
                    (int) Math.ceil(periodoEOQ/i)
            );
        }
        periodoList.add(periodoEOQ); //Add the last day, which is the periodoEOQ

        //Building the data (days and EOQ level) for the plot
        List<Integer> diasAcumulados = new ArrayList<>(periodoList); //x-data
        List<Double> nivelInventario = new ArrayList<Double>(); //y-data (EOQ)
        List<Double> inventarioMedio = new ArrayList<Double>(); //y-data (inventario medio)
        List<Integer> puntoReorden = new ArrayList<Integer>(); //y-data (inventario medio)
        Integer cantidadCiclos = 4;
        for(int i = 1; i <= cantidadCiclos; i++){
            for(int j = 0; j < periodoList.toArray().length; j++){

                //filling the x-data
                diasAcumulados.add(
                        periodoList.get(j) + periodoEOQ*i
                );

                //filling the y-data (EOQ)
                if(j == 0){
                    nivelInventario.add(
                            EOQ - (EOQ*periodoList.get(j)/periodoEOQ)
                    );
                }else{
                    Double previous = nivelInventario.get(j-1);
                    nivelInventario.add(
                            previous - (previous*periodoList.get(j)/periodoEOQ)
                    );
                }

                //filling the y-data (inventario medio)
                inventarioMedio.add(
                        EOQ/2
                );

                //filling the y-data (punto de reorden R)
                puntoReorden.add(
                        R
                );

            }
        }

        for(int j = 0; j < nivelInventario.toArray().length; j++){
            Log.e("", "x: "+diasAcumulados.get(j)+"\t " +
                                "y: "+nivelInventario.get(j)+"\t" +
                                "Inventario medio: "+inventarioMedio.get(j)+"\t" +
                                "R: "+puntoReorden.get(j));
        }

        //Send data to plot function
        //plot();
    }

    private String readCSV(File file){
        RandomAccessFile fileHandler = null;
        try {
            fileHandler = new RandomAccessFile( file, "r" );
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();

            for(long filePointer = fileLength; filePointer != -1; filePointer--){
                fileHandler.seek( filePointer );
                int readByte = fileHandler.readByte();

                if( readByte == 0xA ) {
                    if( filePointer == fileLength ) {
                        continue;
                    }
                    break;
                } else if( readByte == 0xD ) {
                    if( filePointer == fileLength - 1 ) {
                        continue;
                    }
                    break;
                }
                sb.append( ( char ) readByte );
            }
            //Reverse the lastline
            String lastLine = sb.reverse().toString();
            return lastLine;
        } catch( java.io.FileNotFoundException e ) {
            e.printStackTrace();
            return null;
        } catch( java.io.IOException e ) {
            e.printStackTrace();
            return null;
        } finally {
            if (fileHandler != null )
                try {
                    fileHandler.close();
                } catch (IOException e) {
                    /* ignore */
                }
        }
    }

}
