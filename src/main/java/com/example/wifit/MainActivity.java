package com.example.wifit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.tensorflow.lite.Interpreter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.round;

public class MainActivity extends AppCompatActivity {

    WifiManager wifiManager;
    WifiReceive wifiRecerver;
    ListView wifiList;
    TextView t;
    List  mywifiList;
    TextView pe;
    ImageView  im;
    List<ScanResult> wifiLIst;
    ArrayList<String> ar = new ArrayList<>();
    ArrayList<String> ar2 = new ArrayList<>();
    Button gu, Scan;
    File file;
    String ruta, carpeta = "/archivo/";

    Interpreter tflite,tflite1,tflite2;

    @SuppressLint({"WifiManagerLeak", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Creando el objeto de tflite, cargado del archivo de modelo
        try{
            tflite = new Interpreter(loadModelFile());
            tflite1 = new Interpreter(loadModelFile1());
            tflite2 = new Interpreter(loadModelFile2());
        }catch(Exception ex){
            ex.printStackTrace();
        }



        wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifiRecerver = new WifiReceive();

        gu = findViewById(R.id.bt1);

        im = (ImageView) findViewById(R.id.img);

        gu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardar();
                gu.setEnabled(false);
            }
        });
        pe = findViewById(R.id.TV1);
        Scan = findViewById(R.id.Scan);

        Scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanWifiList();
                Scan.setEnabled(false);
                gu.setEnabled(true);

            }
        });

        gu.setEnabled(false);
        pe.setText("\tBienvenido al localizador de la \n\t\t\t\t\t\t\t\t\tTorre de Ingeniería.");
        pe.setTextColor(this.getResources().getColor(R.color.plateado));
        registerReceiver(wifiRecerver,new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

       if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},0);
        }else {
           scanWifiList();
       }
    }

    // Mapeo de memoria del modelo en assets para clasificador
    private MappedByteBuffer loadModelFile() throws IOException{
        // Abriendo el modelo usando un flujo de entrada
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("ModeloClase.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // Mapeo de memoria del modelo en assets para regresor X
    private MappedByteBuffer loadModelFile1() throws IOException{
        // Abriendo el modelo usando un flujo de entrada
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("ModeloRegX.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // Mapeo de memoria del modelo en assets para regresor Y
    private MappedByteBuffer loadModelFile2() throws IOException{
        // Abriendo el modelo usando un flujo de entrada
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("ModeloRegY.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


    private void scanWifiList() {
        String aux3;

        wifiManager.startScan();
        wifiLIst = wifiManager.getScanResults();


        for(int position = 0; position<wifiLIst.size(); position++ ) {
            aux3 = wifiLIst.get(position).BSSID;

            if (aux3.equals("fc:ec:da:11:0c:1c") || aux3.equals("fc:ec:da:12:0c:1c") || aux3.equals("04:18:d6:a9:37:4f")
                    || aux3.equals("04:18:d6:a9:38:08") || aux3.equals("04:18:d6:a5:1c:95") || aux3.equals("78:8a:20:da:60:cf")
                    || aux3.equals("80:2a:a8:69:29:eb") || aux3.equals("04:18:d6:a9:38:04") || aux3.equals("04:18:d6:a5:21:b5")
                    || aux3.equals("04:18:d6:a5:1d:1d")) {

                ar.add(String.valueOf(wifiLIst.get(position).level));
                ar2.add(String.valueOf(wifiLIst.get(position).BSSID));
                Log.i("RedWifi ", (wifiLIst.get(position).BSSID));
            }
        }

        Log.i("tamañoAR ", String.valueOf(ar.size()));
        Log.i("tamañoMac ", String.valueOf(ar2.size()));
        for(String e: ar){
            Log.i("arRedWifi ", e);
        }

        Toast.makeText(this,"Escaneando Wifi", Toast.LENGTH_SHORT).show();
        wifiLIst.clear();
    }

    class WifiReceive extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }


    public void  guardar(){

        int aux;
        String cf = "";
        String res = "";

        ArrayList<String> LisA = new ArrayList<>(Arrays.asList("fc:ec:da:11:0c:1c","fc:ec:da:12:0c:1c","04:18:d6:a9:37:4f"
                ,"04:18:d6:a9:38:08","04:18:d6:a5:1c:95","78:8a:20:da:60:cf","80:2a:a8:69:29:eb","04:18:d6:a9:38:04","04:18:d6:a5:21:b5",
                "04:18:d6:a5:1d:1d"));
        float[] valores  = new float[10];
        int i=0;

        int j = 0, a,k, cont=1, r;

        String[] GuarL = new String[10];


        ArrayList<String> LisA2 = new ArrayList<>(Arrays.asList("fc:ec:da:11:0c:1c","fc:ec:da:12:0c:1c","04:18:d6:a9:37:4f"
                ,"04:18:d6:a9:38:08","04:18:d6:a5:1c:95","78:8a:20:da:60:cf","80:2a:a8:69:29:eb","04:18:d6:a9:38:04","04:18:d6:a5:21:b5",
                "04:18:d6:a5:1d:1d"));

        String AR2 [] = new String[10];
        HashMap<Integer,String> LisAMap = new HashMap<Integer,String>();

        for(int q = 0 ; q<LisA2.size();q++){
            LisAMap.put(q, LisA2.get(q));
        }

        System.out.println("ar2 sin ordenar: "+ ar2);
        int g=0, au;
        for(Map.Entry<Integer,String> entrada : LisAMap.entrySet()){

            // System.out.println("Valor: " + entrada.getValue());

            for(int t=0; t<ar2.size();t++){

                if(ar2.get(t).equals(entrada.getValue())){
                    au = entrada.getKey();

                    AR2[au] = ar2.get(t);

                }

            }
        }

        ar2.clear();

        for(int f=0; f<AR2.length;f++){
            if(AR2[f]!=null){
                ar2.add(AR2[f]);
                System.out.println("Entra: "+f);
            }

        }
        System.out.println("ar2 ordenada: "+ ar2);
        for(String e: AR2){
            System.out.println("Ordenado: "+e);
        }


         i=0;
        while(j<LisA2.size()){
            //i=0;
            while(i<ar2.size()){

                if(ar2.get(i).equals(LisA2.get(j))){
                    GuarL[j] = ar.get(i);
                    j++;
                }else{
                    a = i;
                    k= a+1;

                    while (k<LisA.size()){
                        if(!ar2.get(a).equals(LisA2.get(k)))
                            cont++;
                        else{
                            cont = cont + i;
                            GuarL[cont] = ar.get(i);
                            break;
                        }
                        k++;
                    }

                    GuarL[j] = "-100";
                    cont = 1;
                }
                i++;

            }
            if(j<LisA.size())
                GuarL[j] = "-100";

            i=0;
            j++;
        }



        for(int p = 0; p<GuarL.length; p++){
            System.out.println("GuarL: "+GuarL[p]);
        }

        Log.i("GuL",String.valueOf(GuarL.length));
        ar.clear();
        ar2.clear();
        Scan.setEnabled(true);


        for(int sep =0; sep<GuarL.length;sep++){
            valores[sep] = Float.valueOf(GuarL[sep]);
        }

        // Llamando funciones de modelos tflite
        int[] prediccion = doInference(valores);
        float  x = doInference1(valores);
        float  y = doInference2(valores);

        // Concatenando valores de clase a cf
        for(int t=0; t<5; t++){
            cf += prediccion[t];

        }

        switch (cf){
            case "10000":
                res = "\t\tUsted esta en la Planta Baja. \t\t\tEn las coordenadas: \n\t\t\tX: "+x+" Y: "+y;
                pe.setText(res);
                pe.setTextColor(this.getResources().getColor(R.color.plateado));
                im.setImageResource(R.drawable.baja_opt);

                break;
            case "01000":
                res = "\t\tUsted esta en el Primer Piso. \t\t\tEn las coordenadas: \n\t\t\tX: "+x+" Y: "+y;
                pe.setText(res);
                pe.setTextColor(this.getResources().getColor(R.color.plateado));
                im.setImageResource(R.drawable.primero_opt);
                break;
            case "00100":
                res = "\t\tUsted esta en el Segundo Piso. \t\t\tEn las coordenadas: \n\t\t\tX: "+x+" Y: "+y;
                pe.setText(res);
                pe.setTextColor(this.getResources().getColor(R.color.plateado));
                im.setImageResource(R.drawable.segundo_opt);
                break;
            case "00010":
                res = "\t\tUsted esta en el Tercer Piso. \t\t\tEn las coordenadas: \n\t\t\tX: "+x+" Y: "+y;
                pe.setText(res);
                pe.setTextColor(this.getResources().getColor(R.color.plateado));
                im.setImageResource(R.drawable.tercero_opt);
                break;
            case "00001":
                res= "\t\tUsted esta en el Cuarto Piso. \t\t\tEn las coordenadas: \n\t\t\tX: "+x+" Y: "+y;
                pe.setText(res);
                pe.setTextColor(this.getResources().getColor(R.color.plateado));
                im.setImageResource(R.drawable.cuarto_opt);
                break;
            default:
                break;
        }

        for(String e: GuarL){
            Log.i("LIn",e);
        }

        j=0;
        Log.i("L",String.valueOf(LisA.size()));
        Scan.setEnabled(true);
        Toast.makeText(this,"Localizado", Toast.LENGTH_SHORT).show();

    }

    // Función para clasificador de piso
    public int[] doInference(float [] val){
        // Variable que guardara el valor de la clase en categorical
        float clase[][] = new float[1][5];

        tflite.run(val, clase);
        // Redondeando los valores retornados de la red neuronal porque son decimales
        int []c = new int [5];
        float redondeo = 0;
        for(int i = 0; i<5;i++){
            Log.i("Prediction: " ,String.valueOf(clase[0][i]));
            c[i] = round(clase[0][i]) ;
        }

        return c;
    }

    // función para regresor de X
    public float doInference1(float [] val){
        // Variable que guardara el valor de la clase
        float regresor[][] = new float[1][1];

        tflite1.run(val, regresor);
        float x=regresor[0][0];
        String s = String.format("%.2f", x);
        x = Float.valueOf(s);

        return x;
    }

    // función para regresor de Y
    public float doInference2(float [] val){
        // Variable que guardara el valor de la clase
        float regresor[][] = new float[1][1];

        tflite2.run(val, regresor);
        float y = regresor[0][0];
        String s = String.format("%.2f", y);
        y = Float.valueOf(s);

        return y;
    }
}