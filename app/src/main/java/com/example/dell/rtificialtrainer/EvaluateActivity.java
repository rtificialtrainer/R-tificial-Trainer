package com.example.dell.rtificialtrainer;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.Interval;
import org.dmg.pmml.OutputField;
import org.dmg.pmml.PMML;
import org.dmg.pmml.Value;
import org.joda.time.LocalDate;
import org.jpmml.evaluator.DaysSinceDate;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.TypeUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import convertpmmltomodel.GTChart;

public class EvaluateActivity extends AppCompatActivity {

    /**
     * Definiuje tryb w którym pracuje obecnie otworzone Activity
     * Wpływa na wybór rozszerzenia plików modeli które będą dostępne do wyboru dla użytkownika oraz
     * na zachowanie widoku i dostępność poszczególnych akcji
     */
    public enum EvaluateMode{
        TRENING,
        REZULTAT;
    }

    TextView choosenAthLabel;//TextView w którym wyświetla się imię i nazwisko wybranego zawodnika
    TextView formLabel;//TextView z napiszem "Wprowadź dane"
    ImageButton removeAthButt;//ImageButton służący do usunięcia wybranego zawodnika
    Button choseAthButt;//Button służący do wybrania zawodnika
    List<Evaluator> evaluatorList = null;//Lista zawierająca obiekty klasy Evaluator wczytane po wybraniu pliku z modelem
    LinearLayout MyInputView;//Widok przechowujący wejścia modelu
    LinearLayout MyOutputView;//Widok przechowujący wyjścia modelu

    LinearLayout chartContainer;//Widok przechowujący wygenerowane wykresy

    //Grupa HashMap która przechowuje elementy formularza takie jak lista rozwijalna, wejścia tekstowe, radio buttony
    //kluczem do nich są FieldName modelu
    LinkedHashMap<FieldName,EditText>AllEditText;
    LinkedHashMap<FieldName,Spinner>AllSpinner;
    LinkedHashMap<FieldName,RadioGroup>AllRadio;
    //Przechowuje wyjścia modelu, kluczem do nich są FieldName modelu
    LinkedHashMap<FieldName,TextView>AllOutputs;
    //Przechowuje wartości displayName wczytanego modelu. Jeżeli wejście nie posiada tego parametru to jego wartość równa się FieldName
    HashMap<FieldName,String> allDisplayNames;
    HashMap<FieldName,Integer> outputFieldsNumbers;

    AssetManager assetManager;//Pozwala na dostęp to assets aplikacji
    Button evaluateButton;//Przycisk który wywołuje funkcję obliczającą wyjścia na podstawie wypełnionego formularza
    InputMethodManager keyboardManager;//Pozwala zarządzać klawiaturą systemową

    /**
     * Przechowuje pliki modeli zawarte w assets bądź pamięci telefonu
     * Jeżeli wartość pod kluczem będącym nazwą pliku ma wartość false to znaczy że pochodzi z pamięci telefonu (katalog Modele)
     * a gdy ma wartość true to pochodzi z assets (pliki zintegrowane z aplikacją na etapie wygenerowania pliku instalacyjnego)
     */
    LinkedHashMap<String,Boolean> files;
    File modelDir;//plik odnoszący się do katalogu z modelami

    Calendar calendar;//przechowuje instancje kalendarza

    AthleteModel choosenAthlete=null;//przechowuje referencje do obiektu wybranego zawodnika
    boolean formCreated=false;//flaga informująca o tym czy formularz został utworzony

    ArrayList<GTChart> chartList;//Lista przechowująca definicje wykresów które może zawierać wczytany plik modelu

    List<PieChart> pieChartList;//Lista widoków reprezentujących poszczególne wykresy które mogły zostać utworzone

    String fileFormat;//Przechowuje ciąg znaków informujący o obecnym rozszerzeniu np. ".gtm" lub ".prm"
    EvaluateMode evaluateMode;//Przechowuje wartość definującą obecny tryb w którym uruchomiono Activity
    Button goToPredictButton;//Przycisk który przenosi do predykcji rezultatu po wygenerownaiu treningu
    TextView outputLabel;//TextView z napisem "Wynik:"

    Spinner modelSpinner;//Lista rozwijalna przechowująca nazwy dostępnych modeli

    HashMap<FieldName,String> inputData=null;//Przechowuje przesłane wartości wyjść z modułu GT bądź obecny stan wypełnionego formularza w celach jego odnowienia
    HashMap<FieldName,String> trainingData=null;//Kopia danych określających wygenerowany trening w module GT

    boolean isEvaluated=false;//flaga określająca czy wykonano obliczenie wartości wyjściowych
    boolean doEvaluate = false;//flaga określająca czy należy wykonać obliczenie wartości wyjściowych np. po odnowieniu Activity

    Button pdfBtn;//Przycisk służący do wygenerowania PDF treningu oraz jego wysłania za pomocą klienta email
    File pdfDir;//katalog na pliki PDF

    String pdfModelName;//Przechowuje nazwe modelu (Dyscypliny sportowej) która używana jest przy generowaniu PDF

    AlertDialog.Builder alertDialog;//Wyswietlanie informacji dla uzytkownika

    boolean modelExist = false;//flaga określająca czy szukany model .prm został odnaleziony

    int selectedPosition = -1;//służy do zabezpieczenia aplikacji przed podwójnym ładowaniem tego samego modelu

    boolean chartDefRecieved = false;//flaga definująca czy odebrano definicje wykresów jeśli nie to czyścimy zmienną pamiętająca ewentualne pozostałości po starym modelu

    Switch roundSwitch;//switch określający czy zaokrąglać wartości na wyjściu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluate);

        alertDialog = new AlertDialog.Builder(this);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        roundSwitch = (Switch)findViewById(R.id.roundSwitch);

        pdfBtn = (Button)findViewById(R.id.pdf_button);

        allDisplayNames = new HashMap<>();
        outputFieldsNumbers = new HashMap<>();

        outputLabel = (TextView)findViewById(R.id.form_output_label);
        assetManager = getAssets();
        modelDir = new File(Environment.getExternalStorageDirectory(),getString(R.string.appDirName)+"/"+getString(R.string.modelDirName));
        pdfDir = new File(Environment.getExternalStorageDirectory(),getString(R.string.appDirName)+"/"+getString(R.string.pdfDirName));
        files = new LinkedHashMap<>();

        calendar = Calendar.getInstance();

        goToPredictButton = (Button)findViewById(R.id.form_gopredict_button);

        AllEditText = new LinkedHashMap<>();
        AllOutputs = new LinkedHashMap<>();
        AllSpinner = new LinkedHashMap<>();
        AllRadio = new LinkedHashMap<>();
        MyInputView=(LinearLayout)findViewById(R.id.FormInputContainer);
        MyOutputView=(LinearLayout)findViewById(R.id.FormOutputContainer);

        chartContainer=(LinearLayout)findViewById(R.id.ChartContainer);

        evaluateButton=(Button)findViewById(R.id.form_evaluate_button);
        evaluateButton.setVisibility(View.GONE);

        keyboardManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

        choosenAthLabel=(TextView)findViewById(R.id.choosen_ath);
        removeAthButt=(ImageButton)findViewById(R.id.remove_ath_butt);
        choseAthButt = (Button)findViewById(R.id.chose_ath_butt);
        formLabel=(TextView)findViewById(R.id.form_label);

