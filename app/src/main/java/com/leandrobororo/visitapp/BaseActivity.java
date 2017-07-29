package com.leandrobororo.visitapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.leandrobororo.visitapp.model.RetornoAPIVisitas;
import com.leandrobororo.visitapp.model.Visita;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static com.leandrobororo.visitapp.Util.CANCELAR;
import static com.leandrobororo.visitapp.Util.DESEJA_ACOMPANHAR_PLANOS_DE_VISITA_DE_AMIGOS_AO_MESMO_LOCAL;
import static com.leandrobororo.visitapp.Util.NAO;
import static com.leandrobororo.visitapp.Util.REGISTRO_CANCELADO;
import static com.leandrobororo.visitapp.Util.SIM;
import static com.leandrobororo.visitapp.Util.gerarDataVisita;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

public class BaseActivity extends AppCompatActivity {

    private static final int PLACE_PICKER_REQUEST = 1;
    private final Context context = this;
    private static GoogleApiClient mGoogleApiClient;
    private APIVisitasService visitasService;
    private APIBackendVisitasService visitasBackendService;
    private Profile profile;
    private AdapterListVisitas adapter;
    private ListView listVisitas;
    private CoordinatorLayout coordinatorLayout;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_base);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        profile = (Profile) extras.get("profile");

        obterReferenciasParaComponentesDaActivity();

        instanciarVisitasService();

        instanciarGoogleAPIClient();

        adicionarEventoAoFloatButton();

        callGetVisitas();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void obterReferenciasParaComponentesDaActivity() {
        listVisitas = (ListView) findViewById(R.id.listVisitas);
        progress = (ProgressBar) findViewById(R.id.progress);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
    }

    private void instanciarGoogleAPIClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi( Places.GEO_DATA_API )
                .addApi( Places.PLACE_DETECTION_API )
                .build();
    }

    private void instanciarVisitasService() {
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(APIVisitasService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        visitasService = retrofit.create(APIVisitasService.class);

        final Retrofit retrofit2 = new Retrofit.Builder()
                .baseUrl(APIBackendVisitasService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        visitasBackendService = retrofit2.create(APIBackendVisitasService.class);
    }

    private void adicionarEventoAoFloatButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                obterLocalVisita();
            }
        });
    }

    private void callGetVisitas() {
        progress.setVisibility(View.VISIBLE);

        Call<List<Visita>> call = visitasBackendService.getVisitas(profile.getId());
        call.enqueue(new Callback<List<Visita>>() {
            @Override
            public void onResponse(Call<List<Visita>> call, Response<List<Visita>> response) {
                if (response.isSuccessful()){

                    List<Visita> visitasUsuario = response.body();

                    adapter = new AdapterListVisitas(BaseActivity.this, visitasUsuario);
                    listVisitas.setAdapter(adapter);
                    listVisitas.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent it = new Intent(context, DetalheVisitaActivity.class);
                            it.putExtra("visita", (Visita) parent.getItemAtPosition(position));
                            startActivity(it);
                        }
                    });
                } else {
                    showSnackBarTentarDeNovoGetVisitas("Erro: " +  " "  + response.code() +" " +  response.message());
                }
                progress.setVisibility(View.GONE);
            }


            @Override
            public void onFailure(Call<List<Visita>> call, Throwable t) {
                showSnackBarTentarDeNovoGetVisitas("Falha: " + t.getMessage());
                progress.setVisibility(View.GONE);
            }

        });
    }

    private void showSnackBarTentarDeNovoGetVisitas(String msg){
        Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_INDEFINITE)
                .setAction("Tentar", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callGetVisitas();
                    }
                }).show();
    }

    private void obterLocalVisita() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(BaseActivity.this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            makeToast("Exceção: " + e.getMessage());
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            makeToast("Exceção: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(getApplicationContext(), data);

                Visita visita = new Visita();
                visita.setPlaceId(place.getId());
                visita.setNomePlace(place.getName().toString());
                visita.setEnderecoPlace(place.getAddress().toString());
                visita.setLatitude(place.getLatLng().latitude);
                visita.setLongitude(place.getLatLng().longitude);

                obterDataHorarioVisita(visita);
            } else {
                makeToast(REGISTRO_CANCELADO);
            }
        }
    }

    private void obterDataHorarioVisita(final Visita visita) {
        Calendar hoje = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                visita.setDataVisita(gerarDataVisita(year, month + 1, dayOfMonth));
                obterHoraInicioEHoraFimVisita(visita);
            }
        }, hoje.get(YEAR), hoje.get(MONTH), hoje.get(DAY_OF_MONTH));

        dialog.setButton(BUTTON_NEGATIVE, CANCELAR, criarDialogOnCancelListener());

        dialog.show();
    }

    private void obterHoraInicioEHoraFimVisita(final Visita visita) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(BaseActivity.this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, final int horaInicio, final int minutoInicio) {
                visita.setHoraInicioVisita(horaInicio);
                visita.setMinutoInicioVisita(minutoInicio);

                TimePickerDialog timePickerDialog = new TimePickerDialog(BaseActivity.this, new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int horaFim, int minutoFim) {
                        visita.setHoraFimVisita(horaFim);
                        visita.setMinutoFimVisita(minutoFim);

                        configurarAcompanharVisitasAmigos(visita);
                    }
                }, 0, 0, true);

                timePickerDialog.setButton(BUTTON_NEGATIVE, CANCELAR, criarDialogOnCancelListener());

                timePickerDialog.show();

            }
        }, 0, 0, true);

        timePickerDialog.setButton(BUTTON_NEGATIVE, CANCELAR, criarDialogOnCancelListener());

        timePickerDialog.show();
    }

    @NonNull
    private DialogInterface.OnClickListener criarDialogOnCancelListener() {
        return new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == BUTTON_NEGATIVE) {
                    makeToast(REGISTRO_CANCELADO);
                }
            }
        };
    }

    private void configurarAcompanharVisitasAmigos(final Visita visita) {
        AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);
        builder.setMessage(DESEJA_ACOMPANHAR_PLANOS_DE_VISITA_DE_AMIGOS_AO_MESMO_LOCAL);

        builder.setPositiveButton(SIM, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                visita.setAcompanharAmigos(true);

                callSalvarVisita(visita);
            }
        });

        builder.setNegativeButton(NAO, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
            visita.setAcompanharAmigos(false);

            callSalvarVisita(visita);
            }
        });

        AlertDialog alerta = builder.create();
        alerta.show();

    }

    private void callSalvarVisita(Visita visita) {
        visita.setIdFacebook(profile.getId());

        Call<ResponseBody> call = visitasBackendService.salvarVisita(visita);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 201){
                    callGetVisitas();
                } else {
                    makeToast(Util.NAO_FOI_POSSIVEL_SALVAR_A_VISITA);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                makeToast(Util.OCORREU_UM_ERRO_AO_ACESSAR_O_SERVICO);

            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_configuracoes) {
        } else if (id == R.id.action_sair) {
            LoginManager.getInstance().logOut();
            Intent it = new Intent(context, MainActivity.class);
            startActivity(it);
        }

        return super.onOptionsItemSelected(item);
    }

    private static GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void configurarImagem(final ImageView imageView, final Visita visitaItem) {
        Bitmap bitmap = readBitmap(visitaItem.getPlaceId());

        if (bitmap == null) {
            Places.GeoDataApi.getPlacePhotos(getGoogleApiClient(), visitaItem.getPlaceId()).setResultCallback(new ResultCallback<PlacePhotoMetadataResult>() {
                @Override
                public void onResult(@NonNull PlacePhotoMetadataResult placePhotoMetadataResult) {

                    if (placePhotoMetadataResult != null && placePhotoMetadataResult.getStatus().isSuccess()) {
                        PlacePhotoMetadataBuffer photoMetadata = placePhotoMetadataResult.getPhotoMetadata();
                        final PlacePhotoMetadata placePhotoMetadata = photoMetadata.get(0);
                        placePhotoMetadata.getPhoto(getGoogleApiClient()).setResultCallback(new ResultCallback<PlacePhotoResult>() {
                                                                                                @Override
                                                                                                public void onResult(@NonNull PlacePhotoResult placePhotoResult) {
                                                                                                    Bitmap bitmap = placePhotoResult.getBitmap();
                                                                                                    salvarBitmap(bitmap, visitaItem.getPlaceId());
                                                                                                    imageView.setImageBitmap(bitmap);
                                                                                                }
                                                                                            }
                        );
                        photoMetadata.release();
                    }
                }
            });
        } else {
            imageView.setImageBitmap(bitmap);
        }
    }

    private void salvarBitmap(Bitmap bitmap, String placeId) {
        try {
            FileOutputStream outputStream = openFileOutput(placeId, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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