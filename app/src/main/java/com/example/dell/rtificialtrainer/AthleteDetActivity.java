package com.example.dell.rtificialtrainer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class AthleteDetActivity extends AppCompatActivity {

    //Pola tekstowe
    TextView name;//imie
    TextView surname;//nazwiko
    TextView id;//id w bazie
    TextView sex;//płeć
    TextView age;//wiek
    TextView bmi;//bmi
    TextView weight;//waga
    TextView height;//wzrost
    TextView record;//rekord
    TextView email;//email

    MyDb myDb;//obiekt klasy realizującej CRUD
    private AlertDialog.Builder dialogDet;//okno dialogowe
    AthleteModel athlete;//referencja na obiekt zawodnika do podglądu
    Toast toast;
    Intent returnIntent;//do sprawdzania stanu w jakim zakończyło się inne Activity
    boolean isUpdate;//czy dokonałem aktualizacji danych rekordu
    public static final String ATHL_PARCEL = "athlete_parcel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_athlete_det);

        returnIntent = new Intent();
        myDb = new MyDb(this);
        isUpdate=false;//jeżeli dokonałem aktualizacji informacji na temat zawodnika to na podstawie tej wartości inaczej obsługuję
        //powrót home'em aby ListView się aktualizował

        name = (TextView)findViewById(R.id.athlete_name);
        surname = (TextView)findViewById(R.id.athlete_surname);
        id = (TextView)findViewById(R.id.athlete_id);
        sex = (TextView)findViewById(R.id.athlete_sex);
        age = (TextView)findViewById(R.id.athlete_age);
        bmi = (TextView)findViewById(R.id.athlete_bmi);
        weight = (TextView)findViewById(R.id.athlete_weight);
        height = (TextView)findViewById(R.id.athlete_height);
        record = (TextView)findViewById(R.id.athlete_record);
        email = (TextView)findViewById(R.id.athlete_email);

        dialogDet = new AlertDialog.Builder(this);
        dialogDet.setMessage(getString(R.string.loading_data_err_msg));
        dialogDet.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        dialogDet.create();

        Bundle bundle = getIntent().getExtras();
        //odebranie Parcelable z zawodnikiem wybranym na ListView
        if(bundle!=null){
            athlete = bundle.getParcelable(ATHL_PARCEL);
            updateDetails();
        }else {
            dialogDet.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.athletes_det_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            //jeżeli isUpdate to ListView w AthletesActivity się zaktualizuje
            case android.R.id.home:
                if(isUpdate){
                    setResult(RESULT_OK, returnIntent);
                } else {
                    setResult(RESULT_CANCELED, returnIntent);
                }
                finish();
                return  true;

            case R.id.option_det_edit:
                onEditClick();
                return true;
            case R.id.option_det_delete:
                onDeleteClick();
                return true;
        }
        return true;
    }

    /**
     * Otworzenie Activity do edycji zawodnika
     */
    private void onEditClick() {
        Intent intent = new Intent(this,AthleteFormActivity.class);
        intent.putExtra(AthleteFormActivity.FORM_PARCEL, (Parcelable) athlete);
        startActivityForResult(intent, 1);//result z FormActivity o id 2
        //Activity z oczekiwaniem na rezultat
    }

    /**
     * Usunięcie zawodnika z bazy danych
     */
    public void onDeleteClick(){

        AlertDialog.Builder dialogDel;
        dialogDel = new AlertDialog.Builder(this);
        dialogDel.setMessage(getString(R.string.del_ath_confirm_msg));
        dialogDel.setPositiveButton(getString(R.string.alert_pos_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int id=0;
                if(athlete!=null)id=athlete.getId();
                int result = myDb.deleteAthlete(id);

                if (result == 0) {
                    toast.makeText(getApplicationContext(), getString(R.string.del_ath_err_msg), Toast.LENGTH_SHORT).show();
                } else if (result == 1) {
                    toast.makeText(getApplicationContext(), getString(R.string.del_ath_succes_msg), Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK, returnIntent);//ret do AthletesActivity po udanym delete
                    //info że ma aktualizować swój widok
                    finish();
                }
            }
        });
        dialogDel.setNegativeButton(getString(R.string.alert_neg_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialogDel.create();
        dialogDel.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //aktualizacja wyświetlanych danych po ich edycji
        //requestCode 2 pochodzi z Form Activity
        if(requestCode==1){
            //OK czy była zmiana trzeba update robić
            if(resultCode==RESULT_OK){
                isUpdate=true;
                Bundle extra = data.getExtras();
                if(extra!=null){
                    athlete=extra.getParcelable(ATHL_PARCEL);
                    updateDetails();
                }
            }
        }
    }

    /**
     * Wprowadzenie danych o zawodniku do pól tekstowych
     */
    public void updateDetails(){
        this.getSupportActionBar().setTitle(athlete.getName()+" "+athlete.getSurname());
        name.setText(athlete.getName());
        surname.setText(athlete.getSurname());
        id.setText(String.valueOf(athlete.getId()));

        if(athlete.getSex().equals("K"))sex.setText(getString(R.string.details_woman));
        else sex.setText(getString(R.string.details_man));

        bmi.setText(String.format(Locale.ROOT,"%.3f",athlete.getBmi()));
        weight.setText(String.valueOf(athlete.getWeight())+" [kg]");
        height.setText(String.valueOf(athlete.getHeight())+" [m]");
        age.setText(String.valueOf(athlete.getAge()));
        record.setText(athlete.getRecord());
        email.setText(athlete.getEmail());
    }
}
