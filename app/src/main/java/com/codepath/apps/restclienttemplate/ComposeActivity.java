package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;

public class ComposeActivity extends AppCompatActivity {

    private EditText composedTweet;
    private TextView characterCount;
    private Button tweetButton;
    public static final int MAX_TWEET_LENGTH = 280;
    public static int lengthOfTweet;
    private TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        client = TwitterApp.getRestClient(this);
        composedTweet = findViewById(R.id.editText_compose);
        tweetButton = findViewById(R.id.tweet_button);
        characterCount = findViewById(R.id.character_count);

        lengthOfTweet = composedTweet.length();
        characterCount.setText("" + (MAX_TWEET_LENGTH - lengthOfTweet));

        composedTweet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                lengthOfTweet = composedTweet.length();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (lengthOfTweet == MAX_TWEET_LENGTH)
                    Toast.makeText(ComposeActivity.this, "Reached maximum character length", Toast.LENGTH_LONG).show();

                characterCount.setText("" + (MAX_TWEET_LENGTH - lengthOfTweet));

            }
        });
        tweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String sentTweet = composedTweet.getText().toString();
                //TODO: Better Error-handling
                if (sentTweet.isEmpty()) {
                    Toast.makeText(ComposeActivity.this, "Your tweet is empty!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (sentTweet.length() > MAX_TWEET_LENGTH) {
                    Toast.makeText(ComposeActivity.this, "Your tweet exceeds 280 characters", Toast.LENGTH_LONG).show();
                    return;
                }

                Toast.makeText(ComposeActivity.this, sentTweet, Toast.LENGTH_LONG).show();
                //API call to Twitter in order to publish tweet
                client.composeTweet(sentTweet, new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.d("TwitterClient", "You posted a tweet! " + response.toString());
                        try {
                            Tweet tweet = Tweet.fromJson(response);
                            Intent data = new Intent();
                            data.putExtra("tweet", Parcels.wrap(tweet));

                            setResult(RESULT_OK, data);
                            finish();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Log.e("TwitterClient", "Failed to post tweet " + responseString);
                    }
                });

            }
        });


    }
}
