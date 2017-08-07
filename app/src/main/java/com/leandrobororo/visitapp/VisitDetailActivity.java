package com.leandrobororo.visitapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.leandrobororo.visitapp.criptografia.DeCryptor;
import com.leandrobororo.visitapp.dto.WeatherReturn;
import com.leandrobororo.visitapp.dto.MatchingVisitsReturn;
import com.leandrobororo.visitapp.model.Forecast;
import com.leandrobororo.visitapp.model.Friend;
import com.leandrobororo.visitapp.model.Visit;
import com.leandrobororo.visitapp.services.APIBackendService;
import com.leandrobororo.visitapp.services.APIWeatherService;
import com.leandrobororo.visitapp.util.Util;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.view.View.GONE;
import static com.leandrobororo.visitapp.util.Util.extractDayMonthYearDateString;
import static com.leandrobororo.visitapp.util.Util.getDayNightChar;
import static com.leandrobororo.visitapp.util.Util.addOneHourAndHalf;
import static com.leandrobororo.visitapp.util.Util.subtractOneHourAndHalf;
import static java.lang.Character.toUpperCase;
import static java.lang.String.format;

public class VisitDetailActivity extends AppCompatActivity {

    private final VisitDetailActivity context = this;

    private APIBackendService backendService;
    private APIWeatherService weatherService;

