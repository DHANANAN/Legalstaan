package com.legalstaan.app;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * PDF viewer with bookmarking.
 *  - For "drive_id" → opens Google Drive /preview in WebView
 *  - For "pdf_url"  → downloads to cache and renders via PdfRenderer (page-by-page)
 *  - For "pdf_asset" → reads from assets/ and renders via PdfRenderer
 *  Bookmark stores last-read page in SharedPreferences keyed by source.
 */
public class PdfNotesActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE     = "title";
    public static final String EXTRA_DRIVE_ID  = "drive_id";
    public static final String EXTRA_PDF_URL   = "pdf_url";
    public static final String EXTRA_PDF_ASSET = "pdf_asset";

    private static final String PREFS = "pdf_bookmarks";

    private WebView webView;
    private ImageView pageView;
    private ProgressBar progress;
    private TextView pageIndicator;
    private View pagerControls;

    private PdfRenderer renderer;
    private ParcelFileDescriptor pfd;
    private int pageCount = 0;
    private int currentPage = 0;
    private String bookmarkKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_notes);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getIntent().getStringExtra(EXTRA_TITLE) != null
                    ? getIntent().getStringExtra(EXTRA_TITLE) : "Notes");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        webView       = findViewById(R.id.webview);
        pageView      = findViewById(R.id.iv_page);
        progress      = findViewById(R.id.progress);
        pageIndicator = findViewById(R.id.tv_page_indicator);
        pagerControls = findViewById(R.id.pager_controls);

        findViewById(R.id.btn_prev_page).setOnClickListener(v -> showPage(currentPage - 1));
        findViewById(R.id.btn_next_page).setOnClickListener(v -> showPage(currentPage + 1));
        findViewById(R.id.btn_bookmark).setOnClickListener(v -> {
            saveBookmark();
            Toast.makeText(this, "Bookmarked page " + (currentPage + 1), Toast.LENGTH_SHORT).show();
        });

        String driveId = getIntent().getStringExtra(EXTRA_DRIVE_ID);
        String pdfUrl  = getIntent().getStringExtra(EXTRA_PDF_URL);
        String asset   = getIntent().getStringExtra(EXTRA_PDF_ASSET);

        if (driveId != null) {
            bookmarkKey = "drive:" + driveId;
            openInWebView("https://drive.google.com/file/d/" + driveId + "/preview");
        } else if (pdfUrl != null) {
            bookmarkKey = "url:" + pdfUrl;
            new DownloadAndRenderTask().execute(pdfUrl);
        } else if (asset != null) {
            bookmarkKey = "asset:" + asset;
            try {
                File cached = copyAssetToCache(asset);
                openLocalPdf(cached);
            } catch (IOException e) {
                Toast.makeText(this, "Couldn't open asset: " + e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            Toast.makeText(this, "No PDF source provided", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void openInWebView(String url) {
        webView.setVisibility(View.VISIBLE);
        pageView.setVisibility(View.GONE);
        pagerControls.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView v, String u) { progress.setVisibility(View.GONE); }
        });
        webView.loadUrl(url);
    }

    private void openLocalPdf(File f) {
        try {
            pfd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
            renderer = new PdfRenderer(pfd);
            pageCount = renderer.getPageCount();
            pageView.setVisibility(View.VISIBLE);
            pagerControls.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);
            currentPage = loadBookmark();
            if (currentPage >= pageCount) currentPage = 0;
            showPage(currentPage);
        } catch (IOException e) {
            Toast.makeText(this, "Couldn't render PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void showPage(int index) {
        if (renderer == null || index < 0 || index >= pageCount) return;
        try (PdfRenderer.Page page = renderer.openPage(index)) {
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = (int) (width * ((float) page.getHeight() / page.getWidth()));
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bmp.eraseColor(Color.WHITE);
            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            pageView.setImageBitmap(bmp);
            currentPage = index;
            pageIndicator.setText("Page " + (index + 1) + " of " + pageCount);
        }
    }

    private File copyAssetToCache(String asset) throws IOException {
        File out = new File(getCacheDir(), "asset_" + asset.hashCode() + ".pdf");
        if (out.exists() && out.length() > 0) return out;
        try (InputStream in = getAssets().open(asset);
             FileOutputStream fos = new FileOutputStream(out)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) fos.write(buf, 0, n);
        }
        return out;
    }

    private int loadBookmark() {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        return sp.getInt(bookmarkKey, 0);
    }

    private void saveBookmark() {
        if (bookmarkKey == null) return;
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putInt(bookmarkKey, currentPage).apply();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (renderer != null) saveBookmark();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (renderer != null) renderer.close();
            if (pfd != null) pfd.close();
        } catch (IOException ignore) {}
    }

    private class DownloadAndRenderTask extends AsyncTask<String, Void, File> {
        private String error;
        @Override protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
            pageView.setVisibility(View.GONE);
            webView.setVisibility(View.GONE);
        }
        @Override protected File doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                File out = new File(getCacheDir(), "remote_" + params[0].hashCode() + ".pdf");
                if (out.exists() && out.length() > 0) return out;
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);
                conn.connect();
                try (InputStream in = conn.getInputStream();
                     FileOutputStream fos = new FileOutputStream(out)) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) > 0) fos.write(buf, 0, n);
                }
                return out;
            } catch (Exception e) {
                error = e.getMessage();
                return null;
            }
        }
        @Override protected void onPostExecute(File f) {
            progress.setVisibility(View.GONE);
            if (f == null) {
                Toast.makeText(PdfNotesActivity.this, "Download failed: " + error, Toast.LENGTH_LONG).show();
                finish();
            } else {
                openLocalPdf(f);
            }
        }
    }
}
