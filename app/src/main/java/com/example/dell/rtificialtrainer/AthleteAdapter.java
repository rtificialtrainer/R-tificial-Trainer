package com.example.dell.rtificialtrainer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Customowy adapter do wyświetlania danych zawodników w ListView
 */
public class AthleteAdapter extends ArrayAdapter<AthleteModel> implements Filterable {

    private List<AthleteModel> athletes;
    //na potrzeby filtra
    private List<AthleteModel> originalAthletes;

    //contextual
    private SparseBooleanArray SelectedArray = new SparseBooleanArray();
    //

    private Context context;

    public AthleteAdapter(Context context, ArrayList<AthleteModel> athletes) {
        super(context, R.layout.athletes_list_layout);
        this.athletes = athletes;
        this.context = context;
    }

    @Override
    public int getCount() {
        return athletes.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        AthleteHolder athleteHolder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(R.layout.athletes_list_layout, null);

            athleteHolder = new AthleteHolder();
            athleteHolder.namesurname = (TextView) row.findViewById(R.id.name_list);

            row.setTag(athleteHolder);
        } else {
            athleteHolder = (AthleteHolder) row.getTag();
        }
        //kolorowanie selekcji
        if(SelectedArray.get(position,false)){
            row.setBackgroundColor(context.getResources().getColor(R.color.actionModeSelectColor));
        }
        else row.setBackgroundColor(Color.TRANSPARENT);

        AthleteModel athleteModel = athletes.get(position);
        athleteHolder.namesurname.setText(athleteModel.getName() + " " + athleteModel.getSurname());

        return row;
    }

    static class AthleteHolder {
        private TextView namesurname;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                athletes=(List<AthleteModel>)results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<AthleteModel> filteredAthletes = new ArrayList<AthleteModel>();

                if (originalAthletes == null) {
                    originalAthletes = new ArrayList<>(athletes);
                }
                if (constraint == null || constraint.length() == 0) {
                    results.count = originalAthletes.size();
                    results.values = originalAthletes;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < originalAthletes.size(); i++) {
                        AthleteModel ath = originalAthletes.get(i);
                        if (ath.getName().toLowerCase().startsWith(constraint.toString()) || ath.getSurname().toLowerCase().startsWith(constraint.toString())) {
                            filteredAthletes.add(ath);
                        }
                    }
                    results.count = filteredAthletes.size();
                    results.values = filteredAthletes;
                }
                return results;
            }
        };
        return filter;
    }

    public AthleteModel getItem(int position){

        return athletes.get(position);
    }

    public void setSelection(int position, boolean value){
        SelectedArray.put(position, value);
        notifyDataSetChanged();
    }

    public void removeSelection(int position){
        SelectedArray.delete(position);
        notifyDataSetChanged();
    }

    public void clearSelection(){
        SelectedArray = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public boolean allIsChecked(int count) {
        for (int i = 0; i < count; i++) {
            if (!SelectedArray.get(i, false)) return false;
        }
        return true;
    }

    public String[] athletesToDelete(){
        int size=SelectedArray.size();
        ArrayList<String> id = new ArrayList<String>();
        Integer position;

        for(int i=0;i<size;i++){
            position=SelectedArray.keyAt(i);
            id.add(String.valueOf(athletes.get(position).getId()));
        }
        String[] result = new String[id.size()];
        id.toArray(result);

        return result;
    }

    public int selectedsize(){
        return SelectedArray.size();
    }
}
