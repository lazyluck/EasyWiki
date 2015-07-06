package io.github.lazyluck.easywiki.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.lazyluck.easywiki.util.SerializableList;
import io.github.lazyluck.easywiki.R;

/***************************************
 * Activity that displays search results or
 * the saved favorite titles and display them
 * in a listview.
 ***************************************/

public class ListActivity extends AppCompatActivity {

    /***************************************
     * Variables
     ***************************************/
    private Set<String> favorite_set;
    private SharedPreferences sharedPref;

    /***************************************
     * Layout Elements
     ***************************************/

    private ListView title_listview;

    /***************************************
     * Initialization
     ***************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        //Retrieve sharedpreferences to be saved
        sharedPref = this.getSharedPreferences("favorite_pref", MODE_PRIVATE);
        favorite_set = sharedPref.getStringSet("favorite_list", new HashSet<String>());

        //Sets the title of the actionbar
        setTitle(this.getIntent().getStringExtra("page_title"));

        //Fetch title String list and initialize adapter
        SerializableList title_serializable = (SerializableList) this.getIntent().getSerializableExtra("title_list");
        List<String> title_list = (List<String>) title_serializable.getList();
        ListAdapter titleAdapter = new ListAdapter(this, title_list);
        title_listview = (ListView) this.findViewById(R.id.title_listview);
        title_listview.setAdapter(titleAdapter);
    }

    /***************************************
     * Methods that launch Intents
     ***************************************/

    // - Launches external broswer with link
    private void WebJump(String link){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(browserIntent);
    }

    /***************************************
     * Adapter
     ***************************************/

    private class ListAdapter extends BaseAdapter {
        private Context context;
        private List<String> list;

        public ListAdapter(Context context, List<String> list) {
            this.context = context;
            this.list = list;
        }

        public void setlist(List<String> list){
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int arg0) {
            return list.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup arg2) {
            final Holder holder;
            if(view==null){
                holder = new Holder();
                view = LayoutInflater.from(context).inflate(R.layout.title_listview, null);

                //Alternates background color by position
                if(position%2==1){
                    view.setBackgroundColor(getResources().getColor(R.color.bggrey));
                } else{
                    view.setBackgroundColor(getResources().getColor(R.color.bgwhite));
                }

                holder.text_title = (TextView) view.findViewById(R.id.text_title);
                holder.icon_favorite = (ImageView) view.findViewById(R.id.icon_favorite);
                view.setTag(holder);
            } else{
                holder = (Holder) view.getTag();
            }

            //Update icon if saved in favorites
            if(favorite_set.contains(list.get(position))){
                holder.icon_favorite.setImageResource(R.drawable.favorite_enabled);
            } else{
                holder.icon_favorite.setImageResource(R.drawable.favorite_disabled);
            }

            //Sets the text to [Number. Title]
            holder.text_title.setText((position + 1) + ". " + list.get(position));

            //Launch Wikipedia page on click
            holder.text_title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    WebJump("https://en.m.wikipedia.org/wiki/" + list.get(position));
                }
            });

            //Toggles favorites icon and saved state
            holder.icon_favorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (favorite_set.contains(list.get(position))) {
                        holder.icon_favorite.setImageResource(R.drawable.favorite_disabled);
                        favorite_set.remove(list.get(position));
                    } else {
                        holder.icon_favorite.setImageResource(R.drawable.favorite_enabled);
                        favorite_set.add(list.get(position));
                    }
                }
            });

            return view;
        }

        // - Elements class
        class Holder{
            private TextView text_title;
            private ImageView icon_favorite;
        }
    }

    /***************************************
     * Life Cycle
     ***************************************/

    @Override
    protected void onPause() {
        super.onPause();
        //Save sharedpreferences when focus is lost
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putStringSet("favorite_list", favorite_set);
        prefEditor.commit();
    }

    /***************************************
     * Action Bar
     ***************************************/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate action bar and its buttons
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_return: // Return button
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}