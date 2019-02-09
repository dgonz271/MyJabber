package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder>{

    private Context context;
    private List<Tweet> tweets;


    //Pass in context and list of tweets
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    //for each row, inflate layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, viewGroup, false);
        return new ViewHolder(view);
    }
    //bind values based on position of element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
    Tweet tweet = tweets.get(i);
    viewHolder.tvBodyTweet.setText(tweet.body);
    viewHolder.tvHandle.setText( tweet.user.name + "  @" + tweet.user.handleName);
    viewHolder.timeCreated.setText("Tweeted "+ getRelativeTimeAgo(tweet.createdAt));

    Glide.with(context).load(tweet.user.profileImageUrl).into(viewHolder.ivProfilePic);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<Tweet> tweetList) {
        tweets.addAll(tweetList);
        notifyDataSetChanged();
    }
    //Define Viewholder
    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView ivProfilePic;
        public TextView tvHandle;
        public TextView tvBodyTweet;
        public TextView timeCreated;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivProfilePic = itemView.findViewById(R.id.iv_profile_pic);
            tvHandle = itemView.findViewById(R.id.tv_handle);
            tvBodyTweet = itemView.findViewById(R.id.tv_body_tweet);
            timeCreated = itemView.findViewById(R.id.time_created);
        }
    }

    //Method copied from https://gist.github.com/nesquena/f786232f5ef72f6e10a7
    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }


}
