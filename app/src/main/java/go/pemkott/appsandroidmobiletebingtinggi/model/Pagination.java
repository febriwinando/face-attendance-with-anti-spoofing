package go.pemkott.appsandroidmobiletebingtinggi.model;

import com.google.gson.annotations.SerializedName;

public class Pagination {
    @SerializedName("current_page")
    private int currentPage;

    @SerializedName("last_page")
    private int lastPage;

    @SerializedName("per_page")
    private int perPage;

    @SerializedName("total")
    private int total;

    public int getCurrentPage() { return currentPage; }
    public int getLastPage() { return lastPage; }
    public int getPerPage() { return perPage; }
    public int getTotal() { return total; }
}