        String modelName=null;
        /*
        Sprawdzamy co zostało przesłane do Activity podczas jego uruchomienia
        Pobieramy wartość pod kluczem TRYB definująca tryb w którym pracuje obecnie Activity
        Określamy rozszerzenia plików, definicje wykresów itp.
         */
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            switch (bundle.getString("TRYB")) {
                case "REZULTAT":
                    roundSwitch.setVisibility(View.GONE);
                    fileFormat=".prm";
                    evaluateMode = EvaluateMode.REZULTAT;
                    modelName = bundle.getString("MODEL");
                    inputData = (HashMap<FieldName,String>)bundle.getSerializable("TRENING");
                    if(inputData!=null){
                        trainingData = new HashMap<>();
                        trainingData.putAll(inputData);
                    }
                    chartList=(ArrayList<GTChart>)bundle.getSerializable("WYKRESYDEF");
                    if(chartList==null)Log.wtf("Wykresy","null");
                    else {
                        chartDefRecieved=true;
                        Log.wtf("Wykresy", "" + chartList.size());
                    }
                    allDisplayNames=(HashMap<FieldName,String>)bundle.getSerializable("DISPNAMES");
                    if(allDisplayNames==null)allDisplayNames = new HashMap<>();

                    outputFieldsNumbers=(HashMap<FieldName,Integer>)bundle.getSerializable("OUTPUTSNUMBERS");
                    if(outputFieldsNumbers==null)outputFieldsNumbers = new HashMap<>();

                    choosenAthlete=bundle.getParcelable(AthleteModel.ATHL_PARCEL);
                    if(choosenAthlete!=null){
                        choseAthButt.setVisibility(View.GONE);
                        removeAthButt.setVisibility(View.INVISIBLE);
                        removeAthButt.setEnabled(false);
                    }
                    setChoosenAthleteDetails();
                    initModelSpinner(modelName);
                    break;
                case "TRENING":
                    fileFormat=".gtm";
                    evaluateMode = EvaluateMode.TRENING;
                    getSupportActionBar().setTitle(R.string.gtModeActionBar);
                    initModelSpinner(modelName);
                    break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return  true;
        }
        return true;
    }

    /**
     * Metoda wywoływana po naciśnięciu na przycisk evaluateButton
     * Pobiera wartości wpisane w formularz wywołuje mechanizm ewaluacji na wczytanym modlu
     * a otrzymane wyniki prezentuje na ekranie użytkownika
     * @param view
     */
    public void onEvaluateBtnClick(View view) {
        outputLabel.setVisibility(View.GONE);
        MyOutputView.setVisibility(View.GONE);
        pdfBtn.setVisibility(View.GONE);
        goToPredictButton.setVisibility(View.GONE);
        chartContainer.setVisibility(View.GONE);

        for(Evaluator evaluator : evaluatorList) {
            List<FieldName> activeFields = evaluator.getActiveFields();
            List<FieldName> targetFields = org.jpmml.evaluator.EvaluatorUtil.getTargetFields(evaluator);

            org.jpmml.evaluator.FieldValue fieldValue = null;
            Map<FieldName, org.jpmml.evaluator.FieldValue> inputs = new LinkedHashMap<>();

            Map res;

            for (FieldName fieldName : activeFields) {
                try {
                    Object input = null;
                    if(AllEditText.containsKey(fieldName)){
                        if(isTimePickerInput(allDisplayNames.get(fieldName)))input=getTimeDouble(AllEditText.get(fieldName).getText().toString());
                        else input = AllEditText.get(fieldName).getText().toString();
                    }else if(AllSpinner.containsKey(fieldName)){
                        input = AllSpinner.get(fieldName).getSelectedItem().toString().replace(" ",".");
                    }else if(AllRadio.containsKey(fieldName)){
                        if(AllRadio.get(fieldName).getCheckedRadioButtonId()==R.id.form_radio_yes)input=1;
                        else input = 0;
                    }
                    if(evaluator.getDataField(fieldName).getDataType()==null){
                        Log.wtf("evaluate", "Błędny typ danych " + allDisplayNames.get(fieldName));
                        alertError(getString(R.string.datatype_error_msg) + allDisplayNames.get(fieldName));
                        hideKeyboard();
                        return;
                    }
                    fieldValue = evaluator.prepare(fieldName, input);
                    inputs.put(fieldName, fieldValue);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.wtf("evaluate","Błędne wejście " + allDisplayNames.get(fieldName));
                    alertError(getString(R.string.input_error_msg) + allDisplayNames.get(fieldName));
                    hideKeyboard();
                    return;
                }
            }
            try {
                res = evaluator.evaluate(inputs);
            } catch (Exception e) {
                e.printStackTrace();
                Log.wtf("evaluate", "Błąd ewaluatora " + e.getMessage());
                alertError(getString(R.string.evaluator_error_msg) + e.getMessage());
                hideKeyboard();
                return;
            }

            //wyniki
            for (FieldName tF : targetFields){
                Object resultValue = res.get(tF);
                if(resultValue instanceof Number){
                    //Jeżeli displanyName kończy się na [czas] lub [s] to następuję odpowiednia konwersja liczby typu double na
                    //string sformatowany w postaci oznaczającej czas (00:00:00 dla [s] oraz 00:00:00.00 dla [czas])
                    if(roundSwitch.isChecked()){
                        resultValue = Math.round(((Number) resultValue).doubleValue());
                    }

                    if(isTimePickerInput(allDisplayNames.get(tF))){
                        Log.wtf("Before timeString", ": " + resultValue);
                        //AllOutputs.get(tF).setText(getTimeString(new Double(String.format(Locale.ROOT, "%.2f", resultValue))));
                        AllOutputs.get(tF).setText(getTimeString(((Number) resultValue).doubleValue()));
                    }else if(allDisplayNames.get(tF).endsWith("[s]")){
                        //String timeString = getTimeString(new Double(String.format(Locale.ROOT, "%.2f", resultValue)));
                        String timeString = getTimeString(((Number) resultValue).doubleValue());
                        timeString = timeString.substring(0, timeString.indexOf("."));
                        AllOutputs.get(tF).setText(timeString);
                    }else if(allDisplayNames.get(tF).endsWith(getString(R.string.amount_format))&&!roundSwitch.isChecked()){
                        //jeżeli displayName kończy się na [ilość] to wynik zostaje zaokrąglony do wartości całkowitych
                        resultValue = Math.round(((Number) resultValue).doubleValue());
                        AllOutputs.get(tF).setText(""+resultValue);
                    }else if(!roundSwitch.isChecked()){
                        AllOutputs.get(tF).setText(String.format(Locale.ROOT,"%.2f",resultValue));
                    }
                    else{
                        AllOutputs.get(tF).setText(""+resultValue);
                    }
                }else{
                    alertError(getString(R.string.output_datatype_error) + tF.toString() + getString(R.string.output_datatype_error_ending)+resultValue);
                    return;
                }
            }
        }

        if(evaluateMode.equals(EvaluateMode.TRENING))goToPredictButton.setVisibility(View.VISIBLE);
        if(trainingData!=null&&choosenAthlete!=null&&evaluateMode.equals(EvaluateMode.REZULTAT))pdfBtn.setVisibility(View.VISIBLE);
        outputLabel.setVisibility(View.VISIBLE);
        MyOutputView.setVisibility(View.VISIBLE);
        addCharts();
        isEvaluated=true;
        doEvaluate=false;
        hideKeyboard();
    }

    /**
     * Chowa klawiaturę widoczną na ekranie
     */
    private void hideKeyboard() {
        if(keyboardManager!=null&&keyboardManager.isActive()){
            try{
                View currentFocus = getCurrentFocus();
                if(currentFocus!=null)keyboardManager.hideSoftInputFromWindow(currentFocus.getWindowToken(),0);
            }catch(Exception e){
                Log.wtf("Hide keyboard","Error");
            }
        }
    }

    /**
     * Przelicza czas na odpowiadającą mu ilość sekund
     * @param timeString czas w formacie 00:00:00 bądź 00:00:00.00
     * @return ilość sekund
     */
    private Object getTimeDouble(String timeString) {
        String timeParts[] = timeString.split(":");
        int hours = Integer.valueOf(timeParts[0])*3600;
        int minutes = Integer.valueOf(timeParts[1])*60;
        //sekundy i centysekundy po przecinku
        double seconds = Double.valueOf(timeParts[2]);
        return hours+minutes+seconds;
    }

    /**
     * Zwraca czas odpowiadający podanej liczbie sekund
     * @param timeSeconds ilość sekund
     * @return string w formacie czasu odpowiadający parametrowi ilości sekund
     */
    private String getTimeString(double timeSeconds){
        Log.wtf("getTimeString",": "+timeSeconds);
        int hours = (int)timeSeconds/3600;
        int minutes = (int)(timeSeconds-hours*3600)/60;
        int seconds = (int)timeSeconds-(hours*3600)-(minutes*60);
        double csecondsTemp = timeSeconds-(hours*3600)-(minutes*60)-seconds;
        int cseconds = (int)(csecondsTemp*100);
        return String.format(Locale.ROOT,"%02d:%02d:%02d.%02d",hours,minutes,seconds,cseconds);
    }

    /**
     * Dodaje wykresy do widoku przeznacznego na ich wyświetlanie
     */
    private void addCharts() {
        if(chartList!=null&&evaluateMode.equals(EvaluateMode.TRENING)){
            Log.wtf("addCharts","weszło "+chartList);
            if(!chartList.isEmpty()){
                Log.wtf("addCharts","niepusta ile:"+chartList.size());
                pieChartList = createChartViewList();

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int barHeight = getSupportActionBar().getHeight();
                int statusHeight = 0;
                int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                if(resId>0){
                    statusHeight=getResources().getDimensionPixelSize(resId);
                }
                int paddPx = (int)(32*displayMetrics.density);

                chartContainer.removeAllViews();

                chartContainer.setVisibility(View.VISIBLE);

                //W zależności od orientacji ekranu wykresy dodawane są do widoku z odpowiednimi parametrami rozmiaru
                for(View pieChart : pieChartList){
                    if(getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT){
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,(int)(0.75*(displayMetrics.widthPixels-paddPx)));
                        chartContainer.addView(pieChart, chartContainer.getChildCount(), params);
                    }else{
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,(int)(0.8*(displayMetrics.heightPixels-barHeight-statusHeight-paddPx)));
                        chartContainer.addView(pieChart, params);
                    }
                }
            }
        }
    }

    /**
     * Tworzy formularz na podstawie wybranego modelu
     * @param modelName nazwa pliku modelu
     */
    public void createForm(String modelName){
            if (!AllEditText.isEmpty() || !AllOutputs.isEmpty() || !AllSpinner.isEmpty() || !AllRadio.isEmpty()) {
                int size = AllEditText.size() + AllRadio.size() + AllSpinner.size();
                MyInputView.removeViews(0, size);
                MyOutputView.removeViews(0, AllOutputs.size());
                chartContainer.removeAllViews();
                chartContainer.setVisibility(View.GONE);
                AllEditText = new LinkedHashMap<>();
                AllOutputs = new LinkedHashMap<>();
                AllSpinner = new LinkedHashMap<>();
                AllRadio = new LinkedHashMap<>();
            }

            try {
                InputStream is = loadModelFile(modelName);
                Map<String,List> modelMap = EvaluatorListUtil.deserializeMapPMML(is);
                List<PMML> pmmlList = modelMap.get("model");
                pdfModelName=pmmlList.get(0).getModels().get(0).getModelName();
                evaluatorList = EvaluatorListUtil.createEvaluatorList(pmmlList);
                if(modelMap.containsKey("chart")){
                    chartList = new ArrayList<>();
                    chartList.addAll(modelMap.get("chart"));
                }else if(!chartDefRecieved)chartList=null;//jeżeli nie otrzymano definicji wykresów to wymarz te ewentualnie zachowane w pamięci
            } catch (IOException ioe) {
                alertError(getString(R.string.model_acces_error));
                Log.w("evaluate IOException", "Błąd podczas ładowania pliku");
                ioe.printStackTrace();
                return;
            } catch (Exception e) {
                alertError(getString(R.string.eval_create_error));
                Log.w("evaluate Exception", "Błędna wczytana klasa");
                e.printStackTrace();
                return;
            }

            LayoutInflater inflater = getLayoutInflater();

            //Przechodzę po liście list zawierającej wejścia dla każdego modelu
            //jeżeli już takowego nie zrobiłem to tworzę dla niego input
            //i umieszczam go w hashu
            int outputIndex = 0;
            for (Evaluator evaluator : evaluatorList) {
                List<FieldName> ActiveFields = evaluator.getActiveFields();
                for (FieldName aF : ActiveFields) {
                    if (!AllEditText.containsKey(aF) && !AllRadio.containsKey(aF) && !AllSpinner.containsKey(aF)) {
                        DataField dataField = evaluator.getDataField(aF);
                        if (dataField == null) {
                            alertError(getString(R.string.form_create_error));
                            Log.w("evaluate create form", "dataField ==null");
                            return;
                        }else if(dataField.getDataType()==null){
                            alertError(getString(R.string.dataType_create_error) + dataField.getName().toString());
                            return;
                        }
                        switch (dataField.getOpType().value()) {
                            case "continuous":
                                if (dataField.hasValues()) addCategoricalInput(dataField, inflater);
                                else addContinuousInput(dataField, inflater);
                                break;
                            case "ordinal":
                            case "categorical":
                                addCategoricalInput(dataField, inflater);
                                break;
                        }
                    }
                }
                List<FieldName> TargetFields = evaluator.getTargetFields();
                for (FieldName tF : TargetFields) {
                    outputIndex++;
                    if (!AllOutputs.containsKey(tF)) {
                        OutputField outputField = evaluator.getOutputField(new FieldName("Predicted_" + tF.toString()));
                        DataField dataField = evaluator.getDataField(tF);
                        String fieldString = null;
                        if (outputField == null) {
                            Log.w("outputField", "null");
                            if(dataField == null){
                                Log.w("dataField", "null");
                            }else fieldString=dataField.getDisplayName();
                        } else fieldString=outputField.getDisplayName();


                        View rowView = inflater.inflate(R.layout.form_row, null);
                        TextView prTextView = (TextView) rowView.findViewById(R.id.form_textview);
                        prTextView.setVisibility(View.VISIBLE);
                        TextView dispname = (TextView)rowView.findViewById(R.id.form_dispname);
                        if(evaluateMode.equals(EvaluateMode.TRENING)){
                            dispname.setText("("+outputIndex+") "+getVariableName(tF, fieldString));
                            outputFieldsNumbers.put(tF, outputIndex);
                        }
                        else dispname.setText(""+getVariableName(tF, fieldString));
                        dispname.setVisibility(View.VISIBLE);

                        AllOutputs.put(tF, prTextView);
                        //DISP
                        allDisplayNames.put(tF,getVariableName(tF,fieldString));
                        MyOutputView.addView(rowView, MyOutputView.getChildCount());
                    }
                }

            }

            evaluateButton.setVisibility(View.VISIBLE);
            formCreated = true;
            fillAthleteData(true);
            fillInputData();
            formLabel.setVisibility(View.VISIBLE);
    }

    /**
     * Wypełnia liste rozwijalną nazwami dostępnych w pamięci modeli
     * @param modelName - jeżeli parametr nie równa się null to wtedy ustawiana jest ta wartość jako wybrana po zainicjowaniu
     */
    public void initModelSpinner(final String modelName){
        modelSpinner = (Spinner)findViewById(R.id.model_spinner);
        List<String> modelList = listModelFiles();

        if(!modelList.isEmpty()){
            ArrayAdapter<String>modelAdapter=new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,modelList);
            modelAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
            modelSpinner.setAdapter(modelAdapter);
            if(modelName!=null){
                int count = modelAdapter.getCount();
                for(int i=0;i<count;i++){
                    if(modelAdapter.getItem(i).toString().equals(modelName)){
                        modelSpinner.setSelection(i);
                        modelExist=true;
                        modelSpinner.setEnabled(false);
                        break;
                    }
                }
                if(!modelExist){
                    alertError(getString(R.string.model_not_found_error));
                    return;
                }
            }

            modelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    //warunek zabezpiecza przed przypadkowym podwójnym wywołaniem podczas zmiany orientacji ekranu
                    if(position!=selectedPosition){
                        selectedPosition=position;
                        Log.wtf("Before Item selected","doEvaluate: "+doEvaluate+" isEvaluated: "+isEvaluated+" inputData: "+inputData);
                        Log.wtf("ItemSelected","dla pos:" +position);
                        //Aby po obliczeniu zmianie modelu i obrocie nie próbowało liczyć
                        isEvaluated = false;
                        outputLabel.setVisibility(View.GONE);
                        MyOutputView.setVisibility(View.GONE);
                        pdfBtn.setVisibility(View.GONE);
                        goToPredictButton.setVisibility(View.GONE);
                        chartContainer.setVisibility(View.GONE);
                        evaluateButton.setVisibility(View.GONE);
                        formLabel.setVisibility(View.GONE);
                        createForm(parent.getAdapter().getItem(position).toString());
                        Log.wtf("After doEvaluate", "doEvaluate: " + doEvaluate + " isEvaluated: " + isEvaluated + " inputData: " + inputData);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }else {
            alertError(getString(R.string.no_models_error)+fileFormat);
            Log.w("evaluate", "brak plików z modelami");
        }
    }

    /**
     * Zwraca listęp dostępnych modeli w pamięci urządzenia oraz tworzy wypełnia HashMap informacjami gdzie te modele się znajdują
     * @return lista nazwa plików modeli
     */
    public List<String> listModelFiles() {

        try {
            String[] fileNameArray = assetManager.list("models");
            for(String asset : fileNameArray){
                if(asset.endsWith(fileFormat))files.put(asset,true);
                //true - plik z asset
            }
            if(modelDir.exists()){
                fileNameArray = modelDir.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        return filename.endsWith(fileFormat);
                    }
                });
                for(String file : fileNameArray){
                    files.put(file,false);
                    //false - plik z katalogu użytkownika
                }
            }
        } catch (Exception e){
            alertError("Błąd odczytu podczas wczytywania modeli");
        }

        List<String> assets = new ArrayList<>();

        for(String key : files.keySet()){
            assets.add(key);
        }

        return assets;
    }

    /**
     * Wczytuje plik modelu
     * @param modelName nazwa pliku modelu który chcemy wczytać
     * @return InputStream pliku z modelem
     * @throws Exception Błąd podczas dostępu do pliku
     */
    public InputStream loadModelFile(String modelName) throws Exception{

        InputStream inputStream = null;
        if(files.get(modelName)){
            try{
                inputStream = assetManager.open("models/"+modelName);
            }catch (IOException ioe){
                throw ioe;
            }
        }else{
                File model = new File(modelDir,modelName);
                try{
                    inputStream = new FileInputStream(model);
                }catch (IOException e) {
                    throw e;
                }
        }
        return inputStream;
    }

    /**
     * Dodaje do formualrza wejście typu continous np. zwykłe wejścia na liczby, time bądź date pickery
     * @param dF
     * @param inflater
     */
    void addContinuousInput(final DataField dF, LayoutInflater inflater){
        boolean isSupported=false;
        boolean pickerInput=false;
        View rowView = inflater.inflate(R.layout.form_row,null);
        final EditText prEditText = (EditText)rowView.findViewById(R.id.form_edittext);
        prEditText.setVisibility(View.VISIBLE);

        TextView dispname = (TextView)rowView.findViewById(R.id.form_dispname);
        dispname.setVisibility(View.VISIBLE);
        dispname.setText(getVariableName(dF.getName(), dF.getDisplayName())+":");

        String dataType;
        if(dF.getDataType()==null){
            dataType = "unsupported";
        }else dataType = dF.getDataType().value();

        switch (dataType){
            case "integer":
                isSupported=true;
                //Jeżeli wartości mogą być ujemne to ustawiam filtr na wprowadzanie
                //Ponadto ze względu że jest to typ całkowity ustawiam filtr pozwalający na wprowadzanie tylko takich wartości
                //analogicznie postępuję z innymi typami danych
                if(isSigned(dF))prEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                else prEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

                break;
            case "float":
            case "double":
                isSupported=true;
                //Jeżeli kończy się na [czas] to wejście będzie obłsugiwane przez TimePicker a w przeciwnym wypadku jako normalne wejście
                if(isTimePickerInput(getVariableName(dF.getName(),dF.getDisplayName()))){
                    pickerInput=true;
                    prEditText.setTextIsSelectable(false);
                    prEditText.setInputType(InputType.TYPE_NULL);
                    final String errorMessage = createIntervalErrorMsg(dF);

                    final com.example.dell.rtificialtrainer.MyTimePickerDialog myTimePickerDialog = new com.example.dell.rtificialtrainer.MyTimePickerDialog(this, new com.example.dell.rtificialtrainer.MyTimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(com.example.dell.rtificialtrainer.TimePicker view, int hourOfDay, int minute, int seconds, int cseconds) {
                            prEditText.setText(String.format("%02d",hourOfDay)+":"+String.format("%02d",minute)+":"+String.format("%02d",seconds)+"."+String.format("%02d",cseconds));

                            if (dF.hasIntervals()) {
                                if (!checkValueIntervals(dF, new Double(getTimeDouble(prEditText.getText().toString()).toString()))) {
                                    prEditText.setError(errorMessage);
                                    prEditText.setText("");
                                }else prEditText.setError(null);
                            }
                        }
                    },0,0,0,0,true);
                    prEditText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            myTimePickerDialog.show();
                        }
                    });

                    prEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if (hasFocus) {
                                myTimePickerDialog.show();
                            }
                        }
                    });

                    prEditText.setHint("00:00:00.00");

                }else{
                    if(isSigned(dF))prEditText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    else prEditText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
                }
                break;
            case "string": {
                isSupported=false;
                prEditText.setText(getString(R.string.unsupported_type)+"Continuous String");
                prEditText.setFocusable(false);
                break;
            }
            case "boolean":{
                addCategoricalInput(dF, inflater);
                return;
            }
            case "time":{
                isSupported=false;
                prEditText.setText(getString(R.string.unsupported_type)+"Continuous Time");
                prEditText.setFocusable(false);
                break;
            }
            case "timeSeconds":{
                pickerInput=true;
                isSupported=true;
                prEditText.setTextIsSelectable(false);
                prEditText.setInputType(InputType.TYPE_NULL);

                final String errorMessage = createIntervalErrorMsg(dF);

                final com.example.dell.rtificialtrainer.MyTimePickerDialog myTimePickerDialog = new com.example.dell.rtificialtrainer.MyTimePickerDialog(this, new com.example.dell.rtificialtrainer.MyTimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(com.example.dell.rtificialtrainer.TimePicker view, int hourOfDay, int minute, int seconds, int cseconds) {
                        prEditText.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute) + ":" + String.format("%02d", seconds));

                        if (dF.hasIntervals()) {
                            if (!checkValueIntervals(dF, new Double(getTimeDouble(prEditText.getText().toString()).toString()))) {
                                prEditText.setError(errorMessage);
                                prEditText.setText("");
                            }else prEditText.setError(null);
                        }
                    }
                },0,0,0,0,true);
                prEditText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myTimePickerDialog.show();
                    }
                });
                prEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if(hasFocus){
                            myTimePickerDialog.show();
                        }
                    }
                });

                prEditText.setHint("00:00:00");

                break;
            }
            case "dateDaysSince[1960]":
            case "dateDaysSince[1970]":
            case "dateDaysSince[1980]":{
                pickerInput=true;
                isSupported=true;
                prEditText.setTextIsSelectable(false);
                prEditText.setInputType(InputType.TYPE_NULL);

                final String errorMessage = createIntervalErrorMsg(dF);

                final DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        prEditText.setText(year + "-" + String.format("%02d", monthOfYear + 1) + "-" + String.format("%02d", dayOfMonth));

                        if(dF.hasIntervals()) {
                            DaysSinceDate daysSinceDate = (DaysSinceDate)TypeUtil.parse(dF.getDataType(), prEditText.getText().toString());
                            if (!checkValueIntervals(dF, daysSinceDate.doubleValue())) {
                                prEditText.setError(errorMessage);
                                prEditText.setText("");
                            }else prEditText.setError(null);
                        }
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

                prEditText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        datePickerDialog.show();
                    }
                });
                prEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            datePickerDialog.show();
                        }
                    }
                });
                prEditText.setHint("0000-00-00");
                break;
            }
            case "dateDaysSince[0]":
            case "dateTimeSecondsSince[0]":
            case "dateTimeSecondsSince[1960]":
            case "dateTimeSecondsSince[1970]":
            case "dateTimeSecondsSince[1980]":
            case "date":{
                isSupported=false;
                prEditText.setText(getString(R.string.unsupported_type)+"Continuous "+dataType);
                prEditText.setFocusable(false);
                break;
            }
            case "dateTime":{
                isSupported=false;
                prEditText.setText(getString(R.string.unsupported_type)+"Continuous dateTime");
                prEditText.setFocusable(false);
                break;
            }
            case "unsupported":{
                isSupported=false;
                prEditText.setText(getString(R.string.unsupported_type2));
                prEditText.setFocusable(false);
                break;
            }
        }

        //jeżeli wejścia ma określone przedziały dozwolonych wartości (Intervals) to tworzę obsługę weryfikacji wprowadzonych wartości
        if(dF.hasIntervals()&&isSupported&&!pickerInput){

            final String errorMessage = createIntervalErrorMsg(dF);
            final String HintText = getVariableName(dF.getName(), dF.getDisplayName());
            prEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        prEditText.setHint(HintText);
                        prEditText.setSelectAllOnFocus(true);
                    } else prEditText.setHint("");
                    if (!prEditText.getText().toString().isEmpty()) {

                        if (!checkValueIntervals(dF, Double.parseDouble(prEditText.getText().toString()))) {
                            prEditText.setError(errorMessage);
                            prEditText.setText("");
                            if(keyboardManager!= null&&keyboardManager.isActive()) {
                                try {
                                    keyboardManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                } catch (Exception e) {
                                    Log.wtf("HIDE KEYBOARD", "error on hide keyboard");
                                }
                            }
                        }
                    }
                }
            });
            prEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE && !v.getText().toString().isEmpty()) {

                        if (!checkValueIntervals(dF, Double.parseDouble(v.getText().toString()))) {
                            v.setError(errorMessage);
                            v.setText("");
                            hideKeyboard();
                        }
                    }
                    return false;
                }
            });
        }else if(!dF.hasIntervals()&&isSupported&&!pickerInput){
            final String HintText = getVariableName(dF.getName(), dF.getDisplayName());
                prEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            prEditText.setHint(HintText);
                            prEditText.setSelectAllOnFocus(true);
                        }
                        else prEditText.setHint("");
                    }
                });
        }


        AllEditText.put(dF.getName(), prEditText);
        //DISP
        allDisplayNames.put(dF.getName(),getVariableName(dF.getName(),dF.getDisplayName()));
        MyInputView.addView(rowView, MyInputView.getChildCount());
    }

    /**
     * Sprawdza czy można wprowadzić wartości ujemne
     * @param dF
     * @return flaga określająca czy wartości są ujemne czy też nie
     */
    private boolean isSigned(DataField dF) {
        if(dF.getIntervals().isEmpty())return true;
        for(Interval interval : dF.getIntervals()){
            if(interval.getLeftMargin()==null)return true;
            else if(interval.getRightMargin()!=null){
                if(interval.getLeftMargin()<0||interval.getRightMargin()<0)return true;
            }
        }
        return false;
    }

    /**
     * Dodaje do formualrza wejście typu categorical np. lista rozwijalna czy też radio button
     * @param dF
     * @param inflater
     */
    void addCategoricalInput(DataField dF, LayoutInflater inflater){
        View rowView = inflater.inflate(R.layout.form_row, null);

        if(dF.getDataType().value().equals("boolean")){
            RadioGroup radioGroup = (RadioGroup)rowView.findViewById(R.id.form_radiogroup);
            radioGroup.setVisibility(View.VISIBLE);
            TextView textView = (TextView)rowView.findViewById(R.id.form_radioname);
            textView.setText(getVariableName(dF.getName(), dF.getDisplayName()) + ":");


            AllRadio.put(dF.getName(), radioGroup);
            //DISP
            allDisplayNames.put(dF.getName(), getVariableName(dF.getName(), dF.getDisplayName()));
            MyInputView.addView(rowView, MyInputView.getChildCount());
        }else{

            TextView dispname = (TextView)rowView.findViewById(R.id.form_dispname);
            dispname.setVisibility(View.VISIBLE);
            dispname.setText(getVariableName(dF.getName(), dF.getDisplayName())+":");

            Spinner prSpinner = (Spinner)rowView.findViewById(R.id.form_spinner);
            prSpinner.setVisibility(View.VISIBLE);

            List<String> valueList = new ArrayList<>();
            for(Value value : dF.getValues()){
                valueList.add(value.getValue().replace("."," "));
            }

            ArrayAdapter<String>valueAdapter=new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,valueList);
            valueAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
            prSpinner.setAdapter(valueAdapter);
            prSpinner.setPrompt(getVariableName(dF.getName(), dF.getDisplayName()));

            AllSpinner.put(dF.getName(), prSpinner);
            //DISP
            allDisplayNames.put(dF.getName(),getVariableName(dF.getName(),dF.getDisplayName()));
            MyInputView.addView(rowView, MyInputView.getChildCount());
        }
    }

    /**
     * Zwraca wartość określającą etykietę wejścia.
     * @param Name
     * @param displayName
     * @return zwraca Name gdy displayName jest równe null
     */
    String getVariableName(FieldName Name, String displayName){
        if(displayName==null||displayName.isEmpty())
            return Name.toString().replace("."," ");
        return displayName;
    }

    /**
     * Sprawdza czy wprowadzona wartość mieści się w ustalonym zakresie
     * @param dataField pole dla którego następuje weryfikacja
     * @param value wartość poddawana weryfikacji
     * @return true jeśli mieści się w zakresie false jeśli nie
     */
    boolean checkValueIntervals(DataField dataField,Double value){
        for (Interval interval : dataField.getIntervals()) {
            switch (interval.getClosure().value()) {
                case "openClosed":
                    if(interval.getLeftMargin()!=null&&interval.getRightMargin()!=null){
                        if(value>interval.getLeftMargin()&&value<=interval.getRightMargin())return true;
                    }else if(interval.getLeftMargin()!=null&&interval.getRightMargin()==null){
                        if(value>interval.getLeftMargin())return true;
                    }else if(interval.getLeftMargin()==null&&interval.getRightMargin()!=null){
                        if(value<=interval.getRightMargin())return true;
                    }
                    break;
                case "openOpen":
                    if(interval.getLeftMargin()!=null&&interval.getRightMargin()!=null){
                        if(value>interval.getLeftMargin()&&value<interval.getRightMargin())return true;
                    }else if(interval.getLeftMargin()!=null&&interval.getRightMargin()==null){
                        if(value>interval.getLeftMargin())return true;
                    }else if(interval.getLeftMargin()==null&&interval.getRightMargin()!=null){
                        if(value<interval.getRightMargin())return true;
                    }
                    break;
                case "closedOpen":
                    if(interval.getLeftMargin()!=null&&interval.getRightMargin()!=null){
                        if(value>=interval.getLeftMargin()&&value<interval.getRightMargin())return true;
                    }else if(interval.getLeftMargin()!=null&&interval.getRightMargin()==null){
                        if(value>=interval.getLeftMargin())return true;
                    }else if(interval.getLeftMargin()==null&&interval.getRightMargin()!=null){
                        if(value<interval.getRightMargin())return true;
                    }
                    break;
                case "closedClosed":
                    if(interval.getLeftMargin()!=null&&interval.getRightMargin()!=null){
                        if(value>=interval.getLeftMargin()&&value<=interval.getRightMargin())return true;
                    }else if(interval.getLeftMargin()!=null&&interval.getRightMargin()==null){
                        if(value>=interval.getLeftMargin())return true;
                    }else if(interval.getLeftMargin()==null&&interval.getRightMargin()!=null){
                        if(value<=interval.getRightMargin())return true;
                    }
                    break;
            }
        }
        return false;//nie mieści się w zakresie
    }

    /**
     * Tworzy wiadomość w wyświetlaną użytkownikowi gdy wprowadzi wartość spoza zakresu
     * @param dF
     * @return wiadomość o błędzie
     */
    String createIntervalErrorMsg(DataField dF){
        String text;
        final StringBuilder errorMessage = new StringBuilder();
        errorMessage.append(getString(R.string.out_off_interval));
        for(Interval interval : dF.getIntervals()) {
            Double rightMargin = interval.getRightMargin();
            Double leftMargin = interval.getLeftMargin();
            String rightMarginStr = "null";
            String leftMarginStr = "null";
            if(isTimePickerInput(getVariableName(dF.getName(),dF.getDisplayName()))){
                if(rightMargin!=null)rightMarginStr = getTimeString(rightMargin);
                if(leftMargin!=null)leftMarginStr = getTimeString(leftMargin);
            } else if(dF.getDataType().value().equals("timeSeconds")){
                if(rightMargin!=null){
                    rightMarginStr = getTimeString(rightMargin);
                    rightMarginStr = rightMarginStr.substring(0,rightMarginStr.length()-3);
                }
                if(leftMargin!=null){
                    leftMarginStr = getTimeString(leftMargin);
                    leftMarginStr = leftMarginStr.substring(0, leftMarginStr.length() - 3);
                }
            }else if(dF.getDataType().value().startsWith("dateDaysSince[")){
                LocalDate localDate = null;

                switch(dF.getDataType().value()){
                    case "dateDaysSince[1960]":
                        localDate = new LocalDate(1960,1,1);
                        break;
                    case "dateDaysSince[1970]":
                        localDate = new LocalDate(1970,1,1);
                        break;
                    case "dateDaysSince[1980]":
                        localDate = new LocalDate(1980,1,1);
                        break;
                }

                if(rightMargin!=null)rightMarginStr = localDate.plusDays(rightMargin.intValue()).toString();
                if(leftMargin!=null)leftMarginStr = localDate.plusDays(leftMargin.intValue()).toString();
            }
            else{
                if(rightMargin!=null)rightMarginStr = rightMargin.toString();
                if(leftMargin!=null)leftMarginStr = leftMargin.toString();
            }

            switch (interval.getClosure().value()) {
                case "openClosed":
                    text = "(" + leftMarginStr + " ; " + rightMarginStr + "] ";
                    if(text.startsWith("(null"))text=text.replace("(null","(-"+getString(R.string.infinity_str));
                    errorMessage.append(text);
                    break;
                case "openOpen":
                    text ="(" + leftMarginStr + " ; " + rightMarginStr + ") ";
                    if(text.startsWith("(null"))text=text.replace("(null","(-"+getString(R.string.infinity_str));
                    if(text.endsWith("null) "))text=text.replace("null) ","+"+getString(R.string.infinity_str)+") ");
                    errorMessage.append(text);
                    break;
                case "closedOpen":
                    text = "[" + leftMarginStr + " ; " + rightMarginStr + ") ";
                    if(text.endsWith("null) "))text=text.replace("null) ","+"+getString(R.string.infinity_str)+") ");
                    errorMessage.append(text);
                    break;
                case "closedClosed":
                    errorMessage.append("[" + leftMarginStr + " ; " + rightMarginStr + "] ");
                    break;
            }
        }

        return errorMessage.toString();
    }

    /**
     * Otwiera Activity do wyboru zawodnika
     * @param view
     */
    public void chooseAthlete(View view) {
        Intent athlChooseIntent = new Intent(this,AthletesActivity.class);
        startActivityForResult(athlChooseIntent, 1);
    }

    /**
     * Jeżeli wybrano zawodnika to wprowadza się jego dane do formularza
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1){
            if(resultCode==RESULT_OK){
                Bundle bundle = data.getExtras();
                if(bundle!=null){
                    choosenAthlete = bundle.getParcelable(AthleteModel.ATHL_PARCEL);
                    setChoosenAthleteDetails();
                    fillAthleteData(true);
                    if(isEvaluated)evaluateButton.performClick();
                }
            }
        }
    }

    /**
     * Wpełnia formularz danymi zawodnika
     * @param fill jeśli true to wypełnij danymi wybranego zawodnika
     *             w przeciwnym wypadku wyczyść dane zawodnika
     */
    private void fillAthleteData(boolean fill) {
        if(choosenAthlete!=null&&formCreated){
            //Wypełnianie wartości w Spinner formularza modelu dla płci
            for(FieldName fieldName : AllSpinner.keySet()){
                if(fieldName.toString().toLowerCase().startsWith(getString(R.string.fill_data_sex))){
                    if(!fill){
                        AllSpinner.get(fieldName).setSelection(0);
                        break;
                    }else{
                        Adapter adapter = AllSpinner.get(fieldName).getAdapter();
                        int n = adapter.getCount();
                        String sex = choosenAthlete.getSex().toLowerCase();
                        if(sex.startsWith("k")&&getString(R.string.details_woman).toLowerCase().startsWith("w"))sex="w";
                        for(int i=0;i<n;i++){
                            String adapterItem = adapter.getItem(i).toString().toLowerCase();
                            if(adapterItem.startsWith(sex)){
                                AllSpinner.get(fieldName).setSelection(i);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
            //Wypełnianie wartości w EditText formularza modelu
            for(FieldName fieldName : AllEditText.keySet()){
                String fName = fieldName.toString().toLowerCase();
                if(fName.startsWith("wiek") || fName.startsWith("age")) {
                    if(fill)AllEditText.get(fieldName).setText(""+choosenAthlete.getAge());
                    else AllEditText.get(fieldName).setText("");
                }else if(fName.startsWith("waga") || fName.startsWith("weight")) {
                    if(fill)AllEditText.get(fieldName).setText(String.format(Locale.ROOT,"%.2f",choosenAthlete.getWeight()));
                    else AllEditText.get(fieldName).setText("");
                }else if(fName.startsWith("wzrost") || fName.startsWith("height")) {
                    if(fill){
                        EditText heightEditText = AllEditText.get(fieldName);
                        int heightEditTextInputType = heightEditText.getInputType();
                        if(heightEditTextInputType==InputType.TYPE_CLASS_NUMBER || heightEditTextInputType==(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED)){
                            int height = (int)(choosenAthlete.getHeight()*100);
                            AllEditText.get(fieldName).setText(""+height);
                        }else AllEditText.get(fieldName).setText(String.format(Locale.ROOT,"%.2f",choosenAthlete.getHeight()));
                    }
                    else AllEditText.get(fieldName).setText("");
                }else if(fName.startsWith("bmi")) {
                    if(fill)AllEditText.get(fieldName).setText(String.format(Locale.ROOT,"%.3f",choosenAthlete.getBmi()));
                    else AllEditText.get(fieldName).setText("");
                }else if(fName.startsWith("rezultat") || fName.startsWith("result") || fName.startsWith("current_result")) {
                    if(fill){
                        if(isTimePickerInput(allDisplayNames.get(fieldName)))AllEditText.get(fieldName).setText(choosenAthlete.getRecord());
                        else {
                            String timeRecord = choosenAthlete.getRecord().substring(0,choosenAthlete.getRecord().indexOf("."));
                            AllEditText.get(fieldName).setText(timeRecord);
                        }
                    }
                    else AllEditText.get(fieldName).setText("");
                }
            }
        }
    }

    /**
     * Usuwa informacje o wybranym zawodniku
     * @param view
     */
    public void removeAthlete(View view) {
        choosenAthLabel.setVisibility(View.GONE);
        removeAthButt.setVisibility(View.GONE);
        fillAthleteData(false);
        choosenAthlete=null;
        pdfBtn.setVisibility(View.GONE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(choosenAthlete!=null)outState.putParcelable(AthleteModel.ATHL_PARCEL, choosenAthlete);
        outState.putBoolean("doEvaluate", isEvaluated);

        HashMap<FieldName,String> inputFields = getAllInputsData();
        Log.wtf("Save", "inputFields: "+inputFields.toString());

        outState.putSerializable("dispNames", allDisplayNames);
        outState.putSerializable("OUTPUTSNUMBERS", outputFieldsNumbers);

        if(!inputFields.isEmpty())outState.putSerializable("inputFields",inputFields);
        if(evaluateMode.equals(EvaluateMode.REZULTAT))outState.putSerializable("trainingData",trainingData);
        if(evaluateMode.equals(EvaluateMode.TRENING))outState.putBoolean("roundSwitch",roundSwitch.isChecked());
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if(evaluateMode.equals(EvaluateMode.TRENING)){
            roundSwitch.setSelected(savedInstanceState.getBoolean("roundSwitch"));
        }
        choosenAthlete=savedInstanceState.getParcelable(AthleteModel.ATHL_PARCEL);
        inputData=(HashMap<FieldName,String>)savedInstanceState.getSerializable("inputFields");

        doEvaluate=savedInstanceState.getBoolean("doEvaluate");
        allDisplayNames=(HashMap<FieldName,String>)savedInstanceState.getSerializable("dispNames");
        outputFieldsNumbers=(HashMap<FieldName,Integer>)savedInstanceState.getSerializable("OUTPUTSNUMBERS");
        Log.wtf("DISPY:", allDisplayNames.toString());
        if(evaluateMode.equals(EvaluateMode.REZULTAT))trainingData=(HashMap<FieldName,String>)savedInstanceState.getSerializable("trainingData");
        setChoosenAthleteDetails();
    }

    /**
     * Przejście do trybu predkcji po wygenrowaniu treningu
     * @param view
     */
    public void onGoToPredictClick(View view) {
        Intent predictIntent = new Intent(this,EvaluateActivity.class);
        predictIntent.putExtra("TRYB", "REZULTAT");
        HashMap<FieldName,String> trainingOutput = new HashMap<>();
        for(FieldName fieldName : AllOutputs.keySet()){
            trainingOutput.put(fieldName, AllOutputs.get(fieldName).getText().toString());
        }
        String modelName = modelSpinner.getSelectedItem().toString();
        modelName = modelName.replace(".gtm", ".prm");
        predictIntent.putExtra("MODEL", modelName);
        predictIntent.putExtra("TRENING", trainingOutput);
        if(chartList!=null)predictIntent.putExtra("WYKRESYDEF", chartList);
        if(choosenAthlete!=null)predictIntent.putExtra(AthleteModel.ATHL_PARCEL,choosenAthlete);
        predictIntent.putExtra("DISPNAMES",allDisplayNames);
        predictIntent.putExtra("OUTPUTSNUMBERS",outputFieldsNumbers);
        startActivity(predictIntent);
    }

    /**
     * Wypełnia formularz danymi przesłanymi z modułu GT bądź odzyskanymi po wznownieu Activity
     */
    public void fillInputData(){
        if(inputData!=null){
            Log.wtf("FILL","inputFields: "+inputData.toString());
            for(FieldName fieldName : inputData.keySet()){
                if(!inputData.get(fieldName).isEmpty()) {
                    if (AllEditText.containsKey(fieldName)){
                        Log.wtf("FILL ROW", fieldName + ": " + inputData.get(fieldName));
                        int inputType = AllEditText.get(fieldName).getInputType();
                        if(inputType==(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED) || inputType==InputType.TYPE_CLASS_NUMBER){
                            String inputString = inputData.get(fieldName);
                            double value = Double.parseDouble(inputString);
                            value = Math.round(value);
                            AllEditText.get(fieldName).setText(""+(int)value);
                        }else AllEditText.get(fieldName).setText(inputData.get(fieldName));
                    }
                    else if(AllSpinner.containsKey(fieldName)) {
                        Adapter adapter = AllSpinner.get(fieldName).getAdapter();
                        int count = adapter.getCount();
                        for(int i=0;i<count;i++){
                            if(adapter.getItem(i).toString().equals(inputData.get(fieldName))){
                                AllSpinner.get(fieldName).setSelection(i);
                                break;
                            }
                        }
                    }
                    else if(AllRadio.containsKey(fieldName)) {
                        RadioGroup radioGroup = AllRadio.get(fieldName);
                        String value = inputData.get(fieldName);
                        if(value.equals("true"))radioGroup.check(R.id.form_radio_yes);
                        else if(value.equals("false"))radioGroup.check(R.id.form_radio_no);
                    }
                }
            }
            //To po to żeby po zapisie danych i ich odnowieniu po obrocie nie wpisały się do innego modelu gdy jakieś pola się pokryją
            inputData=null;
        }
        Log.wtf("Before doEvaluate", "doEvaluate: " + doEvaluate + " isEvaluated: " + isEvaluated+" inputData: "+inputData);
        if(doEvaluate){
            Log.wtf("doEvaluate", "click");
            evaluateButton.performClick();
        }
    }

    /**
     * Wpisuje w TextView imię i nazwisko zawodnika oraz ustawia widoczność elemtów widoku
     */
    public void setChoosenAthleteDetails(){
        if(choosenAthlete!=null) {
            choosenAthLabel.setText(getString(R.string.chosen_ath_label) + choosenAthlete.getName() + " " + choosenAthlete.getSurname());
            choosenAthLabel.setVisibility(View.VISIBLE);
            removeAthButt.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Generowanie PDF z treningiem oraz wysłanie ich klientem email do zawodnika
     * @param view
     */
    public void onPdfGenerateClick(View view) {

        int width = 595;
        int height = 842;
        int pageNumber = 1;

        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width,height,pageNumber).create();

        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Paint mainHeaderPaint = new Paint();
        mainHeaderPaint.setTextSize(20);
        mainHeaderPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        mainHeaderPaint.setTextAlign(Paint.Align.CENTER);

        Paint dateHeaderPaint = new Paint();
        dateHeaderPaint.setTextSize(15);
        dateHeaderPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        dateHeaderPaint.setTextAlign(Paint.Align.RIGHT);

        Paint headerPaint = new Paint();
        headerPaint.setTextSize(16);
        headerPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        headerPaint.setTextAlign(Paint.Align.CENTER);

        Paint bodyPaint = new Paint();
        bodyPaint.setTextSize(12);

        Paint bodyPaintRight = new Paint();
        bodyPaintRight.setTextSize(12);
        bodyPaintRight.setTextAlign(Paint.Align.RIGHT);

        Paint bodyPaintBold = new Paint();
        bodyPaintBold.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        bodyPaintBold.setTextSize(12);

        float margin = 20;

        String zawText = getString(R.string.ath_pdf_text);
        float zawTextWidth = bodyPaintBold.measureText(zawText,0,zawText.length());

        String zawodnikName = "" + choosenAthlete.getName() + " " + choosenAthlete.getSurname();
        float nameTextWidth = bodyPaint.measureText(zawodnikName,0,zawodnikName.length());

        float totalTextWidth = zawTextWidth+nameTextWidth;

        String wiekText = getString(R.string.age_pdf_text);

        String bmiText = "BMI: ";

        int yStart = 30;
        String date = String.format("%02d-%02d-", calendar.get(Calendar.DAY_OF_MONTH),calendar.get(Calendar.MONTH)+1)+calendar.get(Calendar.YEAR);
        page.getCanvas().drawText(date, width - margin, yStart, dateHeaderPaint);
        int yPos = 60;
        page.getCanvas().drawText(getString(R.string.training_plan_pdf_text)+pdfModelName, width / 2, yPos, mainHeaderPaint);
        yPos+=40;
        page.getCanvas().drawText(zawText, margin, yPos, bodyPaintBold);
        page.getCanvas().drawText(zawodnikName, zawTextWidth + margin, yPos, bodyPaint);
        page.getCanvas().drawLine(margin, yPos + 5, width - margin, yPos + 5, bodyPaint);
        yPos+=20;
        page.getCanvas().drawText(wiekText, margin, yPos, bodyPaintBold);
        page.getCanvas().drawText("" + choosenAthlete.getAge(), totalTextWidth + margin, yPos, bodyPaintRight);
        page.getCanvas().drawLine(margin, yPos + 5, width - margin, yPos + 5, bodyPaint);
        yPos+=20;
        page.getCanvas().drawText(bmiText, margin, yPos, bodyPaintBold);
        page.getCanvas().drawText(String.format(Locale.ROOT, "%.3f", choosenAthlete.getBmi()), totalTextWidth + margin, yPos, bodyPaintRight);
        page.getCanvas().drawLine(margin, yPos + 5, width - margin, yPos + 5, bodyPaint);
        yPos+=30;
        page.getCanvas().drawText(getString(R.string.training_pdf_text), width / 2, yPos, headerPaint);
        yPos+=30;

        List<String> allChartKeys = new ArrayList<>();
        if(chartList!=null){
            for(GTChart gtChart : chartList){
                //lista wartości zawartych w definicji wykresów
                for(String key : gtChart.getEntries().keySet()){
                    allChartKeys.add(key);
                }
            }
        }

        //Jeżeli użytkownik zmodyfikowałby trening w formularzu predykcji rezultatu to należy
        //wprowadzić te zmiany do tego co pojawi się w PDF
        HashMap<FieldName,String> newTrainingData = new HashMap<>();
        newTrainingData.putAll(trainingData);
        HashMap<FieldName,String> allInputs = getAllInputsData();
        for(FieldName key : trainingData.keySet()){
            if(allInputs.containsKey(key))newTrainingData.put(key,allInputs.get(key));
        }
        trainingData=newTrainingData;

        boolean hasFieldsWithoutChart = false;
        //Jeżeli jakieś pola nie znajdują się na wykresach to wypisujemy je najpierw jeden pod drugim
        for(FieldName fieldName : trainingData.keySet()){
            if(!allChartKeys.contains(fieldName.toString())){
                if(!hasFieldsWithoutChart)hasFieldsWithoutChart=true;
                String fieldText = "("+outputFieldsNumbers.get(fieldName).toString()+") "+allDisplayNames.get(fieldName)+": ";
                float fieldTextWidth = bodyPaintBold.measureText(fieldText,0,fieldText.length());
                page.getCanvas().drawText(fieldText, margin, yPos,bodyPaintBold);
                page.getCanvas().drawText(trainingData.get(fieldName), fieldTextWidth+margin, yPos,bodyPaint);
                page.getCanvas().drawLine(margin, yPos + 5, width - margin, yPos + 5, bodyPaint);
                yPos+=30;

                if(yPos>=height-30){
                    pageNumber++;
                    page=createNewPdfPage(pdfDocument,page,pageNumber);
                    yPos=yStart;
                }
            }
        }
        if(hasFieldsWithoutChart)yPos+=40;
        if(yPos>=height-30){
            pageNumber++;
            page=createNewPdfPage(pdfDocument,page,pageNumber);
            yPos=yStart;
        }

        Paint chartPaint = new Paint();
        chartPaint.setTextAlign(Paint.Align.CENTER);
        int index=0;

        //Rysujemy wykresy oraz odpowiadające im zbiory wartości
        if(chartList!=null){
            pieChartList=createChartViewList();

            for(PieChart pieChart : pieChartList){
                Log.wtf("PDF wykres","OK");
                GTChart chartDef = chartList.get(index);
                Map<String,String> chartEnt =  chartDef.getEntries();
                for(String key : chartEnt.keySet()){
                    FieldName fieldName = new FieldName(key);
                    String fieldText = "("+outputFieldsNumbers.get(fieldName).toString()+") "+allDisplayNames.get(fieldName)+": ";
                    float fieldTextWidth = bodyPaintBold.measureText(fieldText,0,fieldText.length());
                    page.getCanvas().drawText(fieldText, margin, yPos,bodyPaintBold);
                    page.getCanvas().drawText(trainingData.get(fieldName), fieldTextWidth+margin, yPos,bodyPaint);
                    page.getCanvas().drawLine(margin, yPos + 5, width - margin, yPos + 5, bodyPaint);
                    yPos+=30;
                    if(yPos>=height-30){
                        pageNumber++;
                        page=createNewPdfPage(pdfDocument,page,pageNumber);
                        yPos=yStart;
                    }
                }
                if(yPos+200>=height-30){
                    pageNumber++;
                    page=createNewPdfPage(pdfDocument,page,pageNumber);
                    yPos=yStart;
                }
                pieChart.measure(View.MeasureSpec.makeMeasureSpec(2775, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY));
                pieChart.layout(0, 0, pieChart.getMeasuredWidth(), pieChart.getMeasuredHeight());
                Bitmap bitmap = pieChart.getChartBitmap();
                page.getCanvas().drawBitmap(bitmap, null, new Rect(20, yPos, 575, yPos + 200), chartPaint);
                yPos+=230;
                index+=1;
                if(yPos>=height-30){
                    pageNumber++;
                    page=createNewPdfPage(pdfDocument,page,pageNumber);
                    yPos=yStart;
                }
            }
        }

        if(yPos+30>=height-30){
            pageNumber++;
            page=createNewPdfPage(pdfDocument,page,pageNumber);
            yPos=yStart;
        }
        //Wypisujemy predykowany rezultat
        //Najpierw etykieta
        page.getCanvas().drawText(getString(R.string.predicted_result_pdf_text), width / 2, yPos, headerPaint);
        yPos+=30;
        if(yPos>=height-30){
            pageNumber++;
            page=createNewPdfPage(pdfDocument,page,pageNumber);
            yPos=yStart;
        }
        //Następnie zmienna i jej wartość
        for(FieldName fieldName : AllOutputs.keySet()){
            String fieldText = allDisplayNames.get(fieldName)+": ";
            float fieldTextWidth = bodyPaintBold.measureText(fieldText,0,fieldText.length());
            page.getCanvas().drawText(fieldText, margin, yPos,bodyPaintBold);
            page.getCanvas().drawText(AllOutputs.get(fieldName).getText().toString(), fieldTextWidth+margin, yPos,bodyPaint);
            page.getCanvas().drawLine(margin, yPos + 5, width - margin, yPos + 5, bodyPaint);
            yPos+=30;
            if(yPos>=height-30){
                pageNumber++;
                page=createNewPdfPage(pdfDocument,page,pageNumber);
                yPos=yStart;
            }
        }

        pdfDocument.finishPage(page);

        //Tworzymy plik z PDFem
        String fileName = getString(R.string.pdf_file_name);
        File pdfFile = new File(pdfDir,fileName);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(pdfFile,false);
            pdfDocument.writeTo(fileOutputStream);
            MediaScannerConnection.scanFile(this, new String[]{pdfFile.getAbsolutePath()}, null, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioe){
            ioe.printStackTrace();
        }

        pdfDocument.close();

        //Uruchamiamy klienta email z załączonym PDF oraz danymi zawodnika któremu chcemy to wysłać
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/html");
        String[] mailArray = new String[]{choosenAthlete.getEmail()};
        intent.putExtra(Intent.EXTRA_EMAIL, mailArray);
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mail_subject));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.mail_text));
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(pdfFile));

        startActivity(Intent.createChooser(intent, getString(R.string.mail_intent_prompt)));
    }

    /**
     * Zwraca nową stronę dokumentu PDF gdy na poprzedniej nie zmieszczą się już nowe treści
     * @param pdfDocument
     * @param page
     * @param pageNumber
     * @return nowa strona dokumentu PDF
     */
    public PdfDocument.Page createNewPdfPage(PdfDocument pdfDocument, PdfDocument.Page page,int pageNumber){
        int width = 595;
        int height = 842;

        pdfDocument.finishPage(page);
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width,height,pageNumber).create();
        page=pdfDocument.startPage(pageInfo);
        return page;
    }

    /**
     * Zwraca dane wprowadzone w formularzu
     * @return HashMap z danymi wpisanymi formularz. Pod kluczem odpowiadającym nazwie pola
     */
    public HashMap<FieldName,String> getAllInputsData(){
        HashMap<FieldName,String> inputFields = new HashMap<>();
        for(FieldName fieldName : AllEditText.keySet()){
            inputFields.put(fieldName,AllEditText.get(fieldName).getText().toString());
        }
        for(FieldName fieldName : AllRadio.keySet()){
            int radioId = AllRadio.get(fieldName).getCheckedRadioButtonId();
            if(radioId==R.id.form_radio_yes)inputFields.put(fieldName,"true");
            else inputFields.put(fieldName,"false");
        }
        for(FieldName fieldName : AllSpinner.keySet()){
            inputFields.put(fieldName,AllSpinner.get(fieldName).getSelectedItem().toString());
        }
        return inputFields;
    }

    /**
     * Zwraca listę widoków wykresów
     * @return Lista widoków PieChart
     */
    public List<PieChart> createChartViewList(){
        List<PieChart> pieChartList = new ArrayList<>();

        for(GTChart chartDef : chartList){
            PieChart pieChart = new PieChart(this);
                ArrayList<PieEntry> pieEntries = new ArrayList<>();

                Map<String,String> entriesMap = chartDef.getEntries();
                for(String entryKey : entriesMap.keySet()){
                    FieldName fieldName = new FieldName(entryKey);
                    if(evaluateMode.equals(EvaluateMode.TRENING)){
                        if(AllOutputs.containsKey(fieldName)){
                            String outputString = AllOutputs.get(fieldName).getText().toString();
                            Log.wtf("outputString",""+outputString);
                            //Jeżeli mamy doczynienie z wartością sformatowaną na czas to należy to przeliczyć na wartość liczbową
                            if(outputString.contains(":")){
                                Log.wtf("Parse","Wartosc: "+getTimeDouble(outputString));
                                Float value = new Float(getTimeDouble(outputString).toString());
                                pieEntries.add(new PieEntry(value,outputFieldsNumbers.get(fieldName).toString()));
                            }else {
                                pieEntries.add(new PieEntry(Float.parseFloat(outputString),outputFieldsNumbers.get(fieldName).toString()));
                            }
                        }
                    }else{
                        if(trainingData.containsKey(fieldName)){
                            String outputString = trainingData.get(fieldName);
                            //Jeżeli mamy doczynienie z wartością sformatowaną na czas to należy to przeliczyć na wartość liczbową
                            if(outputString.contains(":")){
                                Float value = new Float(getTimeDouble(outputString).toString());
                                pieEntries.add(new PieEntry(value, outputFieldsNumbers.get(fieldName).toString()));
                            }else {
                                pieEntries.add(new PieEntry(Float.parseFloat(outputString), outputFieldsNumbers.get(fieldName).toString()));
                            }
                        }
                    }
                }
                if(pieEntries.size()>0){
                    PieDataSet pieDataSet = new PieDataSet(pieEntries,chartDef.getName());

                    List<Integer> colors = new ArrayList<>();
                    for(int color : ColorTemplate.VORDIPLOM_COLORS){
                        colors.add(color);
                    }
                    for(int color : ColorTemplate.PASTEL_COLORS){
                        colors.add(color);
                    }
                    for(int color : ColorTemplate.MATERIAL_COLORS){
                        colors.add(color);
                    }
                    for(int color : ColorTemplate.COLORFUL_COLORS){
                        colors.add(color);
                    }
                    pieDataSet.setColors(colors);
                    Log.wtf("Colors", "size: " + colors.size());
                    PieData pieData = new PieData(pieDataSet);
                    pieData.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                            return String.format(Locale.ROOT, "%.2f", value);
                        }
                    });
                    pieData.setDrawValues(false);
                    pieChart.setDrawEntryLabels(false);
                    pieChart.setTransparentCircleRadius(0f);
                    pieChart.setHoleRadius(0f);
                    pieChart.setData(pieData);
                    Legend l = pieChart.getLegend();
                    l.setPosition(Legend.LegendPosition.ABOVE_CHART_CENTER);
                    l.setXEntrySpace(7f);
                    l.setYEntrySpace(0f);
                    l.setYOffset(0f);
                    l.setTextColor(Color.BLACK);
                    pieChart.setCenterTextSize(12f);
                    pieChart.invalidate();
                    pieChart.setDescription("");
                    pieChart.setRotationEnabled(false);
                    pieChart.setPadding(0, 50, 0, 50);
                    pieChartList.add(pieChart);
            }
        }
        return pieChartList;
    }

    /**
     * Pokazuje okno dialogowe z informacją o błędzie
     * @param errorMsg wiadomość o błędzie
     */
    public void alertError(String errorMsg) {
        alertDialog.setMessage(errorMsg);
        alertDialog.create();
        alertDialog.show();
    }

    /**
     * Sprawdza czy wejście formularza jest TimePickerWithSeconds
     * @param displayName etykieta pola formularza
     * @return true or false
     */
    public boolean isTimePickerInput(String displayName){
        return displayName.endsWith("[czas]")||displayName.endsWith("[time]");
    }
}
