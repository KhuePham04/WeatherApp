package com.example.weatherapp;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.widget.SearchView;
import android.widget.TextView;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.airbnb.lottie.LottieAnimationView;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView tempTextView;
    private TextView humidityTextView;
    private TextView windSpeedTextView;
    private TextView sunRiseTextView;
    private TextView sunSetTextView;
    private TextView seaLevelTextView;
    private TextView conditionTextView;
    private TextView maxTempTextView;
    private TextView minTempTextView;
    private TextView dayTextView;
    private TextView dateTextView;
    private TextView cityNameTextView;
    private LottieAnimationView lottieAnimationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tempTextView = findViewById(R.id.temperature);
        humidityTextView = findViewById(R.id.humidity);
        windSpeedTextView = findViewById(R.id.windSpeed);
        sunRiseTextView = findViewById(R.id.sunrise);
        sunSetTextView = findViewById(R.id.sunset);
        seaLevelTextView = findViewById(R.id.sea);
        conditionTextView = findViewById(R.id.condition);
        maxTempTextView = findViewById(R.id.maxTemp);
        minTempTextView = findViewById(R.id.minTemp);
        dayTextView = findViewById(R.id.day);
        dateTextView = findViewById(R.id.date);
        cityNameTextView = findViewById(R.id.cityName);
        lottieAnimationView = findViewById(R.id.lottieAnimationView);


        fetchWeatherData("Regina");
        SearchCity();
    }

    private void SearchCity() {
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fetchWeatherData(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle text change if needed
                return true;
            }
        });
    }

    private void fetchWeatherData(String cityName) {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .build();

        AppInterface appInterface = retrofit.create(AppInterface.class);

        Call<WeatherData> call = appInterface.getWeatherData(cityName, "ec38d452785c391ddf9e00fc57e44ba5", "metric");
        call.enqueue(new Callback<WeatherData>() {
            @Override
            public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherData responseBody = response.body();

                    WeatherData.Main main = responseBody.getMain();
                    double temperature = main.getTemp();
                    int humidity = main.getHumidity();
                    double windSpeed = responseBody.getWind().getSpeed();
                    int sunRise = responseBody.getSys().getSunrise();
                    int sunSet = responseBody.getSys().getSunset();
                    int seaLevel = main.getPressure();
                    String condition = responseBody.getWeather().length > 0 ? responseBody.getWeather()[0].getMain() : "unknown";
                    double maxTemp = main.getTemp_max();
                    double minTemp = main.getTemp_min();

                    // Update your TextViews
                    tempTextView.setText(String.format("%s °C", temperature));
                    humidityTextView.setText(String.format("%s %%", humidity));
                    windSpeedTextView.setText(String.format("%s m/s", windSpeed));
                    sunRiseTextView.setText(String.valueOf(sunRise));
                    sunSetTextView.setText(String.valueOf(sunSet));
                    seaLevelTextView.setText(String.format("%s hPa", seaLevel));
                    conditionTextView.setText(condition);
                    maxTempTextView.setText(String.format("Max Temp: %s °C", maxTemp));
                    minTempTextView.setText(String.format("Min Temp: %s °C", minTemp));

                    dayTextView.setText(dayName(System.currentTimeMillis()));
                    dateTextView.setText(date());
                    cityNameTextView.setText(cityName);

                    changeImageAccordingToWeather(condition);

                } else {
                    // Handle error
                    Log.e(TAG, "Error in response");
                }
            }

            @Override
            public void onFailure(Call<WeatherData> call, Throwable t) {
                // Handle failure
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    private void changeImageAccordingToWeather(String conditions) {
        Log.d(TAG, "Weather condition received: " + conditions);

        conditions = conditions.toLowerCase(); // Convert to lowercase for case-insensitive comparison

        // Set the condition TextView text dynamically
        conditionTextView.setText(conditions.toUpperCase());

        if (conditions.contains("clear") || conditions.contains("sunny")) {
            Log.d(TAG, "Setting sunny background");
            findViewById(R.id.constraintLayout).setBackgroundResource(R.drawable.sunny_background);
            lottieAnimationView.setAnimation(R.raw.sun);
        } else if (conditions.contains("haze") || conditions.contains("partly clouds") || conditions.contains("clouds")
                || conditions.contains("overcast") || conditions.contains("mist") || conditions.contains("foggy")) {
            Log.d(TAG, "Setting cloudy background");
            findViewById(R.id.constraintLayout).setBackgroundResource(R.drawable.cloud_background);
            lottieAnimationView.setAnimation(R.raw.cloud);
        } else if (conditions.contains("light rain") || conditions.contains("drizzle") || conditions.contains("moderate rain")
                || conditions.contains("showers") || conditions.contains("heavy rain") || conditions.contains("rain")) {
            Log.d(TAG, "Setting rainy background");
            findViewById(R.id.constraintLayout).setBackgroundResource(R.drawable.rain_background);
            lottieAnimationView.setAnimation(R.raw.rain);
        } else if (conditions.contains("light snow") || conditions.contains("moderate snow") || conditions.contains("heavy snow")
                || conditions.contains("blizzard") || conditions.contains("snow")) {
            Log.d(TAG, "Setting snowy background");
            findViewById(R.id.constraintLayout).setBackgroundResource(R.drawable.snow_background);
            lottieAnimationView.setAnimation(R.raw.snow);
        }

        lottieAnimationView.playAnimation();
    }



    private String date() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String dayName(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
