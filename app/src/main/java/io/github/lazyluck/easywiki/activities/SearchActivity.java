package io.github.lazyluck.easywiki.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.lazyluck.easywiki.util.HttpClient;
import io.github.lazyluck.easywiki.util.SerializableList;
import io.github.lazyluck.easywiki.R;
import io.github.lazyluck.easywiki.util.APIParams;

/***************************************
 * Main page of the application. Contains the search functions,
 * navigation to search results and favorites activity, and a link
 * to Wikipedia's mobile site.
 ***************************************/

public class SearchActivity extends AppCompatActivity {

    /***************************************
     * Variables
     ***************************************/

    private SharedPreferences sharedPref;
    private Set<String> favorite_set;
    private HttpClient httpClient;

    /***************************************
     * Layout Elements
     ***************************************/

    private EditText searchbar;
    private ImageView searchbtn;
    private TextView logo_text1;
    private TextView logo_text2;

    /***************************************
     * Initialization
     ***************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //Grab favorite list in sharedpreferences
        sharedPref = this.getSharedPreferences("favorite_pref", MODE_PRIVATE);
        favorite_set = sharedPref.getStringSet("favorite_list", new HashSet<String>());

        //Search bar & button customization. Overrides enter key to hide keypad and execute search
        final Context activitycontext = this.getApplication();
        searchbtn = (ImageView) findViewById(R.id.button_search);
        searchbar = (EditText) findViewById(R.id.searchbar);
        searchbar.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    //Call search
                    searchWiki(searchbtn);
                    //Hides keypad
                    InputMethodManager imm = (InputMethodManager)activitycontext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchbar.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        // Logo text font customization
        logo_text1 = (TextView) this.findViewById(R.id.logo_text1);
        logo_text2 = (TextView) this.findViewById(R.id.logo_text2);
        Typeface type1 = Typeface.createFromAsset(getAssets(),"fonts/font_logo1.ttf");
        Typeface type2 = Typeface.createFromAsset(getAssets(),"fonts/font_logo2.ttf");
        logo_text1.setTypeface(type1);
        logo_text2.setTypeface(type2);

        //Initialize HTTP POST parameters & client
        APIParams.initParams();
        httpClient = new HttpClient(this.getApplicationContext());
    }

    // - Useful little function to check if the client is currently connected to a network
    private boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }

    /***************************************
     * Search and Parse
     ***************************************/

    // - Grabs text in edittext and runs main search function. Listener for search button & enter key on keypad
    public void searchWiki(View v){
        if(isConnected()){
            String searchterms = searchbar.getText().toString();
            if(searchterms.equals("")) { //Prompt if empty string
                Toast.makeText(getApplicationContext(), R.string.empty_search, Toast.LENGTH_LONG).show();
            } else{
                APIParams.updateSearchTerm(searchterms); //Update search string as parameter before launching HTTP client
                new WikiTask().execute();
            }
        } else{
            Toast.makeText(getApplicationContext(), R.string.error_connection, Toast.LENGTH_LONG).show();
        }
    }

    // - Extracts titles as String list from JSONObject
    private List<String> ExtractTitle(JSONObject queryResult){
        try {
            List<String> title_list = new ArrayList<String>();
            JSONArray titlejsArray = queryResult.getJSONObject("query").getJSONArray("search"); //Specific to the format of the JSON
            if(titlejsArray.length()==0){ //Return empty list if empty results
                return new ArrayList<String>();
            } else{
                for(int i=0;i<titlejsArray.length();i++){
                    title_list.add(titlejsArray.getJSONObject(i).getString("title"));
                }
                return title_list;
            }
        } catch (JSONException e) { //Could occur if the API messes up
            Toast.makeText(getApplicationContext(), R.string.error_fetch, Toast.LENGTH_LONG).show();
            return new ArrayList<String>();
        }
    }

    /***************************************
     * Methods that launch Intents
     ***************************************/

    // - Launches ListActivity with favorites list
    private void FavJump(){
        if(favorite_set.isEmpty()){ //User has nothing saved
            Toast.makeText(getApplicationContext(), R.string.empty_favorites, Toast.LENGTH_LONG).show();
        } else{
            List<String> favorites_list = new ArrayList<String>(favorite_set);
            Intent launchIntent = new Intent(this, ListActivity.class);
            launchIntent.putExtra("page_title", "Favorites"); //Title to be set
            launchIntent.putExtra("title_list", new SerializableList(favorites_list)); //Send list as serializable
            this.startActivity(launchIntent);
        }
    }

    // - Launches ListActivity with search results
    private void ListJump(List<String> title_list){
        Intent launchIntent = new Intent(this, ListActivity.class);
        launchIntent.putExtra("page_title", "Search Result"); //Title to be set
        launchIntent.putExtra("title_list", new SerializableList(title_list));
        this.startActivity(launchIntent);
    }

    /***************************************
     * Classes
     ***************************************/

    // - AsyncTask for the Wikipedia API query to run on the background, note that input is unused
    private class WikiTask extends AsyncTask<String, String, JSONObject>{
        String URLString = "https://en.wikipedia.org/w/api.php";

        @Override
        protected JSONObject doInBackground(String... params){
            try {
                String resultString = httpClient.makeConnection(URLString);
                if(!resultString.equals("error")){ //If network IO error not encountered
                    return new JSONObject(resultString);
                }
            } catch (ProtocolException | MalformedURLException | UnsupportedEncodingException e) { //These Exceptions should not occur. Users cannot change protocol & API URL & encoding
                Toast.makeText(getApplicationContext(), R.string.error_developer, Toast.LENGTH_LONG).show();

            } catch (JSONException e) { //Could occur if the API messes up and returns the wrong format
                Toast.makeText(getApplicationContext(), R.string.error_fetch, Toast.LENGTH_LONG).show();
            }
            return new JSONObject();
        }

        @Override
        protected void onPostExecute(JSONObject result){
            super.onPostExecute(result);
            List<String> title_list = ExtractTitle(result);
            if(title_list.isEmpty()){ //Empty search results prompt
                Toast.makeText(getApplicationContext(), R.string.empty_result, Toast.LENGTH_LONG).show();
            }else{
                ListJump(title_list);
            }
        }
    }

    /***************************************
     * Life Cycle
     ***************************************/

    @Override
    protected void onResume() {
        super.onResume();
        //Update sharedpreferences on activity start and resume
        sharedPref = this.getSharedPreferences("favorite_pref", MODE_PRIVATE);
        favorite_set = sharedPref.getStringSet("favorite_list", new HashSet<String>());
    }

    /***************************************
     * Action Bar
     ***************************************/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate action bar and its buttons
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favorites: //Favorites button
                FavJump();
                break;
            case R.id.action_wikipedia: //Wikipedia link button
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://en.m.wikipedia.org/wiki/Main_Page"));
                startActivity(browserIntent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }



}