    private CoordinatorLayout coordinatorLayout;
    private ProgressBar progress;
    private Set<Friend> sameDayFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progress = (ProgressBar) findViewById(R.id.progressDetalhe);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayoutDetalhe);

        Bundle extras = getIntent().getExtras();
        final Visit visit = (Visit) extras.get(getString(R.string.extra_visita));

        getWeatherServiceReference();
        getVisitsServiceReference();
        setStats(visit);
        setButtons(visit);
        enableDisableFriendsDataComponents(visit);
        obtainForecastDataAndConfigComponents(visit);
    }

    @NonNull
    private void setButtons(final Visit visit) {
        Button buttonEstacionamentos = (Button) findViewById(R.id.buttonEstacionamento);
        buttonEstacionamentos.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("geo:" + visit.getLatitude() + "," + visit.getLongitude() + "?q=estacionamentos");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        Button buttonAmigos = (Button) findViewById(R.id.buttonAmigos);
        buttonAmigos.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                ArrayList<Friend> friends = new ArrayList<Friend>();

                if (sameDayFriends != null) {
                    friends.addAll(sameDayFriends);
                }

                Intent it = new Intent(context, FriendsActivity.class);
                it.putExtra(getString(R.string.extra_amigos), friends);
                it.putExtra(getString(R.string.extra_visita), visit);
                startActivity(it);
            }
        });
    }

    private void setStats(Visit visit) {
        ImageView imagePlace = (ImageView) findViewById(R.id.imgPlaceDetail);
        imagePlace.setImageBitmap(readBitmap(visit.getPlaceId()));

        TextView txtNomePlace = (TextView) findViewById(R.id.txtPlaceDescriptionDetail);
        txtNomePlace.setText(visit.getPlaceName());

        TextView txtEnderecoPlace = (TextView) findViewById(R.id.txtPlaceAddressDetail);
        txtEnderecoPlace.setText(visit.getPlaceAddress());

        TextView txtHorarioVisita = (TextView) findViewById(R.id.txtVisitDescriptionDetail);
        txtHorarioVisita.setText(visit.getDescricaoVisita());
    }

    private void getVisitsServiceReference() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(APIBackendService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        backendService = retrofit.create(APIBackendService.class);
    }

    private void getWeatherServiceReference() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(APIWeatherService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        weatherService = retrofit.create(APIWeatherService.class);
    }

    private void enableDisableFriendsDataComponents(Visit visit) {
        if (!visit.isFollowUpFriends()) {
            findViewById(R.id.buttonAmigos).setVisibility(GONE);
            findViewById(R.id.txtSameDayFriendsNumber).setVisibility(GONE);
            findViewById(R.id.txtSameMomentFriendsNumber).setVisibility(GONE);
        }
    }

    private void obtainFriendsDataAndConfigComponents(final Visit visit) {
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            JSONArray jsonArray = (JSONArray) response.getJSONObject().get("data");

                            Map<String, String> mapIdName = new HashMap<>();
                            for (int i = 0; i < jsonArray.length(); i ++) {
                                JSONObject amigo = (JSONObject) jsonArray.get(i);
                                mapIdName.put((String)amigo.get("id"), (String)amigo.get("name"));
                            }

                            configFriendsDataComponents(mapIdName, visit);

                            if (jsonArray.length() == 0) {
                                progress.setVisibility(GONE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();
    }

    private void obtainForecastDataAndConfigComponents(final Visit visit) {
        progress.setVisibility(View.VISIBLE);

        Call<WeatherReturn> call = weatherService.getForecast(visit.getLatitude(), visit.getLongitude());
        call.enqueue(new Callback<WeatherReturn>() {
            @Override
            public void onResponse(Call<WeatherReturn> call, Response<WeatherReturn> response) {
                if (response.isSuccessful()){

                    final WeatherReturn WeatherReturn = response.body();

                    runOnUiThread(new Runnable()
                    {
                        public void run() {
                            configForecastDataComponents(WeatherReturn, visit);

                            obtainFriendsDataAndConfigComponents(visit);
                        }
                    });
                } else {
                    showSnackBarError(getString(R.string.erro) + ": " + response.code() +" " +  response.message());
                }
            }

            @Override
            public void onFailure(Call<WeatherReturn> call, Throwable t) {
                showSnackBarError(getString(R.string.falha) + ": " +  t.getMessage());
            }
        });
    }

    private void configFriendsDataComponents(final Map<String, String> mapIdName, final Visit visit) {

        Call<MatchingVisitsReturn> call = backendService.getMatchingVisits(visit.getIdFacebook(),
                getAccessToken(),
                extractIdListAsString(mapIdName),
                visit.getPlaceId(),
                visit.getVisitDate(),
                visit.getStartHour(),
                visit.getEndHour());

        call.enqueue(new Callback<MatchingVisitsReturn>() {
            @Override
            public void onResponse(Call<MatchingVisitsReturn> call, Response<MatchingVisitsReturn> response) {
                if (response.isSuccessful()){

                    final MatchingVisitsReturn matchingVisitsReturn = response.body();

                    if (!matchingVisitsReturn.getSameDayVisits().isEmpty()) {
                        sameDayFriends = new HashSet<Friend>();
                        Set<Friend> sameMomentFriendsSet = new HashSet<Friend>();

                        for (Visit visit : matchingVisitsReturn.getSameDayVisits()) {
                            Friend friend = new Friend();
                            friend.setName(mapIdName.get(visit.getIdFacebook()));
                            friend.setVisit(visit);

                            sameDayFriends.add(friend);
                        }

                        for (Visit visit : matchingVisitsReturn.getSameTimeVisits()) {
                            Friend friend = new Friend();
                            friend.setName(mapIdName.get(visit.getIdFacebook()));
                            friend.setVisit(visit);

                            sameMomentFriendsSet.add(friend);
                        }

                        TextView txtSameDayFriends = (TextView) findViewById(R.id.txtSameDayFriendsNumber);
                        txtSameDayFriends.setText(". "+ sameDayFriends.size() + " pessoas visitarão o local neste dia");

                        TextView txtSameMomentFriends = (TextView) findViewById(R.id.txtSameMomentFriendsNumber);
                        txtSameMomentFriends.setText(". "+ sameMomentFriendsSet.size() + " pessoas visitarão o local durante a sua visit");

                    }
                } else {
                    if (response.code() == 401) {
                        showSnackBarUserForbidden(getString(R.string.usuario_nao_autoriado));
                    } else {
                        showSnackBarError(getString(R.string.erro) +  ": "  + response.code() +" " +  response.message());
                    }
                }

                progress.setVisibility(GONE);
            }


            @Override
            public void onFailure(Call<MatchingVisitsReturn> call, Throwable t) {
                showSnackBarError(getString(R.string.falha) + ": " +  t.getMessage());
                progress.setVisibility(GONE);
            }

        });
    }

    private double configForecastDataComponents(WeatherReturn weatherReturn, Visit visit) {
        ForecastData forecastData = obtainForecastData(weatherReturn, visit);

        TextView txtForecastTitle = (TextView) findViewById(R.id.txtForecastTitle);
        TextView txtRain = (TextView) findViewById(R.id.txtRain);
        TextView txtForecast = (TextView) findViewById(R.id.txtForecast);
        TextView txtMaxTempture = (TextView) findViewById(R.id.txtMaxTemp);
        TextView txtMinTempture = (TextView) findViewById(R.id.txtMinTemp);
        ImageView imgForecast = (ImageView) findViewById(R.id.imgForecast);

        txtForecastTitle.setText("Previsão do tempo ("+ forecastData.forecastDate + ")");

        txtRain.setText(format("Precipitação(mm): %3.4f", forecastData.rain));

        txtForecast.setText(toUpperCase(forecastData.forecast.charAt(0)) + forecastData.forecast.substring(1, forecastData.forecast.length()));

        txtMaxTempture.setText(format(Locale.FRENCH, "Máxima: %2.2f°", forecastData.maxTemp));

        txtMinTempture.setText(format(Locale.FRENCH, "Mínima: %2.2f°", forecastData.minTemp));

        Picasso.with(context).load("http://openweathermap.org/img/w/" + forecastData.iconId.substring(0, 2) + getDayNightChar(startTimeEndTimeMeanToCalculate(visit)) + ".png").into(imgForecast);

        return forecastData.rain;
    }

    @NonNull
    private String extractIdListAsString(Map<String, String> mapIdName) {
        StringBuffer ids = new StringBuffer();
        for (Map.Entry<String, String> entry : mapIdName.entrySet()) {
            ids.append(entry.getKey() + ",");
        }
        return ids.toString();
    }

    private ForecastData obtainForecastData(WeatherReturn weatherReturn, Visit visit) {
        String lastForecastDay = weatherReturn.getList().get(weatherReturn.getList().size() - 1).getDayMonthYearForecastString();
        String formatedVisitDay = Util.formatDateToCalculate(visit.getVisitDate());
        String formatedVisitTimeMean = startTimeEndTimeMeanToCalculate(visit);

        ForecastData forecastData = new ForecastData();

        if (lastForecastDay.compareTo(formatedVisitDay) >= 0) {
            forecastData.forecastDate = extractDayMonthYearDateString(formatedVisitDay);

            for (int i = 0; i < weatherReturn.getList().size(); i ++) {
                Forecast forecast = weatherReturn.getList().get(i);
                String forecastDateTime = forecast.getDt_txt();
                String forecastDate = forecastDateTime.substring(0, 10);

                if (forecastDate.compareTo(formatedVisitDay) == 0) {
                    if (forecast.getRain() != null && forecast.getRain().get3h() != null) {
                        forecastData.rain += forecast.getRain().get3h();
                    }

                    String forecastTime = forecastDateTime.substring(11, forecastDateTime.length());
                    String oneHourAndHalfBeforeForecastTime = subtractOneHourAndHalf(forecastTime);
                    String oneHourAndHalfAfterForecastTime = addOneHourAndHalf(forecastTime);

                    if (forecastTime.compareTo(formatedVisitTimeMean) > 0) {
                        forecastData.forecast = forecast.getWeather().get(0).getDescription();
                        forecastData.iconId = forecast.getWeather().get(0).getIcon();
                    } else if (formatedVisitTimeMean.compareTo(oneHourAndHalfBeforeForecastTime) >= 0 && formatedVisitTimeMean.compareTo(oneHourAndHalfAfterForecastTime) <= 0) {
                        forecastData.forecast = forecast.getWeather().get(0).getDescription();
                        forecastData.iconId = forecast.getWeather().get(0).getIcon();
                    }

                    if (forecastData.minTemp > forecast.getMain().getTemp_min().doubleValue()) {
                        forecastData.minTemp = forecast.getMain().getTemp_min().doubleValue();
                    }

                    if (forecastData.maxTemp < forecast.getMain().getTemp_max().doubleValue()) {
                        forecastData.maxTemp = forecast.getMain().getTemp_max().doubleValue();
                    }
                }
            }

        } else {
            forecastData.forecastDate = extractDayMonthYearDateString(lastForecastDay);

            for (int i = weatherReturn.getList().size() - 1; i >= 0; i--) {
                Forecast forecast = weatherReturn.getList().get(i);
                String forecastDateTime = forecast.getDt_txt();
                String forecastDate = forecastDateTime.substring(0, 10);

                if (forecastDate.compareTo(lastForecastDay) != 0) {
                    break;
                }

                forecastData.forecast = forecast.getWeather().get(0).getDescription();
                forecastData.iconId = forecast.getWeather().get(0).getIcon();

                if (forecastData.minTemp > forecast.getMain().getTemp_min().doubleValue()) {
                    forecastData.minTemp = forecast.getMain().getTemp_min().doubleValue();
                }

                if (forecastData.maxTemp < forecast.getMain().getTemp_max().doubleValue()) {
                    forecastData.maxTemp = forecast.getMain().getTemp_max().doubleValue();
                }

                if (forecast.getRain() != null && forecast.getRain().get3h() != null) {
                    forecastData.rain += forecast.getRain().get3h();
                }
            }
        }

        return forecastData;
    }

    private Bitmap readBitmap(String placeId) {
        Bitmap bitmap = null;

        try {
            FileInputStream fis = openFileInput(placeId);
            bitmap = BitmapFactory.decodeStream(fis);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    private void showSnackBarError(String msg){
        Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                }).show();
    }

    private void showSnackBarUserForbidden(String msg){
        Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.log_in, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LoginManager.getInstance().logOut();
                        Intent it = new Intent(context, LoginActivity.class);
                        startActivity(it);
                    }
                }).show();
    }

    private String startTimeEndTimeMeanToCalculate(Visit visit) {
        return Util.getHorarioMedioString(visit.getStartHour(),
                visit.getStartMinute(),
                visit.getEndHour(),
                visit.getEndMinute());
    }

    private String getAccessToken() {
        try {
            return DeCryptor.getInstance().getAccessToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private class ForecastData {
        public double rain = 0.0;
        public double maxTemp = 0.0;
        public double minTemp = 100.0;
        public String forecast = "";
        public String forecastDate;
        public String iconId = "";
    }
}
