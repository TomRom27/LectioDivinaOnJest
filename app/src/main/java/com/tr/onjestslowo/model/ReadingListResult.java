
package com.tr.onjestslowo.model;

import java.util.ArrayList;
import java.util.List;

// http://jsongen.byingtondesign.com/
public class ReadingListResult {

    public ReadingListResult() {
        posts = new ArrayList<Post>();
    }
   	public int count;
   	public int count_total;
   	public int pages;
   	public List<Post> posts;
   	public String status;
}
