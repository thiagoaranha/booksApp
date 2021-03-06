package com.thiagoaranha.booksapp;

import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.thiagoaranha.booksapp.model.Book;
import com.thiagoaranha.booksapp.model.ImageLinks;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Thiago on 09/08/17.
 */

public class BookActivity extends AppCompatActivity {

    FragmentManager fm;

    ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        fm = getSupportFragmentManager();
        progressBar = (ProgressBar) findViewById(R.id.progress);

        //Pegando os dados da notificação para exibir o livro
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            if(bundle.containsKey("bookId")){
                String bookId = (String)bundle.get("bookId");

                findBookById(bookId);
            }
        }

    }

    public void findBookById(String bookId) {

        if(bookId.length()>0)
        {
            bookId = bookId.replace(" ", "+");
            String url = "https://www.googleapis.com/books/v1/volumes/";
            url = url + bookId;

            AsyncHttpClient client = new AsyncHttpClient();
            client.get(url, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                    Book book = new Book(Parcel.obtain());

                    String json = new String(responseBody);

                    try {
                        JSONObject object = new JSONObject(json);

                        String id = object.getString("id");
                        book.setId(id);

                        JSONObject volumeInfo = object.getJSONObject("volumeInfo");

                        String title = volumeInfo.getString("title");
                        book.getVolumeInfo().setTitle(title);

                        JSONArray authorsJson = volumeInfo.getJSONArray("authors");
                        String[] authors = new String[authorsJson.length()];
                        for(int i = 0; i < authorsJson.length(); i++){
                            authors[i] = authorsJson.getString(i);
                        }
                        book.getVolumeInfo().setAuthors(authors);

                        JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");
                        String smallThumbnail = imageLinks.getString("smallThumbnail");
                        ImageLinks imgLinks = new ImageLinks(smallThumbnail);
                        book.getVolumeInfo().setImageLinks(imgLinks);

                        String description = volumeInfo.getString("description");
                        book.getVolumeInfo().setDescription(description);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    BookFragment bookFragment = new BookFragment();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("book", book);
                    bookFragment.setArguments(bundle);

                    android.support.v4.app.Fragment fragmentLoaded = (android.support.v4.app.Fragment) fm.findFragmentById(R.id.main_container);
                    fm = getSupportFragmentManager();

                    if(fragmentLoaded != null) {
                        fm.beginTransaction().replace(R.id.main_container, bookFragment).commit();
                    }else{
                        fm.beginTransaction().add(R.id.main_container, bookFragment).commit();
                    }


                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onStart() {
                    super.onStart();
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFinish() {
                    progressBar.setVisibility(View.GONE);
                    super.onFinish();
                }

                @Override
                public void onCancel() {
                    progressBar.setVisibility(View.GONE);
                    super.onCancel();
                }

                @Override
                public void onProgress(long bytesWritten, long totalSize) {
                    super.onProgress(bytesWritten, totalSize);
                }

            });
        }

    }

}
