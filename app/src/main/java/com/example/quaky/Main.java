package com.example.quaky;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;


public class Main extends AppCompatActivity {

    private static final String TAG = "Main";

    String urlString = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=2020-01-01&endtime=2020-01-01&minmagnitude=5";
    ArrayList<EarhtqueClass> originalArrayList = new ArrayList<>();

    Comparator<EarhtqueClass> compareMag = new Comparator<EarhtqueClass>() {
        @Override
        public int compare(EarhtqueClass o1, EarhtqueClass o2) {
            return Double.compare(o1.getMag(), o2.getMag());
        }
    };

    Comparator<EarhtqueClass> compareCountry = new Comparator<EarhtqueClass>() {
        @Override
        public int compare(EarhtqueClass o1, EarhtqueClass o2) {
            return getCountry(o1.getLocation()).compareTo(getCountry(o2.getLocation()));
        }
    };

    int responsecode;
    boolean ascendant1 = false;
    boolean ascendant2 = true;
    String[] orderList = {"by date(default)", "by magnitude", "by Country"};
    String[] magnitudes = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};
    int minMagValue = 2;
    int maxMagValue = 8;
    String selectedStartDate = "2020-05-01";
    String selectedEndDate = "2020-06-01";
    String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(new GregorianCalendar().getTime());
    Country selectedCountry = new Country("All countries", 0, 0, 0, 0);




    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        final EarthquakeAsyncTask[] task = {new EarthquakeAsyncTask()};
        task[0].execute();

        final ListView myList = findViewById(R.id.myList);
        final Spinner spinner1 = findViewById(R.id.order1);
        final Spinner spinner2 = findViewById(R.id.order2);
        final Button applyOrder = findViewById(R.id.apply_order);
        Button cancelOrder = findViewById(R.id.cancel_order_button);
        Button resetOrder = findViewById(R.id.reset_order);
        Button order = findViewById(R.id.orderBy);
        final Button updown1 = findViewById(R.id.upDown1);
        final Button updown2 = findViewById(R.id.upDown2);
        Button applyFilter = findViewById(R.id.apply_filter);
        Button cancelFilter= findViewById(R.id.cancel_filter_button);
        Button resetFilter = findViewById(R.id.reset_filter);
        Button filter = findViewById(R.id.filter);
        final EditText minMag = findViewById(R.id.minMag);
        final EditText maxMag = findViewById(R.id.maxMag);
        final EditText startDate = findViewById(R.id.startDate);
        final EditText endDate = findViewById(R.id.endDate);
        final EditText country = findViewById(R.id.countries);

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popUpView = inflater.inflate(R.layout.popup, null);
        Button MagBtn = (Button) popUpView.findViewById(R.id.MagBtn);
        final NumberPicker magPicker = (NumberPicker) popUpView.findViewById(R.id.MagPicker);

        final PopupWindow minMagPopupWindow = new PopupWindow(popUpView, 300, 600, true);

        final PopupWindow maxMagPopupWindow = new PopupWindow(popUpView, 300, 600, true);


        final View datePopUpView = inflater.inflate(R.layout.datepopup, null);
        Button dateBtn = datePopUpView.findViewById(R.id.dateBtn);
        final DatePicker datePicker = datePopUpView.findViewById(R.id.datePicker);

        final PopupWindow startDatePopUpWindow = new PopupWindow(datePopUpView, 450, 600, true);

        final PopupWindow endDatePopUpWindow = new PopupWindow(datePopUpView, 450, 600, true);


        final ListView countrylist = findViewById(R.id.countryListView);

        final ArrayList<Country> countryArrayList = new ArrayList<>();

        try {
            ReadCountriesFromJSON(countryArrayList);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CountryAdapter countryAdapter = new CountryAdapter(this, countryArrayList);

        countrylist.setAdapter(countryAdapter);


        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinner1.getItemAtPosition(position).toString()!="by date(default)") {
                    spinner2.setEnabled(true);
                } else {
                    spinner2.setEnabled(false);
                }

                if (spinner1.getSelectedItem()==spinner2.getSelectedItem() && spinner2.isEnabled()) {
                    spinner2.setSelection(0);
                    Toast.makeText(getApplicationContext(), "the same order  can't be chosen twice", LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, orderList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner1.setAdapter(adapter);


        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinner2.getSelectedItem()==spinner1.getSelectedItem() && spinner2.isEnabled()) {
                    spinner2.setSelection(0);
                    Toast.makeText(getApplicationContext(), "the same order  can't be chosen twice", LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner2.setEnabled(false);

        spinner2.setAdapter(adapter);


        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.orderView).setVisibility(View.VISIBLE);
            }
        });


        applyOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.orderView).setVisibility(View.INVISIBLE);
                applyOrder(myList, spinner1, spinner2);
            }
        });


        cancelOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.orderView).setVisibility(View.INVISIBLE);
            }
        });


        resetOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner1.setSelection(0);
                ascendant1 = reverseDirection(updown1, true);
                ascendant2 = reverseDirection(updown2, false);
            }
        });


        updown1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ascendant1 = reverseDirection(updown1, ascendant1);
            }
        });

        updown2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ascendant2 = reverseDirection(updown2, ascendant2);
            }
        });


        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.filterView).setVisibility(View.VISIBLE);
            }
        });

        cancelFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.filterView).setVisibility(View.INVISIBLE);
            }
        });

        resetFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minMag.setText("2");
                maxMag.setText("8");
                startDate.setText("2020-05-01");
                endDate.setText(todayDate);
                country.setText("All countries");
                minMagValue = 2;
                maxMagValue = 8;
                selectedStartDate = "2020-05-01";
                selectedEndDate = todayDate;
                selectedCountry = new Country("All countries", 0, 0, 0, 0);

            }
        });

        applyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                urlString = urlString(selectedStartDate, selectedEndDate, minMagValue, maxMagValue, selectedCountry);
                findViewById(R.id.filterView).setVisibility(View.INVISIBLE);
                task[0].cancel(true);
                task[0] = new EarthquakeAsyncTask();
                task[0].execute();

            }
        });

        minMag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                magPicker.setValue(Integer.parseInt(String.valueOf(minMag.getText())));
                minMagPopupWindow.showAtLocation(popUpView, Gravity.CENTER, 0, 0);
            }
        });

        maxMag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                magPicker.setValue(Integer.parseInt(String.valueOf(maxMag.getText())));
                maxMagPopupWindow.showAtLocation(popUpView, Gravity.CENTER, 0, 0);
            }
        });

        magPicker.setMinValue(1);
        magPicker.setMaxValue(9);
        magPicker.setWrapSelectorWheel(false);
        magPicker.setDisplayedValues(magnitudes);


        MagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (minMagPopupWindow.isShowing()) {
                    minMagPopupWindow.dismiss();
                    if (magPicker.getValue()>=maxMagValue) {
                        Toast.makeText(getApplicationContext(),"min mag should be lesser than max mag", LENGTH_SHORT).show();
                    } else {
                        minMagValue = magPicker.getValue();
                    }
                    minMag.setText(Integer.toString(minMagValue));
                } else  {
                    maxMagPopupWindow.dismiss();
                    if (magPicker.getValue()<=minMagValue) {
                        Toast.makeText(getApplicationContext(),"max mag should be bigger than min mag", LENGTH_SHORT).show();
                    } else {
                        maxMagValue = magPicker.getValue();
                    }
                    maxMag.setText(Integer.toString(maxMagValue));
                }
            }
        });

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDatePopUpWindow.showAtLocation(datePopUpView, Gravity.CENTER, 0, 0);
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endDatePopUpWindow.showAtLocation(datePopUpView, Gravity.CENTER, 0, 0);
            }
        });

        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startDatePopUpWindow.isShowing()) {
                    selectedStartDate = datePicker.getYear() + "-" + (datePicker.getMonth() + 1) + "-" + datePicker.getDayOfMonth();
                    startDatePopUpWindow.dismiss();
                    startDate.setText(selectedStartDate);
                } else {
                    selectedEndDate = datePicker.getYear() + "-" + (datePicker.getMonth() + 1) + "-" + datePicker.getDayOfMonth();
                    endDatePopUpWindow.dismiss();
                    endDate.setText(selectedEndDate);
                }
            }
        });

        country.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.countryView).setVisibility(View.VISIBLE);
            }
        });

        countrylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedCountry = countryArrayList.get(position);
                country.setText(selectedCountry.getName());
                findViewById(R.id.countryView).setVisibility(View.INVISIBLE);

            }
        });

    }


    public class EarhtqueClass {
        private double mMag;
        private String mLocation;
        private String mDetails;
        private String mDate;
        private String mTime;
        private String mUrl;

        public EarhtqueClass(double mag, String location, String details, String date, String time, String url) {
            mMag = mag;
            mLocation = location;
            mDate = date;
            mDetails = details;
            mTime = time;
            mUrl = url;
        }

        public double getMag() {
            return mMag;
        }

        public String getLocation() {
            return mLocation;
        }

        public String getDate() {
            return mDate;
        }

        public String getDetails() {
            return mDetails;
        }

        public String getTime() {
            return mTime;
        }

        public String getUrl() {
            return mUrl;
        }
    }

    public class EarthquakeAdapter extends ArrayAdapter {

        public EarthquakeAdapter(@NonNull Context context, ArrayList<EarhtqueClass> resource) {
            super(context,0, (List) resource);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            View listViewItem = convertView;
            EarhtqueClass currentEarthquake = (EarhtqueClass) getItem(position);
            if (listViewItem == null) {
                listViewItem = LayoutInflater.from(getContext()).inflate(R.layout.earthquake, parent, false);
            }


            TextView mag = listViewItem.findViewById(R.id.mag);
            mag.setText(Double.toString(currentEarthquake.getMag()));
            GradientDrawable circle = (GradientDrawable) mag.getBackground();
            circle.setColor(getResources().getColor(getMagColor(currentEarthquake.getMag())));


            TextView location = listViewItem.findViewById(R.id.location);
            location.setText(currentEarthquake.getLocation());

            TextView details = listViewItem.findViewById(R.id.details);
            details.setText(currentEarthquake.getDetails());

            TextView date = listViewItem.findViewById(R.id.date);
            date.setText(currentEarthquake.getDate());

            TextView time = listViewItem.findViewById(R.id.time);
            time.setText(currentEarthquake.getTime());

            return listViewItem;
        }
    }

    private int getMagColor(double mag) {
        switch ((int) mag) {
            case 1: return R.color.magnitude1;
            case 2: return R.color.magnitude2;
            case 3: return R.color.magnitude3;
            case 4: return R.color.magnitude4;
            case 5: return R.color.magnitude5;
            case 6: return R.color.magnitude6;
            case 7: return R.color.magnitude7;
            case 8: return R.color.magnitude8;
            case 9: return R.color.magnitude9;
            default: return R.color.magnitude10plus;
        }
    }

    private URL formUrl(String s) {
        URL url = null;
        try {
            url = new URL(s);
        } catch (MalformedURLException e) {
            Log.e(TAG, "formUrl: ",e );
        }
        return url;
    }

    public class EarthquakeAsyncTask extends AsyncTask<URL, Void, ArrayList> {

        @Override
        protected ArrayList doInBackground(URL... urls) {
            ArrayList<EarhtqueClass> arrayList = new ArrayList<>();
            try {
                readFromJSON(arrayList, MakeHTTpRequest(formUrl(urlString(selectedStartDate, selectedEndDate, minMagValue,
                                                                            maxMagValue, selectedCountry))));
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            removeNotExactLocation(arrayList, selectedCountry.getName());
            originalArrayList = arrayList;
            return arrayList;
        }

        @Override
        protected void onPostExecute(ArrayList arrayList) {
            UpdateUI(arrayList);
            super.onPostExecute(arrayList);
        }

        @Override
        protected void onPreExecute() {
            TextView description =  findViewById(R.id.description);
            description.setText("loading events...");
            super.onPreExecute();
        }
    }

    private void UpdateUI(final ArrayList<EarhtqueClass> arrayList) {
        final ListView myList = findViewById(R.id.myList);

        final EarthquakeAdapter adapter = new EarthquakeAdapter(this, arrayList);

        TextView description = findViewById(R.id.description);
        if (responsecode==200) {
            description.setText("Thre is " + adapter.getCount() + " events matching current filters");
        } else if (responsecode==400){
            description.setText(R.string.eventsLimit);
        } else {
            description.setText("some error occured, try again");
        }

        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openBrowser(arrayList.get(position).getUrl());
            }
        });

        myList.setAdapter(adapter);
        applyOrder(myList, (Spinner) findViewById(R.id.order1), (Spinner) findViewById(R.id.order2));
    }

    public InputStream MakeHTTpRequest(URL url) throws IOException {
        HttpURLConnection conx = (HttpURLConnection) url.openConnection();
        conx.setRequestMethod("GET");
        conx.connect();
        responsecode = conx.getResponseCode();
        if (conx.getResponseCode()==400) {
            return null;
        }
        int n = conx.getResponseCode();
        return conx.getInputStream();
    }

    private void readFromJSON(ArrayList<EarhtqueClass> arrayList, InputStream response) throws JSONException, IOException {
        if (response==null) {
            return;}
        arrayList.clear();
        EarhtqueClass earthquake;
        InputStreamReader redear = new InputStreamReader(response, Charset.forName("UTF-8"));
        BufferedReader buffer = new BufferedReader(redear);
        String line = buffer.readLine();
        StringBuilder builder = new StringBuilder();
        while (line != null) {
            builder.append(line);
            line = buffer.readLine();
        }
        JSONObject jsonResponse = new JSONObject(builder.toString());

        JSONArray jsonResponseArray = jsonResponse.getJSONArray("features");
        for (int i=0; i<jsonResponseArray.length(); i++) {
            JSONObject earthquakeJson = jsonResponseArray.getJSONObject(i);
            JSONObject earthquakeProperties = earthquakeJson.getJSONObject("properties");
            earthquake = new EarhtqueClass(earthquakeProperties.getDouble("mag"), FormatLocation(earthquakeProperties.getString("place")),
                                        FormatDetails(earthquakeProperties.getString("place")), FormatDate(earthquakeProperties.getString("time")),
                                FormatTime(earthquakeProperties.getString("time")), earthquakeProperties.getString("url"));
                arrayList.add(earthquake);
        }

    }

    private String FormatLocation(String place) {
        return place.substring(place.indexOf("of ")+3);
    }

    private String FormatDetails(String place) {
        return place.split("of ")[0]+"of";
    }

    private String FormatTime(String time) {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
        String Time = format.format(new Date(Long.parseLong(time)));
        return Time;
    }

    private String FormatDate(String time) {
        SimpleDateFormat format = new SimpleDateFormat("MMM dd yyyy");
        String date = format.format(new Date(Long.parseLong(time)));
        return date;
    }

    public String getCountry(String location) {
        if (location.contains(",")) {
            return location.substring(location.indexOf(", ")+2);
        }
        return location;
    }

    public void sortMagLocation(ArrayList<EarhtqueClass> arraylist, Comparator compareMag, Comparator compareCountry, boolean ascendant1, boolean ascendant2) {
        ArrayList<EarhtqueClass> mArrayList = new ArrayList<>();
        ArrayList<EarhtqueClass> equals = new ArrayList<>();
        if (ascendant1) {
            Collections.sort(arraylist, compareMag);
        } else {
            Collections.sort(arraylist, compareMag.reversed());
        }
        equals.add(arraylist.get(0));
        for (int i=1; i<arraylist.size(); i++) {
            if (arraylist.get(i).getMag()==arraylist.get(i-1).getMag()) {
                equals.add(arraylist.get(i));
            }
            else {
                if (ascendant2) {
                    Collections.sort(equals, compareCountry);
                } else {
                    Collections.sort(equals, compareCountry.reversed());
                }
                for (int j=0; j<equals.size(); j++) {
                    mArrayList.add(equals.get(j));
                }
                equals.clear();
                equals.add(arraylist.get(i));
            }
        }
        arraylist.clear();
        copyArraylist(arraylist, mArrayList);

    }

    public void sortCountryMag(ArrayList<EarhtqueClass> arraylist, Comparator compareCountry, Comparator compareMag, boolean ascendant1, boolean ascendant2) {
        ArrayList<EarhtqueClass> mArrayList = new ArrayList<>();
        ArrayList<EarhtqueClass> equals = new ArrayList<>();
        if (ascendant1) {
            Collections.sort(arraylist, compareCountry);
        } else {
            Collections.sort(arraylist, compareCountry.reversed());
        }
        equals.add(arraylist.get(0));
        for (int i=1; i<arraylist.size(); i++) {
            if (getCountry(arraylist.get(i).getLocation()).equals(getCountry(arraylist.get(i - 1).getLocation()))) {
                equals.add(arraylist.get(i));
            }
            else {
                if (ascendant2) {
                    Collections.sort(equals, compareMag);
                } else {
                    Collections.sort(equals, compareMag.reversed());
                }
                for (int j=0; j<equals.size(); j++) {
                    mArrayList.add(equals.get(j));
                }
                equals.clear();
                equals.add(arraylist.get(i));
            }
        }
        arraylist.clear();
        for (int i=0; i<mArrayList.size(); i++) {
            arraylist.add(mArrayList.get(i));
        }
    }

    public void copyArraylist(ArrayList<EarhtqueClass> arrayList1, ArrayList<EarhtqueClass> arrayList2) {
        for (int i=0; i<arrayList2.size(); i++) {
            arrayList1.add(arrayList2.get(i));
        }
    }

    public void openBrowser(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public boolean reverseDirection(Button button, boolean ascendant) {
        if (ascendant) {
            button.setBackgroundResource(R.drawable.order_down);
            return false;
        } else {
            button.setBackgroundResource(R.drawable.order_up);
            return true;
        }
    }

    public static class Country {
        private String mName;
        private long mMaxlong, mMinlong, mMaxlat, mMinlat;

        public Country(String name, long maxLong, long minLong, long maxLat, long minLat) {
            mName = name;
            mMaxlat = maxLat;
            mMinlat = minLat;
            mMaxlong = maxLong;
            mMinlong = minLong;

        }


        public long getMaxlat() {
            return mMaxlat;
        }

        public long getMaxlong() {
            return mMaxlong;
        }

        public long getMinlat() {
            return mMinlat;
        }

        public long getMinlong() {
            return mMinlong;
        }

        public String getName() {
            return mName;
        }

    }

    public void ReadCountriesFromJSON(ArrayList<Country> coutryarraylist) throws IOException, JSONException {
        coutryarraylist.clear();
        coutryarraylist.add(new Country("All countries", 0, 0, 0, 0));
        InputStream s = getAssets().open("countries.json");
        InputStreamReader reader = new InputStreamReader(s, Charset.forName("UTF-8"));
        BufferedReader buffer = new BufferedReader(reader);
        String line = buffer.readLine();
        StringBuilder sb = new StringBuilder();
        while (line!=null) {
            sb.append(line);
            line = buffer.readLine();
        }

        JSONArray jArray = new JSONArray(sb.toString());



        for (int i=0; i<jArray.length(); i++) {
            JSONObject jObj = jArray.getJSONObject(i);
            if (isCountryAvailable(jObj)) {
                coutryarraylist.add(new Country(jObj.getString("name"), jObj.getLong("maxlongitude"),
                        jObj.getLong("minlongitude"), jObj.getLong("maxlatitude"),
                        jObj.getLong("minlatitude")));
            }

        }
    }

    public class CountryAdapter extends ArrayAdapter {

        public CountryAdapter(@NonNull Context context, ArrayList<Country> resource) {
            super(context, 0,(List) resource);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listViewItem = convertView;
            Country currentCountry = (Country) getItem(position);

            if (listViewItem == null) {
                listViewItem = LayoutInflater.from(getContext()).inflate(R.layout.country, parent, false);
            }

            TextView itemCountry = listViewItem.findViewById(R.id.countryItem);
            itemCountry.setText(currentCountry.getName());

            return listViewItem;
        }
    }

    private boolean isCountryAvailable(JSONObject jObj) {
        try {
            jObj.getString("name");
            jObj.getLong("maxlongitude");
            jObj.getLong("minlongitude");
            jObj.getLong("maxlatitude");
            jObj.getLong("minlatitude");
            if (jObj.getLong("maxlongitude")+ jObj.getLong("minlongitude")+ jObj.getLong("maxlatitude")+
            jObj.getLong("minlatitude")!=0) {
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
    }
        return false;
    }

    public String urlString (String startDate, String endDate, long minMag, long maxMag, Country country) {
        String url = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson";
        url += "&starttime="+startDate+"&endtime="+endDate+"&minmagnitude="+minMag+"&maxmagnitude="+maxMag;
        if (country.getMinlat()+country.getMaxlat()+country.getMaxlong()+country.getMinlong()==0) {
            return url;
        }
        url +=  "&minlatitude="+country.getMinlat()+"&maxlatitude="+country.getMaxlat()+"&maxlongitude="+
                country.getMaxlong()+"&minlongitude="+country.getMinlong();
        return url;
    }

    public void removeNotExactLocation(ArrayList<EarhtqueClass> arraylist, String country) {
        if (country.equals("All countries")) {
            return;}
        int i=0;
        while (i<arraylist.size()) {
            if (!arraylist.get(i).getLocation().contains(country)) {
                arraylist.remove(i);
                continue;
            }
            i++;
        }
    }

    public ArrayList<EarhtqueClass> getCurrentArrayList(ListView myList) {
        EarthquakeAdapter adapter = (EarthquakeAdapter) myList.getAdapter();
        ArrayList<EarhtqueClass> arrayList = new ArrayList<>();
        for (int i=0; i<adapter.getCount(); i++) {
            arrayList.add((EarhtqueClass) adapter.getItem(i));
        }
        return arrayList;
    }

    public  void applyOrder(ListView list, Spinner spinner1, Spinner spinner2) {

        ArrayList<EarhtqueClass> arrayList = getCurrentArrayList(list);

        if (spinner1.getSelectedItem().toString()=="by magnitude" &&
                spinner2.getSelectedItem().toString()=="by Country") {
            arrayList.clear();
            copyArraylist(arrayList, originalArrayList);
            sortMagLocation(arrayList, compareMag, compareCountry, ascendant1, ascendant2);
        }
        else if (spinner1.getSelectedItem().toString()=="by Country" &&
                spinner2.getSelectedItem().toString()=="by magnitude") {
            arrayList.clear();
            copyArraylist(arrayList, originalArrayList);
            sortCountryMag(arrayList, compareCountry, compareMag, ascendant1, ascendant2);
        }
        else if (spinner1.getSelectedItem().toString()=="by magnitude") {
            arrayList.clear();
            copyArraylist(arrayList, originalArrayList);
            if (ascendant2) {
                Collections.reverse(arrayList);
            }
            if (ascendant1) {
                Collections.sort(arrayList, compareMag);
            } else {
                Collections.sort(arrayList, compareMag.reversed());
            }

        }
        else if (spinner1.getSelectedItem().toString()=="by Country") {
            arrayList.clear();
            copyArraylist(arrayList, originalArrayList);
            if (ascendant2) {
                Collections.reverse(arrayList);
            }
            if (ascendant1) {
                Collections.sort(arrayList, compareCountry);
            } else {
                Collections.sort(arrayList, compareCountry.reversed());
            }

        }
        else if (spinner1.getSelectedItem().toString()=="by date(default)") {
            arrayList.clear();
            copyArraylist(arrayList, originalArrayList);
            if (ascendant1) {
                Collections.reverse(arrayList);
            }
        }

        EarthquakeAdapter adapter = new EarthquakeAdapter(Main.this, arrayList);
        list.setAdapter(adapter);
    }

}