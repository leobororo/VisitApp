package com.leandrobororo.visitapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.leandrobororo.visitapp.model.RetornoAPIVisitas;
import com.leandrobororo.visitapp.model.Previsao;
import com.leandrobororo.visitapp.model.RetornoCallWeather;
import com.leandrobororo.visitapp.model.Visita;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.leandrobororo.visitapp.model.Amigo;

import static android.view.View.GONE;
import static com.leandrobororo.visitapp.Util.extrairDiaMesAnoStringDataVisita;
import static com.leandrobororo.visitapp.Util.getDiaOuNoite;
import static com.leandrobororo.visitapp.Util.somaUmaHoraMeia;
import static com.leandrobororo.visitapp.Util.subtraiUmaHoraMeia;
import static java.lang.Character.toUpperCase;
import static java.lang.String.format;

public class DetalheVisitaActivity extends AppCompatActivity {

    private DetalheVisitaActivity context = this;
    private APIWeatherService apiWeatherService;
    private APIVisitasService apiVisitasService;
    private ProgressBar progress;
    private ArrayList<Amigo> amigos;
    private int numeroAmigosDia;
    private int numeroAmigosMomento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe_visita);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progress = (ProgressBar) findViewById(R.id.progressDetalhe);

        Bundle extras = getIntent().getExtras();
        final Visita visita = (Visita) extras.get("visita");

        instanciarAPIWeatherService();

        instanciarAPIVisitasService();

        resetContagemAmigosEListaAmigos();

        configurarDadosEstaticos(visita);

        configurarEventosBotoes(visita);

        desabilitarDadosAmigosSeVisitaSemAcompanharAmigos(visita);

        obterPrevisaoConfigurarDadosPrevisaoTela(visita);
    }

    private void resetContagemAmigosEListaAmigos() {
        numeroAmigosDia = 0;
        numeroAmigosMomento = 0;
        amigos = new ArrayList<Amigo>();
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
                .baseUrl(APIVisitasService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiVisitasService = retrofit.create(APIVisitasService.class);
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
                            JSONArray amigos = (JSONArray) response.getJSONObject().get("data");

                            java.util.List<String> ids = new ArrayList<String>();
                            for (int i = 0; i < amigos.length(); i ++) {
                                JSONObject amigo = (JSONObject) amigos.get(i);
                                callAtualizarContagemAmigos((String)amigo.get("id"), (String)amigo.get("name"), visita);
                            }

                            if (amigos.length() == 0) {
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
                    makeToast("Erro: " +  " "  + response.code() +" " +  response.message());
                }
            }

            @Override
            public void onFailure(Call<RetornoCallWeather> call, Throwable t) {
                makeToast("Falha: " +  t.getMessage());
            }
        });
    }

    private void callAtualizarContagemAmigos(final String idFacebook, final String nome, final Visita visita) {
        Call<RetornoAPIVisitas> call = apiVisitasService.getVisitas();

        call.enqueue(new Callback<RetornoAPIVisitas>() {
            @Override
            public void onResponse(Call<RetornoAPIVisitas> call, Response<RetornoAPIVisitas> response) {
                if (response.isSuccessful()){

                    final RetornoAPIVisitas retornoCallWeather = response.body();

                    Amigo amigo;
                    for (Visita visitaAux : retornoCallWeather.getResults()) {
                        if (mesmoUsuarioPlaceData(visitaAux, visita, idFacebook)) {
                            numeroAmigosDia++;

                            amigo = new Amigo();
                            amigo.setNome(nome);
                            amigo.setId(idFacebook);
                            amigo.setVisita(visitaAux);
                            amigos.add(amigo);

                            if ((visita.getHorarioInicio().compareTo(visitaAux.getHorarioInicio()) >= 0 && visita.getHorarioInicio().compareTo(visitaAux.getHorarioFim()) <= 0)
                                    ||
                                    (visita.getHorarioFim().compareTo(visitaAux.getHorarioInicio()) >= 0 && visita.getHorarioFim().compareTo(visitaAux.getHorarioFim()) <= 0)){
                                numeroAmigosMomento++;
                            }

                            TextView txtVisitantesDia = (TextView) findViewById(R.id.txtVisitantesPlaceDia);
                            txtVisitantesDia.setText(". "+ numeroAmigosDia + " pessoas visitarão o local neste dia");

                            TextView txtVisitantesMomento = (TextView) findViewById(R.id.txtVisitantesPlaceMomento);
                            txtVisitantesMomento.setText(". "+ numeroAmigosMomento + " pessoas visitarão o local durante a sua visita");

                            break;
                        }
                    }
                } else {
                    makeToast("Erro: " +  " "  + response.code() +" " +  response.message());
                }

                progress.setVisibility(GONE);
            }


            @Override
            public void onFailure(Call<RetornoAPIVisitas> call, Throwable t) {
                makeToast("Falha: " +  t.getMessage());
                progress.setVisibility(GONE);
            }

        });
    }

    private boolean mesmoUsuarioPlaceData(Visita visitaAux, Visita visita, String idFacebook) {
        return visitaAux.getPlaceId().equals(visita.getPlaceId()) && visitaAux.getIdFacebook().equals(idFacebook) && visitaAux.getDataVisita().equals(visita.getDataVisita());
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

    private void makeToast(String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT)
                .show();
    }
}
