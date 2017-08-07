package com.leandrobororo.visitapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
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
import com.leandrobororo.visitapp.adapters.AdapterVisitList;
import com.leandrobororo.visitapp.criptografia.DeCryptor;
import com.leandrobororo.visitapp.model.Visit;
import com.leandrobororo.visitapp.services.APIBackendService;
import com.leandrobororo.visitapp.util.Util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static com.leandrobororo.visitapp.util.Util.gerarDataVisita;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

public class DashboardActivity extends AppCompatActivity {

    private static final int PLACE_PICKER_REQUEST = 1;
    private static GoogleApiClient googleClient;

    private final Context context = this;

    private APIBackendService backendService;
    private CoordinatorLayout coordinatorLayout;
    private SwipeMenuListView listView;
    private AdapterVisitList adapter;
    private Profile facebookProfile;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        facebookProfile = (Profile) extras.get(getString(R.string.extra_profile));

        getComponentsReferences();
        getServiceReference();
        getGoogleAPIClientReference();
        setFloatButton();
        setVisitsListComponent();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (googleClient != null)
            googleClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (googleClient != null && googleClient.isConnected()) {
            googleClient.disconnect();
        }
    }

    private void getComponentsReferences() {
        listView = (SwipeMenuListView) findViewById(R.id.listVisitas);

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                openItem.setWidth(convertFromDpToPx(90));

                // set item title
                openItem.setTitle(getString(R.string.abrir));
                openItem.setTitleSize(18);
                openItem.setTitleColor(Color.WHITE);

                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                deleteItem.setWidth(convertFromDpToPx(90));

                // set item title
                deleteItem.setTitle(getString(R.string.excluir));
                deleteItem.setTitleSize(18);
                deleteItem.setTitleColor(Color.WHITE);

                menu.addMenuItem(deleteItem);
            }
        };

        // set creator
        listView.setMenuCreator(creator);

        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        Intent it = new Intent(context, VisitDetailActivity.class);
                        it.putExtra(getString(R.string.extra_visita), (Visit) adapter.getItem(position));
                        startActivity(it);

                        break;
                    case 1:
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage(R.string.deseja_excluir_visita);

                        builder.setPositiveButton(R.string.sim, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                ((DashboardActivity)context).deleteVisit((Visit) adapter.getItem(position));
                            }
                        });

                        builder.setNegativeButton(R.string.nao, null);

                        AlertDialog alerta = builder.create();
                        alerta.show();

                        break;
                }

                return false;
            }
        });

        progress = (ProgressBar) findViewById(R.id.progress);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
    }

    private void getGoogleAPIClientReference() {
        googleClient = new GoogleApiClient.Builder(context)
                .addApi( Places.GEO_DATA_API )
                .addApi( Places.PLACE_DETECTION_API )
                .build();
    }

    private void getServiceReference() {
        final Retrofit retrofit2 = new Retrofit.Builder()
                .baseUrl(APIBackendService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        backendService = retrofit2.create(APIBackendService.class);
    }

    private void setFloatButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                obterLocalVisita();
            }
        });
    }

    private void setVisitsListComponent() {
        progress.setVisibility(View.VISIBLE);

        Call<List<Visit>> call = backendService.getVisits(facebookProfile.getId(), getAccessToken());
        call.enqueue(new Callback<List<Visit>>() {
            @Override
            public void onResponse(Call<List<Visit>> call, Response<List<Visit>> response) {
                if (response.isSuccessful()){

                    List<Visit> visits = response.body();

                    adapter = new AdapterVisitList(DashboardActivity.this, visits);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent it = new Intent(context, VisitDetailActivity.class);
                            it.putExtra(context.getString(R.string.extra_visita), (Visit) parent.getItemAtPosition(position));
                            startActivity(it);
                        }
                    });
                } else {
                    if (response.code() == 401) {
                        showSnackBarUserForbidden(getString(R.string.usuario_nao_autoriado));
                    } else {
                        showSnackBarTryAgainGetVisits(getString(R.string.erro) + ": " + response.code() + " " + response.message());
                    }
                }
                progress.setVisibility(View.GONE);
            }


            @Override
            public void onFailure(Call<List<Visit>> call, Throwable t) {
                showSnackBarTryAgainGetVisits(getString(R.string.falha) + ": " + t.getMessage());
                progress.setVisibility(View.GONE);
            }

        });
    }

    private String getAccessToken() {
        try {
            return DeCryptor.getInstance().getAccessToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void saveVisit(final Visit visit) {
        visit.setIdFacebook(facebookProfile.getId());

        progress.setVisibility(View.VISIBLE);

        Call<ResponseBody> call = backendService.saveVisit(visit, visit.getIdFacebook(), getAccessToken());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progress.setVisibility(View.GONE);
                if (response.isSuccessful()){
                    setVisitsListComponent();
                } else {
                    if (response.code() == 401) {
                        showSnackBarUserForbidden(getString(R.string.usuario_nao_autoriado));
                    } else {
                        showSnackBarTryAgainSaveVisit(getString(R.string.erro) +  ": "  + response.code() +  " " +  response.message());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progress.setVisibility(View.GONE);
                showSnackBarTryAgainSaveVisit(getString(R.string.falha) + ": " + t.getMessage());
            }

        });
    }

    private void deleteVisit(final Visit visit) {
        progress.setVisibility(View.VISIBLE);

        Call<ResponseBody> call = backendService.deletVisit(visit.getObjectId(), visit.getIdFacebook(), getAccessToken());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progress.setVisibility(View.GONE);
                if (response.isSuccessful()){
                    setVisitsListComponent();
                } else {
                    if (response.code() == 401) {
                        showSnackBarUserForbidden(getString(R.string.usuario_nao_autoriado));
                    } else {
                        showSnackBarTryAgainDeleteVisit(getString(R.string.erro) +  ": "  + response.code() +  " " +  response.message(), visit);
                    }
                }
            }


            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progress.setVisibility(View.GONE);
                showSnackBarTryAgainDeleteVisit(getString(R.string.falha) + ": " + t.getMessage(), visit);
            }

        });
    }

    private void showSnackBarTryAgainDeleteVisit(String msg, final Visit visit){
        Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.tentar, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteVisit(visit);
                    }
                }).show();
    }

    private void showSnackBarTryAgainSaveVisit(String msg){
        Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                }).show();
    }

    private void showSnackBarTryAgainGetVisits(String msg){
        Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.tentar, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setVisitsListComponent();
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

    private void obterLocalVisita() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(DashboardActivity.this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            makeToast(getString(R.string.excecao) + ": " + e.getMessage());
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            makeToast(getString(R.string.excecao) + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(getApplicationContext(), data);

                Visit visit = new Visit();
                visit.setPlaceId(place.getId());
                visit.setPlaceName(place.getName().toString());
                visit.setPlaceAddress(place.getAddress().toString());
                visit.setLatitude(place.getLatLng().latitude);
                visit.setLongitude(place.getLatLng().longitude);

                obterDataHorarioVisita(visit);
            } else {
                makeToast(this.getString(R.string.registro_cancelado));
            }
        }
    }

    private void obterDataHorarioVisita(final Visit visit) {
        final Calendar hoje = Calendar.getInstance();
        hoje.set(Calendar.HOUR, 0);
        hoje.set(Calendar.MINUTE, 0);

        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(YEAR, year);
                calendar.set(MONTH, month);
                calendar.set(DAY_OF_MONTH, dayOfMonth);

                if (hoje.compareTo(calendar) >= 0) {
                    makeToast(getString(R.string.data_visita_posterior));
                } else {
                    visit.setVisitDate(gerarDataVisita(year, month + 1, dayOfMonth));
                    obterHoraInicioEHoraFimVisita(visit);
                }
            }
        }, hoje.get(YEAR), hoje.get(MONTH), hoje.get(DAY_OF_MONTH));

        dialog.setButton(BUTTON_NEGATIVE, this.getString(R.string.cancelar), criarDialogOnCancelListener());

        dialog.show();
    }

    private void obterHoraInicioEHoraFimVisita(final Visit visit) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(DashboardActivity.this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, final int horaInicio, final int minutoInicio) {
                visit.setStartHour(horaInicio);
                visit.setStartMinute(minutoInicio);

                TimePickerDialog timePickerDialog = new TimePickerDialog(DashboardActivity.this, new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int horaFim, int minutoFim) {

                        if (Util.isTimeBoxValid(horaInicio, minutoInicio, horaFim, minutoFim )) {
                            visit.setEndHour(horaFim);
                            visit.setEndMinute(minutoFim);

                            configurarAcompanharVisitasAmigos(visit);
                        } else {
                            makeToast(getString(R.string.hora_fim_posterior));
                        }
                    }
                }, 0, 0, true);

                timePickerDialog.setButton(BUTTON_NEGATIVE, context.getString(R.string.cancelar), criarDialogOnCancelListener());

                timePickerDialog.show();

            }
        }, 0, 0, true);

        timePickerDialog.setButton(BUTTON_NEGATIVE, context.getString(R.string.cancelar), criarDialogOnCancelListener());

        timePickerDialog.show();
    }

    @NonNull
    private DialogInterface.OnClickListener criarDialogOnCancelListener() {
        return new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == BUTTON_NEGATIVE) {
                    makeToast(context.getString(R.string.registro_cancelado));
                }
            }
        };
    }

    private void configurarAcompanharVisitasAmigos(final Visit visit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
        builder.setMessage(this.getString(R.string.deseja_acompanhar_amigos));

        builder.setPositiveButton(this.getString(R.string.sim), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                visit.setFollowUpFriends(true);

                saveVisit(visit);
            }
        });

        builder.setNegativeButton(this.getString(R.string.nao), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
            visit.setFollowUpFriends(false);

            saveVisit(visit);
            }
        });

        AlertDialog alerta = builder.create();
        alerta.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sair) {
            LoginManager.getInstance().logOut();
            Intent it = new Intent(context, LoginActivity.class);
            startActivity(it);
        }

        return super.onOptionsItemSelected(item);
    }

    private static GoogleApiClient getGoogleClient() {
        return googleClient;
    }

    public void configurarImagem(final ImageView imageView, final Visit visitItem) {
        Bitmap bitmap = readBitmap(visitItem.getPlaceId());

        if (bitmap == null) {
            Places.GeoDataApi.getPlacePhotos(getGoogleClient(), visitItem.getPlaceId()).setResultCallback(new ResultCallback<PlacePhotoMetadataResult>() {
                @Override
                public void onResult(@NonNull PlacePhotoMetadataResult placePhotoMetadataResult) {

                    if (placePhotoMetadataResult != null && placePhotoMetadataResult.getStatus().isSuccess()) {
                        PlacePhotoMetadataBuffer photoMetadata = placePhotoMetadataResult.getPhotoMetadata();
                        final PlacePhotoMetadata placePhotoMetadata = photoMetadata.get(0);
                        placePhotoMetadata.getPhoto(getGoogleClient()).setResultCallback(new ResultCallback<PlacePhotoResult>() {
                                                                                                @Override
                                                                                                public void onResult(@NonNull PlacePhotoResult placePhotoResult) {
                                                                                                    Bitmap bitmap = placePhotoResult.getBitmap();
                                                                                                    saveBitmap(bitmap, visitItem.getPlaceId());
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

    private void saveBitmap(Bitmap bitmap, String placeId) {
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

    private int convertFromDpToPx(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        int pixels = (int) (dp * scale + 0.5f);
        return pixels;
    }

    private void makeToast(String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }
}