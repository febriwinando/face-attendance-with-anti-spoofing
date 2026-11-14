package go.pemkott.appsandroidmobiletebingtinggi.model.chat;

import com.google.gson.annotations.SerializedName;

public class ChatPagination {
    @SerializedName("current_page")
    private int currentPage = 1;

    @SerializedName("per_page")
    private int perPage = 25;

    @SerializedName("total")
    private int total = 0;

    @SerializedName("last_page")
    private int lastPage = 1;

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPerPage() {
        return perPage;
    }

    public int getTotal() {
        return total;
    }

    public int getLastPage() {
        return Math.max(1, lastPage);
    }
}

