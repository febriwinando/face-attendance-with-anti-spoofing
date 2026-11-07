package go.pemkott.appsandroidmobiletebingtinggi.model.berita;

import com.google.gson.annotations.SerializedName;

public class BeritaItem {
    @SerializedName("id")
    private long id;

    @SerializedName("title")
    private String title;

    @SerializedName("slug")
    private String slug;

    @SerializedName("summary")
    private String summary;

    @SerializedName("excerpt")
    private String excerpt;

    @SerializedName("thumbnail")
    private String thumbnail;

    @SerializedName("thumbnail_url")
    private String thumbnailUrl;

    @SerializedName("category")
    private String category;

    @SerializedName("category_slug")
    private String categorySlug;

    @SerializedName("author")
    private String author;

    @SerializedName("views")
    private int views;

    @SerializedName("published_at")
    private String publishedAt;

    @SerializedName("date_formatted")
    private String dateFormatted;

    @SerializedName("web_url")
    private String webUrl;

    @SerializedName("content")
    private String content;

    public static String stripHtml(String html) {
        if (html == null || html.trim().isEmpty()) {
            return "";
        }
        String clean;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            clean = android.text.Html.fromHtml(html, android.text.Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            clean = android.text.Html.fromHtml(html).toString();
        }
        clean = clean.replace('\u00A0', ' ')
                     .replace("&nbsp;", " ")
                     .replaceAll("<[^>]*>", "")
                     .replaceAll("(?m)^[ \t]*\r?\n", "")
                     .trim();
        return clean;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return stripHtml(title);
    }

    public String getSlug() {
        return slug;
    }

    public String getSummary() {
        String raw = summary != null && !summary.isEmpty() ? summary : excerpt;
        return stripHtml(raw);
    }

    public String getExcerpt() {
        String raw = excerpt != null && !excerpt.isEmpty() ? excerpt : summary;
        return stripHtml(raw);
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getThumbnailUrl() {
        String urlToUse = thumbnailUrl;
        if (urlToUse == null || urlToUse.trim().isEmpty()) {
            urlToUse = thumbnail;
        }

        if (urlToUse == null || urlToUse.trim().isEmpty()) {
            return null;
        }

        urlToUse = urlToUse.trim();

        // Convert local IP addresses / HTTP to production HTTPS domain
        if (urlToUse.contains("172.15.1.13") || urlToUse.contains("localhost") || urlToUse.contains("10.0.2.2")) {
            urlToUse = urlToUse.replace("http://172.15.1.13:4321", "https://diskominfo.tebingtinggikota.go.id")
                              .replace("http://172.15.1.13", "https://diskominfo.tebingtinggikota.go.id")
                              .replace("http://localhost:4321", "https://diskominfo.tebingtinggikota.go.id")
                              .replace("http://10.0.2.2:4321", "https://diskominfo.tebingtinggikota.go.id");
        }

        if (urlToUse.startsWith("http://diskominfo.tebingtinggikota.go.id")) {
            urlToUse = urlToUse.replace("http://diskominfo.tebingtinggikota.go.id", "https://diskominfo.tebingtinggikota.go.id");
        }

        if (urlToUse.startsWith("http://") || urlToUse.startsWith("https://")) {
            return urlToUse;
        }

        if (urlToUse.startsWith("/")) {
            return "https://diskominfo.tebingtinggikota.go.id" + urlToUse;
        }

        return "https://diskominfo.tebingtinggikota.go.id/" + urlToUse;
    }

    public String getCategory() {
        return category != null ? category : "Diskominfo";
    }

    public String getCategorySlug() {
        return categorySlug;
    }

    public String getAuthor() {
        return author != null ? author : "Redaksi Diskominfo";
    }

    public int getViews() {
        return views;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public String getDateFormatted() {
        return dateFormatted != null ? dateFormatted : "";
    }

    public String getWebUrl() {
        if (webUrl != null && !webUrl.isEmpty()) {
            return webUrl;
        }
        return "https://diskominfo.tebingtinggikota.go.id/berita/" + (slug != null ? slug : "");
    }

    public String getContent() {
        return content != null ? content : "";
    }
}
