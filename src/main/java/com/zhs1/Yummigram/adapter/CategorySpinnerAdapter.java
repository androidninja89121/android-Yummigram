package com.zhs1.Yummigram.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhs1.Yummigram.R;
import com.zhs1.Yummigram.global.Global;

/**
 * Created by Administrator on 7/17/2015.
 */
public class CategorySpinnerAdapter extends BaseAdapter {
    private Context mContext;
    String[] arrCategory = {"All Recipes", "Vegan", "Vegeterian", "Meat", "Fish", "Bakery", "Sweets"};

    public CategorySpinnerAdapter(Context context) {
        mContext = context;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public int getCount() {
        return arrCategory.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View mySpinner = inflater.inflate(R.layout.cell_category_spinner, parent, false);
        TextView main_text = (TextView) mySpinner .findViewById(R.id.tvTitleCategoryCell);

        main_text.setText(arrCategory[position]);
        main_text.setTextColor(Color.WHITE);

        Global.strCategory = arrCategory[position];

        return mySpinner;
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View mySpinner = inflater.inflate(R.layout.cell_category_spinner, parent, false);
        TextView main_text = (TextView) mySpinner .findViewById(R.id.tvTitleCategoryCell);
        main_text.setText(arrCategory[position]);

        if(Global.strCategory.equals(arrCategory[position])){
            main_text.setTextColor(Color.WHITE);
        }else{
            main_text.setTextColor(Color.LTGRAY);
        }

        return mySpinner;
    }
}
