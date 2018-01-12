package com.example.dell.rtificialtrainer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class AthletesActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, AbsListView.MultiChoiceModeListener {

    private ArrayList<AthleteModel> athletes;//Lista zawodników pobrana z bazy danych
    private AthleteAdapter adapter;//Adapter do wyświetlenia w ListView
    private ListView listView;//Widok zawierający zawodników
    private MyDb myDb;//obiekt klasy odpowiadającej za operacje CRUD na bazie zawodników
    private SearchView searchView;//Pole wyszukiwania zawodników
    private ActionMode myActionMode=null;//tryb kontekstowy UI

    String searchFilter;//wyszukiwana fraza

    TextView empty_base_text;//napis o braku zawodników

    private int nr = 0;//ile zaznaczono do usunięcia
    private boolean all;//czy wszystkich zaznaczono

    private boolean fromEvaluateActivity = false;//czy z modułu ewaluacji

    Toast toast;//informacja dla uzytkownika

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_athletes);

        empty_base_text = (TextView)findViewById(R.id.empty_base_text);

        myDb = new MyDb(this);
        listView=(ListView)findViewById(R.id.athleteslistview);
        athletes = new ArrayList<>();

        updateListViewItems();

        //contextual menu
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(this);

        listView.setOnItemLongClickListener(this);
        listView.setOnItemClickListener(this);

        //Z jakiego Activity zostało uruchomione
        if(getCallingActivity()!=null){
            Log.wtf("AthletesActivity callingAct", getCallingActivity().getShortClassName());
            if(getCallingActivity().getShortClassName().equals(".EvaluateActivity")){
                fromEvaluateActivity =true;
                Log.wtf("AthletesActivity flaga", ""+ fromEvaluateActivity);
                getSupportActionBar().setTitle(getString(R.string.select_ath_text));

            }else fromEvaluateActivity =false;
        }else fromEvaluateActivity =false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                if(!searchView.isIconified())searchView.onActionViewCollapsed();
                else finish();
                break;
            case R.id.option_add:
                Intent athlform = new Intent(this,AthleteFormActivity.class);
                startActivityForResult(athlform, 2);//result z Form pod add
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.athletes_activity_menu, menu);
        searchView = (SearchView)menu.findItem(R.id.option_search).getActionView();
        if(searchView!=null)searchView.setOnQueryTextListener(this);
        if(fromEvaluateActivity) {
            menu.getItem(1).setEnabled(false);
            menu.getItem(1).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if(!athletes.isEmpty()) {
            adapter.getFilter().filter(query);
            searchView.clearFocus();
            searchFilter=query;
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(!athletes.isEmpty()){
            adapter.getFilter().filter(newText);
            searchFilter=newText;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AthleteModel athlete = adapter.getItem(position);
        if(!fromEvaluateActivity) {
            Intent intent = new Intent(this,AthleteDetActivity.class);
            intent.putExtra(AthleteDetActivity.ATHL_PARCEL, athlete);
            startActivityForResult(intent, 1);//result z DetActivity
        }else {
            //Zwróc dane wybranego zawodnika do modułu GT lu PR
            Intent returnIntent = new Intent();
            returnIntent.putExtra(AthleteModel.ATHL_PARCEL, athlete);
            setResult(RESULT_OK, returnIntent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //jeżeli z Details i OK to odśwież bo znaczy że usuwałeś
        //lub edytowałeś

        if(requestCode==1){
            if(resultCode==RESULT_OK){
                updateListViewItems();
                if(!searchView.isIconified())adapter.getFilter().filter(searchFilter);
            }
        }
        //2 znaczy że dodawałeś
        if(requestCode==2){
            if(resultCode==RESULT_OK){
                updateListViewItems();
            }
        }
    }

    /**
     * Odświeżanie zawarości listy zawodników
     */
    public void updateListViewItems(){

        athletes=myDb.getAthletes();

        if(athletes!=null&&!athletes.isEmpty()) {
            adapter = new AthleteAdapter(this, athletes);
            listView.setAdapter(adapter);
            listView.setVisibility(View.VISIBLE);
            empty_base_text.setVisibility(View.GONE);
        } else {
            listView.setVisibility(View.GONE);
            empty_base_text.setVisibility(View.VISIBLE);
            if (athletes==null)toast.makeText(getApplicationContext(), getString(R.string.read_error_toast), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        listView.setItemChecked(position,true);
        return false;
    }

    /**
     * Usuwanie wielu zawodników
     * @param nr ilość zaznaczonych rekordów
     */
    public void onMultipleDeleteClick(int nr){
        AlertDialog.Builder dialogDel;
        dialogDel = new AlertDialog.Builder(this);
        if(nr==1)dialogDel.setMessage(getString(R.string.del_ath_confirm_msg));
        else dialogDel.setMessage(getString(R.string.del_mult_ath_confirm_msg) + nr + getString(R.string.del_mult_ath_confirm_msg_ending));
        dialogDel.setPositiveButton(getString(R.string.alert_pos_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int result = myDb.deleteMultiAthletes(adapter.athletesToDelete());
                if (result == 0) {
                    toast.makeText(getApplicationContext(), getString(R.string.del_ath_err_msg), Toast.LENGTH_SHORT).show();
                } else if (result == 1) {
                    toast.makeText(getApplicationContext(), getString(R.string.del_ath_succes_msg), Toast.LENGTH_SHORT).show();
                    myActionMode.finish();
                    updateListViewItems();
                } else if (result > 1) {
                    toast.makeText(getApplicationContext(), getString(R.string.del_mult_ath_succ_msg) + result + getString(R.string.del_mult_ath_succ_msg_ending), Toast.LENGTH_SHORT).show();
                    myActionMode.finish();
                    updateListViewItems();
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
    //Contextual menu
    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        if (checked) {
            nr++;
            adapter.setSelection(position,true);
        } else {
            nr--;
            adapter.removeSelection(position);
        }
        mode.setTitle(getString(R.string.bar_select_ath_counter) + nr);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        nr = 0;
        getMenuInflater().inflate(R.menu.contextual_menu, menu);
        myActionMode=mode;
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.contex_delete:
                onMultipleDeleteClick(nr);
                break;
            case R.id.contex_all:
                all=adapter.allIsChecked(listView.getCount());
                //Jeśli wszyscy zaznaczeni i znów klikasz wszyscy to odznacz wszystkich
                //w przeciwnym wypadku zaznacz wszytskich
                if(all){
                    int count = listView.getCount();
                    for(int i=0;i<count;i++){
                        listView.setItemChecked(i,false);
                    }
                } else{
                    nr=0;
                    int count = listView.getCount();
                    for(int i=0;i<count;i++){
                        listView.setItemChecked(i,true);
                    }
                }
                break;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        adapter.clearSelection();
        myActionMode=null;
        if(searchView!=null)searchView.onActionViewCollapsed();
    }

    @Override
    public void onBackPressed() {
        if(searchView!=null){
            if(!searchView.isIconified())searchView.onActionViewCollapsed();
            else super.onBackPressed();
        } else super.onBackPressed();
    }
}
