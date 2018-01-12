package com.example.dell.rtificialtrainer;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.Calendar;

public class AthleteFormActivity extends AppCompatActivity {

    EditText name;//wejścia na imie
    EditText surname;//wejścia na nazwisko
    EditText dateOfBirth;//wejścia date
    EditText height;//wejścia na wzrost
    EditText weight;//wejścia na wage
    EditText record;//wejścia na rekord
    EditText email;//wejścia na email
    RadioGroup sex;//wejścia na płec
    private AlertDialog.Builder dialogForm;//okno dialogowe
    MyDb myDb;//obiekt klasy odpowiadającej za CRUD

    Button addbut;//przycisk dodaj zawodnika
    Button editbut;//przycisk edytuj zawodnika
    Button restbut;//przycisk przywróć domyślne

    Intent returnIntent;//rezultaty

    public static final String FORM_PARCEL = "form_parcel";//id extra zawierającego parcel zawodnika - wtedy jest Edycja a nie dodawanie

    AthleteModel edit_athlete;//referencja na obiekt zawodnika do edycji w tym trybie

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_athlete_form);

        myDb = new MyDb(this);

        name = (EditText) findViewById(R.id.form_name);
        surname = (EditText) findViewById(R.id.form_surname);

        dateOfBirth = (EditText) findViewById(R.id.form_dateOfBirth);
        dateOfBirth.setTextIsSelectable(false);
        dateOfBirth.setInputType(InputType.TYPE_NULL);
        Calendar calendar = Calendar.getInstance();

        height = (EditText) findViewById(R.id.form_height);
        weight = (EditText) findViewById(R.id.form_weight);
        sex = (RadioGroup) findViewById(R.id.form_sex);

        record = (EditText) findViewById(R.id.form_record);
        record.setTextIsSelectable(false);
        record.setInputType(InputType.TYPE_NULL);

        final com.example.dell.rtificialtrainer.MyTimePickerDialog myTimePickerDialog = new com.example.dell.rtificialtrainer.MyTimePickerDialog(this, new com.example.dell.rtificialtrainer.MyTimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(com.example.dell.rtificialtrainer.TimePicker view, int hourOfDay, int minute, int seconds, int cseconds) {
                record.setText(String.format("%02d",hourOfDay)+":"+String.format("%02d",minute)+":"+String.format("%02d",seconds)+"."+String.format("%02d",cseconds));
                email.requestFocus();
            }
        },0,0,0,0,true);
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myTimePickerDialog.show();
            }
        });
        record.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    myTimePickerDialog.show();
                }
            }
        });


        email = (EditText) findViewById(R.id.form_email);

        dialogForm = new AlertDialog.Builder(this);
        dialogForm.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        addbut = (Button) findViewById(R.id.form_add_but);
        editbut = (Button) findViewById(R.id.form_update_but);
        restbut = (Button) findViewById(R.id.form_restore_but);

        Bundle bundle = getIntent().getExtras();

        returnIntent = new Intent();

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        //jeśli coś otrzymał to znaczy że jest Edycja i pobieram zawodnika do wstawienia danych w formularz w celu ich edycji
        //w przeciwnym wypadku formularz został otworzony w celu dodania nowego zawodnika
        if (bundle != null) {
            edit_athlete = bundle.getParcelable(FORM_PARCEL);
            this.getSupportActionBar().setTitle(R.string.editFormActionBar);
            addbut.setVisibility(View.GONE);
            editbut.setVisibility(View.VISIBLE);
            restbut.setVisibility(View.VISIBLE);
            fillMyForm();
            String[] dateParts = edit_athlete.getDateOfBirth().split("-");
            day = Integer.parseInt(dateParts[0]);
            month = Integer.parseInt(dateParts[1])-1;
            year = Integer.parseInt(dateParts[2]);

        } else {
            this.getSupportActionBar().setTitle(R.string.addFormActionBar);
            addbut.setVisibility(View.VISIBLE);
            editbut.setVisibility(View.GONE);
            restbut.setVisibility(View.GONE);
        }

        final DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                dateOfBirth.setText(String.format("%02d",dayOfMonth)+"-"+String.format("%02d",monthOfYear+1)+"-"+year);
                weight.requestFocus();
            }
        }, year, month, day);

        dateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });
        dateOfBirth.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    datePickerDialog.show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
        }
        return true;
    }

    /**
     * Obsługa przycisku czyszczenia formularza
     * @param view
     */
    public void clearAthleteForm(View view) {
        AlertDialog.Builder dialogClear = new AlertDialog.Builder(this);
        dialogClear.setMessage(getString(R.string.clear_form_confirm_txt));
        dialogClear.setPositiveButton(getString(R.string.alert_pos_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearMyForm();
            }
        });
        dialogClear.setNegativeButton(getString(R.string.alert_neg_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialogClear.create();
        dialogClear.show();
    }

    /**
     * Obsługa przycisku dodawania nowego zawodnika
     * @param view
     */
    public void addAthleteBut(View view) {
        AthleteModel athlete = parseMyForm();
        if (athlete != null) {

            long result = myDb.insertAthlete(athlete);

            Toast toast;

            if (result == -1) {
                toast = Toast.makeText(this, getString(R.string.add_athlete_error_txt), Toast.LENGTH_SHORT);
            } else {
                toast = Toast.makeText(this, getString(R.string.add_athlete_succ_txt), Toast.LENGTH_SHORT);
                clearMyForm();
                email.clearFocus();
                setResult(RESULT_OK, returnIntent);//dodano info dla Listy
            }

            toast.show();
        }
    }

    /**
     * Czyści formularz
     */
    public void clearMyForm() {
        name.setText("");
        surname.setText("");
        dateOfBirth.setText("");
        height.setText("");
        weight.setText("");
        sex.check(R.id.form_woman);
        record.setText("");
        email.setText("");
    }

    /**
     * Sprawdza czy tekst zawiera tylko dozwolone znaki
     * @param text tekst do sprawdzenia
     * @param flag 1 dla imion (same litery różnych języków) 2 dla nazwisk (czy zaczyna się od znaku oraz dopuszcza " ", "-" oraz "'")
     *             stosowane w różnych językach
     * @return true jeśli spełnia kryteria false jeśli nie
     */
    public boolean onlyLetters(String text, int flag) {

        boolean result = false;

        if (flag == 1) {
            result = text.matches("\\p{L}+");
        }
        if (flag == 2) {
            String test = String.valueOf(text.charAt(0));
            if (test.matches("\\p{L}+")) {
                result = text.matches("^([ \\u00c0-\\u01ffa-zA-Z'\\-])+$");
            } else {
                result = false;
            }
        }
        return result;
    }

    /**
     * Zwraca tekst z zamienioną pierwszą literą na dużą
     * @param text
     * @return Zwraca tekst z zamienioną pierwszą literą na dużą
     */
    public String firstToUpper(String text) {
        if (!text.isEmpty()) {
            return Character.toUpperCase(text.charAt(0)) + text.substring(1);
        }
        return text;
    }

    /**
     * Otwiera okno dialogowe z komunikatem dla uzytkownika
     * @param text tekst komunikatu
     */
    public void formAlert(String text) {
        dialogForm.setMessage(text);
        dialogForm.create();
        dialogForm.show();
    }

    /**
     * Aktualizuje dane zawodnika w bazie
     */
    public void updateAthlete() {
        AthleteModel athlete = parseMyForm();
        if (athlete != null) {

            Toast toast;

            if (edit_athlete.equals(athlete)) {
                toast = Toast.makeText(this, getString(R.string.ath_already_actual_msg), Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            athlete.setId(this.edit_athlete.getId());
            long result = myDb.updateAthlete(athlete);

            if (result == 0) {
                toast = Toast.makeText(this, getString(R.string.db_error_msg), Toast.LENGTH_SHORT);
            } else {
                toast = Toast.makeText(this, getString(R.string.ath_updated_msg), Toast.LENGTH_SHORT);
                returnIntent.putExtra(AthleteDetActivity.ATHL_PARCEL, (Parcelable) athlete);
                setResult(RESULT_OK, returnIntent);//rezultat dla Details że ma zmodyfikować dane po edycji  id 2
                finish();
            }

            toast.show();
        }
    }

    /**
     * Przetwarza dane wprowadzone w formualrzu
     * @return obiekt klasy AthleteModel reprezentujący zawodnika
     */
    public AthleteModel parseMyForm() {
        AthleteModel athlete;

        String name_value = firstToUpper(name.getText().toString().trim());
        String surname_value = firstToUpper(surname.getText().toString().trim());
        String email_value = email.getText().toString().trim();
        String record_value = record.getText().toString().trim();

        String sex_value;
        if (sex.getCheckedRadioButtonId() == R.id.form_woman) {
            sex_value = "K";
        } else sex_value = "M";


        if (name_value.isEmpty()
                || surname_value.isEmpty()
                || dateOfBirth.getText().toString().isEmpty()
                || weight.getText().toString().isEmpty()
                || height.getText().toString().isEmpty()
                || email_value.isEmpty()
                || record_value.isEmpty()) {
            formAlert(getString(R.string.fill_all_fields_error_msg));
            return null;
        }

        if(record_value.equals("00:00:00.00")){
            formAlert(getString(R.string.record_value_error));
            return null;
        }

        String dateOfBirth_value = dateOfBirth.getText().toString();
        if(getYearsFromDate(dateOfBirth_value)<5){
            formAlert(getString(R.string.age_value_error));
            return null;
        }

        float weight_value = Float.valueOf(weight.getText().toString());
        float height_value = Float.valueOf(height.getText().toString());

        if (!onlyLetters(name_value, 1)) {
            formAlert(getString(R.string.name_value_error));
            return null;
        }

        if (!onlyLetters(surname_value, 2)) {
            formAlert(getString(R.string.surname_value_error));
            return null;
        }

        if (weight_value == 0) {
            formAlert(getString(R.string.weight_value_error));
            return null;
        }
        if (height_value == 0) {
            formAlert(getString(R.string.height_value_0_error));
            return null;
        }
        if (height_value >= 3) {
            formAlert(getString(R.string.height_value_error));
            return null;
        }

        if(!isEmailValid(email_value)){
            formAlert(getString(R.string.email_value_error));
            return null;
        }

        athlete = new AthleteModel(name_value,surname_value,sex_value,weight_value,height_value,email_value,record_value,dateOfBirth_value);
        return athlete;
    }

    /**
     * Waliduje podany adres email
     * @param email adres do sprawdzenia
     * @return falge true gdy prawidłowy bądź false gdy nie
     */
    public boolean isEmailValid(String email){
        if(!email.contains("@"))return false;
        if(!email.substring(email.lastIndexOf("@")).contains("."))return false;
        return true;
    }

    /**
     * Wypełnia formularz danymi zawodnika w trybie edycji
     */
    public void fillMyForm() {
        name.setText(edit_athlete.getName());
        surname.setText(edit_athlete.getSurname());
        dateOfBirth.setText(String.valueOf(edit_athlete.getDateOfBirth()));
        height.setText(String.valueOf(edit_athlete.getHeight()));
        weight.setText(String.valueOf(edit_athlete.getWeight()));
        if (edit_athlete.getSex().equals("K")) {
            sex.check(R.id.form_woman);
        } else {
            sex.check(R.id.form_man);
        }
        record.setText(edit_athlete.getRecord());
        email.setText(edit_athlete.getEmail());
    }

    /**
     * Obsługa przycisku przywracania domyślnych danych zawodnika
     * @param view
     */
    public void restoreAthleteBut(View view) {
        AlertDialog.Builder dialogRestore = new AlertDialog.Builder(this);
        dialogRestore.setMessage(getString(R.string.confirm_restore_form_msg));
        dialogRestore.setPositiveButton(getString(R.string.alert_pos_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fillMyForm();
            }
        });
        dialogRestore.setNegativeButton(getString(R.string.alert_neg_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialogRestore.create();
        dialogRestore.show();
    }

    /**
     * Obsługa przycisku aktualizowania danych zawodnika
     * @param view
     */
    public void updateAthleteBut(View view) {
        AlertDialog.Builder dialogUpdate = new AlertDialog.Builder(this);
        dialogUpdate.setMessage(getString(R.string.confirm_modify_form_msg));
        dialogUpdate.setPositiveButton(getString(R.string.alert_pos_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateAthlete();
            }
        });
        dialogUpdate.setNegativeButton(getString(R.string.alert_neg_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialogUpdate.create();
        dialogUpdate.show();
    }

    /**
     * Zwraca ilość lat od podanej daty do teraz
     * @param date
     * @return
     */
    public int getYearsFromDate(String date){
        Calendar today = Calendar.getInstance();
        String[] dateParts = date.split("-");
        int bDay = Integer.parseInt(dateParts[0]);
        int bMonth = Integer.parseInt(dateParts[1]);
        int bYear = Integer.parseInt(dateParts[2]);
        int age = today.get(Calendar.YEAR) - bYear;
        if (bMonth > today.get(Calendar.MONTH)+1 ||
                (bMonth == today.get(Calendar.MONTH)+1 && bDay > today.get(Calendar.DATE))) {
            age--;
        }
        return age;
    }
}
