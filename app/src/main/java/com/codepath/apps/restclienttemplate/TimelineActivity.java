package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Build;
import android.os.Parcel;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.Toolbar;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity {

    private TwitterClient client;
    private RecyclerView recViewTweets;
    private TweetsAdapter tweetsAdapter;
    private List<Tweet> tweets;

    LinearLayoutManager linearLayoutManager;

    private SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;

    private final int REQUEST_CODE = 100;
    private long lowestID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);



        client = TwitterApp.getRestClient(this);
        swipeContainer = findViewById(R.id.swipeContainer);


        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        //Find recycler view
        recViewTweets = findViewById(R.id.recview_tweets);
        //Initialize list of tweets and adapter from data source
        tweets = new ArrayList<>();
        tweetsAdapter = new TweetsAdapter(this, tweets);
        //Setup recycler view: layout manager and setting adapter
        linearLayoutManager = new LinearLayoutManager(this);
        recViewTweets.setLayoutManager(linearLayoutManager);
        recViewTweets.setAdapter(tweetsAdapter);


        // Retain an instance so that you can call resetState() for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                loadNextDataFromApi(page);

                Log.d("Debugging", "Was more Loaded?");
            }
        };
        recViewTweets.addOnScrollListener(scrollListener);
        populateHomeTimeline();


        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("TwitterClient", "Refreshed Content");
                populateHomeTimeline();
                Toast.makeText(TimelineActivity.this, "Timeline Refreshed", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.compose)
        {
            Toast.makeText(this, "Compose button clicked", Toast.LENGTH_SHORT).show();
            Intent i = new Intent( this, ComposeActivity.class);
            startActivityForResult(i, REQUEST_CODE);

        }
        if(item.getItemId() == R.id.logout)
            Toast.makeText(this, "Logout button clicked", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK)
        {
            //obtaining data from composeActivity (Tweet)
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            //Update RecyclerView with tweet
            tweets.add(0, tweet);
            tweetsAdapter.notifyItemInserted(0);
            recViewTweets.smoothScrollToPosition(0);
        }
    }


    private void populateHomeTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                //Log.d("TwitterClient", response.toString());
                //Iterate through list of tweets coming from JsonArray response
                List<Tweet> tweetsToAdd = new ArrayList<>();

                for (int i = 0; i < response.length(); i++) {
                    try {
                        //Convert each jsonobject into tweet object
                        JSONObject jsonTweetObject = response.getJSONObject(i);
                        Tweet tweet = Tweet.fromJson(jsonTweetObject);
                        //Add tweet into our data source
                        tweetsToAdd.add(tweet);

                        Log.d("TweetID", "This tweet's id is: " + tweetsToAdd.get(i).uid);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                //Clearing existing tweets
                tweetsAdapter.clear();
                //Showing new data (tweets)
                tweetsAdapter.addAll(tweetsToAdd);

                lowestID = tweetsToAdd.get(tweetsToAdd.size() - 1).uid;
                //Calling setRefreshing method to indicate that refresh is done
                swipeContainer.setRefreshing(false);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e("TwitterClient", responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e("TwitterClient", errorResponse.toString());
            }
        });
    }

    // Append the next page of data into the adapter
    // This method probably sends out a network request and appends new data items to your adapter.
    public void loadNextDataFromApi(int offset) {
        // Send an API request to retrieve appropriate paginated data
        //  --> Send the request including an offset value (i.e `page`) as a query parameter.
        //  --> Deserialize and construct new model objects from the API response
        //  --> Append the new data objects to the existing set of items inside the array of items
        //  --> Notify the adapter of the new items made with `notifyItemRangeInserted()`

        Log.d("DG", "Loading more data");
        client.getMoreTweets(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("TwitterClient", response.toString());
                //Iterate through list of tweets coming from JsonArray response
                List<Tweet> tweetsToAdd = new ArrayList<>();

                for (int i = 0; i < response.length(); i++) {
                    try {
                        //Convert each jsonobject into tweet object
                        JSONObject jsonTweetObject = response.getJSONObject(i);
                        Tweet tweet = Tweet.fromJson(jsonTweetObject);
                        //Add tweet into our data source
                        tweetsToAdd.add(tweet);
                        Log.d("TweetID", "This tweet's id is: " + tweetsToAdd.get(i).uid);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


                //Clearing existing tweets
                //tweetsAdapter.clear();
                //Showing new data (tweets)
                tweetsAdapter.addAll(tweetsToAdd);
                //Calling setRefreshing method to indicate that refresh is done
                swipeContainer.setRefreshing(false);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e("TwitterClient", responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e("TwitterClient", errorResponse.toString());
            }
        }, lowestID - 1);

        Log.d("DG", "LowestID - 1: " + (lowestID - 1));
    }


}


