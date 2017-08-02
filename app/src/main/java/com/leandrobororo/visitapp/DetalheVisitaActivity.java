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
import com.leandrobororo.visitapp.model.MatchingVisits;
import com.leandrobororo.visitapp.model.Previsao;
import com.leandrobororo.visitapp.model.RetornoCallWeather;
import com.leandrobororo.visitapp.model.Visita;
import com.leandrobororo.visitapp.services.APIBackendVisitasService;
import com.leandrobororo.visitapp.services.APIWeatherService;
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

import com.leandrobororo.visitapp.model.Amigo;

import static android.view.View.GONE;
import static com.leandrobororo.visitapp.util.Util.extrairDiaMesAnoStringDataVisita;
import static com.leandrobororo.visitapp.util.Util.getDiaOuNoite;
import static com.leandrobororo.visitapp.util.Util.somaUmaHoraMeia;
import static com.leandrobororo.visitapp.util.Util.subtraiUmaHoraMeia;
import static java.lang.Character.toUpperCase;
import static java.lang.String.format;

public class DetalheVisitaActivity extends AppCompatActivity {

    private DetalheVisitaActivity context = this;
    private CoordinatorLayout coordinatorLayout;
    private APIWeatherService apiWeatherService;
    private APIBackendVisitasService backendService;
    private ProgressBar progress;
    private Set<Amigo> amigosMesmoDia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe_visita);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progress = (ProgressBar) findViewById(R.id.progressDetalhe);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayoutDetalhe);

        Bundle extras = getIntent().getExtras();
        final Visita visita = (Visita) extras.get("visita");

        instanciarAPIWeatherService();

        instanciarAPIVisitasService();

        configurarDadosEstaticos(visita);

        configurarEventosBotoes(visita);

        desabilitarDadosAmigosSeVisitaSemAcompanharAmigos(visita);

        obterPrevisaoConfigurarDadosPrevisaoTela(visita);
    }

    @NonNull
    private void configurarEventosBotoes(final Visita visita) {
        Button buttonEstacionamentos = (Button) findViewById(R.id.buttonEstacionamento);
        buttonEstacionamentos.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("geo:" + visita.getLatitude() + "," + visita.getLongitude() + "?q=estacionamentos");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        Button buttonAmigos = (Button) findViewById(R.id.buttonAmigos);
        buttonAmigos.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                ArrayList<Amigo> amigos = new ArrayList<Amigo>();

                if (amigosMesmoDia != null) {
                    amigos.addAll(amigosMesmoDia);
                }

                Intent it = new Intent(context, AmigosActivity.class);
                it.putExtra("amigos", amigos);
                it.putExtra("visita", visita);
                startActivity(it);
            }
        });
    }

    private void configurarDadosEstaticos(Visita visita) {
        ImageView imagePlace = (ImageView) findViewById(R.id.imgDetalheGooglePlace);
        imagePlace.setImageBitmap(readBitmap(visita.getPlaceId()));

        TextView txtNomePlace = (TextView) findViewById(R.id.txtDescricaoPlace);
        txtNomePlace.setText(visita.getNomePlace());

        TextView txtEnderecoPlace = (TextView) findViewById(R.id.txtEnderecoPlace);
        txtEnderecoPlace.setText(visita.getEnderecoPlace());

        TextView txtHorarioVisita = (TextView) findViewById(R.id.txtHorarioVisita);
        txtHorarioVisita.setText(visita.getDescricaoVisita());
    }

    private void instanciarAPIVisitasService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(APIBackendVisitasService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        backendService = retrofit.create(APIBackendVisitasService.class);
    }

    private void instanciarAPIWeatherService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(APIWeatherService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiWeatherService = retrofit.create(APIWeatherService.class);
    }

    private void desabilitarDadosAmigosSeVisitaSemAcompanharAmigos(Visita visita) {
        if (!visita.isAcompanharAmigos()) {
            findViewById(R.id.buttonAmigos).setVisibility(GONE);
            findViewById(R.id.txtVisitantesPlaceDia).setVisibility(GONE);
            findViewById(R.id.txtVisitantesPlaceMomento).setVisibility(GONE);
        }
    }

    private void obterVisitasAmigosConfigurarDadosAmigosTela(final Visita visita) {
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

                            callAtualizarContagemAmigos(mapIdName, visita);

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

    private void obterPrevisaoConfigurarDadosPrevisaoTela(final Visita visita) {
        progress.setVisibility(View.VISIBLE);

        Call<RetornoCallWeather> call = apiWeatherService.obterPrevisao(visita.getLatitude(), visita.getLongitude());
        call.enqueue(new Callback<RetornoCallWeather>() {
            @Override
            public void onResponse(Call<RetornoCallWeather> call, Response<RetornoCallWeather> response) {
                if (response.isSuccessful()){

                    final RetornoCallWeather retornoCallWeather = response.body();

                    runOnUiThread(new Runnable()
                    {
                        public void run() {
                            configurarDadosPrevisao(retornoCallWeather, visita);

                            obterVisitasAmigosConfigurarDadosAmigosTela(visita);
                        }
                    });
                } else {
                    showSnackBarErro("Erro: " +  " "  + response.code() +" " +  response.message());
                }
            }

            @Override
            public void onFailure(Call<RetornoCallWeather> call, Throwable t) {
                showSnackBarErro("Falha: " +  t.getMessage());
            }
        });
    }

    private void callAtualizarContagemAmigos(final Map<String, String> mapIdName, final Visita visita) {

        StringBuffer ids = new StringBuffer();
        for (Map.Entry<String, String> entry : mapIdName.entrySet()) {
            ids.append(entry.getKey() + ",");
        }

        Call<MatchingVisits> call = backendService.getMatchingVisits(visita.getIdFacebook(), getAccessToken(), ids.toString(), visita.getPlaceId(), visita.getDataVisita(), visita.getHoraInicioVisita(), visita.getHoraFimVisita());
        call.enqueue(new Callback<MatchingVisits>() {
            @Override
            public void onResponse(Call<MatchingVisits> call, Response<MatchingVisits> response) {
                if (response.isSuccessful()){

                    final MatchingVisits matchingVisits = response.body();

                    if (!matchingVisits.getSameDayVisits().isEmpty()) {
                        amigosMesmoDia = new HashSet<Amigo>();
                        Set<Amigo> amigosMesmoMomento = new HashSet<Amigo>();

                        for (Visita visita : matchingVisits.getSameDayVisits()) {
                            Amigo amigo = new Amigo();
                            amigo.setNome(mapIdName.get(visita.getIdFacebook()));
                            amigo.setVisita(visita);

                            amigosMesmoDia.add(amigo);
                        }

                        for (Visita visita : matchingVisits.getSameTimeVisits()) {
                            Amigo amigo = new Amigo();
                            amigo.setNome(mapIdName.get(visita.getIdFacebook()));
                            amigo.setVisita(visita);

                            amigosMesmoMomento.add(amigo);
                        }

                        TextView txtVisitantesDia = (TextView) findViewById(R.id.txtVisitantesPlaceDia);
                        txtVisitantesDia.setText(". "+ amigosMesmoDia.size() + " pessoas visitarão o local neste dia");

                        TextView txtVisitantesMomento = (TextView) findViewById(R.id.txtVisitantesPlaceMomento);
                        txtVisitantesMomento.setText(". "+ amigosMesmoMomento.size() + " pessoas visitarão o local durante a sua visita");

                    }
                } else {
                    if (response.code() == 401) {
                        showSnackBarUsuarioNaoAutorizado("Erro: " + " " + response.code() + " " + response.message());
                    } else {
                        showSnackBarErro("Erro: " +  " "  + response.code() +" " +  response.message());
                    }
                }

                progress.setVisibility(GONE);
            }


            @Override
            public void onFailure(Call<MatchingVisits> call, Throwable t) {
                showSnackBarErro("Falha: " +  t.getMessage());
                progress.setVisibility(GONE);
            }

        });
    }

    private void showSnackBarErro(String msg){
        Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_INDEFINITE)
                .setAction("Ok", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                }).show();
    }

    private void showSnackBarUsuarioNaoAutorizado(String msg){
        Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_INDEFINITE)
                .setAction("Log in", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LoginManager.getInstance().logOut();
                        Intent it = new Intent(context, MainActivity.class);
                        startActivity(it);
                    }
                }).show();
    }

    private double configurarDadosPrevisao(RetornoCallWeather retornoCallWeather, Visita visita) {
        String ultimoDiaPrevisao = retornoCallWeather.getList().get(retornoCallWeather.getList().size() - 1).getStringDiaMesAnoPrevisao();
        String diaVisita = visita.obterDiaMesAnoStringDataVisita();
        String horaMediaVisita = visita.getHorarioMedioString();

        TextView txtTituloPrevisao = (TextView) findViewById(R.id.txtTituloPrevisao);
        TextView txtChuva = (TextView) findViewById(R.id.txtChuva);
        TextView txtPrevisao = (TextView) findViewById(R.id.txtPrevisao);
        TextView txtTempMaxima = (TextView) findViewById(R.id.txtTempMaxima);
        TextView txtTempMinima = (TextView) findViewById(R.id.txtTempMinima);
        ImageView imagePrevisao = (ImageView) findViewById(R.id.imgPrevisao);

        double precipitacao = 0.0;
        double temperaturaMaxima = 0.0;
        double temperaturaMinima = 100.0;
        String textoPrevisao = "";
        String dataPrevisao;
        String icon = "";

        if (ultimoDiaPrevisao.compareTo(diaVisita) >= 0) {
            dataPrevisao = extrairDiaMesAnoStringDataVisita(diaVisita);

            for (int i = 0; i < retornoCallWeather.getList().size(); i ++) {
                Previsao previsao = retornoCallWeather.getList().get(i);
                String dataHorario = previsao.getDt_txt();
                String data = dataHorario.substring(0, 10);

                if (data.compareTo(diaVisita) == 0) {
                    if (previsao.getRain() != null && previsao.getRain().get3h() != null) {
                        precipitacao += previsao.getRain().get3h();
                    }

                    String horario = dataHorario.substring(11, dataHorario.length());
                    String umaHoraMeiaAntes = subtraiUmaHoraMeia(horario);
                    String umaHoraMeiaDepois = somaUmaHoraMeia(horario);

                    if (horario.compareTo(horaMediaVisita) > 0) {
                        textoPrevisao = previsao.getWeather().get(0).getDescription();
                        icon = previsao.getWeather().get(0).getIcon();
                    } else if (horaMediaVisita.compareTo(umaHoraMeiaAntes) >= 0 && horaMediaVisita.compareTo(umaHoraMeiaDepois) <= 0) {
                        textoPrevisao = previsao.getWeather().get(0).getDescription();
                        icon = previsao.getWeather().get(0).getIcon();
                    }

                    if (temperaturaMinima > previsao.getMain().getTemp_min().doubleValue()) {
                        temperaturaMinima = previsao.getMain().getTemp_min().doubleValue();
                    }

                    if (temperaturaMaxima < previsao.getMain().getTemp_max().doubleValue()) {
                        temperaturaMaxima = previsao.getMain().getTemp_max().doubleValue();
                    }
                }
            }

        } else {
            dataPrevisao = extrairDiaMesAnoStringDataVisita(ultimoDiaPrevisao);

            for (int i = retornoCallWeather.getList().size() - 1; i >= 0; i--) {
                Previsao previsao = retornoCallWeather.getList().get(i);
                String dataHorario = previsao.getDt_txt();
                String data = dataHorario.substring(0, 10);

                if (data.compareTo(ultimoDiaPrevisao) != 0) {
                    break;
                }

                textoPrevisao = previsao.getWeather().get(0).getDescription();
                icon = previsao.getWeather().get(0).getIcon();

                if (temperaturaMinima > previsao.getMain().getTemp_min().doubleValue()) {
                    temperaturaMinima = previsao.getMain().getTemp_min().doubleValue();
                }

                if (temperaturaMaxima < previsao.getMain().getTemp_max().doubleValue()) {
                    temperaturaMaxima = previsao.getMain().getTemp_max().doubleValue();
                }

                if (previsao.getRain() != null && previsao.getRain().get3h() != null) {
                    precipitacao += previsao.getRain().get3h();
                }
            }
        }

        txtTituloPrevisao.setText("Previsão do tempo ("+ dataPrevisao + ")");

        txtChuva.setText(format("Precipitação(mm): %3.4f", precipitacao));

        txtPrevisao.setText(toUpperCase(textoPrevisao.charAt(0)) + textoPrevisao.substring(1, textoPrevisao.length()));

        txtTempMaxima.setText(format(Locale.FRENCH, "Máxima: %2.2f°", temperaturaMaxima));

        txtTempMinima.setText(format(Locale.FRENCH, "Mínima: %2.2f°", temperaturaMinima));

        Picasso.with(context).load("http://openweathermap.org/img/w/" + icon.substring(0, 2) + getDiaOuNoite(visita.getHorarioMedioString()) + ".png").into(imagePrevisao);

        return precipitacao;
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

    private String getAccessToken() {
        try {
            return DeCryptor.getInstance().getAccessToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
