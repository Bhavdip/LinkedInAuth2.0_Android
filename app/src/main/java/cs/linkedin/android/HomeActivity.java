package cs.linkedin.android;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity {

  private static final String PROFILE_URL = "https://api.linkedin.com/v1/people/~";
  private static final String OAUTH_ACCESS_TOKEN_PARAM ="oauth2_access_token";
  private static final String QUESTION_MARK = "?";
  private static final String EQUALS = "=";

  private TextView welcomeText;
  private ProgressDialog pd;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);
    welcomeText = findViewById(R.id.activity_profile_welcome_text);

    //Request basic profile of the user
    SharedPreferences preferences = this.getSharedPreferences("user_info", 0);
    String accessToken = preferences.getString("accessToken", null);
    if(accessToken!=null){
      String profileUrl = getProfileUrl(accessToken);
      new GetProfileRequestAsyncTask().execute(profileUrl);
    }
  }

  private static final String getProfileUrl(String accessToken){
    return PROFILE_URL
        +QUESTION_MARK
        +OAUTH_ACCESS_TOKEN_PARAM+EQUALS+accessToken;
  }

  @SuppressLint("StaticFieldLeak")
  private class GetProfileRequestAsyncTask extends AsyncTask<String, Void, JSONObject> {

    @Override
    protected void onPreExecute(){
      pd = ProgressDialog.show(HomeActivity.this, "", "Loading..",true);
    }

    @Override
    protected JSONObject doInBackground(String... urls) {
      if(urls.length>0){
        String url = urls[0];
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).addHeader("x-li-format", "json").build();
        try{
          //HttpResponse response = httpClient.execute(httpget);
          Response response = okHttpClient.newCall(request).execute();
          if(response!=null){
            //If status is OK 200
            if(response.isSuccessful()){
              return new JSONObject(response.body().string());
            }
          }
        }catch(IOException e){
          Log.e("Authorize","Error Http response "+e.getLocalizedMessage());
        } catch (JSONException e) {
          Log.e("Authorize","Error Http response "+e.getLocalizedMessage());
        }
      }
      return null;
    }

    @Override
    protected void onPostExecute(JSONObject data){
      if(pd!=null && pd.isShowing()){
        pd.dismiss();
      }
      if(data!=null){

        try {
          String welcomeTextString = String.format("Welcome %1$s %2$s, You are a %3$s",data.getString("firstName"),data.getString("lastName"),data.getString("headline"));
          welcomeText.setText(welcomeTextString);
        } catch (JSONException e) {
          Log.e("Authorize","Error Parsing json "+e.getLocalizedMessage());
        }
      }
    }


  };
}
