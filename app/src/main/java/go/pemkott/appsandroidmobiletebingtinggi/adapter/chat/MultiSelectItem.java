package go.pemkott.appsandroidmobiletebingtinggi.adapter.chat;

public class MultiSelectItem {
    private long id;
    private String title;
    private String subtitle;
    private String avatarUrl;
    private boolean isChecked;

    public MultiSelectItem(long id, String title, String subtitle, String avatarUrl, boolean isChecked) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.avatarUrl = avatarUrl;
        this.isChecked = isChecked;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public String getSubtitle() {
        return subtitle != null ? subtitle : "";
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
