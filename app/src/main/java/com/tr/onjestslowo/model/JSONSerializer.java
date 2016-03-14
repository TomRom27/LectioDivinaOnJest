package com.tr.onjestslowo.model;

import com.google.gson.Gson;

/**
 * Created by bpl2111 on 2014-05-26.
 */
public class JSONSerializer {

    public static ReadingListResult deserializePostListResult(String jsonString) throws org.json.JSONException {

        Gson gson = new Gson();

        ReadingListResult readingListResult;
        readingListResult = gson.fromJson(jsonString, ReadingListResult.class);

        return readingListResult;
    }
}
    //<editor-fold desc="'manual' deserialization">
    /*
    public static PostListResult DeserializePostListResult(String jsonString) throws org.json.JSONException {

        try {
            JSONObject jsonPostsResult = new JSONObject(jsonString);

            PostListResult postListResult = new PostListResult();

            postListResult.count = jsonPostsResult.getInt("count");
            postListResult.Count_total = jsonPostsResult.getInt("count_total");
            postListResult.Pages = jsonPostsResult.getInt("pages");
            postListResult.status = jsonPostsResult.getString("status");


            org.json.JSONArray jsonPosts = jsonPostsResult.getJSONArray("posts");

            for (int i = 0; i < jsonPosts.length(); i++) {
                JSONObject jsonPost = jsonPosts.getJSONObject(i);
                Post post = DeserializePost(jsonPost);
                postListResult.posts.add(post);
            }

            return postListResult;

        } catch (org.json.JSONException ex) {
            // just re-throw an exception
            throw ex;
        }
    }

    private static Post DeserializePost(JSONObject jsonPost) throws org.json.JSONException {
        Post post = new Post();

        try {
            post.content = jsonPost.getString("content");
            post.date = jsonPost.getString("date"); //todo
            post.title = jsonPost.getString("title");

        /  *
        private List mAttachments;
        private author mAuthor;
        private List mCategories;
        private int mComment_count;
        private String mComment_status;
        private List mComments;
        private Custom_fields mCustom_fields;
        private String mDate;
        private String mExcerpt;
        private int mId;
        private String mModified;
        private String mSlug;
        private String mStatus;
        private List mTags;
        private String title;
        private String mTitle_plain;
        private String mType;
        private String mUrl;
              *  /

            return post;
        } catch (org.json.JSONException ex) {
            // just re-throw an exception
            throw ex;
        }
     */
    //</editor-fold>